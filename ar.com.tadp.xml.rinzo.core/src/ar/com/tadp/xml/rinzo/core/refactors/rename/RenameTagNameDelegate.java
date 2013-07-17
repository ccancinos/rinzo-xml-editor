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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.visitor.FindTagsByNameVisitor;
import ar.com.tadp.xml.rinzo.core.refactors.RefactoringProcessorDelegate;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * Delegate object that contains the logic used by the processor to rename a Tag.
 * 
 * @author ccancinos
 */
public class RenameTagNameDelegate implements RefactoringProcessorDelegate {
	private final RenameTagInfo info;
	private final IDocument document;
	private final XMLNode rootNode;

	RenameTagNameDelegate(final RenameTagInfo info, XMLNode xmlNode, IDocument document) {
		this.info = info;
		this.rootNode = xmlNode;
		this.document = document;
	}

	public String getName() {
		return "Rename Tag";
	}

	public RefactoringStatus checkInitialConditions() {
		RefactoringStatus result = new RefactoringStatus();
		IFile sourceFile = info.getSourceFile();
		if (sourceFile == null || !sourceFile.exists()) {
			result.addFatalError("File does not exist");
		} else if (info.getSourceFile().isReadOnly()) {
			result.addFatalError("File is read only");
		}

		XMLNode node = XMLTreeModelUtilities.getActiveNode(this.document, this.info.getOffset());
		if(!node.isTag() && !node.isEmptyTag()) {
			result.addFatalError("Cannot rename current selection.");
		}
		
		return result;
	}

	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm,
			final CheckConditionsContext ctxt) {
		RefactoringStatus result = new RefactoringStatus();
		pm.beginTask("Checking", 100);
		pm.worked(50);

		if (ctxt != null) {
			IFile[] arrayFiles = new IFile[] {info.getSourceFile()};
			IConditionChecker checker = ctxt.getChecker(ValidateEditChecker.class);
			ValidateEditChecker editChecker = (ValidateEditChecker) checker;
			editChecker.addFiles(arrayFiles);
		}
		
		pm.done();
		return result;
	}

	public void createChange(final IProgressMonitor pm,
			final CompositeChange rootChange) {
		try {
			pm.beginTask("Collecting changes", 100);
			// the property which was directly selected by the user
			pm.worked(10);
			// all files in the same bundle
			if(info.isUpdateAllInParent()) {
				rootChange.addAll(createChangesForParent());
			} else {
				if(info.isUpdateAllInFile()) {
					rootChange.addAll(createChangesForFile());
				} else {
					rootChange.add(createSingleRenameChange());
				}
			}
			
			pm.worked(90);
		} finally {
			pm.done();
		}
	}

	private Change createSingleRenameChange() {
		TextFileChange result = new TextFileChange(info.getSourceFile().getName(), info.getSourceFile());
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		result.setEdit(fileChangeRootEdit);

		XMLNode activeNode = XMLTreeModelUtilities.getActiveNode(this.document, info.getOffset());
		this.addReplaceEdit(activeNode, fileChangeRootEdit);
		
		return result;
	}

	private Change[] createChangesForFile() {
	    List<TextFileChange> result = new ArrayList<TextFileChange>();
		TextFileChange tfc = new TextFileChange(info.getSourceFile().getName(), info.getSourceFile());
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		tfc.setEdit(fileChangeRootEdit);

	    FindTagsByNameVisitor visitor = new FindTagsByNameVisitor(info.getOldName());
		this.rootNode.accept(visitor);
		
		for (XMLNode node : visitor.getNodes()) {
			this.addReplaceEdit(node, fileChangeRootEdit);
		}
		result.add(tfc);
		
		return result.toArray(new Change[result.size()]);
	}

	private Change[] createChangesForParent() {
	    List<TextFileChange> result = new ArrayList<TextFileChange>();
		TextFileChange tfc = new TextFileChange(info.getSourceFile().getName(), info.getSourceFile());
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		tfc.setEdit(fileChangeRootEdit);

	    FindTagsByNameVisitor visitor = new FindTagsByNameVisitor(info.getOldName());
		XMLNode activeNode = XMLTreeModelUtilities.getActiveNode(this.document, info.getOffset());
		activeNode.getParent().accept(visitor);
		
		for (XMLNode node : visitor.getNodes()) {
			this.addReplaceEdit(node, fileChangeRootEdit);
		}
		result.add(tfc);
		
		return result.toArray(new Change[result.size()]);
	}

	private void addReplaceEdit(XMLNode node, MultiTextEdit fileChangeRootEdit) {
		ReplaceEdit edit = new ReplaceEdit(node.getSelectionOffset(), info.getOldName().length(), info.getNewName());
		fileChangeRootEdit.addChild(edit);
		
		XMLNode correspondingNode = node.getCorrespondingNode();
		if(correspondingNode != null) {
			int offset = correspondingNode.getSelectionOffset();
			edit = new ReplaceEdit(offset, info.getOldName().length(), info.getNewName());
			fileChangeRootEdit.addChild(edit);
		}
	}

}
