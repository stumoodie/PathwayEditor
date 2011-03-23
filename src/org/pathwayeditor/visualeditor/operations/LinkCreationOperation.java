package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

public class LinkCreationOperation implements ILinkCreationOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private ILinkObjectType linkObjectType;
	private Point originDelta;
	private Dimension sizeDelta;
//	private Point startLocation;
	private boolean canCreationSucceed = false;
	private final IViewControllerModel viewModel;
	private IFeedbackNode srcShape;
	private IFeedbackNode tgtShape;

	public LinkCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel, IViewControllerModel viewModel, ICommandStack commandStack) {
		this.shapePane = shapePane;
		this.viewModel = viewModel;
		this.feedbackModel = feedbackModel;
		this.commandStack = commandStack;
	}

//	@Override
//	public void createShape(Point origin) {
//		Envelope bounds = new Envelope(origin, this.shapeObjectType.getDefaultAttributes().getSize());
//		ICommand cmd = new ShapeCreationCommand(new RootNodeFacade(domainModel.getGraph().getRoot()), this.shapeObjectType, bounds);
//		this.commandStack.execute(cmd);
//		if(logger.isDebugEnabled()){
//			logger.debug("Create a new shape at: " + cmd);
//		}
//		this.shapePane.updateView();
//	}

	@Override
	public ILinkObjectType getLinkObjectType() {
		return this.linkObjectType;
	}

	@Override
	public void setLinkObjectType(ILinkObjectType linkType) {
		this.linkObjectType = linkType;
	}

	@Override
	public void finishCreationDrag(Point delta) {
//		calculateBounds(delta);
//		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
//		node.resizePrimitive(originDelta, sizeDelta);
//		IDrawingElementController potentialParent = getParentElement(node);
//		ICanvasElementAttribute drawingElementAtt = potentialParent.getDrawingElement().getAttribute();
//		if(drawingElementAtt.getObjectType().getParentingRules().isValidChild(getLinkObjectType())){
//			Envelope bounds = new Envelope(this.startLocation, new Dimension(0, 0)).resize(originDelta, sizeDelta);
//			ICommand cmd = new LinkCreationCommand(potentialParent.getDrawingElement(), this.linkObjectType, bounds);
//			this.commandStack.execute(cmd);
//			if(logger.isDebugEnabled()){
//				logger.debug("Create a new shape at: " + cmd);
//			}
//		}
//		
//		this.shapePane.updateView();
//		this.feedbackModel.clear();
//		this.shapePane.updateView();
	}

	@Override
	public void ongoingCreationDrag(Point delta) {
		IIntersectionCalculator intCalc = this.viewModel.getIntersectionCalculator();
		intCalc.setFilter(new IIntersectionCalcnFilter() {
			
			@Override
			public boolean accept(IDrawingElementController element) {
				return element instanceof IDrawingNode;
			}
		});
//		calculateBounds(delta);
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		this.shapePane.updateView();
		IDrawingElementController potentialParent = getParentElement(node);
		ICanvasElementAttribute drawingElementAtt = potentialParent.getDrawingElement().getAttribute();
		this.canCreationSucceed = drawingElementAtt.getObjectType().getParentingRules().isValidChild(getLinkObjectType());
		if(logger.isTraceEnabled()){
			logger.trace("Ongoing drag. newLocation=" + originDelta + ", sizeDelta=" + sizeDelta + ",creationSucceed=" + canCreationSucceed);
		}
	}
	
	private IDrawingElementController getParentElement(IFeedbackNode node){
		SortedSet<IDrawingElementController> intersections = viewModel.getIntersectionCalculator().findIntersectingNodes(node.getConvexHull());
		IDrawingElementController potentialParent = intersections.first();
		return potentialParent;
	}

	@Override
	public void startCreationDrag(IShapeNode hitNode) {
		this.canCreationSucceed = false;
//		this.startLocation = origin;
		feedbackModel.clear();
		this.srcShape = this.feedbackModel.getFeedbackNodeBuilder().createFromDrawingNodeAttribute(hitNode.getAttribute());
		this.tgtShape = this.feedbackModel.getFeedbackNodeBuilder().createDefaultNode(new Envelope(hitNode.getAttribute().getBounds().getCentre(), new Dimension(0, 0)));
//		IFeedbackLink edge = createFeedbackLink();
//		node.setFillColour(RGB.BLUE);
		this.shapePane.updateView();
		if(logger.isTraceEnabled()){
			logger.trace("Starting create link drag. src=" + hitNode);
		}
	}

//	private IFeedbackLink createFeedbackLink(){
//		IFeedbackLink retVal = new FeedbackLink(this.srcShape, this.tgtShape);
//		edge.setLineColour(RGB.RED);
//		edge.setLineStyle(LineStyle.SOLID);
//		edge.setLineWidth(1.0);
//		return retVal;
//	}
	
//	private void calculateBounds(Point delta){
//		double originX = 0.0;
//		double originY = 0.0;
//		double sizeX = delta.getX();
//		double sizeY = delta.getY();
//		if(delta.getX() < 0.0){
//			originX = delta.getX();
//			sizeX = -delta.getX();
//		}
//		if(delta.getY() < 0.0){
//			originY = delta.getY();
//			sizeY = -delta.getY();
//		}
//		this.originDelta = new Point(originX, originY);
//		this.sizeDelta = new Dimension(sizeX, sizeY);
//	}

	@Override
	public boolean canCreationSucceed() {
		return this.canCreationSucceed ;
	}
	
//	protected final Point calculateLocationDelta(Point newLocation){
//		return this.startLocation.difference(newLocation);
//	}
	
}
