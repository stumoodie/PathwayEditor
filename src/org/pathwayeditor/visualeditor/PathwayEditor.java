package org.pathwayeditor.visualeditor;

import java.awt.Canvas;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.IResizeOperation;
import org.pathwayeditor.visualeditor.behaviour.MouseBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.commands.ReparentSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ResizeNodeCommand;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.feedback.FeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class PathwayEditor {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private final ShapePane shapePane;
	private IViewControllerStore viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;
	private ISelectionChangeListener selectionChangeListener;
	private final FeedbackModel feedbackModel;
	
	public PathwayEditor(ICanvas boCanvas, int width, int height){
        this.commandStack = new CommandStack();
		viewModel = new ViewControllerStore(boCanvas.getModel());
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(this.selectionRecord);
		this.shapePane = new ShapePane(viewModel, this.selectionRecord, this.feedbackModel);
		this.shapePane.setSize(width, height);
        IEditingOperation editOperation = new IEditingOperation(){

			@Override
			public void moveFinished(Point delta, ReparentingStateType reparentingState) {
				if(logger.isTraceEnabled()){
					logger.trace("Move finished. Delta=" + delta);
				}
				if(reparentingState.equals(ReparentingStateType.CAN_REPARENT)){
					createMoveCommand(delta, true);
				}
				else if(reparentingState.equals(ReparentingStateType.CAN_MOVE)){
					createMoveCommand(delta, false);
				}
//				else{
//					viewModel.synchroniseWithDomainModel();
//				}
				feedbackModel.clear();
				selectionRecord.clear();
				shapePane.repaint();
			}

			@Override
			public void moveOngoing(Point delta) {
				if(logger.isTraceEnabled()){
					logger.trace("Ongoning move. Delta=" + delta);
				}
				moveSelection(delta);
				shapePane.repaint();
			}

			@Override
			public void moveStarted() {
				logger.trace("Move started.");
				feedbackModel.rebuildIncludingHierarchy();
			}

			@Override
			public ReparentingStateType getReparentingState(Point delta) {
				ReparentingStateType retVal = ReparentingStateType.FORBIDDEN;
				CommonParentCalculator newParentCalc = new CommonParentCalculator(viewModel);
				newParentCalc.findCommonParent(selectionRecord.getGraphSelection(), delta);
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

        };
        IResizeOperation resizeOperation = new IResizeOperation() {
			
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
				selectionRecord.clear();
				shapePane.repaint();
			}
			
			@Override
			public void resizeContinuing(Point originDelta, Dimension resizeDelta) {
				resizeSelection(originDelta, resizeDelta);
				shapePane.repaint();
			}

			@Override
			public boolean canResize(Point originDelta, Dimension resizeDelta) {
				return canContinueToResize(originDelta, resizeDelta);
			}
		};
        this.editBehaviourController = new MouseBehaviourController(shapePane, editOperation, resizeOperation);
        this.selectionChangeListener = new ISelectionChangeListener() {
			
			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				shapePane.repaint();
			}
		}; 
	}
	
	private void resizeSelection(Point originDelta, Dimension resizeDelta) {
		Iterator<IFeedbackNode> moveNodeIterator = this.feedbackModel.nodeIterator();
		while(moveNodeIterator.hasNext()){
			IFeedbackNode nodePrimitive = moveNodeIterator.next();
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
			if(logger.isTraceEnabled()){
				logger.trace("Resizing shape to bounds: " + nodePrimitive.getBounds());
			}
		}
	}

	private void createResizeCommand(Point originDelta, Dimension resizeDelta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new ResizeNodeCommand(nodePrimitive.getDrawingElement(), originDelta, resizeDelta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}

	private boolean canContinueToResize(Point originDelta, Dimension resizeDelta){
		boolean retVal = true;
		Iterator<INodeSelection> iter = this.selectionRecord.selectedNodesIterator();
		while(iter.hasNext() && retVal){
			INodeController nodeController = iter.next().getPrimitiveController();
			retVal = nodeController.canResize(originDelta, resizeDelta);
//			if(nodeController instanceof IShapeController){
//				retVal = canContinueToResizeChild((IShapeController)nodeController);
//			}
		}
		
		return retVal;
	}
	
	
	private void createMoveCommand(Point delta, boolean reparentingEnabled){
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement(), delta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		if(reparentingEnabled){
			INodeController target = calculateReparentTarget(delta);
			ICommand cmd = new ReparentSelectionCommand(target.getDrawingElement().getCurrentDrawingElement(), this.selectionRecord.getGraphSelection());
			cmpCommand.addCommand(cmd);
		}
		this.commandStack.execute(cmpCommand);
	}
	
	private INodeController calculateReparentTarget(Point delta) {
		INodeController retVal = null;
		CommonParentCalculator newParentCalc = new CommonParentCalculator(viewModel);
		newParentCalc.findCommonParent(selectionRecord.getGraphSelection(), delta);
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

	
	private void moveSelection(Point delta) {
		Iterator<IFeedbackNode> moveNodeIterator = this.feedbackModel.nodeIterator();
		while(moveNodeIterator.hasNext()){
			IFeedbackNode nodePrimitive = moveNodeIterator.next();
			nodePrimitive.translatePrimitive(delta);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
	}
	
	public void initialise(){
		this.editBehaviourController.initialise();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
		this.shapePane.repaint();
	}

	public Canvas getCanvas() {
		return this.shapePane;
	}
	
}
