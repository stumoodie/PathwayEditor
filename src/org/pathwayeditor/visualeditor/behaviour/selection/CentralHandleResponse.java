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
package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation.ReparentingStateType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class CentralHandleResponse extends HandleResponse implements ISelectionDragResponse {
	private final IEditingOperation editingOperation;
	private ReparentingStateType reparentingState = ReparentingStateType.FORBIDDEN;
	private Point lastDelta;
	
	public CentralHandleResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = this.calculateLocationDelta(newLocation);
		this.editingOperation.moveOngoing(this.lastDelta);
		this.reparentingState = this.editingOperation.getReparentingState(this.lastDelta);
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.editingOperation.moveFinished(this.lastDelta, reparentingState);
	}

	@Override
	public void dragStarted(Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		this.lastDelta = this.calculateLocationDelta(newLocation);
		this.reparentingState = ReparentingStateType.FORBIDDEN;
		this.editingOperation.moveStarted();
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public boolean canReparent() {
		return this.reparentingState.equals(ReparentingStateType.CAN_REPARENT);
	}

	@Override
	public boolean canOperationSucceed() {
		return this.reparentingState.equals(ReparentingStateType.CAN_MOVE);
	}

	@Override
	public void setSelectionHandle(ISelectionHandle selectionHandle) {
	}

	@Override
	protected void handleAltSelection(boolean isSelected) {
		this.editingOperation.setCopyOnMove(isSelected);
	}

}
