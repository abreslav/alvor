package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class CleanCheckHandler extends AbstractHandler {
	private static final ILog LOG = Logs.getLog(CleanCheckHandler.class);
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Timer timer = new Timer("Clean check time");
		try {
			// first expect right-click in package explorer
//			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
//			final IProject project;
//			
//			if (element == null) {
//				project = GuiUtil.getCurrentJavaProject().getProject();
//			}
//			else {
//				project = element.getJavaProject().getProject();
//			}
			
			final IProject project = GuiUtil.getSelectedProject();
			
			if (JavaModelUtil.projectHasJavaErrors(project)) {
				GuiUtil.showDialog("Please correct Java errors before checking SQL", "Problem");
				return null;
			}
			
			LOG.message("Checking project: " + project.getName());
			Job job = new Job("Full SQL checking") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					GuiChecker.INSTANCE.cleanUpdateProjectMarkers(project, monitor);
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.INTERACTIVE);
			job.setUser(true);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					timer.printTime();
				}
			});
			job.schedule();
		} 
		catch (Exception e) {
			LOG.exception(e);
		}
		
		return null;
	}
	
	

}
