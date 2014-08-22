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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;

import ar.com.tadp.xml.rinzo.core.AbstractRinzoTest;

/**
 * 
 * @author ccancinos
 */
public class FormatDocumentTest extends AbstractRinzoTest {

	@Test
	public void formatExample() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatExample.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatExampleExpected.xml");
	}

	@Test
	public void addNewLinesExample() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/addNewLines.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/addNewLinesExpected.xml");
	}
	
	@Test
	public void formatKeepInnerText() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatScript.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatScriptExpected.xml");
	}
	
	@Test
	public void formatCdata() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/cdata.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/cdataExpected.xml");
	}
	
	@Test
	public void formatNonSchemaRootTag() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatNonSchemaRootTag.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatNonSchemaRootTagExpected.xml");
	}
	
	@Test
	public void formatSchemaRootTag() throws Exception {
		this.validateFormatting(
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatSchemaRootTag.xml",
				"src/ar/com/tadp/xml/rinzo/core/model/visitor/formatSchemaRootTagExpected.xml");
	}

	private void validateFormatting(String inputXml, String expectedXml)
			throws FileNotFoundException, CoreException, IOException {
		this.useFile(inputXml);
		ToStringVisitor visitor = new ToStringVisitor();
		this.xmlTreeModel.getTree().getRootNode().accept(visitor);
		String expectedContent = this.getExpectedFileContent(expectedXml);
		Assert.assertEquals(expectedContent, visitor.getString());
	}

}
