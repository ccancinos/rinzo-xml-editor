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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.model.XMLAttribute;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.partitioner.IXMLPartitions;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * In charge of retrieving the labels to be displayed on each element of the
 * outline tree.
 * 
 * @author ccancinos
 */
public class XMLOutlineLabelProvider extends LabelProvider {
	private static final String OUTLINE_LABEL_SHOW_FULL_NAME = "Outline.Label.showFullName";
	private Map<String, Image> images = new HashMap<String, Image>();
	private boolean showFullName = false;

	public XMLOutlineLabelProvider() {
		this.showFullName = XMLEditorPlugin.getDefault().getPreferenceStore().getBoolean(OUTLINE_LABEL_SHOW_FULL_NAME);

		this.images.put(IXMLPartitions.XML_TAG, PluginImages.get(PluginImages.IMG_XML_TAGDEF));
		this.images.put(IXMLPartitions.XML_EMPTYTAG, PluginImages.get(PluginImages.IMG_XML_EMPTYTAGDEF));
		this.images.put(IXMLPartitions.XML_TEXT, PluginImages.get(PluginImages.IMG_XML_TXT));
		this.images.put(IXMLPartitions.XML_PI, PluginImages.get(PluginImages.IMG_XML_PI));
		this.images.put(IXMLPartitions.XML_DECLARATION, PluginImages.get(PluginImages.IMG_XML_PI));
		this.images.put(IXMLPartitions.XML_COMMENT, PluginImages.get(PluginImages.IMG_XML_COMMENT));
	}

	public String getText(Object element) {
		XMLNode node = (XMLNode) element;
		if (element instanceof XMLNode) {
			if (this.isShowFullName()) {
				String text = node.getTagName() + " (";
				for (Iterator<XMLAttribute> iterator = node.getAttributes().values().iterator(); iterator.hasNext();) {
					XMLAttribute attribute = iterator.next();
					text += iterator.hasNext() ? attribute + ", " : attribute.toString();
				}
				text += ")";
				return text.replaceAll(FileUtils.LINE_SEPARATOR, " ").replaceAll(FileUtils.TAB, "");
			}
			if (node.isPiTag()) {
				String comment = node.getContent();
				return comment.substring(0, Math.min(20, comment.length())) + "...";
			}
			if (node.isCommentTag()) {
				String content = node.getContent();
				String comment = content.substring(content.indexOf("<!--") + 4, content.indexOf("-->")).trim();
				return (comment.length() <= 20) ? comment : comment.substring(0, 20) + "...";
			}
			return node.getTagName().replaceAll(FileUtils.LINE_SEPARATOR, " ").replaceAll(FileUtils.TAB, "");
		}

		return super.getText(element);
	}

	public Image getImage(Object element) {
		if (element instanceof XMLNode) {
			XMLNode node = (XMLNode) element;
			Image image = this.images.get(node.getType());
			if (image != null) {
				return image;
			}
		}

		return super.getImage(element);
	}

	public boolean isShowFullName() {
		return this.showFullName;
	}

	public void setShowFullName(boolean showFullName) {
		XMLEditorPlugin.getDefault().getPreferenceStore().setValue(OUTLINE_LABEL_SHOW_FULL_NAME, showFullName);
		this.showFullName = showFullName;
	}

}
