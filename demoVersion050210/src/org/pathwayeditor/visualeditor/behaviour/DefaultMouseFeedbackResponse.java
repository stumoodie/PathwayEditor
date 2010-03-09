package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;

public class DefaultMouseFeedbackResponse implements IMouseFeedbackResponse {
	private StateType currentState = StateType.DEFAULT;

	@Override
	public Cursor getCurrentCursor() {
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}

	@Override
	public void changeState(StateType newState) {
		this.currentState = newState;
	}

	@Override
	public StateType getCurrentState() {
		return this.currentState;
	}

	@Override
	public void reset() {
	}

}
