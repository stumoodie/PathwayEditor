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
package org.pathwayeditor.visualeditor.behaviour.creation;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;

public class CreationDragResponse extends HandleResponse {
	private final IShapeCreationOperation shapeCreationOperation;
	private final IShapeTypeInspector shapeTypeInspector;
	private Point lastDelta;

	public CreationDragResponse(IShapeCreationOperation shapeCreationOperation, IShapeTypeInspector shapeTypeInspector) {
		this.shapeCreationOperation = shapeCreationOperation;
		this.shapeTypeInspector = shapeTypeInspector;
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}
	

	@Override
	public void dragStarted(Point startLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(startLocation);
		this.lastDelta = this.calculateLocationDelta(startLocation);
		this.shapeCreationOperation.setShapeObjectType(shapeTypeInspector.getCurrentShapeType());
		this.shapeCreationOperation.startCreationDrag(startLocation);
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = this.calculateLocationDelta(newLocation); 
		this.shapeCreationOperation.ongoingCreationDrag(this.lastDelta);
	}

	@Override
	public void dragFinished() {
		this.shapeCreationOperation.finishCreationDrag(lastDelta);
		this.exitDragOngoingState();
	}

	@Override
	public boolean canReparent() {
		return this.shapeCreationOperation.canCreationSucceed();
	}

	@Override
	public boolean canOperationSucceed() {
		return this.shapeCreationOperation.canCreationSucceed();
	}

	@Override
	protected void handleAltSelection(boolean isSelected) {
	}

}
