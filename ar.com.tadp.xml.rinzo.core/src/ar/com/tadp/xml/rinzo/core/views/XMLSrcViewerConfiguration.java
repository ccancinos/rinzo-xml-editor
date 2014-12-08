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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ar.com.tadp.xml.rinzo.core.highlighting.ColorManager;
import ar.com.tadp.xml.rinzo.core.highlighting.XMLTagScanner;

/**
 * 
 * @author ccancinos
 */
public class XMLSrcViewerConfiguration extends SourceViewerConfiguration {

	private XMLTagScanner scanner;
	private IDocument requestDocument;
	private final Color DEFAULT_TAG_COLOR = new Color(Display.getCurrent(), new RGB(0, 0, 200));;

	public XMLSrcViewerConfiguration() {
		// TODO Auto-generated constructor stub
	}

	public XMLSrcViewerConfiguration(IDocument document) {
		requestDocument = document;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		DefaultDamagerRepairer damager = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(damager, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(damager, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}

	private ITokenScanner getTagScanner() {
		if (scanner == null) {
			scanner = new XMLTagScanner(new ColorManager());// XmlRuleScanner();
			scanner.setDefaultReturnToken(new Token(new TextAttribute(DEFAULT_TAG_COLOR)));
		}
		return scanner;
	}

	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		ContentFormatter formatter = new ContentFormatter();
		IFormattingStrategy keyword = new XmlDocumentFormattingStrategy(requestDocument);
		formatter.setFormattingStrategy(keyword, IDocument.DEFAULT_CONTENT_TYPE);
		return formatter;
	}
}
