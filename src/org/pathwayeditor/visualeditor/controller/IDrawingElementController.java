package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;

public interface IDrawingElementController extends Comparable<IDrawingElementController> {

	int getIndex();
	
	IViewControllerModel getViewModel();
	
	/**
	 * Get the view controller sub-model for this element
	 * @return the view controller sub-model, which cannot be null.
	 */
	IViewControllerSubModel getViewControllerSubModel(); 
	
	/**
	 * Gets the drawing element that is the domain model for this primitive.
	 * @return the drawing element, which cannot be null.
	 */
	IDrawingElement getDrawingElement();
	
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

	void addDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener);
	
	void removeDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener);
		
	List<IDrawingElementControllerListener> getDrawingPrimitiveControllerListeners();
	
	boolean containsPoint(Point p);

	boolean intersectsBounds(Envelope drawnBounds);
}
