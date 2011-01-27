package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.BendPointChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointLocationChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusValueChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.LinkTerminusChangeType;
import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFacade;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class LinkController extends DrawingElementController implements ILinkController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICanvasElementAttribute parentAttribute;
	private final ILinkEdge linkAttribute;
	private final ILinkPointDefinition linkDefinition;
	private boolean isActive;
	private final ILinkTerminusChangeListener srcTermChangeListener;
	private final ILinkTerminusChangeListener tgtTermChangeListener;
	private final IBendPointChangeListener bpChangeListener;
	private final ICanvasAttributeChangeListener parentDrawingElementPropertyChangeListener;
	
	public LinkController(IViewControllerModel localViewControllerStore, ILinkEdge localLinkAttribute, int index){
		super(localViewControllerStore, index);
		this.linkAttribute = localLinkAttribute;
		this.parentAttribute = (ICanvasElementAttribute)this.linkAttribute.getGraphElement().getParent().getAttribute();
		this.linkDefinition = new LinkPointDefinition(linkAttribute.getAttribute());
		this.srcTermChangeListener = new ILinkTerminusChangeListener() {
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setSrcAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.tgtTermChangeListener = new ILinkTerminusChangeListener() {
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setTgtAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.parentDrawingElementPropertyChangeListener = new ICanvasAttributeChangeListener() {
			
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				linkAttribute.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.bpChangeListener = new IBendPointChangeListener() {
			
			@Override
			public void locationChange(IBendPointLocationChangeEvent e) {
				Point bpPosn = e.getNewPosition();
				int idx = e.getBendPointIndex();
				linkDefinition.setBendPointPosition(idx, bpPosn);
				updateLinksToBendPoints(idx, bpPosn);
			}

			@Override
			public void propertyChange(IBendPointChangeEvent e) {
				Envelope originalDrawnBounds = getDrawnBounds();
				if(e.getChangeType().equals(BendPointChange.BEND_POINT_ADDED)){
					int bpIdx = e.getNewIndexPos();
					Point bpPosn = e.getBendPoint();
					linkDefinition.addNewBendPoint(bpIdx, bpPosn);
					updateLinksToBendPoints(bpIdx, bpPosn);
				}
				else if(e.getChangeType().equals(BendPointChange.BEND_POINT_REMOVED)){
					int bpIdx = e.getOldIndexPos();
					linkDefinition.removeBendPoint(bpIdx);
					if(bpIdx < linkDefinition.numBendPoints()){
						// recalculate anchor points on remaining bend-point(s)
						Point bpPosn = linkAttribute.getAttribute().getBendPointContainer().getBendPoint(bpIdx);
						updateLinksToBendPoints(bpIdx, bpPosn);
					}
					else if(linkDefinition.numBendPoints() == 0){
						// no bend-points
						updateAnchorPoints();
					}
					else{
						// in this case the last bp was removed so we need to take the new last bp
						int lastBpIdx = linkDefinition.numBendPoints()-1;
						Point bpPosn = linkAttribute.getAttribute().getBendPointContainer().getBendPoint(lastBpIdx);
						updateLinksToBendPoints(lastBpIdx, bpPosn);
					}
				}
				notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
			}
		};
	}
	
	private void updateAnchorPoints() {
		updateSrcAnchor(linkAttribute.getAttribute().getTargetTerminus().getLocation());
		updateTgtAnchor(linkAttribute.getAttribute().getSourceTerminus().getLocation());
	}

	private void updateSrcAnchor(Point otherEndPos){
		IShapeController shapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getSourceShape()));
		IAnchorLocator anchorCalc = shapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		anchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = anchorCalc.calcAnchorPosition();
		linkAttribute.getAttribute().getSourceTerminus().setLocation(newSrcPosn);
	}
	
	private void updateTgtAnchor(Point otherEndPos){
		IShapeController shapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getTargetShape()));
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
		if(bpIdx == linkAttribute.getAttribute().getBendPointContainer().numBendPoints()-1){
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
		this.parentAttribute.addChangeListener(parentDrawingElementPropertyChangeListener);
		this.linkAttribute.getAttribute().getSourceTerminus().addLinkTerminusChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().addLinkTerminusChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().getBendPointContainer().addChangeListener(this.bpChangeListener);
//		this.srcNode.getDrawingElement().addChangeListener(srcNodeChangeListener);
//		this.tgtNode.getDrawingElement().addChangeListener(tgtNodeChangeListener);
		this.isActive = true;
	}

	@Override
	public void inactivate() {
		this.linkAttribute.getAttribute().getSourceTerminus().removeLinkTerminusChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().removeLinkTerminusChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().getBendPointContainer().removeChangeListener(this.bpChangeListener);
		this.parentAttribute.removeChangeListener(parentDrawingElementPropertyChangeListener);
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.linkDefinition.getBounds();
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
