package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;

public class ViewControllerSubModel implements IViewControllerSubModel {

	@SuppressWarnings("unused")
	public ViewControllerSubModel(IViewControllerModel viewModel, DrawingElementController drawingPrimitiveController) {
	}

	@Override
	public Iterator<IDrawingNode> childNodeIterator() {
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public IDrawingNode getParentNode() {
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public IViewControllerModel getViewControllerModel() {
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException("Not implemented yet!");
		
	}

	@Override
	public int compareTo(IViewControllerSubModel o) {
		throw new UnsupportedOperationException("Not implemented yet!");
		
	}

}
