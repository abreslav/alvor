package com.zeroturnaround.alvor.gui;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.NewASE;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;

public class AbstractStringPrinter implements IEditorActionDelegate{
	
	private static final ILog LOG = Logs.getLog(AbstractStringPrinter.class);
	
	private ISelection selection;
	private IEditorPart targetEditor;

	public AbstractStringPrinter() {
		// TODO Auto-generated constructor stub
	}

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
		
		ITypeRoot root = JavaUI.getEditorInputTypeRoot(targetEditor.getEditorInput());
		CompilationUnit ast = SharedASTProvider
			.getAST(root, SharedASTProvider.WAIT_YES, null);
		
		ASTNode node = NodeFinder.perform(ast, textSel.getOffset(), textSel.getLength());
		if (node == null) {
			LOG.error("ERROR: Did not find node");
		}
		else if (node instanceof Expression) {
			assert LOG.message("###############################");
			assert LOG.message("Selection is : " + node.getClass().getName());
			assert LOG.message("Abstract value is: ");
			
			NodeSearchEngine.clearCache();
			CacheService.getCacheService().clearAll();
			LOG.message(NewASE.evaluateExpression
					((Expression)node).toString());
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
