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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * 
 * @author ccancinos
 */
public class XmlDocumentFormatter {

	private static class CommentReader extends TagReader {

		private boolean complete = false;

		protected void clear() {
			this.complete = false;
		}

		public String getStartOfTag() {
			return "<!--";
		}

		protected String readTag() throws IOException {
			int intChar;
			char c;
			StringBuffer node = new StringBuffer();
			while (!complete && (intChar = reader.read()) != -1) {
				c = (char) intChar;
				node.append(c);
				if (c == '>' && node.toString().endsWith("-->")) {
					complete = true;
				}
			}
			return node.toString();
		}
	}

	private static class DoctypeDeclarationReader extends TagReader {
		private boolean complete = false;

		protected void clear() {
			this.complete = false;
		}

		public String getStartOfTag() {
			return "<!"; //$NON-NLS-1$
		}

		protected String readTag() throws IOException {
			int intChar;
			char c;
			StringBuffer node = new StringBuffer();
			while (!complete && (intChar = reader.read()) != -1) {
				c = (char) intChar;
				node.append(c);
				if (c == '>') {
					complete = true;
				}
			}
			return node.toString();
		}

	}

	private static class ProcessingInstructionReader extends TagReader {

		private boolean complete = false;

		protected void clear() {
			this.complete = false;
		}

		public String getStartOfTag() {
			return "<?";
		}

		protected String readTag() throws IOException {
			int intChar;
			char c;
			StringBuffer node = new StringBuffer();
			while (!complete && (intChar = reader.read()) != -1) {
				c = (char) intChar;
				node.append(c);
				if (c == '>' && node.toString().endsWith("?>")) { //$NON-NLS-1$
					complete = true;
				}
			}
			return node.toString();
		}
	}

	private static abstract class TagReader {

		protected Reader reader;
		private String tagText;

		protected abstract void clear();

		public int getPostTagDepthModifier() {
			return 0;
		}

		public int getPreTagDepthModifier() {
			return 0;
		}

		abstract public String getStartOfTag();

		public String getTagText() {
			return this.tagText;
		}

		public boolean isTextNode() {
			return false;
		}

		protected abstract String readTag() throws IOException;

		public boolean requiresInitialIndent() {
			return true;
		}

		public void setReader(Reader reader) throws IOException {
			this.reader = reader;
			this.clear();
			this.tagText = readTag();
		}

		public boolean startsOnNewline() {
			return true;
		}
	}

	private static class TagReaderFactory {

		// Warning: the order of the Array is important!
		private static TagReader[] tagReaders = new TagReader[] { new CommentReader(), new DoctypeDeclarationReader(),
				new ProcessingInstructionReader(), new XmlElementReader() };

		private static TagReader textNodeReader = new TextReader();

		public static TagReader createTagReaderFor(Reader reader) throws IOException {

			char[] buf = new char[10];
			reader.mark(10);
			reader.read(buf, 0, 10);
			reader.reset();

			String startOfTag = String.valueOf(buf);

			for (int i = 0; i < tagReaders.length; i++) {
				if (startOfTag.startsWith(tagReaders[i].getStartOfTag())) {
					tagReaders[i].setReader(reader);
					return tagReaders[i];
				}
			}
			textNodeReader.setReader(reader);
			return textNodeReader;
		}
	}

	private static class TextReader extends TagReader {

		private boolean complete;
		private boolean isTextNode;

		protected void clear() {
			this.complete = false;
		}

		public String getStartOfTag() {
			return ""; //$NON-NLS-1$
		}

		public boolean isTextNode() {
			return this.isTextNode;
		}

		protected String readTag() throws IOException {
			StringBuffer node = new StringBuffer();
			while (!complete) {
				reader.mark(1);
				int intChar = reader.read();
				if (intChar == -1)
					break;
				char c = (char) intChar;
				if (c == '<') {
					reader.reset();
					complete = true;
				} else {
					node.append(c);
				}
			}

			// if this text node is just whitespace
			// remove it, except for the newlines.
			if (node.length() < 1) {
				this.isTextNode = false;
			} else if (node.toString().trim().length() == 0) {
				String whitespace = node.toString();
				node = new StringBuffer();
				for (int i = 0; i < whitespace.length(); i++) {
					char whitespaceCharacter = whitespace.charAt(i);
					if (whitespaceCharacter == '\n' || whitespaceCharacter == '\r') {
						node.append(whitespaceCharacter);
					}
				}
				this.isTextNode = false;
			} else {
				this.isTextNode = true;
			}
			return node.toString();
		}

		public boolean requiresInitialIndent() {
			return false;
		}

		public boolean startsOnNewline() {
			return false;
		}
	}

	private static class XmlElementReader extends TagReader {

		private boolean complete = false;

		protected void clear() {
			this.complete = false;
		}

		public int getPostTagDepthModifier() {
			if (getTagText().endsWith("/>") || getTagText().endsWith("/ >")) {
				return 0;
			} else if (getTagText().startsWith("</")) {
				return 0;
			} else {
				return +1;
			}
		}

		public int getPreTagDepthModifier() {
			if (getTagText().startsWith("</")) {
				return -1;
			}
			return 0;
		}

		public String getStartOfTag() {
			return "<";
		}

