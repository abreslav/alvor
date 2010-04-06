package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
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

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.cache.ICacheService;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.IPosition;

/**
 * Implements some Java searches + Node searches required by AbstractStringEvaluator
 * @author Aivar
 *
 */
public class NodeSearchEngine {
	private static final ILog LOG = Logs.getLog(NodeSearchEngine.class);
	
	private static final CachedSearcher<NodeRequest, IPosition> ARGUMENT_NODES_SEARCHER = new CachedSearcher<NodeRequest, IPosition>() {

		@Override
		protected void performSearchInScope(List<IJavaElement> scopeToSearchIn,
				NodeRequest key, List<? super IPosition> values) {
			NodeSearchEngine.performSearchInScope(key, scopeToSearchIn, values);
			
		}

	};
	
	private static Map<ICompilationUnit, ASTNode> astCache = 
		new HashMap<ICompilationUnit, ASTNode>();
	
	public static void clearCache() {
		astCache.clear();
	}
	
	public static List<IPosition> findArgumentNodes(IJavaElement[] scope, final Collection<NodeRequest> requests) {
		// No requests -- no results
		if (requests.isEmpty()) {
			return Collections.emptyList();
		}
		
		//LOG.message("SEARCH SCOPE: " + searchScope);

		final ICacheService cacheService = CacheService.getCacheService();
		final List<IFile> allFilesInScope = getAllFilesInScope(scope);

		final List<IPosition> result = new ArrayList<IPosition>();
		for (NodeRequest nodeRequest : requests) {
			LOG.message("Request " + nodeRequest);
			
			ARGUMENT_NODES_SEARCHER.performCachedSearch(
					allFilesInScope, 
					cacheService.getHotspotCache(), 
					nodeRequest, result);
		}
		return result;
	}

	/*package*/ static List<IFile> getAllFilesInScope(IJavaElement[] searchScope) {
		final List<IFile> allFilesInScope = new ArrayList<IFile>();
		IResourceVisitor visitor = new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource.isPhantom() || resource.isHidden() || resource.isTeamPrivateMember()) {
					return false;
				}
				if (resource.getType() == IResource.FILE) {
					allFilesInScope.add((IFile) resource);
				}
				return true;
			}
		};
		for (IJavaElement scopeElement : searchScope) {
			IResource resource = scopeElement.getResource();
			try {
				resource.accept(visitor);
			} catch (CoreException e) {
				LOG.exception(e);
				e.printStackTrace();
			}
		}
		
		return allFilesInScope;
	}

	private static void performSearchInScope(
			final NodeRequest nodeRequest, List<IJavaElement> scopeToSearchIn,
			final List<? super IPosition> result) {
		SearchPattern pattern = SearchPattern.createPattern(nodeRequest.getPatternString(), 
				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_ERASURE_MATCH | SearchPattern.R_CASE_SENSITIVE);
		LOG.message(pattern);
				
		IJavaElement[] elems = scopeToSearchIn.toArray(new IJavaElement[scopeToSearchIn.size()]);
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
				if (!nodeRequest.signatureMatches(signature)) {
					LOG.error("Signature does not match: " + methodBinding);
					return;
				}					
				
				// TODO overloading may complicate things -- no, patterns support complete signatures
				int requestedArgumentIndex = nodeRequest.getArgumentIndex();
				if (invoc.arguments().size() < requestedArgumentIndex) {
					LOG.error("can't find required argument (" + requestedArgumentIndex + "), method="
							+ methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
					return;
				}
				

				ASTNode arg = (ASTNode) invoc.arguments().get(requestedArgumentIndex - 1);
				if (arg instanceof Expression) {
					IPosition position = PositionUtil.getPosition(arg);
					result.add(position);
					CacheService.getCacheService().getHotspotCache().add(nodeRequest, position);
//					CacheService.getCacheService().addHotspot(nodeRequest, position);
				}
			}

		};
		
		executeSearch(pattern, requestor, scope);
	}

	private static void executeSearch(SearchPattern pattern, SearchRequestor requestor,
			IJavaSearchScope scope) {
		System.out.println("SEARCH");
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, 
				scope, requestor, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static List<MethodDeclaration> findMethodDeclarations(IJavaElement[] searchScope, final MethodInvocation inv) {
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
		
		Set<IJavaProject> projects = new HashSet<IJavaProject>();
		for (IJavaElement element : searchScope) {
			projects.add(element.getJavaProject());
		}
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				projects.toArray(new IJavaElement[projects.size()]), IJavaSearchScope.SOURCES);		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match); // gives SimpleName (IIRC)
				MethodDeclaration decl = (MethodDeclaration)node.getParent();
				if (ASTUtil.invocationMayUseDeclaration(inv, decl)) {
					result.add(decl);
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		return result;
	}
	
	public static VariableDeclarationFragment findFieldDeclarationFragment
			(IJavaElement[] searchScope, String qualifiedName) {
		//LOG.message("Searching for: " + qualifiedName);
		SearchPattern pattern = SearchPattern.createPattern(
				qualifiedName, IJavaSearchConstants.FIELD, 
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				searchScope, IJavaSearchScope.SOURCES);		
		
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
	
	/*package*/ static ASTNode getASTNode(IPosition position) {
		IFile file = PositionUtil.getFile(position);
		ICompilationUnit cUnit = JavaCore.createCompilationUnitFrom(file);
		int start = position.getStart();
		int length = position.getLength();
		
		if (cUnit == null) {
			System.err.println("Compilation unit is null for the position: " + position);
		}
		return getASTNode(cUnit, start, length);
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
		int start = match.getOffset();
		int length = match.getLength();
		
		if (cUnit == null) {
			System.err.println("Compilation unit is null for the match: " + match);
		}
		return getASTNode(cUnit, start, length);
	}

	private static ASTNode getASTNode(ICompilationUnit cUnit, int start, int length) {
		assert cUnit != null;
		
		ASTNode ast = astCache.get(cUnit);
		if (ast == null || cUnit.hasResourceChanged()) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(cUnit);
			ast = parser.createAST(null);
			astCache.put(cUnit, ast);
		}
		return NodeFinder.perform(ast, start, length);
	}
}
