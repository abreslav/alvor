package com.googlecode.alvor.gui.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;
import com.googlecode.alvor.configuration.ConfigurationManager;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.crawler.StringExpressionEvaluator;
import com.googlecode.alvor.crawler.util.ASTUtil;
import com.googlecode.alvor.string.IAbstractString;

public class AbstractStringPrinter implements IEditorActionDelegate{
	
	private static final ILog LOG = Logs.getLog(AbstractStringPrinter.class);
	
	private ISelection selection;
	private IEditorPart targetEditor;

	public void run(IAction action) {
		try {
			doit();
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	void doit() throws Exception {
		//System.out.println("-- Editor action --" + selection.toString());
		
		assert selection instanceof ITextSelection;
		ITextSelection textSel = (ITextSelection) selection;
		
//		ITypeRoot root = JavaUI.getEditorInputTypeRoot(targetEditor.getEditorInput());
//		CompilationUnit ast = SharedASTProvider
//			.getAST(root, SharedASTProvider.WAIT_YES, null);
		
		IResource resource = (IResource) targetEditor.getEditorInput().getAdapter(IResource.class);
		ICompilationUnit icu = (ICompilationUnit) JavaCore.create(resource);
		ASTNode ast = ASTUtil.parseCompilationUnit(icu, true);
		
		ASTNode node = NodeFinder.perform(ast, textSel.getOffset(), textSel.getLength());
		if (node == null) {
			LOG.error("ERROR: Did not find node", null);
		}
		else if (node instanceof Expression) {
			LOG.message("###############################");
			LOG.message("Selection is : " + node.getClass().getName());
			ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(resource.getProject(), true);
			StringExpressionEvaluator evaluator = new StringExpressionEvaluator(conf);
			HotspotDescriptor desc = evaluator.evaluate((Expression)node, StringExpressionEvaluator.ParamEvalMode.AS_HOTSPOT);
			if (desc instanceof StringHotspotDescriptor) {
				IAbstractString abstr = ((StringHotspotDescriptor)desc).getAbstractValue();
				LOG.message("Abstract value is: " + abstr.toString());
			}
			else if (desc instanceof UnsupportedHotspotDescriptor) {
				LOG.message("Error: " + ((UnsupportedHotspotDescriptor)desc).getProblemMessage());
			}
		} 
		else {
			assert LOG.message("Selection is not expression, but: "
					+ node.getClass().getName());
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}
}
