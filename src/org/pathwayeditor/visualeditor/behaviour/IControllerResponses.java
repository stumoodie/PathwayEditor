package org.pathwayeditor.visualeditor.behaviour;

import java.util.Iterator;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface IControllerResponses {

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getFeedbackResponse(SelectionHandleType type);

	ISelectionResponse getSelectionResponse();

	IKeyboardResponse getKeyboardResponse();

	IPopupMenuResponse getPopupMenuResponse(SelectionHandleType popupSelectionHandle);

	Iterator<IPopupMenuResponse> popResponseIterator();
	
	int numPopupResponses();
}
