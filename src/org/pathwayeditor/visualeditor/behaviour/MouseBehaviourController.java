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
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.IShapePane;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse.CursorType;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class MouseBehaviourController implements IMouseBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private MouseListener mouseSelectionListener;
	private MouseMotionListener mouseMotionListener;
	private KeyListener keyListener;
	private final INodeIntersectionCalculator intCalc;
	private IShapePane shapePane;
	private Map<SelectionRegion, IDragResponse> dragResponseMap;
	private IKeyboardResponse keyboardResponseMap;
	private Map<SelectionRegion, IMouseFeedbackResponse> mouseResponseMap;
	private IDragResponse currDragResponse;

	public MouseBehaviourController(IShapePane pane, IEditingOperation moveOp, IResizeOperation resizeOp){
		this.shapePane = pane;
		this.dragResponseMap = new HashMap<SelectionRegion, IDragResponse>();
		this.mouseResponseMap = new HashMap<SelectionRegion, IMouseFeedbackResponse>();
		this.keyboardResponseMap = new KeyboardResponse(moveOp);
		initialiseDragResponses(moveOp, resizeOp);
		initialiseMouseResponse();
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
				if(currDragResponse != null){
					currDragResponse.dragFinished();
					currDragResponse = null;
					shapePane.repaint();
				}
				Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
				e.getComponent().setCursor(getCurrentCursorResponse(location));
			}
        	
        };
        this.mouseMotionListener = new MouseMotionListener(){

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(currDragResponse == null){
						ISelectionHandle selectionModel = shapePane.getSelectionRecord().findSelectionModelAt(location);
						if(selectionModel != null){
							currDragResponse = dragResponseMap.get(selectionModel.getRegion());
						}
					}
					if(currDragResponse != null){
						if(currDragResponse.isDragOngoing()){
							if(currDragResponse.canContinueDrag(location)){
								currDragResponse.dragContinuing(location);
								if(currDragResponse.canReparent()){
									e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
								}
								else{
									getCurrentCursorResponse(location);
								}
							}
						}
						else{
							currDragResponse.dragStarted(location);
						}
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
				e.getComponent().setCursor(getCurrentCursorResponse(location));
			}
        	
        };
        this.keyListener = new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					handleKeyPress(CursorType.Right);
				}
				else if(e.getKeyCode() == KeyEvent.VK_LEFT){
					handleKeyPress(CursorType.Left);
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP){
					handleKeyPress(CursorType.Up);
				}
				else if(e.getKeyCode() == KeyEvent.VK_DOWN){
					handleKeyPress(CursorType.Down);
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
	
//	private boolean canReparent(){
//		boolean retVal = false;
//		CommonParentCalculator newParentCalc = new CommonParentCalculator(this.shapePane.getViewModel());
//		newParentCalc.findCommonParent(this.shapePane.getSelectionRecord().getGraphSelection());
//        if(newParentCalc.hasFoundCommonParent()) {
//        	if(logger.isTraceEnabled()){
//        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
//        	}
//        	// parent is consistent - now we need to check if any node already has this parent
//        	// if all do then we move, in one or more doesn't then we fail reparenting
//        	retVal = newParentCalc.canReparentSelection();
//        }
//        else{
//        	logger.trace("No common parent found.");
//        }
//    	if(logger.isTraceEnabled()){
//    		logger.trace("Can reparent=" + retVal);
//    	}
//        return retVal;
//	}

	private Cursor getCurrentCursorResponse(Point location){
		ISelectionHandle selectionModel = shapePane.getSelectionRecord().findSelectionModelAt(location);
		SelectionRegion selectionRegion = selectionModel != null ? selectionModel.getRegion() : SelectionRegion.None;
		IMouseFeedbackResponse mouseFeedbackResponse = mouseResponseMap.get(selectionRegion);
		return Cursor.getPredefinedCursor(mouseFeedbackResponse.getCursorFeeback(location));
	}
	
	private void initialiseMouseResponse(){
		this.mouseResponseMap.put(SelectionRegion.Central, new MouseFeedbackResponse(SelectionRegion.Central));
		this.mouseResponseMap.put(SelectionRegion.N, new MouseFeedbackResponse(SelectionRegion.N));
		this.mouseResponseMap.put(SelectionRegion.NE, new MouseFeedbackResponse(SelectionRegion.NE));
		this.mouseResponseMap.put(SelectionRegion.E, new MouseFeedbackResponse(SelectionRegion.E));
		this.mouseResponseMap.put(SelectionRegion.SE, new MouseFeedbackResponse(SelectionRegion.SE));
		this.mouseResponseMap.put(SelectionRegion.S, new MouseFeedbackResponse(SelectionRegion.S));
		this.mouseResponseMap.put(SelectionRegion.SW, new MouseFeedbackResponse(SelectionRegion.SW));
		this.mouseResponseMap.put(SelectionRegion.W, new MouseFeedbackResponse(SelectionRegion.W));
		this.mouseResponseMap.put(SelectionRegion.NW, new MouseFeedbackResponse(SelectionRegion.NW));
		this.mouseResponseMap.put(SelectionRegion.None, new DefaultMouseFeedbackResponse());
	}
	
	private void initialiseDragResponses(IEditingOperation moveOp, IResizeOperation resizeOp) {
		this.dragResponseMap.put(SelectionRegion.Central, new CentralHandleResponse(moveOp));
		this.dragResponseMap.put(SelectionRegion.N, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(0.0, delta.getY());
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(0.0, -delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.NE, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(0.0, delta.getY());
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(delta.getX(), -delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.E, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(0.0, 0.0);
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(delta.getX(), 0.0);
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.SE, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(0.0, 0.0);
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(delta.getX(), delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.S, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(0.0, 0.0);
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(0.0, delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.SW, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(delta.getX(), 0.0);
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(-delta.getX(), delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.W, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(delta.getX(), 0.0);
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(-delta.getX(), 0.0);
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
		this.dragResponseMap.put(SelectionRegion.NW, new ResizeHandleResponse(new INewPositionCalculator() {
			private Point delta;
			
			@Override
			public Point getResizedOrigin() {
				return new Point(delta.getX(), delta.getY());
			}
			
			@Override
			public Dimension getResizedDelta() {
				return new Dimension(-delta.getX(), -delta.getY());
			}
			
			@Override
			public void calculateDeltas(Point newDelta) {
				this.delta = newDelta;
			}

			@Override
			public Point getLastDelta() {
				return this.delta;
			}
		}, resizeOp));
	}

	private void handleKeyRelease(){
		if(this.keyboardResponseMap.isKeyPressed()){
			this.keyboardResponseMap.cursorsKeyUp();
		}
	}
	
	private void handleKeyPress(CursorType cursorPressed){
		if(!this.keyboardResponseMap.isKeyPressed()){
			this.keyboardResponseMap.cursorKeyDown(cursorPressed);
		}
		else{
			this.keyboardResponseMap.cursorKeyStillDown(cursorPressed);
		}
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
