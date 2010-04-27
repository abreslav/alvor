package ee.stacc.productivity.edsl.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class GuiUtil {
	public static List<IJavaElement> getSelectedJavaElements() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		List<IJavaElement> result = new ArrayList<IJavaElement>();
		
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			for (Object element : structSel.toList()) {
				if (element instanceof IJavaElement) {
					result.add((IJavaElement)element);
				}
			}
			return result;
//			Object firstElement = structSel.getFirstElement();
//			if (firstElement instanceof IJavaElement) {
//				return (IJavaElement) firstElement;
//			} else if (firstElement instanceof IAdaptable) {
//				IAdaptable adaptable = (IAdaptable) firstElement;
//				Object adapter = adaptable.getAdapter(IJavaElement.class);
//				return (IJavaElement) adapter;
//			}
		}
		throw new IllegalStateException("No Java element selected");
	}


}
