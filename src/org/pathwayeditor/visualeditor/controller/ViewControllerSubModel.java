package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;

public class ViewControllerSubModel implements IViewControllerSubModel {

	public ViewControllerSubModel(IViewControllerModel viewModel, DrawingElementController drawingPrimitiveController) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Iterator<IDrawingNode> childNodeIterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public IDrawingNode getParentNode() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public IViewControllerModel getViewControllerModel() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public int getLevel() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
		
	}

	@Override
	public int compareTo(IViewControllerSubModel o) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
		
	}

}
