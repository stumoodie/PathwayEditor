package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface INodeController extends IDrawingPrimitiveController {

	/**
	 * Returns the bounds of the drawn node, which may be different from that of the underlying model.
	 * @return the envelope of the drawn node.
	 */
	Envelope getBounds();
	
	/**
	 * Returns the hull of the drawn node primitive. 
	 * @return the hull of the drawn node primitive.
	 */
	IConvexHull getConvexHull();
	
	@Override
	IDrawingNodeAttribute getDrawingElement();
	

	boolean intersectsHull(IConvexHull queryHull);

	boolean canResize(Point originDelta, Dimension resizeDelta);
	
}
