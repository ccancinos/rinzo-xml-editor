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
package ar.com.tadp.xml.rinzo.core.highlighting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * 
 * @author serranoc
 */
public class SingleCharacterWordDetector implements IWordDetector {

	private List<Character> _chars = new ArrayList<Character>();
    
    public void addChar(char c) {
        _chars.add(c);
    }
     
    public boolean isWordPart(char c) {
        return false;
    }
 
    public boolean isWordStart(char c) {
        return _chars.contains(c);
    }

}
