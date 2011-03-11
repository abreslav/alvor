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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
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

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.cache.FileRecord;
import com.zeroturnaround.alvor.cache.PatternRecord;
import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
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
	private static final int MAX_ITERATIONS_FOR_FINDING_FIXPOINT = 5;
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
			cache.startNewBatch();
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
			System.out.println("Count=" + count);
			System.out.println(files);
			System.out.println("FileCount=" + files.size());
		}
		
		// TODO should be more granular
		cache.markFilesAsCurrent(fileRecords);
	}
	
	private void processNodeForPatterns(ASTNode node, Collection<PatternRecord> patterns) {
		// detect which of the patterns this node is found for
		
		assert !patterns.isEmpty();
		
		boolean foundMatch = false;
		for (PatternRecord rec : patterns) {
			
			if (node instanceof MethodInvocation 
					&& rec.getPattern() instanceof HotspotPattern
					&& ((MethodInvocation) node).getName().getIdentifier().equals(rec.getPattern().getMethodName())
					) {
				foundMatch = true;
				processHotspot((MethodInvocation)node, rec);
			}
			else {
				System.out.println("processNode: " + node.getClass());
			}
		}
		
		if (!foundMatch) {
			LOG.error("Couldn't match node with patterns, node class=" + node.getClass());
			//throw new UnsupportedStringOpExAtNode("Couldn't match node with patterns", node);
		}
	}
	
	private void processHotspot(MethodInvocation inv, PatternRecord patternRecord) {
		int argOffset = patternRecord.getPattern().getArgumentNo()-1;
		Expression node = (Expression)inv.arguments().get(argOffset);
		HotspotDescriptor desc = Crawler2.INSTANCE.evaluate(node);
		
		if (desc instanceof StringNodeDescriptor) {
			System.out.println(((StringNodeDescriptor)desc).getAbstractValue());
		}
		else if (desc instanceof UnsupportedNodeDescriptor) {
			System.out.println(((UnsupportedNodeDescriptor)desc).getProblemMessage());
		}
		else {
			System.out.println("WHAAAT?");
		}
		cache.addHotspot(patternRecord, desc);
		
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
		cache.addFiles(project.getName(), JavaModelUtil.getCompilationUnitNames(units));
	}

	private SearchPattern getSearchPattern(IJavaProject javaProject, StringPattern stringPattern) {
		
		SearchPattern searchPattern = searchPatterns.get(stringPattern);
		if (searchPattern == null) {
			
			if (stringPattern instanceof HotspotPattern) {
				Collection<IMethod> methods = findHotspotMethods(javaProject,
						stringPattern.getClassName(),
						stringPattern.getMethodName(),
						stringPattern.getArgumentNo());
				searchPattern = createCombinedMethodReferencePattern(methods);
			}
			else if (stringPattern instanceof FunctionPattern) {
				throw new IllegalStateException("What now?"); // TODO
			}
			searchPatterns.put(stringPattern, searchPattern);
		}
		
		return searchPattern;		
	}
	
	private List<IMethod> findHotspotMethods(IJavaProject javaProject, String className, String methodName, 
			int stringArgumentNo) {
		int stringArgIndex = stringArgumentNo-1;
		try {
			IType type = javaProject.findType(className);
			List<IMethod> result = new ArrayList<IMethod>();
			for (IMethod method: type.getMethods()) {
				if (method.getElementName().equals(methodName)) {
					String[] paramTypes = method.getParameterTypes();
					if (paramTypes.length > stringArgIndex && (
							paramTypes[stringArgIndex].equals("Ljava.lang.String;")
							|| paramTypes[stringArgIndex].equals("Qjava.lang.String;")
							|| paramTypes[stringArgIndex].equals("QString;")
							)) {
						result.add(method);
					}
				}
			}
			return result;
		} 
		catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
	
	private SearchPattern createCombinedMethodReferencePattern(Collection<IMethod> methods) {
		SearchPattern combinedPattern = null;
		for (IMethod method : methods) {
			SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
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
