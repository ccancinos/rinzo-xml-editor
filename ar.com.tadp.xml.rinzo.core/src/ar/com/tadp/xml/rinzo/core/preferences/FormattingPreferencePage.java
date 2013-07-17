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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * Preference Page definition for Rinzo
 * 
 * @author ccancinos
 */
public class FormattingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Map<String, String> colorPreferences = new TreeMap<String, String>();

	public FormattingPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(XMLEditorPlugin.getDefault().getPreferenceStore());
		setDescription("XML Editor settings:");

		initializeDefaults();

		this.colorPreferences.put("Line number foreground", "lineNumberColor");
		this.colorPreferences.put("Current line highlight", "currentLineColor");
		this.colorPreferences.put("Print margin", "printMarginColor");
		this.colorPreferences.put("Selection foreground color", "AbstractTextEditor.Color.SelectionForeground");
		this.colorPreferences.put("Selection background color", "AbstractTextEditor.Color.SelectionBackground");
		this.colorPreferences.put("Background color", "AbstractTextEditor.Color.Background");
	}

	private void initializeDefaults() {
		org.eclipse.jface.preference.IPreferenceStore store = getPreferenceStore();
		AbstractDecoratedTextEditorPreferenceConstants.initializeDefaultValues(store);
		// this line avoids the settings of the default
		// color_background_system_default and refreshes the editor with the
		// selected color
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
	}

	public void createFieldEditors() {
		String[][] labelAndValues = new String[][] { { "Indent using tabs", "false" },
				{ "Indent using spaces", "true" } };
		addField(new RadioGroupFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
				"Indentation", 1, labelAndValues, getFieldEditorParent()));
		addField(new IntegerFieldEditor(XMLEditorPlugin.PREF_MAX_LINE_WIDTH,
				"Maximum line width:", 3, 3, getFieldEditorParent()));
		addField(new IntegerFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH,
				"Indentation size:", 3, 3, getFieldEditorParent()));

		addField(new IntegerFieldEditor("printMarginColumn", "Print margin column", 3, 3, getFieldEditorParent()));
		addField(new BooleanFieldEditor("overviewRuler", "Show overview ruler", getFieldEditorParent()));
		addField(new BooleanFieldEditor("lineNumberRuler", "Show line numbers", getFieldEditorParent()));
		addField(new BooleanFieldEditor("currentLine", "Highlight current line", getFieldEditorParent()));
		addField(new BooleanFieldEditor("printMargin", "Show print margin", getFieldEditorParent()));
		addField(new ColorPreferenceEditor("appearanceColors", "Appearance color options:", getFieldEditorParent(),
				this.colorPreferences));
	}

	public void init(IWorkbench iworkbench) {
	}
	
}
