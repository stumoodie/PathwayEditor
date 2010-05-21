package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	ISelectionRecord getSelectionRecord();

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);
}
