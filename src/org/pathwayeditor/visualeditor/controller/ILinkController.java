package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.graphicsengine.ILinkPointDefinition;

public interface ILinkController extends IDrawingPrimitiveController {

	ILinkPointDefinition getLinkDefinition();
	
	@Override
	ILinkEdge getDrawingElement();
	
}
