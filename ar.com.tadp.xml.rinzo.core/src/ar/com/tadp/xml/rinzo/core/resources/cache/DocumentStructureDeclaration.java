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

/**
 * Defines the location for the language definition of an xml.
 * 
 * @author ccancinos
 */
public class DocumentStructureDeclaration {
	/**This two names corresponds to the ones defined on the xml*/
    private String publicId;
    private String systemId;
    private String namespace;
    /**This name corresponds to the cached file for this public and absolute names*/
    private String localCachedName;

    public DocumentStructureDeclaration() { }
    
    public DocumentStructureDeclaration(String publicName, String absoluteName, String localCachedName) {
        this.publicId = publicName;
        this.systemId = absoluteName;
        this.localCachedName = localCachedName;
    }

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getLocalCachedName() {
		return localCachedName;
	}

	public void setLocalCachedName(String localCachedName) {
		this.localCachedName = localCachedName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localCachedName == null) ? 0 : localCachedName.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result
				+ ((publicId == null) ? 0 : publicId.hashCode());
		result = prime * result
				+ ((systemId == null) ? 0 : systemId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentStructureDeclaration other = (DocumentStructureDeclaration) obj;
		if (localCachedName == null) {
			if (other.localCachedName != null)
				return false;
		} else if (!localCachedName.equals(other.localCachedName))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (publicId == null) {
			if (other.publicId != null)
				return false;
		} else if (!publicId.equals(other.publicId))
			return false;
		if (systemId == null) {
			if (other.systemId != null)
				return false;
		} else if (!systemId.equals(other.systemId))
			return false;
		return true;
	}
	
}