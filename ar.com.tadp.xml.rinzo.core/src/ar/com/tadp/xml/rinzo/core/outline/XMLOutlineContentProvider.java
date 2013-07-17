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
package ar.com.tadp.xml.rinzo.core.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * 
 * @author ccancinos
 */
public class XMLOutlineContentProvider implements ITreeContentProvider {
	private XMLNode rootNode;

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof XMLNode) {
			XMLNode node = (XMLNode) parentElement;
			return node.getChildren(false);
		}

		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof XMLNode) {
			XMLNode node = (XMLNode) element;
			return node.getParent();
		}

		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof XMLNode) {
			XMLNode node = (XMLNode) element;
			return node.hasChildren();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return rootNode != null ? rootNode.getChildren(false) : null;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		rootNode = (XMLNode) newInput;
	}

}
