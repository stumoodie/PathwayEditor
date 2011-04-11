package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;

public interface IMouseFeedbackResponse {

	enum StateType { DEFAULT, REPARENTING, FORBIDDEN };
	
	void changeState(StateType newState);

	StateType getCurrentState();
	
	Cursor getCurrentCursor();

	void reset();

	void altSelected(boolean isAltSelected);
}
