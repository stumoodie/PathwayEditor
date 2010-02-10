package org.pathwayeditor.visualeditor.controller;

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
		return this.linkDefinition.getBounds();
	}

	@Override
	public boolean containsPoint(Point p) {
		return this.linkDefinition.containsPoint(p);
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return false;
	}

}
