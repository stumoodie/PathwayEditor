package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface ISelectionStateBehaviourController extends IMouseStateBehaviourController {

	ISelectionResponse getClickResponse();

	IDragResponse getDragResponse(SelectionHandleType type);

	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);

	ISelectionHandle getSelectionHandle(Point location);

	IPopupMenuResponse getPopupMenuResponse(SelectionHandleType popupSelectionHandle);

	void showPopupMenus(JPopupMenu popup, int x, int y);
}
