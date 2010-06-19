package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IBendPoint;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.BendPointChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointLocationChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointLocationChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class LinkController extends DrawingElementController implements ILinkController {
	private final Logger logger = Logger.getLogger(this.getClass());
//	private static final double LINE_SEGMENT_INTERSECTION_TOLERANCE = 1.0;
//	private static final double LINE_HIT_TOLERENCE = 50.0;
	//	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkEdge linkAttribute;
	private ILinkPointDefinition linkDefinition;
	private boolean isActive;
	private ICanvasAttributePropertyChangeListener srcTermChangeListener;
	private ICanvasAttributePropertyChangeListener tgtTermChangeListener;
	private IBendPointChangeListener bpChangeListener;
//	private ICanvasAttributePropertyChangeListener srcNodeChangeListener;
//	private ICanvasAttributePropertyChangeListener tgtNodeChangeListener;
//	private IShapeController srcNode;
//	private IShapeController tgtNode;
	private IBendPointLocationChangeListener bpLocationChangeListener;
	
	public LinkController(IViewControllerModel localViewControllerStore, ILinkEdge localLinkAttribute, int index){
		super(localViewControllerStore, index);
		this.linkAttribute = localLinkAttribute;
		this.linkDefinition = new LinkPointDefinition(linkAttribute.getAttribute());
//		srcNode = (IShapeController)this.viewModel.getNodePrimitive(this.linkAttribute.getCurrentDrawingElement().getSourceShape().getAttribute());
//		tgtNode = (IShapeController)this.viewModel.getNodePrimitive(this.linkAttribute.getCurrentDrawingElement().getTargetShape().getAttribute());
		this.srcTermChangeListener = new ICanvasAttributePropertyChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setSrcAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.tgtTermChangeListener = new ICanvasAttributePropertyChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setTgtAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.bpLocationChangeListener = new IBendPointLocationChangeListener() {
			
			@Override
			public void propertyChange(IBendPointLocationChangeEvent e) {
				Iterator<IBendPoint> bpIter = linkAttribute.getAttribute().bendPointIterator();
				int idx = -1;
				while(bpIter.hasNext()){
					idx++;
					IBendPoint bp = bpIter.next();
					if(bp.equals(e.getBendPoint())){
						break;
					}
				}
				Point bpPosn = e.getBendPoint().getLocation();
				linkDefinition.setBendPointPosition(idx, bpPosn);
				updateLinksToBendPoints(idx, bpPosn);
			}
		};
		this.bpChangeListener = new IBendPointChangeListener() {
			
			@Override
			public void propertyChange(IBendPointChangeEvent e) {
				if(e.getChangeType().equals(BendPointChange.BEND_POINT_ADDED)){
					int bpIdx = e.getNewIndexPos();
					Point bpPosn = e.getBendPoint().getLocation();
					linkDefinition.addNewBendPoint(bpIdx, bpPosn);
					updateLinksToBendPoints(bpIdx, bpPosn);
					e.getBendPoint().addChangeListener(bpLocationChangeListener);
				}
				else if(e.getChangeType().equals(BendPointChange.BEND_POINT_REMOVED)){
					int bpIdx = e.getOldIndexPos();
					linkDefinition.removeBendPoint(bpIdx);
					if(bpIdx < linkDefinition.numBendPoints()){
						// recalculate anchor points on remaining bend-point(s)
						Point bpPosn = linkAttribute.getAttribute().getBendPoint(bpIdx).getLocation();
						updateLinksToBendPoints(bpIdx, bpPosn);
					}
					else if(linkDefinition.numBendPoints() == 0){
						// no bend-points
						updateAnchorPoints();
					}
					else{
						// in this case the last bp was removed so we need to take the new last bp
						int lastBpIdx = linkDefinition.numBendPoints()-1;
						Point bpPosn = linkAttribute.getAttribute().getBendPoint(lastBpIdx).getLocation();
						updateLinksToBendPoints(lastBpIdx, bpPosn);
					}
					e.getBendPoint().removeChangeListener(bpLocationChangeListener);
				}
			}
		};
//		this.srcNodeChangeListener = new ICanvasAttributePropertyChangeListener() {
//			@Override
//			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
//				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
//					changeSourceAnchor();
//				}
//			}
//		};
//		this.tgtNodeChangeListener = new ICanvasAttributePropertyChangeListener() {
//			@Override
//			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
//				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
//					changeTargetAnchor();
//				}
//			}
//		};
	}
	
	private void updateAnchorPoints() {
		updateSrcAnchor(linkAttribute.getAttribute().getTargetTerminus().getLocation());
		updateTgtAnchor(linkAttribute.getAttribute().getSourceTerminus().getLocation());
	}

	private void updateSrcAnchor(Point otherEndPos){
		IShapeController shapeController = getViewModel().getShapeController(linkAttribute.getSourceShape());
		IAnchorLocator anchorCalc = shapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		anchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = anchorCalc.calcAnchorPosition();
		linkAttribute.getAttribute().getSourceTerminus().setLocation(newSrcPosn);
	}
	
	private void updateTgtAnchor(Point otherEndPos){
		IShapeController shapeController = getViewModel().getShapeController(linkAttribute.getTargetShape());
		IAnchorLocator anchorCalc = shapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		anchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = anchorCalc.calcAnchorPosition();
		linkAttribute.getAttribute().getTargetTerminus().setLocation(newSrcPosn);
	}
	
	private void updateLinksToBendPoints(int bpIdx, Point bpPosn){
		// check if bp attached to anchor and recalc anchor if it is
		if(bpIdx == 0){
			updateSrcAnchor(bpPosn);
		}
		if(bpIdx == linkAttribute.getAttribute().numBendPoints()-1){
			updateTgtAnchor(bpPosn);
		}
	}
	
	@Override
	public ILinkEdge getDrawingElement() {
		return this.linkAttribute;
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.linkDefinition;
	}

	@Override
	public void activate() {
		this.linkAttribute.getAttribute().getSourceTerminus().addChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().addChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().addChangeListener(this.bpChangeListener);
//		this.srcNode.getDrawingElement().addChangeListener(srcNodeChangeListener);
//		this.tgtNode.getDrawingElement().addChangeListener(tgtNodeChangeListener);
		this.isActive = true;
	}

	@Override
	public void inactivate() {
		this.linkAttribute.getAttribute().getSourceTerminus().removeChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().removeChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().removeChangeListener(this.bpChangeListener);
//		this.srcNode.getDrawingElement().removeChangeListener(srcNodeChangeListener);
//		this.tgtNode.getDrawingElement().removeChangeListener(tgtNodeChangeListener);
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.linkDefinition.getBounds();
//		double minX = Double.MAX_VALUE;
//		double maxX = Double.MIN_VALUE;
//		double minY = Double.MAX_VALUE;
//		double maxY = Double.MIN_VALUE;
//		final double halfLineHeight = this.linkAttribute.getLineWidth();// + LINE_HIT_TOLERENCE;
//		Iterator<Point> pointIter = this.linkDefinition.pointIterator();
//		while(pointIter.hasNext()){
//			Point p = pointIter.next();
//			minX = Math.min(minX, p.getX()-halfLineHeight);
//			maxX = Math.max(maxX, p.getX()+halfLineHeight);
//			minY = Math.min(minY, p.getY()-halfLineHeight);
//			maxY = Math.max(maxY, p.getY()+halfLineHeight);
//		}
//		return new Envelope(minX, minY, maxX-minX, maxY-minY);
	}

	@Override
	public boolean containsPoint(Point p) {
		boolean retVal = false;
		Envelope bounds = getDrawnBounds(); 
		if(bounds.containsPoint(p)){
//			final double halfLineHeight = this.linkAttribute.getLineWidth() + LINE_HIT_TOLERENCE;
			retVal = this.linkDefinition.containsPoint(p);//, halfLineHeight); 
			if(logger.isTraceEnabled() && retVal){
				logger.trace("Bounds contains point. bounds=" + bounds + ",point=" + p);
			}
		}
		return retVal;
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		boolean retVal = false;
		Iterator<LineSegment> iter = this.linkDefinition.lineSegIterator();
		while(iter.hasNext() && !retVal){
			LineSegment line = iter.next();
			retVal = isLineIntersectingBounds(line, drawnBounds);
			if(logger.isTraceEnabled() && retVal){
				logger.trace("Line intersects bounds. Bounds=" + drawnBounds + " lineSeg=" + line);
			}
		}
		return retVal;
	}

	private boolean isLineIntersectingBounds(LineSegment line, Envelope drawnBounds) {
		Point origin = drawnBounds.getOrigin();
		Point horizontalCorner = drawnBounds.getHorizontalCorner();
		Point diagonalCorner = drawnBounds.getDiagonalCorner();
		Point verticalCorner = drawnBounds.getVerticalCorner();
		return drawnBounds.containsPoint(line.getOrigin()) || drawnBounds.containsPoint(line.getTerminus())
			|| line.intersect(new LineSegment(origin, horizontalCorner), this.linkAttribute.getAttribute().getLineWidth()) != null
			|| line.intersect(new LineSegment(horizontalCorner, diagonalCorner), this.linkAttribute.getAttribute().getLineWidth()) != null
			|| line.intersect(new LineSegment(diagonalCorner, verticalCorner), this.linkAttribute.getAttribute().getLineWidth()) != null
			|| line.intersect(new LineSegment(verticalCorner, origin), this.linkAttribute.getAttribute().getLineWidth()) != null;
	}
}
