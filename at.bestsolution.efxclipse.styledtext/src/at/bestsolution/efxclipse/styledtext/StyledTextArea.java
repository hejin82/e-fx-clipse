/*******************************************************************************
* Copyright (c) 2013 IBM & BestSolution.at and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* 	Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
* 	IBM Corporation - initial API and implementation
*******************************************************************************/
package at.bestsolution.efxclipse.styledtext;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import at.bestsolution.efxclipse.styledtext.skin.StyledTextSkin;

public class StyledTextArea extends Control {
	private ObjectProperty<StyledTextContent> contentProperty = new SimpleObjectProperty<StyledTextContent>(this, "content", new DefaultContent());
	private StyledTextRenderer renderer = new StyledTextRenderer();
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new StyledTextSkin(this);
	}

	public void setContent(StyledTextContent content) {
		contentProperty.set(content);
	}
	
	public StyledTextContent getContent() {
		return contentProperty.get();
	}
	
	public ObjectProperty<StyledTextContent> contentProperty() {
		return contentProperty;
	}
	
	public void setStyleRange(StyleRange range) {
		if (range != null) {
			if (range.isUnstyled()) {
				setStyleRanges(range.start, range.length, null, null, false);
			} else {
				setStyleRanges(range.start, 0, null, new StyleRange[]{range}, false);
			}
		} else {
			setStyleRanges(0, 0, null, null, true);
		}
	}
	
	public void setStyleRanges(int start, int length, int[] ranges, StyleRange[] styles) {
		if (ranges == null || styles == null) {
			setStyleRanges(start, length, null, null, false);
		} else {
			setStyleRanges(start, length, ranges, styles, false);
		}
	}
	
	public void setStyleRanges(int[] ranges, StyleRange[] styles) {
		if (ranges == null || styles == null) {
			setStyleRanges(0, 0, null, null, true);
		} else {
			setStyleRanges(0, 0, ranges, styles, true);
		}
	}
	
	public void setStyleRanges(StyleRange[] ranges) {
		setStyleRanges(0, 0, null, ranges, true);
	}
	
	public void replaceStyleRanges(int start, int length, StyleRange[] ranges) {
	 	if (ranges == null) throw new IllegalArgumentException();
	 	setStyleRanges(start, length, null, ranges, false);
	}
	
	void setStyleRanges(int start, int length, int[] ranges, StyleRange[] styles, boolean reset) {
		int charCount = getContent().getCharCount();
		int end = start + length;
		if (start > end || start < 0) {
			throw new IllegalArgumentException();
		}
		
		if (styles != null) {
			if (end > charCount) {
				throw new IllegalArgumentException();
			}
			if (ranges != null) {
				if (ranges.length != styles.length << 1) throw new IllegalArgumentException();
			}
			int lastOffset = 0;
//			boolean variableHeight = false; 
			for (int i = 0; i < styles.length; i ++) {
				if (styles[i] == null) {
					throw new IllegalArgumentException();
				}
				int rangeStart, rangeLength;
				if (ranges != null) {
					rangeStart = ranges[i << 1];
					rangeLength = ranges[(i << 1) + 1];
				} else {
					rangeStart = styles[i].start;
					rangeLength = styles[i].length;
				}
				if (rangeLength < 0) {
					throw new IllegalArgumentException(); 
				}
				if (!(0 <= rangeStart && rangeStart + rangeLength <= charCount)) {
					throw new IllegalArgumentException();
				}
				if (lastOffset > rangeStart) {
					throw new IllegalArgumentException();
				}
//				variableHeight |= styles[i].isVariableHeight();
				lastOffset = rangeStart + rangeLength;
			}
//			if (variableHeight) setVariableLineHeight();
		}
		
		if( reset ) {
			renderer.setStyleRanges(null, null);
		} else {
			renderer.updateRanges(start, length, length);
		}
		
		if (styles != null && styles.length > 0) {
			renderer.setStyleRanges(ranges, styles);
		}
	}
	
	public StyleRange[] getStyleRanges(int start, int length, boolean includeRanges) {
		StyleRange[] ranges = renderer.getStyleRanges(start, length, includeRanges);
		if (ranges != null) return ranges;
		return new StyleRange[0];
	}
	
	/*******************************************************************************
	 * Copyright (c) 2000, 2011 IBM Corporation and others.
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html
	 *
	 * Contributors:
	 *     IBM Corporation - initial API and implementation
	 *******************************************************************************/
	static class StyledTextRenderer {
		StyleRange[] stylesSet;
		int stylesSetCount = 0;
		int[] ranges;
		int styleCount;
		StyleRange[] styles;
		boolean hasLinks;
		
		final static boolean COMPACT_STYLES = true;
		final static boolean MERGE_STYLES = true;

		final static int GROW = 32;
		
		void setStyleRanges (int[] newRanges, StyleRange[] newStyles) {
			if (newStyles == null) {
				stylesSetCount = styleCount = 0;
				ranges = null;
				styles = null;
				stylesSet = null;
				hasLinks = false;
				return;
			}
			if (newRanges == null && COMPACT_STYLES) {
				newRanges = new int[newStyles.length << 1];		
				StyleRange[] tmpStyles = new StyleRange[newStyles.length];
				if (stylesSet == null) stylesSet = new StyleRange[4];
				for (int i = 0, j = 0; i < newStyles.length; i++) {
					StyleRange newStyle = newStyles[i];
					newRanges[j++] = newStyle.start;
					newRanges[j++] = newStyle.length;
					int index = 0;
					while (index < stylesSetCount) {
						if (stylesSet[index].similarTo(newStyle)) break;
						index++;
					}
					if (index == stylesSetCount) {
						if (stylesSetCount == stylesSet.length) {
							StyleRange[] tmpStylesSet = new StyleRange[stylesSetCount + 4];
							System.arraycopy(stylesSet, 0, tmpStylesSet, 0, stylesSetCount);
							stylesSet = tmpStylesSet;
						}
						stylesSet[stylesSetCount++] = newStyle;
					}
					tmpStyles[i] = stylesSet[index];
				}
				newStyles = tmpStyles;
			}
			
			if (styleCount == 0) {
				if (newRanges != null) {
					ranges = new int[newRanges.length];
					System.arraycopy(newRanges, 0, ranges, 0, ranges.length);
				}
				styles = new StyleRange[newStyles.length];
				System.arraycopy(newStyles, 0, styles, 0, styles.length);
				styleCount = newStyles.length;
				return;
			}
			if (newRanges != null && ranges == null) {
				ranges = new int[styles.length << 1];
				for (int i = 0, j = 0; i < styleCount; i++) {
					ranges[j++] = styles[i].start;
					ranges[j++] = styles[i].length;
				}
			}
			if (newRanges == null && ranges != null) {
				newRanges = new int[newStyles.length << 1];
				for (int i = 0, j = 0; i < newStyles.length; i++) {
					newRanges[j++] = newStyles[i].start;
					newRanges[j++] = newStyles[i].length;
				}
			}
			if (ranges != null) {
				int rangeCount = styleCount << 1;
				int start = newRanges[0];
				int modifyStart = getRangeIndex(start, -1, rangeCount), modifyEnd;
				boolean insert = modifyStart == rangeCount;
				if (!insert) {
					int end = newRanges[newRanges.length - 2] + newRanges[newRanges.length - 1];
					modifyEnd = getRangeIndex(end, modifyStart - 1, rangeCount);
					insert = modifyStart == modifyEnd && ranges[modifyStart] >= end;
				}
				if (insert) {
					addMerge(newRanges, newStyles, newRanges.length, modifyStart, modifyStart);
					return;
				}
				modifyEnd = modifyStart;
				int[] mergeRanges = new int[6];
				StyleRange[] mergeStyles = new StyleRange[3];
				for (int i = 0; i < newRanges.length; i += 2) {
					int newStart = newRanges[i];
					int newEnd = newStart + newRanges[i + 1];
					if (newStart == newEnd) continue;
					int modifyLast = 0, mergeCount = 0;
					while (modifyEnd < rangeCount) {
						if (newStart >= ranges[modifyStart] + ranges[modifyStart + 1]) modifyStart += 2;
						if (ranges[modifyEnd] + ranges[modifyEnd + 1] > newEnd) break;
						modifyEnd += 2;
					}
					if (ranges[modifyStart] < newStart && newStart < ranges[modifyStart] + ranges[modifyStart + 1]) {
						mergeStyles[mergeCount >> 1] = styles[modifyStart >> 1];
						mergeRanges[mergeCount] = ranges[modifyStart];
						mergeRanges[mergeCount + 1] = newStart - ranges[modifyStart];				
						mergeCount += 2;
					}
					mergeStyles[mergeCount >> 1] = newStyles[i >> 1];
					mergeRanges[mergeCount] = newStart;
					mergeRanges[mergeCount + 1] = newRanges[i + 1];
					mergeCount += 2;
					if (modifyEnd < rangeCount && ranges[modifyEnd] < newEnd && newEnd < ranges[modifyEnd] + ranges[modifyEnd + 1]) {
						mergeStyles[mergeCount >> 1] = styles[modifyEnd >> 1];
						mergeRanges[mergeCount] = newEnd;
						mergeRanges[mergeCount + 1] = ranges[modifyEnd] + ranges[modifyEnd + 1] - newEnd;
						mergeCount += 2;
						modifyLast = 2;
					}
					int grow = addMerge(mergeRanges, mergeStyles, mergeCount, modifyStart, modifyEnd + modifyLast);
					rangeCount += grow;
					modifyStart = modifyEnd += grow;
				}
			} else {
				int start = newStyles[0].start;
				int modifyStart = getRangeIndex(start, -1, styleCount), modifyEnd;
				boolean insert = modifyStart == styleCount;
				if (!insert) {
					int end = newStyles[newStyles.length - 1].start + newStyles[newStyles.length - 1].length;
					modifyEnd = getRangeIndex(end, modifyStart - 1, styleCount);
					insert = modifyStart == modifyEnd && styles[modifyStart].start >= end;
				}
				if (insert) {
					addMerge(newStyles, newStyles.length, modifyStart, modifyStart);
					return;
				}
				modifyEnd = modifyStart;
				StyleRange[] mergeStyles = new StyleRange[3];
				for (int i = 0; i < newStyles.length; i++) {
					StyleRange newStyle = newStyles[i], style; 
					int newStart = newStyle.start;
					int newEnd = newStart + newStyle.length;
					if (newStart == newEnd) continue;
					int modifyLast = 0, mergeCount = 0;
					while (modifyEnd < styleCount) {
						if (newStart >= styles[modifyStart].start + styles[modifyStart].length) modifyStart++;
						if (styles[modifyEnd].start + styles[modifyEnd].length > newEnd) break;
						modifyEnd++;
					}
					style = styles[modifyStart];
					if (style.start < newStart && newStart < style.start + style.length) {
						style = mergeStyles[mergeCount++] = (StyleRange)style.clone();
						style.length = newStart - style.start;
					}
					mergeStyles[mergeCount++] = newStyle;
					if (modifyEnd < styleCount) {
						style = styles[modifyEnd];
						if (style.start < newEnd && newEnd < style.start + style.length) {
							style = mergeStyles[mergeCount++] = (StyleRange)style.clone();
							style.length += style.start - newEnd;
							style.start = newEnd;
							modifyLast = 1;
						}
					}
					int grow = addMerge(mergeStyles, mergeCount, modifyStart, modifyEnd + modifyLast);
					modifyStart = modifyEnd += grow;
				}
			}
		}
		
		int[] getRanges(int start, int length) {
			if (length == 0) return null;
			int[] newRanges;
			int end = start + length - 1;
			if (ranges != null) {
				int rangeCount = styleCount << 1;
				int rangeStart = getRangeIndex(start, -1, rangeCount);
				if (rangeStart >= rangeCount) return null;
				if (ranges[rangeStart] > end) return null;
				int rangeEnd = Math.min(rangeCount - 2, getRangeIndex(end, rangeStart - 1, rangeCount));
				if (ranges[rangeEnd] > end) rangeEnd = Math.max(rangeStart, rangeEnd - 2);
				newRanges = new int[rangeEnd - rangeStart + 2];
				System.arraycopy(ranges, rangeStart, newRanges, 0, newRanges.length);
			} else {
				int rangeStart = getRangeIndex(start, -1, styleCount);
				if (rangeStart >= styleCount) return null;
				if (styles[rangeStart].start > end) return null;
				int rangeEnd = Math.min(styleCount - 1, getRangeIndex(end, rangeStart - 1, styleCount));
				if (styles[rangeEnd].start > end) rangeEnd = Math.max(rangeStart, rangeEnd - 1);
				newRanges = new int[(rangeEnd - rangeStart + 1) << 1];
				for (int i = rangeStart, j = 0; i <= rangeEnd; i++, j += 2) {
					StyleRange style = styles[i];
					newRanges[j] = style.start;
					newRanges[j + 1] = style.length;
				}
			}
			if (start > newRanges[0]) {
				newRanges[1] = newRanges[0] + newRanges[1] - start;
				newRanges[0] = start;
			}
			if (end < newRanges[newRanges.length - 2] + newRanges[newRanges.length - 1] - 1) {
				newRanges[newRanges.length - 1] = end - newRanges[newRanges.length - 2] + 1;
			}
			return newRanges;
		}
		
		int getRangeIndex(int offset, int low, int high) {
			if (styleCount == 0) return 0;
			if (ranges != null)  {
				while (high - low > 2) {
					int index = ((high + low) / 2) / 2 * 2;
					int end = ranges[index] + ranges[index + 1];
					if (end > offset) {
						high = index;
					} else {
						low = index;
					}
				}
			} else {
				while (high - low > 1) {
					int index = ((high + low) / 2);
					int end = styles[index].start + styles[index].length;
					if (end > offset) {
						high = index;
					} else {
						low = index;
					}
				}
			}
			return high;
		}
		
		int addMerge(int[] mergeRanges, StyleRange[] mergeStyles, int mergeCount, int modifyStart, int modifyEnd) {
			int rangeCount = styleCount << 1;
			StyleRange endStyle = null;
			int endStart = 0, endLength = 0;
			if (modifyEnd < rangeCount) {
				endStyle = styles[modifyEnd >> 1];
				endStart = ranges[modifyEnd];
				endLength = ranges[modifyEnd + 1];
			}
			int grow = mergeCount - (modifyEnd - modifyStart);
			if (rangeCount + grow >= ranges.length) {
				int[] tmpRanges = new int[ranges.length + grow + (GROW << 1)];
				System.arraycopy(ranges, 0, tmpRanges, 0, modifyStart);
				StyleRange[] tmpStyles = new StyleRange[styles.length + (grow >> 1) + GROW];
				System.arraycopy(styles, 0, tmpStyles, 0, modifyStart >> 1);
				if (rangeCount > modifyEnd) {
					System.arraycopy(ranges, modifyEnd, tmpRanges, modifyStart + mergeCount, rangeCount - modifyEnd);
					System.arraycopy(styles, modifyEnd >> 1, tmpStyles, (modifyStart + mergeCount) >> 1, styleCount - (modifyEnd >> 1));
				}
				ranges = tmpRanges;
				styles = tmpStyles;
			} else {
				if (rangeCount > modifyEnd) {
					System.arraycopy(ranges, modifyEnd, ranges, modifyStart + mergeCount, rangeCount - modifyEnd);
					System.arraycopy(styles, modifyEnd >> 1, styles, (modifyStart + mergeCount) >> 1, styleCount - (modifyEnd >> 1));
				}
			}
			if (MERGE_STYLES) {
				int j = modifyStart;	
				for (int i = 0; i < mergeCount; i += 2) {
					if (j > 0 && ranges[j - 2] + ranges[j - 1] == mergeRanges[i] && mergeStyles[i >> 1].similarTo(styles[(j - 2) >> 1])) {
						ranges[j - 1] += mergeRanges[i + 1];
					} else {
						styles[j >> 1] = mergeStyles[i >> 1];
						ranges[j++] = mergeRanges[i];
						ranges[j++] = mergeRanges[i + 1];
					}
				}
				if (endStyle != null && ranges[j - 2] + ranges[j - 1] == endStart && endStyle.similarTo(styles[(j - 2) >> 1])) {
					ranges[j - 1] += endLength;
					modifyEnd += 2;
					mergeCount += 2;
				}
				if (rangeCount > modifyEnd) {
					System.arraycopy(ranges, modifyStart + mergeCount, ranges, j, rangeCount - modifyEnd);
					System.arraycopy(styles, (modifyStart + mergeCount) >> 1, styles, j >> 1, styleCount - (modifyEnd >> 1));
				}
				grow = (j - modifyStart) - (modifyEnd - modifyStart);
			} else {
				System.arraycopy(mergeRanges, 0, ranges, modifyStart, mergeCount);
				System.arraycopy(mergeStyles, 0, styles, modifyStart >> 1, mergeCount >> 1);
			}
			styleCount += grow >> 1;
			return grow;
		}
		int addMerge(StyleRange[] mergeStyles, int mergeCount, int modifyStart, int modifyEnd) {
			int grow = mergeCount - (modifyEnd - modifyStart);
			StyleRange endStyle = null;
			if (modifyEnd < styleCount) endStyle = styles[modifyEnd];
			if (styleCount + grow >= styles.length) {
				StyleRange[] tmpStyles = new StyleRange[styles.length + grow + GROW];
				System.arraycopy(styles, 0, tmpStyles, 0, modifyStart);
				if (styleCount > modifyEnd) {
					System.arraycopy(styles, modifyEnd, tmpStyles, modifyStart + mergeCount, styleCount - modifyEnd);
				}
				styles = tmpStyles;
			} else {
				if (styleCount > modifyEnd) {
					System.arraycopy(styles, modifyEnd, styles, modifyStart + mergeCount, styleCount - modifyEnd);
				}
			}
			if (MERGE_STYLES) {
				int j = modifyStart;
				for (int i = 0; i < mergeCount; i++) {
					StyleRange newStyle = mergeStyles[i], style;
					if (j > 0 && (style = styles[j - 1]).start + style.length == newStyle.start && newStyle.similarTo(style)) {
						style.length += newStyle.length;
					} else {
						styles[j++] = newStyle;
					}
				}
				StyleRange style = styles[j - 1];
				if (endStyle != null && style.start + style.length == endStyle.start && endStyle.similarTo(style)) {
					style.length += endStyle.length;
					modifyEnd++;
					mergeCount++;
				}
				if (styleCount > modifyEnd) {
					System.arraycopy(styles, modifyStart + mergeCount, styles, j, styleCount - modifyEnd);
				}
				grow = (j - modifyStart) - (modifyEnd - modifyStart);
			} else {
				System.arraycopy(mergeStyles, 0, styles, modifyStart, mergeCount);
			}
			styleCount += grow;
			return grow;
		}
		
		void updateRanges(int start, int replaceCharCount, int newCharCount) {
			if (styleCount == 0 || (replaceCharCount == 0 && newCharCount == 0)) return;
			if (ranges != null) {
				int rangeCount = styleCount << 1;
				int modifyStart = getRangeIndex(start, -1, rangeCount);
				if (modifyStart == rangeCount) return;
				int end = start + replaceCharCount;
				int modifyEnd = getRangeIndex(end, modifyStart - 1, rangeCount);
				int offset = newCharCount - replaceCharCount;
				if (modifyStart == modifyEnd && ranges[modifyStart] < start && end < ranges[modifyEnd] + ranges[modifyEnd + 1]) {
					if (newCharCount == 0) {
						ranges[modifyStart + 1] -= replaceCharCount;
						modifyEnd += 2;
					} else {
						if (rangeCount + 2 > ranges.length) {
							int[] newRanges = new int[ranges.length + (GROW << 1)];
							System.arraycopy(ranges, 0, newRanges, 0, rangeCount);
							ranges = newRanges;
							StyleRange[] newStyles = new StyleRange[styles.length + GROW];
							System.arraycopy(styles, 0, newStyles, 0, styleCount);
							styles = newStyles;
						}
						System.arraycopy(ranges, modifyStart + 2, ranges, modifyStart + 4, rangeCount - (modifyStart + 2));
						System.arraycopy(styles, (modifyStart + 2) >> 1, styles, (modifyStart + 4) >> 1, styleCount - ((modifyStart + 2) >> 1));
						ranges[modifyStart + 3] = ranges[modifyStart] + ranges[modifyStart + 1] - end;
						ranges[modifyStart + 2] = start + newCharCount;
						ranges[modifyStart + 1] = start - ranges[modifyStart];
						styles[(modifyStart >> 1) + 1] = styles[modifyStart >> 1]; 
						rangeCount += 2;
						styleCount++;
						modifyEnd += 4;
					}
					if (offset != 0) {
						for (int i = modifyEnd; i < rangeCount; i += 2) {
							ranges[i] += offset;
						}
					}
				} else {
					if (ranges[modifyStart] < start && start < ranges[modifyStart] + ranges[modifyStart + 1]) {
						ranges[modifyStart + 1] = start - ranges[modifyStart];
						modifyStart += 2;
					}
					if (modifyEnd < rangeCount && ranges[modifyEnd] < end && end < ranges[modifyEnd] + ranges[modifyEnd + 1]) {
						ranges[modifyEnd + 1] = ranges[modifyEnd] + ranges[modifyEnd + 1] - end;
						ranges[modifyEnd] = end;
					}
					if (offset != 0) {
						for (int i = modifyEnd; i < rangeCount; i += 2) {
							ranges[i] += offset;
						}
					}
					System.arraycopy(ranges, modifyEnd, ranges, modifyStart, rangeCount - modifyEnd);
					System.arraycopy(styles, modifyEnd >> 1, styles, modifyStart >> 1, styleCount - (modifyEnd >> 1));
					styleCount -= (modifyEnd - modifyStart) >> 1;
				}
			} else {
				int modifyStart = getRangeIndex(start, -1, styleCount);
				if (modifyStart == styleCount) return;
				int end = start + replaceCharCount;
				int modifyEnd = getRangeIndex(end, modifyStart - 1, styleCount);
				int offset = newCharCount - replaceCharCount;
				if (modifyStart == modifyEnd && styles[modifyStart].start < start && end < styles[modifyEnd].start + styles[modifyEnd].length) {
					if (newCharCount == 0) {
						styles[modifyStart].length -= replaceCharCount;
						modifyEnd++;
					} else {
						if (styleCount + 1 > styles.length) {
							StyleRange[] newStyles = new StyleRange[styles.length + GROW];
							System.arraycopy(styles, 0, newStyles, 0, styleCount);
							styles = newStyles;
						}
						System.arraycopy(styles, modifyStart + 1, styles, modifyStart + 2, styleCount - (modifyStart + 1));
						styles[modifyStart + 1] = (StyleRange)styles[modifyStart].clone();
						styles[modifyStart + 1].length = styles[modifyStart].start + styles[modifyStart].length - end;
						styles[modifyStart + 1].start = start + newCharCount;
						styles[modifyStart].length = start - styles[modifyStart].start;
						styleCount++;
						modifyEnd += 2;
					}
					if (offset != 0) {
						for (int i = modifyEnd; i < styleCount; i++) {
							styles[i].start += offset;
						}
					}
				} else {
					if (styles[modifyStart].start < start && start < styles[modifyStart].start + styles[modifyStart].length) {
						styles[modifyStart].length = start - styles[modifyStart].start;
						modifyStart++;
					}
					if (modifyEnd < styleCount && styles[modifyEnd].start < end && end < styles[modifyEnd].start + styles[modifyEnd].length) {
						styles[modifyEnd].length = styles[modifyEnd].start + styles[modifyEnd].length - end;
						styles[modifyEnd].start = end;
					}
					if (offset != 0) {
						for (int i = modifyEnd; i < styleCount; i++) {
							styles[i].start += offset;
						}
					}
					System.arraycopy(styles, modifyEnd, styles, modifyStart, styleCount - modifyEnd);
					styleCount -= modifyEnd - modifyStart;
				}
			}
		}
		
		StyleRange[] getStyleRanges(int start, int length, boolean includeRanges) {
			if (length == 0) return null;
			StyleRange[] newStyles;
			int end = start + length - 1;
			if (ranges != null) {
				int rangeCount = styleCount << 1;
				int rangeStart = getRangeIndex(start, -1, rangeCount);
				if (rangeStart >= rangeCount) return null;
				if (ranges[rangeStart] > end) return null;
				int rangeEnd = Math.min(rangeCount - 2, getRangeIndex(end, rangeStart - 1, rangeCount));
				if (ranges[rangeEnd] > end) rangeEnd = Math.max(rangeStart, rangeEnd - 2);
				newStyles = new StyleRange[((rangeEnd - rangeStart) >> 1) + 1];
				if (includeRanges) {
					for (int i = rangeStart, j = 0; i <= rangeEnd; i += 2, j++) {
						newStyles[j] = (StyleRange)styles[i >> 1].clone();
						newStyles[j].start = ranges[i];
						newStyles[j].length = ranges[i + 1];
					}
				} else {
					System.arraycopy(styles, rangeStart >> 1, newStyles, 0, newStyles.length);
				}
			} else {
				int rangeStart = getRangeIndex(start, -1, styleCount);
				if (rangeStart >= styleCount) return null;
				if (styles[rangeStart].start > end) return null;
				int rangeEnd = Math.min(styleCount - 1, getRangeIndex(end, rangeStart - 1, styleCount));
				if (styles[rangeEnd].start > end) rangeEnd = Math.max(rangeStart, rangeEnd - 1);
				newStyles = new StyleRange[rangeEnd - rangeStart + 1];
				System.arraycopy(styles, rangeStart, newStyles, 0, newStyles.length);
			}
			if (includeRanges || ranges == null) {
				StyleRange style = newStyles[0];
				if (start > style.start) {
					newStyles[0] = style = (StyleRange)style.clone();
					style.length = style.start + style.length - start;
					style.start = start;
				}
				style = newStyles[newStyles.length - 1];
				if (end < style.start + style.length - 1) {
					newStyles[newStyles.length - 1] = style = (StyleRange)style.clone();
					style.length = end - style.start + 1;
				}
			}
			return newStyles;
		}
	}
	
	/*******************************************************************************
	 * Copyright (c) 2000, 2011 IBM Corporation and others.
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html
	 *
	 * Contributors:
	 *     IBM Corporation - initial API and implementation
	 *******************************************************************************/
	static class DefaultContent implements StyledTextContent {

		private char[] textStore;
		private int lineCount;
		private int[][] lines = new int[50][2];
		private int expandExp = 1;

		@Override
		public void setText(String text) {
			textStore = text.toCharArray();
			indexLines();
		}
		
		public int getCharCount() {
//			int length = gapEnd - gapStart;
//			return (textStore.length - length);
			return textStore.length;
		}

		public String getTextRange(int start, int length) {
			return new String(textStore,start,length);
		}
		
		public String getLine(int index) {
			int start = lines[index][0];
			int length = lines[index][1];
			
			return new String(textStore,start,length);
		}
		
		public int getLineCount(){
			return lineCount;
		}
		
		public int getOffsetAtLine(int lineIndex) {
			int start = lines[lineIndex][0];
			return start;
		}
		
		void indexLines(){
			int start = 0;
			lineCount = 0;
			int textLength = textStore.length;
			int i;
			for (i = start; i < textLength; i++) {
				char ch = textStore[i];					
				if (ch == '\r') {
					// see if the next character is a LF
					if (i + 1 < textLength) {
						ch = textStore[i+1];
						if (ch == '\n') {
							i++;
						}
					}
					addLineIndex(start, i - start + 1);
					start = i + 1;
				} else if (ch == '\n') {
					addLineIndex(start, i - start + 1);
					start = i + 1;
				}
			}
			addLineIndex(start, i - start);
		}
		
		void addLineIndex(int start, int length) {
			int size = lines.length;
			if (lineCount == size) {
				// expand the lines by powers of 2
				int[][] newLines = new int[size+(int)Math.pow(2,expandExp)][2];
				System.arraycopy(lines, 0, newLines, 0, size);
				lines = newLines;
				expandExp++;
			}
			int[] range = new int[] {start, length};
			lines[lineCount] = range;
			lineCount++;
		}
	}
}
