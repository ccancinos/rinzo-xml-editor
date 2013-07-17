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
package ar.com.tadp.xml.rinzo.core.outline;

/**
 * @author ccancinos
 */
public class OutlineUpdater implements Runnable {
	private final XMLOutlinePage outlinePage;
	private boolean stop = false;

	public OutlineUpdater(XMLOutlinePage outlinePage) {
		this.outlinePage = outlinePage;
	}

	public void run() {
		while (!isStop()) {
			this.sleep(2200);
			if (outlinePage.isDirty()) {
				outlinePage.setDirty(false);
				sleep(100);
				if (outlinePage.isDirty()) {
					outlinePage.internalUpdate();
				}
			}
		}
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	public synchronized boolean isStop() {
		return stop;
	}

	public synchronized void setStop() {
		this.stop = true;
	}

}
