package ee.stacc.productivity.edsl.cmdline;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.checkers.AbstractStringCheckerManager;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.main.JavaElementChecker;
import ee.stacc.productivity.edsl.main.OptionLoader;
import ee.stacc.productivity.edsl.string.IPosition;

/**
 * This class controls all aspects of the application's execution
 */
public class EDSLApplication implements IApplication {

	private ILog log;
	private final ISQLErrorHandler errorHandler = new ISQLErrorHandler() {
		
		@Override
		public void handleSQLError(
				String errorMessage,
				IPosition position) {
			log.error("ERROR: " + errorMessage);
		}
		
		@Override
		public void handleSQLWarning(
				String message,
				IPosition position) {
			log.error("WARNING: " + message);
		}
		
	};
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Logs.configureFromStream(EDSLApplication.class.getClassLoader().getResourceAsStream("logging.properties"));

		CacheService.getCacheService();
		
		log = Logs.getLog(EDSLApplication.class);
		log.message("Projects found:");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel javaModel = JavaCore.create(root);
		IJavaProject[] projects = javaModel.getJavaProjects();
		
		for (IJavaProject javaProject : projects) {
			File propertiesFile = OptionLoader.getElementSqlCheckerPropertiesFile(javaProject);
			if (!propertiesFile.exists()) {
				continue;
			}
			log.message(javaProject.getElementName());
			IJavaElement[] children = javaProject.getChildren();
			for (IJavaElement iJavaElement : children) {
				if (iJavaElement instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot sf = (IPackageFragmentRoot) iJavaElement;
					if (!sf.isExternal() && !sf.isArchive()) {
						log.message(sf.getElementName());
						JavaElementChecker projectChecker = new JavaElementChecker();
						
						Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(iJavaElement);
						List<INodeDescriptor> hotspots = findHotspots(sf,
								projectChecker, options);
						findHotspots(sf, projectChecker, options);
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForLong,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForInt,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForLong,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForInt,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForObject,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForObject,1"));
//						findHotspots(sf,
//								projectChecker, Collections.<String, Object>singletonMap("hotspots", ",queryForObject,1;,queryForInt,1;,queryForLong,1;"));
//						findHotspots(sf,
//								projectChecker, options);
//						int count = 5;
//						long totalTime = 0;
//						for (int i = 0; i < count; i++) {
//							hotspots = findHotspots(sf,	projectChecker, options);
//							totalTime += time;
//						}
//						log.format("Average time: %f", (totalTime + 0.0) / count);
						
						checkHotspots(hotspots, projectChecker, options);
						checkHotspots(hotspots, projectChecker, options);
						checkHotspots(hotspots, projectChecker, options);
					}
				}
			}
		}
		return IApplication.EXIT_OK;
	}

	private void checkHotspots(List<INodeDescriptor> hotspots,
			JavaElementChecker projectChecker, Map<String, Object> options) {
		long t = System.currentTimeMillis();
		projectChecker.processHotspots(hotspots, 
				errorHandler, 
				AbstractStringCheckerManager.INSTANCE.getCheckers(),
				options
			);
		log.format("Checker time: %d\n", System.currentTimeMillis() - t);
	}

	long time;
	private List<INodeDescriptor> findHotspots(IPackageFragmentRoot sf,
			JavaElementChecker projectChecker, Map<String, Object> options) {
		long time = System.currentTimeMillis();
		List<INodeDescriptor> hotspots = projectChecker.findHotspots(new IJavaElement[] {sf}, options);
		time = System.currentTimeMillis() - time;
		this.time = time;
		log.format("Crawler time: %d for %d hotspots\n", time, hotspots.size());
		return hotspots;
	}

	@Override
	public void stop() {
		// nothing to do
	}
	
//	private void regularSeqrch(IPackageFragmentRoot sf)
//			throws JavaModelException {
//		SearchPattern subPattern = SearchPattern.createPattern("org.springframework.jdbc.core.simple.SimpleJdbcTemplate.queryForObject", 
//				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
//				SearchPattern.R_ERASURE_MATCH | SearchPattern.R_CASE_SENSITIVE);
//		long t = System.currentTimeMillis();
//		SearchResultGroup[] search = RefactoringSearchEngine.search(
//				subPattern, 
//				SearchEngine.createJavaSearchScope(new IJavaElement[] {sf}, IJavaSearchScope.SOURCES), 
//				null, 
//				null);
//		t = System.currentTimeMillis() - t;
//		int c = 0;
//		for (SearchResultGroup searchResultGroup : search) {
//			SearchMatch[] searchResults = searchResultGroup.getSearchResults();
//			c += searchResults.length;
//		}
//		System.out.println(t + " for " + c);
//	}
	
}
