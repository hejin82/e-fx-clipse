/******************************************************************************* 
 * Copyright (c) 2012 TESIS DYNAware GmbH and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Torsten Sommer <torsten.sommer@tesis.de> - initial API and implementation 
 *******************************************************************************/
package at.bestsolution.efxclipse.runtime.emf.edit.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.provider.IItemColorProvider;
import org.eclipse.emf.edit.provider.IItemFontProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

/**
 * Base class for the AdapterFactoryCellFactories
 */
public abstract class AdapterFactoryCellFactory {

	/**
	 * A listener interface for callbacks that process newly created
	 * {@link Cell}s
	 */
	public interface ICellCreationListener {

		/**
		 * @param cell the newly created {@link Cell}
		 */
		void cellCreated(Cell<?> cell);

	}

	/**
	 * A listener interface for callbacks that process {@link Cell}s being
	 * updated during {@link Cell#updateItem}.
	 */
	public interface ICellUpdateListener {

		/**
		 * @param cell the {@link Cell} being updated
		 * @param item as defined in {@link Cell#updateItem}
		 * @param empty as defined in {@link Cell#updateItem}
		 */
		void updateItem(Cell<?> cell, Object item, boolean empty);

	}

	final protected AdapterFactory adapterFactory;
	final/* package */List<ICellCreationListener> cellCreationListeners = new ArrayList<>();
	final/* package */List<ICellUpdateListener> cellUpdateListeners = new ArrayList<>();

	public AdapterFactoryCellFactory(AdapterFactory adapterFactory) {
		super();

		if (adapterFactory == null)
			throw new IllegalArgumentException("AdapterFactory must not be null.");

		this.adapterFactory = adapterFactory;
	}

	public AdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public void addCellCreationListener(ICellCreationListener listener) {
		cellCreationListeners.add(listener);
	}

	public void removeCellCreationListener(ICellCreationListener listener) {
		cellCreationListeners.remove(listener);
	}

	public void addCellUpdateListener(ICellUpdateListener listener) {
		cellUpdateListeners.add(listener);
	}

	public void removeCellUpdateListener(ICellUpdateListener listener) {
		cellUpdateListeners.remove(listener);
	}

	public void applyItemProviderStyle(Object item, Cell<?> cell, AdapterFactory adapterFactory) {
		applyItemProviderLabel(item, cell, adapterFactory);
		applyItemProviderColor(item, cell, adapterFactory);
		applyItemProviderFont(item, cell, adapterFactory);
	}

	public void applyItemProviderFont(Object item, Cell<?> cell, AdapterFactory adapterFactory) {
		IItemFontProvider fontProvider = (IItemFontProvider) adapterFactory.adapt(item, IItemFontProvider.class);
		if (fontProvider != null) {
			Font font = fontFromObject(fontProvider.getFont(item));
			if (font != null)
				cell.setFont(font);
		}
	}

	/* package */Font fontFromObject(Object object) {

		if (object instanceof URI) {
			URI fontURI = (URI) object;
			if (!"font".equals(fontURI.scheme()))
				throw new IllegalArgumentException("Only 'font' scheme is recognized" + fontURI);

			if (fontURI.segmentCount() != 2)
				throw new IllegalArgumentException("The URI must have an authority and two segments");

			// font name
			String fontNameSpecification = fontURI.authority();
			if ("".equals(fontNameSpecification))
				fontNameSpecification = null;

			// font height
			String heightSpecification = fontURI.segment(0);
			boolean delta;
			int height;
			if (heightSpecification.startsWith("+")) {
				delta = true;
				height = Integer.parseInt(heightSpecification.substring(1));
			} else if ("".equals(heightSpecification)) {
				delta = true;
				height = 0;
			} else {
				height = Integer.parseInt(heightSpecification);
				delta = height < 0;
			}

			// font style
			String styleSpecification = fontURI.segment(1);
			boolean italic = false;
			boolean bold = false;
			if ("bold".equals(styleSpecification))
				bold = true;
			else if ("italic".equals(styleSpecification))
				italic = true;
			else if ("italic+bold".equals(styleSpecification) || "bold+italic".equals(styleSpecification))
				bold = italic = true;

			double size = delta ? Font.getDefault().getSize() + height : height;

			return Font.font(fontNameSpecification, bold ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC
					: FontPosture.REGULAR, size);
		}

		return null;
	}

	/* package */void applyItemProviderLabel(Object item, Cell<?> cell, AdapterFactory adapterFactory) {
		IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(item, IItemLabelProvider.class);

		if (labelProvider != null) {
			cell.setText(labelProvider.getText(item));

			if (item != null) {
				Object image = labelProvider.getImage(item);
				if (image instanceof URL) {
					Node graphic = new ImageView(((URL) image).toExternalForm());
					cell.setGraphic(graphic);
				}
			}
		}
	}

	/* package */void applyItemProviderColor(Object item, Cell<?> cell, AdapterFactory adapterFactory) {
		IItemColorProvider colorProvider = (IItemColorProvider) adapterFactory.adapt(item, IItemColorProvider.class);
		if (colorProvider != null) {
			Color foreground = colorFromObject(colorProvider.getForeground(item));
			if (foreground != null)
				cell.setTextFill(foreground);

			String background = cssColorFromObject(colorProvider.getBackground(item));
			if (background != null)
				cell.setStyle("-fx-background-color: " + background);
		}
	}

	/* package */Color colorFromObject(Object object) {
		if (object instanceof URI) {
			URI colorURI = (URI) object;

			if (!"color".equals(colorURI.scheme()))
				throw new IllegalArgumentException("Only 'color' scheme is recognized " + colorURI);

			if (!"rgb".equals(colorURI.authority())) {
				throw new IllegalArgumentException("Only 'rgb' authority is recognized " + colorURI);
			} else {
				if (colorURI.segmentCount() != 3)
					throw new IllegalArgumentException("Color must have 3 segments (r, g, b) " + colorURI);

				try {
					int red = Integer.parseInt(colorURI.segment(0));
					int green = Integer.parseInt(colorURI.segment(1));
					int blue = Integer.parseInt(colorURI.segment(2));
					return Color.rgb(red, green, blue);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Cannot parse color values " + colorURI + ". " + e.getMessage());
				}

			}
		}

		return null;
	}

	/* package */String cssColorFromObject(Object object) {
		if (object instanceof URI) {
			URI colorURI = (URI) object;

			if (!"color".equals(colorURI.scheme()))
				throw new IllegalArgumentException("Only 'color' scheme is recognized " + colorURI);

			if (!"rgb".equals(colorURI.authority()))
				throw new IllegalArgumentException("Only 'rgb' authority is recognized " + colorURI);

			if (colorURI.segmentCount() != 3)
				throw new IllegalArgumentException("Color must have 3 segments (r, g, b) " + colorURI);

			return String.format("rgb(" + colorURI.segment(0) + ", " + colorURI.segment(1) + ", " + colorURI.segment(2) + ")");
		}

		return null;
	}

}
