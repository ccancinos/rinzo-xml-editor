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

import ar.com.tadp.xml.rinzo.core.utils.FileUtils;
import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;

/**
 * Model element to configure which Tags will contain class names also defining
 * which class will extend all contents
 * 
 * @author ccancinos
 */
public class ClassElement implements Extending {
	private String displayName;
	private String extending;
	
	public ClassElement(String displayName, String assistString){
		this.displayName = displayName;
		this.extending = assistString;
	}
	
	public String getExtending() {
		return extending;
	}
	public void setExtending(String extending) {
		this.extending = extending;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public static List<ClassElement> loadFromPreference(boolean defaults){
		IPreferenceStore store = RinzoJDTPlugin.getDefault().getPreferenceStore();
		String value = null;
		if(defaults){
			value = store.getDefaultString(RinzoJDTPlugin.PREF_CLASSNAME_ELEMENTS);
		} else {
			value = store.getString(RinzoJDTPlugin.PREF_CLASSNAME_ELEMENTS);
		}
		List<ClassElement> list = new ArrayList<ClassElement>();
		if(value!=null){
			String[] values = value.split("\n");
			for(int i=0;i<values.length;i++){
				String[] split = values[i].split(FileUtils.TAB);
				if(split.length==2){
					list.add(new ClassElement(split[0], split[1]));
				}
			}
		}
		return list;
	}
	
	public static void saveToPreference(List<ClassElement> list){
		IPreferenceStore store = RinzoJDTPlugin.getDefault().getPreferenceStore();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<list.size();i++){
			ClassElement element = list.get(i);
			sb.append(element.getDisplayName());
			sb.append(FileUtils.TAB);
			sb.append(element.getExtending());
			sb.append("\n");
		}
		store.setValue(RinzoJDTPlugin.PREF_CLASSNAME_ELEMENTS, sb.toString());
	}
}
