package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.graphicsengine.ILinkPointDefinition;

public interface ILinkPrimitive extends IDrawingPrimitive {

	ILinkPointDefinition getLinkDefinition();
	
	@Override
	ILinkEdge getDrawingElement();
	
}
