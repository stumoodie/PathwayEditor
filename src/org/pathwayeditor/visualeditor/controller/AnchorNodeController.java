package org.pathwayeditor.visualeditor.controller;

import java.awt.Graphics2D;

import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.impl.facades.DrawingNodeFacade;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;

import uk.ac.ed.inf.graph.compound.ICompoundNode;

public class AnchorNodeController extends DrawingElementController implements INodeController {
	private final IAnchorNodeAttribute anchorNodeAttribute;
	private boolean isActive;
	
	
	public AnchorNodeController(IViewControllerModel viewController, IAnchorNodeAttribute anchorNodeAttribute, int index) {
		super(viewController, index);
		this.anchorNodeAttribute = anchorNodeAttribute;
		this.isActive = false;
	}

	@Override
	public IMiniCanvas getMiniCanvas() {
		return new IMiniCanvas() {
			
			@Override
			public void paint(Graphics2D g) {
				
			}
			
			@Override
			public Envelope getBounds() {
				return AnchorNodeController.this.getBounds();
			}
		};
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.anchorNodeAttribute.getBounds();
	}

	@Override
	public void activate() {
		this.isActive = true;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void inactivate() {
		this.isActive = false;
	}


	@Override
	public boolean containsPoint(Point p) {
		return false;
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		return false;
	}

//	@Override
//	public IAnchorLocatorFactory getAnchorLocatorFactory() {
//		ILinkAttribute linkAtt = this.anchorNodeAttribute.getAssociatedCurveSegment().getOwningLink();
//		ILinkController parentLinkController = this.getViewModel().getLinkController(new LinkEdgeFacade(linkAtt.getCurrentElement()));
//		return parentLinkController.getAnchorLocatorFactory();
////		return new IAnchorLocatorFactory() {
////			private ILinkPointDefinition linkDefnCopy;
////			
////			@Override
////			public IAnchorLocator createAnchorLocator(Envelope newBounds) {
////				ILinkAttribute linkAtt = anchorNodeAttribute.getAssociatedTerminus().getOwningLink();
////				ILinkPointDefinition linkDefnCopy = new LinkPointDefinition(linkAtt);
////				linkDefnCopy.changeEnvelope(newBounds);
////				return new Link2LinkAnchorLocator(linkDefnCopy);
////			}
////			
////			@Override
////			public IAnchorLocator createAnchorLocator() {
////				return new Link2LinkAnchorLocator(linkDefnCopy);
////			}
////
////		};
//	}

//	@Override
//	public Point getAnchorReferencePoint(Point originalRefPoint) {
//		ILinkAttribute linkAtt = this.anchorNodeAttribute.getAssociatedCurveSegment().getOwningLink();
//		ILinkController parentLinkController = this.getViewModel().getLinkController(new LinkEdgeFacade(linkAtt.getCurrentElement()));
//		return parentLinkController.getAnchorReferencePoint(originalRefPoint);
////		IAnchorLocator locator = new Link2LinkAnchorLocator(this.anchorNodeAttribute.getAssociatedTerminus().getOwningLink());
////		locator.setOtherEndPoint(originalRefPoint);
////		return locator.calcAnchorPosition();
//	}

	
	@Override
	public Envelope getBounds() {
		return getDrawnBounds();
	}

	@Override
	public IConvexHull getConvexHull() {
		return new RectangleHull(this.anchorNodeAttribute.getBounds());
	}

	@Override
	public IDrawingNode getDrawingElement() {
		return new DrawingNodeFacade((ICompoundNode)this.anchorNodeAttribute.getCurrentElement());
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return this.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		return false;
	}

}
