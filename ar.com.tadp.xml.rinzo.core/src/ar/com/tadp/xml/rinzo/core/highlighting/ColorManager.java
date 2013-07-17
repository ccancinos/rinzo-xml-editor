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
package ar.com.tadp.xml.rinzo.core.highlighting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * Manages colors used in the editor. This behavior is centralized since Color's
 * are a resources that should be disposed after used because SWT linked them
 * with OS resources.
 * 
 * @author ccancinos
 */
public class ColorManager {
	protected Map<Serializable, Color> colorTable;
	protected Map<String, Boolean> boldTable;

	public ColorManager() {
		colorTable = new HashMap<Serializable, Color>(10);
		boldTable = new HashMap<String, Boolean>(10);
	}

	public void dispose() {
		for (Color color : this.colorTable.values()) {
			color.dispose();
		}
	}

	public Color getColor(RGB rgb) {
		Color color = colorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colorTable.put(rgb, color);
		}
		return color;
	}

	public Color getColor(String colorName) {
		Color color = colorTable.get(colorName);
		if (color == null) {
			color = new Color(Display.getCurrent(), this.createRGB(colorName));
			colorTable.put(colorName, color);
		}
		return color;
	}

	private RGB createRGB(String colorName) {
		return PreferenceConverter.getColor(XMLEditorPlugin.getDefault().getPreferenceStore(), colorName);
	}

	public int isBold(String tag) {
		Boolean bold = boldTable.get(tag);
		if (bold == null) {
			bold = Boolean.valueOf(XMLEditorPlugin.getDefault().getPreferenceStore().getBoolean(tag + "#bold"));
			boldTable.put(tag, bold);
		}
		return bold.booleanValue() ? 1 : 0;
	}

}
