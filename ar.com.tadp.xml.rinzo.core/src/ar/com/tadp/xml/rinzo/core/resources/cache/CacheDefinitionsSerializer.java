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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * @author ccancinos
 */
public class CacheDefinitionsSerializer {
    private static final String NULPUB = "<pub null='true'/>";
    private static final String BEGPUB = "<pub>";
    private static final String ENDPUB = "</pub>";
    private static final String NULABS = "<abs null='true'/>";
    private static final String BEGABS = "<abs>";
    private static final String ENDABS = "</abs>";
    private static final String BEGREL = "<rel>";
    private static final String ENDREL = "</rel>";
    private static final String BEGFIL = "<file>";
    private static final String ENDFIL = "</file>";

    
    private static final String INDENT = "  ";
    private static final String FILE_START = "<?xml version='1.0' encoding='UTF-8'?>" + FileUtils.EOL + 
    "<!-- Cached file directory. Do not edit this file by hand -->" + FileUtils.EOL +
    "<meta>" + FileUtils.EOL;
    
    private static final String FILE_END = "</meta>" + FileUtils.EOL;
    
    private static final MessageFormat FULL_FILE_DEF = new MessageFormat(
    		INDENT + "<file>" + FileUtils.EOL +
    		INDENT + INDENT + "<pub>{0}</pub>" + FileUtils.EOL +
    		INDENT + INDENT + "<abs>{1}</abs>" + FileUtils.EOL +
    		INDENT + INDENT + "<rel>{2}</rel>" + FileUtils.EOL +
    		INDENT + "</file>"
    		); 

    private static final MessageFormat NO_PUB_FILE_DEF = new MessageFormat(
    		INDENT + "<file>" + FileUtils.EOL +
    		INDENT + INDENT + "<pub null='true'/>" + FileUtils.EOL +
    		INDENT + INDENT + "<abs>{1}</abs>" + FileUtils.EOL +
    		INDENT + INDENT + "<rel>{2}</rel>" + FileUtils.EOL +
    		INDENT + "</file>"
    		); 

    /**
     * Levanta las el mapeo de los documentos cacheados de la persistencia en
     * xml
     * @param cacheDefinitionsFile 
     */
    public Collection<DocumentStructureDeclaration> getCacheDefinitions(File cacheDefinitionsFile) throws IOException {
    	Collection<DocumentStructureDeclaration> entries = new ArrayList<DocumentStructureDeclaration>();
        char ac[] = FileUtils.readContents(fileReader(cacheDefinitionsFile));
        int i = 0;
        int j = ac.length;
        do {
            i = skipto('<', ac, i, j);
            if (i != j) {
                boolean flag = match(BEGPUB, ac, i, j);
                if (!flag && !match(NULPUB, ac, i, j)) {
                    i++;
                } else {
                    String s = null;
                    if (flag) {
                        i += BEGPUB.length();
                        int k = i;
                        i = skipto('<', ac, i, j);
                        if (i == j || !match(ENDPUB, ac, i, j))
                            throw new IllegalStateException("Missing " + ENDPUB + " in meta file");
                        s = decode(new String(ac, k, i - k));
                        i += ENDPUB.length();
                    } else {
                        i += NULPUB.length();
                    }
                    String s1 = null;
                    i = skipto('<', ac, i, j);
                    if (match(NULABS, ac, i, j)) {
                        i += NULABS.length();
                    } else {
                        if (i == j || !match(BEGABS, ac, i, j))
                            throw new IllegalStateException("Missing " + NULABS + " in meta file");
                        i += BEGABS.length();
                        int l = i;
                        i = skipto('<', ac, i, j);
                        if (i == j || !match(ENDABS, ac, i, j))
                            throw new IllegalStateException("Missing </abs> in meta file");
                        s1 = decode(new String(ac, l, i - l));
                        i += ENDABS.length();
                    }
                    i = skipto('<', ac, i, j);
                    if (i == j || !match(BEGREL, ac, i, j))
                        throw new IllegalStateException("Missing <rel> in meta file");
                    i += BEGREL.length();
                    int i1 = i;
                    i = skipto('<', ac, i, j);
                    if (i == j || !match(ENDREL, ac, i, j))
                        throw new IllegalStateException("Missing </rel> in meta file");
                    String s2 = new String(ac, i1, i - i1);
                    i += ENDREL.length();
                    //FIXME URGENT!!! aca se comprobaba que el nombre del archivo en cache termine con un n�mero menor al de fMaxRel.
//                    try {
//                        int j1 = Integer.parseInt(s2.substring(1));
//                        if (fMaxRel < j1)
//                            fMaxRel = j1;
//                    } catch (NumberFormatException _ex) {
//                        throw new IllegalStateException("Unknown rel format " + s2);
//                    }
                    entries.add(new DocumentStructureDeclaration(s, s1, s2));
                }
            } else {
                return entries;
            }
        } while (true);
    }

