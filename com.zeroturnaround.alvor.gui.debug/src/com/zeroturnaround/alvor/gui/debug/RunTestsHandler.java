package com.zeroturnaround.alvor.gui.debug;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;


public class RunTestsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IHandlerService service = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
		try {
			service.executeCommand("com.zeroturnaround.alvor.plugin.CleanCheck", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
