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
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.CopySelectionCommand;
import org.pathwayeditor.visualeditor.commands.DeleteSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveLinkCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.commands.ReparentSelectionCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.feedback.IFeedbackElement;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

public class DefaultEditingOperationDelegate implements IEditingOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommonParentCalculator newParentCalc;
	private final ICommandStack commandStack;
	private boolean copyOnMove;
	
	public DefaultEditingOperationDelegate(IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			ICommonParentCalculator newParentCalc, ICommandStack commandStack){
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.newParentCalc = newParentCalc;
		this.commandStack = commandStack;
	}
	
	@Override
	public void moveFinished(Point delta, ReparentingStateType reparentingState) {
		feedbackModel.clear();
		if(reparentingState.equals(ReparentingStateType.CAN_REPARENT)){
			if(this.copyOnMove){
				createCopyCommand(delta);
			}
			else{
				createMoveCommand(delta, true);
			}
			selectionRecord.restoreSelection();
		}
		else if(reparentingState.equals(ReparentingStateType.CAN_MOVE)){
			if(this.copyOnMove){
				createCopyCommand(delta);
			}
			else{
				createMoveCommand(delta, false);
			}
			selectionRecord.restoreSelection();
		}
	}

	private void createCopyCommand(Point delta) {
		IDrawingElementController target = calculateReparentTarget(delta);
		ICommand cmd = new CopySelectionCommand(target.getAssociatedAttribute().getCurrentElement(), this.selectionRecord.getSubgraphSelection().getDrawingElementSelection(), delta);
		this.commandStack.execute(cmd);
	}

	@Override
	public void moveOngoing(Point delta) {
		moveSelection(delta);
	}

	@Override
	public void moveStarted() {
		logger.trace("Move started.");
		feedbackModel.rebuildIncludingHierarchy();
	}

	@Override
	public ReparentingStateType getReparentingState(Point delta) {
		ReparentingStateType retVal = ReparentingStateType.FORBIDDEN;
		newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
        if(newParentCalc.hasFoundCommonParent()) {
        	if(logger.isTraceEnabled()){
        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
        	}
        	// parent is consistent - now we need to check if any node already has this parent
        	// if all do then we move, in one or more doesn't then we fail reparenting
        	if(newParentCalc.canReparentSelection()){
        		retVal = ReparentingStateType.CAN_REPARENT;
        	}
        	else if(newParentCalc.canMoveSelection()){
        		retVal = ReparentingStateType.CAN_MOVE;
        	}
        }
        else{
        	logger.trace("No common parent found.");
        }
    	if(logger.isTraceEnabled()){
    		logger.trace("Reparent state=" + retVal);
    	}
        return retVal;
	}

	private void moveSelection(Point delta) {
		ISubgraphSelection subgraphSelection = this.selectionRecord.getSubgraphSelection();
		Iterator<INodeSelection> moveNodeIterator = subgraphSelection.selectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			ISelection selection = moveNodeIterator.next();
			IFeedbackElement feedbackElement = this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			feedbackElement.translatePrimitive(delta);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged feedback element: " + feedbackElement);
			}
		}
		Iterator<ILinkSelection> moveLinkIterator = subgraphSelection.selectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkSelection selection = moveLinkIterator.next();
			IFeedbackLink feedbackLink = (IFeedbackLink)this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			for(int bpIdx = 0; bpIdx < feedbackLink.getLinkDefinition().numBendPoints(); bpIdx++){
				feedbackLink.translateBendPoint(bpIdx, delta);
				if(logger.isTraceEnabled()){
					logger.trace("Moved bendpont=" + bpIdx + " of feedback element: " + feedbackLink);
				}
			}
		}
	}

	private void createMoveCommand(Point delta, boolean reparentingEnabled){
		ICompoundCommand cmpCommand = new CompoundCommand();
		Iterator<ILinkSelection> moveLinkIterator = this.selectionRecord.getSubgraphSelection().topSelectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkAttribute nodePrimitive = moveLinkIterator.next().getPrimitiveController().getAssociatedAttribute();
			ICommand cmd = new MoveLinkCommand(nodePrimitive, delta);
			cmpCommand.addCommand(cmd);
		}
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getSubgraphSelection().topSelectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getAssociatedAttribute(), delta);
			cmpCommand.addCommand(cmd);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
			}
		}
		if(reparentingEnabled){
			IDrawingElementController target = calculateReparentTarget(delta);
			ICommand cmd = new ReparentSelectionCommand(target.getAssociatedAttribute().getCurrentElement(), this.selectionRecord.getSubgraphSelection().getDrawingElementSelection());
			cmpCommand.addCommand(cmd);
		}
		this.commandStack.execute(cmpCommand);
	}

	private IDrawingElementController calculateReparentTarget(Point delta) {
		IDrawingElementController retVal = null;
		newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
        if(newParentCalc.hasFoundCommonParent()) {
        	if(logger.isTraceEnabled()){
        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
        	}
        	// parent is consistent - now we need to check if any node already has this parent
        	// if all do then we move, in one or more doesn't then we fail reparenting
        	retVal = newParentCalc.getCommonParent();
        }
        else{
        	logger.trace("No common parent found.");
        }
    	if(logger.isTraceEnabled()){
    		logger.trace("Can reparent=" + retVal);
    	}
        return retVal;
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
