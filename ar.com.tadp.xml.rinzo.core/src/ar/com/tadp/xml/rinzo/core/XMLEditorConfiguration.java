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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.contentassist.processors.CompositeContentAssistProcessor;
import ar.com.tadp.xml.rinzo.core.contentassist.processors.CompositeXMLContentAssistProcessor;
import ar.com.tadp.xml.rinzo.core.contentassist.processors.IXMLContentAssistProcessor;
import ar.com.tadp.xml.rinzo.core.eclipse.copies.HTMLAnnotationHover;
import ar.com.tadp.xml.rinzo.core.eclipse.copies.HTMLTextPresenter;
import ar.com.tadp.xml.rinzo.core.highlighting.ColorManager;
import ar.com.tadp.xml.rinzo.core.highlighting.IXMLColorConstants;
import ar.com.tadp.xml.rinzo.core.highlighting.NonRuleBasedDamagerRepairer;
import ar.com.tadp.xml.rinzo.core.highlighting.XMLScanner;
import ar.com.tadp.xml.rinzo.core.highlighting.XMLTagScanner;
import ar.com.tadp.xml.rinzo.core.indenting.XMLAutoIndentStrategy;
import ar.com.tadp.xml.rinzo.core.partitioner.IXMLPartitions;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLPartitionScanner;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * 
 * @author ccancinos
 */
public class XMLEditorConfiguration extends SourceViewerConfiguration implements EditorConfiguration {
    private static final String RINZO_XML_CONTENTASIST_EXTENSION_POINT_ID = "ar.com.tadp.xml.rinzo.core.xmlContentAssist";
    private static final String RINZO_CONTENTASIST_EXTENSION_POINT_ID = "ar.com.tadp.xml.rinzo.core.contentAssist";
    private ColorManager colorManager;
    private ContentAssistant contentAssist;
    private XMLTagScanner tagScanner;
    private XMLScanner scanner;
    private IPreferenceStore preferenceStore;
	private final RinzoXMLEditor xmlEditor;
	
	public XMLEditorConfiguration() {
		this(null);
	}

