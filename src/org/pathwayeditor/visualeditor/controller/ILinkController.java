package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface ILinkController extends IDrawingElementController {

	ILinkPointDefinition getLinkDefinition();
	
	@Override
	ILinkEdge getDrawingElement();
	
}
