package de.mpg.aai.shhaa.authz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthAttributes;


/**
 * implementation of {@link Rule}, represents an attribute-require rule, 
 * as defined in the config file,
 * <p>syntax:<br /> 
 * {@code <require match="case|nocase|regex" id="attribute-name">attribute-value</require> }<br />
 * {@code <miss match="case|nocase|regex" id="attribute-name">attribute-value</require> }
 * </p>
 * 
 * <ul>attributes:
 * <li>match - mode to match target: see {@link Matcher} </li>
 * <li>id - name of the attribute (which's value have to match)</li>
 * </ul>
 * attribute-value: the expected value to match (in order to {@link #evaluate(AuthAttributes)} true 
 * @see Matcher
 * 
 * @author megger
 */
public class Requirement extends Matcher implements Rule {
	/** the logger */
	private static Logger	log = LoggerFactory.getLogger(Requirement.class);
	/** this rules evaluation-mode (require or miss), true means MISS, default false */
	private boolean modeMiss;
	/** the attribute name */
	private String	id;
	/** the attribute value to check on (require|miss) */
	private String	value;
	
	
	/**
	 * constructor, initializes this requirement with given attribute name and expected/required value
	 * @param name name of the attribute to check 
	 * @param val required value of the attribute (to evaluate true)
	 */
	public Requirement(String name, String val) {
		this(name, val, Matcher.MATCHMODE_CASE_SENSITIVE, false);		// default to require-mode
	}
	/**
	 * constructor, initializes this requirement with given attribute name, its expected/required value
	 * and this rules evaluation-mode (require or miss)
	 * @param name name of the attribute to check 
	 * @param val required value of the attribute (to evaluate true)
	 * @param MISSmode this rules evaluation-mode (require or miss), true means MISS
	 * @see #evaluate(AuthAttributes)
	 * @see #modeMiss
	 */
	public Requirement(String name, String val, String matchMode, boolean MISSmode) {
		super(matchMode);
		this.modeMiss = MISSmode;
		this.id = name.trim();
		this.value = val.trim();
	}
	
	
	/**
	 * determines whether the given credentials (attributes) meet the requirements of this instance;
	 * <div>
	 * the behavior depends strongly on the value of {@link #modeMiss}:
	 * <ul>
	 * <li>false (default) - means REQUIRED: evaluate to true if one of the given attributes matches the requirement,
	 * means (one of its) values must match {@link #value} according to this instance's {@link Matcher#matchMode}</li>
	 * <li>true - means MISS: expects the attribute(-value) NOT to be there/to be missed! 
	 * evaluates to true if NONE of the given attributes matches</li> 
	 * </ul>
	 * </div>
	 * @see Rule#evaluate(AuthAttributes)
	 */
	@Override
	public boolean evaluate(AuthAttributes credentials) {
		// on hit:
		//	mode require -> return true
		//	mode miss    -> false
		AuthAttribute<?> attb = credentials.get(this.id);
		if(attb == null)	// attribute not found -> no hit
			return this.modeMiss;	// mode required -> false, mode-miss -> true

		// found: check values
		for(Object val : attb.getValues()) {
			if(!(val instanceof String))
//				throw new IllegalArgumentException("expecting string attribute, found " + val.getClass().getName());
				log.error("found non-string attribute value of class '{}', trying #toString");
			if(this.matches(this.value, val.toString()))
				return !this.modeMiss;	// hit: mode-req -> true, mode-miss -> false
		}
		// no hits 
		return this.modeMiss;	// mode required -> false, mode-miss -> true
	}
	
	
	/**
	 * NOT SUPPORTED! => throws always UnsupportedOperationException
	 * did'nt want to create separate Rule interface for this only class & method
	 * just to differ from all the other rules
	 * @throws UnsupportedOperationException always
	 */
	@SuppressWarnings("unused")
	@Override
	public void addRule(Rule rule) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("#addRule(Rule) not supported, Requirement can't take child rules");
	}
	
	
	/**{@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.modeMiss ? "Miss: " : "Require: ");
		result.append(this.id);
		result.append(" '").append(this.value).append("'");
		return result.toString();
	}
}
