package de.mpg.aai.shhaa.authz;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import de.mpg.aai.shhaa.model.AuthAttributes;


/**
 * implementation of {@link Rule}, represents a logic rule,
 * as defined in the config file, see {@link Location};
 * <p>syntax: {@code <rule logic="AND|OR">}</p>
 * the attribute 'logic' specifies the behavior of how this rule's child rules ({@link #childRules} 
 * are interpreted during {@link #evaluate(AuthAttributes)}:
 * <ul><li>AND - all (child)rules need to match (themselves evaluate to true) to evaluate (this) to true</li>
 * <li>OR - evaluate true as soon one (child)rule evaluates true</li>
 * @author megger
 */
public class LogicRule implements Rule {
	/** flag indicating the OR/AND behavior (true means OR) */
	private boolean		modeOR;
	/** list holding the child rules of this */
	private List<Rule>	childRules;
	
	
	/**
	 * constructor to init the logic mode of this Logic-Rule:  AND | OR
	 * @param ORmode if true this Logic-Rule evaluates its internal rules in OR-mode 
	 */
	public LogicRule(boolean ORmode) {
		this.modeOR = ORmode;
	}
	
	/**
	 * constructor to init the logic mode of this Logic-Rule:  AND | OR
	 * @param logicMode the destined mode as string, is parsed to boolean: {@link #parseLogicMode(String)}
	 * @throws IllegalArgumentException if logicMode could not be parsed 
	 * @see #parseLogicMode(String)
	 */
	public LogicRule(String logicMode) {
		Boolean mode = parseLogicMode(logicMode);
		if(mode == null)
			throw new IllegalArgumentException("could not parse logic-mode, exptected 'AND', 'OR', got " + logicMode);
		this.modeOR = mode.booleanValue();
	}
	
	
	/**
	 * checks allowed values as logic-mode
	 * @param value expects 'OR', 'AND'
	 * @return true if mode is 'OR' (case insensitive), false if 'AND', null for any other value (= could not parse) 
	 * @throws  
	 */
	private static Boolean parseLogicMode(String value) {
		if(value == null || value.isEmpty())
			return false;
		if(value.equalsIgnoreCase("OR"))
			return true;
		if(value.equalsIgnoreCase("AND"))
			return false;
		return null;
	}
	
	/**
	 * determines whether this rule actually has any child rules set
	 * @return true if no child rules there
	 */
	public boolean isEmpty() {
		return this.childRules == null || this.childRules.isEmpty();
	}
	
	/**
	 * <div>{@inheritDoc}</div>
	 * see class comment for AND/OR evaluation behavior {@link LogicRule}
	 * @see LogicRule
	 */
	@Override
	public boolean evaluate(AuthAttributes credentials) {
		boolean result = false;
		for(Rule rule : this.childRules) {
			result = rule.evaluate(credentials);
			// OR-mode? => return on first hit
			if(this.modeOR) {
				 if(result)
					 return result;
				 continue;	// or try next
			} 
			// else = AND-mode => return of first fail
			if(!result)
				return result;
			// and continue with next
		}
		// not returned yet 
		// 	OR-mode  => no hits = fail
		//	AND-mode => no fails = success  
		return result;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void addRule(Rule rule) {
		if(this.childRules == null)
			this.childRules = Collections.synchronizedList(new Vector<Rule>());
		this.childRules.add(rule);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("LogicRule[").append(this.modeOR ? "OR" : "AND").append("] ");
		return result.toString();
	}
}
