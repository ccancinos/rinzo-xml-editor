package ar.com.tadp.xml.rinzo.core.contentassist.processors;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;

import ar.com.tadp.xml.rinzo.core.AbstractRinzoTest;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * @author ccancinos
 * 
 */
public class XMLContentAssistProcessorTest extends AbstractRinzoTest {

	@Test
	public void testBodyTagProposals() throws Exception {
		Collection<String> expectedResult = Arrays.asList("alias", "bean",
				"import", "util:constant", "util:list", "util:map",
				"util:properties", "util:property-path", "util:set",
				"description");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 915;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(
				offset);
		processor.addBodyProposals(currentNode, prefix,
				this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(10, resultList.size());
		assertTrue(this.successResult(resultList, expectedResult));
	}

	@Test
	public void testTagsAttrsProposals() throws Exception {
		Collection<String> expected = Arrays.asList("abstract", "autowire",
				"autowire-candidate", "dependency-check", "depends-on",
				"destroy-method", "factory-bean", "factory-method",
				"init-method", "lazy-init", "name", "parent", "scope");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 708;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(
				offset);
		processor.addAttributeProposals(currentNode, prefix,
				this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(13, resultList.size());
		assertTrue(this.successResult(resultList, expected));
	}

	private boolean successResult(Collection<ICompletionProposal> resultList,
			Collection<String> expected) {
		for (String expectedProposal : expected) {
			for (ICompletionProposal iCompletionProposal : resultList) {
				if (expectedProposal.equals(iCompletionProposal
						.getDisplayString())) {
					return true;
				}
			}
		}
		return false;
	}

}
