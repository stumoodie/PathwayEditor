package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.LinkCreationCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkCreationOperation implements ILinkCreationOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private ILinkObjectType linkObjectType;
	private boolean canCreationSucceed = false;
	private final IViewControllerModel viewModel;
	private IFeedbackNode srcShape;
	private IFeedbackNode tgtShape;
	private IFeedbackLink currentEdge;
	private IShapeNode srcNode;

	public LinkCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel, IViewControllerModel viewModel, ICommandStack commandStack) {
		this.shapePane = shapePane;
		this.viewModel = viewModel;
		this.feedbackModel = feedbackModel;
		this.commandStack = commandStack;
	}

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
		IShapeController potentialTargetNode = findTopPermittedController(delta);
		if(potentialTargetNode != null){
			ILinkPointDefinition linkDefn = this.currentEdge.getLinkDefinition();
			IAnchorLocator tgtLocator = potentialTargetNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
			tgtLocator.setOtherEndPoint(linkDefn.getSrcAnchorPosition());
			//TODO: Deal with self edge
			ICommand cmd = new LinkCreationCommand(srcNode, potentialTargetNode.getDrawingElement(), this.linkObjectType, linkDefn.getSrcAnchorPosition(), tgtLocator.calcAnchorPosition());
			this.commandStack.execute(cmd);
			if(logger.isDebugEnabled()){
				logger.debug("Create a new shape at: " + cmd);
			}
		}
		this.feedbackModel.clear();
		this.shapePane.updateView();
		this.srcShape = null;
		this.srcNode = null;
		this.tgtShape = null;
		this.currentEdge = null;
		this.canCreationSucceed = false;
	}
	
	private IShapeController findTopPermittedController(Point delta){
		IIntersectionCalculator intnCalc = this.viewModel.getIntersectionCalculator();
		intnCalc.setFilter(new IIntersectionCalcnFilter() {
			@Override
			public boolean accept(IDrawingElementController node) {
				return node instanceof IShapeController;
			}
		});
		SortedSet<IDrawingElementController> hits = intnCalc.findDrawingPrimitivesAt(this.currentEdge.getLinkDefinition().getSrcAnchorPosition().translate(delta));
		IShapeController retVal = null;
		if(!hits.isEmpty()){
			IShapeController potentialTargetNode = (IShapeController)hits.first();
			if(this.linkObjectType.getLinkConnectionRules().isValidTarget(srcNode.getAttribute().getObjectType(), potentialTargetNode.getDrawingElement().getAttribute().getObjectType())){
				retVal = potentialTargetNode;
			}
		}
		return retVal;
	}

	@Override
	public void ongoingCreationDrag(Point delta) {
		this.tgtShape.translatePrimitive(delta);
		this.shapePane.updateView();
	}
	
	@Override
	public void startCreationDrag(IShapeNode hitNode) {
		this.canCreationSucceed = false;
		feedbackModel.clear();
		if(this.linkObjectType.getLinkConnectionRules().isValidSource(hitNode.getAttribute().getObjectType())){
			this.srcNode = hitNode;
			this.srcShape = this.feedbackModel.getFeedbackNodeBuilder().createFromDrawingNodeAttribute(hitNode.getAttribute());
			this.tgtShape = this.feedbackModel.getFeedbackNodeBuilder().createDefaultNode(new Envelope(hitNode.getAttribute().getBounds().getCentre(), new Dimension(1.0, 1.0)));
			currentEdge = feedbackModel.getFeedbackLinkBuilder().createFromObjectType(srcShape, tgtShape, linkObjectType);
			this.shapePane.updateView();
			if(logger.isTraceEnabled()){
				logger.trace("Starting create link drag. link=" + currentEdge);
			}
		}
	}


	@Override
	public boolean canCreationSucceed() {
		return this.canCreationSucceed ;
	}
	
}
