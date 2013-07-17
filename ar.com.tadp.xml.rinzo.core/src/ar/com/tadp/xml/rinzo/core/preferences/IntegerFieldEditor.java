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

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author ccancinos
 */
public class IntegerFieldEditor extends StringFieldEditor {
	private int minValidValue;
	private int maxValidValue;

	protected IntegerFieldEditor() {
		minValidValue = 0;
		maxValidValue = 0x7fffffff;
	}

	public IntegerFieldEditor(String name, String labelText, Composite parent) {
		this(name, labelText, parent, 10);
	}

	public IntegerFieldEditor(String name, String labelText, Composite parent, int textLimit) {
		minValidValue = 0;
		maxValidValue = 0x7fffffff;
		init(name, labelText);
		setTextLimit(textLimit);
		setEmptyStringAllowed(false);
		setErrorMessage(JFaceResources.getString("IntegerFieldEditor.errorMessage"));
		createControl(parent);
	}

	public IntegerFieldEditor(String name, String labelText, int width, int textLimit, Composite parent) {
		super(name, labelText, width, parent);
		minValidValue = 0;
		maxValidValue = 0x7fffffff;
		setTextLimit(textLimit);
		setEmptyStringAllowed(false);
		setErrorMessage(JFaceResources.getString("IntegerFieldEditor.errorMessage"));
	}

	public void setValidRange(int min, int max) {
		minValidValue = min;
		maxValidValue = max;
	}

	protected boolean checkState() {
		Text text = getTextControl();
		if (text == null) {
			return false;
		}
		String numberString = text.getText();
		try {
			int number = Integer.valueOf(numberString).intValue();
			if (number >= minValidValue && number <= maxValidValue) {
				clearErrorMessage();
				return true;
			} else {
				showErrorMessage();
				return false;
			}
		} catch (NumberFormatException _ex) {
			showErrorMessage();
		}
		return false;
	}

	protected void doLoad() {
		Text text = getTextControl();
		if (text != null) {
			int value = getPreferenceStore().getInt(getPreferenceName());
			text.setText(Integer.toString(value));
		}
	}

	protected void doLoadDefault() {
		Text text = getTextControl();
		if (text != null) {
			int value = getPreferenceStore().getDefaultInt(getPreferenceName());
			text.setText(Integer.toString(value));
		}
		valueChanged();
	}

	protected void doStore() {
		Text text = getTextControl();
		if (text != null) {
			getPreferenceStore().setValue(getPreferenceName(), new Integer(text.getText()).intValue());
		}
	}

	public int getIntValue() throws NumberFormatException {
		return Integer.valueOf(getStringValue()).intValue();
	}
}
