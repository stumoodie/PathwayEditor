package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.IShapePane;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

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
//	private IEditingOperation moveOperation;
	private Point startPosition;
	private Point lastDelta = new Point(0,0);
	private Map<SelectionRegion, IDragResponse> dragResponseMap;
	private IDragResponse currDragResponse;

	public MouseBehaviourController(IShapePane pane, IEditingOperation moveOp, IResizeOperation resizeOp){
		this.shapePane = pane;
//		this.moveOperation = moveOp;
		this.dragResponseMap = new HashMap<SelectionRegion, IDragResponse>();
		initialiseDragResponses(moveOp, resizeOp);
        intCalc = new ShapeIntersectionCalculator(shapePane.getViewModel());
        this.mouseSelectionListener = new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					if(!e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodeController nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							shapePane.getSelectionRecord().setPrimarySelection(nodeController);
						}
						else{
							shapePane.getSelectionRecord().clear();
						}
					}
					else if(e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodeController nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							shapePane.getSelectionRecord().addSecondarySelection(nodeController);
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
					if(currDragResponse != null){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						Point delta = startPosition.difference(location);
						currDragResponse.dragFinished(delta);
						currDragResponse = null;
					}
				}
			}
        	
        };
        this.mouseMotionListener = new MouseMotionListener(){

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(dragStatus == DragStatus.FINISHED){
						ISelectionHandle selectionModel = shapePane.getSelectionRecord().findSelectionModelAt(location);
						if(selectionModel != null){
							dragStatus = DragStatus.STARTED;
							startPosition = location;
							currDragResponse = dragResponseMap.get(selectionModel.getRegion());
						}
					}
					else {
						if(currDragResponse != null){
							Point delta = startPosition.difference(location);
							currDragResponse.dragContinuing(delta);
						}
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
				ISelectionHandle selectionModel = shapePane.getSelectionRecord().findSelectionModelAt(location);
				if (selectionModel != null) {
					if (SelectionRegion.Central.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting move cursor at position: " + location);
						}
					} else if (SelectionRegion.N.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting N resize cursor at position: " + location);
						}
					} else if (SelectionRegion.NE.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting NE resize cursor at position: " + location);
						}
					} else if (SelectionRegion.E.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting E resize cursor at position: " + location);
						}
					} else if (SelectionRegion.SE.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting SE resize cursor at position: " + location);
						}
					} else if (SelectionRegion.S.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting S resize cursor at position: " + location);
						}
					} else if (SelectionRegion.SW.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting SW resize cursor at position: " + location);
						}
					} else if (SelectionRegion.W.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting W resize cursor at position: " + location);
						}
					} else if (SelectionRegion.NW.equals(selectionModel.getRegion())) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
						if (logger.isTraceEnabled()) {
							logger.trace("Setting NW resize cursor at position: " + location);
						}
					}
				} else {
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
	
	private void initialiseDragResponses(IEditingOperation moveOp, IResizeOperation resizeOp) {
		this.dragResponseMap.put(SelectionRegion.Central, new CentralHandleResponse(moveOp));
		this.dragResponseMap.put(SelectionRegion.N, new NorthHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.E, new EastHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.S, new SouthHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.W, new WestHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.NE, new NorthEastHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.SE, new SouthEastHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.SW, new SouthWestHandleResponse(resizeOp));
		this.dragResponseMap.put(SelectionRegion.NW, new NorthWestHandleResponse(resizeOp));
	}

	private void handleKeyRelease(){
		if(keyDragStatus.equals(DragStatus.STARTED)){
			keyDragStatus = DragStatus.FINISHED;
			if(currDragResponse != null){
				currDragResponse.dragFinished(lastDelta);
				currDragResponse = null;
			}
			lastDelta = null;
		}
	}
	
	private void handleKeyPress(double x, double y){
		if(keyDragStatus.equals(DragStatus.FINISHED)){
			keyDragStatus = DragStatus.STARTED;
			currDragResponse = dragResponseMap.get(SelectionRegion.Central);
			currDragResponse.dragStarted();
			lastDelta = new Point(0,0);
		}
		Point delta = lastDelta.translate(x, y);
		currDragResponse.dragContinuing(delta);
		lastDelta = delta;
	}
	
	private INodeController findDrawingNodeAt(Point location) {
		SortedSet<INodeController> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
		INodeController retVal = hits.first();
		logger.info("Found hit at: " + retVal);
		return retVal;
	}

	public void initialise(){
		this.shapePane.addKeyListener(this.keyListener);
        this.shapePane.addMouseListener(this.mouseSelectionListener);
        this.shapePane.addMouseMotionListener(this.mouseMotionListener);
	}
}