    public void saveCacheDefinitions(File cacheDefinitionsFile, Collection<DocumentStructureDeclaration> entries) {
        Writer writer = fileWriter(cacheDefinitionsFile);
        try {
        	writer.write(FILE_START);
            for (DocumentStructureDeclaration structureDeclaration : entries) {
                Object[] params = {this.encode(structureDeclaration.getPublicId()), this.encode(structureDeclaration.getSystemId()), structureDeclaration.getLocalCachedName()};
                MessageFormat formatter = (structureDeclaration.getPublicId() == null) ? NO_PUB_FILE_DEF : FULL_FILE_DEF;
                writer.write(formatter.format(params));
            }
        	writer.write(FILE_END);
        	
        } catch (Exception e) {
            throw new RuntimeException("Error trying to write the cached file directory",e);
        } finally {
            try {
                writer.close();
            } catch (IOException exception) {
                throw new RuntimeException("Error trying to clse the cached file directory", exception);
            }
        }
        return;
    }
    
    public void saveCacheDefinitionsOld(File cacheDefinitionsFile, Collection<DocumentStructureDeclaration> entries) {
        Writer writer = fileWriter(cacheDefinitionsFile);
        try {
            writer.write("<?xml version='1.0' encoding='UTF-8'?>");
            writeEOL(writer);
            writer.write("<!-- Cached file directory. Do not edit this file by hand -->");
            writeEOL(writer);
            writer.write("<meta>");
            writeEOL(writer);
            for (DocumentStructureDeclaration structureDeclaration : entries) {
                writer.write("  " + BEGFIL);
                writeEOL(writer);
                if (structureDeclaration.getPublicId() != null) {
                    writer.write("    " + BEGPUB);
                    writer.write(encode(structureDeclaration.getPublicId()));
                    writer.write(ENDPUB);
                } else {
                    writer.write("    " + NULPUB);
                }
                writeEOL(writer);
                if (structureDeclaration.getSystemId() != null) {
                    writer.write("    " + BEGABS);
                    writer.write(encode(structureDeclaration.getSystemId()));
                    writer.write(ENDABS);
                } else {
                    writer.write("    " + NULABS);
                }
                writeEOL(writer);
                writer.write("    " + BEGREL);
                writer.write(structureDeclaration.getLocalCachedName());
                writer.write(ENDREL);
                writeEOL(writer);
                writer.write("  " + ENDFIL);
            }

            writer.write("</meta>");
            writeEOL(writer);
        } catch (Exception e) {
            throw new RuntimeException("Error trying to write the cached file directory",e);
        } finally {
            try {
                writer.close();
            } catch (IOException exception) {
                throw new RuntimeException("Error trying to clse the cached file directory", exception);
            }
        }
        return;
    }

    private int skipto(char c, char ac[], int i, int j) {
        for (; i < j && ac[i] != c; i++)
            if (ac[i] == c)
                break;

        return i;
    }

    private boolean match(String s, char ac[], int i, int j) {
        int k = i + s.length();
        if (k > j)
            return false;
        int l = 0;
        for (; i < k; i++) {
            if (ac[i] != s.charAt(l))
                return false;
            l++;
        }

        return true;
    }

    private String decode(String s) {
        StringBuffer stringbuffer = new StringBuffer();
        char ac[] = s.toCharArray();
        int i = 0;
        for (int j = ac.length; i < j; i++)
            if (ac[i] == '&') {
                if (match("&lt;", ac, i, j)) {
                    stringbuffer.append('<');
                    i += 3;
                } else if (match("&amp;", ac, i, j)) {
                    stringbuffer.append('&');
                    i += 4;
                } else {
                    stringbuffer.append('&');
                }
            } else {
                stringbuffer.append(ac[i]);
            }

        return stringbuffer.toString();
    }

    private void writeEOL(Writer writer) throws IOException {
        writer.write(FileUtils.EOL);
    }

    private String encode(String s) {
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        for (int j = s.length(); i < j; i++) {
            char c = s.charAt(i);
            if (c == '<')
                stringbuffer.append("&lt;");
            else if (c == '&')
                stringbuffer.append("&amp;");
            else
                stringbuffer.append(c);
        }

        return stringbuffer.toString();
    }

    private Reader fileReader(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    private Writer fileWriter(File file) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        } catch (Exception exception) {
            throw new RuntimeException("Error trying to create a writer for a file", exception);
        }
    }

}
