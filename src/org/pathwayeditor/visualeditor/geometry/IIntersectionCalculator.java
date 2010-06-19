/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import java.util.Comparator;
import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;

/**
 * @author smoodie
 *
 */
public interface IIntersectionCalculator {

	IViewControllerModel getModel();

	void setFilter(IIntersectionCalcnFilter filter);

	void setComparator(Comparator<IDrawingElementController> comparator);
	
	SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull, IDrawingElementController queryNode);

	SortedSet<IDrawingElementController> findDrawingPrimitivesAt(Point p);

	SortedSet<IDrawingElementController> findIntersectingController(Envelope bounds);
	
	SortedSet<IDrawingElementController> findIntersectingControllerBounds(Envelope bounds);
}