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
package ar.com.tadp.xml.rinzo.core.refactors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import ar.com.tadp.xml.rinzo.core.refactors.rename.RenameTagInfo;

/**
 * The processor is where the work is delegated to if participants are involved.
 * The processor loads the participants and manages the lifecycle of the
 * refactoring. In order to do that, the refactoring entry point methods must be
 * implemented.
 * 
 * @author ccancinos
 */
public class BaseRefactoringProcessor extends RefactoringProcessor {

	private final RenameTagInfo info;
	private final RefactoringProcessorDelegate delegate;

	public BaseRefactoringProcessor(final RenameTagInfo info, RefactoringProcessorDelegate delegate) {
		this.info = info;
		this.delegate = delegate;
	}

	public Object[] getElements() {
		// usually, this would be some element object in the object model on
		// which
		// we work (e.g. a Java element if we were in the Java Model); in this
		// case
		// we have only the property name
		return new Object[] { info.getOldName() };
	}

	public String getIdentifier() {
		return getClass().getName();
	}

	public String getProcessorName() {
		return delegate.getName();
	}

	public boolean isApplicable() throws CoreException {
		return true;
	}

	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) {
		return delegate.checkInitialConditions();
	}

	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm,
			final CheckConditionsContext context) {
		return delegate.checkFinalConditions(pm, context);
	}

	public Change createChange(final IProgressMonitor pm) {
		CompositeChange result = new CompositeChange(getProcessorName());
		delegate.createChange(pm, result);
		return result;
	}

	public RefactoringParticipant[] loadParticipants(
			final RefactoringStatus status,
			final SharableParticipants sharedParticipants) {
		// This would be the place to load the participants via the
		// ParticipantManager and decide which of them are allowed to
		// participate.
		return new RefactoringParticipant[0];
	}
}
