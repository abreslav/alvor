package com.zeroturnaround.alvor.gui;

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

import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.conntracker.ConnectionDescriptor;
import com.zeroturnaround.alvor.conntracker.ConnectionTracker;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;

public class ConnectionSourceFinder implements IEditorActionDelegate {
	private static final ILog LOG = Logs.getLog(ConnectionSourceFinder.class);
	
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
		
		IResource resource = (IResource) targetEditor.getEditorInput().getAdapter(IResource.class);
		ICompilationUnit icu = (ICompilationUnit) JavaCore.create(resource);
		ASTNode ast = ASTUtil.parseCompilationUnit(icu, true);
		
		ASTNode node = NodeFinder.perform(ast, textSel.getOffset(), textSel.getLength());
		if (node == null) {
			LOG.error("ERROR: Did not find node", null);
		}
		else if (node instanceof Expression) {
			assert LOG.message("###############################");
			assert LOG.message("Selection is : " + node.getClass().getName());
			assert LOG.message("Connection source is: ");
			
			ConnectionDescriptor desc = ConnectionTracker.getConnectionDescriptor((Expression)node); 
			assert LOG.message(desc.getPos().getPath() + ": " 
				+ PositionUtil.getLineNumber(desc.getPos()) + ", DESC: " + desc);
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
