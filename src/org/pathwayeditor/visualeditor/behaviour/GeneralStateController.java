package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.ISelectionLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class GeneralStateController implements ISelectionStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
//	private final IKeyboardResponse keyboardResponseMap;
//	private final MouseListener popupMenuListener;
	private boolean activated = false;
	private final SelectionMouseListener mouseListener;
	private final IControllerResponses responses;

	public GeneralStateController(IShapePane pane, IControllerResponses responses){
		this.shapePane = pane;
		this.mouseListener = new SelectionMouseListener(this);
		this.responses = responses;
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
	

	@Override
	public void activate(){
//		this.shapePane.addKeyListener(this.keyListener);
        this.shapePane.addMouseMotionListener(this.mouseListener);
        this.shapePane.addMouseListener(this.mouseListener);
//        this.shapePane.addMouseListener(popupMenuListener);
//        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
//        	popupResponse.activate();
//        }
        this.activated = true;
	}

	@Override
	public void deactivate(){
//		this.shapePane.removeKeyListener(this.keyListener);
        this.shapePane.removeMouseMotionListener(this.mouseListener);
        this.shapePane.removeMouseListener(this.mouseListener);
//        this.shapePane.removeMouseListener(popupMenuListener);
//        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
//        	popupResponse.deactivate();
//        }
        this.activated = false;
	}


//	private void handleKeyRelease(){
//		if(this.keyboardResponseMap.isKeyPressed()){
//			this.keyboardResponseMap.cursorsKeyUp();
//			logger.trace("Key release detected");
//		}
//	}
//	
//	private void handleKeyPress(CursorType cursorPressed){
//		if(!this.keyboardResponseMap.isKeyPressed()){
//			this.keyboardResponseMap.cursorKeyDown(cursorPressed);
//			logger.trace("Initial key press detected");
//		}
//		else{
//			this.keyboardResponseMap.cursorKeyStillDown(cursorPressed);
//			logger.trace("Key press ongoing");
//		}
//	}
	
	@Override
	public IDragResponse getDragResponse(SelectionHandleType type) {
		return this.responses.getDragResponse(type);
	}


	@Override
	public IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type) {
		return this.responses.getFeedbackResponse(type);
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
	public ISelectionResponse getClickResponse() {
		return this.responses.getSelectionResponse();
	}


	@Override
	public ISelectionHandle getSelectionHandle(Point location) {
		ISelectionLayer selectionLayer = this.shapePane.getLayer(LayerType.SELECTION);		
		return selectionLayer.getSelectionRecord().findSelectionModelAt(location);
	}
}
