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
package ar.com.tadp.xml.rinzo.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.actions.FormatAction;
import ar.com.tadp.xml.rinzo.core.eclipse.copies.MatchingCharacterPainter;
import ar.com.tadp.xml.rinzo.core.keyListeners.AutoInsertEndTagHandler;
import ar.com.tadp.xml.rinzo.core.keyListeners.CommentSelectionHandler;
import ar.com.tadp.xml.rinzo.core.keyListeners.NavigateTagsHandler;
import ar.com.tadp.xml.rinzo.core.keyListeners.NodeRangeHighlighter;
import ar.com.tadp.xml.rinzo.core.model.TreeModelContainer;
import ar.com.tadp.xml.rinzo.core.model.XMLExternalFileDocumentProvider;
import ar.com.tadp.xml.rinzo.core.model.XMLInternalFileDocumentProvider;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.XMLTreeModel;
import ar.com.tadp.xml.rinzo.core.model.tags.XMLTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.model.tags.dtd.DTDTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.model.tags.nodef.NoDefTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.model.tags.xsd.CompositeXMLTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.model.tags.xsd.XSDTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.model.visitor.FoldingNodesVisitor;
import ar.com.tadp.xml.rinzo.core.outline.XMLOutlinePage;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Definition of the structure of Rinzo's editor
 * 
 * @author ccancinos
 */
public class RinzoXMLEditor extends TextEditor implements ISelectionChangedListener {
	/** The ID of this editor as defined in plugin.xml */
	public static final String EDITOR_ID = "ar.com.tadp.xml.rinzo.core";
	/** The ID of the editor context menu */
	public static final String EDITOR_CONTEXT = EDITOR_ID + ".context";
	/** The ID of the editor ruler context menu */
	public static final String RULER_CONTEXT = EDITOR_CONTEXT + ".ruler";

	private XMLOutlinePage outlinePage;
	private NoDefTagDefinitionProvider containersRegistry = new NoDefTagDefinitionProvider();
	private XMLTagDefinitionProvider schemaContaintersRegistry;
	private DTDTagDefinitionProvider dtdContaintersRegistry;
	private EditorConfiguration editorFileConfigurationStrategy;
	private boolean changed = true;
	private EditorUpdater updater;
	private TagPairMatcher pairMatcher;

	private ProjectionSupport projectionSupport;
	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;

