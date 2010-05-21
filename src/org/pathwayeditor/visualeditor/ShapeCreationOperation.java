package org.pathwayeditor.visualeditor;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ShapeCreationCommand;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;

public class ShapeCreationOperation implements IShapeCreationOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IModel viewModel;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private IShapeObjectType shapeObjectType;
	private Point originDelta;
	private Dimension sizeDelta;
	private Point startLocation;

	public ShapeCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel, IModel viewModel, ICommandStack commandStack) {
		this.shapePane = shapePane;
		this.viewModel = viewModel;
		this.feedbackModel = feedbackModel;
		this.commandStack = commandStack;
	}

	@Override
	public void createShape(Point origin) {
		Envelope bounds = new Envelope(origin, this.shapeObjectType.getDefaultAttributes().getSize());
		ICommand cmd = new ShapeCreationCommand(viewModel.getRootNode(), this.shapeObjectType, bounds);
		this.commandStack.execute(cmd);
		if(logger.isDebugEnabled()){
			logger.debug("Create a new shape at: " + cmd);
		}
		this.shapePane.updateView();
	}

	@Override
	public IShapeObjectType getShapeObjectType() {
		return this.shapeObjectType;
	}

	@Override
	public void setShapeObjectType(IShapeObjectType shapeType) {
		this.shapeObjectType = shapeType;
	}

	@Override
	public void finishCreationDrag(Point newLocation) {
		calculateBounds(newLocation);
		ICommand cmd = new ShapeCreationCommand(viewModel.getRootNode(), this.shapeObjectType, new Envelope(originDelta, sizeDelta));
		this.commandStack.execute(cmd);
		if(logger.isDebugEnabled()){
			logger.debug("Create a new shape at: " + cmd);
		}
		this.shapePane.updateView();
		this.feedbackModel.clear();
		this.shapePane.updateView();
	}

	@Override
	public void ongoingCreationDrag(Point newLocation) {
		calculateBounds(newLocation);
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		this.shapePane.updateView();
		if(logger.isTraceEnabled()){
			logger.trace("Create shape drag. newLocation=" + newLocation + ", feedbackBounds=" + node.getBounds());
		}
	}

	@Override
	public void startCreationDrag(Point origin) {
		this.startLocation = origin;
		feedbackModel.clear();
		IFeedbackNode node = feedbackModel.createSingleNode(new Envelope(origin, new Dimension(0.1, 0.1)));
		node.setFillColour(RGB.BLUE);
		node.setLineColour(RGB.RED);
		node.setLineStyle(LineStyle.SOLID);
		node.setLineWidth(1.0);
		this.shapePane.updateView();
		if(logger.isTraceEnabled()){
			logger.trace("Starting create shape drag. origin=" + origin);
		}
	}

	private void calculateBounds(Point currentLocation){
		double originX = 0.0;
		double originY = 0.0;
		Point delta = this.calculateLocationDelta(currentLocation);
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
	
	protected final Point calculateLocationDelta(Point newLocation){
		return this.startLocation.difference(newLocation);
	}
	
}
