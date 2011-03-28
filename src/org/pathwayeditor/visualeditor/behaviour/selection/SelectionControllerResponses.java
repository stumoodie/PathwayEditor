package org.pathwayeditor.visualeditor.behaviour.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IMarqueeOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionControllerResponses implements IControllerResponses {
	private final Map<SelectionHandleType, ISelectionDragResponse> dragResponseMap;
	private final Map<SelectionHandleType, IMouseFeedbackResponse> mouseResponseMap;
	private final Map<SelectionHandleType, IPopupMenuResponse> popupMenuMap;
	private final SelectionResponse selectionResponse;
	private final IKeyboardResponse keyboardResponse;

	public SelectionControllerResponses(IOperationFactory opFactory) {
		this.dragResponseMap = new HashMap<SelectionHandleType, ISelectionDragResponse>();
		initialiseDragResponses(opFactory);
		this.mouseResponseMap = new HashMap<SelectionHandleType, IMouseFeedbackResponse>();
		initialiseMouseResponse();
        this.selectionResponse = new SelectionResponse(opFactory.getSelectionOperation());
        this.keyboardResponse = new KeyboardResponse(opFactory.getMoveOperation());
        this.popupMenuMap = new HashMap<SelectionHandleType, IPopupMenuResponse>();
        initialisePopupMenuResponse(opFactory);
	}

	@Override
	public IDragResponse getDragResponse(ISelectionHandle handle) {
		SelectionHandleType type = getSelectionHandleType(handle);
		ISelectionDragResponse retVal = this.dragResponseMap.get(type);
		retVal.setSelectionHandle(handle);
		return retVal;
	}

	
	private static SelectionHandleType getSelectionHandleType(ISelectionHandle handle){
		return handle != null ? handle.getType() : SelectionHandleType.None;
	}
	
	@Override
	public IMouseFeedbackResponse getFeedbackResponse(ISelectionHandle handle) {
		SelectionHandleType type = getSelectionHandleType(handle);
		IMouseFeedbackResponse retVal = this.mouseResponseMap.get(type);
		return retVal;
	}

	@Override
	public ISelectionResponse getSelectionResponse() {
		return this.selectionResponse;
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

	@Override
	public IKeyboardResponse getKeyboardResponse() {
		return this.keyboardResponse;
	}

	@Override
	public IPopupMenuResponse getPopupMenuResponse(ISelectionHandle popupSelectionHandle) {
		IPopupMenuResponse retVal = this.popupMenuMap.get(getSelectionHandleType(popupSelectionHandle));
		retVal.setSelectionHandle(popupSelectionHandle);
		return retVal;
	}

	@Override
	public Iterator<IPopupMenuResponse> popResponseIterator() {
		return this.popupMenuMap.values().iterator();
	}

	@Override
	public int numPopupResponses() {
		return this.popupMenuMap.size();
	}
}
