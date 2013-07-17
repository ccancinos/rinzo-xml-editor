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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.outline.XMLOutlineLabelProvider;
import ar.com.tadp.xml.rinzo.core.outline.XMLOutlinePage;

/**
 * Action to toggle the label being displayed for each tag in the outline view.
 * 
 * @author ccancinos
 */
public class ShowFullNameAction extends Action {

	private final XMLOutlineLabelProvider labelProvider;
	private final XMLOutlinePage outlinePage;

	public ShowFullNameAction(XMLOutlinePage outlinePage, XMLOutlineLabelProvider labelProvider) {
		this.outlinePage = outlinePage;
		this.labelProvider = labelProvider;
		setDescription("Show Full Name");
		setToolTipText("Show Full Tag");
		this.setImageDescriptor(ImageDescriptor.createFromImage(PluginImages.get(PluginImages.IMG_SHOWFULLNAME)));
		this.setChecked(labelProvider.isShowFullName());
	}

	public void run() {
		this.labelProvider.setShowFullName(!this.labelProvider.isShowFullName());
		this.outlinePage.internalUpdate();
	}

	public int getStyle() {
		return IAction.AS_CHECK_BOX;
	}
}
