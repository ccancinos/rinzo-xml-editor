/*****************************************************************************
 * This file is part of Rinzo
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/editorxml
 * Copyright (C): 2008, Claudio Cancinos
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.xml.rinzo.jdt.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;

/**
 * Preference Page for JDT integration. It will configure:
 * 
 * <ul>
 * <li>Severity used to mark errors (Error, Warning, Info, Ignore).</li>
 * <li>Switch between validating all attributes and tag's bodies or just the ones configured for such a task.</li>
 * <li>In configured tag's and attributes it is possible to define from which class/interface should extend/implement the written values.</li>
 * </ul>
 * 
 * @author ccancinos
 */
public class ClassNamesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button enableClassName;

	private TabFolder tabFolder;
	private TableViewer attributeTableViewer;
	private TableViewer elementTableViewer;
	
	private List<ClassElement> elementModel = new ArrayList<ClassElement>();
	private List<ClassAttribute> attributeModel = new ArrayList<ClassAttribute>();

	private Combo severityCombo;
	
	public ClassNamesPreferencePage(){
		super("ClassNames");
		setPreferenceStore(RinzoJDTPlugin.getDefault().getPreferenceStore());
		setDescription("Add Elements/Attributes containing Class names.");
	}
	
	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));

		Composite labelCombo = new Composite(composite, SWT.NULL);
		labelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelCombo.setLayout(new GridLayout(2, false));
		Label label = new Label(labelCombo, SWT.NULL);
		label.setText("&Severity:");

		severityCombo = new Combo (labelCombo, SWT.READ_ONLY);
		severityCombo.setItems (new String [] {"Error", "Warning", "Info", "Ignore"});
		severityCombo.setText(RinzoJDTPlugin.getCompilationSeverity());

		// checkbox to toggle the classname support
		enableClassName = new Button(composite, SWT.CHECK);
		enableClassName.setText("&Validation and content assist only for configured Tags and Attributes.");
		enableClassName.setSelection(RinzoJDTPlugin.isEnableClassName());
		enableClassName.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				updateControls();
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		enableClassName.setLayoutData(gd);

		tabFolder = new TabFolder(composite, SWT.NULL);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem attributeTab = new TabItem(tabFolder,SWT.NULL);
		attributeTab.setText("Attributes");
		attributeTab.setControl(createAttributeArea(tabFolder));
		
		TabItem elementTab = new TabItem(tabFolder,SWT.NULL);
		elementTab.setText("Tags");
		elementTab.setControl(createElementArea(tabFolder));

		// set initial values
		attributeModel.addAll(ClassAttribute.loadFromPreference(false));
		attributeTableViewer.refresh();
		elementModel.addAll(ClassElement.loadFromPreference(false));
		elementTableViewer.refresh();
		
		this.updateControls();
		
		return composite;
	}
	
	private Control createElementArea(Composite parent){
		TableViewerSupport<ClassElement> support = new TableViewerSupport<ClassElement>(elementModel, parent){

			protected void initTableViewer(TableViewer viewer) {
				Table table = viewer.getTable();
				TableColumn col1 = new TableColumn(table, SWT.NULL);
				col1.setText("Tag Name");
				col1.setWidth(100);

				TableColumn col2 = new TableColumn(table, SWT.NULL);
				col2.setText("Extends");
				col2.setWidth(220);
			}

			protected ClassElement doAdd() {
				ClassElementDialog dialog = new ClassElementDialog(getShell());
				if(dialog.open()==Dialog.OK){
					return dialog.getCustomElement();
				}
				return null;
			}

			protected void doEdit(ClassElement element) {
				ClassElementDialog dialog = new ClassElementDialog(getShell(), element);
				if(dialog.open()==Dialog.OK){
					ClassElement newElement = dialog.getCustomElement();
					element.setDisplayName(newElement.getDisplayName());
					element.setExtending(newElement.getExtending());
				}
			}

			protected ITableLabelProvider createLabelProvider() {
				return new ClassAssistLabelProvider();
			}
		};
		
		elementTableViewer = support.getTableViewer();
		return support.getControl();
	}
	
	private Control createAttributeArea(Composite parent){
		TableViewerSupport<ClassAttribute> support 
			= new TableViewerSupport<ClassAttribute>(attributeModel, parent){

			protected void initTableViewer(TableViewer viewer) {
				Table table = viewer.getTable();
				
				TableColumn col1 = new TableColumn(table, SWT.NULL);
				col1.setText("Target Tag");
				col1.setWidth(90);
				
				TableColumn col2 = new TableColumn(table, SWT.NULL);
				col2.setText("Attribute Name");
				col2.setWidth(90);
				
				TableColumn col3 = new TableColumn(table, SWT.NULL);
				col3.setText("Extends");
				col3.setWidth(220);
			}

			protected ClassAttribute doAdd() {
				ClassAttributeDialog dialog = new ClassAttributeDialog(getShell());
				if(dialog.open()==Dialog.OK){
					return dialog.getCustomAttribute();
				}
				return null;
			}

			protected void doEdit(ClassAttribute attrInfo) {
				ClassAttributeDialog dialog = new ClassAttributeDialog(getShell(), attrInfo);
				if(dialog.open()==Dialog.OK){
					ClassAttribute newAttrInfo = dialog.getCustomAttribute();
					attrInfo.setTargetTag(newAttrInfo.getTargetTag());
					attrInfo.setAttributeName(newAttrInfo.getAttributeName());
					attrInfo.setExtending(newAttrInfo.getExtending());
				}
			}

			protected ITableLabelProvider createLabelProvider() {
				return new ClassAssistLabelProvider();
			}
			
		};
		
		attributeTableViewer = support.getTableViewer();
		return support.getControl();
	}
	
	protected void performDefaults() {
		attributeModel.clear();
		attributeModel.addAll(ClassAttribute.loadFromPreference(true));
		attributeTableViewer.refresh();
		
		elementModel.clear();
		elementModel.addAll(ClassElement.loadFromPreference(true));
		elementTableViewer.refresh();
		
		enableClassName.setSelection(RinzoJDTPlugin.isEnableClassName());
		severityCombo.setText(RinzoJDTPlugin.getCompilationSeverity());

		this.updateControls();
	}
	
	public boolean performOk() {
		ClassAttribute.saveToPreference(attributeModel);
		ClassElement.saveToPreference(elementModel);
		RinzoJDTPlugin.setEnableClassName(enableClassName.getSelection());
		RinzoJDTPlugin.setCompilationSeverity(this.severityCombo.getText());
		return true;
	}
	
	/**
	 * Updates controls status.
	 */
	private void updateControls(){
		boolean enabled = this.enableClassName.getSelection();
		tabFolder.setEnabled(enabled);
		attributeTableViewer.getControl().setEnabled(enabled);
		elementTableViewer.getControl().setEnabled(enabled);
	}

	/**
	 * LabelProvider for TableViewers
	 */
	private static class ClassAssistLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof ClassAttribute){
				ClassAttribute attr = (ClassAttribute)element;
				if(columnIndex==0){
					return attr.getTargetTag();
				} else if(columnIndex==1){
					return attr.getAttributeName();
				} else if(columnIndex == 2) {
					return attr.getExtending();
				}
			} else if(element instanceof ClassElement){
				ClassElement elem = (ClassElement)element;
				if(columnIndex==0){
					return elem.getDisplayName();
				} else if(columnIndex==1){
					return elem.getExtending();
				}
			}
			return null;
		}
		
		public void addListener(ILabelProviderListener listener) {
		}
		
		public void dispose() {
		}
		
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	/**
	 * The dialog to add / edit the code completion proposal for elements.
	 */
	private static class ClassElementDialog extends Dialog {
		private Text displayName;
		private Text extending;
		private ClassElement element;
		
		public ClassElementDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		
		public ClassElementDialog(Shell parentShell, ClassElement element) {
			super(parentShell);
			this.element = element;
		}
		
		protected Point getInitialSize() {
			Point size = super.getInitialSize();
			size.x = 300;
			return size;
		}

		protected Control createDialogArea(Composite parent) {
			getShell().setText("Elements");
			
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setLayout(new GridLayout(2,false));
			
			Label label = new Label(composite, SWT.NULL);
			label.setText("Tag Name");
			
			displayName = new Text(composite, SWT.BORDER);
			if(element!=null){
				displayName.setText(element.getDisplayName());
			}
			displayName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.NULL);
			label.setText("Extends");
			
			extending = new Text(composite, SWT.BORDER);
			if(element!=null){
				extending.setText(element.getExtending());
			}
			extending.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			return composite;
		}
		
		protected void okPressed() {
			if(displayName.getText().length()==0){
				RinzoJDTPlugin.openAlertDialog("Required: Display Name");
				return;
			}
			element = new ClassElement(displayName.getText(), extending.getText());
			super.okPressed();
		}
		
		public ClassElement getCustomElement(){
			return element;
		}
	}
	
	/**
	 * The dialog to add / edit the code completion proposal for attributes.
	 */
	private static class ClassAttributeDialog extends Dialog {
		private Text target;
		private Text attributeName;
		private Text extending;
		private ClassAttribute attrInfo;
		
		public ClassAttributeDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		
		public ClassAttributeDialog(Shell parentShell, ClassAttribute attrInfo) {
			super(parentShell);
			this.attrInfo = attrInfo;
		}
		
		protected Point getInitialSize() {
			Point size = super.getInitialSize();
			size.x = 300;
			return size;
		}
		
		protected Control createDialogArea(Composite parent) {
			getShell().setText("Attributes");
			
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setLayout(new GridLayout(2,false));
			
			Label label = new Label(composite, SWT.NULL);
			label.setText("Target Tag");
			
			target = new Text(composite, SWT.BORDER);
			if(attrInfo != null){
				target.setText(attrInfo.getTargetTag());
			} else {
				target.setText("*");
			}
			target.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.NULL);
			label.setText("Attribute Name");
			
			attributeName = new Text(composite, SWT.BORDER);
			attributeName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if(attrInfo != null){
				attributeName.setText(attrInfo.getAttributeName());
			}
			
			label = new Label(composite, SWT.NULL);
			label.setText("Extends");

			extending = new Text(composite, SWT.BORDER);
			extending.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if(attrInfo != null){
				extending.setText(attrInfo.getExtending());
			}

			return composite;
		}
		
		protected void okPressed() {
			if(target.getText().length()==0){
				RinzoJDTPlugin.openAlertDialog("Required: Target Tag");
				return;
			}
			if(attributeName.getText().length()==0){
				RinzoJDTPlugin.openAlertDialog("Required: Attribute Name");
				return;
			}
			attrInfo = new ClassAttribute(target.getText(), attributeName.getText(), extending.getText());
			super.okPressed();
		}
		
		public ClassAttribute getCustomAttribute(){
			return attrInfo;
		}
	}

}