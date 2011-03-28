package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.KeyListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.visualeditor.behaviour.creation.CreationControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.creation.CreationDragResponse;
import org.pathwayeditor.visualeditor.behaviour.creation.IShapeTypeInspector;
import org.pathwayeditor.visualeditor.behaviour.creation.MouseCreationFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.creation.ShapeCreationMouseBehaviourListener;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.ILinkTypeInspector;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.LinkCreationControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.LinkCreationMouseBehaviourListener;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.selection.PopupMenuListener;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionKeyListener;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionMouseBehaviourListener;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class MouseBehaviourController implements IMouseBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private IMouseBehaviourListener currentStateController; 
	private final IMouseBehaviourListener selectionStateController;
	private final IMouseBehaviourListener shapeCreationStateController;
	private final IMouseBehaviourListener linkCreationStateController;

	private boolean activated;
	private IObjectType currShapeType;
	private final IShapePane shapePane;
	private final IPopupMenuListener popupMenuListener;
	private final KeyListener keyListener; 
	
	public MouseBehaviourController(IShapePane pane, IOperationFactory opFactory){
		this.shapePane = pane;
		IControllerResponses selectionResponse = new SelectionControllerResponses(opFactory);
		this.keyListener = new SelectionKeyListener(selectionResponse);
		ISelectionStateBehaviourController selectionController = new GeneralStateController(pane, selectionResponse);
		this.popupMenuListener = new PopupMenuListener(selectionController, selectionResponse);
		this.selectionStateController = new SelectionMouseBehaviourListener(selectionController);
		this.shapeCreationStateController = new ShapeCreationMouseBehaviourListener(new GeneralStateController(pane, new CreationControllerResponses(opFactory,
				new IShapeTypeInspector() {
					
					@Override
					public IShapeObjectType getCurrentShapeType() {
						return (IShapeObjectType)currShapeType;
					}
		})), new CreationDragResponse(opFactory.getShapeCreationOperation(), new IShapeTypeInspector() {
			
			@Override
			public IShapeObjectType getCurrentShapeType() {
				return (IShapeObjectType)currShapeType;
			}
		}), new MouseCreationFeedbackResponse());
		this.linkCreationStateController = new LinkCreationMouseBehaviourListener(new GeneralStateController(pane, new LinkCreationControllerResponses(opFactory,
				new ILinkTypeInspector() {
			
			@Override
			public ILinkObjectType getCurrentLinkType() {
				return (ILinkObjectType)currShapeType;
			}
		})));
		this.currentStateController = this.selectionStateController;
	}

	@Override
	public void activate(){
		this.shapePane.addKeyListener(this.keyListener);
        this.shapePane.addMouseMotionListener(this.currentStateController);
        this.shapePane.addMouseListener(this.currentStateController);
        this.shapePane.addMouseListener(popupMenuListener);
//        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
//        	popupResponse.activate();
//        }
        this.popupMenuListener.activate();
//        Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
//        while(iter.hasNext()){
//        	IPopupMenuResponse popupResponse = iter.next();
//        	popupResponse.activate();
//        }
        this.activated = true;
	}

	@Override
	public void deactivate(){
		this.shapePane.removeKeyListener(this.keyListener);
        this.shapePane.removeMouseMotionListener(this.currentStateController);
        this.shapePane.removeMouseListener(this.currentStateController);
        this.shapePane.removeMouseListener(popupMenuListener);
//        for(IPopupMenuResponse popupResponse : this.popupMenuMap.values()){
//        	popupResponse.deactivate();
//        }
        this.popupMenuListener.deactivate();
//        Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
//        while(iter.hasNext()){
//        	IPopupMenuResponse popupResponse = iter.next();
//        	popupResponse.deactivate();
//        }
        this.activated = false;
	}

	@Override
	public boolean isActivated(){
		return this.activated;
	}
	
	@Override
	public void setLinkCreationMode(ILinkObjectType linkType) {
		this.currShapeType = linkType;
		if(!this.currentStateController.equals(this.linkCreationStateController)){
			if(this.isActivated()){
		        this.shapePane.removeMouseMotionListener(this.currentStateController);
		        this.shapePane.removeMouseListener(this.currentStateController);
			}
			this.currentStateController = this.linkCreationStateController;
			if(this.isActivated()){
		        this.shapePane.addMouseMotionListener(this.currentStateController);
		        this.shapePane.addMouseListener(this.currentStateController);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("In link creation mode for object type: " + linkType.getName());
		}
	}

	@Override
	public void setSelectionMode() {
		if(this.currentStateController != this.selectionStateController){
			if(this.isActivated()){
		        this.shapePane.removeMouseMotionListener(this.currentStateController);
		        this.shapePane.removeMouseListener(this.currentStateController);
			}
			this.currentStateController = this.selectionStateController;
			if(this.isActivated()){
		        this.shapePane.addMouseMotionListener(this.currentStateController);
		        this.shapePane.addMouseListener(this.currentStateController);
			}
			logger.debug("Setting selection controller state");
		}
	}

	@Override
	public void setShapeCreationMode(IShapeObjectType shapeType) {
		this.currShapeType = shapeType;
		if(!this.currentStateController.equals(this.shapeCreationStateController)){
			if(this.isActivated()){
		        this.shapePane.removeMouseMotionListener(this.currentStateController);
		        this.shapePane.removeMouseListener(this.currentStateController);
			}
			this.currentStateController = this.shapeCreationStateController;
			if(this.isActivated()){
		        this.shapePane.addMouseMotionListener(this.currentStateController);
		        this.shapePane.addMouseListener(this.currentStateController);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("In shape creation mode for object type: " + shapeType.getName());
		}
	}

}
