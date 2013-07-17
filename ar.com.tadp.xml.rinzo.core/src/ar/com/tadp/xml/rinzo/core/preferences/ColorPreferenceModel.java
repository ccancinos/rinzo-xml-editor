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
package ar.com.tadp.xml.rinzo.core.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * 
 * @author ccancinos
 */
public class ColorPreferenceModel {
	private final String preferenceKey;
	private final String displayText;
	private RGB value;
	private boolean bold;

	public ColorPreferenceModel(String displayText, String preferenceKey) {
		this.displayText = displayText;
		this.preferenceKey = preferenceKey;
		this.bold = false;
	}

	public String getDisplayText() {
		return displayText;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

	public RGB getColorValue() {
		return value;
	}

	public void setColorValue(RGB value) {
		this.value = value;
	}

	public boolean isBold() {
		return this.bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}
}
