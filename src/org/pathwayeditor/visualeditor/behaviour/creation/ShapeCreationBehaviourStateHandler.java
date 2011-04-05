package org.pathwayeditor.visualeditor.behaviour.creation;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourOperationCompletionEvent;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourStateHandler;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
import org.pathwayeditor.visualeditor.behaviour.ISelectionStateBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourStateHandlerChangeListener;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class ShapeCreationBehaviourStateHandler implements IViewBehaviourStateHandler, MouseMotionListener, MouseListener {
	private final IDragResponse currDragResponse;
	private final IMouseFeedbackResponse currMouseFeedbackResponse;
	private final ISelectionStateBehaviourController mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	private boolean active;
	private final List<IViewBehaviourStateHandlerChangeListener> listeners;
	
	public ShapeCreationBehaviourStateHandler(ISelectionStateBehaviourController mouseBehaviourController, IDragResponse creationDragResponse,
			IMouseFeedbackResponse mouseFeedbackResponse) {
		this.mouseBehaviourController = mouseBehaviourController;
		this.currMouseFeedbackResponse = mouseFeedbackResponse;
		this.currDragResponse = creationDragResponse;
		this.listeners = new LinkedList<IViewBehaviourStateHandlerChangeListener>();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			Point location = this.mouseBehaviourController.getDiagramLocation();
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
				currDragResponse.dragStarted(location);
			}
		}
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
		IMouseFeedbackResponse currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
		Cursor feedbackCursor = currMouseFeedbackResponse.getCurrentCursor();
		if(logger.isTraceEnabled()){
			logger.trace("feedback cursor = " + feedbackCursor.getName());
		}
		e.getComponent().setCursor(feedbackCursor);
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
		currDragResponse.dragFinished();
		currMouseFeedbackResponse.reset();
		this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
		notifyComplete(true);
	}

	private void notifyComplete(final boolean successfulCompletion) {
		IViewBehaviourOperationCompletionEvent e = new IViewBehaviourOperationCompletionEvent(){

			@Override
			public boolean wasCompletedSuccessfully() {
				return successfulCompletion;
			}
			
		};
		for(IViewBehaviourStateHandlerChangeListener l : this.listeners){
			l.operationCompletionEvent(e);
		}
	}

	@Override
	public void activate(IShapePane shapePane) {
		shapePane.addMouseListener(this);
		shapePane.addMouseMotionListener(this);
		this.active = true;
	}

	@Override
	public void deactivate(IShapePane shapePane) {
		shapePane.removeMouseListener(this);
		shapePane.removeMouseMotionListener(this);
		this.active = false;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void addViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l) {
		this.listeners.remove(l);
	}

	@Override
	public List<IViewBehaviourStateHandlerChangeListener> getViewBehaviourStateHandlerChangeListener() {
		return new ArrayList<IViewBehaviourStateHandlerChangeListener>(this.listeners);
	}

}
