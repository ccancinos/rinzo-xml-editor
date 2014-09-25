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
	public void testRootBodyTagProposals() throws Exception {
		Collection<String> expectedResult = Arrays.asList("aop:aspectj-autoproxy", "aop:config", "aop:scoped-proxy",
				"aop-spring-configure", "beans:alias", "beans:bean", "beans:import", "job", "job-listener",
				"job-repository", "step", "step-listener", "tx:advice", "tx:annotation-drive", "beans:description");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 1220;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(offset);
		processor.addBodyProposals(currentNode, prefix, this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(expectedResult.size(), resultList.size());
		assertTrue(this.successResult(resultList, expectedResult));
	}

	@Test
	public void testTagsAttrsProposals() throws Exception {
		Collection<String> expected = Arrays.asList("abstract", "autowire", "autowire-candidate", "dependency-check",
				"depends-on", "destroy-method", "factory-bean", "factory-method", "init-method", "lazy-init", "name",
				"parent", "scope");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 1216;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(offset);
		processor.addAttributeProposals(currentNode, prefix, this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(expected.size(), resultList.size());
		assertTrue(this.successResult(resultList, expected));
	}
	
	@Test
	public void testTagsAttrsValuesProposals() throws Exception {
		Collection<String> expected = Arrays.asList("Audi", "Golf", "BMW");
		this.copyFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/orden.xsd");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/orden.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 442;
		String prefix = "name=\"";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(offset);
		processor.addAttributeValuesProposals(currentNode, "name", prefix, this.editor.getSourceViewerEditor(), offset, resultList);
		
		assertEquals(expected.size(), resultList.size());
		assertTrue(this.successResult(resultList, expected));
	}

	@Test
	public void testBodyTagProposals() throws Exception {
		Collection<String> expectedResult = Arrays.asList("aop:include");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 1265;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(offset);
		processor.addBodyProposals(currentNode, prefix, this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(expectedResult.size(), resultList.size());
		assertTrue(this.successResult(resultList, expectedResult));
	}

	@Test
	public void testBodyTagProposals2() throws Exception {
		Collection<String> expectedResult = Arrays.asList("aop:aspectj-autoproxy", "aop:config", "aop:scoped-proxy",
				"aop-spring-configure", "beans:concstructor-arg", "beans:description", "beans:lookup-method",
				"beans:meta", "beans:property", "beans:replaced-method", "job", "job-listener", "job-repository",
				"step", "step-listener", "tx:advice", "tx:annotation-drive");
		this.useFile("src/ar/com/tadp/xml/rinzo/core/contentassist/processors/appCtx.xml");
		XMLContentAssistProcessor processor = new XMLContentAssistProcessor();
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		int offset = 1687;
		String prefix = "";
		XMLNode currentNode = this.xmlTreeModel.getTree().getPreviousNode(offset);
		processor.addBodyProposals(currentNode, prefix, this.editor.getSourceViewerEditor(), offset, resultList);

		assertEquals(expectedResult.size(), resultList.size());
		assertTrue(this.successResult(resultList, expectedResult));
	}

	private boolean successResult(Collection<ICompletionProposal> resultList, Collection<String> expected) {
		for (String expectedProposal : expected) {
			for (ICompletionProposal iCompletionProposal : resultList) {
				if (expectedProposal.equals(iCompletionProposal.getDisplayString())) {
					return true;
				}
			}
		}
		return false;
	}

}
