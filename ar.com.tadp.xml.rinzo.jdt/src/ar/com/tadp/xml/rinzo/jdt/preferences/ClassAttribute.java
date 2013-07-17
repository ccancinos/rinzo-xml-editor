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
package ar.com.tadp.xml.rinzo.jdt.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;

/**
 * Model element to configure which Attributes will contain class names also
 * defining which class will extend all contents
 * 
 * @author ccancinos
 */
public class ClassAttribute implements Extending {
	private String targetTag;
	private String attributeName;
	private String extending;
	
	public ClassAttribute(String targetTag, String attributeName, String extendingElement){
		this.targetTag = targetTag;
		this.attributeName = attributeName;
		extending = extendingElement;
	}
	
	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getTargetTag() {
		return targetTag;
	}

	public void setTargetTag(String targetTag) {
		this.targetTag = targetTag;
	}
	
	public String getExtending() {
		return extending;
	}
	
	public void setExtending(String extending) {
		this.extending = extending;
	}
	
	public static List<ClassAttribute> loadFromPreference(boolean defaults){
		IPreferenceStore store = RinzoJDTPlugin.getDefault().getPreferenceStore();
		String value = null;
		if(defaults){
			value = store.getDefaultString(RinzoJDTPlugin.PREF_CLASSNAME_ATTRS);
		} else {
			value = store.getString(RinzoJDTPlugin.PREF_CLASSNAME_ATTRS);
		}
		List<ClassAttribute> list = new ArrayList<ClassAttribute>();
		if(value!=null){
			String[] values = value.split("\n");
			for(int i=0;i<values.length;i++){
				String[] split = values[i].split("\t");
				if(split.length==3){
					list.add(new ClassAttribute(split[0], split[1], split[2]));
				}
			}
		}
		return list;
	}
	
	public static void saveToPreference(List<ClassAttribute> list){
		IPreferenceStore store = RinzoJDTPlugin.getDefault().getPreferenceStore();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<list.size();i++){
			ClassAttribute attrInfo = list.get(i);
			sb.append(attrInfo.getTargetTag());
			sb.append("\t");
			sb.append(attrInfo.getAttributeName());
			sb.append("\t");
			sb.append(attrInfo.getExtending());
			sb.append("\n");
		}
		store.setValue(RinzoJDTPlugin.PREF_CLASSNAME_ATTRS, sb.toString());
	}

	public boolean equals(String tagName, String attrName) {
		return this.equalsTagName(tagName) && this.equalsAttributeName(attrName);
	}

	private boolean equalsAttributeName(String attrName) {
		return this.attributeName.equals(attrName) || this.attributeName.equals("*");
	}

	private boolean equalsTagName(String tagName) {
		return this.targetTag.equals(tagName) || this.targetTag.equals("*");
	}
}
