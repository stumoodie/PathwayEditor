package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
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

	INodeController getNodePrimitive(IDrawingNodeAttribute testNode);
	
	boolean containsDrawingElement(ICanvasAttribute testPrimitive);

	void activate();
	
	void deactivate();
	
	boolean isActive();
	
	void addViewControllerChangeListener(IViewControllerChangeListener listener);
	
	void removeViewControllerChangeListener(IViewControllerChangeListener listener);
	
	List<IViewControllerChangeListener> getViewControllerChangeListeners();
}
