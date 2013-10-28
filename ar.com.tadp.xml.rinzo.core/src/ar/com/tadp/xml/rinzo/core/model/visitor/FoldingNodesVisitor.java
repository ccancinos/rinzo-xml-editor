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
package ar.com.tadp.xml.rinzo.core.model.visitor;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Generates the list of segments to fold/unfold in the editor's projection
 * 
 * @author ccancinos
 */
public class FoldingNodesVisitor implements HierarchicalVisitor {
	private HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();
	private final IDocument document;
	private String lineSeparator;

	public FoldingNodesVisitor(IDocument document, String lineSeparator) {
		this.document = document;
		this.lineSeparator = lineSeparator;
	}

	public boolean visitStart(XMLNode node) {
		try {
			if(!node.isEmpty() && 
				(node.isTag() || node.isCdata()) && 
				!node.isEndTag() &&
				!this.isInSameLine(node, node.getCorrespondingNode())) {

				int end = node.getCorrespondingNode().getOffset() + node.getCorrespondingNode().getLength();
				int length = end - node.getOffset() + this.lineSeparator.length();
				if ((node.getOffset() + length) > this.document.getLength()) {
					length = length - this.lineSeparator.length();
				}
				int offset = this.document.getLineOffset(this.document.getLineOfOffset(node.getOffset()));
				Position position = new Position(offset, length);
				this.newAnnotations.put(new ProjectionAnnotation(), position);
			}
		} catch (Exception e) {
			// DO NOTHING. Failing folding detection should not cause an error.
		}
		return true;
	}

	public boolean visitEnd(XMLNode node) {
		return true;
	}

	public boolean visitChild(XMLNode node) {
		try {
			int offset = this.document.getLineOffset(this.document.getLineOfOffset(node.getOffset()));
			if((node.isCommentTag() || node.isCdata()) && 
					!this.isInSameLine(offset, offset + node.getLength())) {
				int length = node.getLength() + this.lineSeparator.length();
				Position position = new Position(offset, length);
				this.newAnnotations.put(new ProjectionAnnotation(), position);
			}
		} catch (Exception e) {
			// DO NOTHING. Failing folding detection should not cause an error.
		}
		return true;
	}

	public HashMap<ProjectionAnnotation, Position> getAnnotationsMap() {
		return this.newAnnotations;
	}

	public Annotation[] getAnnotations() {
		Set<ProjectionAnnotation> keySet = this.newAnnotations.keySet();
		Annotation[] keys = new Annotation[keySet.size()];
		keySet.toArray(keys);
		return keys;
	}

	private boolean isInSameLine(XMLNode node, XMLNode correspondingNode) {
		if(node == null || correspondingNode == null) {
			return false;
		} else {
			return this.isInSameLine(node.getOffset(), correspondingNode.getOffset());
		}
	}

	private boolean isInSameLine(int start, int end) {
		try {
			return this.document.getLineOfOffset(start) == this.document.getLineOfOffset(end);
		} catch (BadLocationException e) {
			return false;
		}
	}
	
}
