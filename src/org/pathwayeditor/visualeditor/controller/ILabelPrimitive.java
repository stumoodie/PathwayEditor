package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.figure.figuredefn.IFigureController;

public interface ILabelPrimitive extends INodePrimitive {

	IFigureController getFigureController();
	
	@Override
	ILabelNode getDrawingElement();
	
}
