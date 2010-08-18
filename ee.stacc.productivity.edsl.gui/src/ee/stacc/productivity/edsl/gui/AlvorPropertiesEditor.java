package ee.stacc.productivity.edsl.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ee.stacc.productivity.edsl.main.OptionLoader;

public class AlvorPropertiesEditor extends FormEditor {


	/** The text editor used in page 0. */
	private TextEditor editor;

	/** The font chosen in page 1. */
	private Font font;

	/** The text widget used in page 2. */
	private StyledText text;
	
	

	private class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$

		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}

		protected void createFormContent(IManagedForm managedForm) {
			FormToolkit toolkit = managedForm.getToolkit();
			Composite composite = managedForm.getForm().getBody();
			TableWrapLayout layout = new TableWrapLayout();
			composite.setLayout(layout);
			
//			GridLayout layout = new GridLayout();
//			layout.numColumns = 4;
//			body.setLayout(layout);
//			Label label = toolkit.createLabel(body,
//					"The content of the headless page #" + count);
//			GridData gd = new GridData();
//			gd.horizontalSpan = 4;
//			label.setLayoutData(gd);
//			for (int i = 0; i < 80; i++) {
//				toolkit.createLabel(body, "Field " + i);
//				Text text = toolkit.createText(body, null);
//				gd = new GridData(GridData.FILL_HORIZONTAL);
//				text.setLayoutData(gd);
//			}

			ScrolledForm form = toolkit.createScrolledForm(composite);
			layout = new TableWrapLayout();
			layout.numColumns = 2;
			form.getBody().setLayout(layout);

			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			Label label = toolkit.createLabel(form.getBody(), "foobar"); //$NON-NLS-1$
			td.colspan = 2;
			label.setLayoutData(td);

			//					Label label = new Label(composite, SWT.NONE);
			//					label.setText("foobar");

			td = new TableWrapData(TableWrapData.FILL_GRAB);
			label.setLayoutData(td);

			Map<String, Object> props = loadPropertiesFromEditorInput();

			for (Map.Entry<String, Object> entry : props.entrySet()) {
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				label = toolkit.createLabel(form.getBody(), entry.getKey() +":"); //$NON-NLS-1$
				td = new TableWrapData(TableWrapData.RIGHT);
				label.setLayoutData(td);
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				Text text = toolkit.createText(form.getBody(), entry.getValue().toString()); //$NON-NLS-1$
				text.setLayoutData(td);
			}

			int index = addPage(composite);
			setPageText(index, "Alvor settings");

/*			Composite composite = new Composite(getContainer(), SWT.NONE);
			TableWrapLayout layout = new TableWrapLayout();
			composite.setLayout(layout);
			FormToolkit toolkit = new FormToolkit(composite.getDisplay());
			ScrolledForm form = toolkit.createScrolledForm(composite);
			layout.numColumns = 2;
			form.getBody().setLayout(layout);	


			Label label = new Label(composite, SWT.NONE);
			label.setText("foobar");
			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			label.setLayoutData(td);

			int index = addPage(composite); */
		}
	}

	private Map<String, Object> loadPropertiesFromEditorInput() {
		Map<String, Object> props = null;
		
		try {
			// TODO We should use something like this to init this page?
			//			public void init(IEditorSite site, IEditorInput editorInput)
			//				throws PartInitException {
			IEditorInput editorInput = getEditorInput();
			if (!(editorInput instanceof IFileEditorInput))
				throw new PartInitException("Invalid Input: Must be IFileEditorInput");
				
			java.io.File propFile = ((IFileEditorInput) editorInput).getFile().getLocation().toFile();	
			props = OptionLoader.getFileSqlCheckerProperties(propFile);
			
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return props;
	}

	
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage1() {

		Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		Button fontButton = new Button(composite, SWT.NONE);
		GridData gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fontButton.setLayoutData(gd);
		fontButton.setText("Change Font...");
		
		fontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setFont();
			}
		});

		int index = addPage(composite);
		setPageText(index, "Properties");
	}
	/**
	 * Creates page 2 of the multi-page editor,
	 * which shows the sorted text.
	 */
	void createPage2() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);

		int index = addPage(composite);
		setPageText(index, "Preview");
	}
	
		
	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 */
	void createPage3() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}
	
	
	@Override
	protected void addPages() {
		try {
			addPage(new AlvorPropertiesPage(this));
		} catch  (PartInitException e) {
			//
		}
		
//		createPage0();
//		createPage1();
//		createPage2();
//		createPage3();
	}


	/**
	 * Sets the font related data to be applied to the text in page 2.
	 */
	void setFont() {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (font != null)
				font.dispose();
			font = new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

}


