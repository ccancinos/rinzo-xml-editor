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
package ar.com.tadp.xml.rinzo.core.outline.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

import ar.com.tadp.xml.rinzo.core.PluginImages;

/**
 * Action to collapse all tags in the Outline view tree.
 * 
 * @author ccancinos
 */
public class CollapseAllAction extends Action {

	private final TreeViewer treeViewer;

	public CollapseAllAction(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
		setDescription("Collapse All");
		setToolTipText("Collapse All");
		this.setImageDescriptor(ImageDescriptor.createFromImage(PluginImages.get(PluginImages.IMG_COLLAPSEALL)));
	}

	public void run() {
		this.treeViewer.collapseAll();
	}
}
