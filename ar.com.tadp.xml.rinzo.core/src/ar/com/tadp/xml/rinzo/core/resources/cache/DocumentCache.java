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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Used to cache remote resources.
 *  
 * @author ccancinos
 */
public class DocumentCache {
    private static final String CACHED_FILES_PREFIX = "f";
	private String storePathURL;
    private File cacheDefinitionsFile;
    private Collection<DocumentStructureDeclaration> entries;
    private Map<String, DocumentStructureDeclaration> publicNameToEntry;
    private Map<String, DocumentStructureDeclaration> absoluteNameToEntry;
    private int fMaxRel;
    private CacheDefinitionsSerializer serializer = new CacheDefinitionsSerializer();

    private static DocumentCache instance;

    private DocumentCache() {
        this.entries = new LinkedList<DocumentStructureDeclaration>();
        this.publicNameToEntry = new HashMap<String, DocumentStructureDeclaration>();
        this.absoluteNameToEntry = new HashMap<String, DocumentStructureDeclaration>();
        this.fMaxRel = 0;
    }

    public static synchronized DocumentCache getInstance() {
        if (instance == null) {
            instance = new DocumentCache();
        }
        return instance;
    }
    
    /**
     * Retorna la url del archivo local.
     * Si el archivo buscado no es local, se devuelve la url del archivo que lo cachea.
     * Si el archivo buscado es local, se devuelve la misma url que fue pedida. 
     */
    public String getLocation(String publicName, String absoluteName) {
        File inputFile = new File(absoluteName);
		if (inputFile.exists()) {
			return absoluteName;
		}

		if(!this.contains(publicName, absoluteName)) {
			this.store(publicName, absoluteName);
		}
		
		return this.get(publicName, absoluteName);
    }
    
    /**
	 * Given a base path and a url, this method returns the path to the
	 * corresponding cache file, or the absolute remote url to the resource
	 * 
	 * If url is a path to a inexistent file in the cache, it resolve the file to the baseURL. 
	 * 
	 * @param baseURL
	 * @param url
     * @param systemId 
	 * @return
	 */
    public String getLocationFromBase(String baseURL, String publicId, String systemId) {
		String path;
		try {
			path = FileUtils.resolveURI(baseURL, systemId).toString();
			if(path.startsWith("file:") && path.contains(".cache/") && !new File(path).exists()) {
				path = path.substring(path.indexOf(".cache/")+7);
				path = FileUtils.resolveURI(baseURL, path).toString();
			}
			return getLocation(publicId, path);
		} catch (URISyntaxException e) {
			return getLocation(publicId, systemId);
		}
    }

    public synchronized boolean contains(String publicName, String absoluteRealName) {
		return FileUtils.exists(this.get(publicName, absoluteRealName));
    }
    /**
     * Se encarga de cachear el documento con el nombre p�blico y absoluto
     * especificado
     */
    public synchronized void store(String publicName, String absoluteRealName) {
    	DocumentStructureDeclaration structureDeclaration = this.findEntry(publicName, absoluteRealName);
    	DocumentStructureDeclaration newEntry = null;
    	File outputFile = null;
    	int fileIndex = this.fMaxRel;
    	
    	if(structureDeclaration != null) {
    		if(!FileUtils.exists(structureDeclaration.getLocalCachedName())) {
    			outputFile = new File(structureDeclaration.getLocalCachedName());
    		} else {
    			outputFile = this.getNewCacheFile(absoluteRealName.substring(absoluteRealName.lastIndexOf(".")));
    			structureDeclaration.setLocalCachedName(outputFile.getName());
    		}
    	} else {
    		outputFile = this.getNewCacheFile(absoluteRealName.substring(absoluteRealName.lastIndexOf(".")));
			newEntry = new DocumentStructureDeclaration(publicName, absoluteRealName, outputFile.getName());
    	}
    	
    	try {
			FileUtils.saveFile(absoluteRealName, outputFile);
			if (outputFile.length() > 0) {
				if(structureDeclaration == null) {
		            this.addEntry(newEntry);
		            this.saveCacheDefinitions();
				}
			} else {
				FileUtils.safeDelete(outputFile);
				if (fileIndex != this.fMaxRel) {
					this.fMaxRel--;
				}
			}
		} catch (Exception e) {
			FileUtils.safeDelete(outputFile);
			if (fileIndex != this.fMaxRel) {
				this.fMaxRel--;
			}
		}
    }
    
