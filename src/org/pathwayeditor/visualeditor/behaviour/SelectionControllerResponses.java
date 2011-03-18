package org.pathwayeditor.visualeditor.behaviour;

import java.util.HashMap;
import java.util.Map;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionControllerResponses implements IControllerResponses {
	private final Map<SelectionHandleType, IDragResponse> dragResponseMap;
	private final Map<SelectionHandleType, IMouseFeedbackResponse> mouseResponseMap;
	private final SelectionResponse selectionResponse;

	public SelectionControllerResponses(IOperationFactory opFactory) {
		this.dragResponseMap = new HashMap<SelectionHandleType, IDragResponse>();
		initialiseDragResponses(opFactory);
		this.mouseResponseMap = new HashMap<SelectionHandleType, IMouseFeedbackResponse>();
		initialiseMouseResponse();
        this.selectionResponse = new SelectionResponse(opFactory.getSelectionOperation());
	}

	@Override
	public IDragResponse getDragResponse(SelectionHandleType type) {
		return this.dragResponseMap.get(type);
	}

	@Override
	public IMouseFeedbackResponse getFeedbackResponse(SelectionHandleType type) {
		return this.mouseResponseMap.get(type);
	}

	@Override
	public ISelectionResponse getSelectionResponse() {
		return this.selectionResponse;
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
}
