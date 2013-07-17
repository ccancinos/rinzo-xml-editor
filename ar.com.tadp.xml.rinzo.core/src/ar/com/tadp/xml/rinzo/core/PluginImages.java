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
package ar.com.tadp.xml.rinzo.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * Plugin's images provider
 * 
 * @author ccancinos
 */
public class PluginImages {

	private static final String NAME_PREFIX = "ar.com.tadp.xml.rinzo.img.";
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	private static URL iconBaseURL = null;
	private static ImageRegistry imageRegistry = null;

	// ************************************************
	// ** Nombre de Imagenes
	// ************************************************
	/** Tag con boddy */
	public static final String IMG_XML_TAGDEF = NAME_PREFIX + "tagdef.gif";
	/** Tag sin boddy */
	public static final String IMG_XML_EMPTYTAGDEF = NAME_PREFIX + "emptytagdef.gif";
	/** Texto dentro del boddy de un tag */
	public static final String IMG_XML_TXT = NAME_PREFIX + "xmltxt.gif";
	/** Tag de comentario */
	public static final String IMG_XML_COMMENT = NAME_PREFIX + "tagcomment.gif";
	/** Simbolito "?" */
	public static final String IMG_XML_PI = NAME_PREFIX + "xmldecl.gif";

	/** "a" de atributo */
	public static final String IMG_XML_ATTRIBUTE = NAME_PREFIX + "attlist.gif";
	/** Cuadernito de template */
	public static final String IMG_XML_TEMPLATE = NAME_PREFIX + "template.gif";
	/** Minus sign */
	public static final String IMG_COLLAPSEALL = NAME_PREFIX + "collapseall.gif";
	/** Edit field txt */
	public static final String IMG_SHOWFULLNAME = NAME_PREFIX + "metharg_obj.gif";
	/** Edit field icon */
	public static final String IMG_EDIT_INLINE = NAME_PREFIX + "correction_linked_rename.gif";

	public static final String IMG_CHANGE = NAME_PREFIX + "correction_change.gif";

	public static final String IMG_DELETE = NAME_PREFIX + "delete_obj.gif";
	
	public static final String IMG_CLEAR = NAME_PREFIX + "clear.gif";
	/** Run green arrow */
	public static final String IMG_XPATH_AUTO_EVALUATE = NAME_PREFIX + "auto_evaluate.gif";

	public static void init() {
		iconBaseURL = XMLEditorPlugin.getDefault().getBundle().getEntry("icons/");
		imageRegistry = new ImageRegistry();

		addImage("xpath", IMG_CLEAR);
		addImage("xpath", IMG_XPATH_AUTO_EVALUATE);

		// ************************************************
		// ** Imagenes del OutLine
		// ************************************************
		addImage("outline", IMG_XML_TAGDEF);
		addImage("outline", IMG_XML_EMPTYTAGDEF);
		addImage("outline", IMG_XML_TXT);
		addImage("outline", IMG_XML_COMMENT);
		addImage("outline", IMG_XML_PI);
		addImage("outline", IMG_COLLAPSEALL);
		addImage("outline", IMG_SHOWFULLNAME);

		// ************************************************
		// ** Imagenes del Content Assist
		// ************************************************
		addImage("contentAssist", IMG_XML_ATTRIBUTE);
		addImage("contentAssist", IMG_XML_TEMPLATE);
		addImage("contentAssist", IMG_EDIT_INLINE);
		addImage("contentAssist", IMG_CHANGE);
		addImage("contentAssist", IMG_DELETE);
	}

	/**
	 * Agrega la imagen al registro de imagenes
	 * 
	 * @param prefix nombre del directorio donde se encuentra la imagen relativo al root de las imagenes
	 *            dentro del proyecto
	 * @param name nombre de la imagen
	 */
	private static void addImage(String prefix, String name) {
		ImageDescriptor descriptor = getImageDescriptor(prefix, name);
		imageRegistry.put(name, descriptor);
	}

	/**
	 * Obtiene el descriptor de la imagen a partir de su nombre relativo al root de las imagenes
	 */
	private static ImageDescriptor getImageDescriptor(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * Obtiene un objeto URL que hace referencia a la imagen
	 */
	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (iconBaseURL == null) {
			throw new MalformedURLException();
		}
		else {
			StringBuffer buffer = new StringBuffer();
			if (prefix != null) {
				buffer.append(prefix);
				buffer.append('/');
			}
			buffer.append(name);
			return new URL(iconBaseURL, buffer.toString());
		}
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
	}
}
