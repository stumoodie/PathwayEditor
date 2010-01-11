/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import java.util.Comparator;
import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

/**
 * @author smoodie
 *
 */
public interface INodeIntersectionCalculator {

	IViewControllerStore getModel();

	void setFilter(IIntersectionCalcnFilter filter);

	void setComparator(Comparator<INodeController> comparator);
	
	SortedSet<INodeController> findIntersectingNodes(IConvexHull queryHull, INodeController queryNode);

	SortedSet<INodeController> findNodesAt(Point p);
	
}