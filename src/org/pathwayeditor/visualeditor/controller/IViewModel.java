package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;

public interface IViewModel {

	IModel getDomainModel();
	
	void synchroniseWithDomainModel();
	
	Iterator<IDrawingPrimitive> drawingPrimitiveIterator();
	
//	void addViewModelListener(IViewModelListener listener);
	
//	void removeViewModelListener(IViewModelListener listener);
	
//	List<IViewModelListener> getViewModelListeners();

	Iterator<IShapePrimitive> shapePrimitiveIterator();
	
	Iterator<ILabelPrimitive> labelPrimitiveIterator();
	
	Iterator<ILinkPrimitive> linkPrimitiveIterator();

	Iterator<INodePrimitive> nodePrimitiveIterator();

	IRootPrimitive getRootNode();

	INodePrimitive getNodePrimitive(IDrawingNode testNode);
	
	boolean containsDrawingElement(IDrawingElement testPrimitive);
	
}
