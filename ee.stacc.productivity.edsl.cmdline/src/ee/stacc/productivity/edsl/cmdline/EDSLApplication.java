package ee.stacc.productivity.edsl.cmdline;

import java.util.Collections;
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
import ee.stacc.productivity.edsl.main.JavaElementChecker;

/**
 * This class controls all aspects of the application's execution
 */
public class EDSLApplication implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		System.out.println("Projects found:");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel javaModel = JavaCore.create(root);
		IJavaProject[] projects = javaModel.getJavaProjects();
		
		for (IJavaProject javaProject : projects) {
			System.out.println(javaProject.getElementName());
			IJavaElement[] children = javaProject.getChildren();
			for (IJavaElement iJavaElement : children) {
				if (iJavaElement instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot sf = (IPackageFragmentRoot) iJavaElement;
					if (!sf.isExternal() && !sf.isArchive()) {
						System.out.println(sf.getElementName());
						JavaElementChecker projectChecker = new JavaElementChecker();
						
						long time = System.currentTimeMillis();
						Map<String, Object> options = Collections.<String, Object>emptyMap();
						List<IStringNodeDescriptor> hotspots = projectChecker.findHotspots(sf, options);
						time = System.currentTimeMillis() - time;
						System.out.format("%d\n", time);
						for (IStringNodeDescriptor stringNodeDescriptor : hotspots) {
							System.out.println(stringNodeDescriptor.getAbstractValue());
						}
						projectChecker.checkHotspots(hotspots, 
								new ISQLErrorHandler() {

									@Override
									public void handleSQLError(
											String errorMessage,
											IStringNodeDescriptor descriptor) {
										System.err.println(errorMessage);
										System.err.flush();
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

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}
}
