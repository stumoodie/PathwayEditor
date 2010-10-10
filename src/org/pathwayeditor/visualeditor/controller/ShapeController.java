package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

public class ShapeController extends NodeController implements IShapeController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapeNode domainNode;
	private IDrawingNode parentAttribute;
	private final ICanvasAttributePropertyChangeListener shapePropertyChangeListener;
	private final IAnnotationPropertyChangeListener annotPropChangeListener;
	private IFigureController figureController;
	private final IDrawingNodeAttributeListener parentDrawingNodePropertyChangeListener;
	private boolean isActive;
	
	public ShapeController(IViewControllerModel viewModel, IShapeNode node, int index) {
		super(viewModel, index);
		
		this.domainNode = node;
		this.parentAttribute = this.domainNode.getParentNode();
		shapePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					figureController.setLineColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					figureController.setFillColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					figureController.setLineWidth(newLineWidth);
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.SIZE)
						|| e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					Envelope oldDrawnBounds = figureController.getConvexHull().getEnvelope();
					figureController.setRequestedEnvelope(attribute.getBounds());
					figureController.generateFigureDefinition();
					recalculateSrcLinks();
					recalculateTgtLinks();
					notifyDrawnBoundsChanged(oldDrawnBounds, figureController.getConvexHull().getEnvelope());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.setLineStyle(attribute.getLineStyle());
					figureController.generateFigureDefinition();
				}
			}
		};
		annotPropChangeListener = new IAnnotationPropertyChangeListener() {
			@Override
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				IAnnotationProperty prop = e.getPropertyDefinition();
				IShapeNode node = ((IShapeAttribute)prop.getOwner()).getCurrentDrawingElement();
				assignBindVariablesToProperties(node.getAttribute(), figureController);
				figureController.generateFigureDefinition();
			}	
		};
		parentDrawingNodePropertyChangeListener = new IDrawingNodeAttributeListener() {
			@Override
			public void nodeTranslated(IDrawingNodeAttributeTranslationEvent e) {
				domainNode.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(IDrawingNodeAttributeResizedEvent e) {
			}
		};
		this.figureController = createController(domainNode.getAttribute());
	}

	@Override
	public void activate(){
		addListeners(this.domainNode);
		this.isActive = true;
	}
	
	private void assignBindVariablesToProperties(IShapeAttribute att, final IFigureController figureController) {
		for(final String varName : figureController.getBindVariableNames()){
			if(att.containsProperty(varName)){
				IAnnotationProperty prop = att.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					@Override
					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureController.setBindBoolean(varName, prop.getValue());
					}

					@Override
					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureController.setBindInteger(varName, prop.getValue());
					}

					@Override
					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					@Override
					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					@Override
					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}

	private IFigureController createController(IShapeAttribute attribute){
//		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(attribute.getShapeDefinition());
//		compiler.compile();
//		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		IFigureController figureController = new FigureController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private void recalculateSrcLinks(){
		Iterator<ILinkEdge> edgeIter = this.domainNode.sourceLinkIterator();
		while(edgeIter.hasNext()){
			ILinkEdge link = edgeIter.next();
			ILinkController linkController = this.getViewModel().getLinkController(link);
			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(link.getSourceShape());
			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(link.getTargetShape());
			changeSourceAnchor(linkController, srcNode, tgtNode);
//			changeTargetAnchor(linkController, srcNode, tgtNode);
		}
	}

	private void recalculateTgtLinks(){
		Iterator<ILinkEdge> edgeIter = this.domainNode.targetLinkIterator();
		while(edgeIter.hasNext()){
			ILinkEdge link = edgeIter.next();
			ILinkController linkController = this.getViewModel().getLinkController(link);
			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(link.getSourceShape());
			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(link.getTargetShape());
//			changeSourceAnchor(linkController, srcNode, tgtNode);
			changeTargetAnchor(linkController, srcNode, tgtNode);
		}
	}

	public void changeSourceAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
		if(linkController.getLinkDefinition().numBendPoints() > 0){
			// there are bend-points so we use the bp as the reference position
			Point refPoint = linkController.getLinkDefinition().getSourceLineSegment().getTerminus();
			this.calculateSourceAnchor(linkController, srcNode, refPoint);
		}
		else{
			// otherwise we just use the centre positions of the shapes
			// as they will be after the move is completed
			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
		}
	}

	public void changeTargetAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
		if(linkController.getLinkDefinition().numBendPoints() > 0){
			// there are bend-points so we use the bp as the reference position
			Point refPoint = linkController.getLinkDefinition().getTargetLineSegment().getTerminus();
			this.calculateTargetAnchor(linkController, tgtNode, refPoint);
		}
		else{
			// otherwise we just use the centre positions of the shapes
			// as they will be after the move is completed
			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
		}
	}
	
	private void calculateSourceAnchor(ILinkController linkController, IShapeController srcNode, Point refPosn){
		Point newAnchorLocn = getRevisedAnchorPosition(srcNode, refPosn);
		linkController.getDrawingElement().getAttribute().getSourceTerminus().setLocation(newAnchorLocn);
		if(logger.isTraceEnabled()){
			logger.trace("Recalculating src anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
		}
	}
	
	private void calculateTargetAnchor(ILinkController linkController, IShapeController tgtNode, Point refPosn){
		Point newAnchorLocn = getRevisedAnchorPosition(tgtNode, refPosn);
		linkController.getDrawingElement().getAttribute().getTargetTerminus().setLocation(newAnchorLocn);
		if(logger.isTraceEnabled()){
			logger.trace("Recalculating tgt anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
		}
	}
	
	private Point getRevisedAnchorPosition(IShapeController nodeController, Point refPoint){
		IFigureController controller = nodeController.getFigureController();
		IAnchorLocator calc = controller.getAnchorLocatorFactory().createAnchorLocator();
		calc.setOtherEndPoint(refPoint);
		return calc.calcAnchorPosition();
	}
	
	
	private void addListeners(IShapeNode attribute) {
		attribute.getAttribute().addChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.getAttribute().propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.addChangeListener(annotPropChangeListener);
		}
		this.parentAttribute.getAttribute().addDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
	}
	
	private void removeListeners() {
		final IShapeAttribute attribute = this.domainNode.getAttribute();
		attribute.removeChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.removeChangeListener(annotPropChangeListener);
		}
		parentAttribute.getAttribute().removeDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
	}
		
	@Override
	public IShapeNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public IFigureController getFigureController() {
		return this.figureController;
	}

	@Override
	public Envelope getBounds() {
		return this.figureController.getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.figureController.getConvexHull();
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		boolean retVal = false;
		// algorithm is to find the intersecting shapes and then check if the
		// children and parents are in the intersection list
		Envelope newBounds = this.domainNode.getAttribute().getBounds().resize(originDelta, resizeDelta);
		if(logger.isTraceEnabled()){
			logger.trace("In can resize. New bounds = " + newBounds + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
		}
		if(newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0){
			INodeController parentNode = this.getViewModel().getNodeController(this.domainNode.getParentNode());
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

	private boolean childrenIntersect(IShapeController parentNode, SortedSet<IDrawingElementController> intersectingNodes){
		Iterator<IShapeNode> iter = parentNode.getDrawingElement().getSubModel().shapeNodeIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			INodeController child = this.getViewModel().getNodeController(iter.next());
			retVal = intersectingNodes.contains(child);
		}
		return retVal;
	}
	
	@Override
	public void inactivate() {
		removeListeners();
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.figureController.getEnvelope();
	}

	@Override
	public boolean containsPoint(Point p) {
		IConvexHull attributeHull = this.getConvexHull();
		boolean retVal = attributeHull.containsPoint(p); 
		if(logger.isTraceEnabled()){
			logger.trace("Testing contains node:" + this + ",retVal=" + retVal + ", hull=" + attributeHull + ", point=" + p);
		}
		return retVal;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return this.figureController.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public boolean intersectsBounds(Envelope otherBounds) {
		IConvexHull otherHull = new RectangleHull(otherBounds);
		return intersectsHull(otherHull);
	}
}
