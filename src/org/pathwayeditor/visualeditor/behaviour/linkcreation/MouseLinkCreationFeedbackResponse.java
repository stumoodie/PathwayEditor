package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.Cursor;

import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;

public class MouseLinkCreationFeedbackResponse implements IMouseFeedbackResponse {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private StateType currentState;
	
	public MouseLinkCreationFeedbackResponse(){
		this.currentState = StateType.DEFAULT;
	}
	
	@Override
	public Cursor getCurrentCursor() {
		int retVal = Cursor.DEFAULT_CURSOR; 
		if(this.currentState.equals(StateType.DEFAULT)){
			retVal = Cursor.MOVE_CURSOR;
		}
		else if(this.currentState.equals(StateType.REPARENTING)){
			retVal = Cursor.HAND_CURSOR;
		}
		else if(this.currentState.equals(StateType.FORBIDDEN)){
			retVal = Cursor.WAIT_CURSOR;
		}
		return Cursor.getPredefinedCursor(retVal);
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
		this.currentState = StateType.DEFAULT;
	}

	@Override
	public void altSelected(boolean isAltSelected) {
	}

}
