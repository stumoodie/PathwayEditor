package org.pathwayeditor.visualeditor.behaviour.creation;

import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;

public interface ILinkCreationDragResponse extends IDragResponse {

	void setCurrentNode(IDrawingElementController drawingPrimitiveController);
	
}
