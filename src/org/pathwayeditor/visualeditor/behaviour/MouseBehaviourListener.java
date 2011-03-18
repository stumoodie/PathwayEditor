package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class MouseBehaviourListener implements MouseListener, MouseMotionListener {
	private ISelectionHandle currSelectionHandle;
	private IDragResponse currDragResponse;
	private IMouseFeedbackResponse currMouseFeedbackResponse;
	private final ISelectionStateBehaviourController mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public MouseBehaviourListener(ISelectionStateBehaviourController mouseBehaviourController) {
		this.mouseBehaviourController = mouseBehaviourController;
	}

	private void setCurrentCursorResponse(Point location){
//		ISelectionHandle selectionModel = this.mouseBehaviourController.getSelectionRecord().findSelectionModelAt(location);
//		SelectionHandleType selectionRegion = selectionModel != null ? selectionModel.getType() : SelectionHandleType.None;
		ISelectionHandle selectionRegion = this.mouseBehaviourController.getSelectionHandle(location);
		SelectionHandleType selectionType = selectionRegion != null ? selectionRegion.getType() : SelectionHandleType.None;
		currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse(selectionType);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			Point location = mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(currDragResponse == null){
				currSelectionHandle = null;
				currSelectionHandle = mouseBehaviourController.getSelectionHandle(location);
				if(currSelectionHandle != null){
					currDragResponse = this.mouseBehaviourController.getDragResponse(currSelectionHandle.getType());
					currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse(currSelectionHandle.getType());
				}
				else{
					currDragResponse = this.mouseBehaviourController.getDragResponse(SelectionHandleType.None);
					currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse(SelectionHandleType.None);
				}
			}
			if(currDragResponse != null){
				if(currDragResponse.isDragOngoing()){
					if(currDragResponse.canContinueDrag(location)){
						currDragResponse.dragContinuing(location);
						if(currDragResponse.canReparent()){
							currMouseFeedbackResponse.changeState(StateType.REPARENTING);
							logger.trace("Setting hand cursor as reparenting enabled");
						}
						else if(currDragResponse.canOperationSucceed()){
							logger.trace("Can move, but cannot reparent. Setting to default for current location");
							currMouseFeedbackResponse.changeState(StateType.DEFAULT);
						}
						else{
							currMouseFeedbackResponse.changeState(StateType.FORBIDDEN);
							logger.trace("Move is forbidden");
						}
					}
				}
				else{
					currDragResponse.dragStarted(currSelectionHandle, location);
				}
			}
		}
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
		ISelectionHandle selectionHandle = this.mouseBehaviourController.getSelectionHandle(location);
		if(logger.isTraceEnabled()){
			logger.trace("Selection handle = " + selectionHandle);
		}
		SelectionHandleType selectionRegion = selectionHandle != null ? selectionHandle.getType() : SelectionHandleType.None;
		IMouseFeedbackResponse currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse(selectionRegion);
		if(logger.isTraceEnabled()){
			logger.trace("selection handle type = " + selectionRegion);
		}
		Cursor feedbackCursor = currMouseFeedbackResponse.getCurrentCursor();
		if(logger.isTraceEnabled()){
			logger.trace("feedback cursor = " + feedbackCursor.getName());
		}
		e.getComponent().setCursor(feedbackCursor);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
//			currSelectionHandle = mouseBehaviourController.getSelectionHandle(location);
			ISelectionResponse currSelnResponse = null;
			if(currSelectionHandle != null){
				currSelnResponse = this.mouseBehaviourController.getClickResponse();
			}
			else{
				currSelnResponse = this.mouseBehaviourController.getClickResponse();
			}
			if(!e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.primaryClick(location);
//				Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
//				IDrawingElementController nodeController = this.mouseBehaviourController.findDrawingElementAt(location);
//				if(nodeController != null){
//					this.mouseBehaviourController.getSelectionRecord().setPrimarySelection(nodeController);
//				}
//				else{
//					this.mouseBehaviourController.getSelectionRecord().clear();
//				}
			}
			else if(e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.secondaryClick(location);
//				Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
//				IDrawingElementController nodeController = this.mouseBehaviourController.findDrawingElementAt(location);
//				if(nodeController != null){
//					this.mouseBehaviourController.getSelectionRecord().addSecondarySelection(nodeController);
//				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(currDragResponse != null){
			currDragResponse.dragFinished();
			currMouseFeedbackResponse.reset();
			currDragResponse = null;
//			this.mouseBehaviour.updateView();
		}
		Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
		setCurrentCursorResponse(location);
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

}
