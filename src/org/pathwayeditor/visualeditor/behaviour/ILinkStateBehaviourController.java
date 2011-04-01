package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.ILinkCreationResponse;

public interface ILinkStateBehaviourController extends IMouseStateBehaviourController {

	void setMousePosition(double x, double y);

	IMouseFeedbackResponse getMouseFeedbackResponse();

	IShapeNode getShapeAtCurrentLocation();

	ILinkCreationResponse getLinkCreationFeedback();

}
