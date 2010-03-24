package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;

public interface ISelectionHandle extends Comparable<ISelectionHandle> {
	enum SelectionHandleType { Central, N, NE, E, SE, S, SW, W, NW, None, LinkMidPoint, LinkBendPoint, Link };
	
	void drawShape(IHandleShapeDrawer drawer);
	
	ISelection getSelection();
	
	Envelope getBounds();
	
	SelectionHandleType getType();
	
	IDrawingPrimitiveController getDrawingPrimitiveController();

	boolean containsPoint(Point point);

	void translate(Point delta);

	/**
	 * If there is more than one handle of the same type for a particular selection  then
	 * the index disambiguates them.
	 */
	int getHandleIndex();
}
