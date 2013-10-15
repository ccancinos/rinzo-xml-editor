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

import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.ITextViewer;

import ar.com.tadp.xml.rinzo.core.contentassist.processors.IXMLContentAssistProcessor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;

/**
 * Generates content assist class names proposals base on different strategies
 * according to preferences configuration.
 * 
 * @author ccancinos
 */
@SuppressWarnings("rawtypes")
public class ClassNameContentAssistProcessor implements IXMLContentAssistProcessor {
    private IJavaProject project;
    
    private ClassNameProcessor classNameProcessor = new ClassNameProcessor();
    private FilteredClassNameProcessor filteredClassNameProcessor = new FilteredClassNameProcessor();
    
    public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix, ITextViewer viewer, int offset, Collection resultList) {
    	if (JDTUtils.isJavaProject(this.getProject())) {
			if (!RinzoJDTPlugin.isEnableClassName()) {
				classNameProcessor.addAttributeValuesProposals(currentNode, attributeName, prefix, viewer, offset, resultList);
			} else {
				filteredClassNameProcessor.addAttributeValuesProposals(currentNode, attributeName, prefix, viewer, offset, resultList);
			}
		}
    }
    
    public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection resultList) {
    	if (JDTUtils.isJavaProject(this.getProject())) {
			if (!RinzoJDTPlugin.isEnableClassName()) {
				classNameProcessor.addBodyProposals(currentNode, prefix, viewer, offset, resultList);
			} else {
				filteredClassNameProcessor.addBodyProposals(currentNode, prefix, viewer, offset, resultList);
			}
		}
    }

	public void addCloseTagProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection resultList) {
	}

	public void addAttributeProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection resultList) {
	}
    
    private IJavaProject getProject() {
    	if(this.project == null) {
			this.project = JDTUtils.getActiveJavaProject();
    	}
		return this.project;
	}

}
