/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.efxclipse.runtime.workbench.renderers.fx;

import javax.inject.Inject;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import at.bestsolution.efxclipse.runtime.workbench.renderers.base.BasePartRenderer;
import at.bestsolution.efxclipse.runtime.workbench.renderers.base.widget.WPart;
import at.bestsolution.efxclipse.runtime.workbench.renderers.fx.widget.WLayoutedWidgetImpl;

@SuppressWarnings("restriction")
public class DefPartRenderer extends BasePartRenderer<BorderPane> {

	@Override
	protected Class<? extends WPart<BorderPane>> getWidgetClass(MPart part) {
		return PartImpl.class;
	}

	public static class PartImpl extends WLayoutedWidgetImpl<BorderPane, BorderPane, MPart> implements WPart<BorderPane> {
		@Inject
		EPartService service;
		
		@Override
		protected BorderPane createWidget() {
			BorderPane p = new BorderPane();
			p.setOnMouseReleased(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					service.activate(getDomElement(),true);
				}
			});
			return p;
		}

		@Override
		protected BorderPane getWidgetNode() {
			return getWidget();
		}
	}
}
