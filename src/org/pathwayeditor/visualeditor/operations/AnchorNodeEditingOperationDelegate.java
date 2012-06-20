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
import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.DeleteSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.feedback.IFeedbackElement;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.CurveSegmentAnchorCalculator;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class AnchorNodeEditingOperationDelegate implements IEditingOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
//	private EnvelopeBuilder refreshBoundsBuilder;
	private boolean copyOnMove;
	private Point lastPoint;
	private CurveSegmentAnchorCalculator calc;
	private IAnchorNodeAttribute anchorAtt;
	private Point origLocn;
	
	public AnchorNodeEditingOperationDelegate(IFeedbackModel feedbackModel, ISelectionRecord selectionRecord, ICommandStack commandStack){
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
	}
	
	@Override
	public void moveFinished(Point delta, ReparentingStateType reparentingState) {
		feedbackModel.clear();
		if(reparentingState.equals(ReparentingStateType.CAN_MOVE) && !this.copyOnMove){
			createAnchorNodeMoveCommand(delta);
			selectionRecord.restoreSelection();
		}
	}

	@Override
	public void moveOngoing(Point delta) {
		moveAnchorNode(delta);
	}

	private void moveAnchorNode(Point delta) {
		IFeedbackElement feedbackElement = this.feedbackModel.getFeedbackElement(selectionRecord.getPrimarySelection().getPrimitiveController());
		calc.setAnchorPoint(origLocn.translate(delta));
		Point point = calc.adjustAnchorOnCurveSegment();
		if(point == null){
			if(lastPoint == null){
				point = origLocn;
			}
			else{
				point = lastPoint;
			}
		}
		feedbackElement.translatePrimitive(origLocn.difference(point));
		lastPoint = point;
		if(logger.isTraceEnabled()){
			logger.trace("Dragged feedback element: " + feedbackElement + " constrained anchor posn move=" + point);
		}
	}

	@Override
	public void moveStarted() {
		logger.trace("Move started.");
		this.lastPoint = null;
		calc = new CurveSegmentAnchorCalculator();
		ISelection selection = this.selectionRecord.getPrimarySelection();
		anchorAtt = (IAnchorNodeAttribute)selection.getPrimitiveController().getAssociatedAttribute();
		calc.setCurveSegment(anchorAtt.getAssociatedCurveSegment());
		origLocn = anchorAtt.getAnchorLocation();
		feedbackModel.rebuildIncludingHierarchy();
	}

	@Override
	public ReparentingStateType getReparentingState(Point delta) {
		return ReparentingStateType.CAN_MOVE;
//		ReparentingStateType retVal = ReparentingStateType.FORBIDDEN;
//		calc.setAnchorPoint(origLocn.translate(delta));
//		Point point = calc.adjustAnchorOnCurveSegment();
//		// check if point is still on line segment
//        if(point != null && !this.copyOnMove){
//        	retVal = ReparentingStateType.CAN_MOVE;
//        }
//        else{
//        	logger.trace("Not on line segment.");
//        }
//    	if(logger.isTraceEnabled()){
//    		logger.trace("Reparent state=" + retVal);
//    	}
//        return retVal;
	}

	private void createAnchorNodeMoveCommand(Point delta){
		ICompoundCommand cmpCommand = new CompoundCommand();
		calc.setAnchorPoint(origLocn.translate(delta));
		Point point = calc.adjustAnchorOnCurveSegment();
		if(point == null){
			if(lastPoint == null){
				point = origLocn;
			}
			else{
				point = lastPoint;
			}
		}
		Point constrainedDelta = origLocn.difference(point);
		ICommand cmd = new MoveNodeCommand(anchorAtt, constrainedDelta);
		cmpCommand.addCommand(cmd);
		if(logger.isTraceEnabled()){
			logger.trace("Dragged anchor node to location: " + constrainedDelta);
		}
		this.commandStack.execute(cmpCommand);
	}

	@Override
	public void deleteSelection() {
		if(this.selectionRecord.numSelected() > 0){
			commandStack.execute(new DeleteSelectionCommand(selectionRecord.getEdgeIncludedSelection()));
			selectionRecord.clear();
		}
	}

	@Override
	public void setCopyOnMove(boolean isSelected) {
		this.copyOnMove = isSelected;
	}
}
