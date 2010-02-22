package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.figuredefn.IFigureController;

public interface IShapeController extends INodeController {

	IFigureController getFigureController();
	
	@Override
	IShapeAttribute getDrawingElement();
}
