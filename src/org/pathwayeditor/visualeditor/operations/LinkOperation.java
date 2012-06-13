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
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.commands.CreateBendPointCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.MoveBendPointCommand;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class LinkOperation implements ILinkOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
	private Point newBpStartPosn;

	public LinkOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			ICommandStack commandStack){
		this.shapePane = shapePane;
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
	}
	
	@Override
	public void moveBendPointFinished(ISelectionHandle bendPointHandle, Point delta) {
		if(logger.isTraceEnabled()){
			logger.trace("Move bendpoint finished. bpIdx=" + bendPointHandle.getHandleIndex() + ",position=" + delta);
		}
		createMoveBendPointCommand(bendPointHandle.getHandleIndex(), delta);
		Envelope refreshBounds = this.feedbackModel.uniqueFeedbackLink().getLinkDefinition().getBounds();
		feedbackModel.clear();
		selectionRecord.restoreSelection();
		shapePane.updateView(refreshBounds);
	}

	@Override
	public void moveBendPointOngoing(ISelectionHandle handle, Point delta) {
		if(logger.isTraceEnabled()){
			logger.trace("Move bendpoint ongoing. bpIdx=" + handle.getHandleIndex() + ",position=" + delta);
		}
		moveBendPoint(handle.getHandleIndex(), delta);
		shapePane.updateView(this.feedbackModel.uniqueFeedbackLink().getLinkDefinition().getBounds());
	}

	@Override
	public void moveBendPointStated(ISelectionHandle selectionHandle) {
		logger.trace("Move bendpoint started");
		feedbackModel.rebuildOnLinkSelection((ILinkSelection)selectionHandle.getSelection());
	}

	@Override
	public void newBendPointFinished(ISelectionHandle selectionHandle, Point delta) {
		if(logger.isTraceEnabled()){
			logger.trace("New bendpoint finished. lineSeg=" + selectionHandle.getHandleIndex() + ",position=" + delta);
		}
		createNewBendPointCommand(selectionHandle.getHandleIndex(), delta);
		Envelope refreshBounds = this.feedbackModel.uniqueFeedbackLink().getLinkDefinition().getBounds();
		feedbackModel.clear();
		selectionRecord.restoreSelection();
		shapePane.updateView(refreshBounds);
	}

	@Override
	public void newBendPointOngoing(ISelectionHandle handle, Point delta) {
		if(logger.isTraceEnabled()){
			logger.trace("Moving new bendpoint ongoing. bpIdx=" + handle.getHandleIndex() + ",position=" + delta);
		}
		moveBendPoint(handle.getHandleIndex(), delta);
		shapePane.updateView(this.feedbackModel.uniqueFeedbackLink().getLinkDefinition().getBounds());
	}

	@Override
	public void newBendPointStarted(ISelectionHandle selectionHandle) {
		logger.trace("New bendpoint started");
		feedbackModel.rebuildOnLinkSelection((ILinkSelection)selectionHandle.getSelection());
		IFeedbackLink feedbackLink = feedbackModel.uniqueFeedbackLink();
		Point handleOrigin = selectionHandle.getBounds().getOrigin();
		newBpStartPosn = handleOrigin.translate(selectionHandle.getBounds().getDimension().getWidth()/2.0,
				selectionHandle.getBounds().getDimension().getHeight()/2.0);
		feedbackLink.newBendPoint(selectionHandle.getHandleIndex(), newBpStartPosn);
	}

	private void moveBendPoint(int bendPointIdx, Point delta) {
		IFeedbackLink feedbackLink = this.feedbackModel.uniqueFeedbackLink();
		feedbackLink.moveBendPoint(bendPointIdx, delta);
	}

	private void createNewBendPointCommand(int lineSegmentIdx, Point delta) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection();
		Point bpLocation = this.newBpStartPosn.translate(delta);
		ICommand cmd = new CreateBendPointCommand(linkSelection.getPrimitiveController().getAssociatedAttribute().getBendPointContainer(), lineSegmentIdx, bpLocation);
		this.commandStack.execute(cmd);
	}

	private void createMoveBendPointCommand(int bpIdx, Point delta) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new MoveBendPointCommand(linkSelection.getPrimitiveController().getAssociatedAttribute().getBendPointContainer(), bpIdx, delta);
		this.commandStack.execute(cmd);
	}
}
