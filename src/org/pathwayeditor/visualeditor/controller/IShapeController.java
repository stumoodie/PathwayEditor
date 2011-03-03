package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public interface IShapeController extends INodeController {

	IFigureRenderingController getFigureController();
	
	@Override
	IShapeNode getDrawingElement();
}
