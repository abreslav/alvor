package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.cache.IHotspotPattern;

/**
 * Tells {@link NodeSearchEngine} which methods to look for and which arguments to process for each method
 * 
 * @author abreslav
 *
 */
public final class NodeRequest implements IHotspotPattern {
	private final String className;
	private final String methodName;
	private final String signatureString;
	private final String signatureStringWithoutArgTypes;
	private final String patternString;
	private final int argumentIndex;
	
	public NodeRequest(String className, String methodName, int argumentIndex) {
		this.patternString = methodName;
		this.signatureString = (!className.isEmpty() ? className + "." : "") + methodName;
		this.argumentIndex = argumentIndex;
		this.className = className;
		this.methodName = methodName;
		if (signatureString.contains("(")) {
			this.signatureStringWithoutArgTypes = 
				signatureString.substring(0, signatureString.indexOf('('));
		}
		else {
			this.signatureStringWithoutArgTypes = signatureString;
		}
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
		
		return methodSignature.endsWith(signatureStringWithoutArgTypes);
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

	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	@Override
	public String toString() {
		return "Argument " + getArgumentIndex() + " of " + getPatternString();
	}
}