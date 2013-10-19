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

import java.util.StringTokenizer;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;
import ar.com.tadp.xml.rinzo.core.utils.Utils;

/**
 * This visitor creates a formated String from a node and his children.
 * 
 * Improvements:
 * 1) If I have something like: <servlet-name>StripesDispatcher</servlet-name> it might not pass it to 3 lines.
 * 2) Take into account the previously formated node because of the CR. Because tags following a comment tag should not add an extra CR. 
 * This is really messy if I have something like this:
 *   <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
 *   <!--              Configuration                                          -->
 *   <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
 *
 * NOTE: Maybe is not such a good idea that the elements right inside the root element are separated by CR :-\
 * 3) Think about comments tags. I should let them to be separated as they want from anything else in order to gain clarity in the xml
 * 
 * @author ccancinos
 */
public class ToStringVisitor implements StringGeneratorVisitor {
	private StringBuffer buffer = new StringBuffer();
	private int identation = 0;
	private int addIndentation = 0;
	private boolean addLineSeparator = true;
	private String lineSeparator = System.getProperty("line.separator");
	
	public boolean visitStart(XMLNode node) {
		if(this.identation >0) {
			this.addLineSeparator();
		}
		this.addIdentation();
		this.addLine(node.getContent());
		this.identation++;
		
		if(!node.hasChildren() && node.getCorrespondingNode() == null) {
			this.addLineSeparator = false;
		}
		return true;
	}

	public boolean visitEnd(XMLNode node) {
		this.identation--;
		if(this.addLineSeparator) {
			this.addLineSeparator();
			this.addIdentation();
		} else {
			this.addLineSeparator = true;
		}
		this.addLine(node.getCorrespondingNode().getContent().trim());
		
		return true;
	}
	public boolean visitChild(XMLNode node) {
		String content = node.getContent();
		String trimmedContent = "";
		if ((node.isTextTag()) || (node.isCdata()) || (node.isEmptyTag())
                || (node.isCommentTag())) {
			trimmedContent = content.trim();
			if(Utils.isEmpty(trimmedContent)) {
				this.addBlankLines(content);
			}
		}
		if(trimmedContent.length() > 0) {
			boolean singleChild = node.getParent().getChildren().size() == 1;
			if(node.isCommentTag() || node.isEmptyTag() || trimmedContent.length() > 60 || !singleChild) {
				this.addLineSeparator();
				this.addIdentation();
			} else {
				if(singleChild) {
					this.addLineSeparator = false;
				}
			}
			this.addLine(content);
		}
		
		return true;
	}

	private void addBlankLines(String content) {
		int index = content.indexOf(lineSeparator);
		index = content.indexOf(lineSeparator, index + 1);
		while(index >= 0) {
			this.addLineSeparator();
			this.addIdentation();
			index = content.indexOf(lineSeparator, index + 1);
		}
	}

	private void addIdentation() {
		for (int i = 0; i < identation + this.addIndentation; i++) {
			this.buffer.append(this.getIndentToken());
		}
	}

	private void addLineSeparator() {
		this.buffer.append(lineSeparator);
	}

	public void reset() {
		this.buffer = new StringBuffer();
		this.addIndentation = 0;
	}

	public String getString() {
		return this.buffer.toString();
	}
	
	private void addLine(String line) {
		if(line.length() <= this.getMaxLineLength()) {
			this.buffer.append(line);
		} else {
			StringTokenizer tokenizer = new StringTokenizer(line, " \n\r\t");
			StringBuffer currentLine = new StringBuffer();
			StringBuffer finalLine = new StringBuffer();
			while(tokenizer.hasMoreTokens()) {
				while(tokenizer.hasMoreTokens() && currentLine.length() <= this.getMaxLineLength()) {
					String nextToken = tokenizer.nextToken().trim();
                    currentLine.append((currentLine.toString().trim().length() == 0) ? nextToken : " " + nextToken);
				}
				if(currentLine.length() != 0) {
					if(tokenizer.hasMoreTokens()) {
						currentLine.append(lineSeparator);
					}
					finalLine.append(currentLine.toString());
					currentLine = new StringBuffer();
					for (int i = 0; i < identation + 1 + this.addIndentation; i++) {
						currentLine.append(this.getIndentToken());
					}
				}
			}
			this.buffer.append(finalLine.toString());
		}
	}

	private int getMaxLineLength() {
		return XMLEditorPlugin.getMaximumLineWidth();
	}

	private String getIndentToken() {
		return XMLEditorPlugin.getDefault().getIndentToken();
	}
    
    public void setAddIndentation(int addIndentation) {
        this.addIndentation = addIndentation;
    }	

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}
