package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse.CursorType;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.editingview.IDomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.ISelectionLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionStateController implements ISelectionStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
//	private MouseListener mouseSelectionListener;
	private final IShapePane shapePane;
	private final Map<SelectionHandleType, IDragResponse> dragResponseMap;
	private final KeyListener keyListener;
	private final IKeyboardResponse keyboardResponseMap;
	private final Map<SelectionHandleType, IMouseFeedbackResponse> mouseResponseMap;
	private final Map<SelectionHandleType, IPopupMenuResponse> popupMenuMap;
	private final MouseListener popupMenuListener;
	private boolean activated = false;
	private final SelectionMouseListener mouseListener;
	private final ISelectionResponse selectionResponse;

	public SelectionStateController(IShapePane pane, IOperationFactory opFactory){
		this.shapePane = pane;
		this.dragResponseMap = new HashMap<SelectionHandleType, IDragResponse>();
		this.mouseResponseMap = new HashMap<SelectionHandleType, IMouseFeedbackResponse>();
		this.keyboardResponseMap = new KeyboardResponse(opFactory.getMoveOperation());
		this.popupMenuMap = new HashMap<SelectionHandleType, IPopupMenuResponse>(); 
		initialiseDragResponses(opFactory);
		initialiseMouseResponse();
		initialisePopupMenuResponse(opFactory);
		this.mouseListener = new SelectionMouseListener(this);
        this.keyListener = new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
				logger.trace("Key press detected");
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
				logger.trace("Key release detected");
				if(e.getKeyCode() == KeyEvent.VK_RIGHT ||
						e.getKeyCode() == KeyEvent.VK_LEFT ||
						e.getKeyCode() == KeyEvent.VK_UP ||
						e.getKeyCode() == KeyEvent.VK_DOWN){
					handleKeyRelease();
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				logger.trace("Key type detected");
			}
        	
        };
        this.popupMenuListener = new MouseListener(){

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
				if(e.isPopupTrigger()){
					SelectionHandleType popupSelectionHandle = SelectionHandleType.None;
					Point location = getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
					ISelectionLayer selectionLayer = shapePane.getLayer(LayerType.SELECTION);
					IDrawingElementController nodeController = findDrawingElementAt(location);
					if(nodeController != null){
						if(!selectionLayer.getSelectionRecord().isNodeSelected(nodeController)){
							// not selected so select first before do anything else
							selectionLayer.getSelectionRecord().setPrimarySelection(nodeController);
						}
					}
					ISelectionHandle currSelectionHandle = selectionLayer.getSelectionRecord().findSelectionModelAt(location);
					if(currSelectionHandle != null){
						popupSelectionHandle = currSelectionHandle.getType();
					}
					IPopupMenuResponse response = popupMenuMap.get(popupSelectionHandle);
					JPopupMenu popup = response.getPopupMenu(currSelectionHandle);
					if(popup != null){
						popup.show((JPanel)shapePane, e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
        	
        };
        this.selectionResponse = new SelectionResponse(opFactory.getSelectionOperation());
	}
	

	@Override
	public Point getAdjustedMousePosition(double originalMouseX, double originalMouseY){
//		AffineTransform paneTransform = this.shapePane.getLastUsedTransform();
		Point retVal = this.shapePane.getPaneBounds().getOrigin();
//		if(paneTransform == null){
			retVal = retVal.translate(originalMouseX, originalMouseY);
//		}
//		else{
//			retVal = new Point((originalMouseX-paneTransform.getTranslateX())/paneTransform.getScaleX(), (originalMouseY-paneTransform.getTranslateY())/paneTransform.getScaleY()); 
//		}
		if(logger.isTraceEnabled()){
//			logger.trace("Adjust position. origX=" + originalMouseX + ",origY=" + originalMouseY + " : adjustedPoint=" + retVal + ", transform=" + paneTransform);
			logger.trace("Adjust position. origX=" + originalMouseX + ",origY=" + originalMouseY + " : adjustedPoint=" + retVal + ", paneBounds=" + shapePane.getPaneBounds());
		}
		return retVal;  
	}
	
	private void initialiseMouseResponse(){
		this.mouseResponseMap.put(SelectionHandleType.Central, new MouseFeedbackResponse(SelectionHandleType.Central));
		this.mouseResponseMap.put(SelectionHandleType.N, new MouseFeedbackResponse(SelectionHandleType.N));
		this.mouseResponseMap.put(SelectionHandleType.NE, new MouseFeedbackResponse(SelectionHandleType.NE));
		this.mouseResponseMap.put(SelectionHandleType.E, new MouseFeedbackResponse(SelectionHandleType.E));
		this.mouseResponseMap.put(SelectionHandleType.SE, new MouseFeedbackResponse(SelectionHandleType.SE));
		this.mouseResponseMap.put(SelectionHandleType.S, new MouseFeedbackResponse(SelectionHandleType.S));
		this.mouseResponseMap.put(SelectionHandleType.SW, new MouseFeedbackResponse(SelectionHandleType.SW));
		this.mouseResponseMap.put(SelectionHandleType.W, new MouseFeedbackResponse(SelectionHandleType.W));
		this.mouseResponseMap.put(SelectionHandleType.NW, new MouseFeedbackResponse(SelectionHandleType.NW));
		this.mouseResponseMap.put(SelectionHandleType.LinkMidPoint, new MouseFeedbackResponse(SelectionHandleType.LinkMidPoint));
		this.mouseResponseMap.put(SelectionHandleType.LinkBendPoint, new MouseFeedbackResponse(SelectionHandleType.LinkBendPoint));
		this.mouseResponseMap.put(SelectionHandleType.None, new DefaultMouseFeedbackResponse());
		this.mouseResponseMap.put(SelectionHandleType.Link, new DefaultMouseFeedbackResponse());
	}
	
	private void initialisePopupMenuResponse(IOperationFactory opFactory){
		this.popupMenuMap.put(SelectionHandleType.Central, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.N, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.NE, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.E, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.SE, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.S, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.SW, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.W, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.NW, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.Link, new LinkPopupMenuResponse(opFactory.getLinkPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.LinkMidPoint, new LinkPopupMenuResponse(opFactory.getLinkPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.LinkBendPoint, new LinkBendpointPopupMenuResponse(opFactory.getLinkBendpointPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.None, new DefaultPopupMenuResponse(opFactory.getDefaultPopupMenuResponse()));
	}
	
	private void initialiseDragResponses(IOperationFactory opFactory) {
		IEditingOperation moveOp = opFactory.getMoveOperation();
		IResizeOperation resizeOp = opFactory.getResizeOperation();
		ILinkOperation linkOp = opFactory.getLinkOperation();
		IMarqueeOperation marqueeOp = opFactory.getMarqueeOperation();
		this.dragResponseMap.put(SelectionHandleType.Central, new CentralHandleResponse(moveOp));
		this.dragResponseMap.put(SelectionHandleType.N, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.NE, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.E, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.SE, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.S, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.SW, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.W, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.NW, new ResizeHandleResponse(new INewPositionCalculator() {
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
		this.dragResponseMap.put(SelectionHandleType.LinkMidPoint, new LinkMidPointResponse(linkOp));
		this.dragResponseMap.put(SelectionHandleType.LinkBendPoint, new LinkBendPointResponse(linkOp));
		this.dragResponseMap.put(SelectionHandleType.None, new MarqueeSelectionHandleResponse(marqueeOp));
		this.dragResponseMap.put(SelectionHandleType.Link, new MarqueeSelectionHandleResponse(marqueeOp));
	}

//	@Override
	private IDrawingElementController findDrawingElementAt(Point location) {
		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
		IIntersectionCalculator intCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
		intCalc.setFilter(null);
		SortedSet<IDrawingElementController> hits = intCalc.findDrawingPrimitivesAt(new Point(location.getX(), location.getY()));
		IDrawingElementController retVal = null;
		if(!hits.isEmpty()){
			retVal = hits.first();
			if(logger.isTraceEnabled()){
				logger.trace("Found hit at: " + retVal);
			}
		}
		return retVal;
	}

	@Override
	public void activate(){
		this.shapePane.addKeyListener(this.keyListener);
//        this.shapePane.addMouseListener(this.mouseSelectionListener);
//        this.shapePane.addMouseMotionListener(this.dragListener);
//        this.shapePane.addMouseListener(this.dragListener);
//        this.shapePane.addMouseMotionListener(this.mouseFeedbackListener);
//        this.shapePane.addMouseListener(selectionFeedbackListener);
        this.shapePane.addMouseMotionListener(this.mouseListener);
        this.shapePane.addMouseListener(this.mouseListener);
        this.shapePane.addMouseListener(popupMenuListener);
        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
        	popupResponse.activate();
        }
        this.activated = true;
	}

	@Override
	public void deactivate(){
		this.shapePane.removeKeyListener(this.keyListener);
//        this.shapePane.removeMouseListener(this.mouseSelectionListener);
//        this.shapePane.removeMouseMotionListener(this.mouseFeedbackListener);
//        this.shapePane.removeMouseMotionListener(this.dragListener);
//        this.shapePane.removeMouseListener(this.dragListener);
//        this.shapePane.removeMouseListener(selectionFeedbackListener);
        this.shapePane.removeMouseMotionListener(this.mouseListener);
        this.shapePane.removeMouseListener(this.mouseListener);
        this.shapePane.removeMouseListener(popupMenuListener);
        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
        	popupResponse.deactivate();
        }
        this.activated = false;
	}


	private void handleKeyRelease(){
		if(this.keyboardResponseMap.isKeyPressed()){
			this.keyboardResponseMap.cursorsKeyUp();
			logger.trace("Key release detected");
		}
	}
	
	private void handleKeyPress(CursorType cursorPressed){
		if(!this.keyboardResponseMap.isKeyPressed()){
			this.keyboardResponseMap.cursorKeyDown(cursorPressed);
			logger.trace("Initial key press detected");
		}
		else{
			this.keyboardResponseMap.cursorKeyStillDown(cursorPressed);
			logger.trace("Key press ongoing");
		}
	}
	
	@Override
	public IDragResponse getDragResponse(SelectionHandleType type) {
		return this.dragResponseMap.get(type);
	}


	@Override
	public IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type) {
		return this.mouseResponseMap.get(type);
	}


//	@Override
//	public ISelectionRecord getSelectionRecord() {
//		ISelectionLayer selectionLayer = this.shapePane.getLayer(LayerType.SELECTION);
//		return selectionLayer.getSelectionRecord();
//	}


//	@Override
//	public void updateView() {
//		this.shapePane.updateView();
//	}


	@Override
	public boolean isActivated() {
		return this.activated ;
	}


	@Override
	public ISelectionResponse getSelectionResponse(SelectionHandleType type) {
		return this.selectionResponse;
	}


	@Override
	public ISelectionHandle getSelectionHandle(Point location) {
		ISelectionLayer selectionLayer = this.shapePane.getLayer(LayerType.SELECTION);		
		return selectionLayer.getSelectionRecord().findSelectionModelAt(location);
	}
}
