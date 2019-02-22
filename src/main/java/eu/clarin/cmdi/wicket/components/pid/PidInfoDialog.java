/*
 * Copyright (C) 2018 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.wicket.components.pid;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.wicket.components.BaseInfoDialog;
import eu.clarin.cmdi.wicket.components.DialogButton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class PidInfoDialog extends BaseInfoDialog {
 
    private final static Logger logger = LoggerFactory.getLogger(PidInfoDialog.class);
    
    private final static String TITLE = "Persistent Identifier information";
    
    private final IModel<PersistentIdentifieable> model;
    
    private final static JavaScriptResourceReference INIT_JAVASCRIPT_REFERENCE = new JavaScriptResourceReference(PidInfoDialog.class, "PidInfoDialog.js");
    
    public class PidResolutionModel extends AbstractReadOnlyModel<String> {
    
        private final String ref;
            
        private transient final ExecutorService executor = Executors.newSingleThreadExecutor();
        private transient Future<String> future;
        private long counter;
        
        public PidResolutionModel (String ref) {
            this.ref = ref;
            this.future = null;
            this.counter = 0;
        }
 
        @Override
        public String getObject() {
            if(future == null) {
                future = resolve();
            }
            
            String result = "Resolving PID";
            for(int i = 0; i < this.counter; i++) {
                result+=".";
            }
            
            try {
                if(future.isDone()) {
                    result = future.get();
                } else if(future.isCancelled()) {
                    result = "Cancelled PID resolution";
                }
            } catch(InterruptedException | ExecutionException ex) {
                result = "Failed to resolve handle. Error: " + ex.getMessage();
            }
            
            this.counter++;
            if(this.counter > 5) {
                this.counter = 0;
            }
                    
            return result;
        }
        
        public Future<String> resolve() {        
            return executor.submit(() -> {
                //Thread.sleep(5000);
                try {
                    String hdlTarget = resolvePid(ref);
                    return hdlTarget;
                } catch(IOException ex) {
                    return "Failed to resolve handle. Error: "+ex.getMessage();
                }                
            });
        }

    }
    
    public PidInfoDialog(String id, final IModel<PersistentIdentifieable> model, String context) {
        super(id, TITLE);
        this.model = model;
        this.build(context);
    }
    
    private void build(String context) {
         List<DialogButton> buttons = Arrays.asList(
                new DialogButton("Close") {
                    @Override
                    public void handleButtonClick(AjaxRequestTarget target) {
                        PidInfoDialog.this.close(target);
                    }
                });
        buildContent(TITLE, new Body(getContentWicketId(), context), buttons);
    }
    
    private class Body extends Panel {
        public Body(String id, String context) {
            super(id);
            String actionableUri = HandleLinkModel.getActionableUri(model.getObject().getPidUri());            
            TextField<String> input = new TextField("pid", new Model(actionableUri));           
            add(input);
            
            PidType type = HandleLinkModel.getPidType(model.getObject().getPidUri());
            switch(type) {
                case DOI: 
                    add(new Label("type", "doi")); break;
                case HANDLE: 
                    add(new Label("type", "handle")); break;
                case NBN: 
                    add(new Label("type", "nbn")); break;
                case UNKOWN: 
                default:
                    add(new Label("type", "Unkown")); break;            
            }
            
            add(new Label("context1", new Model(context)));
            add(new Label("context2", new Model(context)));
            
            Label handleResolutionLabel = new Label("hdl-target", new PidResolutionModel(model.getObject().getPidUri()));
            AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(500));
            handleResolutionLabel.add(timer);
            add(handleResolutionLabel);
        }
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forReference(INIT_JAVASCRIPT_REFERENCE));
    }
    
    private String resolvePid(String uri) throws IOException {
        String result = "Unkown";
        HttpParams httpParams = new BasicHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);
        DefaultHttpClient client = new DefaultHttpClient(httpParams);        

        HttpContext ctx = new BasicHttpContext();
        try {         
            HttpResponse response = client.execute(new HttpGet(uri), ctx);
            StatusLine status = response.getStatusLine();
            if(status.getStatusCode() != 302) {
                result = "Unexpected HTTP response code: "+status.getStatusCode();
            } else {
                Header[] headers = response.getHeaders("Location");
                for(Header h : headers) {            
                    result = h.getValue();
                }
            }
        } finally {
            client.getConnectionManager().shutdown();
        }
        return result;
    }
}
