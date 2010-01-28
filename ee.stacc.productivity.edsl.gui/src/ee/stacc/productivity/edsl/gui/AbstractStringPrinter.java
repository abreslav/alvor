package ee.stacc.productivity.edsl.gui;

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

import ee.stacc.productivity.edsl.crawler.OldAbstractStringEvaluator;

public class AbstractStringPrinter implements IEditorActionDelegate{
	private ISelection selection;
	private IEditorPart targetEditor;

	public AbstractStringPrinter() {
		// TODO Auto-generated constructor stub
	}

	public void run(IAction action) {
		try {
			doit();
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
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
			System.err.println("ERROR: Did not find node");
		}
		else if (node instanceof Expression) {
			System.out.println("###############################");
			System.out.println("Selection is : " + node.getClass().getName());
			// TODO kontrolli et on string tüüpi expression
			System.out.println("Abstract value is: ");
			System.out.println(OldAbstractStringEvaluator.getValOf((Expression)node, 1).toString());
		} 
		else {
			System.out.println("Selection is not expression, but: "
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
