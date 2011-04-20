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
package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;


public abstract class HandleResponse implements IDragResponse {
	private boolean altSelected = false;
	private boolean cmdSelected = false;
	private boolean shiftSelected = false;
	private boolean isDragOngoing = false;
	private Point startLocation;
	
	protected HandleResponse(){
	}
	
	protected final Point getStartLocation(){
		return this.startLocation;
	}
	
	protected final void setStartLocation(Point startLocation){
		this.startLocation  = startLocation;
	}
	
	protected final Point calculateLocationDelta(Point newLocation){
		return this.startLocation.difference(newLocation);
	}
	
	protected final boolean isAltSelected() {
		return altSelected;
	}

	protected final boolean isCmdSelected() {
		return cmdSelected;
	}

	protected final boolean isShiftSelected() {
		return shiftSelected;
	}
	
	protected final void enterDragOngoingState(){
		this.isDragOngoing = true;
	}
	
	protected final void exitDragOngoingState(){
		this.isDragOngoing = false;
	}
	
	@Override
	public final boolean isDragOngoing(){
		return this.isDragOngoing;
	}

	@Override
	public final void altSelected(boolean isSelected) {
		this.altSelected = isSelected;
		handleAltSelection(isSelected);
	}

	protected abstract void handleAltSelection(boolean isSelected);

	@Override
	public final void cmdSelected(boolean isSelected) {
		this.cmdSelected = isSelected;
	}

	@Override
	public final void shiftSelected(boolean isSelected) {
		this.shiftSelected = isSelected;
	}
}
