package com.zeroturnaround.alvor.gui.configuration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;


public class AlvorXmlEditor extends FormEditor {
	private static final ILog LOG = Logs.getLog(AlvorXmlEditor.class);
	private FormToolkit toolkit = new FormToolkit(PlatformUI.getWorkbench().getDisplay());
	AlvorXmlEditorMainPage mainPage = new AlvorXmlEditorMainPage(this, toolkit);
	TextEditor texteditor = new TextEditor();

	
	public AlvorXmlEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false; //texteditor != null && texteditor.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void setFocus() {
		mainPage.setFocus();
	}

	@Override
	protected void addPages() {
		try {
			this.addPage(mainPage);
			
			int index = addPage(texteditor, getEditorInput());
			setPageText(index, texteditor.getTitle());
		} catch (PartInitException e) {
			LOG.exception(e);
		}
	}

}
