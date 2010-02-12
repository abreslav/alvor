package ee.stacc.productivity.edsl.cmdline;

import java.util.List;

import org.eclipse.core.resources.IFile;
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
import ee.stacc.productivity.edsl.main.SQLUsageChecker;

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
						SQLUsageChecker projectChecker = new SQLUsageChecker();
						
						long time = System.currentTimeMillis();
						List<IStringNodeDescriptor> hotspots = projectChecker.findHotspots(sf);
						time = System.currentTimeMillis() - time;
						System.out.format("%d\n", time);
						for (IStringNodeDescriptor stringNodeDescriptor : hotspots) {
							System.out.println(stringNodeDescriptor.getAbstractValue());
						}
						projectChecker.checkJavaElement(hotspots, 
								new ISQLErrorHandler() {
									
									@Override
									public void handleSQLError(String errorMessage, IFile file,
											int startPosition, int length) {
										System.err.println(errorMessage);
										System.err.flush();
									}
								}, 
								AbstractStringCheckerManager.INSTANCE.getCheckers());
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
