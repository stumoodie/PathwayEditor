package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;

public class CreationMouseListener implements MouseListener, MouseMotionListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapeCreationOperation creationOperation;
	private IMouseStateBehaviourController controller;
	private boolean dragMayBeStarting;
	private java.awt.Point possibleDragStartPosition;
	private boolean dragStarted;
	
	public CreationMouseListener(IMouseStateBehaviourController controller, IShapeCreationOperation creationOp) {
		this.creationOperation = creationOp;
		this.controller = controller;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		logger.trace("Mouse clicked at: " + e.getPoint());
		Point origin = controller.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
		creationOperation.createShape(origin);
		dragStarted = false;
		dragMayBeStarting = false; 
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		logger.trace("Mouse pressed at: " + e.getPoint());
		dragMayBeStarting = true; 
		possibleDragStartPosition = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		logger.trace("Mouse released at: " + e.getPoint());
		if(dragStarted){
			creationOperation.finishCreationDrag(controller.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY()));
		}
		dragStarted = false;
		dragMayBeStarting = false; 
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		logger.trace("Mouse dragged at: " + e.getPoint());
		if(this.dragMayBeStarting){
			creationOperation.startCreationDrag(controller.getAdjustedMousePosition(possibleDragStartPosition.getX(), possibleDragStartPosition.getY()));
			this.dragMayBeStarting = false;
		}
		creationOperation.ongoingCreationDrag(controller.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY()));
		dragStarted = true;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		logger.trace("Mouse moved at: " + e.getPoint());
		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

}
