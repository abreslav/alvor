package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Implements some Java searches + Node searches required by AbstractStringEvaluator
 * @author Aivar
 *
 */
public class NodeSearchEngine {
	
	private static Map<ICompilationUnit, ASTNode> astCache = 
		new HashMap<ICompilationUnit, ASTNode>();
	private static IJavaElement[] scopeElems = {null};
	
	public static void clearCache() {
		astCache.clear();
	}
	
	public static List<NodeDescriptor> findArgumentNodes(IJavaElement searchScope, final Collection<NodeRequest> requests) {
		// No requests -- no results
		if (requests.isEmpty()) {
			return Collections.emptyList();
		}
		
		//System.out.println("SEARCH SCOPE: " + searchScope);
		
		final List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();

		// FIXME temporary, to speed up a bit -- must not be a problem any more
		/*
		if (methodName.equals("get")) {
			return new ArrayList<NodeSearchResult>();
		}
		*/
		
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
		System.out.println(pattern);
		
		scopeElems[0] = searchScope;
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
				
				// TODO: check that object has that type -- not needed, the type is in the patterns
				
				if (methodBinding == null || methodBinding.getDeclaringClass() == null) {
					System.err.println("TODO: crawler methodBinding.getDeclaringClass() == null");
					return;
				}
				
				// Find a request corresponding to the current match
				// TODO: Bad assumption: only one argument for each method
				int requestedArgumentIndex = -1;
				for (NodeRequest request : requests) {
						String signature =
							methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + 
							methodBinding.getMethodDeclaration().getName();
						if (request.signatureMatches(signature)) {
							requestedArgumentIndex = request.getArgumentIndex();
							break;
						}
				}
				
				if (requestedArgumentIndex < 0) {
					System.err.println("No matching request found for method: " + methodBinding);
					return;
				}
				
// This code should not be needed: everything is encoded in the pattern				
//				if (!methodBinding.getDeclaringClass().getQualifiedName().equals(typeName)
//						&& /* FIXME */ !"prepareStatement".equals(methodName)) {
					/*
					System.out.println("Wrong match, want: " + typeName + "." + methodName
							+ ", was: " + methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
					*/
//					return;
//				}
				
				// TODO overloading may complicate things -- no, patterns support complete signatures
				if (invoc.arguments().size() < requestedArgumentIndex) {
					throw new UnsupportedStringOpEx("can't find required argument (" + requestedArgumentIndex + "), method="
							+ methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
				}
				
				ASTNode arg = (ASTNode) invoc.arguments().get(requestedArgumentIndex - 1);
				if (arg instanceof Expression) {
					result.add(new NodeDescriptor((Expression)arg, 
							(IFile)match.getResource(), 
							getNodeLineNumber(match, arg),
							arg.getStartPosition(),
							arg.getLength()));
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
	
	public static List<MethodDeclaration> findMethodDeclarations(final MethodInvocation inv) {
		System.out.println("FIND METHOD DECL: " + inv);
//		ITypeBinding objectType = inv.getExpression().resolveTypeBinding();
		final List<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
		
		SearchPattern pattern = SearchPattern.createPattern(
				inv.getName().getIdentifier(), IJavaSearchConstants.METHOD, 
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(null,//scopeElems,
				IJavaSearchScope.SOURCES);		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match);
				MethodDeclaration decl = (MethodDeclaration)node.getParent();
				if (declarationIsCompatibleWithInvocation(decl, inv)) {
					result.add(decl);
					/*
					System.err.println("decl " + inv.getName().getIdentifier() 
							+ " in " + match.getResource()
							+ ", offset=" + match.getOffset());
					*/
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		
		return result;
		//throw new UnsupportedStringOpEx("Crawler.getMethodDeclarations, expr type = "
		//		+ objectType.getName());
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
	
	private static boolean declarationIsCompatibleWithInvocation
		(MethodDeclaration decl, MethodInvocation inv) {
		ITypeBinding declType = getContainingTypeDeclaration(decl).resolveBinding();
		ITypeBinding invExprType = inv.getExpression().resolveTypeBinding();
		
		// TODO take subtyping into account
		/*
		if (!declType.isEqualTo(invExprType)) {
			System.err.println("not compatible, decl=: " + declType.getQualifiedName()
					+ ", invExp=" + invExprType.getQualifiedName());
		}
		*/
		return declType.isEqualTo(invExprType);
	}
	
	static TypeDeclaration getContainingTypeDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof TypeDeclaration)) {
			result = result.getParent();
		}
		return (TypeDeclaration)result;
	}
	
	private static int getNodeLineNumber(SearchMatch match, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(match.getOffset());
		}
		else {
			return -1;
		}
	}}