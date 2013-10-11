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
package ar.com.tadp.xml.rinzo.jdt.wizards;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * 
 * @author ccancinos
 *
 */
public class MessageConsoleParticipant implements IConsolePageParticipant {
	private Action closeAction;

	public void init(IPageBookViewPage page, IConsole console) {
		IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
		this.closeAction = new CloseConsoleAction(console);
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.closeAction);
	}

	public void dispose() {
		this.closeAction = null;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void activated() {
	}

	public void deactivated() {
	}

}
