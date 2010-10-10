package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.figuredefn.IFigureController;

import uk.ac.ed.inf.graph.compound.ICompoundNode;

public interface IShapeController extends INodeController {

	IFigureController getFigureController();
	
	@Override
	ICompoundNode getDrawingElement();
}
