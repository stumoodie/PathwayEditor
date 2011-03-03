package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public interface ILabelController extends INodeController {

	IFigureRenderingController getFigureController();
	
	@Override
	ILabelNode getDrawingElement();
	
}
