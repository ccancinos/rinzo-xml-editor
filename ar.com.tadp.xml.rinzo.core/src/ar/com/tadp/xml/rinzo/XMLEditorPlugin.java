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

import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;

import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.highlighting.IXMLColorConstants;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Plugin definition entry point
 * 
 * @author ccancinos
 */
public class XMLEditorPlugin extends AbstractUIPlugin {
    public static final String PREF_VALIDATION_SEVERITY = "__pref_compilation_severity";
    public static final String PREF_MAX_LINE_WIDTH = "__pref_max_line_width";
    public static final String FORMAT_ON_SAVE = "__pref_format_on_save";
	private static XMLEditorPlugin plugin;
    private ResourceBundle resourceBundle;
    private Map<Collection<DocumentStructureDeclaration>, Validator> schemaValidatorsCache = new WeakHashMap<Collection<DocumentStructureDeclaration>, Validator>();

    public XMLEditorPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("ar.com.tadp.xml.rinzo.core.EditorMessages");
		} catch (MissingResourceException _ex) {
			resourceBundle = null;
		}
    }

    public void start(BundleContext context) throws Exception {
		super.start(context);
		this.initializeDefaultPreferences(this.getPreferenceStore());
		DocumentCache.getInstance().setCacheLocation(
				"file:" + XMLEditorPlugin.getDefault().getStateLocation().toString() + "/.cache");
		PluginImages.init();
    }

    public static XMLEditorPlugin getDefault() {
        return plugin;
    }

    public static String getResourceString(String key) {
		ResourceBundle bundle = getDefault().getResourceBundle();
		try {
			return bundle == null ? key : bundle.getString(key);
		} catch (MissingResourceException _ex) {
			return key;
		}
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void logErrorMessage(String message) {
    	logErrorMessage(message, null);
    }

    public static void logErrorMessage(String message, Throwable exception) {
    	log(new Status(4, getPluginId(), 10001, message, exception));
    }

    public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
		} else {
			MultiStatus multi = new MultiStatus(getPluginId(), 10001, message, null);
			multi.add(status);
			log(multi);
		}
    }

    public static void log(Throwable e) {
		log(new Status(4, getPluginId(), 10001, "Internal error ", e));
    }

    public static String getPluginId() {
        return getDefault().getBundle().getSymbolicName();
    }
    
    public Map<Collection<DocumentStructureDeclaration>, Validator> getSchemaValidatorsCache() {
		return schemaValidatorsCache;
	}

	public RinzoXMLEditor getActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor instanceof RinzoXMLEditor)
					return (RinzoXMLEditor) editor;
			}
		}
		return null;
	}

    protected void initializeDefaultPreferences(IPreferenceStore store) {
		// Defaults para los colores de la sintaxis del lenguaje
		store.setDefault(IXMLColorConstants.XML_COMMENT, "63,95,191");
		store.setDefault(IXMLColorConstants.PROC_INSTR, "128,128,128");
		store.setDefault(IXMLColorConstants.DECLARATION, "128,128,128");
		store.setDefault(IXMLColorConstants.STRING, "42,0,255");
		store.setDefault(IXMLColorConstants.ATTRIBUTE, "127,0,127");
		store.setDefault(IXMLColorConstants.DEFAULT, "0,0,0");
		store.setDefault(IXMLColorConstants.TAG, "63,127,127");
		store.setDefault(IXMLColorConstants.CDATA, "128,128,128");
		store.setDefault(IXMLColorConstants.CDATA + "#bold", true);

		store.setDefault(IXMLColorConstants.EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter
				.setDefault(store, IXMLColorConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192));

		// avoids the plugin to load the color_background_system_default color
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);

		store.setDefault(XMLEditorPlugin.PREF_VALIDATION_SEVERITY, "Error");
		store.setDefault(PREF_MAX_LINE_WIDTH, 80);
    }    

    /**
     * Returns the indentation token as defined in the preferences pages.
     * It take into account the width of the token and if it should be spaces or tab
     */
	public String getIndentToken() {
		String isSpaces = this.getPreferenceStore().getString(
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
		int width = Integer.parseInt(this.getPreferenceStore().getString(
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		StringBuilder stringBuilder = new StringBuilder();
		if (Boolean.parseBoolean(isSpaces)) {
			for (int i = 0; i < width; i++) {
				stringBuilder.append(" ");
			}
		} else {
			stringBuilder.append(FileUtils.TAB);
		}
		return stringBuilder.toString();
	}

	public static String getCompilationSeverity() {
		return XMLEditorPlugin.getDefault().getPreferenceStore().getString(PREF_VALIDATION_SEVERITY);
	}

	public static void setCompilationSeverity(String severity) {
		XMLEditorPlugin.getDefault().getPreferenceStore().setValue(PREF_VALIDATION_SEVERITY, severity);
	}
	
	public static int getMaximumLineWidth() {
		return XMLEditorPlugin.getDefault().getPreferenceStore().getInt(PREF_MAX_LINE_WIDTH);
	}
	
	public static void setMaximumLineWidth(int lineWidth) {
		XMLEditorPlugin.getDefault().getPreferenceStore().setValue(PREF_MAX_LINE_WIDTH, lineWidth);
	}
	
	public static boolean isFormatOnSave() {
		return XMLEditorPlugin.getDefault().getPreferenceStore().getBoolean(FORMAT_ON_SAVE);
	}

}
