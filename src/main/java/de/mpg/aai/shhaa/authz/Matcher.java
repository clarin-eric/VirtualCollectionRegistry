package de.mpg.aai.shhaa.authz;


/**
 * abstract base class for implementations of {@link Rule} which have a match-mode attribute
 * @see Location 
 * @see Requirement
 * @author megger
 */
public abstract class Matcher {
	
	/** flag for matching behavior for this target (@see {@link #matches(String)}):<ul>
	 * <li>"case" - default: match case sensitive (recognizes trailing asterisk *)</li>
	 * <li>"nocase" - ignore case on matching (recognizes trailing asterisk *)</li>
	 * <li>"regex" - match as regular expression (treat target pattern as regEx)</li></ul>
	 */
	private String				matchMode;
	/** match case sensitive, default */
	public static final String	MATCHMODE_CASE_SENSITIVE	= "case";
	/** ignore case on matching */
	public static final String	MATCHMODE_CASE_IGNORE		= "nocase";
	/** treat match expression as regular expression */
	public static final String	MATCHMODE_REGEX				= "regex";
	
	
	/**
	 * default constructor, defaults to case-sensitive match mode
	 */
	public Matcher() {
		this(MATCHMODE_CASE_SENSITIVE);	// default always case sensitive
	}
	/**
	 * constructor, initializes with given match-mode
	 * @param mode match-mode to set, must one of defined MATCH_MODE_... constants, see {@link Matcher}
	 * @throws IllegalArgumentException if given match mode unknown
	 * @see #MATCHMODE_CASE_SENSITIVE
	 * @see #MATCHMODE_CASE_IGNORE
	 * @see #MATCHMODE_REGEX
	 */
	public Matcher(String mode) {
		if(mode == null) {
			this.matchMode = MATCHMODE_CASE_SENSITIVE;
			return;
		}
		if(MATCHMODE_CASE_SENSITIVE.equalsIgnoreCase(mode)
		|| MATCHMODE_CASE_IGNORE.equalsIgnoreCase(mode)
		|| MATCHMODE_REGEX.equalsIgnoreCase(mode)) {
			this.matchMode = mode;
			return;
		}
		throw new IllegalArgumentException("match mode must not be null");
	}
	
	
	/**
	 * checks whether the given target matches by the given expression 
	 * @param expression to match the given target by: blank string, with trailing asterisk or as regEx
	 * @param target the string to check 
	 * @return true if expression matches target
	 * @see #matchMode
	 * @see match mode constants
	 * @see #isRegEx()
	 * @see #isIgnoreCase() 
	 */
	protected boolean matches(final String expression, final String target) {
//		String pattern = this.target;
		String pattern = expression;
		String source = target.trim();
		if(this.isRegEx())
			return source.matches(pattern);
		
		if(this.isIgnoreCase()) {
			pattern = pattern.toLowerCase();
			source = source.toLowerCase();
		}
		
		if(pattern.endsWith("*")) {
			pattern = pattern.substring(0, pattern.length()-1);
			if(source.startsWith(pattern))
				return true;
			return false;
		}
		return source.equals(pattern);
	}
	/**
	 * @return the match mode {@link #matchMode}
	 */
	protected String getMachMode() {
		return this.matchMode;
	}
	/**
	 * @return true if match mode is set to regular-expression
	 * @see #MATCHMODE_REGEX 
	 */
	public boolean isRegEx() {
		return MATCHMODE_REGEX.equalsIgnoreCase(this.matchMode);
	}
	/**
	 * @return true if match mode is set to ignore-case
	 * @see #MATCHMODE_CASE_IGNORE
	 */
	public boolean isIgnoreCase() {
		return MATCHMODE_CASE_IGNORE.equalsIgnoreCase(this.matchMode);
	}
}
