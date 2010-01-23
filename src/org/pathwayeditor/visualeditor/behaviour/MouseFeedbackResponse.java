package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class MouseFeedbackResponse implements IMouseFeedbackResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final SelectionRegion region;
	
	public MouseFeedbackResponse(SelectionRegion region){
		this.region = region;
	}
	
	@Override
	public int getCursorFeeback(Point location) {
		int retVal = Cursor.DEFAULT_CURSOR; 
		if (SelectionRegion.Central.equals(this.region)) {
			retVal = Cursor.MOVE_CURSOR;
		} else if (SelectionRegion.N.equals(this.region)) {
			retVal = Cursor.N_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting N resize cursor at position: " + location);
			}
		} else if (SelectionRegion.NE.equals(this.region)) {
			retVal = Cursor.NE_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NE resize cursor at position: " + location);
			}
		} else if (SelectionRegion.E.equals(this.region)) {
			retVal = Cursor.E_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting E resize cursor at position: " + location);
			}
		} else if (SelectionRegion.SE.equals(this.region)) {
			retVal = Cursor.SE_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SE resize cursor at position: " + location);
			}
		} else if (SelectionRegion.S.equals(this.region)) {
			retVal = Cursor.S_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting S resize cursor at position: " + location);
			}
		} else if (SelectionRegion.SW.equals(this.region)) {
			retVal = Cursor.SW_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting SW resize cursor at position: " + location);
			}
		} else if (SelectionRegion.W.equals(this.region)) {
			retVal = Cursor.W_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting W resize cursor at position: " + location);
			}
		} else if (SelectionRegion.NW.equals(this.region)) {
			retVal = Cursor.NW_RESIZE_CURSOR;
			if (logger.isTraceEnabled()) {
				logger.trace("Setting NW resize cursor at position: " + location);
			}
		}
		return retVal;
	}

}