		protected String readTag() throws IOException {

			StringBuffer node = new StringBuffer();

			boolean insideQuote = false;
			int intChar;

			while (!complete && (intChar = reader.read()) != -1) {
				char c = (char) intChar;
				node.append(c);
				// TODO logic incorrectly assumes that " is quote character
				// when it could also be '
				if (c == '"') {
					insideQuote = !insideQuote;
				}
				if (c == '>' && !insideQuote) {
					complete = true;
				}
			}
			return node.toString();
		}
	}

	private int depth;
	private StringBuffer formattedXml;
	private boolean lastNodeWasText;
	private String fDefaultLineDelimiter = "";

	public XmlDocumentFormatter() {
		super();
		depth = -1;
	}

	private void copyNode(Reader reader, StringBuffer out) throws IOException {

		TagReader tag = TagReaderFactory.createTagReaderFor(reader);
		depth = depth + tag.getPreTagDepthModifier();
		if (!lastNodeWasText) {
			if (tag.startsOnNewline() && !hasNewlineAlready(out)) {
				out.append(fDefaultLineDelimiter);
			}
			if (tag.requiresInitialIndent()) {
				out.append(indent("\t"));
			}
		}
		out.append(tag.getTagText());
		depth = depth + tag.getPostTagDepthModifier();
		lastNodeWasText = tag.isTextNode();

	}

	/**
	 * Returns the indent of the given string.
	 * 
	 * @param line
	 *            the text line
	 * @param tabWidth
	 *            the width of the '\t' character.
	 */
	public static int computeIndent(String line, int tabWidth) {
		int result = 0;
		int blanks = 0;
		int size = line.length();
		for (int i = 0; i < size; i++) {
			char c = line.charAt(i);
			if (c == '\t') {
				result++;
				blanks = 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					result++;
					blanks = 0;
				}
			} else {
				return result;
			}
		}
		return result;
	}

	/**
	 * Indent char is a space char but not a line delimiters.
	 * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
	 */
	public static boolean isIndentChar(char ch) {
		return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
	}

	/**
	 * Line delimiter chars are '\n' and '\r'.
	 */
	public static boolean isLineDelimiterChar(char ch) {
		return ch == '\n' || ch == '\r';
	}

	public String format(String documentText) {

		Assert.isNotNull(documentText);
		Reader reader = new StringReader(documentText);
		formattedXml = new StringBuffer();
		if (depth == -1) {
			depth = 0;
		}
		lastNodeWasText = false;
		try {
			while (true) {
				reader.mark(1);
				int intChar = reader.read();
				reader.reset();

				if (intChar != -1) {
					copyNode(reader, formattedXml);
				} else {
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			// XPathUtilLog.logError(XPathWorkbookActivator.getDefault(),
			// e.toString());
		}
		return formattedXml.toString();
	}

	private boolean hasNewlineAlready(StringBuffer out) {
		return out.lastIndexOf("\n") == formattedXml.length() - 1 || out.lastIndexOf("\r") == formattedXml.length() - 1;
	}

	private String indent(String canonicalIndent) {
		StringBuffer indent = new StringBuffer(30);
		for (int i = 0; i < depth; i++) {
			indent.append(canonicalIndent);
		}
		return indent.toString();
	}

	public void setInitialIndent(int indent) {
		depth = indent;
	}

	/**
	 * Returns the indentation of the line at <code>offset</code> as a
	 * <code>StringBuffer</code>. If the offset is not valid, the empty string
	 * is returned.
	 * 
	 * @param offset
	 *            the offset in the document
	 * @return the indentation (leading whitespace) of the line in which
	 *         <code>offset</code> is located
	 */
	public static StringBuffer getLeadingWhitespace(int offset, IDocument document) {
		StringBuffer indent = new StringBuffer();
		try {
			IRegion line = document.getLineInformationOfOffset(offset);
			int lineOffset = line.getOffset();
			int nonWS = findEndOfWhiteSpace(document, lineOffset, lineOffset + line.getLength());
			indent.append(document.get(lineOffset, nonWS - lineOffset));
			return indent;
		} catch (BadLocationException e) {
			// XPathUtilLog.logError(XPathWorkbookActivator.getDefault(),
			// e.toString());
			return indent;
		}
	}

	/**
	 * Returns the first offset greater than <code>offset</code> and smaller
	 * than <code>end</code> whose character is not a space or tab character. If
	 * no such offset is found, <code>end</code> is returned.
	 * 
	 * @param document
	 *            the document to search in
	 * @param offset
	 *            the offset at which searching start
	 * @param end
	 *            the offset at which searching stops
	 * @return the offset in the specifed range whose character is not a space
	 *         or tab
	 * @exception BadLocationException
	 *                if position is an invalid range in the given document
	 */
	public static int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	/**
	 * Creates a string that represents one indent (can be spaces or tabs..)
	 * 
	 * @return one indentation
	 */
	public static StringBuffer createIndent() {
		StringBuffer oneIndent = new StringBuffer();
		oneIndent.append('\t');
		return oneIndent;
	}

	public void setDefaultLineDelimiter(String defaultLineDelimiter) {
		fDefaultLineDelimiter = defaultLineDelimiter;

	}
}
