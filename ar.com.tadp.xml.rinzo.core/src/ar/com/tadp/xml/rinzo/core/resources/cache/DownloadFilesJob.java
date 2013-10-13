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
package ar.com.tadp.xml.rinzo.core.resources.cache;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * In charge of displaying a notification while downloading files to cache
 * 
 * @author ccancinos
 */
public class DownloadFilesJob extends Job {
	private final Map<String, String> storeFiles;
	private boolean cancel = false;

	/**
	 * 
	 * @param storeFiles
	 *            mapping between public and absolute name of each file to be
	 *            downloaded
	 */
	public DownloadFilesJob(Map<String, String> storeFiles) {
		super("Saving files to Rinzo's cache");
		this.storeFiles = storeFiles;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
		for (Map.Entry<String, String> storeFile : storeFiles.entrySet()) {
			try {
				if (this.cancel) {
					break;
				}
				String publicName = storeFile.getKey();
				String absoluteRealName = storeFile.getValue();
				monitor.setTaskName("Downloading: " + publicName + "\n" + absoluteRealName);
				DocumentCache.getInstance().innerStore(publicName, absoluteRealName);
				monitor.worked(1);
			} catch (Exception e) {
				// DO NOTHING, PROCESS NEXT FILE
			}
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	protected void canceling() {
		this.cancel = true;
	}
}