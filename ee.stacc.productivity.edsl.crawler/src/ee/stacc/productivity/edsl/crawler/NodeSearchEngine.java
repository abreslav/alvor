package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;

/**
 * Implements some Java searches + Node searches required by AbstractStringEvaluator
 * @author Aivar
 *
 */
public class NodeSearchEngine {
	private static final ILog LOG = Logs.getLog(NodeSearchEngine.class);
	
	private static Map<ICompilationUnit, ASTNode> astCache = 
		new HashMap<ICompilationUnit, ASTNode>();
	
	public static void clearCache() {
		astCache.clear();
	}
	
	public static List<NodeDescriptor> findArgumentNodes(IJavaElement searchScope, final Collection<NodeRequest> requests) {
		// No requests -- no results
		if (requests.isEmpty()) {
			return Collections.emptyList();
		}
		
		//LOG.message("SEARCH SCOPE: " + searchScope);
		
		final List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();

		// Create one big pattern from all the requests
		// NB: This is likely to be faster than searching for each request separately,
		//     but we have to confirm this by and experiment
		SearchPattern pattern = null;
		for (NodeRequest nodeRequest : requests) {
			SearchPattern subPattern = SearchPattern.createPattern(nodeRequest.getPatternString(), 
					IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
					SearchPattern.R_ERASURE_MATCH | SearchPattern.R_CASE_SENSITIVE);
			if (pattern == null) {
				pattern = subPattern;
			} else {
				pattern = SearchPattern.createOrPattern(pattern, subPattern);
			}
		}
		LOG.message(pattern);
		
		IJavaElement[] elems = {searchScope};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems, IJavaSearchScope.SOURCES);
		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				// TODO: Should not be needed -- we look only for methods
				if (! (match.getElement() instanceof IMethod)) {
					return;
				}
				
				ASTNode node = getASTNode(match);
				
				if (! (node instanceof MethodInvocation)) {
					System.err.println("Crawler: not MethodInvocation, but: " + node.getClass());
					return;
				}
				
				MethodInvocation invoc = (MethodInvocation)node;
				IMethodBinding methodBinding = invoc.resolveMethodBinding();
				
				if (methodBinding == null || methodBinding.getDeclaringClass() == null) {
					System.err.println("TODO: crawler methodBinding.getDeclaringClass() == null");
					return;
				}
				
				// Find a request corresponding to the current match
				// TODO: Bad assumption: only one argument for each method
				String signature =
					methodBinding.getDeclaringClass().getQualifiedName()
					+ "." + 
					methodBinding.getMethodDeclaration().getName();
				int requestedArgumentIndex = -1;
				for (NodeRequest request : requests) {
						if (request.signatureMatches(signature)) {
							requestedArgumentIndex = request.getArgumentIndex();
							break;
						}
				}
				
				if (requestedArgumentIndex < 0) {
					System.err.println("No matching request found for method: " + methodBinding);
					return;
				}
				
				// TODO overloading may complicate things -- no, patterns support complete signatures
				if (invoc.arguments().size() < requestedArgumentIndex) {
					throw new UnsupportedStringOpEx("can't find required argument (" + requestedArgumentIndex + "), method="
							+ methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
				}
				
				ASTNode arg = (ASTNode) invoc.arguments().get(requestedArgumentIndex - 1);
				if (arg instanceof Expression) {
					result.add(new NodeDescriptor((Expression)arg, 
							ASTUtil.getNodeLineNumber(match, arg)));
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		return result;
	}
	
	private static void executeSearch(SearchPattern pattern, SearchRequestor requestor,
			IJavaSearchScope scope) {
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, 
				scope, requestor, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static List<MethodDeclaration> findMethodDeclarations(IJavaElement searchScope, final MethodInvocation inv) {
		final List<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
		
		String patternStr = inv.getName().getIdentifier() + "(";
		for (int i = 0; i < inv.arguments().size(); i++) {
			// following works only when argument types are exactly same as parameter types 			
			// patternStr += ((Expression)arg).resolveTypeBinding().getQualifiedName() + ",";
			if (i > 0) {
				patternStr += ',';
			}
			patternStr += "?";
		}
		patternStr += ")";
		
		LOG.message("findMethodDeclarations: " + patternStr);
		
		SearchPattern pattern = SearchPattern.createPattern(
				patternStr, 
				IJavaSearchConstants.METHOD, 
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				new IJavaElement[]{searchScope}, IJavaSearchScope.SOURCES);		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match); // gives SimpleName (IIRC)
				MethodDeclaration decl = (MethodDeclaration)node.getParent();
				if (ASTUtil.invocationMayReferToDeclaration(inv, decl)) {
					result.add(decl);
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		// TODO: if it finds nothing then something's wrong
		return result;
	}
	
	public static VariableDeclarationFragment findFieldDeclarationFragment
			(IJavaElement searchScope, String qualifiedName) {
		//LOG.message("Searching for: " + qualifiedName);
		SearchPattern pattern = SearchPattern.createPattern(
				qualifiedName, IJavaSearchConstants.FIELD, 
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				new IJavaElement[]{searchScope}, IJavaSearchScope.SOURCES);		
		
		final List<VariableDeclarationFragment> result = new ArrayList<VariableDeclarationFragment>();
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match); // node is SimpleName
				result.add((VariableDeclarationFragment)node.getParent());
			}
		};
		
		executeSearch(pattern, requestor, scope);
		
		assert (result.size() == 1);
		return result.get(0);
	}
	
	private static ICompilationUnit getCompilationUnit(SearchMatch match) {
		if (match.getElement() instanceof IMember) {
			return ((IMember)match.getElement()).getCompilationUnit();
		}
		else {
			throw new IllegalArgumentException("getCompilationUnit: Can't find");
		}
	}
	
	private static ASTNode getASTNode(SearchMatch match) {
		ICompilationUnit cUnit = getCompilationUnit(match);
		
		if (cUnit == null) {
			System.err.println("Compilation unit is null for the match: " + match.getElement());
		}
		assert cUnit != null;
		
		ASTNode ast = astCache.get(cUnit);
		// TODO check if AST is still good (or file has been edited after creating AST)
		if (ast == null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(cUnit);
			ast = parser.createAST(null);
			astCache.put(cUnit, ast);
		}
		return NodeFinder.perform(ast, match.getOffset(), match.getLength());
	}
	
}
