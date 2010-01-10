/**
 * 
 */
package org.pathwayeditor.visualeditor;

import java.util.Comparator;
import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

/**
 * @author smoodie
 *
 */
public interface INodeIntersectionCalculator {

	IViewModel getModel();

	void setFilter(IIntersectionCalcnFilter filter);

	void setComparator(Comparator<INodePrimitive> comparator);
	
	SortedSet<INodePrimitive> findIntersectingNodes(IConvexHull queryHull, INodePrimitive queryNode);

	SortedSet<INodePrimitive> findNodesAt(Point p);
	
}