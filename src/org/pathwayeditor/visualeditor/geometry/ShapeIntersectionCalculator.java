/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

/**
 * @author smoodie
 *
 */
public class ShapeIntersectionCalculator implements INodeIntersectionCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private static final IIntersectionCalcnFilter DEFAULT_FILTER = new IIntersectionCalcnFilter(){
		public boolean accept(INodeController node) {
			return true;
		}
	};
	
	private static final Comparator<INodeController> DEFAULT_COMPARATOR = new Comparator<INodeController>(){

		public int compare(INodeController o1, INodeController o2) {
			int retVal = 0;
			if(o1.getDrawingElement().getLevel() < o2.getDrawingElement().getLevel()){
				retVal = 1;
			}
			else if(o1.getDrawingElement().getLevel() > o2.getDrawingElement().getLevel()){
				retVal = -1;
			}
			else{
				int o1Idx = o1.getDrawingElement().getIndex();
				int o2Idx = o2.getDrawingElement().getIndex();
				retVal = o1Idx < o2Idx ? 1 : (o1Idx > o2Idx ? -1 : 0); 
			}
			return retVal;
		}
		
	};
	
	private final IViewControllerStore model;
	private IIntersectionCalcnFilter filter;
	private Comparator<INodeController> comparator = DEFAULT_COMPARATOR;
	
	public ShapeIntersectionCalculator(IViewControllerStore model){
		this.model = model;
		this.filter = DEFAULT_FILTER;
	}
	
	public void setComparator(Comparator<INodeController> comparator){
		this.comparator = comparator;
	}
	
	
	public IViewControllerStore getModel(){
		return this.model;
	}
	
	public void setFilter(IIntersectionCalcnFilter filter){
		if(filter == null){
			this.filter = DEFAULT_FILTER;
		}
		else{
			this.filter = filter;
		}
	}
	
	public SortedSet<INodeController> findIntersectingNodes(IConvexHull queryHull, INodeController queryNode){
		Iterator<INodeController> iter = model.nodePrimitiveIterator();
		SortedSet<INodeController> retVal = new TreeSet<INodeController>(this.comparator);
		// the root node will always intersect - that's a give so add it in and exclude it from
		// intersection tests
		IRootController rootNode = model.getRootNode();
		retVal.add(rootNode);
		while(iter.hasNext()){
			INodeController node = iter.next();
			IConvexHull attributeHull = node.getConvexHull();
			// ignore matches to self
			if(!node.equals(queryNode) && !node.equals(rootNode) && filter.accept(node) && queryHull.hullsIntersect(attributeHull)){
				retVal.add(node);
			}
		}
		return retVal;
	}

	public SortedSet<INodeController> findNodesAt(Point p) {
		Iterator<INodeController> iter = model.nodePrimitiveIterator();
		SortedSet<INodeController> retVal = new TreeSet<INodeController>(this.comparator);
		while(iter.hasNext()){
			INodeController node = iter.next();
			IConvexHull attributeHull = node.getConvexHull();
			logger.trace("Testing contains node:" + node + ", hull=" + attributeHull + ", point=" + p);
			if(filter.accept(node) && attributeHull.containsPoint(p)){
				logger.trace("Found containing node");
				retVal.add(node);
			}
		}
		return retVal;
	}
}
