/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkSelectionHandle extends SelectionHandle implements ILinkSelectionHandleShape {
	private final ILinkPointDefinition linkPointDefinition;

	public LinkSelectionHandle(LinkSelection linkSelection, ILinkController controller) {
		super(controller, SelectionHandleType.Link, linkSelection);
		this.linkPointDefinition = controller.getLinkDefinition();
	}

	@Override
	public boolean containsPoint(Point point) {
		return this.linkPointDefinition.containsPoint(point);
	}

	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}

	@Override
	public Envelope getBounds() {
		return this.linkPointDefinition.getBounds();
	}

	@Override
	public void translate(Point delta) {
		this.linkPointDefinition.translate(delta);
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return ((ILinkController)this.getDrawingPrimitiveController()).getLinkDefinition();
	}

}
