package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;

public interface IViewControllerCollection {

	Iterator<IDrawingPrimitiveController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapeControllerIterator();
	
	Iterator<ILabelController> labelControllerIterator();
	
	Iterator<ILinkController> linkControllerIterator();

	Iterator<INodeController> nodeControllerIterator();

	IDrawingPrimitiveController getDrawingPrimitiveController(ICanvasAttribute testAttribute);
	
	INodeController getNodeController(IDrawingNodeAttribute testNode);
	
	ILinkController getLinkController(ILinkAttribute attribute);

	IShapeController getShapeController(IShapeAttribute attribute);

	boolean containsDrawingElement(ICanvasAttribute testPrimitive);
}
