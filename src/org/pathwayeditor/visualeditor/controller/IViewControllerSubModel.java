package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;

public interface IViewControllerSubModel extends Comparable<IViewControllerSubModel> {

	IViewControllerModel getViewControllerModel();
	
	IDrawingNode getParentNode();
	
	Iterator<IDrawingNode> childNodeIterator();

	int getLevel();
	
}
