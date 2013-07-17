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
package ar.com.tadp.xml.rinzo.core.highlighting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;

/**
 * 
 * @author ccancinos
 */
public class NonRuleBasedDamagerRepairer
    implements IPresentationDamager, IPresentationRepairer {

    protected IDocument document;
    protected TextAttribute defaultTextAttribute;

    public NonRuleBasedDamagerRepairer(TextAttribute defaultTextAttribute) {
        this.defaultTextAttribute = defaultTextAttribute;
    }

    public void setDocument(IDocument document) {
        this.document = document;
    }

    protected int endOfLineOf(int offset) throws BadLocationException {
		IRegion info = document.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength()) {
			return info.getOffset() + info.getLength();
		}
		int line = document.getLineOfOffset(offset);
		try {
			info = document.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException _ex) {
			return document.getLength();
		}
    }

    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged)
			try {
				IRegion info = document.getLineInformationOfOffset(event.getOffset());
				int start = Math.max(partition.getOffset(), info.getOffset());
				int end = event.getOffset() + (event.getText() != null ? event.getText().length() : event.getLength());
				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					end = info.getOffset() + info.getLength();
				} else {
					end = endOfLineOf(end);
				}
				end = Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);
			} catch (BadLocationException _ex) {
			}
		return partition;
    }

    public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		addRange(presentation, region.getOffset(), region.getLength(), defaultTextAttribute);
    }

    protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null) {
			presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr
					.getStyle()));
		}
    }
}
