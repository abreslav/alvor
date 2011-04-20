package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
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
import com.zeroturnaround.alvor.common.ProgressUtil;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.StringPattern;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
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
	private final Cache cache;
	
	public static void updateProjectCache(IProject project, IProgressMonitor monitor) {
		StringCollector collector = new StringCollector(CacheProvider.getCache(project.getName()));
		collector.doUpdateProjectCache(project, monitor);
	}
	
	private StringCollector(Cache cache) {
		this.cache = cache;		
	}
	
	private void doUpdateProjectCache(IProject project, IProgressMonitor monitor) {
		try {
			int workLeft = 100; 
			int work;
			ProgressUtil.beginTask(monitor, "Collecting strings", workLeft);
			
			if (!cache.projectHasFiles()) {
				initializeProject(project);
			}
			
			Timer timer = new Timer("Cache updating time");
			int i = 0;
			while (i < MAX_ITERATIONS_FOR_FINDING_FIXPOINT) {
				LOG.message("ITERATION " + i);
				ProgressUtil.checkAbort(monitor);
				
				List<PatternRecord> patternRecords = cache.getNewProjectPatterns();
				if (patternRecords.isEmpty()) { // found fixpoint
					break; 
				}
				else {
					work = (int)(workLeft * 0.75);
					IProgressMonitor subMonitor = ProgressUtil.subMonitor(monitor, work);
					if (subMonitor != null) {
						subMonitor.setTaskName("Collecting strings, iteration " + (i+1));
					}
					updateProjectCacheForNewPatterns(JavaModelUtil.getJavaProjectFromProject(project),
							patternRecords, subMonitor);
					workLeft -= work;
				}
				i++;
			}
			if (i == MAX_ITERATIONS_FOR_FINDING_FIXPOINT
					&& ! cache.getNewProjectPatterns().isEmpty()) {
				LOG.error("Fixpoint not found while updating cache");
			}
			timer.printTime();
		} 
		finally {
			
		}
	}
	
	private void initializeProject(IProject project) {
		IJavaProject javaProject = JavaModelUtil.getJavaProjectFromProject(project); 
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits(javaProject, false);
		List<String> files = JavaModelUtil.getCompilationUnitNames(units);
		
		// add also files from required projects
		Set<IJavaProject> reqProjects = JavaModelUtil.getAllRequiredProjects(javaProject);
		for (IJavaProject p : reqProjects) {
			units = JavaModelUtil.getAllCompilationUnits(p, false);
			files.addAll(JavaModelUtil.getCompilationUnitNames(units));
		}
		
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		cache.initializeProject(conf.getHotspotPatterns(), files);
	}
	
	private void updateProjectCacheForNewPatterns(IJavaProject javaProject, 
			final List<PatternRecord> patternRecords, final IProgressMonitor monitor) {
		
		List<FileRecord> fileRecords = cache.getFilesToUpdate();
		
		// group files into batches and search each batch separately according to their needed patterns
		Map<Integer, Set<ICompilationUnit>> fileGroups = groupFiles(fileRecords);
		
		for (Map.Entry<Integer, Set<ICompilationUnit>> group : fileGroups.entrySet()) {
			List<PatternRecord> groupPatterns = filterPatternRecords(patternRecords, group.getKey());
			LOG.message("SEARCH " + group.getValue().size() 
					+ " files with batch_no=" + group.getKey() 
					+ " for " + groupPatterns.size() + " patterns");
			SearchPattern searchPattern = createCombinedSearchPattern(javaProject, groupPatterns);
			
			if (searchPattern != null) { // can be null after code renames (when old patterns don't apply anymore, but aren't cleaned yet)  
				Timer searchTimer = new Timer("Search time");
				performSearch(group.getValue(), searchPattern, new SearchRequestor() {
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						ProgressUtil.checkAbort(monitor);
						ASTNode node = getASTNode(match);
						processNodeForPatterns(node, patternRecords);
					}
				}, monitor);
				
				searchTimer.printTime();
			}
		}
		
		cache.updateFilesBatchNo(fileRecords, getMaxPatternBatchNo(patternRecords));
	}
	
	private int getMaxPatternBatchNo(Collection<PatternRecord> patternRecords) {
		int batchNo = 0;
		for (PatternRecord rec : patternRecords) {
			batchNo = Math.max(rec.getBatchNo(), batchNo);			
		}
		return batchNo;
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
				HotspotDescriptor desc = StringExpressionEvaluator.INSTANCE.evaluateFinalField(decl);
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
				HotspotDescriptor desc = StringExpressionEvaluator.INSTANCE.getMethodTemplateDescriptor
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
		HotspotDescriptor desc = StringExpressionEvaluator.INSTANCE.evaluate(node, StringExpressionEvaluator.ParamEvalMode.AS_HOTSPOT);
		
		cache.addHotspot(patternRecord, desc);
		
		if (desc instanceof StringHotspotDescriptor) {
			assert LOG.message("processHotspot: " + ((StringHotspotDescriptor)desc).getAbstractValue());
		}
		else if (desc instanceof UnsupportedHotspotDescriptor) {
			assert LOG.message("processHotspot: " + ((UnsupportedHotspotDescriptor)desc).getProblemMessage());
		}
	}

	private void performSearch(Collection<ICompilationUnit> units, SearchPattern pattern, 
			SearchRequestor requestor, IProgressMonitor monitor) {
		
		assert pattern != null;
		
		try {
			IJavaElement[] elements = units.toArray(new ICompilationUnit[units.size()]);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
			SearchParticipant[] participants = { SearchEngine.getDefaultSearchParticipant()};
			
			searchEngine.search(pattern, participants, scope, requestor, monitor);
		}
		catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<PatternRecord> filterPatternRecords(List<PatternRecord> patternRecords, int baseBatchNo) {
		List<PatternRecord> result = new ArrayList<PatternRecord>();
		for (PatternRecord rec : patternRecords) {
			if (rec.getBatchNo() > baseBatchNo) {
				result.add(rec);
			}
		}
		return result;
	}
	
	private SearchPattern createCombinedSearchPattern(IJavaProject javaProject,
			List<PatternRecord> patternRecords) {
		SearchPattern resultPattern = null; 
		for (PatternRecord rec : patternRecords) {
			try {
				SearchPattern subPattern = getSearchPattern(javaProject, rec.getPattern());
				if (subPattern == null) {
					// probably the class or method is deleted or renamed
					if (rec.isPrimaryPattern()) {
						LOG.error("Primary hotspot pattern (" + rec.getPattern() + ") is not valid");
					}
				}
				else {
					if (resultPattern == null) {
						resultPattern = subPattern;
					}
					else {
						resultPattern = SearchPattern.createOrPattern(subPattern, resultPattern);
					}
				}
			}
			catch (RuntimeException e) {
				LOG.error("Failed creating search pattern for: " + rec.getPattern(), e);
				throw e;
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
	
	
	private SearchPattern getSearchPattern(IJavaProject javaProject, StringPattern stringPattern) {
		
		// TODO maybe shouldn't cache patterns, because after renames the methods are not there anymore
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
				if (methods.isEmpty()) {
					return null;
				}
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
				return Collections.emptyList();
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
