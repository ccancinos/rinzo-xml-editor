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
package ar.com.tadp.xml.rinzo.core.partitioner;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * @author ccancinos
 */
public class XMLPartitionScanner extends RuleBasedPartitionScanner {

	public static final String CONTENT_TYPES[] = { IXMLPartitions.XML_TAG, IXMLPartitions.XML_EMPTYTAG,
			IXMLPartitions.XML_INCOMPLETETAG, IXMLPartitions.XML_ENDTAG, IXMLPartitions.XML_TEXT,
			IXMLPartitions.XML_COMMENT, IXMLPartitions.XML_DECLARATION, IXMLPartitions.XML_PI,
			IXMLPartitions.XML_CDATA, "__dftl_partition_content_type" };

	public static final IToken TOKEN_XML_TAG = new Token(IXMLPartitions.XML_TAG);
	public static final IToken TOKEN_XML_ENDTAG = new Token(IXMLPartitions.XML_ENDTAG);
	public static final IToken TOKEN_XML_INCOMPLETETAG = new Token(IXMLPartitions.XML_INCOMPLETETAG);
	public static final IToken TOKEN_XML_EMPTYTAG = new Token(IXMLPartitions.XML_EMPTYTAG);
	public static final IToken TOKEN_XML_TEXT = new Token(IXMLPartitions.XML_TEXT);
	public static final IToken TOKEN_XML_COMMENT = new Token(IXMLPartitions.XML_COMMENT);
	public static final IToken TOKEN_XML_UNDEFINED = new Token(IXMLPartitions.XML_UNDEFINED);
	public static final IToken TOKEN_XML_DECLARATION = new Token(IXMLPartitions.XML_DECLARATION);
	public static final IToken TOKEN_XML_PI = new Token(IXMLPartitions.XML_PI);
	public static final IToken TOKEN_XML_CDATA = new Token(IXMLPartitions.XML_CDATA);

	public XMLPartitionScanner() {
		IPredicateRule rules[] = new IPredicateRule[1];
		rules[0] = new XMLRule2();
		setPredicateRules(rules);
	}
}
