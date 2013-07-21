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

import java.util.Set;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

public class RinzoProjectionViewer extends ProjectionViewer {

    public RinzoProjectionViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
    }

    @Override
    public void doOperation(int operation) {
        if (operation == TextViewer.SHIFT_RIGHT || operation == TextViewer.SHIFT_LEFT) {
            setIndentByPreferences();
        }
        super.doOperation(operation);
    }

    private void setIndentByPreferences() {
        String[] indentPrefixesByPreferences = XMLEditorConfiguration.getIndentPrefixesByPreferences();
        Set<?> keySet = fIndentChars.keySet();
        for (Object object : keySet) {
            String key = (String) object;
            setIndentPrefixes(indentPrefixesByPreferences, key);
        }
    }
}
