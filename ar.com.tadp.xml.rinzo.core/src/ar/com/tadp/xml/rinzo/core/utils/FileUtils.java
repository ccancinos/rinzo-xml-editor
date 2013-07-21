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
package ar.com.tadp.xml.rinzo.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author ccancinos
 */
public class FileUtils {
	public static final String EOL;
	private static final String FILE_PROTOCOL = "file:";
	protected static final String PROTOCOL_PATTERN = ":"; 
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String TAB = "\t";

	static {
		EOL = File.separatorChar != '\\' ? 
				File.separatorChar != '/' ? "\r"
				: "\n" : "\r\n";
	}

	public static String relPathToUrl(String s) {
		char c = File.separatorChar;
		return c == '/' ? s : s.replace(c, '/');
	}

	/**
	 * Creates an absolute URI from a relative one
	 * 
	 * @param basePath
	 * @param relativePath
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI resolveURI(String basePath, String relativePath) throws URISyntaxException {
		return Utils.isEmpty(basePath) ? null : createURI(basePath).resolve(relativePath.replaceAll(" ", "%20"));
	}

	public static char[] readContents(Reader reader) throws IOException {
		int i = 4096;
		char ac[] = new char[i];
		char ac1[] = new char[0];
		int j = 0;
		do {
			int k = reader.read(ac, 0, i);
			if (k < 0)
				break;
			if (k > 0) {
				int l = ac1.length;
				char ac2[] = new char[l + k];
				System.arraycopy(ac1, 0, ac2, 0, l);
				System.arraycopy(ac, 0, ac2, l, k);
				ac1 = ac2;
				if (++j >= 8 && i < 300000) {
					j = 0;
					i *= 2;
					ac = new char[i];
				}
			}
		} while (true);
		return ac1;
	}
	
	public static String fileUrlToPath(String s) {
		String s1 = s;
		int i = s.indexOf(':');
		int j = s.indexOf('/');
		if (i > 0 && (j < 0 || i < j)) {
			if (!s.startsWith("file:"))
				throw new IllegalArgumentException(
						"Url must begin with \"file:\"");
			int k = "file:".length();
			int l = s.length();
			int i1;
			for (i1 = 0; k < l && s.charAt(k) == '/'; i1++)
				k++;
	
			if (i1 > 0 && (i1 & 1) == 0)
				k -= 2;
			s1 = (File.separatorChar != '/' ? "" : "/") + s.substring(k);
		}
		if (File.separatorChar != '/')
			s1 = s1.replace('/', File.separatorChar);
		return s1;
	}

	public static String addProtocol(String uri) {
	    if (!hasProtocol(uri))
	    {                           
	      String prefix = FILE_PROTOCOL;
	      prefix += uri.startsWith("/") ? "//" : "///";
	      uri = prefix + uri;
	    }
	    return uri;
	}

	private static URI createURI(String path) throws URISyntaxException {
		return new URI(path.replaceAll(" ", "%20"));
	}
	

	private static boolean hasProtocol(String uri)
	  {
	    boolean result = false;     
	    if (uri != null)
	    {
	      int index = uri.indexOf(PROTOCOL_PATTERN);
	      if (index != -1 && index > 2) // assume protocol with be length 3 so that the'C' in 'C:/' is not interpreted as a protocol
	      {
	        result = true;
	      }
	    }
	    return result;
	  }     


    /**
     * Guarda un documento localmente en la cache
     */
    public static void saveFile(String inputFileName, File outputFile) {
    	InputStream openStream = null;
    	BufferedReader reader = null;
    	BufferedWriter writer = null;
        try {
            File inputFile = new File(inputFileName);
            if (!inputFile.exists()) {
				openStream = new URL(inputFileName).openStream();
                InputStreamReader is = new InputStreamReader(openStream);
                String encoding = is.getEncoding();
				reader = new BufferedReader(is);
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile,
                        false), encoding));
				
				String line = reader.readLine();
				while(line != null) {
					writer.write(line);
					writer.newLine();
					writer.flush();
					line = reader.readLine();
				}
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error trying to save \'" + inputFileName + "\' in the cache.", exception);
        } finally {
        	try {
				if(writer != null) {
					writer.flush();
					writer.close();
				}
				if(reader != null) {
					reader.close();
				}
				if(openStream != null) {
					openStream.close();
				}
			} catch (IOException e) {
	            throw new RuntimeException("Error trying to close files while saving \'" + inputFileName + "\' in the cache.", e);
			}
        }
    }

	public static boolean exists(String localCachedName) {
		return new File(localCachedName).exists();
	}
	
	public static void safeClose(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
		}
	}
	
	public static void safeDelete(File c) {
		try {
			if (c != null) {
				c.delete();
			}
		} catch (Exception e) {
		}
	}

	public static String getLineSeparator(IProject project) {
	    String lineSeparator = null;
	
	    if (Platform.isRunning()) {
	        // line delimiter in project preference
	        IScopeContext[] scopeContext;
	        if (project != null) {
	            scopeContext= new IScopeContext[] { new ProjectScope(project) };
	            lineSeparator= Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
	            if (lineSeparator != null)
	                return lineSeparator;
	        }
	
	        // line delimiter in workspace preference
	        scopeContext= new IScopeContext[] { InstanceScope.INSTANCE };
	        lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
	        if (lineSeparator != null)
	            return lineSeparator;
	    }
	
	    // system line delimiter
	    return System.getProperty("line.separator");
	}

	public static IProject getProject(ITextEditor textEditor) {
	    IEditorInput editorInput = textEditor.getEditorInput();
	    if (editorInput == null) {
	        return null;
	    }
	    IResource resource = (IResource) editorInput.getAdapter(IResource.class);
	    if (resource == null) {
	        return null;
	    }
	    return resource.getProject();
	}

	public static String getLineSeparator(ITextEditor textEditor) {
	    return getLineSeparator(getProject(textEditor));
	}
	
}
