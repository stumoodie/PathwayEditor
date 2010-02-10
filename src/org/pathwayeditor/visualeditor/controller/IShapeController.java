package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

public interface IShapeController extends INodeController {

	IFigureController getFigureController();
	
	@Override
	IShapeAttribute getDrawingElement();
	
	void setIntersectionCalculator(IIntersectionCalculator nodeIntersectionCalculator);
	
	IIntersectionCalculator getIntersectionCalculator();
}
