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
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IAnchorNodeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class EditingOperation implements IEditingOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IEditingOperation operationDelegate;
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommonParentCalculator newParentCalc;
	private final ICommandStack commandStack;
	private boolean copyOnMove;
	
	public EditingOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			ICommonParentCalculator newParentCalc, ICommandStack commandStack){
		this.shapePane = shapePane;
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.newParentCalc = newParentCalc;
		this.commandStack = commandStack;
	}
	
	@Override
	public void moveFinished(Point delta, ReparentingStateType reparentingState) {
		this.operationDelegate.setCopyOnMove(copyOnMove);
		this.operationDelegate.moveFinished(delta, reparentingState);
		shapePane.updateView();
	}

	@Override
	public void moveOngoing(Point delta) {
		this.operationDelegate.moveOngoing(delta);
		shapePane.updateView();
	}

	@Override
	public void moveStarted() {
		logger.trace("Move started.");
		if(this.selectionRecord.numSelected() == 1 && this.selectionRecord.getPrimarySelection().getPrimitiveController() instanceof IAnchorNodeController){
			this.operationDelegate = new AnchorNodeEditingOperationDelegate(feedbackModel, selectionRecord, commandStack);
		}
		else{
			this.operationDelegate = new DefaultEditingOperationDelegate(feedbackModel, selectionRecord, newParentCalc, commandStack);
		}
		this.operationDelegate.moveStarted();
	}

	@Override
	public ReparentingStateType getReparentingState(Point delta) {
		return this.operationDelegate.getReparentingState(delta);
	}

	@Override
	public void deleteSelection() {
		this.operationDelegate.deleteSelection();
		shapePane.updateView();
	}

	@Override
	public void setCopyOnMove(boolean isSelected) {
		this.copyOnMove = isSelected;
	}
}
