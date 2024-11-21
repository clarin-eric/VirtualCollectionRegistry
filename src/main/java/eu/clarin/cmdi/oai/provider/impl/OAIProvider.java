package eu.clarin.cmdi.oai.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Repository;
import eu.clarin.cmdi.oai.provider.ext.Argument;
import eu.clarin.cmdi.oai.provider.ext.OAIErrorCode;
import eu.clarin.cmdi.oai.provider.ext.OAIOutputStream;
import eu.clarin.cmdi.oai.provider.ext.RepositoryAdapter;
import eu.clarin.cmdi.oai.provider.ext.ResumptionToken;
import eu.clarin.cmdi.oai.provider.ext.Verb;
import eu.clarin.cmdi.oai.provider.ext.VerbContext;

public class OAIProvider {
    private static final Logger logger =
        LoggerFactory.getLogger(OAIProvider.class);
    private static final String[] DATEFORMATS_DAYS =
        { "yyyy-MM-dd" };
    private static final String[] DATEFORMATS_FULL =
        { "yyyy-MM-dd'T'HH:mm:ss'Z'" , "yyyy-MM-dd"};
    private static final OAIProvider s_instance = new OAIProvider();
    private final List<Verb> verbs = new ArrayList<Verb>();
    private final Map<String, ResumptionToken> resumptionTokens =
        new HashMap<String, ResumptionToken>(64);
    private Timer timer = new Timer("OAI-Provider-Maintenance", true);
    private AtomicBoolean isAvailable = new AtomicBoolean();
    private RepositoryAdapter repository;

