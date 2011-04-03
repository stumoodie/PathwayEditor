package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse.CursorType;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourStateHandler;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionStateBehaviourController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class SelectionViewBehaviourStateHandler implements IViewBehaviourStateHandler, MouseMotionListener, MouseListener, KeyListener {
//	private ISelectionHandle currSelectionHandle;
	private ISelectionDragResponse currDragResponse;
	private IMouseFeedbackResponse currMouseFeedbackResponse;
	private final ISelectionStateBehaviourController mouseBehaviourController;
	private final IControllerResponses responses;
	private final Logger logger = Logger.getLogger(this.getClass());
	private boolean isActive = false;;
	
	public SelectionViewBehaviourStateHandler(ISelectionStateBehaviourController mouseBehaviourController,
			IControllerResponses responses) {
		this.mouseBehaviourController = mouseBehaviourController;
		this.responses = responses;
	}

	private void setCurrentCursorResponse(){
		currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(currDragResponse == null){
				currDragResponse = (ISelectionDragResponse)this.mouseBehaviourController.getDragResponse();
				currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
			}
			if(currDragResponse != null){
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
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			Point location = this.mouseBehaviourController.getDiagramLocation();
			ISelectionResponse currSelnResponse = null;
				currSelnResponse = this.mouseBehaviourController.getClickResponse();
			if(!e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.primaryClick(location);
			}
			else if(e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.secondaryClick(location);
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
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public void activate(IShapePane shapePane) {
		shapePane.addKeyListener(this);
		shapePane.addMouseMotionListener(this);
		shapePane.addMouseListener(this);
        Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
        while(iter.hasNext()){
        	IPopupMenuResponse popupResponse = iter.next();
        	popupResponse.activate();
        }
        this.isActive  = true;
	}

	@Override
	public void deactivate(IShapePane shapePane) {
		Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
        while(iter.hasNext()){
        	IPopupMenuResponse popupResponse = iter.next();
        	popupResponse.deactivate();
        }
		shapePane.removeKeyListener(this);
		shapePane.removeMouseMotionListener(this);
		shapePane.removeMouseListener(this);
        this.currDragResponse = null;
        this.currMouseFeedbackResponse = null;
        this.isActive = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isPopupTrigger()){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			
			IPopupMenuResponse response = mouseBehaviourController.getPopupMenuResponse();
			JPopupMenu popup = response.getPopupMenu();
			if(popup != null){
				mouseBehaviourController.showPopupMenus(popup);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(currDragResponse != null){
			currDragResponse.dragFinished();
			currMouseFeedbackResponse.reset();
			currDragResponse = null;
		}
		this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
		setCurrentCursorResponse();
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

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

	private void handleKeyRelease(){
		if(this.responses.getKeyboardResponse().isKeyPressed()){
			this.responses.getKeyboardResponse().cursorsKeyUp();
			logger.trace("Key release detected");
		}
	}
	
	private void handleKeyPress(CursorType cursorPressed){
		if(!this.responses.getKeyboardResponse().isKeyPressed()){
			this.responses.getKeyboardResponse().cursorKeyDown(cursorPressed);
			logger.trace("Initial key press detected");
		}
		else{
			this.responses.getKeyboardResponse().cursorKeyStillDown(cursorPressed);
			logger.trace("Key press ongoing");
		}
	}
}
