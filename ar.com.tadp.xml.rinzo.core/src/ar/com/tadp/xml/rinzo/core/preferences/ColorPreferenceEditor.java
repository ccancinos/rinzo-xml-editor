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

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;

/**
 * 
 * @author ccancinos
 */
public class ColorPreferenceEditor extends FieldEditor {
	private List list;
	private ColorChooser colorChooser;
	private ArrayList<ColorPreferenceModel> colorModelList;

	protected ColorPreferenceEditor() {
	}

	public ColorPreferenceEditor(String name, String labelText, Composite parent, Map<String, String> colorPreferencesMap) {
		init(name, labelText);
		setupColorPreferences(colorPreferencesMap);
		createControl(parent);
	}

	private void setupColorPreferences(Map<String, String> colorPreferencesMap) {
		for (Entry<String, String> colorPreference : colorPreferencesMap.entrySet()) {
			setupColorPreference((String) colorPreference.getKey(), (String) colorPreference.getValue());
		}
	}

	protected void setupColorPreference(String displayText, String key) {
		// Hacer que un ColorPreferenceModel mappee el valor del color, y
		// tambien si debe ser bold o no
		ColorPreferenceModel lineNumberForeGround = new ColorPreferenceModel(displayText, key);
		getColorModelList().add(lineNumberForeGround);
	}

	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
		list = getListControl(parent);
		gd = new GridData();
		gd.verticalAlignment = 1;
		gd.horizontalAlignment = 1;
		gd.widthHint = 180;
		gd.heightHint = 100;
		gd.horizontalSpan = numColumns - 2;
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = true;
		list.setLayoutData(gd);
		colorChooser = getColorChooserControl(parent);
		gd = new GridData();
		gd.verticalAlignment = 1;
		gd.horizontalAlignment = 1;
		gd.widthHint = -1;
		gd.heightHint = -1;
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 1;
		gd.verticalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = false;
		colorChooser.setLayoutData(gd);
		colorChooser.layout();
		initializeColorList();
	}

	private void initializeColorList() {
		for (ColorPreferenceModel colorModel : this.getColorModelList()) {
			this.list.add(colorModel.getDisplayText());
		}

		this.list.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (list != null && !list.isDisposed()) {
					list.select(0);
					updateColorChooser();
				}
			}
		});
	}

	protected void doLoad() {
		for (ColorPreferenceModel colorModel : getColorModelList()) {
			colorModel.setColorValue(PreferenceConverter.getColor(getPreferenceStore(), colorModel.getPreferenceKey()));
			colorModel.setBold(getPreferenceStore().getBoolean(colorModel.getPreferenceKey() + "#bold"));
		}
	}

	protected void doLoadDefault() {
		for (ColorPreferenceModel colorModel : getColorModelList()) {
			colorModel.setColorValue(PreferenceConverter.getDefaultColor(getPreferenceStore(), colorModel.getPreferenceKey()));
			colorModel.setBold(getPreferenceStore().getDefaultBoolean(colorModel.getPreferenceKey() + "#bold"));
		}
	}

	public void store() {
		if (getPreferenceStore() == null) {
			return;
		}
		if (presentsDefaultValue()) {
			for (ColorPreferenceModel colorModel : this.getColorModelList()) {
				this.getPreferenceStore().setToDefault(colorModel.getPreferenceKey());
			}
		} else {
			doStore();
		}
	}

	protected void doStore() {
		for (ColorPreferenceModel colorModel : getColorModelList()) {
			PreferenceConverter.setValue(getPreferenceStore(), colorModel.getPreferenceKey(), colorModel.getColorValue());
			getPreferenceStore().setValue(colorModel.getPreferenceKey() + "#bold", colorModel.isBold());
		}
	}

	public int getNumberOfControls() {
		return 3;
	}

	public List getListControl(Composite parent) {
		if (list == null) {
			list = new List(parent, 2820);
			list.setFont(parent.getFont());
			list.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					org.eclipse.swt.widgets.Widget widget = event.widget;
					if (widget == list)
						updateColorChooser();
				}
			});
			list.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					list = null;
				}
			});
		} else {
			checkParent(list, parent);
		}
		return list;
	}

	public ColorChooser getColorChooserControl(Composite parent) {
		if (colorChooser == null) {
			colorChooser = new ColorChooser(parent, 0);

			colorChooser.addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					int index = list.getSelectionIndex();
					ColorPreferenceModel cpm = colorModelList.get(index);
					cpm.setColorValue(colorChooser.getColorValue());
				}
			});

			colorChooser.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					int index = list.getSelectionIndex();
					ColorPreferenceModel cpm = colorModelList.get(index);
					cpm.setBold(colorChooser.isBold());
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			colorChooser.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					colorChooser = null;
				}
			});
		} else {
			checkParent(colorChooser, parent);
		}
		return colorChooser;
	}

	public ArrayList<ColorPreferenceModel> getColorModelList() {
		if (colorModelList == null) {
			colorModelList = new ArrayList<ColorPreferenceModel>();
		}
		return colorModelList;
	}

	private void updateColorChooser() {
		int index = list.getSelectionIndex();
		ColorPreferenceModel cpm = colorModelList.get(index);
		colorChooser.setColorValue(cpm.getColorValue());
		colorChooser.setBold(cpm.isBold());
	}
}
