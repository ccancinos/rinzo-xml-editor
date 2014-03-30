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

	private void validateFormatting(String inputXml, String expectedXml)
			throws FileNotFoundException, CoreException, IOException {
		this.useFile(inputXml);
		ToStringVisitor visitor = new ToStringVisitor();
		this.xmlTreeModel.getTree().getRootNode().accept(visitor);
		String expectedContent = this.getExpectedFileContent(expectedXml);
		Assert.assertEquals(expectedContent, visitor.getString());
	}

}
