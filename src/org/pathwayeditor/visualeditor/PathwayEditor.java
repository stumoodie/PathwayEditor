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
import org.pathwayeditor.visualeditor.commands.ResizeNodeCommand;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
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
	
	public PathwayEditor(ICanvas boCanvas, int width, int height){
		this.selectionRecord = new SelectionRecord();
        this.commandStack = new CommandStack();
		viewModel = new ViewControllerStore(boCanvas.getModel());
		this.shapePane = new ShapePane(viewModel, this.selectionRecord);
		this.shapePane.setSize(width, height);
        IEditingOperation editOperation = new IEditingOperation(){

			@Override
			public void moveFinished(Point delta) {
				if(logger.isTraceEnabled()){
					logger.trace("Move finished. Delta=" + delta);
				}
				createMoveCommand(delta);
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
			}

			@Override
			public void copyFinished(Point delta) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void copyOngoing(Point delta) {
				// TODO Auto-generated method stub
				
			}

        };
        IResizeOperation resizeOperation = new IResizeOperation() {
			
			@Override
			public void resizeStarted() {
				logger.trace("Resize started");
			}
			
			@Override
			public void resizeFinished(Point originDelta, Dimension resizeDelta) {
				if(logger.isTraceEnabled()){
					logger.trace("Resize finished. originDelta=" + originDelta + ", dimDelta=" + resizeDelta);
				}
				createResizeCommand(originDelta, resizeDelta);
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
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
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
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
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
	
	
	private void createMoveCommand(Point delta){
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.translatePrimitive(delta);
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement(), delta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}
	
	private void moveSelection(Point delta) {
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.translatePrimitive(delta);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
//		this.currentCommand = cmpCommand;
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
