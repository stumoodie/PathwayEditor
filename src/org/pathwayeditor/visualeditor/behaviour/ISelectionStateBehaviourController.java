package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	ISelectionRecord getSelectionRecord();

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);
}
