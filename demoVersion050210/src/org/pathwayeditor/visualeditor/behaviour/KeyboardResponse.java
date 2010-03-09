package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation.ReparentingStateType;

public class KeyboardResponse implements IKeyboardResponse {
	private static final double MIN_DELTA = 1.0;
	private IEditingOperation editingOperation;
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
	}

	@Override
	public void cursorKeyStillDown(CursorType cursorKeyType) {
		Point delta = lastDelta.translate(calcDelta());
		this.editingOperation.moveOngoing(delta);
		lastDelta = delta;
		this.currentCursorKey = cursorKeyType;
		
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
		return retVal;
	}

	@Override
	public void cursorsKeyUp() {
		this.currentCursorKey = CursorType.None;
		this.editingOperation.moveFinished(lastDelta, ReparentingStateType.CAN_MOVE);
		this.keyDown = false;
	}

	@Override
	public CursorType getCurrentCursorKey() {
		return this.currentCursorKey;
	}

	@Override
	public boolean isKeyPressed() {
		return this.keyDown;
	}
	
}
