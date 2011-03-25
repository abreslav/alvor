package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.cache.FileRecord;
import com.zeroturnaround.alvor.cache.PatternRecord;
import com.zeroturnaround.alvor.common.FieldPattern;
import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.StringPattern;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

/**
 * This class combines services from SearchEngine, Crawler and Cache to update Cache
 * and updates Cache
 * 
 * Observations about speed of searching:
 *   * searching with SearchPattern.createSearchPattern(IJavaElement, ...) is 2x faster than
 *     with SearchPattern.createSearchPattern(String, ...)
 *   * Or-ing several patterns together(with IJavaElement version) doesn't make search noticeably slower
 *   * Searching with scope=IJavaProject vs scope=allFilesInJavaProject seems equally fast
 */
public class StringCollector {
	private static final ILog LOG = Logs.getLog(StringCollector.class);
	private static final int MAX_ITERATIONS_FOR_FINDING_FIXPOINT = 7;
	private Map<StringPattern, SearchPattern> searchPatterns = new HashMap<StringPattern, SearchPattern>();
	private SearchEngine searchEngine = new SearchEngine();
	private Map<ICompilationUnit, ASTNode> astCache = new WeakHashMap<ICompilationUnit, ASTNode>();
	private Cache cache = CacheProvider.getCache();
	
	public static void updateProjectCache(IProject project, Cache cache, IProgressMonitor monitor) {
		StringCollector collector = new StringCollector(cache);
		collector.doUpdateProjectCache(project, monitor);
	}
	
	private StringCollector(Cache cache) {
		this.cache = cache;		
	}
	
	private void doUpdateProjectCache(IProject project, IProgressMonitor monitor) {
		// 0) TODO update inter-project stuff (import patterns)
		
		if (!cache.projectIsInitialized(project.getName())) {
			updateProjectPrimaryPatterns(project);
			populateCacheWithFilesInfo(project);
		}
		
		Timer timer = new Timer("loop");
		for (int i = 0; i < MAX_ITERATIONS_FOR_FINDING_FIXPOINT; i++) {
			cache.startNextBatch();
			List<PatternRecord> patternRecords = cache.getNewProjectPatterns(project.getName());
			if (patternRecords.isEmpty()) { // found fixpoint
				break; 
			}
			else {
				updateProjectCacheForNewPatterns(JavaModelUtil.getJavaProjectFromProject(project),
						patternRecords, monitor);
			}
		}
		timer.printTime();
	}
	
	private void updateProjectPrimaryPatterns(IProject project) {
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		cache.setProjectPrimaryPatterns(project.getName(), conf.getHotspotPatterns());
	}
	
	private void updateProjectCacheForNewPatterns(IJavaProject javaProject, 
			final Collection<PatternRecord> patternRecords, IProgressMonitor monitor) {
		
		List<FileRecord> fileRecords = cache.getFilesToUpdate(javaProject.getProject().getName());
		
		// group files into batches and search each batch separately according to their needed patterns
		Map<Integer, Set<ICompilationUnit>> fileGroups = groupFiles(fileRecords);
		
		for (Map.Entry<Integer, Set<ICompilationUnit>> group : fileGroups.entrySet()) {
			SearchPattern searchPattern = createCombinedSearchPattern(javaProject, patternRecords, group.getKey());
			Timer searchTimer = new Timer("Search");
			final Collection<IResource> files = new HashSet<IResource>();
			final AtomicInteger count = new AtomicInteger(0);
			performSearch(group.getValue(), searchPattern, new SearchRequestor() {
				@Override
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					//System.out.println(match.getElement().getClass().getCanonicalName());
					files.add(match.getResource());
					
					// TODO parse everything together
					
					ASTNode node = getASTNode(match);
					//System.out.println(node.getClass());
					count.incrementAndGet();
					processNodeForPatterns(node, patternRecords);
				}
			});
			
			searchTimer.printTime();
			System.out.println("Match count=" + count);
			System.out.println(files);
			System.out.println("File count=" + files.size());
		}
		
