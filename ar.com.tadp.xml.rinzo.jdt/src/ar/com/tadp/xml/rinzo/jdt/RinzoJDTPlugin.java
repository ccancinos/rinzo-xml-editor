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
package ar.com.tadp.xml.rinzo.jdt;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RinzoJDTPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "ar.com.tadp.xml.rinzo.jdt";

	public static final String PREF_ENABLE_CLASSNAME  = "__pref_enable_classname";
	public static final String PREF_CLASSNAME_ATTRS   = "__pref_classname_attrs";
	public static final String PREF_CLASSNAME_ELEMENTS   = "__pref_classname_elements";
	public static final String PREF_COMPILATION_SEVERITY  = "__pref_compilation_severity";

	private ResourceBundle resourceBundle;
	private static RinzoJDTPlugin plugin;
	
	public RinzoJDTPlugin() {
		try {
			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".PluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static RinzoJDTPlugin getDefault() {
		return plugin;
	}
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		store.setDefault(RinzoJDTPlugin.PREF_ENABLE_CLASSNAME, false);
		store.setDefault(RinzoJDTPlugin.PREF_CLASSNAME_ATTRS, "*\tclass\t*\n*\ttype\t*\n");
		store.setDefault(RinzoJDTPlugin.PREF_CLASSNAME_ELEMENTS, "servlet-class\tjavax.servlet.http.HttpServlet\nfilter-class\tjavax.servlet.Filter\nvalue\t*\nprop\t*\n");
		store.setDefault(RinzoJDTPlugin.PREF_COMPILATION_SEVERITY, "Warning");
	}
	
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = RinzoJDTPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Generates a message from a template and parameters.
	 * Replace template {0}{1}.. with parametersB
	 *
	 * @param message message
	 * @param params  parameterd
	 * @return generated message
	 */
	public static String createMessage(String message,String[] params){
		for(int i=0;i<params.length;i++){
			message = message.replaceAll("\\{"+i+"\\}",params[i]);
		}
		return message;
	}

	/**
	 * Open the alert dialog.
	 * @param message message
	 */
	public static void openAlertDialog(String message){
		MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(),SWT.NULL|SWT.ICON_ERROR);
		box.setMessage(message);
		box.setText(getResourceString("ErrorDialog.Caption"));
		box.open();
	}

	public static boolean isEnableClassName() {
		return RinzoJDTPlugin.getDefault().getPreferenceStore().getBoolean(RinzoJDTPlugin.PREF_ENABLE_CLASSNAME);
	}
	
	public static void setEnableClassName(boolean enable) {
		RinzoJDTPlugin.getDefault().getPreferenceStore().setValue(RinzoJDTPlugin.PREF_ENABLE_CLASSNAME, enable);
	}

	public static String getCompilationSeverity() {
		return RinzoJDTPlugin.getDefault().getPreferenceStore().getString(RinzoJDTPlugin.PREF_COMPILATION_SEVERITY);
	}

	public static void setCompilationSeverity(String severity) {
		RinzoJDTPlugin.getDefault().getPreferenceStore().setValue(RinzoJDTPlugin.PREF_COMPILATION_SEVERITY, severity);
	}
}
