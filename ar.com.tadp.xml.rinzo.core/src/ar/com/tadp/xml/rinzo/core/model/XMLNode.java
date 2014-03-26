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
package ar.com.tadp.xml.rinzo.core.model;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.TypedPosition;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;
import ar.com.tadp.xml.rinzo.core.model.visitor.HierarchicalVisitor;
import ar.com.tadp.xml.rinzo.core.model.visitor.Visitable;
import ar.com.tadp.xml.rinzo.core.partitioner.IXMLPartitions;

/**
 * An object representing a node in the xml file. Similar to a DOM object
 * 
 * @author ccancinos
 */
public class XMLNode extends TypedPosition implements Visitable, IDocumentListener, IDocumentPartitioningListener {
	private XMLNode parent;
	private List<XMLNode> children;
	/** Reference to the opening/end tag as needed */
	private XMLNode correspondingNode;
	private String fullTagName = "";
	private String tagName = "";
	private String namespace = "";
	private Map<String, XMLAttribute> attributes = new HashMap<String, XMLAttribute>();
	protected boolean documentChanged = true;
	private IDocument document;
	private RinzoXMLEditor editor;

	public XMLNode(int offset, int length, String type, IDocument document) {
		super(offset, length, type);
		children = new ArrayList<XMLNode>();
		this.document = document;
		if (document != null) {
			this.addListeners();
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	public void hasChanged() {
		this.documentChanged = true;
	}

	public void documentChanged(DocumentEvent event) {
		documentChanged = true;
	}

	public void documentPartitioningChanged(IDocument document) {
		documentChanged = true;
	}

	private void addListeners() {
		this.document.addDocumentListener(this);
		this.document.addDocumentPartitioningListener(this);
	}

	public IDocument getDocument() {
		return this.document;
	}

	public TagTypeDefinition getTypeDefinition() {
		return this.editor.getTagContainersRegistry().getTagDefinition(this);
	}

	public void setEditor(RinzoXMLEditor editor) {
		this.editor = editor;
	}

	public RinzoXMLEditor getEditor() {
		return this.editor;
	}

	public void setParent(XMLNode node) {
		this.parent = node;
	}

	public XMLNode getParent() {
		return (this.isLastNode() && this.isTag()) ? this : this.parent;
	}

	public List<XMLNode> getChildren() {
		return this.children;
	}

	// Used by the outline
	public XMLNode[] getChildren(boolean includeEmptyText) {
		if (includeEmptyText) {
			return this.children.toArray(new XMLNode[this.children.size()]);
		}
		List<XMLNode> excludeEmptyText = new ArrayList<XMLNode>();
		for (Iterator<XMLNode> iter = this.children.iterator(); iter.hasNext();) {
			XMLNode node = iter.next();
			if (!node.isEmpty()) {
				excludeEmptyText.add(node);
			}
		}
		return excludeEmptyText.toArray(new XMLNode[excludeEmptyText.size()]);
	}

	public void addChild(XMLNode node) {
		node.setParent(this);
		this.children.add(node);
	}

	public String getContent() {
		try {
			return document.get(offset, length);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getFullTagName() {
		if (this.documentChanged || StringUtils.isEmpty(this.fullTagName)) {
			this.documentChanged = false;
			StringTokenizer tagNameTokenizer = new StringTokenizer(this.getContent(), " \t\n\r<>/");
			this.fullTagName = (tagNameTokenizer.hasMoreTokens()) ? tagNameTokenizer.nextToken() : "";
		}
		return this.fullTagName;
	}

	public String getTagName() {
		if (this.documentChanged || StringUtils.isEmpty(this.tagName)) {
			this.tagName = (this.getFullTagName().contains(":")) ? 
					this.getFullTagName().substring(this.getFullTagName().indexOf(":") + 1) : this.getFullTagName();
		}
		return this.tagName;
	}

	public String getNamespace() {
		if (this.documentChanged || StringUtils.isEmpty(this.namespace)) {
			this.namespace = (this.getFullTagName().contains(":")) ? 
					this.getFullTagName().substring(0, this.getFullTagName().indexOf(":")) : 
					"";
		}
		return this.namespace;
	}

	public String getXPath() {
		if (this.isEndTag()) {
			return this.getCorrespondingNode().getXPath();
		}
		if (!this.isTag() && !this.isEmptyTag()) {
			return this.getParent().getXPath();
		}

		String xpathName;
		if (this.getParent() == null || StringUtils.isEmpty(this.getParent().getContent())) {
			xpathName = "/"
					+ (StringUtils.isEmpty(this.getNamespace()) ? this.getTagName() : this.getNamespace() + ":"
							+ this.getTagName());
		} else {
			xpathName = this.getParent().getXPath()
					+ "/"
					+ (StringUtils.isEmpty(this.getNamespace()) ? this.getTagName() : this.getNamespace() + ":"
							+ this.getTagName());
			int index = 1;
			int count = 1;
			for (Iterator<XMLNode> iterator = this.getParent().getChildren().iterator(); iterator.hasNext();) {
				XMLNode sibling = iterator.next();
				if (sibling.getNamespace().equals(this.getNamespace())
						&& sibling.getTagName().equals(this.getTagName())) {
					if (sibling == this) {
						index = count;
					} else {
						count++;
					}
				}
			}
			if (count > 1) {
				xpathName += "[" + index + "]";
			}
		}

		return xpathName;
	}

	/**
	 * Returns the attributs written in this tag in the editor
	 */
	public Map<String, XMLAttribute> getAttributes() {
		if (this.documentChanged || this.attributes.isEmpty()) {
			this.documentChanged = false;
			String str = this.getContent();
			this.attributes.clear();

			char[] charArray = str.toCharArray();
			for (int pos = charArray.length - 1; pos >= 0; pos--) {
				char currentChar = charArray[pos];
				XMLAttribute attribute = null;

				if (currentChar == '\"') {
					int equalIndex = pos;
					pos--;
					while (pos > 0 && charArray[pos] != '\"') {
						pos--;
					}
					attribute = new XMLAttribute(str.substring(pos + 1, equalIndex).trim(), this.offset + pos + 1,
							equalIndex - pos - 1);
					while (pos > 0 && charArray[pos] != '=') {
						pos--;
					}
					currentChar = charArray[pos];
				}

				if (currentChar == '=') {
					int equalIndex = pos;
					if (charArray[--pos] == ' ') {
						while (charArray[pos] == ' ' && pos > 0)
							pos--;
					}
					while (pos > 0 && charArray[pos] != ' ' && charArray[pos] != '\t' && charArray[pos] != '\r'
							&& charArray[pos] != '\n') {
						pos--;
					}
					if (attribute != null) {
						attribute.setName(str.substring(pos + 1, equalIndex).trim());
					}
				}
				if (attribute != null) {
					this.attributes.put(attribute.getName(), attribute);
				}
			}
		}
		return this.attributes;
	}

	public int getSelectionOffset() {
		if (getType().equals(IXMLPartitions.XML_TEXT)) {
			return this.getOffset();
		}
		String tagContent = getContent();
		if (tagContent.length() > 0) {
			for (int pos = 0; pos <= tagContent.length(); pos++) {
				if (tagContent.charAt(pos) != '<' && tagContent.charAt(pos) != '/') {
					return this.getOffset() + pos;
				}
			}
		}
		return 0;
	}

	public int getSelectionLength() {
		if (getType().equals(IXMLPartitions.XML_TEXT)) {
			return this.getLength();
		}
		return this.getTagName() != null ? this.getTagName().length() : 0;
	}

	/**
	 * Devuelve el String sobre el que est� posicionado el cursor
	 */
	public String getStringAt(int offset) {
		int relativeOffset = offset - this.offset;
		int start = 0, end = 0;
		String content = this.getContent();
		StringCharacterIterator iter = new StringCharacterIterator(content);
		char c;

		for (c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE && this.isFullIdentifierPart(c); c = iter
				.previous()) {
		}
		start = this.isFullIdentifierPart(iter.current()) ? iter.getIndex() : iter.getIndex() + 1;

		for (c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE && this.isFullIdentifierPart(c); c = iter
				.next()) {
		}
		end = iter.getIndex();

		return (start <= end) ? content.substring(start, end) : "";
	}

	private boolean isFullIdentifierPart(char c) {
		return c != '\"' && c != '\'' && c != '<' && c != '>' && c != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '?';
	}

	public boolean isEmpty() {
		return this.getContent() != null && this.getContent().trim().length() == 0;
	}

	public boolean isLastNode() {
		return this.document.getLength() == offset + length;
	}

	public boolean hasChildren() {
		return this.children != null ? this.children.size() > 0 : false;
	}

	public void setCorrespondingNode(XMLNode node) {
		this.correspondingNode = node;
	}

	public XMLNode getCorrespondingNode() {
		return this.correspondingNode;
	}

	public String toString() {
		return this.getType() + ": " + ((this.length > 0) ? "\"" + this.getContent() + "\"" : "Empty Tag");
	}

	public boolean accept(HierarchicalVisitor visitor) {
		// If I would have separated the concept of composite element and simple
		// element (composite and leaf in a tree), this if will be resolved
		// polimorphically :(

		// if it is a leaf
		if (!this.hasChildren() && this.getCorrespondingNode() == null) {
			return visitor.visitChild(this);
		}

		// if it is a composite node
		if (visitor.visitStart(this)) {
			boolean accept = true;
			Iterator<XMLNode> iterator = this.getChildren().iterator();
			try {
				while (iterator.hasNext() && accept) {
						XMLNode child = iterator.next();
						accept = child.accept(visitor);
				}
			} catch (ConcurrentModificationException e) {
				XMLEditorPlugin.logErrorMessage("ConcurrentModificationException iterating childs of " + this.getContent(), e);
			}
		}
		return visitor.visitEnd(this);
	}

	public boolean isRoot() {
		XMLNode node = this.getParent();
		while (node != null && !node.isTag()) {
			node = node.getParent();
		}
		return (node == null || node instanceof XMLRootNode) ? true : false;
	}

	public boolean isTag() {
		return IXMLPartitions.XML_TAG.equals(this.getType());
	}

	public boolean isEndTag() {
		return IXMLPartitions.XML_ENDTAG.equals(this.getType());
	}

	public boolean isIncompleteTag() {
		return IXMLPartitions.XML_INCOMPLETETAG.equals(this.getType());
	}

	public boolean isEmptyTag() {
		return IXMLPartitions.XML_EMPTYTAG.equals(this.getType());
	}

	public boolean isTextTag() {
		return IXMLPartitions.XML_TEXT.equals(this.getType());
	}

	public boolean isCommentTag() {
		return IXMLPartitions.XML_COMMENT.equals(this.getType());
	}

	public boolean isDeclarationTag() {
		return IXMLPartitions.XML_COMMENT.equals(this.getType());
	}

	public boolean isPiTag() {
		return IXMLPartitions.XML_PI.equals(this.getType());
	}

	public boolean isCdata() {
		return IXMLPartitions.XML_CDATA.equals(this.getType());
	}

}
