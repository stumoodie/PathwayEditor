package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;

import org.pathwayeditor.figure.geometry.Point;

public class DefaultMouseFeedbackResponse implements IMouseFeedbackResponse {

	@Override
	public int getCursorFeeback(Point location) {
		return Cursor.DEFAULT_CURSOR;
	}

}
