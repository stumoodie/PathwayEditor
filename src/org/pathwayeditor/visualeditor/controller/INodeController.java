package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface INodeController extends IDrawingElementController {

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

	/**
	 * Get the drawing element. 
	 */
	@Override
	IDrawingNode getDrawingElement();
	
	/**
	 * Tests if the convex hull intersects this shape.
	 * @param queryHull the queryHull to test/
	 * @return true if it intersects, false otherwise.
	 */
	boolean intersectsHull(IConvexHull queryHull);

	/**
	 * Tests id this node can resize to the new settings provided. 
	 * @param originDelta the change to the origin of the shape's bounds. 
	 * @param resizeDelta the change to the size of the shapes bounds.
	 * @return true of the resize will succeed, false otherwise.
	 */
	boolean canResize(Point originDelta, Dimension resizeDelta);
	
}
