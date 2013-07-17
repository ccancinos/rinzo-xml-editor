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
package ar.com.tadp.xml.rinzo.core.keyListeners;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.TagUtils;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * In charge to observe keyboard to automatically autocomplete an ending tag <XXX></XXX>
 * or to remove it leaving a tag without body <XXX/> depending if the character ">" or "/"
 * is written at the end of a tag.
 * 
 * REVIEW refactor to use the AST. Keep in mind the AST is generated containing the last 
 * written character.
 * 
 * @author ccancinos
 */
public class AutoInsertEndTagHandler extends KeyAdapter {
    private boolean fAutoInsertEndTag = true;
    private final ISourceViewer sourceViewer;

    public AutoInsertEndTagHandler(ISourceViewer sourceViewer) {
        this.sourceViewer = sourceViewer;
    }

    public void keyReleased(KeyEvent keyevent) {
        if (keyevent.character == '>' && keyevent.stateMask == 0x20000 && fAutoInsertEndTag) {
            this.addEndTag();
        }
        if (keyevent.character == '/') {
            this.removeEndTag();
        }
    }

    //***********************************************
    // Magia para manipular el texto del editor
    // TODO mejorar muchisimo estos m�todos chotos que no se bancan mucho
    //***********************************************

	/**
     * Agrega el tag de cierre
     */
    private void addEndTag() {
        if (this.sourceViewer != null) {
            Point point = this.sourceViewer.getSelectedRange();
            if (point.y == 0 && point.x > 0) {
                IDocument idocument = this.sourceViewer.getDocument();

                try {
                    if(!">".equals(idocument.get(point.x-2, 1))) {
                        XMLNode previousNode = XMLTreeModelUtilities.getPreviousNode(idocument, point.x);
						if(XMLTreeModelUtilities.getActiveNode(idocument, point.x).getCorrespondingNode() == null && 
                        		!previousNode.isPiTag() && !previousNode.isDeclarationTag() &&
                        		!previousNode.isCommentTag() && !previousNode.isCdata()) {
                            boolean isEndTag = TagUtils.isEndTag(this.sourceViewer, point.x);
                            String str = TagUtils.getTagName(this.sourceViewer, point.x - 1);
                            if (!isEndTag && str != null) {
                                idocument.replace(point.x, 0, "</" + str + ">");
                                this.sourceViewer.setSelectedRange(point.x, 0);
                            }
                        }
                    }
                } catch (BadLocationException badlocationexception) {
                	XMLEditorPlugin.log(badlocationexception);
                }
            }
        }
    }

    /**
     * Remueve el tag de cierre cuando hago que el tag de inicio finaliza con  />
     * TODO BUGS:
     * -Si en <tag1/></tag1>, escribo <tag1//></tag1>, lo pasa a <tag1//> 
     */
    private void removeEndTag() {
        if(this.sourceViewer != null) {
            Point point = this.sourceViewer.getSelectedRange();
            if (point.y == 0 && point.x > 0) {
                IDocument idocument = this.sourceViewer.getDocument();
                
                try {
                    if(">".equals(idocument.get(point.x, 1))) {
                        int posStartEndTag = idocument.search(point.x, "</", true, false, false) + 1;
                        int posEndEndTag = idocument.search(posStartEndTag, ">", true, false, false) + 1;
                        
                        String startTagName = TagUtils.getTagName(this.sourceViewer, point.x);
                        String endTagName = TagUtils.getEndTagName(idocument.get(posStartEndTag-1, posEndEndTag-posStartEndTag+1));
                        
                        if(endTagName.equals(startTagName)) {
                            int cursorPos = point.x;
                            int size = posEndEndTag - cursorPos -1;
                            idocument.replace(cursorPos, size, "");
                            this.sourceViewer.setSelectedRange(cursorPos , 0);
                        }
                    }
                }
                catch(BadLocationException badlocationexception) {
                	XMLEditorPlugin.log(badlocationexception);
                }
            }
        }
    }

}