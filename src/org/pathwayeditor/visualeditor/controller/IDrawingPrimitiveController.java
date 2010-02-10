package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface IDrawingPrimitiveController extends Comparable<IDrawingPrimitiveController> {

	int getIndex();
	
	IViewControllerStore getViewModel();
	
	/**
	 * Gets the drawing element that is the domain model for this primitive.
	 * @return the drawing element, which cannot be null.
	 */
	ICanvasAttribute getDrawingElement();
	
	Envelope getDrawnBounds();
	
	/**
	 * Enables listeners  
	 */
	void activate();
	
	/**
	 * Is this instance in an active state, i.e. with listeners enabled.
	 * @return
	 */
	boolean isActive();
	
	/**
	 * Turns off listeners.
	 */
	void inactivate();

	void addDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener);
	
	void removeDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener);
		
	List<IDrawingPrimitiveControllerListener> getDrawingPrimitiveControllerListeners();
	
	boolean containsPoint(Point p);

	boolean intersectsHull(IConvexHull queryHull);
}
