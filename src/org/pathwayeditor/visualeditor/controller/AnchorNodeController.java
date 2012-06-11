package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICurveSegmentChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICurveSegmentContainerEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICurveSegmentContainerListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICurveSegmentLocationChangeEvent;
import org.pathwayeditor.businessobjects.impl.facades.DrawingNodeFacade;
import org.pathwayeditor.businessobjects.impl.facades.LinkEdgeFacade;
import org.pathwayeditor.businessobjects.impl.facades.SubModelFacade;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.figure.rendering.GenericFont;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.editingview.FigureDefinitionMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundNode;

public class AnchorNodeController extends DrawingElementController implements IAnchorNodeController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IAnchorNodeAttribute anchorNodeAttribute;
	private final AnchorNodeFigureControllerHelper helper;
	private final ICanvasAttributeChangeListener anchorNodePropertyChangeListener;
	private ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	private boolean isActive;
	private final ICurveSegmentChangeListener associatedCurveChangeListener;
	private final ICurveSegmentContainerListener curveSegmentChangeListener;

	
	public AnchorNodeController(IViewControllerModel viewController, IAnchorNodeAttribute lanchorNodeAttribute, int index) {
		super(viewController, index);
		this.anchorNodeAttribute = lanchorNodeAttribute;
		this.helper  = new AnchorNodeFigureControllerHelper(anchorNodeAttribute);
		this.helper.createFigureController();
		anchorNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					helper.getFigureController().setLineColour((Colour)e.getNewValue());
					helper.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					helper.getFigureController().setFillColour((Colour)e.getNewValue());
					helper.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FONT_COLOUR)){
					helper.getFigureController().setFontColour((Colour)e.getNewValue());
					helper.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FONT)){
					helper.getFigureController().setFont((GenericFont)e.getNewValue());
					helper.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					helper.getFigureController().setLineWidth(newLineWidth);
					helper.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					IAnchorNodeAttribute attribute = (IAnchorNodeAttribute)e.getAttribute();
					Envelope oldDrawnBounds = helper.getFigureController().getConvexHull().getEnvelope();
					if(logger.isTraceEnabled()){
						logger.trace("Detected shape bound change event. Recalculating figure defn bounds. Att=" + attribute + ",newBounds=" + attribute.getBounds());
					}
					helper.getFigureController().setEnvelope(attribute.getBounds());
					helper.refreshGraphicalAttributes();
//					recalculateSrcLinks();
//					recalculateTgtLinks();
					if(logger.isTraceEnabled()){
						logger.trace("Notifying this controller of bounds change");
					}
					notifyDrawnBoundsChanged(oldDrawnBounds, helper.getFigureController().getConvexHull().getEnvelope());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IAnchorNodeAttribute attribute = (IAnchorNodeAttribute)e.getAttribute();
					helper.getFigureController().setLineStyle(attribute.getLineStyle());
					helper.refreshGraphicalAttributes();
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
//				if(logger.isTraceEnabled()){
//					logger.trace("Detected translation att=" + anchorNodeAttribute + ", delta=" + e.getTranslationDelta());
//				}
//				anchorNodeAttribute.translate(e.getTranslationDelta());
			}
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.associatedCurveChangeListener = new ICurveSegmentChangeListener() {
			
			@Override
			public void locationChange(ICurveSegmentLocationChangeEvent e) {
				if(logger.isTraceEnabled()){
					logger.trace("Detected associatedSeg move. Old seg posns=" + e.getOldPosition() + ", Old anchorPosn=" + anchorNodeAttribute.getAnchorLocation());
				}
				AnchorPointChangeCalculator calc = new AnchorPointChangeCalculator(e.getOldPosition(), e.getNewPosition());
				calc.setAnchorPosn(getConvexHull().getCentre());
				ICurveSegment curveSeg = getAssociatedCurveSegment();
				curveSeg.visit(calc);
				anchorNodeAttribute.setAnchorLocation(calc.getNewAnchorPosn());
				if(logger.isTraceEnabled()){
					logger.trace("Calculated new anchor posn. New seg posns=" + e.getNewPosition() + ", New anchorPosn=" + anchorNodeAttribute.getAnchorLocation());
				}
			}
		};
		this.curveSegmentChangeListener = new ICurveSegmentContainerListener() {
			
			@Override
			public void curveSegmentsReplaced(ICurveSegmentContainerEvent e) {
				ICurveSegment currSeg = getAssociatedCurveSegment();
				for(ICurveSegment cs : e.getOriginalSegments()){
					if(cs.equals(currSeg)){
						//TODO: need to work out how to replace this seg with a new one
						logger.info("This is where I work out which line seg to replace the old one with");
					}
				}
			}
		};
		this.isActive = false;
	}

	private ILinkAttribute getParentAttribute(){
		return (ILinkAttribute)this.anchorNodeAttribute.getCurrentElement().getParent().getAttribute();
	}
	
	private ICurveSegment getAssociatedCurveSegment(){
		return this.anchorNodeAttribute.getAssociatedCurveSegment();
	}
	
	@Override
	public IMiniCanvas getMiniCanvas() {
		IFigureRenderingController renderingController = this.helper.getFigureController();
		return new FigureDefinitionMiniCanvas(renderingController.getFigureDefinition(), renderingController.getEnvelope());
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.helper.getFigureController().getEnvelope();
	}

	private void addListeners() {
		anchorNodeAttribute.addChangeListener(anchorNodePropertyChangeListener);
		this.getParentAttribute().addChangeListener(parentDrawingNodePropertyChangeListener);
		this.getParentAttribute().getCurveSegmentContainer().addCurveSegmentContainerListener(this.curveSegmentChangeListener);
		this.getAssociatedCurveSegment().addCurveSegmentChangeListener(this.associatedCurveChangeListener);
	}
	
	private void removeListeners() {
		anchorNodeAttribute.removeChangeListener(anchorNodePropertyChangeListener);
		getParentAttribute().removeChangeListener(parentDrawingNodePropertyChangeListener);
		this.getParentAttribute().getCurveSegmentContainer().removeCurveSegmentContainerListener(this.curveSegmentChangeListener);
		this.getAssociatedCurveSegment().removeCurveSegmentChangeListener(this.associatedCurveChangeListener);
	}

	@Override
	public void activate() {
		addListeners();
		this.isActive = true;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void inactivate() {
		removeListeners();
		this.isActive = false;
	}


	@Override
	public boolean containsPoint(Point p) {
		return getConvexHull().containsPoint(p);
	}

	@Override
	public boolean intersectsBounds(Envelope otherBounds) {
		IConvexHull otherHull = new RectangleHull(otherBounds);
		return intersectsHull(otherHull);
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
		return this.helper.getFigureController().getConvexHull();
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
		boolean retVal = false;
		// algorithm is to find the intersecting shapes and then check if the
		// children and parents are in the intersection list
		Envelope newBounds = anchorNodeAttribute.getBounds().resize(originDelta, resizeDelta);
		if(logger.isTraceEnabled()){
			logger.trace("In can resize. New bounds = " + newBounds + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
		}
		if(newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0){
			ILinkController parentNode = this.getViewModel().getLinkController(new LinkEdgeFacade((ICompoundEdge)this.anchorNodeAttribute.getCurrentElement().getParent()));
			IIntersectionCalculator intCal = this.getViewModel().getIntersectionCalculator();
			intCal.setFilter(new IIntersectionCalcnFilter() {
				@Override
				public boolean accept(IDrawingElementController node) {
					return !(node instanceof ILabelController);
				}
			});
			SortedSet<IDrawingElementController> intersectingNodes = intCal.findIntersectingNodes(this.getConvexHull().changeEnvelope(newBounds), this);
			boolean parentIntersects = intersectingNodes.contains(parentNode);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with parent" + parentIntersects);
			}
			boolean childrenIntersect = childrenIntersect(this, intersectingNodes);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with children" + childrenIntersect);
			}
			retVal = parentIntersects && childrenIntersect;
		}
		return retVal;
	}

	private boolean childrenIntersect(IAnchorNodeController parentNode, SortedSet<IDrawingElementController> intersectingNodes){
		Iterator<ICompoundNode> iter = new SubModelFacade(parentNode.getDrawingElement().getGraphElement().getChildCompoundGraph()).shapeNodeIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			INodeController child = this.getViewModel().getNodeController(new DrawingNodeFacade(iter.next()));
			retVal = intersectingNodes.contains(child);
		}
		return retVal;
	}
	
	@Override
	public IFigureRenderingController getFigureController() {
		return this.helper.getFigureController();
	}

	@Override
	public IAnchorNodeAttribute getAssociatedAttribute() {
		return this.anchorNodeAttribute;
	}

}
