package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.Cursor;
import java.awt.dnd.DragSource;

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class MouseFeedbackResponse implements IMouseFeedbackResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final SelectionHandleType region;
	private StateType currentState;
	private boolean isAltSelected;
	
	public MouseFeedbackResponse(SelectionHandleType region){
		this.region = region;
		this.currentState = StateType.DEFAULT;
	}
	
	@Override
	public Cursor getCurrentCursor() {
		Cursor retVal =  Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR); 
		if (SelectionHandleType.Central.equals(this.region)) {
			if(this.currentState.equals(StateType.DEFAULT)){
				if(this.isAltSelected){
					retVal = DragSource.DefaultCopyDrop;
				}
				else{
					retVal = DragSource.DefaultMoveDrop;
				}
//				retVal = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
			}
			else if(this.currentState.equals(StateType.REPARENTING)){
				if(this.isAltSelected){
					retVal = DragSource.DefaultCopyDrop;
				}
				else{
					retVal = DragSource.DefaultLinkDrop;
//					retVal = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				}
//				retVal = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
			else if(this.currentState.equals(StateType.FORBIDDEN)){
				if(this.isAltSelected){
					retVal = DragSource.DefaultCopyNoDrop;
				}
				else{
					retVal = DragSource.DefaultMoveNoDrop;
//					retVal = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				}
//				retVal = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
			}
		} else if (SelectionHandleType.N.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting N resize cursor");
			}
		} else if (SelectionHandleType.NE.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NE resize cursor");
			}
		} else if (SelectionHandleType.E.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting E resize cursor");
			}
		} else if (SelectionHandleType.SE.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SE resize cursor");
			}
		} else if (SelectionHandleType.S.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting S resize cursor");
			}
		} else if (SelectionHandleType.SW.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SW resize cursor");
			}
		} else if (SelectionHandleType.W.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting W resize cursor");
			}
		} else if (SelectionHandleType.NW.equals(this.region)) {
			retVal = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NW resize cursor");
			}
		}
		else if(SelectionHandleType.LinkMidPoint.equals(this.region)){
			retVal = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting LinkMidPoint cursor");
			}
		}
		else if(SelectionHandleType.LinkBendPoint.equals(this.region)){
			retVal = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			if (logger.isTraceEnabled()) {
				logger.trace("Setting LinkBendPoint cursor");
			}
		}
		return retVal;
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
		this.isAltSelected = false;
	}

	@Override
	public void altSelected(boolean isAltSelected) {
		this.isAltSelected = isAltSelected;
	}

}
