package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface IMouseStateBehaviourController {

	void activate();

	boolean isActivated();
	
	void deactivate();

	Point getAdjustedMousePosition(double x, double y);

	ISelectionRecord getSelectionRecord();

	IDrawingPrimitiveController findDrawingElementAt(Point location);

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);

//	void updateView();
}
