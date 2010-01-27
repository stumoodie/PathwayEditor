package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;

public interface IViewControllerStore {

	IModel getDomainModel();
	
//	void synchroniseWithDomainModel();
	
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

	ILinkController getLinkController(ILinkAttribute attribute);

	IShapeController getShapeController(IShapeAttribute attribute);
	
//	void addViewControllerChangeListener(IViewControllerChangeListener listener);
//	
//	void removeViewControllerChangeListener(IViewControllerChangeListener listener);
//	
//	List<IViewControllerChangeListener> getViewControllerChangeListeners();
}
