package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class LinkController extends DrawingPrimitiveController implements ILinkController {
	private static final double LINE_HIT_TOLERENCE = 5.0;
	//	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkAttribute linkAttribute;
	private ILinkPointDefinition linkDefinition;
	private boolean isActive;
	private ICanvasAttributePropertyChangeListener srcTermChangeListener;
	private ICanvasAttributePropertyChangeListener tgtTermChangeListener;
//	private ICanvasAttributePropertyChangeListener srcNodeChangeListener;
//	private ICanvasAttributePropertyChangeListener tgtNodeChangeListener;
//	private IShapeController srcNode;
//	private IShapeController tgtNode;
	
	public LinkController(IViewControllerStore viewControllerStore, ILinkAttribute linkAttribute, int index){
		super(viewControllerStore, index);
		this.linkAttribute = linkAttribute;
		this.linkDefinition = new LinkPointDefinition(linkAttribute);
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
	
	@Override
	public ILinkAttribute getDrawingElement() {
		return this.linkAttribute;
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.linkDefinition;
	}

	@Override
	public void activate() {
		this.linkAttribute.getSourceTerminus().addChangeListener(srcTermChangeListener);
		this.linkAttribute.getTargetTerminus().addChangeListener(tgtTermChangeListener);
//		this.srcNode.getDrawingElement().addChangeListener(srcNodeChangeListener);
//		this.tgtNode.getDrawingElement().addChangeListener(tgtNodeChangeListener);
		this.isActive = true;
	}

	@Override
	public void inactivate() {
		this.linkAttribute.getSourceTerminus().removeChangeListener(srcTermChangeListener);
		this.linkAttribute.getTargetTerminus().removeChangeListener(tgtTermChangeListener);
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
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		final double halfLineHeight = this.linkAttribute.getLineWidth() + LINE_HIT_TOLERENCE;
		Iterator<Point> pointIter = this.linkDefinition.pointIterator();
		while(pointIter.hasNext()){
			Point p = pointIter.next();
			minX = Math.min(minX, p.getX()-halfLineHeight);
			maxX = Math.max(maxX, p.getX()+halfLineHeight);
			minY = Math.min(minY, p.getY()-halfLineHeight);
			maxY = Math.max(maxY, p.getY()+halfLineHeight);
		}
		return new Envelope(minX, minY, maxX-minX, maxY-minY);
	}

	@Override
	public boolean containsPoint(Point p) {
		boolean retVal = false;
		if(getDrawnBounds().containsPoint(p)){
			final double halfLineHeight = this.linkAttribute.getLineWidth() + LINE_HIT_TOLERENCE;
			retVal = this.linkDefinition.containsPoint(p, halfLineHeight); 
		}
		return retVal;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return false;
	}

}
