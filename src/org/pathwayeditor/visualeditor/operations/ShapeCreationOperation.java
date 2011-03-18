package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ShapeCreationCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;

public class ShapeCreationOperation implements IShapeCreationOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
//	private final IModel domainModel;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private IShapeObjectType shapeObjectType;
	private Point originDelta;
	private Dimension sizeDelta;
	private Point startLocation;
	private boolean canCreationSucceed = false;
	private final IViewControllerModel viewModel;

	public ShapeCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel, IViewControllerModel viewModel, ICommandStack commandStack) {
		this.shapePane = shapePane;
		this.viewModel = viewModel;
//		this.domainModel = viewModel.getDomainModel();
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
	public IShapeObjectType getShapeObjectType() {
		return this.shapeObjectType;
	}

	@Override
	public void setShapeObjectType(IShapeObjectType shapeType) {
		this.shapeObjectType = shapeType;
	}

	@Override
	public void finishCreationDrag(Point delta) {
		calculateBounds(delta);
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		IDrawingElementController potentialParent = getParentElement(node);
		ICanvasElementAttribute drawingElementAtt = potentialParent.getDrawingElement().getAttribute();
		if(drawingElementAtt.getObjectType().getParentingRules().isValidChild(getShapeObjectType())){
			Envelope bounds = new Envelope(this.startLocation, new Dimension(0, 0)).resize(originDelta, sizeDelta);
			ICommand cmd = new ShapeCreationCommand(potentialParent.getDrawingElement(), this.shapeObjectType, bounds);
			this.commandStack.execute(cmd);
			if(logger.isDebugEnabled()){
				logger.debug("Create a new shape at: " + cmd);
			}
		}
		
		this.shapePane.updateView();
		this.feedbackModel.clear();
		this.shapePane.updateView();
	}

	@Override
	public void ongoingCreationDrag(Point delta) {
		calculateBounds(delta);
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		this.shapePane.updateView();
		IDrawingElementController potentialParent = getParentElement(node);
		ICanvasElementAttribute drawingElementAtt = potentialParent.getDrawingElement().getAttribute();
		this.canCreationSucceed = drawingElementAtt.getObjectType().getParentingRules().isValidChild(getShapeObjectType());
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
	public void startCreationDrag(Point origin) {
		this.canCreationSucceed = false;
		this.startLocation = origin;
		calculateBounds(origin);
		feedbackModel.clear();
		IFeedbackNode node = feedbackModel.getFeedbackNodeBuilder().createFromDrawingNodeObjectType(this.shapeObjectType, new Envelope(origin, new Dimension(0.1, 0.1)));
//		node.setFillColour(RGB.BLUE);
		node.setLineColour(RGB.RED);
		node.setLineStyle(LineStyle.SOLID);
		node.setLineWidth(1.0);
		this.shapePane.updateView();
		if(logger.isTraceEnabled()){
			logger.trace("Starting create shape drag. origin=" + origin);
		}
	}

	private void calculateBounds(Point delta){
		double originX = 0.0;
		double originY = 0.0;
		double sizeX = delta.getX();
		double sizeY = delta.getY();
		if(delta.getX() < 0.0){
			originX = delta.getX();
			sizeX = -delta.getX();
		}
		if(delta.getY() < 0.0){
			originY = delta.getY();
			sizeY = -delta.getY();
		}
		this.originDelta = new Point(originX, originY);
		this.sizeDelta = new Dimension(sizeX, sizeY);
	}

	@Override
	public boolean canCreationSucceed() {
		return this.canCreationSucceed ;
	}
	
//	protected final Point calculateLocationDelta(Point newLocation){
//		return this.startLocation.difference(newLocation);
//	}
	
}
