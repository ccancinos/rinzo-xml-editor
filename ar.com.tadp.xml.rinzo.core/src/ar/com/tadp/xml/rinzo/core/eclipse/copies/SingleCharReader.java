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

public abstract class SingleCharReader extends Reader {
	
	/**
	 * @see Reader#read()
	 */
	public abstract int read() throws IOException;

	/**
	 * @see Reader#read(char[],int,int)
	 */
	public int read(char cbuf[], int off, int len) throws IOException {
		int end= off + len;
		for (int i= off; i < end; i++) {
			int ch= read();
			if (ch == -1) {
				if (i == off) {
					return -1;
				} else {
					return i - off;
				}
			}
			cbuf[i]= (char)ch;
		}
		return len;
	}		
	
	/**
	 * @see Reader#ready()
	 */		
    public boolean ready() throws IOException {
		return true;
	}
	
	/**
	 * Gets the content as a String
	 */
	public String getString() throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch;
		while ((ch= read()) != -1) {
			buf.append((char)ch);
		}
		return buf.toString();
	}
}
