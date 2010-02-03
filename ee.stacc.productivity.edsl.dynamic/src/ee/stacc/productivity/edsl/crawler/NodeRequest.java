package ee.stacc.productivity.edsl.crawler;

/**
 * Tells {@link NodeSearchEngine} which methods to look for and which arguments to process for each method
 * 
 * @author abreslav
 *
 */
public final class NodeRequest {
	private final String patternString;
	private final int argumentIndex;
	
	public NodeRequest(String className, String methodName, int argumentIndex) {
		this.patternString = className + "." + methodName;
		this.argumentIndex = argumentIndex;
	}
	
	/**
	 * 
	 * @param methodSignature a signature in the format <pre>[type .] name [(argTypes)]</pre>
	 * @return true if methodSignature matches this request, false otherwise
	 */
	public boolean signatureMatches(String methodSignature) {
		if (methodSignature.contains("(")) {
			throw new IllegalArgumentException("Argument types are not supported");
		}
		return methodSignature.endsWith(patternString);
	}
	
	/**
	 * @return a string that will be used to create {@link org.eclipse.jdt.core.search.SearchPattern}
	 * @see {@link org.eclipse.jdt.core.search.SearchPattern#createPattern(String, int, int, int)}
	 */
	public String getPatternString() {
		return patternString;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}
	
	@Override
	public String toString() {
		return "Argument " + getArgumentIndex() + " of " + getPatternString();
	}
}