package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	void setMousePosition(double x, double y);
	
	ISelectionResponse getClickResponse();

	IDragResponse getDragResponse();

	IMouseFeedbackResponse getMouseFeedbackResponse();

//	ISelectionHandle getSelectionHandle();

	IPopupMenuResponse getPopupMenuResponse();

	void showPopupMenus(JPopupMenu popup);

	Point getDiagramLocation();
	
	INodeController getNodeAtCurrentPoint();
}
