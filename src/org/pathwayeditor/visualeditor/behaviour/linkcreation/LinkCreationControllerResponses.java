package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.selection.DefaultPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.KeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.LinkBendpointPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.LinkPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.ShapePopupMenuResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkCreationControllerResponses implements IControllerResponses {
	private final IDragResponse dragResponse;
	private final ISelectionResponse selectionResponse;
	private final IMouseFeedbackResponse feedbackResponse;
	private final IKeyboardResponse keyboardResponse;
	private final Map<SelectionHandleType, IPopupMenuResponse> popupMenuMap; 
	
	public LinkCreationControllerResponses(IOperationFactory opFactory, ILinkTypeInspector shapeTypeInspector) {
		this.dragResponse = new LinkCreationDragResponse(opFactory.getLinkCreationOperation(), shapeTypeInspector);
        this.selectionResponse = new SelectionLinkCreationResponse();
        this.feedbackResponse = new MouseLinkCreationFeedbackResponse();
        this.keyboardResponse = new KeyboardResponse(new IEditingOperation() {
			
			@Override
			public void moveStarted() {
			}
			
			@Override
			public void moveOngoing(Point delta) {
			}
			
			@Override
			public void moveFinished(Point delta, ReparentingStateType reparentingState) {
			}
			
			@Override
			public ReparentingStateType getReparentingState(Point delta) {
				return ReparentingStateType.FORBIDDEN;
			}
		});
        this.popupMenuMap = new HashMap<SelectionHandleType, IPopupMenuResponse>();
        initialisePopupMenuResponse(opFactory);
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

	@Override
	public IDragResponse getDragResponse(SelectionHandleType type) {
		return this.dragResponse;
	}

	@Override
	public IMouseFeedbackResponse getFeedbackResponse(SelectionHandleType type) {
		return this.feedbackResponse;
	}

	@Override
	public ISelectionResponse getSelectionResponse() {
		return this.selectionResponse;
	}

	@Override
	public IKeyboardResponse getKeyboardResponse() {
		return this.keyboardResponse;
	}

	@Override
	public IPopupMenuResponse getPopupMenuResponse(SelectionHandleType popupSelectionHandle) {
		return this.popupMenuMap.get(popupSelectionHandle);
	}

	@Override
	public Iterator<IPopupMenuResponse> popResponseIterator() {
		return popupMenuMap.values().iterator();
	}

	@Override
	public int numPopupResponses() {
		return popupMenuMap.size();
	}

}
