package com.zeroturnaround.alvor.gui.configuration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

/**
 * @author Aivar
 *
 */

public class AlvorXmlEditor extends FormEditor {
	private static final ILog LOG = Logs.getLog(AlvorXmlEditor.class);
	private FormToolkit toolkit = new FormToolkit(PlatformUI.getWorkbench().getDisplay());
	private AlvorXmlEditorFormPage formPage;
	private TextEditor texteditor;
	private ProjectConfiguration configuration;

	
	public AlvorXmlEditor() {
		super();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		texteditor.doSave(monitor);
		formPage.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isDirty() {
		return texteditor != null && texteditor.isDirty()
			|| formPage != null && formPage.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void setFocus() {
		formPage.setFocus();
	}

	@Override
	protected void addPages() {
		try {
			assert(this.configuration != null); // setInput is called before addPages
			
			// form page
			formPage = new AlvorXmlEditorFormPage(this, toolkit, configuration);
			this.addPage(formPage);
			
			// text-editor page
			texteditor = new TextEditor();
			int index = addPage(texteditor, getEditorInput());
			setPageText(index, texteditor.getTitle());
		}
		catch (PartInitException e) {
			LOG.exception(e);
		}
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		FileEditorInput inp = (FileEditorInput) input;
		this.configuration = ConfigurationManager.loadFromFile(inp.getPath().toFile());
	}
}
