package ar.com.tadp.xml.rinzo.core.model.visitor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ar.com.tadp.xml.rinzo.core.AbstractRinzoTest;
import ar.com.tadp.xml.rinzo.core.model.visitor.ToStringVisitor;

/**
 * 
 * @author ccancinos
 */
public class FormatDocumentTest extends AbstractRinzoTest {

	private static final String INPUT_XML = "src/ar/com/tadp/xml/rinzo/core/model/visitor/formatExample.xml";
	private static final String EXPECTED_XML = "src/ar/com/tadp/xml/rinzo/core/model/visitor/formatExampleExpected.xml";

	@Before
	public void init() throws Exception {
		this.useFile(INPUT_XML);
	}

	@After
	public void finalize() throws Exception {
		if (this.editor != null) {
			this.editor.close(false);
		}
	}

	@Test
	public void toStringVisitor() throws Exception {
		ToStringVisitor visitor = new ToStringVisitor();
		this.xmlTreeModel.getTree().getRootNode().accept(visitor);
		String expectedContent = this.getExpectedFileContent(EXPECTED_XML);
		Assert.assertEquals(expectedContent, visitor.getString());
	}

}
