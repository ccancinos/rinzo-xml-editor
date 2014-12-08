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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;

/**
 * In charge of evaluating a String containing an XPath expression.
 * 
 * @author ccancinos
 */
public class XPathEvaluator {
	private XPath xpath;
	private DocumentBuilder documentBuilder;

	public XPathEvaluator() {
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	public String evaluate(String xpathExpression, String source) {
		StringBuffer buffer = new StringBuffer();
		try {
			Document document = this.getDocumentBuilder().parse(this.createInputSource(source));
			NodeList selectedNodes = (NodeList) this.xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
			if (selectedNodes.getLength() != 0) {
				for (int i = 0; i < selectedNodes.getLength(); i++) {
					Node node = selectedNodes.item(i);
					buffer.append(this.xmlToString(node));
					buffer.append("\n");
				}
			} else {
				buffer.append(this.xpath.evaluate(xpathExpression, this.createInputSource(source)));
			}
		} catch (Exception e) {
			throw new RuntimeException((e.getLocalizedMessage() != null) ? e.getLocalizedMessage() : e.getCause()
					.getLocalizedMessage(), e);
		}

		return buffer.toString();
	}

	private InputSource createInputSource(String string) {
		return new InputSource(new StringReader(string));
	}

	private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		if (this.documentBuilder == null) {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			this.documentBuilder = domFactory.newDocumentBuilder();
		}
		return this.documentBuilder;
	}

	private String xmlToString(Node node) {
		try {
			if (AttrImpl.class.isAssignableFrom(node.getClass())) {
				AttrImpl attr = (AttrImpl) node;
				return attr.toString();
			}
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);
			String string = stringWriter.getBuffer().toString();
			if (string.startsWith("<?")) {
				string = string.substring(string.indexOf("?>") + 2);
			}
			return string;
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

}
