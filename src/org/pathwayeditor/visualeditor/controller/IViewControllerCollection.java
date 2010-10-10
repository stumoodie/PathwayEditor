package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;

public interface IViewControllerCollection {

	Iterator<IDrawingElementController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapeControllerIterator();
	
	Iterator<ILabelController> labelControllerIterator();
	
	Iterator<ILinkController> linkControllerIterator();

	Iterator<INodeController> nodeControllerIterator();

	IDrawingElementController getDrawingPrimitiveController(ICompoundGraphElement testAttribute);
	
	INodeController getNodeController(ICompoundNode testNode);
	
	ILinkController getLinkController(ICompoundEdge attribute);

	IShapeController getShapeController(ICompoundNode attribute);

	boolean containsDrawingElement(ICompoundGraphElement testPrimitive);
}
