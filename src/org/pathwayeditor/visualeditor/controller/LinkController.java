package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class LinkController implements ILinkController {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkAttribute linkAttribute;
	private ILinkPointDefinition linkDefinition;
	private IViewControllerStore viewModel;
	private boolean isActive;
	private ICanvasAttributePropertyChangeListener srcTermChangeListener;
	private ICanvasAttributePropertyChangeListener tgtTermChangeListener;
//	private ICanvasAttributePropertyChangeListener srcNodeChangeListener;
//	private ICanvasAttributePropertyChangeListener tgtNodeChangeListener;
//	private IShapeController srcNode;
//	private IShapeController tgtNode;
	
	public LinkController(IViewControllerStore viewControllerStore, ILinkAttribute linkAttribute){
		this.linkAttribute = linkAttribute;
		this.linkDefinition = new LinkPointDefinition(linkAttribute);
		this.viewModel = viewControllerStore;
//		srcNode = (IShapeController)this.viewModel.getNodePrimitive(this.linkAttribute.getCurrentDrawingElement().getSourceShape().getAttribute());
//		tgtNode = (IShapeController)this.viewModel.getNodePrimitive(this.linkAttribute.getCurrentDrawingElement().getTargetShape().getAttribute());
		this.srcTermChangeListener = new ICanvasAttributePropertyChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setSrcAnchorPosition(newLocation);
				}
			}
		};
		this.tgtTermChangeListener = new ICanvasAttributePropertyChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setTgtAnchorPosition(newLocation);
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
	public void dispose() {
		if(this.isActive()){
			inactivate();
		}
		this.linkAttribute = null;
		this.linkDefinition = null;
	}

	@Override
	public IViewControllerStore getViewModel() {
		return this.viewModel;
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
	public int compareTo(IDrawingPrimitiveController o) {
		Integer otherIndex = o.getDrawingElement().getCreationSerial();
		return Integer.valueOf(this.linkAttribute.getCreationSerial()).compareTo(otherIndex);
	}

}
