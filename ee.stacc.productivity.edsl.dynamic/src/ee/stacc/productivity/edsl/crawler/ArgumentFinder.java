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
import org.eclipse.jdt.core.dom.Expression;
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
public class ArgumentFinder extends SearchRequestor {

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		// TODO Auto-generated method stub

	}
	
	/**
	 * @param methodName
	 * @param argNo
	 * @param searchScope eg. project or file 
	 * @return
	 */
	List<Expression> findArgumentNodesFor(String methodName, final int argNo, IJavaElement searchScope) {
		SearchPattern pattern = SearchPattern.createPattern(methodName, 
				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
		IJavaElement[] elems = {searchScope};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems);
		
		final List<Expression> result = new ArrayList<Expression>();
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				if (match.getElement() instanceof IMethod) {
					ICompilationUnit cUnit = ((IMethod)match.getElement()).getCompilationUnit(); 
					// TODO tutorial recommends to hold only one AST at a time
					ASTParser parser = ASTParser.newParser(AST.JLS3);
					parser.setKind(ASTParser.K_COMPILATION_UNIT); // TODO maybe it's more efficent to parse only method body
					parser.setSource(cUnit);
					parser.setResolveBindings(true);
					ASTNode ast = parser.createAST(null);
					ASTNode node = NodeFinder.perform(ast, match.getOffset(), match.getLength());
					
					if (node instanceof MethodInvocation) {
						MethodInvocation invoc = (MethodInvocation)node;
						/*
						System.out.println();
						System.out.println(match.getResource());
						System.out.println(match.getOffset());
						System.out.println(match.getLength());
						System.out.println("----------");
						*/
						ASTNode arg = (ASTNode) invoc.arguments().get(argNo-1);
						if (arg instanceof Expression) {
							result.add((Expression)arg);
						}
					}
				}
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

	List<IAbstractString> findArgumentAbstractValues(String methodName, final int argNo, IJavaElement searchScope) {
		SearchPattern pattern = SearchPattern.createPattern(methodName, 
				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
		IJavaElement[] elems = {searchScope};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems);
		
		final List<IAbstractString> result = new ArrayList<IAbstractString>();
		
		
		SearchRequestor requestor = new SearchRequestor() {
			ICompilationUnit cUnit = null;
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			ASTNode ast = null;
			
			public void acceptSearchMatch(SearchMatch match) {
				if (match.getElement() instanceof IMethod) {
					IMethod method = (IMethod)match.getElement();
					
					//System.out.println(method);
					if (! method.getCompilationUnit().equals(cUnit)) { // new unit, need new AST
						try {
							System.err.println("_______________________________");
							System.err.println("#### NEW UNIT: " + method.getUnderlyingResource());
						} catch (Exception e) {}
						if (cUnit == null) { // first time
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setResolveBindings(true);
						}
						cUnit = method.getCompilationUnit();
						parser.setSource(cUnit);
						ast = parser.createAST(null);
						
					}
					ASTNode node = NodeFinder.perform(ast, match.getOffset(), match.getLength());
					
					if (node instanceof MethodInvocation) {
						MethodInvocation invoc = (MethodInvocation)node;
						ASTNode arg = (ASTNode) invoc.arguments().get(argNo-1);
						if (arg instanceof Expression) {
							// todo: get astr
							System.err.println("_______________________________");
							System.err.println("Offset2: " + match.getOffset());
							try {
								System.err.println("ARG is: " + arg + ", type: " + arg.getClass());
								IAbstractString aStr = AbstractStringEvaluator.getValOf((Expression)arg);
								System.err.println("ASTR: " + aStr);
								result.add(aStr);
							} 
							catch (Throwable e) {
								System.err.println("EXCEPTION: " + e.getMessage());
							}
						}
					}
				}
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

}
