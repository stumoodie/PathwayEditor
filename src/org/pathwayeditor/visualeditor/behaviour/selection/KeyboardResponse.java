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
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation.ReparentingStateType;

public class KeyboardResponse implements IKeyboardResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final double MIN_DELTA = 1.0;
	private final IEditingOperation editingOperation;
	private Point lastDelta;
	private CursorType currentCursorKey;
	private boolean keyDown = false;
	
	public KeyboardResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
		this.lastDelta = Point.ORIGIN;
		this.currentCursorKey = CursorType.None;
	}

	@Override
	public void cursorKeyDown(CursorType cursorKeyType) {
		this.lastDelta = Point.ORIGIN;
		this.keyDown = true;
		this.editingOperation.moveStarted();
		this.cursorKeyStillDown(cursorKeyType);
		if(logger.isTraceEnabled()){
			logger.trace("Initial key down. lastDelta = " + this.lastDelta);
		}
	}

	@Override
	public void cursorKeyStillDown(CursorType cursorKeyType) {
		this.currentCursorKey = cursorKeyType;
		Point delta = lastDelta.translate(calcDelta());
		this.editingOperation.moveOngoing(delta);
		lastDelta = delta;
		if(logger.isTraceEnabled()){
			logger.trace("Key still down. lastDelta = " + this.lastDelta);
		}
	}

	private Point calcDelta() {
		Point retVal = Point.ORIGIN;
		if(this.currentCursorKey == CursorType.Down){
			retVal = new Point(0, MIN_DELTA);
		}
		else if(this.currentCursorKey == CursorType.Up){
			retVal = new Point(0, -MIN_DELTA);
		}
		else if(this.currentCursorKey == CursorType.Left){
			retVal = new Point(-MIN_DELTA, 0.0);
		}
		else if(this.currentCursorKey == CursorType.Right){
			retVal = new Point(MIN_DELTA, 0.0);
		}
		else{
			throw new RuntimeException("Unknown cursor key type");
		}
		return retVal;
	}

	@Override
	public void cursorsKeyUp() {
		this.currentCursorKey = CursorType.None;
		this.editingOperation.moveFinished(lastDelta, ReparentingStateType.CAN_MOVE);
		this.keyDown = false;
		if(logger.isTraceEnabled()){
			logger.trace("Key up. lastDelta = " + this.lastDelta);
		}
	}

	@Override
	public CursorType getCurrentCursorKey() {
		return this.currentCursorKey;
	}

	@Override
	public boolean isKeyPressed() {
		return this.keyDown;
	}

	@Override
	public void deleteKeyDetected() {
		this.editingOperation.deleteSelection();
	}
	
}