    public XMLEditorConfiguration(RinzoXMLEditor xmlEditor) {
        this.xmlEditor = xmlEditor;
		colorManager = new ColorManager();
    }

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return XMLPartitionScanner.CONTENT_TYPES;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getXMLTagScanner());
        reconciler.setDamager(dr, IXMLPartitions.XML_TAG);
        reconciler.setRepairer(dr, IXMLPartitions.XML_TAG);
        reconciler.setDamager(dr, IXMLPartitions.XML_INCOMPLETETAG);
        reconciler.setDamager(dr, IXMLPartitions.XML_ENDTAG);
        reconciler.setRepairer(dr, IXMLPartitions.XML_ENDTAG);
        reconciler.setDamager(dr, IXMLPartitions.XML_EMPTYTAG);
        reconciler.setRepairer(dr, IXMLPartitions.XML_EMPTYTAG);
        dr = new DefaultDamagerRepairer(getXMLScanner());
        reconciler.setDamager(dr, IXMLPartitions.XML_TEXT);
        reconciler.setRepairer(dr, IXMLPartitions.XML_TEXT);
        NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(IXMLColorConstants.XML_COMMENT), null, colorManager.isBold(IXMLColorConstants.XML_COMMENT)));
        reconciler.setDamager(ndr, IXMLPartitions.XML_COMMENT);
        reconciler.setRepairer(ndr, IXMLPartitions.XML_COMMENT);
        NonRuleBasedDamagerRepairer ndr2 = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(IXMLColorConstants.DECLARATION), null, colorManager.isBold(IXMLColorConstants.DECLARATION)));
        reconciler.setDamager(ndr2, IXMLPartitions.XML_DECLARATION);
        reconciler.setRepairer(ndr2, IXMLPartitions.XML_DECLARATION);
        NonRuleBasedDamagerRepairer ndr3 = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(IXMLColorConstants.PROC_INSTR), null, colorManager.isBold(IXMLColorConstants.PROC_INSTR)));
        reconciler.setDamager(ndr3, IXMLPartitions.XML_PI);
        reconciler.setRepairer(ndr3, IXMLPartitions.XML_PI);
        NonRuleBasedDamagerRepairer ndr4 = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(IXMLColorConstants.CDATA), null, colorManager.isBold(IXMLColorConstants.CDATA)));
        reconciler.setDamager(ndr4, IXMLPartitions.XML_CDATA);
        reconciler.setRepairer(ndr4, IXMLPartitions.XML_CDATA);
        return reconciler;
    }

    protected XMLScanner getXMLScanner() {
        if(scanner == null) {
            scanner = new XMLScanner(colorManager);
            scanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(IXMLColorConstants.DEFAULT))));
        }
        return scanner;
    }

    protected XMLTagScanner getXMLTagScanner() {
        if(tagScanner == null) {
            tagScanner = new XMLTagScanner(colorManager);
            tagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(IXMLColorConstants.TAG))));
        }
        return tagScanner;
    }

    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        if(this.contentAssist == null) {
        	this.contentAssist = new ContentAssistant();
            
            this.addContentAssistProcessors(this.contentAssist);
            
            this.contentAssist.setProposalPopupOrientation(20);
            this.contentAssist.setContextInformationPopupOrientation(10);
            this.contentAssist.setInformationControlCreator(getInformationControlCreator(sourceViewer));
            this.contentAssist.enableAutoActivation(true);
        }
        return this.contentAssist;
    }

	private void addContentAssistProcessors(ContentAssistant assist) {
		CompositeContentAssistProcessor compositeProcessor = new CompositeContentAssistProcessor();
		
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				RINZO_XML_CONTENTASIST_EXTENSION_POINT_ID);
		if (configurationElements != null) {
			CompositeXMLContentAssistProcessor xmlCompositeProcessor = new CompositeXMLContentAssistProcessor(
					this.xmlEditor);
			for (int i = 0; i < configurationElements.length; i++) {
				IConfigurationElement element = configurationElements[i];
				try {
					IXMLContentAssistProcessor processor = (IXMLContentAssistProcessor) element
							.createExecutableExtension("class");
					xmlCompositeProcessor.addProcessor(processor);
				} catch (CoreException e) {
					XMLEditorPlugin.logErrorMessage("cannot create processor: " + element.getName());
					XMLEditorPlugin.log(e);
					e.printStackTrace();
				}
			}
			compositeProcessor.addProcessor(xmlCompositeProcessor);
		}

		configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(RINZO_CONTENTASIST_EXTENSION_POINT_ID);
		if (configurationElements != null) {
			CompositeContentAssistProcessor basicCompositeProcessor = new CompositeContentAssistProcessor();
			for (int i = 0; i < configurationElements.length; i++) {
				IConfigurationElement element = configurationElements[i];
				try {
					IContentAssistProcessor processor = (IContentAssistProcessor) element.createExecutableExtension("class");
					basicCompositeProcessor.addProcessor(processor);
				} catch (CoreException e) {
					XMLEditorPlugin.logErrorMessage("cannot create processor: " + element.getName());
					XMLEditorPlugin.log(e);
					e.printStackTrace();
				}
			}
			compositeProcessor.addProcessor(basicCompositeProcessor);
		}
		
		assist.setContentAssistProcessor(compositeProcessor, IXMLPartitions.XML_TAG);
		assist.setContentAssistProcessor(compositeProcessor, IXMLPartitions.XML_EMPTYTAG);
		assist.setContentAssistProcessor(compositeProcessor, IXMLPartitions.XML_INCOMPLETETAG);
		
		assist.setContentAssistProcessor(compositeProcessor, IDocument.DEFAULT_CONTENT_TYPE);
		assist.setContentAssistProcessor(compositeProcessor, IXMLPartitions.XML_TEXT);
	}

    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, 0, 0, new HTMLTextPresenter());
            }
        };
    }

	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		IQuickAssistAssistant assistant= new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new XMLCorrectionProcessor(this.xmlEditor));
		assistant.setInformationControlCreator(getQuickAssistAssistantInformationControlCreator());

		return assistant;
	}
	
	/**
	 * Returns the information control creator for the quick assist assistant.
	 *
	 * @return the information control creator
	 * @since 3.3
	 */
	private IInformationControlCreator getQuickAssistAssistantInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsPlugin.getAdditionalInfoAffordanceString());
			}
		};
	}

    public int getTabWidth(ISourceViewer sourceViewer) {
        return getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }
    
    public IPreferenceStore getPreferenceStore() {
        if(preferenceStore == null)
            preferenceStore = XMLEditorPlugin.getDefault().getPreferenceStore();
        return preferenceStore;
    }
    
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return new IAutoEditStrategy[] { new XMLAutoIndentStrategy(this.xmlEditor) };
    }
    
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new HTMLAnnotationHover();
    }
    
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new MultipleLinesTextHover(sourceViewer, this.xmlEditor);
    }
    
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		Separator item = new Separator("rinzoAdditions");
		menu.appendToGroup("group.save", item);
		menu.add(item);
	}

	public static String[] getIndentPrefixesByPreferences() {
		List<String> prefixes = new ArrayList<String>();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on preference
		boolean useSpaces = XMLEditorPlugin.getDefault().getPreferenceStore()
				.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
		int indentationWidth = XMLEditorPlugin.getDefault().getPreferenceStore()
				.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);

		for (int i = 0; i <= indentationWidth; i++) {
			StringBuffer prefix = new StringBuffer();
			boolean appendTab = false;

			if (useSpaces) {
				for (int j = 0; j + i < indentationWidth; j++) {
					prefix.append(' ');
				}
				if (i != 0) {
					appendTab = true;
				}
			} else {
				for (int j = 0; j < i; j++) {
					prefix.append(' ');
				}
				if (i != indentationWidth) {
					appendTab = true;
				}
			}
			if (appendTab) {
				prefix.append(FileUtils.TAB);
				prefixes.add(prefix.toString());
				// remove the tab so that indentation - tab is also an indent prefix
				prefix.deleteCharAt(prefix.length() - 1);
			}
			prefixes.add(prefix.toString());
		}
		prefixes.add("");
		return (String[]) prefixes.toArray(new String[prefixes.size()]);
	}
	
    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        return getIndentPrefixesByPreferences();
    }

}
