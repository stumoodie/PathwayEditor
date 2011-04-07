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
	
	SortedSet<IDrawingElementController> findIntersectingParentNodes(IConvexHull queryHull, IDrawingElementController queryNode);

	SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull, IDrawingElementController queryNode);

	SortedSet<IDrawingElementController> findDrawingPrimitivesAt(Point p);

	SortedSet<IDrawingElementController> findIntersectingController(Envelope bounds);

	/**
	 * Find nodes that intersect this hull.
	 * @param queryHull the hull to test
	 * @return the nodes, including the root node that intersect this hull.
	 */
	SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull);
	
//	SortedSet<IDrawingElementController> findIntersectingControllerBounds(Envelope bounds);
}