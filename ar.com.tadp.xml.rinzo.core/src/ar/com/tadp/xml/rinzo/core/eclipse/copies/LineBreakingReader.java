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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.BreakIterator;

import org.eclipse.swt.graphics.GC;

/*
 * Not a real reader. Could change if requested
 */
public class LineBreakingReader {

	private BufferedReader fReader;
	private GC fGC;
	private int fMaxWidth;

	private String fLine;
	private int fOffset;

	private BreakIterator fLineBreakIterator;
	private boolean fBreakWords;

	/**
	 * Creates a reader that breaks an input text to fit in a given width.
	 * 
	 * @param reader Reader of the input text
	 * @param gc The graphic context that defines the currently used font sizes
	 * @param maxLineWidth The max width (pixels) where the text has to fit in
	 */
	public LineBreakingReader(Reader reader, GC gc, int maxLineWidth) {
		fReader= new BufferedReader(reader);
		fGC= gc;
		fMaxWidth= maxLineWidth;
		fOffset= 0;
		fLine= null;
		fLineBreakIterator= BreakIterator.getLineInstance();
		fBreakWords= true;
	}

	public boolean isFormattedLine() {
		return fLine != null;
	}

	/**
	 * Reads the next line. The lengths of the line will not exceed the given maximum
	 * width.
	 * 
	 * @return the next line 
	 * @throws IOException 
	 */
	public String readLine() throws IOException {
		if (fLine == null) {
			String line= fReader.readLine();
			if (line == null)
				return null;

			int lineLen= fGC.textExtent(line).x;
			if (lineLen < fMaxWidth) {
				return line;
			}
			fLine= line;
			fLineBreakIterator.setText(line);
			fOffset= 0;
		}
		int breakOffset= findNextBreakOffset(fOffset);
		String res;
		if (breakOffset != BreakIterator.DONE) {
			res= fLine.substring(fOffset, breakOffset);
			fOffset= findWordBegin(breakOffset);
			if (fOffset == fLine.length()) {
				fLine= null;
			}
		} else {
			res= fLine.substring(fOffset);
			fLine= null;
		}
		return res;
	}

	private int findNextBreakOffset(int currOffset) {
		int currWidth= 0;
		int nextOffset= fLineBreakIterator.following(currOffset);
		while (nextOffset != BreakIterator.DONE) {
			String word= fLine.substring(currOffset, nextOffset);
			int wordWidth= fGC.textExtent(word).x;
			int nextWidth= wordWidth + currWidth;
			if (nextWidth > fMaxWidth) {
				if (currWidth > 0)
					return currOffset;

				if (!fBreakWords)
					return nextOffset;

				// need to fit into fMaxWidth
				int length= word.length();
				while (length >= 0) {
					length--;
					word= word.substring(0, length);
					wordWidth= fGC.textExtent(word).x;
					if (wordWidth + currWidth < fMaxWidth)
						return currOffset + length;
				}
				return nextOffset;
			}
			currWidth= nextWidth;
			currOffset= nextOffset;
			nextOffset= fLineBreakIterator.next();
		}
		return nextOffset;
	}

	private int findWordBegin(int idx) {
		while (idx < fLine.length() && Character.isWhitespace(fLine.charAt(idx))) {
			idx++;
		}
		return idx;
	}
}
