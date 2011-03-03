package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.figuredefn.IFigureRenderingController;

public interface IShapeController extends INodeController {

	IFigureRenderingController getFigureController();
	
	@Override
	IShapeNode getDrawingElement();
}
