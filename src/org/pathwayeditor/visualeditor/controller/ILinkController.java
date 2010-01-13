package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface ILinkController extends IDrawingPrimitiveController {

	ILinkPointDefinition getLinkDefinition();
	
	@Override
	ILinkEdge getDrawingElement();
	
}
