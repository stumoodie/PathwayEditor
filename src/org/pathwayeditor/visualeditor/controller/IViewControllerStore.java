package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;

public interface IViewControllerStore {

	IModel getDomainModel();
	
	void synchroniseWithDomainModel();
	
	Iterator<IDrawingPrimitiveController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapePrimitiveIterator();
	
	Iterator<ILabelController> labelPrimitiveIterator();
	
	Iterator<ILinkController> linkPrimitiveIterator();

	Iterator<INodeController> nodePrimitiveIterator();

	IRootController getRootNode();

	INodeController getNodePrimitive(IDrawingNode testNode);
	
	boolean containsDrawingElement(IDrawingElement testPrimitive);

	void activate();
	
}
