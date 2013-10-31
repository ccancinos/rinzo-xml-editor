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

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;

/**
 * This implementation of {@link org.eclipse.jface.text.ITextHover}. displays
 * 
 * @author ccancinos
 */
public class MultipleLinesTextHover implements ITextHover {
	/** This hover's source viewer */
	private ISourceViewer sourceViewer;
	private RinzoXMLEditor editor;

	/**
	 * Creates a new annotation hover.
	 *
	 * @param sourceViewer this hover's annotation model
	 * @param editor 
	 */
	public MultipleLinesTextHover(ISourceViewer sourceViewer, RinzoXMLEditor editor) {
		this.editor = editor;
		Assert.isNotNull(sourceViewer);
		this.sourceViewer = sourceViewer;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.4, replaced by {@link ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)}
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion.getLength() == 0) {
			return null;
		}
		String annotationMessage = this.getAnnotationMessage(textViewer, hoverRegion);
		if (!StringUtils.isEmpty(annotationMessage)) {
			return annotationMessage;
		}

		try {
			XMLNode activeNode = this.editor.getModel().getTree().getActiveNode(hoverRegion.getOffset());
			if (activeNode.isTag() || activeNode.isEndTag() || activeNode.isEmptyTag()) {
				int startName = activeNode.getOffset();
				int endName = activeNode.getFullTagName().length();

				if (startName <= hoverRegion.getOffset() && ((startName + endName) >= hoverRegion.getOffset())) {
					if (activeNode.isEndTag()) {
						activeNode = activeNode.getCorrespondingNode();
					}
					return activeNode.getTypeDefinition().getComment() + "<br>" + "<b>XPath:</b> "
							+ activeNode.getXPath();
				}

				AttributeDefinition attribute = activeNode.getTypeDefinition().getAttribute(
						this.sourceViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength()));
				if (attribute != null) {
					return attribute.getComment() + "<br>" + "<b>XPath:</b> " + activeNode.getXPath() + "[@"
							+ attribute.getName() + "]";
				}
			}
		} catch (Exception e1) {
			//DO NOTHING
		}
		return null;
	}

	private String getAnnotationMessage(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = getAnnotationModel(sourceViewer);
		if (model == null) {
			return null;
		}

		Iterator e = model.getAnnotationIterator();
		StringBuffer buffer = new StringBuffer();
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();
			if (isIncluded(a)) {
				Position p = model.getPosition(a);
				if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg = a.getText();
					if (msg != null && msg.trim().length() > 0 && !msg.trim().endsWith("changed lines")
							&& !msg.trim().endsWith("changed line")) {
						buffer.append("-" + msg);
						buffer.append("<br>");
					}
				}
			}
		}

		return buffer.toString();
	}

	/*
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
	}

	/**
	 * Tells whether the annotation should be included in
	 * the computation.
	 *
	 * @param annotation the annotation to test
	 * @return <code>true</code> if the annotation is included in the computation
	 */
	protected boolean isIncluded(Annotation annotation) {
		return true;
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension = (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}

	private IRegion findWord(IDocument document, int offset) {
		int start= -2;
		int end= -1;

		try {
			int pos= offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c) && c != '-') {
					break;
				}
				--pos;
			}

			start= pos;
			pos= offset;
			int length= document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c) && c != '-') {
					break;
				}
				++pos;
			}

			end = pos;
		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset) {
				return new Region(offset, 0);
			} else if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}

		return null;
	}
}
