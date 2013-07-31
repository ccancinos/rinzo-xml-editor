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
public class CacheFileJob extends Job {
	private final Map<String, String> storeFiles;
	private boolean cancel = false;

	public CacheFileJob(Map<String, String> storeFiles) {
		super("Saving files to Rinzo's cache");
		this.storeFiles = storeFiles;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);//storeFiles.size());
		for (Map.Entry<String, String> storeFile : storeFiles.entrySet()) {
			try {
				if(this.cancel) {
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