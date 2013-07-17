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
package ar.com.tadp.xml.rinzo;


import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

import ar.com.tadp.xml.rinzo.core.template.XMLContextType;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author ccancinos
 */
public class XmlEditorUI  {
	/** Key to store custom templates. */
	private static final String CUSTOM_TEMPLATES_KEY= "org.eclipse.ui.examples.templateeditor.customtemplates";
	
	/** The shared instance. */
	private static XmlEditorUI instance;
	
	/** The template store. */
	private TemplateStore store;
	/** The context type registry. */
	private ContributionContextTypeRegistry registry;
	
	private XmlEditorUI() {
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static XmlEditorUI getDefault() {
		if (instance == null) {
			instance= new XmlEditorUI();
		}
		return instance;
	}

	/**
	 * Returns this plug-in's template store.
	 * 
	 * @return the template store of this plug-in instance
	 */
	public TemplateStore getTemplateStore() {
		if (store == null) {
			store = new ContributionTemplateStore(getContextTypeRegistry(), 
					XMLEditorPlugin.getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
			try {
				store.load();
			} catch (IOException e) {
				XMLEditorPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, "org.eclipse.ui.examples.javaeditor", IStatus.OK, "", e));
			}
		}
		return store;
	}
    
    public String getPluginId() {
        return XMLEditorPlugin.getPluginId();
    }

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		if (registry == null) {
			// create an configure the contexts available in the template editor
			registry = new ContributionContextTypeRegistry();
			registry.addContextType(XMLContextType.XML_CONTEXT_TYPE);
		}
		return registry;
	}

	public IPreferenceStore getPreferenceStore() {
		return XMLEditorPlugin.getDefault().getPreferenceStore();
	}

	public void savePluginPreferences() {
        XMLEditorPlugin.getDefault().savePluginPreferences();
	}

}
