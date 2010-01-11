package org.pathwayeditor.visualeditor.controller;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.graphicsengine.IShapePane;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;

public class MouseBehaviourController implements IMouseBehaviourController {
	private enum DragStatus { STARTED, FINISHED };

	private final Logger logger = Logger.getLogger(this.getClass());
	private MouseListener mouseSelectionListener;
	private MouseMotionListener mouseMotionListener;
	private KeyListener keyListener;
	private DragStatus dragStatus = DragStatus.FINISHED;
	private DragStatus keyDragStatus = DragStatus.FINISHED;
	private final INodeIntersectionCalculator intCalc;
	private IShapePane shapePane;
	private IEditingOperation moveOperation;
	private Point startPosition;
	private Point lastDelta = new Point(0,0);

	public MouseBehaviourController(IShapePane pane, IEditingOperation moveOp){
		this.shapePane = pane;
		this.moveOperation = moveOp;
        intCalc = new ShapeIntersectionCalculator(shapePane.getViewModel());
        this.mouseSelectionListener = new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					if(!e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							moveOperation.nodePrimarySelection(nodeController);
						}
						else{
							moveOperation.clearSelection();
						}
					}
					else if(e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							moveOperation.addSecondarySelection(nodeController);
						}
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
				
			}

			public void mouseExited(MouseEvent e) {
				
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				if(dragStatus == DragStatus.STARTED){
					dragStatus = DragStatus.FINISHED;
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					Point delta = startPosition.difference(location);
					moveOperation.moveFinished(delta);
				}
			}
        	
        };
        this.mouseMotionListener = new MouseMotionListener(){

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(dragStatus == DragStatus.FINISHED){
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(moveOperation.isNodeSelected(nodeController)){
							dragStatus = DragStatus.STARTED;
							moveOperation.moveStarted();
							logger.info("Starting dragging on: " + nodeController);
							startPosition = location;
						}
					}
					else {
						Point delta = startPosition.difference(location);
						moveOperation.moveOngoing(delta);
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
				INodePrimitive nodeController = findDrawingNodeAt(location);
				if(!(nodeController instanceof IRootPrimitive)){
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					logger.trace("Setting move cursor at position: " + location);
				}
				else{
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					logger.trace("Setting default cursor at position: " + location);
				}
			}
        	
        };
        this.keyListener = new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					handleKeyPress(1.0, 0.0);
				}
				else if(e.getKeyCode() == KeyEvent.VK_LEFT){
					handleKeyPress(-1.0, 0.0);
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP){
					handleKeyPress(0.0, -1.0);
				}
				else if(e.getKeyCode() == KeyEvent.VK_DOWN){
					handleKeyPress(0.0, 1.0);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_RIGHT ||
						e.getKeyCode() == KeyEvent.VK_LEFT ||
						e.getKeyCode() == KeyEvent.VK_UP ||
						e.getKeyCode() == KeyEvent.VK_DOWN){
					handleKeyRelease();
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
        	
        };
	}
	
	private void handleKeyRelease(){
		if(keyDragStatus.equals(DragStatus.STARTED)){
			keyDragStatus = DragStatus.FINISHED;
			moveOperation.moveFinished(lastDelta);
			lastDelta = null;
		}
	}
	
	private void handleKeyPress(double x, double y){
		if(keyDragStatus.equals(DragStatus.FINISHED)){
			keyDragStatus = DragStatus.STARTED;
			moveOperation.moveStarted();
			lastDelta = new Point(0,0);
		}
		Point delta = lastDelta.translate(x, y);
		moveOperation.moveOngoing(delta);
		lastDelta = delta;
	}
	
	private INodePrimitive findDrawingNodeAt(Point location) {
		SortedSet<INodePrimitive> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
		INodePrimitive retVal = hits.first();
		logger.info("Found hit at: " + retVal);
		return retVal;
	}

	public void initialise(){
		this.shapePane.addKeyListener(this.keyListener);
        this.shapePane.addMouseListener(this.mouseSelectionListener);
        this.shapePane.addMouseMotionListener(this.mouseMotionListener);
	}
}
