package org.pathwayeditor.visualeditor.behaviour;

import java.util.Iterator;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface IControllerResponses {

	IDragResponse getDragResponse(ISelectionHandle handle);

	IMouseFeedbackResponse getFeedbackResponse(ISelectionHandle type);

	ISelectionResponse getSelectionResponse();

	IKeyboardResponse getKeyboardResponse();

	IPopupMenuResponse getPopupMenuResponse(ISelectionHandle popupSelectionHandle);

	Iterator<IPopupMenuResponse> popResponseIterator();
	
	int numPopupResponses();
}
