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
import org.pathwayeditor.visualeditor.controller.INodeController;

public class CentralSelectionHandle extends SelectionHandle implements ICentralSelectionHandleShape {
	
	public CentralSelectionHandle(ISelection selection, INodeController nodeController){
		super(nodeController, SelectionHandleType.Central, selection);
	}
	
	
	@Override
	public boolean containsPoint(Point point) {
		return this.getDrawingPrimitiveController().containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.getDrawingPrimitiveController().getDrawnBounds();
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public void translate(Point delta) {
		// do nothing as this uses the node controller which will also be moved
	}


	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}
}