	protected void doSetInput(IEditorInput input) throws CoreException {
		if (input instanceof IFileEditorInput || input instanceof IStorageEditorInput) {
			setDocumentProvider(new XMLInternalFileDocumentProvider(this));
		} else {
			setDocumentProvider(new XMLExternalFileDocumentProvider(this));
		}
		this.setConfiturationStrategy(new XMLEditorConfiguration(this));
		super.doSetInput(input);

		updater = new EditorUpdater(this);
		ThreadExecutorService.getInstance().execute(this.updater);
		getDocumentProvider().getDocument(input).addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			public void documentChanged(DocumentEvent event) {
				changed = true;
			}
		});
	}

	public void dispose() {
		this.updater.setStop();
	}

	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);

		this.pairMatcher = new TagPairMatcher(this);
		support.setCharacterPairMatcher(pairMatcher);
		support.setSymbolicFontName(getFontPropertyPreferenceKey());
	}

	private void setConfiturationStrategy(EditorConfiguration internalFileConfigurationStrategy) {
		this.editorFileConfigurationStrategy = internalFileConfigurationStrategy;
		this.setSourceViewerConfiguration((SourceViewerConfiguration) internalFileConfigurationStrategy);
	}

	protected void initializeEditor() {
		super.initializeEditor();
		setKeyBindingScopes(new String[] { "org.eclipse.ui.rinzoEditorScope" });
		setPreferenceStore(XMLEditorPlugin.getDefault().getPreferenceStore());
		setEditorContextMenuId(EDITOR_CONTEXT);
		setRulerContextMenuId(RULER_CONTEXT);
	}

	public void createPartControl(Composite composite) {
		super.createPartControl(composite);
		StyledText styledtext = this.getSourceViewer().getTextWidget();

		styledtext.addKeyListener(new AutoInsertEndTagHandler(this));
		styledtext.addKeyListener(new CommentSelectionHandler(this));
		styledtext.addKeyListener(new NavigateTagsHandler(this));

		ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
		MatchingCharacterPainter painter = new MatchingCharacterPainter(getSourceViewer(), new TagPairMatcher(this));
		painter.setColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		extension.addPainter(painter);

		((TextViewer) this.getSourceViewer()).addPostSelectionChangedListener(new NodeRangeHighlighter(this));

		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = viewer.getProjectionAnnotationModel();

		this.updateFoldingStructure();
	}

	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new XMLOutlinePage(this);
				outlinePage.addSelectionChangedListener(this);
			}
			return outlinePage;
		}

		return super.getAdapter(adapter);
	}

	public ISourceViewer getSourceViewerEditor() {
		return super.getSourceViewer();
	}

	public void doSave(IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Saving File: " + this.getFileName(), 2);
		try {
			if (XMLEditorPlugin.isFormatOnSave()) {
				FormatAction formatAction = new FormatAction();
				formatAction.setActiveEditor(null, this);
				formatAction.run(null);
			}
			super.doSave(new SubProgressMonitor(progressMonitor, 1));
			progressMonitor.worked(1);
			if (this.getModel().getTree().getRootNode() != null && this.getEditorInputIFile() != null) {
				this.setChanged(true);
			}
			progressMonitor.worked(1);
			if (!this.updater.isStop()) {
				ThreadExecutorService.getInstance().execute(this.updater);
			}
		} catch (Exception e) {
			XMLEditorPlugin.logErrorMessage("Error saving file: " + this.getFileName(), e);
		} finally {
			progressMonitor.done();
		}
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		this.editorFileConfigurationStrategy.editorContextMenuAboutToShow(menu);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			Object element = selection.getFirstElement();
			if (element != null && element instanceof XMLNode) {
				XMLNode node = (XMLNode) element;
				this.selectAndReveal(node.getSelectionOffset(), node.getSelectionLength());
			}
		}
	}

	public void setFocus() {
		super.setFocus();
		setKeyBindingScopes(new String[] { "org.eclipse.ui.rinzoEditorScope" });
	}

	protected void createActions() {
		super.createActions();
		Action action = new ContentAssistAction(XMLEditorPlugin.getDefault().getResourceBundle(),
				"ContentAssistProposal.", this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);
		markAsStateDependentAction("ContentAssistProposal", true);
	}

	public String getFileName() {
		return FileUtils.relPathToUrl(this.getEditorInputFile().getAbsolutePath());
	}

	public IFile getEditorInputIFile() {
		return ResourceUtil.getFile(this.getEditorInput());
	}

	public File getEditorInputFile() {
		File toFile = null;
		IEditorInput input = this.getEditorInput();

		if (input instanceof FileStoreEditorInput) {
			toFile = new File(((FileStoreEditorInput) input).getURI());
		}

		if (input instanceof IPathEditorInput) {
			toFile = ((IPathEditorInput) input).getPath().toFile();
		}

		return toFile;
	}

	public String getLineSeparator() {
		return FileUtils.getLineSeparator(this);
	}

	public XMLTreeModel getModel() {
		return ((TreeModelContainer) this.getDocumentProvider()).getTreeModel();
	}

	public NoDefTagDefinitionProvider getCodeTagContainersRegistry() {
		return this.containersRegistry;
	}

	public XMLTagDefinitionProvider getTagContainersRegistry() {
		try {
			TreeModelContainer documentProvider = (TreeModelContainer) this.getDocumentProvider();
			XMLTreeModel treeModel = documentProvider.getTreeModel();
			Collection<DocumentStructureDeclaration> schemaDefinitions = treeModel.getSchemaDefinitions();
			DocumentStructureDeclaration structureDeclaration = treeModel.getDTDDefinition();

			XMLTagDefinitionProvider registry = this.containersRegistry;

			if (this.getFileName() != null) {
				if (!schemaDefinitions.isEmpty()) {
					registry = this.getCompositeSchemaTagContainersRegistry(schemaDefinitions);
				} else {
					if (structureDeclaration != null) {
						Collection<DocumentStructureDeclaration> definitions = new ArrayList<DocumentStructureDeclaration>();
						definitions.add(structureDeclaration);
						DocumentCache.getInstance().getAllLocations(definitions, this.getFileName());
						registry = this.getDTDTagContainersRegistry(treeModel.getDTDRootNode(), structureDeclaration);
					}
				}
			}

			return registry;
		} catch (Exception exception) {
			return this.containersRegistry;
		}
	}

	private DTDTagDefinitionProvider getDTDTagContainersRegistry(String rootNodeName,
			DocumentStructureDeclaration structureDeclaration) {
		if (this.dtdContaintersRegistry == null) {
			this.dtdContaintersRegistry = new DTDTagDefinitionProvider(this.getFileName(), rootNodeName,
					structureDeclaration);
		} else {
			this.dtdContaintersRegistry.setDefinition(this.getFileName(), structureDeclaration);
		}

		return this.dtdContaintersRegistry;
	}

	private XMLTagDefinitionProvider getCompositeSchemaTagContainersRegistry(
			Collection<DocumentStructureDeclaration> schemaDefinitions) throws URISyntaxException {
		DocumentCache.getInstance().getAllLocations(schemaDefinitions, this.getFileName());
		if (this.schemaContaintersRegistry == null) {
			CompositeXMLTagDefinitionProvider compositeXMLTagDefinitionProvider = new CompositeXMLTagDefinitionProvider();
			for (DocumentStructureDeclaration structureDeclaration : schemaDefinitions) {
				try {
					compositeXMLTagDefinitionProvider.addTagDefinitionProvider(new XSDTagDefinitionProvider(this
							.getFileName(), structureDeclaration));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.schemaContaintersRegistry = compositeXMLTagDefinitionProvider;
		} else {
			Collection<URI> uris = new ArrayList<URI>();
			CompositeXMLTagDefinitionProvider compositeXMLTagDefinitionProvider = (CompositeXMLTagDefinitionProvider) this.schemaContaintersRegistry;
			for (DocumentStructureDeclaration structureDeclaration : schemaDefinitions) {
				try {
					URI schemaURI = FileUtils.resolveURI(this.getFileName(), structureDeclaration.getSystemId());
					uris.add(schemaURI);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			compositeXMLTagDefinitionProvider.setDefinition(this.getFileName(), uris);
		}

		return (XMLTagDefinitionProvider) this.schemaContaintersRegistry;
	}

	public synchronized boolean isChanged() {
		return changed;
	}

	public synchronized void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void updateFoldingStructure() {
		FoldingNodesVisitor visitor = new FoldingNodesVisitor(this.getSourceViewerEditor().getDocument(),
				this.getLineSeparator());
		this.getModel().getTree().accept(visitor);

		HashMap<ProjectionAnnotation, Position> newAnnotations = visitor.getAnnotationsMap();
		Annotation[] annotations = visitor.getAnnotations();

		if (annotationModel != null) {
			annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		}
		oldAnnotations = annotations;
	}

	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new RinzoProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		if (this.getSourceViewer().getTextWidget() != null) {
			super.handlePreferenceStoreChanged(event);
		}
	}

	@Override
	protected String[] collectContextMenuPreferencePages() {
		String[] defaultPages = super.collectContextMenuPreferencePages();
		String[] rinzoPages = new String[] { "ar.com.tadp.xml.rinzo.core.preferences.FormattingPreferencePage", //$NON-NLS-1$
				"ar.com.tadp.xml.rinzo.core.preferences.MainPreferencePage", //$NON-NLS-1$
				"ar.com.tadp.xml.rinzo.core.preferences.TemplatesPreferencePage", //$NON-NLS-1$
				"ar.com.tadp.xml.rinzo.core.preferences.SyntaxColorPreferencePage", //$NON-NLS-1$
				"ar.com.tadp.xml.rinzo.jdt.preferences.ClassNamesPreferencePage", //$NON-NLS-1$
		};
		int rinzoPagesLength = rinzoPages.length;
		int defaultPagesLength = defaultPages.length;

		String pages[] = new String[rinzoPagesLength + defaultPagesLength];
		System.arraycopy(rinzoPages, 0, pages, 0, rinzoPagesLength);
		System.arraycopy(defaultPages, 0, pages, rinzoPagesLength, defaultPagesLength);

		return pages;
	}
}
