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

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author ccancinos
 */
public class ColorChooser extends Composite {

    private ColorSelector changeControl;
    private Label label;
    private Button bold;

    public ColorChooser(Composite parent, int style) {
        super(parent, style);
        initGUI();
    }

    public void initGUI() {
        try {
            label = new Label(this, 0);
            changeControl = new ColorSelector(this);
            setSize(new Point(87, 25));
            GridData label1LData = new GridData();
            label1LData.verticalAlignment = 2;
            label1LData.horizontalAlignment = 2;
            label1LData.widthHint = -1;
            label1LData.heightHint = -1;
            label1LData.horizontalIndent = 0;
            label1LData.horizontalSpan = 1;
            label1LData.verticalSpan = 1;
            label1LData.grabExcessHorizontalSpace = false;
            label1LData.grabExcessVerticalSpace = false;
            label.setLayoutData(label1LData);
            label.setText("Color: ");
            
            GridData griddata = new GridData(30, 20);
            griddata.horizontalAlignment = 1;
            label = new Label(this, 0);
            label.setText("Bold:");
            label.setLayoutData(griddata);
            bold = new Button(this, SWT.CHECK);
            bold.setLayoutData(griddata);

            GridData changeControlLData = new GridData();
            changeControlLData.verticalAlignment = 2;
            changeControlLData.horizontalAlignment = 1;
            changeControlLData.widthHint = -1;
            changeControlLData.heightHint = -1;
            changeControlLData.horizontalIndent = 0;
            changeControlLData.horizontalSpan = 1;
            changeControlLData.verticalSpan = 1;
            changeControlLData.grabExcessHorizontalSpace = false;
            changeControlLData.grabExcessVerticalSpace = false;
            changeControl.getButton().setLayoutData(changeControlLData);
            GridLayout thisLayout = new GridLayout(2, true);
            thisLayout.marginWidth = 1;
            thisLayout.marginHeight = 1;
            thisLayout.numColumns = 2;
            thisLayout.makeColumnsEqualWidth = false;
            thisLayout.horizontalSpacing = 5;
            thisLayout.verticalSpacing = 5;
            setLayout(thisLayout);
            layout();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isBold() {
        return this.bold.getSelection();
    }
    
    public void setBold(boolean bold) {
        this.bold.setSelection(bold);
    }
    
    public RGB getColorValue() {
        return changeControl.getColorValue();
    }

    public void setColorValue(RGB rgb) {
        changeControl.setColorValue(rgb);
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        changeControl.addListener(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        changeControl.removeListener(listener);
    }
    
    public void addSelectionListener(SelectionListener listener) {
        this.bold.addSelectionListener(listener);
    }
    
    public void removeSelectionListener(SelectionListener listener) {
    	this.bold.removeSelectionListener(listener);
    }
}
