package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import ee.stacc.productivity.edsl.string.IAbstractString;

/**
 * Helper class for using Java SearchEngine
 */
public class ArgumentFinder {

	static public List<IAbstractString> findArgumentAbstractValuesAtCallSites(
			final String className, final String methodName, 
			final int argNo, IJavaElement searchScope, final int level) {
		System.out.println("ArgumentFinder for " + className + "." + methodName + "(" + argNo + ")");
		
		if (level > 3) {
			throw new UnsupportedStringOpEx("argument searching level too deep");
		}
		
		SearchPattern pattern = SearchPattern.createPattern(methodName, 
				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
		IJavaElement[] elems = {searchScope};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems);
		
		final List<IAbstractString> result = new ArrayList<IAbstractString>();
		
		SearchRequestor requestor = new SearchRequestor() {
			ICompilationUnit cUnit;
			ASTParser parser;
			ASTNode ast;
			int unsuppCount = 0;
			int exceptionalCount = 0;
			
			
			public void acceptSearchMatch(SearchMatch match) {
				if (match.getElement() instanceof IMethod) {
					IMethod method = (IMethod)match.getElement();
					
					//System.out.println(method);
					if (! method.getCompilationUnit().equals(cUnit)) { // new unit, need new AST
						if (cUnit == null) { // first time
							parser = ASTParser.newParser(AST.JLS3);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setResolveBindings(true);
						}
						cUnit = method.getCompilationUnit();
						parser.setSource(cUnit);
						parser.setResolveBindings(true);
						ast = parser.createAST(null);
						
					}
					ASTNode node = NodeFinder.perform(ast, match.getOffset(), match.getLength());
					
					if (node instanceof MethodInvocation) {
						MethodInvocation invoc = (MethodInvocation)node;
						IMethodBinding methodBinding = invoc.resolveMethodBinding();
						if (!methodBinding.getDeclaringClass().getQualifiedName().equals(className)
								&& /* FIXME */ !"prepareStatement".equals(methodName)) {
							System.err.println("Wrong match, want: " + className + "." + methodName
									+ ", was: " + methodBinding.getDeclaringClass().getQualifiedName());
							return;
						}
						
						
						ASTNode arg = (ASTNode) invoc.arguments().get(argNo-1);
						if (arg instanceof Expression) {
							System.out.println("# FOUND : " + getLineNumber(match, arg));
							// todo: get astr
							try {
								IAbstractString aStr = AbstractStringEvaluator.getValOf((Expression)arg, level);
//								System.out.println("ASTR   : " + aStr);
								result.add(aStr);
							} 
							catch (UnsupportedStringOpEx e) {
								System.out.println("_______________________________");
								try {
									System.out.println("File   : " + method.getUnderlyingResource());
								} catch (Exception _e) {_e.printStackTrace();}
								
								System.out.println("Arg    : " + arg + ", type: " + arg.getClass());
								System.out.println("Line   : " + getLineNumber(match, arg));
								System.out.println("Unsupp : " + e.getMessage());
								unsuppCount++;
							}
							catch (Throwable e) {
								System.err.println("_______________________________");
								try {
									System.err.println("File   : " + method.getUnderlyingResource());
								} catch (Exception _e) {_e.printStackTrace();}
								
								System.err.println("Arg    : " + arg + ", type: " + arg.getClass());
								System.err.println("Offset : " + match.getOffset());
								System.err.println("Line   : " + getLineNumber(match, arg));
								System.err.println("ERR    : " + e.getMessage());
								exceptionalCount++;
							}
						}
					}
				}
			}
			
			public void endReporting() {
				System.out.println("UNSUPP count: " + unsuppCount);
				System.out.println("EXEPTI count: " + exceptionalCount);
			}
		};
		
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(
				pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, 
				scope,
				requestor,
				null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	static int getLineNumber(SearchMatch match, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(match.getOffset());
		}
		else {
			return -1;
		}
			
	}

}
