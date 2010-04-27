package ee.stacc.productivity.edsl.gui;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import ee.stacc.productivity.edsl.main.ResultSetChecker;



public class CheckResultSetsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IJavaElement> selectedJavaElements = GuiUtil.getSelectedJavaElements();
		try {
			for (IJavaElement element : selectedJavaElements) {
				ResultSetChecker.checkUsages(new IJavaElement[] {element});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