    public void clear() {
    	this.entries.clear();
    	this.absoluteNameToEntry.clear();
    	this.publicNameToEntry.clear();
    	this.fMaxRel = 0;
    	saveCacheDefinitions();
    	clearCachedFiles();
    }

    /**
     * @param urlLocation
     *            url de la ubicaci�n del directorio donde se cachear�n los
     *            documentos
     */
    public void setCacheLocation(String urlLocation) throws IOException {
        if (!urlLocation.startsWith("file:"))
            throw new IllegalArgumentException("Must be a file: URL " + urlLocation);
        String pathLocation = FileUtils.fileUrlToPath(urlLocation);
        File file = new File(pathLocation);
        if (file.exists()) {
        	if (!file.isDirectory())
        		throw new IllegalArgumentException("Not a directory " + pathLocation);
        } else {
        	if (!file.mkdir())
        		throw new IllegalArgumentException("Can't make directory " + pathLocation);
        }
        if (!urlLocation.endsWith("/")) {
            urlLocation = urlLocation + "/";
        }
        this.storePathURL = urlLocation;
        String cacheDefinitnionsPath = FileUtils.fileUrlToPath(this.storePathURL + "cacheDefinitions.xml");
        this.cacheDefinitionsFile = new File(cacheDefinitnionsPath);
        if (this.cacheDefinitionsFile.exists()) {
            this.loadCacheDefinitions();
        } else {
            if (!this.cacheDefinitionsFile.createNewFile())
                throw new RuntimeException("Unable to create cache definitions file at " + cacheDefinitnionsPath);
            this.saveCacheDefinitions();
        }
    }

	/**
     * @return url de la ubicaci�n del directorio donde se cachean los
     *         documentos
     */
    public String getCacheLocation() {
        return this.storePathURL;
    }

    /**
     * Retorna el nombre de la url al documento
     */
    private String get(String publicName, String absoluteName) {
        DocumentStructureDeclaration structureDeclaration = this.findEntry(publicName, absoluteName);
        String fileLocation = structureDeclaration == null ? null : structureDeclaration.getLocalCachedName();
        return fileLocation != null ? FileUtils
				.fileUrlToPath(this.getCacheLocation()+ fileLocation) :
				absoluteName;
    }
    
    private void saveCacheDefinitions() {
		this.serializer.saveCacheDefinitions(this.cacheDefinitionsFile, this.entries);
	}

	private void loadCacheDefinitions() throws IOException {
		for (DocumentStructureDeclaration declaration : this.serializer.getCacheDefinitions(this.cacheDefinitionsFile)) {
			this.addEntry(declaration);
		}
	}

	private void clearCachedFiles() {
    	File storeDirectory = new File(FileUtils.fileUrlToPath(this.storePathURL));
		if (storeDirectory.exists()) {
			File[] files = storeDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().startsWith(CACHED_FILES_PREFIX)) {
					FileUtils.safeDelete(files[i]);
				}
			}
		}
    }

    private void addEntry(DocumentStructureDeclaration structureDeclaration) {
    	this.entries.add(structureDeclaration);
		if (structureDeclaration.getPublicId() != null) {
			publicNameToEntry.put(structureDeclaration.getPublicId(), structureDeclaration);
		}
		if (structureDeclaration.getSystemId() != null) {
			absoluteNameToEntry.put(structureDeclaration.getSystemId(), structureDeclaration);
		}
	}

    private DocumentStructureDeclaration findEntry(String publicName, String absoluteName) {
        DocumentStructureDeclaration structureDeclaration = publicName != null ? publicNameToEntry.get(publicName) : null;
        if (structureDeclaration == null) {
            structureDeclaration = (absoluteName != null) ? absoluteNameToEntry.get(absoluteName) : null;
        }
        return structureDeclaration;
    }

    /**
     * Retorna un @link{File} correspondiente al nuevo archivo donde se podr� cachear un documento
     */
    private File getNewCacheFile(String fileExtension) {
        try {
            File file = null;
            do {
                String relativeCachedName = CACHED_FILES_PREFIX + ++fMaxRel + fileExtension;
                String fullPathRelativeCachedName = FileUtils.fileUrlToPath(storePathURL + relativeCachedName);
                file = new File(fullPathRelativeCachedName);
            } while (file.exists());
            file.createNewFile();
            return file;
        } catch (Exception exception) {
            throw new RuntimeException("Error trying to create a file to save in cache a document", exception);
        }
    }


}
