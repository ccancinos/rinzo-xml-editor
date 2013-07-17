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
package ar.com.tadp.xml.rinzo.core.partitioner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * 
 * @author ccancinos
 */
public class XMLDocumentPartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension,
		IDocumentPartitionerExtension2 {

	/**
	 * @deprecated Field CONTENT_TYPES_CATEGORY is deprecated
	 */
	public static final String CONTENT_TYPES_CATEGORY = "__content_types_category";
	protected IPartitionTokenScanner partitionScanner;
	protected String legalContentTypes[];
	protected IDocument document;
	protected int previousDocumentLength;
	protected DefaultPositionUpdater positionUpdater;
	protected int startOffset;
	protected int endOffset;
	protected int deleteOffset;
	private String positionCategory;

	public XMLDocumentPartitioner(IPartitionTokenScanner scanner, String legalContentTypes[]) {
		partitionScanner = scanner;
		this.legalContentTypes = legalContentTypes;
		positionCategory = "__content_types_category" + hashCode();
		positionUpdater = new DefaultPositionUpdater(positionCategory);
	}

	public String[] getManagingPositionCategories() {
		return (new String[] { positionCategory });
	}

	public void connect(IDocument document) {
		this.document = document;
		document.addPositionCategory(positionCategory);
		initialize();
	}

	protected void initialize() {
		partitionScanner.setRange(document, 0, document.getLength());
		try {
			for (IToken token = partitionScanner.nextToken(); !token.isEOF(); token = partitionScanner.nextToken()) {
				String contentType = getTokenContentType(token);
				if (isSupportedContentType(contentType)) {
					TypedPosition p = createPosition(partitionScanner.getTokenOffset(),
							partitionScanner.getTokenLength(), contentType);
					addPosition(document, positionCategory, p);
				}
			}

		} catch (BadLocationException _ex) {
		} catch (BadPositionCategoryException _ex) {
		}
	}

	protected TypedPosition createPosition(int tokenOffset, int tokenLength, String contentType) {
		return new XMLNode(tokenOffset, tokenLength, contentType, document);
	}

	public void disconnect() {
		try {
			document.removePositionCategory(positionCategory);
		} catch (BadPositionCategoryException _ex) {
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		previousDocumentLength = event.getDocument().getLength();
		startOffset = -1;
		endOffset = -1;
		deleteOffset = -1;
	}

	public boolean documentChanged(DocumentEvent event) {
		return this.documentChanged2(event) != null;
	}

	private void rememberRegion(int offset, int length) {
		if (startOffset == -1) {
			startOffset = offset;
		} else if (offset < startOffset) {
			startOffset = offset;
		}
		int localEndOffset = offset + length;
		if (endOffset == -1) {
			endOffset = localEndOffset;
		} else if (localEndOffset > endOffset) {
			endOffset = localEndOffset;
		}
	}

	private void rememberDeletedOffset(int offset) {
		deleteOffset = offset;
	}

	private IRegion createRegion() {
		if (deleteOffset == -1) {
			return startOffset == -1 || endOffset == -1 ? 
					null : 
					(IRegion) new Region(startOffset, endOffset	- startOffset);
		}
		if (startOffset == -1 || endOffset == -1) {
			return new Region(deleteOffset, 0);
		}
		int offset = Math.min(deleteOffset, startOffset);
		int localEndOffset = Math.max(deleteOffset, endOffset);
		return new Region(offset, localEndOffset - offset);
	}

	public IRegion documentChanged2(DocumentEvent e) {
		try {
			IDocument d = e.getDocument();
			Position category[] = d.getPositions(positionCategory);
			IRegion line = d.getLineInformationOfOffset(e.getOffset());
			int reparseStart = line.getOffset();
			int partitionStart = -1;
			String contentType = null;
			int first = d.computeIndexInCategory(positionCategory, reparseStart);
			if (first > 0) {
				TypedPosition partition = (TypedPosition) category[first - 1];
				if (partition.includes(reparseStart)) {
					partitionStart = partition.getOffset();
					contentType = partition.getType();
					if (e.getOffset() == partition.getOffset() + partition.getLength()) {
						reparseStart = partitionStart;
					}
					first--;
				} else if (reparseStart == e.getOffset()
						&& reparseStart == partition.getOffset() + partition.getLength()) {
					partitionStart = partition.getOffset();
					contentType = partition.getType();
					reparseStart = partitionStart;
					first--;
				} else {
					partitionStart = partition.getOffset() + partition.getLength();
					contentType = "__dftl_partition_content_type";
				}
			}
			positionUpdater.update(e);
			for (int i = first; i < category.length; i++) {
				Position p = category[i];
				if (!p.isDeleted) {
					continue;
				}
				rememberDeletedOffset(e.getOffset());
				break;
			}

			category = d.getPositions(positionCategory);
			partitionScanner
					.setPartialRange(d, reparseStart, d.getLength() - reparseStart, contentType, partitionStart);
			int lastScannedPosition = reparseStart;
			for (IToken token = partitionScanner.nextToken(); !token.isEOF();) {
				contentType = getTokenContentType(token);
				if (!isSupportedContentType(contentType)) {
					token = partitionScanner.nextToken();
				} else {
					int start = partitionScanner.getTokenOffset();
					int length = partitionScanner.getTokenLength();
					lastScannedPosition = (start + length) - 1;
					for (; first < category.length; first++) {
						TypedPosition p = (TypedPosition) category[first];
						if (lastScannedPosition < p.offset + p.length
								&& (!p.overlapsWith(start, length) || d.containsPosition(positionCategory, start,
										length) && contentType.equals(p.getType()))) {
							break;
						}
						rememberRegion(p.offset, p.length);
						d.removePosition(positionCategory, p);
					}

					if (d.containsPosition(positionCategory, start, length)) {
						if (lastScannedPosition > e.getOffset()) {
							return createRegion();
						}
						first++;
					} else {
						try {
							TypedPosition p = createPosition(start, length, contentType);
							addPosition(d, positionCategory, p);
							rememberRegion(start, length);
						} catch (BadPositionCategoryException _ex) {
						} catch (BadLocationException _ex) {
						}
					}
					token = partitionScanner.nextToken();
				}
			}

			if (lastScannedPosition != reparseStart)
				lastScannedPosition++;
			for (first = d.computeIndexInCategory(positionCategory, lastScannedPosition); first < category.length;) {
				TypedPosition p = (TypedPosition) category[first++];
				d.removePosition(positionCategory, p);
				rememberRegion(p.offset, p.length);
			}

		} catch (BadPositionCategoryException _ex) {
		} catch (BadLocationException _ex) {
		}
		return createRegion();
	}

	protected void addPosition(IDocument d, String positionCategory, TypedPosition position)
			throws BadLocationException, BadPositionCategoryException {
		d.addPosition(positionCategory, position);
	}

	public TypedPosition findClosestPosition(int offset) {
		try {
			int index = document.computeIndexInCategory(positionCategory, offset);
			Position category[] = document.getPositions(positionCategory);
			if (category.length == 0) {
				return null;
			}
			if (index < category.length && offset == category[index].offset) {
				return (TypedPosition) category[index];
			}
			if (index > 0) {
				index--;
			}
			return (TypedPosition) category[index];
		} catch (BadPositionCategoryException _ex) {
		} catch (BadLocationException _ex) {
		}
		return null;
	}

	public TypedPosition findPreviousNonWhiteSpacePosition(int offset) {
		try {
			int index = document.computeIndexInCategory(positionCategory, offset);
			Position positions[] = document.getPositions(positionCategory);
			if (positions.length == 0) {
				return null;
			}
			if (index == positions.length) {
				index--;
			}
			for (; index >= 0; index--) {
				TypedPosition position = (TypedPosition) positions[index];
				if (position instanceof XMLNode) {
					XMLNode node = (XMLNode) position;
					if (!node.isEmpty())
						return node;
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (BadPositionCategoryException e) {
			e.printStackTrace();
		}
		return null;
	}

	public TypedPosition findPreviousPosition(int offset) {
		try {
			int index = document.computeIndexInCategory(positionCategory, offset);
			Position positions[] = document.getPositions(positionCategory);
			if (positions.length == 0) {
				return null;
			}
			if (index <= positions.length && index >= 1) {
				return (TypedPosition) positions[index - 1];
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (BadPositionCategoryException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getContentType(int offset) {
		TypedPosition p = findClosestPosition(offset);
		return p != null && p.includes(offset) ? p.getType() : "__dftl_partition_content_type";
	}

	public ITypedRegion getPartition(int offset) {
		try {
			Position category[] = document.getPositions(positionCategory);
			if (category == null || category.length == 0) {
				return new TypedRegion(0, document.getLength(), "__dftl_partition_content_type");
			}
			int index = document.computeIndexInCategory(positionCategory, offset);
			if (index < category.length) {
				TypedPosition next = (TypedPosition) category[index];
				if (offset == next.offset) {
					return new TypedRegion(next.getOffset(), next.getLength(), next.getType());
				}
				if (index == 0) {
					return new TypedRegion(0, next.offset, "__dftl_partition_content_type");
				}
				TypedPosition previous = (TypedPosition) category[index - 1];
				if (previous.includes(offset)) {
					return new TypedRegion(previous.getOffset(), previous.getLength(), previous.getType());
				}
				int endOffset = previous.getOffset() + previous.getLength();
				return new TypedRegion(endOffset, next.getOffset() - endOffset, "__dftl_partition_content_type");
			}
			TypedPosition previous = (TypedPosition) category[category.length - 1];
			if (previous.includes(offset)) {
				return new TypedRegion(previous.getOffset(), previous.getLength(), previous.getType());
			}
			int endOffset = previous.getOffset() + previous.getLength();
			return new TypedRegion(endOffset, document.getLength() - endOffset, "__dftl_partition_content_type");
		} catch (BadPositionCategoryException _ex) {
		} catch (BadLocationException _ex) {
		}
		return new TypedRegion(0, document.getLength(), "__dftl_partition_content_type");
	}

	public ITypedRegion[] computePartitioning(int offset, int length) {
		return computePartitioning(offset, length, false);
	}

	public String[] getLegalContentTypes() {
		return legalContentTypes;
	}

	protected boolean isSupportedContentType(String contentType) {
		if (contentType != null) {
			for (int i = 0; i < legalContentTypes.length; i++)
				if (legalContentTypes[i].equals(contentType)) {
					return true;
				}
		}
		return false;
	}

	protected String getTokenContentType(IToken token) {
		Object data = token.getData();
		return (data instanceof String) ? (String) data : null;
	}

	public String getContentType(int offset, boolean preferOpenPartitions) {
		return getPartition(offset, preferOpenPartitions).getType();
	}

	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		ITypedRegion region = getPartition(offset);
		if (preferOpenPartitions && region.getOffset() == offset
				&& !region.getType().equals("__dftl_partition_content_type")) {
			if (offset > 0) {
				region = getPartition(offset - 1);
				if (region.getType().equals("__dftl_partition_content_type")) {
					return region;
				}
			}
			return new TypedRegion(offset, 0, "__dftl_partition_content_type");
		}

		return region;
	}

	public ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions) {
		List<TypedRegion> list = new ArrayList<TypedRegion>();
		try {
			int endOffset = offset + length;
			Position category[] = document.getPositions(positionCategory);
			TypedPosition previous = null;
			TypedPosition current = null;
			Position gap = new Position(0);
			int startIndex = getFirstIndexEndingAfterOffset(category, offset);
			int endIndex = getFirstIndexStartingAfterOffset(category, endOffset);
			for (int i = startIndex; i < endIndex; i++) {
				current = (TypedPosition) category[i];
				int gapOffset = previous == null ? 0 : previous.getOffset() + previous.getLength();
				gap.setOffset(gapOffset);
				gap.setLength(current.getOffset() - gapOffset);
				if (includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length) || gap.getLength() > 0
						&& gap.overlapsWith(offset, length)) {
					int start = Math.max(offset, gapOffset);
					int end = Math.min(endOffset, gap.getOffset() + gap.getLength());
					list.add(new TypedRegion(start, end - start, "__dftl_partition_content_type"));
				}
				if (current.overlapsWith(offset, length)) {
					int start = Math.max(offset, current.getOffset());
					int end = Math.min(endOffset, current.getOffset() + current.getLength());
					list.add(new TypedRegion(start, end - start, current.getType()));
				}
				previous = current;
			}

			if (previous != null) {
				int gapOffset = previous.getOffset() + previous.getLength();
				gap.setOffset(gapOffset);
				gap.setLength(document.getLength() - gapOffset);
				if (includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length) || gap.getLength() > 0
						&& gap.overlapsWith(offset, length)) {
					int start = Math.max(offset, gapOffset);
					int end = Math.min(endOffset, document.getLength());
					list.add(new TypedRegion(start, end - start, "__dftl_partition_content_type"));
				}
			}
			if (list.isEmpty()) {
				list.add(new TypedRegion(offset, length, "__dftl_partition_content_type"));
			}
		} catch (BadPositionCategoryException _ex) {
		}
		TypedRegion result[] = new TypedRegion[list.size()];
		list.toArray(result);
		return result;
	}

	private boolean overlapsOrTouches(Position gap, int offset, int length) {
		return gap.getOffset() <= offset + length && offset <= gap.getOffset() + gap.getLength();
	}

	private int getFirstIndexEndingAfterOffset(Position positions[], int offset) {
		int i = -1;
		int j;
		for (j = positions.length; j - i > 1;) {
			int k = i + j >> 1;
			Position p = positions[k];
			if (p.getOffset() + p.getLength() > offset) {
				j = k;
			} else {
				i = k;
			}
		}

		return j;
	}

	private int getFirstIndexStartingAfterOffset(Position positions[], int offset) {
		int i = -1;
		int j;
		for (j = positions.length; j - i > 1;) {
			int k = i + j >> 1;
			Position p = positions[k];
			if (p.getOffset() >= offset) {
				j = k;
			} else {
				i = k;
			}
		}

		return j;
	}
	
}
