package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.junit.Test;

import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class SearchEngineTest {
	
	@Test
	public void testSearchEngine() throws JavaModelException {
		IJavaProject javaProject = JavaModelUtil.getJavaProjectByName("earved");
		IPackageFragmentRoot pfr = javaProject.findPackageFragmentRoot(javaProject.getPath().append("src"));
		
		IJavaElement[] elements = {pfr};
//		IJavaElement[] elements = {javaProject};
		ICompilationUnit[] files = JavaModelUtil.getAllCompilationUnits(pfr, false).toArray(new ICompilationUnit[0]);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(files);  
		
//		javaProject.get
		
//		IType type = javaProject.findType("java.util.List");
//		String[] typSig = {"int"};
//		IMethod method = type.getMethod("get", typSig);
		
		IType type = javaProject.findType("java.sql.Connection");
		String[] paramSignatures = {"Ljava.lang.String;"};
		
		IMethod method = type.getMethod("prepareStatement", paramSignatures);
		IMethod method2 = type.getMethod("prepareCall", paramSignatures);
		
		// org.springframework.jdbc.core.simple.SimpleJdbcTemplate. queryForInt
		
		SearchPattern pattern1 = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
		SearchPattern pattern2 = SearchPattern.createPattern(method2, IJavaSearchConstants.REFERENCES);
		
		SearchPattern pattern = SearchPattern.createOrPattern(pattern1, pattern2);
		
		List<IMethod> methods = findMethods(javaProject, 
				"org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForInt", 0);
//		List<IMethod> methods = findMethods(javaProject, 
//				"java.sql.Connection", "prepareStatement", 0);
	
		SearchPattern combPattern = createCombinedMethodReferencePattern(methods);
		//System.out.println(methods);
		
		SearchPattern stringPattern = SearchPattern.createPattern(
				"org.springframework.jdbc.core.simple.SimpleJdbcTemplate.queryForInt", 
				IJavaSearchConstants.METHOD,
				IJavaSearchConstants.REFERENCES,
				SearchPattern.R_EXACT_MATCH);
		
		
		final AtomicInteger count = new AtomicInteger(0);
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				System.out.println(match);
				count.incrementAndGet();
			}
		};
		
		
		Timer timer = new Timer("Search");
		executeSearch(combPattern, requestor, scope);
		timer.printTime();
		System.out.println(count);
	}
	
//	private SearchPattern getSinglePatternByString() {
//		SearchPattern pattern = SearchPattern.createPattern(
//				"java.util.List.get(int)", 
//				IJavaSearchConstants.METHOD, 
//				IJavaSearchConstants.REFERENCES, 
//				SearchPattern.R_EXACT_MATCH  | SearchPattern.R_CASE_SENSITIVE
//				);
//		return pattern;
//	}
	
	private static void executeSearch(SearchPattern pattern, SearchRequestor requestor,
			IJavaSearchScope scope) {
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, 
				scope, requestor, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private List<IMethod> findMethods(IJavaProject javaProject, String className, String methodName, 
			int stringArgumentIndex) {
		try {
			IType type = javaProject.findType(className);
			List<IMethod> result = new ArrayList<IMethod>();
			for (IMethod method: type.getMethods()) {
				if (method.getElementName().equals(methodName)) {
					String[] paramTypes = method.getParameterTypes();
					if (paramTypes.length > stringArgumentIndex && (
							paramTypes[stringArgumentIndex].equals("Ljava.lang.String;")
							|| paramTypes[stringArgumentIndex].equals("Qjava.lang.String;")
							|| paramTypes[stringArgumentIndex].equals("QString;")
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
}
