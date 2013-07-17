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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.highlighting.IXMLColorConstants;

/**
 * Preference page for the selection of colors for each element of the XML syntax.
 * 
 * @author ccancinos
 */
public class SyntaxColorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    private Map<String, String> colorPreferences = new TreeMap<String, String>();

    public SyntaxColorPreferencePage() {
        super(1);
        setPreferenceStore(XMLEditorPlugin.getDefault().getPreferenceStore());
        setDescription("XML Editor Syntax Color settings:");

        this.colorPreferences.put("Attribute Name", IXMLColorConstants.ATTRIBUTE);
        this.colorPreferences.put("Attribute Value", IXMLColorConstants.STRING);
        this.colorPreferences.put("Comment", IXMLColorConstants.XML_COMMENT);
        this.colorPreferences.put("Declaration", IXMLColorConstants.DECLARATION);
        this.colorPreferences.put("Element (start and end tag)", IXMLColorConstants.TAG);
        this.colorPreferences.put("Processing Instruction", IXMLColorConstants.PROC_INSTR);
        this.colorPreferences.put("CDATA", IXMLColorConstants.CDATA);
        this.colorPreferences.put("Text", IXMLColorConstants.DEFAULT);
    }
    
    protected void createFieldEditors() {
        addField(new ColorPreferenceEditor("appearanceColors", "Appearance color options:", getFieldEditorParent(), this.colorPreferences));
    }

    public void init(IWorkbench workbench) {
    }

}
