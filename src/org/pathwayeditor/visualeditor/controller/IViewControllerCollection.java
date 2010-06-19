package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;

public interface IViewControllerCollection {

	Iterator<IDrawingElementController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapeControllerIterator();
	
	Iterator<ILabelController> labelControllerIterator();
	
	Iterator<ILinkController> linkControllerIterator();

	Iterator<INodeController> nodeControllerIterator();

	IDrawingElementController getDrawingPrimitiveController(IDrawingElement testAttribute);
	
	INodeController getNodeController(IDrawingNode testNode);
	
	ILinkController getLinkController(ILinkEdge attribute);

	IShapeController getShapeController(IShapeNode attribute);

	boolean containsDrawingElement(IDrawingElement testPrimitive);
}
