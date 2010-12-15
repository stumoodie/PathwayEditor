package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.figuredefn.IFigureController;

public interface IFigureControllerHelper {

	void createFigureController();

	IFigureController getFigureController();
	
	void refreshBoundProperties();

	void refreshGraphicalAttributes();
	
	void refreshAll();
	
}