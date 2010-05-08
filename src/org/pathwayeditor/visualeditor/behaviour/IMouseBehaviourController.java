package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;

public interface IMouseBehaviourController {

	void activate();

	boolean isActivated();
	
	void deactivate();

//	Point getAdjustedMousePosition(double x, double y);
//
//	ISelectionRecord getSelectionRecord();

//	IDrawingPrimitiveController findDrawingElementAt(Point location);

//	IDragResponse getDragResponse(SelectionHandleType type);

//	IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type);

	void updateView();

	void setShapeCreationMode(IShapeObjectType shapeType);

	void setLinkCreationMode(ILinkObjectType linkType);

	void setSelectionMode();
	
}
