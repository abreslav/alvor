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

import ee.stacc.productivity.edsl.checkers.AbstractStringCheckerManager;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.main.JavaElementChecker;
import ee.stacc.productivity.edsl.main.OptionLoader;

/**
 * This class controls all aspects of the application's execution
 */
public class EDSLApplication implements IApplication {

	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Logs.configureFromStream(EDSLApplication.class.getClassLoader().getResourceAsStream("logging.properties"));
		final ILog log = Logs.getLog(EDSLApplication.class);
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
						
						long time = System.currentTimeMillis();
						Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(iJavaElement);
						List<IStringNodeDescriptor> hotspots = projectChecker.findHotspots(sf, options);
						time = System.currentTimeMillis() - time;
						log.format("%d\n", time);
						for (IStringNodeDescriptor stringNodeDescriptor : hotspots) {
							System.out.println(stringNodeDescriptor.getAbstractValue());
						}
						projectChecker.checkHotspots(hotspots, 
								new ISQLErrorHandler() {

									@Override
									public void handleSQLError(
											String errorMessage,
											IStringNodeDescriptor descriptor) {
										log.error(errorMessage);
									}
								}, 
								AbstractStringCheckerManager.INSTANCE.getCheckers(),
								options
							);
					}
				}
			}
		}
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
