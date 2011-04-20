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

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class ResizeHandleResponse extends HandleResponse implements ISelectionDragResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final INewPositionCalculator newPositionCalculator;
	private final IResizeOperation operation;
	
	public ResizeHandleResponse(INewPositionCalculator newPositionCalculator, IResizeOperation resizeOperation){
		super();
		this.newPositionCalculator = newPositionCalculator;
		this.operation = resizeOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		Point delta = this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		if(logger.isTraceEnabled()){
			logger.trace("Drag continuing. newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeContinuing(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		if(logger.isTraceEnabled()){
			logger.trace("Drag finished. originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeFinished(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragStarted(Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		Point delta = calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		if(logger.isTraceEnabled()){
			logger.trace("Drag started. newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeStarted();
	}

	@Override
	public boolean canContinueDrag(Point newLocation) {
		// The problem is we don;t want to change the state of the class here. So we must undo
		// these changes before the method returns.
		Point lastDelta = this.newPositionCalculator.getLastDelta(); 
		Point delta = this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		boolean retVal = this.operation.canResize(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
		if(logger.isTraceEnabled()){
			logger.trace("Can continue drag? retVal=" + retVal + ",newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.newPositionCalculator.calculateDeltas(lastDelta);
		return retVal;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public boolean canOperationSucceed() {
		return true;
	}

	@Override
	public void setSelectionHandle(ISelectionHandle selectionHandle) {
	}

	@Override
	protected void handleAltSelection(boolean isSelected) {
	}

}
