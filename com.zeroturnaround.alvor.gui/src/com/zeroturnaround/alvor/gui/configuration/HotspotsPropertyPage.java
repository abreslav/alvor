package com.zeroturnaround.alvor.gui.configuration;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.HotspotProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public class HotspotsPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private Table table;
	private TableViewer tableViewer;
	private ProjectConfiguration configuration;
	
	public HotspotsPropertyPage() {
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout(2, false);
		body.setLayout(mainLayout);
		
		this.table = new Table(body, SWT.BORDER | SWT.FULL_SELECTION); // Table is actual widget
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL, GridData.FILL, true, true));
		
		Button addButton = new Button(body, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		addButton.setText("Add");
		
		// create viewer and populate with data
		this.tableViewer = createTableViewer(table); // TableViewer manages Table's data
		loadConfiguration();
		tableViewer.setInput(configuration.getHotspots());
		
		
		Text text = new Text(body, SWT.MULTI | SWT.BORDER);
		GridData textLD = new GridData();
		textLD.horizontalSpan = 2;
		textLD.heightHint = 100;
		text.setLayoutData(textLD);
		
		return body;
	}
	
	private TableViewer createTableViewer(Table table) {
		final TableViewer viewer = new TableViewer(table);
		
		// Content provider translates any object into array of (domain) objects 
		// (here each item of array corresponds to one row)
		// ArrayContentProvider knows how to translate arrays or lists (of domain objects) into arrays 
		viewer.setContentProvider(new ArrayContentProvider());

		// (Custom) Label provider knows how to extract individual fields' info from domain object 
		//viewer.setLabelProvider(new LabelProvider());
		
		// later, when tableViewer.setInput(collectionOfDomainObjects) is used,
		// then given input is first translated by content provider and 
		// each individual is processed by label provider (on-demand)
		
		////////////////////////////////////////////////////////
		
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(
				viewer, new FocusCellOwnerDrawHighlighter(viewer));
		
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		
		
		
		ColumnViewerEditorActivationStrategy actStrategy = new ColumnViewerEditorActivationStrategy(
				viewer) {
				protected boolean isEditorActivationEvent(
				ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
				|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
				|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
				|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED);
				}
				};
		
		
		TableViewerEditor.create(viewer, actStrategy,
				//ColumnViewerEditor.DEFAULT
				  ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL 
				| ColumnViewerEditor.KEYBOARD_ACTIVATION
				);
		
		////////////////////////////////////////////////////////
		
//		viewer.getTable().addListener(SWT.EraseItem, new Listener() {
//			public void handleEvent(Event event) {
//				event.detail &= ~SWT.SELECTED;
//			}
//		}); 
		

		// setup columns
		createArgIndexColumn(viewer);
		createMethodNameColumn(viewer);
		createArgIndexColumn(viewer);
//		viewer.getTable().set
		return viewer;
	}
	
	private void createArgIndexColumn(final TableViewer viewer) {
		TableViewerColumn col = createTableViewerColumn(viewer, "Arg", 40, 0);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(String.valueOf(((HotspotProperties)cell.getElement()).getArgumentIndex()));
				viewer.refresh();
			}
		});
		
		col.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				((HotspotProperties)element).setArgumentIndex(Integer.parseInt(String.valueOf(value)));
			}
			
			@Override
			protected Object getValue(Object element) {
				return Integer.valueOf(((HotspotProperties)element).getArgumentIndex());
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}
			
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
	}

	private void createMethodNameColumn(final TableViewer viewer) {
		TableViewerColumn col = createTableViewerColumn(viewer, "ClassName.MethodName", 500, 1);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((HotspotProperties)cell.getElement()).getMethodName());
			}
		});
		
		col.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				((HotspotProperties)element).setMethodName(String.valueOf(value));
				viewer.refresh();
			}
			
			@Override
			protected Object getValue(Object element) {
				return ((HotspotProperties)element).getMethodName();
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}
			
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		
	}

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int width, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		return viewerColumn;
	}

	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.WRAP);
		desc.setText("Here you should list methods and argument indexes (1-based) which shoud be searched for SQL");
		return desc;
	}
	
	private void loadConfiguration() {
		IResource res = getConfigurationResource();
		if (res.exists()) {
			this.configuration = ConfigurationManager.loadFromFile(res.getLocation().toFile());
		}
		else {
			this.configuration = new ProjectConfiguration (
					new ArrayList<HotspotProperties>(), 
					new ArrayList<DataSourceProperties>(), 
					res.getLocation().toFile()
			);
		}
	}
	
	private IResource getConfigurationResource() {
		IAdaptable elem = this.getElement(); // the thing that was right-clicked for getting this page
		assert elem != null && elem instanceof IProject;
		return ConfigurationManager.getConfigurationResource((IProject)elem); 
	}
	
	
	class TableContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return null;
		}
	}
	
	
	
	class LabelProvider extends ColumnLabelProvider {
		
	}
	
	
}
