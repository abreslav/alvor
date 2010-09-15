package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.cache.ICacheService;
import com.zeroturnaround.alvor.cache.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Measurements;
import com.zeroturnaround.alvor.string.IPosition;

/**
 * Implements some Java searches + Node searches required by AbstractStringEvaluator
 * @author Aivar
 *
 */
public class NodeSearchEngine {
	private static final ILog LOG = Logs.getLog(NodeSearchEngine.class);
	
	private static final CachedSearcher<NodeRequest, IPosition> ARGUMENT_NODES_SEARCHER = 
			new CachedSearcher<NodeRequest, IPosition>() {

		@Override
		protected void performSearchInScope(List<IJavaElement> scopeToSearchIn,
				NodeRequest key, List<? super IPosition> values) {
			NodeSearchEngine.performArgumentSearchInScope(key, scopeToSearchIn, values);
			
		}
	};
	
	private static Map<ICompilationUnit, ASTNode> astCache = 
		new WeakHashMap<ICompilationUnit, ASTNode>();
	
	public static void clearASTCache() {
		astCache.clear();
	}
	
	public static void removeASTFromCache(IFile file) {
		astCache.remove(JavaCore.createCompilationUnitFrom(file));
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
			assert LOG.message("Request " + nodeRequest);
			
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
				if (resource.getType() == IResource.FILE
						// TODO check this
						&& "java".equals(resource.getFileExtension())) {
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

	private static void performArgumentSearchInScope(
			final NodeRequest nodeRequest, List<IJavaElement> scopeToSearchIn,
			final List<? super IPosition> result) {
		
		int searchFor = IJavaSearchConstants.METHOD;
		if (nodeRequest.getMethodName().equals("new")) {
			searchFor = IJavaSearchConstants.CONSTRUCTOR;
		}
		SearchPattern pattern = SearchPattern.createPattern(
				nodeRequest.getPatternString(), searchFor,
				IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_ERASURE_MATCH | SearchPattern.R_CASE_SENSITIVE);
		assert LOG.message(pattern);
				
		IJavaElement[] elems = scopeToSearchIn.toArray(new IJavaElement[scopeToSearchIn.size()]);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems, IJavaSearchScope.SOURCES);
		
//		if (nodeRequest.getPatternString().startsWith("getProductInfoValue")) {
//			System.out.println("stop");
//			IJavaElement[] elems2 = {elems[0].getJavaProject()};
//			scope = SearchEngine.createJavaSearchScope(elems2, IJavaSearchScope.SOURCES);
//			for (IJavaElement el : scopeToSearchIn) {
//				System.out.println(el.getElementName());
//			}
//		}
		
		SearchRequestor requestor = new SearchRequestor() {
			@SuppressWarnings("rawtypes")
			public void acceptSearchMatch(SearchMatch match) {
				assert match.getElement() instanceof IMethod;
				
				ASTNode node = getASTNode(match);
				
				List arguments = null;
				ASTNode invoc = null;
				IMethodBinding methodBinding = null;
				String signature = null;
				
				if (node instanceof ClassInstanceCreation) {
					arguments = ((ClassInstanceCreation)node).arguments();
					methodBinding = ((ClassInstanceCreation)node).resolveConstructorBinding();
				}
				else if (node instanceof MethodInvocation) {
					arguments = ((MethodInvocation)node).arguments();
					methodBinding = ((MethodInvocation)node).resolveMethodBinding();
				}
				else { // eg. SuperMethodInvocation
					return; // FIXME should do smth about it
				}
				
				if (methodBinding == null || methodBinding.getDeclaringClass() == null) {
					LOG.error("TODO: crawler methodBinding.getDeclaringClass() == null");
					return;
				}
				signature = methodBinding.getDeclaringClass().getQualifiedName()
					+ "." + methodBinding.getMethodDeclaration().getName();
				
				if (node instanceof MethodInvocation 
						&& !nodeRequest.signatureMatches(signature)) {
					assert LOG.message("Signature does not match: " + methodBinding);
					return;
				}					
				
				// TODO overloading may complicate things 
				int requestedArgumentIndex = nodeRequest.getArgumentIndex();
				if (arguments.size() < requestedArgumentIndex) {
					LOG.error("can't find required argument (" + requestedArgumentIndex + "), method="
							+ methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
					return;
				}
				

				ASTNode arg = (ASTNode) arguments.get(requestedArgumentIndex - 1);
				if (arg instanceof Expression) {
					IPosition position = PositionUtil.getPosition(arg);
					result.add(position);
					assert LOG.message("PATTERN=" + nodeRequest.getPatternString()
							+ ", accepted match=" + PositionUtil.getLineString(position)
							+ ", invocation=" + invoc);
					CacheService.getCacheService().getHotspotCache().add(nodeRequest, position);
				}
			}
		};
		
		Measurements.argumentSearchTimer.start();
		int resultStartSize = result.size();
		executeSearch(pattern, requestor, scope);
		Measurements.argumentSearchTimer.stop();
		
		assert LOG.message("Searched callsites of '" + nodeRequest.getPatternString() + "', found "
				+ (result.size()-resultStartSize) + " matches");
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
		
		String patternStr = inv.getName().getIdentifier()
			+ ASTUtil.getArgumentTypesString(inv.resolveMethodBinding());
		
		assert LOG.message("findMethodDeclarations: " + patternStr);
		
		if (inv.getName().getIdentifier().contains("getSequenceNextValueFunction")
				|| inv.getName().getIdentifier().contains("getNextValueSQL")) {
			System.out.println("stop");
		}
		
		SearchPattern pattern = SearchPattern.createPattern(
				patternStr, 
				IJavaSearchConstants.METHOD, 
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				searchScope, IJavaSearchScope.SOURCES);
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match); // gives SimpleName 
				MethodDeclaration decl = (MethodDeclaration)node.getParent();
				if (ASTUtil.invocationMayUseDeclaration(inv, decl)) {
					result.add(decl);
				}
			}
		};
		
