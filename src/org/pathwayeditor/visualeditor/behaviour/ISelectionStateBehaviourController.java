package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	ISelectionResponse getSelectionResponse(SelectionHandleType type);

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);

	ISelectionHandle getSelectionHandle(Point location);
}
