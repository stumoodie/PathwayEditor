package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class DragListener implements MouseMotionListener, MouseListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private ISelectionHandle currSelectionHandle;
	private IDragResponse currDragResponse;
	private final IMouseBehaviourController mouseBehaviour;
	private IMouseFeedbackResponse currMouseFeedbackResponse;

	public DragListener(IMouseBehaviourController mouseBehaviour){
		this.mouseBehaviour = mouseBehaviour;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			Point location = mouseBehaviour.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(currDragResponse == null){
				currSelectionHandle = null;
				currSelectionHandle = mouseBehaviour.getSelectionRecord().findSelectionModelAt(location);
				if(currSelectionHandle != null){
					currDragResponse = this.mouseBehaviour.getDragResponse(currSelectionHandle.getType());
					currMouseFeedbackResponse = this.mouseBehaviour.getMouseFeedbackResponse(currSelectionHandle.getType());
				}
				else{
					currDragResponse = this.mouseBehaviour.getDragResponse(SelectionHandleType.None);
					currMouseFeedbackResponse = this.mouseBehaviour.getMouseFeedbackResponse(SelectionHandleType.None);
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
						else if(currDragResponse.canMove()){
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
//		Point location = this.mouseBehaviour.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
//		setCurrentCursorResponse(location);
//		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

	private void setCurrentCursorResponse(Point location){
		ISelectionHandle selectionModel = this.mouseBehaviour.getSelectionRecord().findSelectionModelAt(location);
		SelectionHandleType selectionRegion = selectionModel != null ? selectionModel.getType() : SelectionHandleType.None;
		currMouseFeedbackResponse = this.mouseBehaviour.getMouseFeedbackResponse(selectionRegion);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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
			this.mouseBehaviour.updateView();
		}
		Point location = this.mouseBehaviour.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
		setCurrentCursorResponse(location);
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

}
