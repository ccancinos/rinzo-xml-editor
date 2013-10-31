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
package ar.com.tadp.xml.rinzo.core.keyListeners;

import java.util.Scanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;

/**
 * This handler switch between commenting and uncommenting the text selection
 * depending if the current selection is commented or not.
 * 
 * This action is perform in different ways depending if the selection
 * corresponds to a single line or multiple lines.
 * 
 * In case of a single line selection the commented text will result in: <!--
 * text -->
 * 
 * When the text selection corresponds to multiple lines the commented selection
 * will be: <!-- text -->
 * 
 * @author ccancinos
 */
public class CommentSelectionHandler extends KeyAdapter {
	private static final String COMMENT_END = "-->";
	private static final String COMMENT_START = "<!--";

	private final ISourceViewer sourceViewer;
	private IDocument document;
	private String lineSeparator;

	public CommentSelectionHandler(RinzoXMLEditor xmlEditor) {
		this.sourceViewer = xmlEditor.getSourceViewerEditor();
		this.lineSeparator = xmlEditor.getLineSeparator();
		this.document = this.sourceViewer.getDocument();
	}

	public void keyReleased(KeyEvent keyevent) {
		try {
			if (this.isCommentAcceleratorKey(keyevent)) {
				TextSelection selection = this.getTextSelection();

				if (selection.getStartLine() == selection.getEndLine()) {
					// Single line selection
					int currentLineNumber = this.document.getLineOfOffset(selection.getOffset());
					int lineOffset = this.document.getLineOffset(currentLineNumber);
					int lineLength = this.document.getLineLength(currentLineNumber);

					if (this.document.get(lineOffset, lineLength).startsWith(COMMENT_START)) {
						this.manageUnCommentSingleLine(selection, currentLineNumber, lineOffset, lineLength);
					} else {
						this.manageCommentSingleLine(selection, currentLineNumber, lineOffset, lineLength);
					}
				} else {
					// Multiple line selection

					if (this.document.get(this.document.getLineOffset(selection.getStartLine()), selection.getOffset())
							.startsWith(COMMENT_START)) {
						this.manageUncommentMultipleSelection(selection);
					} else {
						this.manageCommentMultipleSelection(selection);
					}

				}
			}
		} catch (BadLocationException exception) {
			exception.printStackTrace();
		}
	}

	private boolean isCommentAcceleratorKey(KeyEvent keyevent) {
		return (keyevent.character == 55 || keyevent.character == '/') && keyevent.stateMask == SWT.CONTROL;
	}

	private TextSelection getTextSelection() {
		return (TextSelection) this.sourceViewer.getSelectionProvider().getSelection();
	}

	private void manageCommentMultipleSelection(TextSelection selection) throws BadLocationException {
		this.transformLines(selection, new LineTransformer() {
			public String transform(String line) {
				return line.startsWith(COMMENT_START) && line.endsWith(COMMENT_END) ? line + lineSeparator
						: COMMENT_START + line + COMMENT_END + lineSeparator;
			}
		});
	}

	private void manageUncommentMultipleSelection(TextSelection selection) throws BadLocationException {
		this.transformLines(selection, new LineTransformer() {
			public String transform(String line) {
				return line.startsWith(COMMENT_START) && line.endsWith(COMMENT_END) ? line.substring(
						COMMENT_START.length(), line.length() - COMMENT_END.length())
						+ lineSeparator : line + lineSeparator;
			}
		});
	}

	private void transformLines(TextSelection selection, LineTransformer lineTransformer) throws BadLocationException {
		int startLineOffset = this.document.getLineOffset(selection.getStartLine());
		int lastLineEndOffset = this.document.getLineOffset(selection.getEndLine())
				+ this.document.getLineLength(selection.getEndLine());
		int selectionLength = lastLineEndOffset - startLineOffset;

		Scanner scanner = new Scanner(this.document.get(startLineOffset, selectionLength));
		StringBuilder builder = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			builder.append(lineTransformer.transform(line));
		}
		String newContent = builder.toString();

		this.document.replace(startLineOffset, selectionLength, newContent);
		this.sourceViewer.setSelectedRange(startLineOffset, newContent.length());
	}

	private void manageCommentSingleLine(TextSelection selection, int currentLineNumber, int lineOffset, int lineLength)
			throws BadLocationException {
		this.document.replace(lineOffset, lineLength, COMMENT_START + this.document.get(lineOffset, lineLength - 2)
				+ COMMENT_END + this.lineSeparator);
		this.sourceViewer.setSelectedRange(lineOffset, lineLength + 5);
	}

	private void manageUnCommentSingleLine(TextSelection selection, int currentLineNumber, int lineOffset,
			int lineLength) throws BadLocationException {
		this.document.replace(lineOffset, lineLength, this.document.get(lineOffset + 4, lineLength - 9)
				+ this.lineSeparator);
		this.sourceViewer.setSelectedRange(lineOffset, lineLength - 8);
	}

	/**
	 * Used to modify the content of a line of text for another content
	 * 
	 * @author ccancinos
	 */
	private static interface LineTransformer {
		public String transform(String line);
	}

}
