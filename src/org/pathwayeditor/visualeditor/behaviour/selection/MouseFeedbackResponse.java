package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.Cursor;

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class MouseFeedbackResponse implements IMouseFeedbackResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final SelectionHandleType region;
	private StateType currentState;
	
	public MouseFeedbackResponse(SelectionHandleType region){
		this.region = region;
		this.currentState = StateType.DEFAULT;
	}
	
	@Override
	public Cursor getCurrentCursor() {
		int retVal = Cursor.DEFAULT_CURSOR; 
		if (SelectionHandleType.Central.equals(this.region)) {
			if(this.currentState.equals(StateType.DEFAULT)){
				retVal = Cursor.MOVE_CURSOR;
			}
			else if(this.currentState.equals(StateType.REPARENTING)){
				retVal = Cursor.HAND_CURSOR;
			}
			else if(this.currentState.equals(StateType.FORBIDDEN)){
				retVal = Cursor.WAIT_CURSOR;
			}
		} else if (SelectionHandleType.N.equals(this.region)) {
			retVal = Cursor.N_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting N resize cursor");
			}
		} else if (SelectionHandleType.NE.equals(this.region)) {
			retVal = Cursor.NE_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NE resize cursor");
			}
		} else if (SelectionHandleType.E.equals(this.region)) {
			retVal = Cursor.E_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting E resize cursor");
			}
		} else if (SelectionHandleType.SE.equals(this.region)) {
			retVal = Cursor.SE_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SE resize cursor");
			}
		} else if (SelectionHandleType.S.equals(this.region)) {
			retVal = Cursor.S_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting S resize cursor");
			}
		} else if (SelectionHandleType.SW.equals(this.region)) {
			retVal = Cursor.SW_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SW resize cursor");
			}
		} else if (SelectionHandleType.W.equals(this.region)) {
			retVal = Cursor.W_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting W resize cursor");
			}
		} else if (SelectionHandleType.NW.equals(this.region)) {
			retVal = Cursor.NW_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NW resize cursor");
			}
		}
		else if(SelectionHandleType.LinkMidPoint.equals(this.region)){
			retVal = Cursor.CROSSHAIR_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting LinkMidPoint cursor");
			}
		}
		else if(SelectionHandleType.LinkBendPoint.equals(this.region)){
			retVal = Cursor.CROSSHAIR_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting LinkBendPoint cursor");
			}
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

}
