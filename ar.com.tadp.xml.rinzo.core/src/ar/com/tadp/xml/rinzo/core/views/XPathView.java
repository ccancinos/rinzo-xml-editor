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
package ar.com.tadp.xml.rinzo.core.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;

/**
 * View used to evaluate XPath expressions over the selected opened editor.
 * 
 * @author ccancinos
 */
public class XPathView extends ViewPart {
	private static final String XPATH_VIEW_AUTO_EVALUATION = "XPathView.autoEvaluate";
	private SourceViewer resultViewer;
	private Combo expressionsCombo;
	private Button evaluateButton;
	private IAction clearAllAction;
	private boolean autoEvaluation;
	private IAction autoEvaluateAction;
	private KeyListener autoEvaluationListener;
	private KeyListener enterEvaluationListener;
	private SelectionListener selectionListener;
	private XPathEvaluator xPathEvaluator = new XPathEvaluator();
	private Document sourceXMLDocument;

	public XPathView() {
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite expresionComposite = new Composite(parent, SWT.NONE);
		expresionComposite.setLayout(new GridLayout(3, false));
		expresionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite expresionContainer = new Composite(expresionComposite, SWT.NONE);
		expresionContainer.setLayout(new GridLayout(3, false));
		expresionContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(expresionContainer, SWT.NONE);
		label.setText("Expression:");
		this.expressionsCombo = new Combo(expresionContainer, SWT.DROP_DOWN | SWT.SINGLE | SWT.LEAD);
		this.expressionsCombo.setLayout(new GridLayout(1, false));
		this.expressionsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.expressionsCombo.setFocus();
		this.expressionsCombo.setVisibleItemCount(8);
		this.expressionsCombo.setToolTipText("Enter XPath for Source XML");
		this.autoEvaluationListener = new AutoEvaluationKeyListener();
		this.enterEvaluationListener = new EnterEvaluationKeyListener();
		this.expressionsCombo.addKeyListener(this.enterEvaluationListener);
		this.selectionListener = new SelectionListenerImplementation();

		this.evaluateButton = new Button(expresionComposite, SWT.PUSH);
		this.evaluateButton.setText("evaluate");
		this.evaluateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				evaluateExpression();
			}
		});

		Composite resultComposite = new Composite(parent, SWT.NONE);
		resultComposite.setLayout(new FillLayout());
		resultComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.resultViewer = new SourceViewer(resultComposite, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		this.resultViewer.setInput(this);
		sourceXMLDocument = new Document("");
		this.resultViewer.configure(new XMLSrcViewerConfiguration(sourceXMLDocument));
		this.resultViewer.setDocument(sourceXMLDocument);

		this.clearAllAction = new Action("Clear All") {
			public void run() {
				resultViewer.getDocument().set("");
				resultViewer.refresh();
				expressionsCombo.removeAll();
			}
		};
		this.clearAllAction
				.setImageDescriptor(ImageDescriptor.createFromImage(PluginImages.get(PluginImages.IMG_CLEAR)));

		this.autoEvaluateAction = new Action("Auto Evaluate XPath", IAction.AS_CHECK_BOX) {
			public void run() {
				setAutoEvaluation(!autoEvaluation);
			}
		};
		this.autoEvaluateAction.setImageDescriptor(ImageDescriptor.createFromImage(PluginImages
				.get(PluginImages.IMG_XPATH_AUTO_EVALUATE)));
		this.setAutoEvaluation(XMLEditorPlugin.getDefault().getPreferenceStore().getBoolean(XPATH_VIEW_AUTO_EVALUATION));
		this.autoEvaluateAction.setChecked(this.autoEvaluation);

		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(this.autoEvaluateAction);
		toolbarManager.add(this.clearAllAction);
	}

	private void evaluateExpression() {
		try {
			String result = this.xPathEvaluator.evaluate(this.getExpression(), this.getEditorContent());
			if (!this.resultViewer.getDocument().get().equals(result.toString()) && result.length() != 0) {
				this.saveHistory();
			}
			this.showXPathResult(result.toString());
		} catch (Exception e) {
			String localizedMessage = (e.getLocalizedMessage() != null) ? e.getLocalizedMessage() : e.getCause()
					.getLocalizedMessage();
			this.showXPathResult(localizedMessage);
		}
	}

	private void setAutoEvaluation(boolean autoEvaluation) {
		XMLEditorPlugin.getDefault().getPreferenceStore().setValue(XPATH_VIEW_AUTO_EVALUATION, autoEvaluation);
		this.autoEvaluation = autoEvaluation;

		this.evaluateButton.setEnabled(!this.autoEvaluation);
		if (this.autoEvaluation) {
			this.expressionsCombo.addKeyListener(this.autoEvaluationListener);
			this.expressionsCombo.addSelectionListener(this.selectionListener);
		} else {
			this.expressionsCombo.removeKeyListener(this.autoEvaluationListener);
			this.expressionsCombo.removeSelectionListener(this.selectionListener);
		}
	}

	public void setFocus() {
		this.expressionsCombo.setFocus();
	}

	private String getEditorContent() {
		return this.getActiveEditor().getSourceViewerEditor().getDocument().get();
	}

	private String getExpression() {
		return this.expressionsCombo.getText();
	}

	private void showXPathResult(String result) {
		this.resultViewer.getDocument().set(result);
		this.resultViewer.refresh();
	}

	private void saveHistory() {
		List<String> history = Arrays.asList(this.expressionsCombo.getItems());
		if (!history.contains(this.getExpression())) {
			this.expressionsCombo.add(this.getExpression(), 0);
		}
	}

	private RinzoXMLEditor getActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor instanceof RinzoXMLEditor) {
					return (RinzoXMLEditor) editor;
				}
			}
		}
		return null;
	}

	private final class SelectionListenerImplementation implements SelectionListener {
		public void widgetSelected(SelectionEvent e) {
			evaluateExpression();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			evaluateExpression();
		}
	}

	private final class AutoEvaluationKeyListener implements KeyListener {
		public void keyReleased(KeyEvent e) {
			evaluateExpression();
		}

		public void keyPressed(KeyEvent e) {
		}
	}

	private final class EnterEvaluationKeyListener implements KeyListener {
		public void keyReleased(KeyEvent event) {
			if (event.keyCode == SWT.CR) {
				evaluateExpression();
			}
		}

		public void keyPressed(KeyEvent e) {
		}
	}

}
