package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CleanCheckHandler extends AbstractHandler {
	GuiChecker checker = new GuiChecker();
	private static final ILog LOG = Logs.getLog(CleanCheckHandler.class);
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
//			assert LOG.message("MEM: totalMemory() == " + Runtime.getRuntime().totalMemory());
//			assert LOG.message("MEM: maxMemory() == " + Runtime.getRuntime().maxMemory());
//			assert LOG.message("MEM: freeMemory() == " + Runtime.getRuntime().freeMemory());
//			assert LOG.message("MEM: used memory == " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//			Timer timer = new Timer();
//			timer.start("TIMER: whole process");
			
			// first expect right-click in package explorer
			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
			// fall back to other means (like active editor)
			if (element == null) {
				element = GuiUtil.getCurrentJavaProject();
			}
			
			final IJavaElement finalElement = element;
			
			LOG.message("Checking project: " + element.getElementName());
			// TODO add dialog
			
			Job job = new Job("Full SQL checking") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					checker.performCleanCheck(finalElement.getJavaProject().getProject(), new IJavaElement[] {finalElement}, monitor);
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.INTERACTIVE);
			job.setUser(true);
			job.schedule();
			
//			timer.printTime();
		} 
		catch (Exception e) {
			LOG.exception(e);
		}
		
		return null;
	}

}
