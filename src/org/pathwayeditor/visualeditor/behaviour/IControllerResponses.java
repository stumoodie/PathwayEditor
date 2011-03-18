package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface IControllerResponses {

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getFeedbackResponse(SelectionHandleType type);

	ISelectionResponse getSelectionResponse();

}