		try {
			executeSearch(pattern, requestor, scope);
		} catch (Exception e) {
			LOG.error("SEARCH ERROR: " + e.getMessage() + ", pattern=" + patternStr);
			throw new IllegalStateException(e);
		}
		return result;
	}
	
	static public List<String> findMethodInvocations(IJavaElement[] searchScope, 
			final String signature) {
		
		final List<String> result = new ArrayList<String>();
		
		SearchPattern pattern = SearchPattern.createPattern(
				signature, 
				IJavaSearchConstants.METHOD, 
				IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH  | SearchPattern.R_CASE_SENSITIVE
				);
		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match);
				result.add(node.toString());
				//result.add(PositionUtil.getPosition(node));
			}
		};
		
		executeSearch(pattern, requestor, elementsToProjectSearchScope(searchScope));
		return result;
	}
	
	private static IJavaSearchScope elementsToProjectSearchScope(IJavaElement[] elements) {
		Set<IJavaProject> projects = new HashSet<IJavaProject>();
		for (IJavaElement element : elements) {
			projects.add(element.getJavaProject());
		}
		
		return SearchEngine.createJavaSearchScope(
				projects.toArray(new IJavaElement[projects.size()]), IJavaSearchScope.SOURCES);		
	}
	
	public static VariableDeclarationFragment findFieldDeclarationFragment
			(IJavaElement[] searchScope, String qualifiedName) {
		//LOG.message("Searching for: " + qualifiedName);
		SearchPattern pattern = SearchPattern.createPattern(
				qualifiedName, IJavaSearchConstants.FIELD, 
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				searchScope, IJavaSearchScope.SOURCES);		
		
		final List<VariableDeclarationFragment> result = new ArrayList<VariableDeclarationFragment>();
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match); // node is SimpleName
				result.add((VariableDeclarationFragment)node.getParent());
			}
		};
		
		Measurements.methodDeclSearchTimer.start();
		executeSearch(pattern, requestor, scope);
		Measurements.methodDeclSearchTimer.stop();
		
		if (result.size() != 1) {
			throw new UnsupportedStringOpEx("findFieldDeclarationFragment: " +
					" name=" + qualifiedName + ", result.size=" + result.size());
		}
		return result.get(0);
	}
	
	/*package*/ public static ASTNode getASTNode(IPosition position) {
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
			
			Measurements.parseTimer.start();
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(cUnit);
			ast = parser.createAST(null);
			Measurements.parseTimer.stop();
			checkClearCache(20);
			astCache.put(cUnit, ast);
		}
		return NodeFinder.perform(ast, start, length);
	}
	
	private static void checkClearCache(int freeMBsRequired) {
		if (astCache.size() > 30 && getAvailableMemory() < freeMBsRequired * 1024 * 1024) {
			astCache.clear();
			System.err.println("Cleaning ast cache");
		}
	}
	
	private static long getAvailableMemory() {
		Runtime rt = Runtime.getRuntime();
		long used = rt.totalMemory() - rt.freeMemory();
		return rt.maxMemory() - used;
	}
}
