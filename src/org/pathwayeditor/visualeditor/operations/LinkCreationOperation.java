package org.pathwayeditor.visualeditor.operations;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.LinkCreationCommand;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkCreationOperation implements ILinkCreationOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private ILinkObjectType linkObjectType;
	private IFeedbackLink currentEdge;
	private IShapeController srcNode;
	private IAnchorLocator srcAnchorLocator;
	private boolean creationStarted;
	private IShapeController potentialTargetNode;

	public LinkCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ICommandStack commandStack) {
		this.shapePane = shapePane;
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
	public void finishCreation() {
		if(potentialTargetNode != null){
			ILinkPointDefinition linkDefn = this.currentEdge.getLinkDefinition();
			IAnchorLocator tgtLocator = potentialTargetNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
			tgtLocator.setOtherEndPoint(linkDefn.getSrcAnchorPosition());
			//TODO: Deal with self edge
			ICommand cmd = new LinkCreationCommand(srcNode.getDrawingElement(), potentialTargetNode.getDrawingElement(), this.linkObjectType, linkDefn.getSrcAnchorPosition(), tgtLocator.calcAnchorPosition());
			this.commandStack.execute(cmd);
			if(logger.isDebugEnabled()){
				logger.debug("Create a new shape at: " + cmd);
			}
		}
		this.feedbackModel.clear();
		this.shapePane.updateView();
		this.srcNode = null;
		this.potentialTargetNode = null;
		this.currentEdge = null;
		this.creationStarted = false;
	}
	
//	private IShapeController findTopPermittedController(Point delta){
//		IIntersectionCalculator intnCalc = this.viewModel.getIntersectionCalculator();
//		intnCalc.setFilter(new IIntersectionCalcnFilter() {
//			@Override
//			public boolean accept(IDrawingElementController node) {
//				return node instanceof IShapeController;
//			}
//		});
//		SortedSet<IDrawingElementController> hits = intnCalc.findDrawingPrimitivesAt(this.currentEdge.getLinkDefinition().getSrcAnchorPosition().translate(delta));
//		IShapeController retVal = null;
//		if(!hits.isEmpty()){
//			IShapeController potentialTargetNode = (IShapeController)hits.first();
//			if(this.linkObjectType.getLinkConnectionRules().isValidTarget(srcNode.getAttribute().getObjectType(), potentialTargetNode.getDrawingElement().getAttribute().getObjectType())){
//				retVal = potentialTargetNode;
//			}
//		}
//		return retVal;
//	}
	
	@Override
	public void creationOngoing(Point newPosition) {
		feedbackModel.clear();
		if(this.srcNode.equals(this.potentialTargetNode)){
			if(logger.isTraceEnabled()){
				logger.trace("Not Drawing link. Posn=" + newPosition);
			}
			this.currentEdge = null;
		}
		else if(this.canFinishCreation()){
			Point oldSrcPosn = null;
			Point srcPosn = this.srcAnchorLocator.calcAnchorPosition();
			Point oldTgtPosn = null;
			Point tgtPosn = newPosition;
			IAnchorLocator tgtAnchorLocator = this.potentialTargetNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
			while(!srcPosn.equals(oldSrcPosn) && !tgtPosn.equals(oldTgtPosn)){
				this.srcAnchorLocator.setOtherEndPoint(newPosition);
				oldSrcPosn = srcPosn;
				srcPosn = this.srcAnchorLocator.calcAnchorPosition();
				tgtAnchorLocator.setOtherEndPoint(srcPosn);
				oldTgtPosn = tgtPosn;
				tgtPosn = tgtAnchorLocator.calcAnchorPosition();
			}
			currentEdge = feedbackModel.getFeedbackLinkBuilder().createNodelessLinkFromObjectType(srcPosn, tgtPosn, linkObjectType);
			if(logger.isTraceEnabled()){
				logger.trace("Drawing link from shape to shape. Link=" + currentEdge);
			}
		}
		else{
			this.srcAnchorLocator.setOtherEndPoint(newPosition);
			Point srcPosn = this.srcAnchorLocator.calcAnchorPosition();
			currentEdge = feedbackModel.getFeedbackLinkBuilder().createNodelessLinkFromObjectType(srcPosn, newPosition, linkObjectType);
			if(logger.isTraceEnabled()){
				logger.trace("Drawing link=" + currentEdge);
			}
		}
		this.shapePane.updateView();
	}
	
	@Override
	public void startCreation() {
		this.creationStarted = true;
		feedbackModel.clear();
		this.srcAnchorLocator = this.srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		this.shapePane.updateView();
	}

	@Override
	public void setPotentialTarget(IShapeController potentialTarget) {
		this.potentialTargetNode = potentialTarget;
	}

	@Override
	public boolean isLinkCreationStarted() {
		return this.creationStarted;
	}

	@Override
	public boolean canFinishCreation() {
		return this.srcNode != null && this.potentialTargetNode != null && this.linkObjectType != null &&
			this.linkObjectType.getLinkConnectionRules().isValidTarget(this.srcNode.getDrawingElement().getAttribute().getObjectType(),
					this.potentialTargetNode.getDrawingElement().getAttribute().getObjectType());
	}

	@Override
	public void setPotentialSourceNode(IShapeController potentialSource) {
		this.srcNode = potentialSource;
	}

	@Override
	public boolean canStartCreation() {
		return this.srcNode != null	&& this.linkObjectType != null &&
			this.linkObjectType.getLinkConnectionRules().isValidSource(this.srcNode.getDrawingElement().getAttribute().getObjectType());
	}


}
