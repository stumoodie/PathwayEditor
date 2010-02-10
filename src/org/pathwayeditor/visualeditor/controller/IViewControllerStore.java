package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.Envelope;

public interface IViewControllerStore {

	IModel getDomainModel();
	
	Iterator<IDrawingPrimitiveController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapeControllerIterator();
	
	Iterator<ILabelController> labelControllerIterator();
	
	Iterator<ILinkController> linkControllerIterator();

	Iterator<INodeController> nodeControllerIterator();

	IRootController getRootNode();

	INodeController getNodeController(IDrawingNodeAttribute testNode);
	
	boolean containsDrawingElement(ICanvasAttribute testPrimitive);

	void activate();
	
	void deactivate();
	
	boolean isActive();

	ILinkController getLinkController(ILinkAttribute attribute);

	IShapeController getShapeController(IShapeAttribute attribute);

	Envelope getCanvasBounds();
	
	void addViewControllerChangeListener(IViewControllerChangeListener listener);
	
	void removeViewControllerChangeListener(IViewControllerChangeListener listener);
	
	List<IViewControllerChangeListener> getViewControllerChangeListeners();
}
