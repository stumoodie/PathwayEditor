package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	ISelectionResponse getClickResponse();

	IDragResponse getDragResponse();

	IMouseFeedbackResponse getMouseFeedbackResponse();

//	ISelectionHandle getSelectionHandle();

	IPopupMenuResponse getPopupMenuResponse();

	void showPopupMenus(JPopupMenu popup);

//	INodeController getNodeAtCurrentPoint();
}
