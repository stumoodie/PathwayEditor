package org.pathwayeditor.visualeditor.behaviour.selection;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation.ReparentingStateType;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse;

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
	
}
