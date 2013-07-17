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
package ar.com.tadp.xml.rinzo.core.template;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Looks up existing ant variables and proposes them. The proposals are sorted by 
 * their prefix-likeness with the variable type.
 * 
 * @author ccancinos
 */
public class AntVariableResolver extends TemplateVariableResolver {
	/*
	 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolveAll(org.eclipse.jface.text.templates.TemplateContext)
	 */
	protected String[] resolveAll(TemplateContext context) {
		String[] proposals= new String[] { "srcDirA", "dstDirB", "dstDirB", "dstDirB" }; //$NON-NLS-1$ //$NON-NLS-2$
		
//		Arrays.sort(proposals, new Comparator() {
//
//			public int compare(Object o1, Object o2) {
//				return getCommonPrefixLength(getType(), (String) o2) - getCommonPrefixLength(getType(), (String) o1);
//			}
//
//			private int getCommonPrefixLength(String type, String var) {
//				int i= 0;
//				CharSequence vSeq= var.subSequence(2, var.length() - 1); // strip away ${}
//				while (i < type.length() && i < vSeq.length())
//					if (Character.toLowerCase(type.charAt(i)) == Character.toLowerCase(vSeq.charAt(i)))
//						i++;
//					else
//						break;
//				return i;
//			}
//		});
		
		return proposals;
	}
}
