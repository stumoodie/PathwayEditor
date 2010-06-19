/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveControllerEvent;
import org.pathwayeditor.visualeditor.controller.IDrawingElementControllerListener;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerChangeListener;
import org.pathwayeditor.visualeditor.controller.IViewControllerNodeStructureChangeEvent;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.controller.IViewControllerNodeStructureChangeEvent.ViewControllerStructureChangeType;

/**
 * @author smoodie
 *
 */
public class FastShapeIntersectionCalculator implements IIntersectionCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private static final IIntersectionCalcnFilter DEFAULT_FILTER = new IIntersectionCalcnFilter(){
		public boolean accept(IDrawingElementController node) {
			return true;
		}
	};
	
	private static final Comparator<IDrawingElementController> DEFAULT_COMPARATOR = new Comparator<IDrawingElementController>(){

		public int compare(IDrawingElementController o1, IDrawingElementController o2) {
			int retVal = 0;
			if(o1.getDrawingElement().getLevel() < o2.getDrawingElement().getLevel()){
				retVal = 1;
			}
			else if(o1.getDrawingElement().getLevel() > o2.getDrawingElement().getLevel()){
				retVal = -1;
			}
			else{
				long o1Idx = o1.getDrawingElement().getUniqueIndex();
				long o2Idx = o2.getDrawingElement().getUniqueIndex();
				retVal = o1Idx < o2Idx ? 1 : (o1Idx > o2Idx ? -1 : 0); 
			}
			return retVal;
		}
		
	};
	
	private final IMutableSpacialIndex2D<IDrawingElementController> spacialIndex;
	private final IViewControllerModel model;
	private IIntersectionCalcnFilter filter;
	private Comparator<IDrawingElementController> comparator = null;
	private IDrawingElementControllerListener primitiveControllerChangeListener;
	private IViewControllerChangeListener viewControllerChangeListener;
	
	public FastShapeIntersectionCalculator(IViewControllerModel model){
		this.model = model;
		this.filter = DEFAULT_FILTER;
		this.spacialIndex = new RTree<IDrawingElementController>();
		this.primitiveControllerChangeListener = new IDrawingElementControllerListener() {
			@Override
			public void drawnBoundsChanged(IDrawingPrimitiveControllerEvent e) {
				spacialIndex.delete(e.getController());
				Envelope drawnBounds = e.getController().getDrawnBounds();
				spacialIndex.insert(e.getController(), drawnBounds);
				if(logger.isTraceEnabled()){
					logger.trace("FastShapeIntersectionCalc: bounds changed for contoller=" + e.getController() + ", bounds=" + drawnBounds);
				}
			}
		};
		this.viewControllerChangeListener = new IViewControllerChangeListener() {
			
			@Override
			public void nodeStructureChangeEvent(IViewControllerNodeStructureChangeEvent e) {
				if(e.getChangeType().equals(ViewControllerStructureChangeType.NODE_ADDED)
						|| e.getChangeType().equals(ViewControllerStructureChangeType.LINK_ADDED)){
					IDrawingElementController cont = e.getChangedElement();
					cont.addDrawingPrimitiveControllerListener(primitiveControllerChangeListener);
					Envelope drawnBounds = cont.getDrawnBounds();
					spacialIndex.insert(cont, drawnBounds);
					if(logger.isTraceEnabled()){
						logger.trace("FastShapeIntersectionCalc: inserted to RTree: contoller=" + cont + ",bound=" + drawnBounds);
					}
				}
				else if(e.getChangeType().equals(ViewControllerStructureChangeType.NODE_REMOVED)
						|| e.getChangeType().equals(ViewControllerStructureChangeType.LINK_REMOVED)){
					IDrawingElementController cont = e.getChangedElement();
					cont.removeDrawingPrimitiveControllerListener(primitiveControllerChangeListener);
					spacialIndex.delete(cont);
					if(logger.isTraceEnabled()){
						logger.trace("FastShapeIntersectionCalc: deleted from RTree: contoller=" + cont);
					}
				}
			}
		};
		buildFromViewController();
		this.model.addViewControllerChangeListener(viewControllerChangeListener);
	}
	
	private void buildFromViewController() {
		Iterator<IDrawingElementController> nodeIter = model.drawingPrimitiveIterator();
		while(nodeIter.hasNext()){
			IDrawingElementController node = nodeIter.next();
			if(!(node instanceof IRootController)){
				// ignore root
				Envelope drawnBounds = node.getDrawnBounds();
				Point origin = drawnBounds.getOrigin();
				Point diagonal = drawnBounds.getDiagonalCorner();
				this.spacialIndex.insert(node, (float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY());
				if(logger.isTraceEnabled()){
					logger.trace("Inserted element=" + node + " into RTree with extent=" + drawnBounds);
				}
				node.addDrawingPrimitiveControllerListener(this.primitiveControllerChangeListener);
			}
		}
	}

	@Override
	public void setComparator(Comparator<IDrawingElementController> comparator){
		this.comparator = comparator;
	}
	
	
	@Override
	public IViewControllerModel getModel(){
		return this.model;
	}
	
	@Override
	public void setFilter(IIntersectionCalcnFilter filter){
		if(filter == null){
			this.filter = DEFAULT_FILTER;
		}
		else{
			this.filter = filter;
		}
	}
	
	private SortedSet<IDrawingElementController> createSortedSet(){
		SortedSet<IDrawingElementController> retVal = null;
		if(this.comparator != null){
			retVal = new TreeSet<IDrawingElementController>(this.comparator);
		}
		else{
			retVal = new TreeSet<IDrawingElementController>(DEFAULT_COMPARATOR);
		}
		return retVal;
	}
	
	@Override
	public SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull, IDrawingElementController queryNode){
		SortedSet<IDrawingElementController> retVal = createSortedSet();
		// the root node will always intersect - that's a give so add it in and exclude it from
		// intersection tests
		IRootController rootNode = model.getRootNode();
		if(filter.accept(rootNode)){
			retVal.add(rootNode);
		}
		Envelope drawnBounds = queryHull.getEnvelope();
		Point origin = drawnBounds.getOrigin();
		Point diagonal = drawnBounds.getDiagonalCorner();
		ISpacialEntry2DEnumerator<IDrawingElementController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
		while(iter.numRemaining() > 0){
			IDrawingElementController prim = iter.nextInt();
			if(prim instanceof INodeController){
				INodeController node = (INodeController)prim;
				// ignore matches to self
				if(!node.equals(queryNode) && !node.equals(rootNode) && filter.accept(node) && node.intersectsHull(queryHull)){
					retVal.add(node);
				}
			}
		}
		return retVal;
	}

	@Override
	public SortedSet<IDrawingElementController> findDrawingPrimitivesAt(Point p) {
		SortedSet<IDrawingElementController> retVal = createSortedSet();
		Point origin = p;
		Point diagonal = p;
		ISpacialEntry2DEnumerator<IDrawingElementController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
		while(iter.numRemaining() > 0){
			IDrawingElementController node = iter.nextInt();
			if(logger.isTraceEnabled()){
				logger.trace("RTree found overlapping node: " + node +",bound=" + node.getDrawnBounds());
			}
			if(filter.accept(node) && node.containsPoint(p)){
				logger.trace("Found containing node");
				retVal.add(node);
			}
		}
		return retVal;
	}

	@Override
	public SortedSet<IDrawingElementController> findIntersectingController(Envelope drawnBounds) {
		if(logger.isDebugEnabled()){
			logger.debug("Finding elements intersecting with bounds=" + drawnBounds);
		}
//		IConvexHullCalculator builder = new ConvexHullCalculator();
//		builder.addPoint(drawnBounds.getOrigin());
//		builder.addPoint(drawnBounds.getDiagonalCorner());
//		builder.addPoint(drawnBounds.getHorizontalCorner());
//		builder.addPoint(drawnBounds.getVerticalCorner());
//		builder.calculate();
//		IConvexHull queryHull = builder.getConvexHull();
		SortedSet<IDrawingElementController> retVal = createSortedSet();
		Point origin = drawnBounds.getOrigin();
		Point diagonal = drawnBounds.getDiagonalCorner();
		ISpacialEntry2DEnumerator< IDrawingElementController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
		while(iter.numRemaining() > 0){
			IDrawingElementController element = iter.nextInt();
			if(logger.isTraceEnabled()){
				logger.trace("R-Tree found overlapping node: " + element + ",elementBound=" + element.getDrawnBounds());
			}
			if(element.intersectsBounds(drawnBounds)){
				if(logger.isTraceEnabled()){
					logger.trace("Element: " + element + " overlaps bounds=" + drawnBounds);
				}
				retVal.add(element);
			}
		}
		return retVal;
	}

	@Override
	public SortedSet<IDrawingElementController> findIntersectingControllerBounds(Envelope drawnBounds) {
		if(logger.isDebugEnabled()){
			logger.debug("Finding elements intersecting with bounds=" + drawnBounds);
		}
		SortedSet<IDrawingElementController> retVal = createSortedSet();
		Point origin = drawnBounds.getOrigin();
		Point diagonal = drawnBounds.getDiagonalCorner();
		ISpacialEntry2DEnumerator< IDrawingElementController> iter = this.spacialIndex.queryOverlap((float)origin.getX(), (float)origin.getY(), (float)diagonal.getX(), (float)diagonal.getY(), null, 0, false);
		while(iter.numRemaining() > 0){
			IDrawingElementController element = iter.nextInt();
			retVal.add(element);
		}
		return retVal;
	}
}