    private OAIProvider() {
        super();
        // register basic verbs
        this.registerVerb(new VerbIdentify());
        this.registerVerb(new VerbListMetadataFormats());
        this.registerVerb(new VerbListIdentifiers());
        this.registerVerb(new VerbListSets());
        this.registerVerb(new VerbListRecords());
        this.registerVerb(new VerbGetRecord());

        // provider maintenance timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (resumptionTokens) {
                    if (resumptionTokens.isEmpty()) {
                        return;
                    }
                    Iterator<ResumptionToken> i =
                        resumptionTokens.values().iterator();
                    while (i.hasNext()) {
                        ResumptionToken token = i.next();
                        synchronized (token) {
                            if (token.checkExpired(scheduledExecutionTime())) {
                                i.remove();
                                logger.debug("resumption token with id '{}' " +
                                             " expired", token.getId());  
                            }
                        } // synchronized (token)
                    } // while
                } // synchronized (resumptionTokens)
            }
        }, 60000, 60000);
    }

    public void setRepository(Repository repository) throws OAIException {
        if (repository == null) {
            throw new NullPointerException("repository == null");
        }
        if (this.repository != null) {
            throw new IllegalStateException("repository is already set");
        }
        logger.debug("setting repository '{}'", repository.getId());
        this.repository = new RepositoryAdapterImpl(this, repository);
        if (!isAvailable.compareAndSet(false, true)) {
            throw new IllegalStateException("unexpected state of isAvailable");
        }
    }

    public boolean isAvailable() {
        return isAvailable.get();
    }

    public void setIsAvailable(boolean value) {
        if (repository != null) {
            isAvailable.set(value);
        }
    }

    public void shutdown() {
        timer.cancel();
    }

    public void process(HttpServletRequest request,
            HttpServletResponse response) throws OAIException {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null) {
            throw new OAIException("provider expects valid ip address");
        }
        if (isAvailable.get()) {
            doProcess(request, response);
        } else {
            response.setHeader("Retry-After", "3600");
            sendHttpResponse(response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The OAI provider is currently not available.");
        }
    }

    private void doProcess(HttpServletRequest request,
            HttpServletResponse response) throws OAIException {
        VerbContextImpl ctx =
            new VerbContextImpl(request, response, repository);

        // process verb argument
        Verb verb = null;
        String verbName = ctx.getParameter("verb");
        if (verbName != null) {
            logger.debug("looking up verb '{}'", verbName);
            if (!ctx.isRepeatedParameter("verb")) {
                for (Verb v : verbs) {
                    if (verbName.equals(v.getName())) {
                        verb = v;
                        break;
                    }
                } // for
                if (verb == null) {
                    ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
                            verbName + "'");
                }
            } else {
                ctx.addError(OAIErrorCode.BAD_VERB, "OAI verb is repeated");
            }
        } else {
            ctx.addError(OAIErrorCode.BAD_VERB, "OAI verb is missing");
        }

        if (verb != null) {
            logger.debug("processing arguments for verb '{}'", verbName);
            ctx.setVerb(verbName);

            // process arguments
            Set<String> remaining = ctx.getParameterNames();

            /*
             *  special handling of resumptionToken, because if it is
             *  available is must be the only argument. 
             */
            if (verb.supportsArgument(DefaultArguments.ARG_RESUMPTIONTOKEN) &&
                remaining.contains(DefaultArguments.ARG_RESUMPTIONTOKEN)) {
                final Argument arg =
                    verb.getArgument(DefaultArguments.ARG_RESUMPTIONTOKEN);
                processArgument(ctx, arg);
                remaining.remove(arg.getName());
            } else {
                // process regular arguments
                Argument[] arguments = verb.getArguments();
                if (arguments != null) {
                    for (Argument arg : verb.getArguments()) {
                        processArgument(ctx, arg);
                        remaining.remove(arg.getName());
                    } // for
                }
            }

            if (!remaining.isEmpty()) {
                logger.debug("received request with illegal arguments");
                for (String key : remaining) {
                    ctx.addError(OAIErrorCode.BAD_ARGUMENT, "OAI verb '" +
                            verb.getName() +
                            "' was submitted with illegal argument '" + key +
                            "' (value='" + ctx.getParameter(key) + "')");
                }
            }

            /*
             * Execute verb, if no error occurred have been recorded.
             */
            if (!ctx.hasErrors()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("processing verb '{}'", verb.getName());
                    Map<String, String> args = ctx.getUnparsedArguments();
                    if (!args.isEmpty()) {
                        int i = 0;
                        for (String name : args.keySet()) {
                            logger.debug("argument[" + i++ + "]: {}='{}'",
                                    name, args.get(name));
                        }
                    }
                }
                verb.process(ctx);
            }
        }

        /*
         * If any errors occurred create a proper response. NOTE: errors may
         * occur, when executing verb, so this block cannot be moved
         */
        if (ctx.hasErrors()) {
            OAIOutputStream out = ctx.getOutputStream();
            for (VerbContext.Error error : ctx.getErrors()) {
                out.writeStartElement("error");
                out.writeAttribute("code",
                        OAIErrorCode.toXmlString(error.getCode()));
                out.writeCharacters(error.getMessage());
                out.writeEndElement(); // error element
            }
            out.close();
        }
    }

    public void registerVerb(Verb verb) {
        if (verb == null) {
            throw new NullPointerException("verb == null");
        }
        if ((verb.getName() == null) || verb.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("verb name is null or empty");
        }
        synchronized (verbs) {
            for (Verb v : verbs) {
                if (verb.getName().equals(v.getName())) {
                    throw new IllegalArgumentException("verb with name '" +
                            verb.getName() + "' has already been registered");
                }
            }
            verbs.add(verb);
        } // synchronized (verbs)
    }

    public static OAIProvider instance() {
        return s_instance;
    }

    ResumptionToken createResumptionToken(long lifetime) {
        long expirationDate = System.currentTimeMillis() +
                ((lifetime > 0) ? lifetime : 600000);
        ResumptionTokenImpl token = new ResumptionTokenImpl();
        token.setExpirationDate(expirationDate);
        synchronized (resumptionTokens) {
            resumptionTokens.put(token.getId(), token);
        } // synchronized (resumptionTokens)
        return token;
    }

    ResumptionToken getResumptionToken(String id, long lifetime) {
        synchronized (resumptionTokens) {
            ResumptionToken token = resumptionTokens.get(id);
            if (token == null) {
                return null;
            }
            synchronized (token) {
                long expirationDate = System.currentTimeMillis() +
                    ((lifetime > 0) ? lifetime : 600000);
                token.setExpirationDate(expirationDate);
                return token;
            } // synchronized (token)
        } // synchronized (resumptionTokens)
    }

    private void processArgument(VerbContextImpl ctx, Argument arg) {
        String value = ctx.getParameter(arg.getName());
        if (value != null) {
            logger.debug("process argument '{}', value = '{}'",
                    arg.getName(), value);
            if (ctx.isRepeatedParameter(arg.getName())) {
                ctx.addError(OAIErrorCode.BAD_ARGUMENT, "OAI verb '" +
                        ctx.getVerb() + "' has repeated values for " +
                        "argument '" + arg.getName() + "'");
            } else {
                if (!setArgument(ctx, arg, value)) {
                    ctx.addError(OAIErrorCode.BAD_ARGUMENT,
                            "Value of argument '" + arg.getName() +
                                    "' of OAI verb '" + ctx.getVerb() +
                                    "' is invalid (value='" + value + "')");
                }
            }
        } else {
            if (arg.isRequired()) {
                ctx.addError(OAIErrorCode.BAD_ARGUMENT,
                        "OAI verb '" + ctx.getVerb() +
                        "' is missing required argument '" +
                        arg.getName() + "'");
            }
        }
    }

    private boolean setArgument(VerbContextImpl ctx, Argument arg,
            String value) {
        if (arg.checkArgument(value)) {
            Object v = parseArgument(arg.getName(), value);
            if (v != null) {
                logger.debug("set: '{}' = {}", arg.getName(), v.toString());
                ctx.setArgument(arg, v);
                return true;
            }
        }
        return false;
    }

    private Object parseArgument(String name, String value) {
        Object result = null;
        if (name.equals(DefaultArguments.ARG_IDENTIFIER)) {
            String localId = extractLocalId(value);
            if (localId != null) {
                result = repository.parseLocalId(localId);
            }
        } else if (name.equals(DefaultArguments.ARG_FROM)
                || name.equals(DefaultArguments.ARG_UNTIL)) {
            try {
                switch (repository.getGranularity()) {
                case DAYS:
                    result =
                        DateUtils.parseDateStrictly(value, DATEFORMATS_DAYS);
                    break;
                default:
                    result =
                        DateUtils.parseDateStrictly(value, DATEFORMATS_FULL);
                }
            } catch (ParseException e) {
                /* ignore */
            }
        } else {
            result = value;
        }
        return result;
    }

    private String extractLocalId(String identifier) {
        int pos1 = identifier.indexOf(':');
        if (pos1 != -1) {
            int pos2 = identifier.indexOf(':', pos1 + 1);
            if (pos2 != -1) {
                // check of repository id matches
                String id = repository.getId();
                if (identifier.regionMatches(pos1 + 1, id, 0, id.length())) {
                    return identifier.substring(pos2 + 1);
                }
            }
        }
        return null;
    }

    private void sendHttpResponse(HttpServletResponse response, int status,
            String message) throws OAIException {
        try {
            response.setStatus(status);
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println(message);
            out.close();
        } catch (IOException e) {
            logger.error("OAI provider error while sending error to client", e);
            throw new OAIException("error", e);
        }
    }
} // class OAIProvider
