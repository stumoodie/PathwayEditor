package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.figure.figuredefn.IFigureController;

public interface ILabelController extends INodeController {

	IFigureController getFigureController();
	
	@Override
	ILabelNode getDrawingElement();
	
}
