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
package ar.com.tadp.xml.rinzo.jdt.contentassist.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.ITextViewer;

import ar.com.tadp.xml.rinzo.core.contentassist.processors.IXMLContentAssistProcessor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.Utils;

/**
 * Generates content assist class names proposals for all Tags and Attributes in
 * a document.
 * 
 * @author ccancinos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
public class ClassNameProcessor implements IXMLContentAssistProcessor {
	private static final String JNI_METHOD_NAME = "registerNatives()";
	private static final String TEMPORAL_METHOD_NAME = "hoge()";
	private static final String TEMPORAL_CLASS_NAME = "_xxx";
	private static final String TEMPORAL_CLASS_START = "public class _xxx { public static void hoge(){ ";
	private static final String TEMPORAL_CLASS_FILE_NAME = TEMPORAL_CLASS_NAME + ".java";
	private IJavaProject project;

	public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection resultList) {
		resultList.addAll(this.getProposals(prefix, offset));
	}

	public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix,
			ITextViewer viewer, int offset, Collection resultList) {
		resultList.addAll(this.getProposals(prefix, offset));
	}

	public void addCloseTagProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection resultList) {
	}

	public void addAttributeProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection resultList) {
	}

	private String getClassNamePrefix(String prefix, int offset) {
		int i = prefix.length() - 1;
		while (i > 0) {
			char ch = prefix.charAt(i);
			if (ch != '<' && ch != '*' && !Character.isJavaIdentifierPart(ch) && ch != '.') {
				break;
			}
			i--;
		}
		return (i == 0) ? prefix.substring(i) : prefix.substring(i + 1);
	}

	protected List getProposals(String initialPrefix, int offset) {
		List result = new ArrayList();
		String prefix = this.getClassNamePrefix(initialPrefix, offset);
		try {
			IJavaCompletionProposal[] proposals = this.createJavaClassesProposals(prefix);

			for (int j = 0; j < proposals.length; j++) {
				AbstractJavaCompletionProposal proposal = (AbstractJavaCompletionProposal) proposals[j];

				if (!proposal.getReplacementString().equals(TEMPORAL_METHOD_NAME)
						&& !proposal.getReplacementString().equals(TEMPORAL_CLASS_NAME)
						&& !proposal.getReplacementString().equals(JNI_METHOD_NAME)
					&& !proposal.getReplacementString().equals(JNI_METHOD_NAME + ";")) {

					proposal.setReplacementOffset(offset - prefix.length());
					proposal.setReplacementLength(prefix.length());

					if (addProposal(proposal,result)) {
						if (proposals[j] instanceof LazyJavaTypeCompletionProposal) {
							LazyJavaTypeCompletionProposal javaTypeProposal = (LazyJavaTypeCompletionProposal) proposals[j];
							javaTypeProposal.setReplacementString(javaTypeProposal.getQualifiedTypeName());
						} 
						if (proposals[j] instanceof JavaCompletionProposal || proposals[j] instanceof JavaMethodCompletionProposal) {
							String displayString = proposal.getDisplayString();
							int indexOfColon = displayString.indexOf(':');
							if(indexOfColon != -1) {
								String replacementString = displayString.substring(0, indexOfColon).trim();
								proposal.setReplacementString(replacementString);
								proposal.setReplacementOffset(offset);
							}
						}
					}
				}
			}

			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected boolean addProposal(AbstractJavaCompletionProposal proposal, List result) {
		result.add(proposal);
		return true;
	}

	private IJavaCompletionProposal[] createJavaClassesProposals(String prefix) throws JavaModelException {
		if (prefix != null && !Utils.isEmpty(prefix.trim())) {
			CompletionProposalCollector collector = new CompletionProposalCollector(this.getProject());
			ICompilationUnit unit = getTemporaryCompilationUnit(this.getProject());
			String source = TEMPORAL_CLASS_START + prefix + "}}";
			setContentsToCU(unit, source);
			unit.codeComplete(source.length() - 2, collector, DefaultWorkingCopyOwner.PRIMARY);
			IJavaCompletionProposal[] proposals = collector.getJavaCompletionProposals();
			return proposals;
		} else {
			return new IJavaCompletionProposal[] {};
		}
	}

	private void setContentsToCU(ICompilationUnit unit, String value) {
		if (unit == null) {
			return;
		}

		synchronized (unit) {
			IBuffer buffer;
			try {
				buffer = unit.getBuffer();
				if (buffer != null) {
					buffer.setContents(value);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}

	private ICompilationUnit getTemporaryCompilationUnit(IJavaProject project) throws JavaModelException {
		IPackageFragment root = project.getPackageFragments()[0];
		ICompilationUnit unit = root.getCompilationUnit(TEMPORAL_CLASS_FILE_NAME).getWorkingCopy(
				new NullProgressMonitor());

		return unit;
	}

	private IJavaProject getProject() {
		if (this.project == null) {
			this.project = JDTUtils.getActiveJavaProject();
		}
		return this.project;
	}

}
