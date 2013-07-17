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
package ar.com.tadp.xml.rinzo.core.outline;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.ThreadExecutorService;
import ar.com.tadp.xml.rinzo.core.model.IModelListener;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.XMLTreeModel;
import ar.com.tadp.xml.rinzo.core.outline.actions.CollapseAllAction;
import ar.com.tadp.xml.rinzo.core.outline.actions.ShowFullNameAction;

/**
 * Page to display the Outline content for an XML document.
 * 
 * @author ccancinos
 */
public class XMLOutlinePage extends ContentOutlinePage implements IModelListener {
	private RinzoXMLEditor editor;
	private XMLTreeModel treeModel;
	private XMLOutlineLabelProvider outlineLabelProvider = new XMLOutlineLabelProvider();
	private boolean dirty;
	private OutlineUpdater updater;

	public XMLOutlinePage(RinzoXMLEditor editor) {
		this.editor = editor;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new XMLOutlineContentProvider());
		viewer.setLabelProvider(this.outlineLabelProvider);
		// FIXME Si no lo pongo dos veces, cuando abre el outline por primera
		// vez, no muestra el texto del root. Ma perque??
		viewer.setInput(getInput());
		viewer.setInput(getInput());
		initListeners(viewer);

		this.updater = new OutlineUpdater(this);
		ThreadExecutorService.getInstance().execute(this.updater);
	}

	public void dispose() {
		super.dispose();
		this.updater.setStop();
	}

	private XMLNode getInput() {
		return getModel().getTree();
	}

	private XMLTreeModel getModel() {
		if (treeModel == null) {
			treeModel = getEditor().getModel();
			treeModel.addModelListener(this);
		}
		return treeModel;
	}

	public RinzoXMLEditor getEditor() {
		return editor;
	}

	public void update() {
		TreeViewer viewer = getTreeViewer();
		if (viewer != null) {
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(getInput());
				control.setRedraw(true);
			}
		}
	}

	protected void initListeners(TreeViewer viewer) {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		TreeViewer viewer = this.getTreeViewer();
		if (viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}
	}

	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager,
			IStatusLineManager statusLineManager) {
		toolBarManager.add(new CollapseAllAction(this.getTreeViewer()));
		toolBarManager.add(new ShowFullNameAction(this, this.outlineLabelProvider));
	}

	public void modelAboutToBeChanged(XMLNode xmlnode) {
	}

	public void modelChanged(XMLNode rootNode) {
		this.setDirty(true);
	}

	public synchronized void setDirty(boolean b) {
		this.dirty = b;
	}

	public synchronized boolean isDirty() {
		return this.dirty;
	}

	public void internalUpdate() {
		if (getControl() == null) {
			return;
		}

		Display d = getControl().getDisplay();
		if (d != null) {
			d.asyncExec(new Runnable() {
				public void run() {
					TreeViewer viewer = getTreeViewer();
					if (viewer != null) {
						Control control = viewer.getControl();
						if (control != null && !control.isDisposed()) {
							control.setRedraw(false);
							viewer.setInput(getInput());
							control.setRedraw(true);
						}
					}
				}
			});
		}
	}

}
