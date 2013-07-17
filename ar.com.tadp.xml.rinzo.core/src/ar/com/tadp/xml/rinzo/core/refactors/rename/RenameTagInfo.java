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
package ar.com.tadp.xml.rinzo.core.refactors.rename;

import org.eclipse.core.resources.IFile;

/**
 * An info object that holds the information that is passed from the user to the
 * refactoring.
 * 
 * @author ccancinos
 */
public class RenameTagInfo {

	// the offset of the property to be renamed in the file
	private int offset;
	// the new name for the property
	private String newName;
	// the old name of the property (as selected by the user)
	private String oldName;
	// the file that contains the property to be renamed
	private IFile sourceFile;
	// whether the refactoring should also change the name of the property
	// in corresponding properties files in the same bundle (i.e. which start
	// with the same name)
	private boolean updateAllInParent;
	// whether the refactoring should also update properties files in other
	// projects than the current one
	private boolean allInFile;
	private boolean updateCurrentTag;

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(final String newName) {
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(final String oldName) {
		this.oldName = oldName;
	}

	public IFile getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(final IFile sourceFile) {
		this.sourceFile = sourceFile;
	}

	public boolean isUpdateAllInFile() {
		return allInFile;
	}

	public void setAllInFile(final boolean allInFile) {
		this.allInFile = allInFile;
	}

	public boolean isUpdateAllInParent() {
		return updateAllInParent;
	}

	public void setAllInParent(final boolean updateAllInParent) {
		this.updateAllInParent = updateAllInParent;
	}

	public void setCurrentTag(boolean updateCurrentTag) {
		this.updateCurrentTag = updateCurrentTag;
	}
	
	public boolean isUpdateCurrentTag() {
		return updateCurrentTag;
	}
	
}
