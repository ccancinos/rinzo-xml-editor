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
package ar.com.tadp.xml.rinzo.core.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.visitor.StringGeneratorVisitor;
import ar.com.tadp.xml.rinzo.core.model.visitor.ToStringVisitor;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * An abstract class containing the template behavior for all actions who wants
 * to modify the content of the current edited document by using a
 * {@link StringGeneratorVisitor}.
 * 
 * @author ccancinos
 */
public abstract class VisitorAction implements IEditorActionDelegate {
	private RinzoXMLEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = (RinzoXMLEditor) targetEditor;
	}

	public void run(IAction action) {
		try {
			IDocument document = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput());
			String lineSeparator = this.editor.getLineSeparator();
			this.editor.getModel().createTree(document);
			XMLNode rootNode = this.editor.getModel().getTree().getRootNode();
            ISourceViewer sourceViewerEditor = editor.getSourceViewerEditor();
            Point selectedRange = sourceViewerEditor.getSelectedRange();
            StringGeneratorVisitor visitor = getVisitor();
            if (selectedRange.y <= 0) {
                runOnFullDocument(document, lineSeparator, rootNode, visitor);
            } else {
                runOnSelection(document, lineSeparator, rootNode, selectedRange, visitor);
            }
		} catch (BadLocationException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void runOnFullDocument(IDocument document, String lineSeparator, XMLNode rootNode, StringGeneratorVisitor visitor)
			throws BadLocationException {
		if (rootNode != null) {
			int prevWitespacesNumberOnLine = getPrevWitespacesNumberOnLine(document.get(), rootNode);
			int initialOffset = rootNode.getOffset() - prevWitespacesNumberOnLine;
			int length = rootNode.getCorrespondingNode().getOffset() + rootNode.getCorrespondingNode().getLength();
			if (visitor instanceof ToStringVisitor) {
				((ToStringVisitor) visitor).setLineSeparator(lineSeparator);
			}
			visitor.reset();
			rootNode.accept(visitor);
			document.replace(initialOffset, length - initialOffset, visitor.getString());
			this.editor.getModel().createTree(document);
		}
	}

	private void runOnSelection(IDocument document, String lineSeparator, XMLNode rootNode, Point selectedRange,
			StringGeneratorVisitor visitor) throws BadLocationException {
		List<XMLNode> selectedNodes = getNodesBySelection(selectedRange.x, selectedRange.y, rootNode);
		XMLNode selectedNode = null;
		if (selectedNodes != null) {
		    selectedNode = selectedNodes.get(0);
		}
		if (selectedNode != null) {
		    XMLNode lastNode = selectedNodes.get(selectedNodes.size() - 1);
		    int originOffset = selectedNode.getOffset();
		    int originLength;
		    XMLNode lastCorrespondingNode = lastNode.getCorrespondingNode();
			if (lastCorrespondingNode == null) {
		        originLength = lastNode.getOffset() + lastNode.getLength() - originOffset;
		    } else {
		        originLength = lastCorrespondingNode.getOffset() + lastCorrespondingNode.getLength() - originOffset;
		    }
		    int replaceOffset;
		    int replaceLength;
		    int selectOffset;
		    int indent = 0;
		    String insertPrefix = "";

		    XMLNode previousSibling = getPreviosSibling(selectedNode);
		    if (previousSibling != null) {
		        // case: (<prevSiblingTag>[whitespaces]</prevSiblingTag>|prevSiblingText[whitespaces])<selectedTag>
		        // or
		        // case: <?xml version="1.0" encoding="UTF-8"?>\n[whitespaces]<selectedTag>
		        if (!previousSibling.isPiTag()) {
		            // case: (<prevSiblingTag>[whitespaces]</prevSiblingTag>|prevSiblingText[whitespaces])<selectedTag>
		            XMLNode previousCorrespondingNode = previousSibling.getCorrespondingNode();
		            int prevOffset;
		            int prevLength;
		            if (previousCorrespondingNode == null) {
		                prevOffset = previousSibling.getOffset();
		                prevLength = previousSibling.getLength();
		            } else {
		                prevOffset = previousCorrespondingNode.getOffset();
		                prevLength = previousCorrespondingNode.getLength();
		            }
		            if (document.getLineOfOffset(prevOffset + prevLength) == document.getLineOfOffset(selectedNode.getOffset())) {
		                // case: (<prevSiblingTag></prevSiblingTag>|prevSiblingText)[no_line_feed_whitespaces]<selectedTag>
		                replaceOffset = prevOffset + prevLength;
		                insertPrefix = lineSeparator;
		                selectOffset = replaceOffset + insertPrefix.length();
		                replaceLength = originLength + (originOffset - replaceOffset);                                
		            } else {
		                // case: (<prevSiblingTag></prevSiblingTag>|prevSiblingText)[line_feed_whitespaces]<selectedTag>
		                int prevWitespacesNumber = getPrevWitespacesNumberOnLine(document.get(), selectedNode);
		                replaceOffset = originOffset - prevWitespacesNumber;
		                replaceLength = originLength + prevWitespacesNumber;
		                selectOffset = replaceOffset;                                
		            }                            
		            
		            indent = getIndent(document, previousSibling);
		        } else {
		            // case: <?xml version="1.0" encoding="UTF-8"?>\n[whitespaces]<selectedTag>
		            int prevWitespacesNumber = getPrevWitespacesNumberOnLine(document.get(), selectedNode);
		            replaceOffset = originOffset - prevWitespacesNumber;
		            replaceLength = originLength + prevWitespacesNumber;
		            selectOffset = replaceOffset;
		        }
		    } else {
		        // case: <prevTag>[whitespaces]<selectedTag>
		        XMLNode parentNode = selectedNode.getParent();
		        if (document.getLineOfOffset(parentNode.getOffset() + parentNode.getLength()) == document.getLineOfOffset(selectedNode.getOffset())) {
		            // case: <prevTag>[no_line_feed_whitespaces]<selectedTag>
		            replaceOffset = parentNode.getOffset() + parentNode.getLength();
		            insertPrefix = lineSeparator;
		            selectOffset = replaceOffset + insertPrefix.length();
		            replaceLength = originLength + (originOffset - replaceOffset);
		        } else {
		            // case: <prevTag>[line_feed_whitespaces]<selectedTag>
		            int prevWitespacesNumber = getPrevWitespacesNumberOnLine(document.get(), selectedNode);
		            replaceOffset = originOffset - prevWitespacesNumber;
		            replaceLength = originLength + prevWitespacesNumber;
		            selectOffset = replaceOffset;
		        }
		        indent = getIndent(document, parentNode) + 1; // parent indent + 1
		    }
		    
		    // format all selected siblings and separate them by line
		    StringBuilder formatedXml = new StringBuilder(insertPrefix);
		    if (visitor instanceof ToStringVisitor) {
		        ((ToStringVisitor) visitor).setLineSeparator(lineSeparator);
		    }
		    for (int i = 0; i < selectedNodes.size(); i++) {
		        if (i > 0) {
		            formatedXml.append(lineSeparator);
		        }
		        visitor.reset();
		        if (visitor instanceof ToStringVisitor) {
		            ((ToStringVisitor) visitor).setAddIndentation(indent);
		        }
		        XMLNode doNode = selectedNodes.get(i);
		        doNode.accept(visitor);
		        formatedXml.append(visitor.getString());
		    }
		    
		    document.replace(replaceOffset, replaceLength, formatedXml.toString());
		    this.editor.getModel().createTree(document);
		    editor.getSourceViewerEditor().setSelectedRange(selectOffset, formatedXml.length() - insertPrefix.length());
		}
	}

	protected abstract StringGeneratorVisitor getVisitor();

	public void selectionChanged(IAction action, ISelection selection) {
	}

	protected XMLNode getSelectedNode() {
		return this.editor.getModel().getTree().getActiveNode();
	}
	
    private int getPrevWitespacesNumberOnLine(String str, XMLNode node) {
		if (!StringUtils.isEmpty(str)) {
			int offset = node.getOffset();
			char ch;
			if (offset > 0) {
				do {
					offset--;
					ch = str.charAt(offset);
				} while (ch != '\r' && ch != '\n' && offset > 0);
				return node.offset - offset - 1;
			}
			return node.offset - offset;
		} else {
			return 0;
		}
    }

    /**
     * Number of indents from start of line to beginning of tag TODO: review it
     * 
     * @param document
     * @param node
     * @return
     */
    private int getIndent(IDocument document, XMLNode node) {
        String str = document.get();
        String indentToken = XMLEditorPlugin.getDefault().getIndentToken();
        try {
            int startOffset = document.getLineOffset(document.getLineOfOffset(node.getOffset()));
            String indentStr = str.substring(startOffset, node.getOffset());
			if (indentToken.startsWith(FileUtils.TAB)) {
                int width = Integer.parseInt(XMLEditorPlugin.getDefault().getPreferenceStore().getString("tabWidth"));
                String replaceStr = new String(new char[width]).replace('\0', ' ');
				indentStr = indentStr.replace(replaceStr, FileUtils.TAB);
                indentStr = indentStr.replace(" ", "");
                return indentStr.length();

            } else {
				indentStr = indentStr.replace(FileUtils.TAB, indentToken);
                int indent = indentStr.length() / indentToken.length();
                int mod = indentStr.length() % indentToken.length();
                if (mod != 0) {
                    indent++; // round upper
                }
                return indent;
            }
        } catch (BadLocationException e) {
            new RuntimeException(e);
        }
        return 3;
    }

    private XMLNode getPreviosSibling(XMLNode node) {
        XMLNode parentNode = node.getParent();
        List<XMLNode> children = parentNode.getChildren();
        XMLNode previousNode = children.get(0);
        if (previousNode.equals(node)) {
            return null; // no sibling
        }
        if (previousNode.isTextTag() && isWhitespace(previousNode.getContent())) {
            previousNode = null;
        }
        for (int i = 1; i < children.size(); i++) {
            XMLNode currentNode = children.get(i);
            if (currentNode.equals(node)) {
                return previousNode;
            }
            if (!currentNode.isTextTag() || !isWhitespace(currentNode.getContent())) {
                previousNode = currentNode;
            }
        }
        throw new RuntimeException("Not found previous sibling nor parent");
    }
    
    private static XMLNode getNextNonspaceSibling(XMLNode node) {
        XMLNode parentNode = node.getParent();
        List<XMLNode> children = parentNode.getChildren();
        boolean found = false;
        for (int i = 0; i < children.size(); i++) {
            XMLNode currentNode = children.get(i);
            if (found) {
                if (!currentNode.isTextTag() || !isWhitespace(currentNode.getContent())) {
                    return currentNode;
                }
            } else {
                if (currentNode.equals(node)) {
                    found = true;
                }                
            }
        }
        return null;
    }

    /**
     * True when there is no other char then whitespace Empty string is also
     * whitespace
     * 
     * @param str
     * @return
     */
    private static boolean isWhitespace(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<XMLNode> getNodesBySelection(int startOffset, int length, XMLNode node) {
        if (node.isTextTag() && isWhitespace(node.getContent())) {
            return null;
        }
        if (startOffset < node.getOffset()) {
            if (startOffset + length > node.getOffset()) {
                ArrayList<XMLNode> nodesBySelection = new ArrayList<XMLNode>();
                nodesBySelection.add(node);
                XMLNode nextNonspaceSibling = node;
                while ((nextNonspaceSibling = getNextNonspaceSibling(nextNonspaceSibling)) != null)  {
                    if (nextNonspaceSibling.getOffset() < startOffset + length) {
                        nodesBySelection.add(nextNonspaceSibling);
                    } else {
                        break;
                    }
                }
                return nodesBySelection;
            }
            return null;
        } else {
            if (startOffset < node.getOffset() + node.getLength()) {
                ArrayList<XMLNode> nodesBySelection = new ArrayList<XMLNode>();
                nodesBySelection.add(node);
                XMLNode nextNonspaceSibling = node;
                while ((nextNonspaceSibling = getNextNonspaceSibling(nextNonspaceSibling)) != null)  {
                    if (nextNonspaceSibling.getOffset() < startOffset + length) {
                        nodesBySelection.add(nextNonspaceSibling);
                    } else {
                        break;
                    }
                }                
                return nodesBySelection;
            }
            List<XMLNode> children = node.getChildren();
            for (XMLNode xmlNode : children) {
                List<XMLNode> nodesBySelection = getNodesBySelection(startOffset, length, xmlNode);
                if (nodesBySelection != null) {
                    return nodesBySelection;
                }
            }
        }
        return null;
    }	
}
