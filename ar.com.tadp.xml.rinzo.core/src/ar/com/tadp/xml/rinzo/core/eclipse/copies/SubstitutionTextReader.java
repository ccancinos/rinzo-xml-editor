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
package ar.com.tadp.xml.rinzo.core.eclipse.copies;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads the text contents from a reader and computes for each character
 * a potential substitution. The substitution may eat more characters than
 * only the one passed into the computation routine.
 */
abstract class SubstitutionTextReader extends SingleCharReader {

	protected static final String LINE_DELIM= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private Reader fReader;
	private boolean fWasWhiteSpace;
	private int fCharAfterWhiteSpace;

	/**
	 * Tells whether white space characters are skipped.
	 */
	private boolean fSkipWhiteSpace= true;

	private boolean fReadFromBuffer;
	private StringBuffer fBuffer;
	private int fIndex;


	protected SubstitutionTextReader(Reader reader) {
		fReader= reader;
		fBuffer= new StringBuffer();
		fIndex= 0;
		fReadFromBuffer= false;
		fCharAfterWhiteSpace= -1;
		fWasWhiteSpace= true;
	}

	/**
	 * Computes the substitution for the given character and if necessary
	 * subsequent characters. Implementation should use <code>nextChar</code>
	 * to read subsequent characters.
	 *
	 * @param c the character to be substituted
	 * @return the substitution for <code>c</code>
	 * @throws IOException in case computing the substitution fails
	 */
	protected abstract String computeSubstitution(int c) throws IOException;

	/**
	 * Returns the internal reader.
	 *
	 * @return the internal reader
	 */
	protected Reader getReader() {
		return fReader;
	}

	/**
	 * Returns the next character.
	 * @return the next character
	 * @throws IOException in case reading the character fails
	 */
	protected int nextChar() throws IOException {
		fReadFromBuffer= (fBuffer.length() > 0);
		if (fReadFromBuffer) {
			char ch= fBuffer.charAt(fIndex++);
			if (fIndex >= fBuffer.length()) {
				fBuffer.setLength(0);
				fIndex= 0;
			}
			return ch;
		}

		int ch= fCharAfterWhiteSpace;
		if (ch == -1) {
			ch= fReader.read();
		}
		if (fSkipWhiteSpace && Character.isWhitespace((char)ch)) {
			do {
				ch= fReader.read();
			} while (Character.isWhitespace((char)ch));
			if (ch != -1) {
				fCharAfterWhiteSpace= ch;
				return ' ';
			}
		} else {
			fCharAfterWhiteSpace= -1;
		}
		return ch;
	}

	/**
	 * @see Reader#read()
	 */
	public int read() throws IOException {
		int c;
		do {

			c= nextChar();
			while (!fReadFromBuffer) {
				String s= computeSubstitution(c);
				if (s == null)
					break;
				if (s.length() > 0)
					fBuffer.insert(0, s);
				c= nextChar();
			}

		} while (fSkipWhiteSpace && fWasWhiteSpace && (c == ' '));
		fWasWhiteSpace= (c == ' ' || c == '\r' || c == '\n');
		return c;
	}

	/**
	 * @see Reader#ready()
	 */
    public boolean ready() throws IOException {
		return fReader.ready();
	}

	/**
	 * @see Reader#close()
	 */
	public void close() throws IOException {
		fReader.close();
	}

	/**
	 * @see Reader#reset()
	 */
	public void reset() throws IOException {
		fReader.reset();
		fWasWhiteSpace= true;
		fCharAfterWhiteSpace= -1;
		fBuffer.setLength(0);
		fIndex= 0;
	}

	protected final void setSkipWhitespace(boolean state) {
		fSkipWhiteSpace= state;
	}

	protected final boolean isSkippingWhitespace() {
		return fSkipWhiteSpace;
	}
}
