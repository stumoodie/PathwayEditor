/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import java.util.Comparator;
import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

/**
 * @author smoodie
 *
 */
public interface IIntersectionCalculator {

	IViewControllerStore getModel();

	void setFilter(IIntersectionCalcnFilter filter);

	void setComparator(Comparator<IDrawingPrimitiveController> comparator);
	
	SortedSet<IDrawingPrimitiveController> findIntersectingNodes(IConvexHull queryHull, IDrawingPrimitiveController queryNode);

	SortedSet<IDrawingPrimitiveController> findDrawingPrimitivesAt(Point p);

	SortedSet<IDrawingPrimitiveController> findIntersectingController(Envelope bounds);
	
	SortedSet<IDrawingPrimitiveController> findIntersectingControllerBounds(Envelope bounds);
}