		// TODO should be more granular
		cache.markFilesAsCurrent(fileRecords);
	}
	
	private void processNodeForPatterns(ASTNode node, Collection<PatternRecord> patterns) {
		// detect which of the patterns this node is found for
		
		assert !patterns.isEmpty();
		
		if (node instanceof MethodInvocation) {
			processInvocationNodeForPatterns((MethodInvocation)node, patterns);
		}
		else if (node instanceof SimpleName && node.getParent() instanceof MethodDeclaration) {
			SimpleName name = (SimpleName)node;
			MethodDeclaration decl = (MethodDeclaration) name.getParent();
			processMethodDeclarationNodeForPatterns(name, decl, patterns);
		}
		else if (node instanceof SimpleName && node.getParent() instanceof VariableDeclarationFragment) {
			SimpleName name = (SimpleName)node;
			VariableDeclarationFragment decl = (VariableDeclarationFragment) name.getParent();
			processFieldDeclarationForPatterns(name, decl, patterns);
		}
		else {
			LOG.error("unexpected search match: " + node.getClass() 
					+ ", parent node: " + node.getParent().getClass());
		}
		
	}
	
	private void processFieldDeclarationForPatterns(SimpleName name,
			VariableDeclarationFragment decl, Collection<PatternRecord> patterns) {
		boolean foundMatch = false;
		String className = decl.resolveBinding().getDeclaringClass().getQualifiedName();
		
		for (PatternRecord rec : patterns) {
			StringPattern pattern = rec.getPattern();
			
			if (pattern instanceof FieldPattern
					&& pattern.getClassName().equals(className)
					&& ((FieldPattern)pattern).getFieldName().equals(name.getIdentifier())) {
				foundMatch = true;
				HotspotDescriptor desc = Crawler2.INSTANCE.evaluateFinalField(decl);
				cache.addHotspot(rec, desc);
			}
		}
		
		if (!foundMatch) {
			LOG.error("Couldn't match search result with patterns, result=" + decl);
		}
	}

	private void processInvocationNodeForPatterns(MethodInvocation inv, Collection<PatternRecord> patterns) {
		// TODO merge with processMethodDeclarationNodeForPatterns
		boolean foundMatch = false;
		IMethodBinding binding = inv.resolveMethodBinding();
		String className = binding.getDeclaringClass().getQualifiedName();
		String argumentTypes = ASTUtil.getSimpleArgumentTypesAsString(binding);
		for (PatternRecord rec : patterns) {
			StringPattern pattern = rec.getPattern();
			if (pattern instanceof HotspotPattern
					&& pattern.getMethodName().equals(inv.getName().getIdentifier())
					&& pattern.getClassName().equals(className)
					&& (pattern.getArgumentTypes().equals(argumentTypes) 
							|| pattern.getArgumentTypes().equals("*"))) {
				foundMatch = true;
				processHotspot(inv, rec);
			}
		}
		
		if (!foundMatch) {
			LOG.error("Couldn't match search result with patterns, result=" + inv);
		}
	}
	
	private void processMethodDeclarationNodeForPatterns(SimpleName name, MethodDeclaration decl, Collection<PatternRecord> patterns) {
		// TODO merge with processInvocationNodeForPatterns
		boolean foundMatch = false;
		IMethodBinding binding = decl.resolveBinding();
		String className = binding.getDeclaringClass().getQualifiedName();
		String argumentTypes = ASTUtil.getSimpleArgumentTypesAsString(binding);
		for (PatternRecord rec : patterns) {
			StringPattern pattern = rec.getPattern();
			if (pattern instanceof FunctionPattern
					&& pattern.getMethodName().equals(name.getIdentifier())
					&& pattern.getClassName().equals(className)
					&& pattern.getArgumentTypes().equals(argumentTypes)) {
				foundMatch = true;
				HotspotDescriptor desc = Crawler2.INSTANCE.getMethodTemplateDescriptor
					(decl, rec.getPattern().getArgumentNo());
				cache.addHotspot(rec, desc);
			}
		}
		
		if (!foundMatch) {
			LOG.error("Couldn't match search result with patterns, decl=" + decl.getName());
		}
	}
	
	private void processHotspot(MethodInvocation inv, PatternRecord patternRecord) {
		int argOffset = patternRecord.getPattern().getArgumentNo()-1;
		Expression node = (Expression)inv.arguments().get(argOffset);
		HotspotDescriptor desc = Crawler2.INSTANCE.evaluate(node, Crawler2.ParamEvalMode.AS_HOTSPOT);
		
		cache.addHotspot(patternRecord, desc);
		
		// TODO temporary
		if (desc instanceof StringNodeDescriptor) {
			System.out.println(((StringNodeDescriptor)desc).getAbstractValue());
		}
		else if (desc instanceof UnsupportedNodeDescriptor) {
			System.out.println(((UnsupportedNodeDescriptor)desc).getProblemMessage());
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	private void performSearch(Collection<ICompilationUnit> units, SearchPattern pattern, 
			SearchRequestor requestor) {
		
		try {
			IJavaElement[] elements = units.toArray(new ICompilationUnit[units.size()]);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
			SearchParticipant[] participants = { SearchEngine.getDefaultSearchParticipant()};
			
			// TODO do I want to use monitor?
			searchEngine.search(pattern, participants, scope, requestor, null);
		}
		catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	private SearchPattern createCombinedSearchPattern(IJavaProject javaProject,
			Collection<PatternRecord> patternRecords, int baseBatchNo) {
		SearchPattern resultPattern = null; 
		for (PatternRecord rec : patternRecords) {
			if (rec.getBatchNo() > baseBatchNo) {
				
				try {
					SearchPattern subPattern = getSearchPattern(javaProject, rec.getPattern());
					if (subPattern == null) {
						LOG.error("Hotspot pattern (" + rec.getPattern() + ") is not valid");
					}
					else if (resultPattern == null) {
						resultPattern = subPattern;
					}
					else {
						resultPattern = SearchPattern.createOrPattern(subPattern, resultPattern);
					}
				}
				catch (RuntimeException e) {
					LOG.error("Failed creating search pattern for: " + rec.getPattern(), e);
					throw e;
				}
			}
		}
		return resultPattern;
	}
	
	private Map<Integer, Set<ICompilationUnit>> groupFiles(List<FileRecord> fileRecords) {
		Map<Integer, Set<ICompilationUnit>> groups = new HashMap<Integer, Set<ICompilationUnit>>();
		for (FileRecord rec: fileRecords) {
			Set<ICompilationUnit> set = groups.get(rec.getBatchNo());
			if (set == null) {
				set = new HashSet<ICompilationUnit>();
				groups.put(rec.getBatchNo(), set);
			}
			set.add(JavaModelUtil.getCompilationUnitByName(rec.getName()));
		}
		
		return groups;
	}
	
	
	private void populateCacheWithFilesInfo(IProject project) {
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits
		(JavaModelUtil.getJavaProjectFromProject(project), false);
		List<String> files = JavaModelUtil.getCompilationUnitNames(units);
		cache.addFiles(project.getName(), files);
	}

	private SearchPattern getSearchPattern(IJavaProject javaProject, StringPattern stringPattern) {
		
		SearchPattern searchPattern = searchPatterns.get(stringPattern);
		
		if (searchPattern == null) {
			
			if (stringPattern instanceof FieldPattern) {
				searchPattern = createFieldSearchPattern(javaProject, (FieldPattern)stringPattern);
			}
			
			else {
				int limitTo;
				if (stringPattern instanceof HotspotPattern) {
					limitTo = IJavaSearchConstants.REFERENCES;
				}
				else if (stringPattern instanceof FunctionPattern) {
					limitTo = IJavaSearchConstants.DECLARATIONS;
				}
				else {
					throw new IllegalArgumentException();
				}
				
				Collection<IMethod> methods = findPatternMethods(javaProject, stringPattern);
				searchPattern = createCombinedMethodSearchPattern(methods, limitTo);
			}
			
			searchPatterns.put(stringPattern, searchPattern);
		}
		
		return searchPattern;		
	}
	
	
	private SearchPattern createFieldSearchPattern(IJavaProject javaProject, FieldPattern pattern) {
		try {
			IType type = javaProject.findType(pattern.getClassName());
			if (type == null) {
				throw new IllegalArgumentException("Can't find type for: " + pattern.getClassName());
			}
			IField field = type.getField(pattern.getFieldName());
			if (field == null) {
				throw new IllegalArgumentException("Can't find field: " + pattern.getFieldName());
			}
			return SearchPattern.createPattern(field, IJavaSearchConstants.DECLARATIONS);			
		}
		catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	private List<IMethod> findPatternMethods(IJavaProject javaProject, StringPattern pattern) {
		try {
			IType type = javaProject.findType(pattern.getClassName());
			if (type == null) {
				throw new IllegalArgumentException("Can't find type for: " + pattern.getClassName());
			}
			
//			System.out.println("LOOKING FOR: " + pattern.getClassName() 
//					+ "." + pattern.getMethodName()
//					+ "(" + pattern.getArgumentTypes() + ")");
			
			List<IMethod> result = new ArrayList<IMethod>();
			for (IMethod method: type.getMethods()) {
				if (method.getElementName().equals(pattern.getMethodName())) {
					
					// all compatible methods
					if (pattern.getArgumentTypes().equals("*")) {
						if (pattern.getArgumentNo() == -1) {
							// TODO check that result is string
							result.add(method);
						}
						else {
							// TODO check that has enough arguments and respective argument type is string
							result.add(method);
						}
					}
					
					// if specific method is required, then type signatures must match
					else {
						String methodArgs = ASTUtil.getSimpleArgumentTypesAsString(method);
//						System.out.println("COMPARING: (" + pattern.getMethodName() 
//								+ ") "+ methodArgs + " vs. " + pattern.getArgumentTypes());
						if (methodArgs.equals(pattern.getArgumentTypes())) {
							result.add(method);
						}
					}
				}
			}
			return result;
		} 
		catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private SearchPattern createCombinedMethodSearchPattern(Collection<IMethod> methods, int limitTo) {
		SearchPattern combinedPattern = null;
		for (IMethod method : methods) {
			SearchPattern pattern = SearchPattern.createPattern(method, limitTo);
			if (combinedPattern == null) {
				combinedPattern = pattern;
			}
			else {
				combinedPattern = SearchPattern.createOrPattern(pattern, combinedPattern);
			}
		}
		
		return combinedPattern;
	}
	
	
	private ASTNode getASTNode(SearchMatch match) {
		
		assert match.getElement() instanceof IMember;
		ICompilationUnit unit = ((IMember)match.getElement()).getCompilationUnit();
		
		ASTNode ast = astCache.get(unit); 
		if (ast == null) {
			ast = ASTUtil.parseCompilationUnit(unit, true);
			if (astCache.size() > 100) {
				astCache.clear();
			}
			astCache.put(unit, ast);
		}
		
		return NodeFinder.perform(ast, match.getOffset(), match.getLength());
	}
}
