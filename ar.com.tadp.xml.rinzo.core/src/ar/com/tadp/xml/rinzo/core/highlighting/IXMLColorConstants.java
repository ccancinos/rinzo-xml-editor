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

/**
 * Define coloring names for each partition
 * 
 * @author ccancinos
 */
public interface IXMLColorConstants {

	String XML_COMMENT = "comment";
	String PROC_INSTR = "procInstr";
	String DECLARATION = "declaration";
	String STRING = "attributeValue";
	String ATTRIBUTE = "attributeName";
	String DEFAULT = "text";
	String TAG = "tag";
	String CDATA = "cdata";

	String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
	String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";

}
