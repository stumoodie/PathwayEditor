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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.ResizeNodeCommand;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class ResizeOperation implements IResizeOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;

	public ResizeOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			ICommandStack commandStack){
		this.shapePane = shapePane;
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
	}
	
	@Override
	public void resizeStarted() {
		logger.trace("Resize started");
		feedbackModel.rebuildWithStrictSelection();
	}
	
	@Override
	public void resizeFinished(Point originDelta, Dimension resizeDelta) {
		if(logger.isTraceEnabled()){
			logger.trace("Resize finished. originDelta=" + originDelta + ", dimDelta=" + resizeDelta);
		}
		createResizeCommand(originDelta, resizeDelta);
		feedbackModel.clear();
		selectionRecord.restoreSelection();
		shapePane.updateView();
	}
	
	@Override
	public void resizeContinuing(Point originDelta, Dimension resizeDelta) {
		resizeSelection(originDelta, resizeDelta);
		shapePane.updateView();
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		return canContinueToResize(originDelta, resizeDelta);
	}

	private void createResizeCommand(Point originDelta, Dimension resizeDelta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new ResizeNodeCommand(nodePrimitive.getDrawingElement().getAttribute(), originDelta, resizeDelta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}

	private boolean canContinueToResize(Point originDelta, Dimension resizeDelta){
		boolean retVal = true;
		Iterator<INodeSelection> iter = this.selectionRecord.selectedNodeIterator();
		while(iter.hasNext() && retVal){
			INodeController nodeController = iter.next().getPrimitiveController();
			retVal = nodeController.canResize(originDelta, resizeDelta);
		}
		
		return retVal;
	}

	private void resizeSelection(Point originDelta, Dimension resizeDelta) {
		Iterator<IFeedbackNode> moveNodeIterator = this.feedbackModel.nodeIterator();
		while(moveNodeIterator.hasNext()){
			IFeedbackNode nodePrimitive = moveNodeIterator.next();
			if(logger.isTraceEnabled()){
				logger.trace("Resizing shape=" + nodePrimitive + "bounds=" + nodePrimitive.getBounds() + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
			}
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
		}
	}
}
