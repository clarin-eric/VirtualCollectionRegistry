package de.mpg.aai.shhaa.authz;

import de.mpg.aai.shhaa.model.AuthAttributes;

/**
 * implementation of {@link Rule}, represents a location-authorization rule, as
 * defined in the config file,
 * <p>
 * syntax:
 * {@code <location match="case|nocase|regex" target="location-expression">}</p>
 * <div>
 * sample config:
 * <pre>
 * {@code
 *		<location match="regex" target="/protected/.*" >
 *			<rule logic="AND">
 *				<require match="regex" id="attribute-A">value-x</require>
 *				<rule logic="OR">
 *					<require id="attribute-B">value-a</require>
 *					<require id="attribute-B">value-b</require>
 *					<miss id="attribute-C">value-u</miss>
 *				</rule>
 *				<miss id="attribute-C">value-v</miss>
 *			</rule>
 *		</location>
 * }
 * <pre></div>
 *
 * a location rule always expect a logic-rule as first child,
 * if not found it reverts to {@code <rule logic="AND">}
 *
 * <ul>attributes:
 * <li>match - mode to match target: see {@link Matcher} </li>
 * <li>target - expression to match (according to match-mode)</li>
 * </ul>
 * @see Matcher
 * @author megger
 *
 */
public class Location extends Matcher implements Rule {

    /**
     * the target this rule is destined for
     */
    private String target;
    /**
     * the http method this rule is destined for
     */
    private String[] methods;
    /**
     * the conditions applying for this target: required attributes needed for
     * access grant
     */
    private LogicRule topRule;

    /**
     * constructor, initializes this Location with the given target("path")
     * expression, matching any method
     *
     * @param path the location expression to match (with the request's destined
     * location)
     * @param mode intended match mode to match (with the request's destined
     * location)
     */
    public Location(String path, String mode) {
        this(path, mode, new String[]{});
    }

    public Location(String path, String[] methods) {
        this(path, Matcher.MATCHMODE_CASE_SENSITIVE, methods);
    }

    /**
     * constructor, initializes this Location with the given target("path")
     * expression and the intended match-mode, matching the specified method(s)
     *
     * @param path the location expression to match (with the request's destined
     * location)
     * @param mode intended match mode to match (with the request's destined
     * location)
     * @param methods http methods (space separated) to match (empty string for
     * all)
     * @see Matcher#Matcher(String)
     * @see Matcher#MATCHMODE_CASE_SENSITIVE
     * @see Matcher#MATCHMODE_CASE_IGNORE
     * @see Matcher#MATCHMODE_REGEX
     */
    public Location(String path, String mode, String[] methods) {
        super(mode);
        this.target = path.trim();
        this.methods = methods;
    }

    /**
     * @return the expression to match
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * determines whether this location matches with the given path
     *
     * @param path to (try/check to) match
     * @return true if given path matches this location's {@link #target}
     * according to its {@link Matcher#matchMode}
     */
    public boolean matchesPath(String path) {
        return super.matches(this.target, path);
    }

    /**
     *
     * @param method to match
     * @return true if the list of accepted methods is empty or one of the items
     * matches the specified method
     */
    public boolean matchesMethod(String method) {
        if (this.methods.length == 0) {
            return true;
        } else {
            for (String acceptedMethod : methods) {
                if (acceptedMethod.equalsIgnoreCase(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * determines whether the given credentials are sufficient to grant access
     * to this location according to its (internal child) rules
     * <div>{@inheritDoc}</div>
     */
    @Override
    public boolean evaluate(AuthAttributes credentials) {
        return this.topRule != null
                ? this.topRule.evaluate(credentials)
                : true;	// no rules at all => validate true
    }

    /**
     * checks whether this location rule has actually any child(rule)s set
     *
     * @return false if no child(rule)s
     */
    public boolean isEmpty() {
        return this.topRule == null || this.topRule.isEmpty();
    }

    /**
     * <div>{@inheritDoc}</div>
     * note: this instance expects a {@link LogicRule} as first-top child, if
     * given rule isn't one, it internally creates one (in 'AND' mode) and adds
     * the given rule to it
     *
     * @see LogicRule
     */
    @Override
    public void addRule(Rule rule) {
        if (this.topRule != null) {
            this.topRule.addRule(rule);
            return;
        }
        // need logic-rule as root: 
        // if not there yet: use given LogicRule
        if (rule instanceof LogicRule) {
            this.topRule = (LogicRule) rule;
            return;
        }
        // ...else default to AND if not there
        this.topRule = new LogicRule(false);
        this.topRule.addRule(rule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Location(Rule) '").append(this.target).append("'");
        return result.toString();
    }

    public String[] getMethods() {
        return methods;
    }
}
