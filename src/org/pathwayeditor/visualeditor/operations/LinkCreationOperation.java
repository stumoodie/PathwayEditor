/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.operations;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.LinkCreationCommand;
import org.pathwayeditor.visualeditor.controller.IConnectingNodeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.ILinkDefinitionAnchorCalculator;
import org.pathwayeditor.visualeditor.geometry.LinkDefinitionAnchorCalculator;

public class LinkCreationOperation implements ILinkCreationOperation {
	private static final int MIN_NUM_SELF_EDGE_BPS = 2;
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private ILinkObjectType linkObjectType;
	private IFeedbackLink currentEdge;
	private IConnectingNodeController srcNode;
	private boolean creationStarted;
	private IConnectingNodeController potentialTargetNode;

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
			final ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(currentEdge.getLinkDefinition());
			anchorCalc.setSrcLocation(this.srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
			anchorCalc.setTgtLocation(this.potentialTargetNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
			anchorCalc.recalculateBothAnchors();
			ICommand cmd = new LinkCreationCommand(srcNode.getAssociatedAttribute(), potentialTargetNode.getAssociatedAttribute(), this.linkObjectType, currentEdge.getLinkDefinition());
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
		if(this.srcNode.equals(this.potentialTargetNode) && this.currentEdge == null){
			if(logger.isTraceEnabled()){
				logger.trace("Not Drawing link. Posn=" + newPosition);
			}
		}
		else if(this.canFinishCreation()){
			if(currentEdge == null){
				currentEdge = feedbackModel.getFeedbackLinkBuilder().createNodelessLinkFromObjectType(this.srcNode.getBounds().getCentre(), newPosition, linkObjectType);
			}
			ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(currentEdge.getLinkDefinition());
			anchorCalc.setSrcLocation(this.srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
			anchorCalc.setTgtLocation(this.potentialTargetNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
			anchorCalc.recalculateBothAnchors();
			if(logger.isTraceEnabled()){
				logger.trace("Drawing link from shape to shape. Link=" + currentEdge);
			}
		}
		else if(this.currentEdge != null && this.srcNode.equals(this.potentialTargetNode) && this.currentEdge.getLinkDefinition().numBendPoints() == 0){
			this.currentEdge = null;
			this.feedbackModel.clear();
		}
		else{
			if(currentEdge == null){
				currentEdge = feedbackModel.getFeedbackLinkBuilder().createNodelessLinkFromObjectType(this.srcNode.getBounds().getCentre(), newPosition, linkObjectType);
			}
			ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(currentEdge.getLinkDefinition());
			anchorCalc.setSrcLocation(this.srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
			currentEdge.getLinkDefinition().setTgtAnchorPosition(newPosition);
			anchorCalc.recalculateSrcAnchor();
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
		this.shapePane.updateView();
	}

	@Override
	public void setPotentialTarget(IConnectingNodeController potentialTarget) {
		this.potentialTargetNode = potentialTarget;
	}

	@Override
	public boolean isLinkCreationStarted() {
		return this.creationStarted;
	}

	@Override
	public boolean canFinishCreation() {
		return this.srcNode != null && this.potentialTargetNode != null && this.linkObjectType != null &&
			this.linkObjectType.getLinkConnectionRules().isValidTarget(this.srcNode.getAssociatedAttribute().getObjectType(),
					this.potentialTargetNode.getAssociatedAttribute().getObjectType()) &&
						(!this.srcNode.equals(this.potentialTargetNode) ||
								(this.srcNode.equals(this.potentialTargetNode) && this.currentEdge.getLinkDefinition().numBendPoints() >= MIN_NUM_SELF_EDGE_BPS));
	}

	@Override
	public void setPotentialSourceNode(IConnectingNodeController potentialSource) {
		this.srcNode = potentialSource;
	}

	@Override
	public boolean canStartCreation() {
		return this.srcNode != null	&& this.linkObjectType != null &&
			this.linkObjectType.getLinkConnectionRules().isValidSource(this.srcNode.getAssociatedAttribute().getObjectType());
	}

	@Override
	public boolean canCreateIntermediatePoint(Point intermediatePoint) {
		return this.creationStarted;
	}

	@Override
	public void createIntermediatePoint(Point intermediatePoint) {
		this.currentEdge.getLinkDefinition().addNewBendPoint(intermediatePoint);
		if(this.logger.isTraceEnabled()){
			this.logger.trace("Adding bendpoint at pt=" + intermediatePoint + ",linkDefn=" + this.currentEdge.getLinkDefinition());
		}
	}

	@Override
	public void cancel() {
		this.feedbackModel.clear();
		this.creationStarted = false;
		this.currentEdge = null;
		this.linkObjectType = null;
		this.srcNode = null;
		this.potentialTargetNode = null;
		this.shapePane.updateView();
	}


}
