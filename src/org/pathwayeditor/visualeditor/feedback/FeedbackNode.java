package org.pathwayeditor.visualeditor.feedback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public class FeedbackNode implements IFeedbackNode {
//	private final Logger logger = Logger.getLogger(this.getClass()); 
	private IFigureRenderingController figureRenderingController;
	private final Envelope initialBounds;
	private final int elementIdentifier;
	private final List<IFeedbackNodeListener> listeners;

	public FeedbackNode(int uniqueId, IFigureRenderingController figureRenderingController, Envelope bounds){
		this.listeners = new LinkedList<IFeedbackNodeListener>();
		this.elementIdentifier = uniqueId;
		this.figureRenderingController = figureRenderingController;
		this.initialBounds = bounds;
	}
	
	@Override
	public int getElementIdentifier() {
		return elementIdentifier;
	}

	@Override
	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
		figureRenderingController.setRequestedEnvelope(this.initialBounds.resize(originDelta, resizeDelta));
		figureRenderingController.generateFigureDefinition();
		notifyResize(this.initialBounds, originDelta, resizeDelta);
	}

	private void notifyResize(final Envelope initialBounds, final Point originDelta, final Dimension resizeDelta) {
		IFeedbackNodeResizeEvent e = new IFeedbackNodeResizeEvent() {
			
			@Override
			public Dimension getSizeDelta() {
				return resizeDelta;
			}
			
			@Override
			public Envelope getOriginalBounds() {
				return initialBounds;
			}
			
			@Override
			public Point getOriginDelta() {
				return originDelta;
			}
			
			@Override
			public IFeedbackNode getNode() {
				return FeedbackNode.this;
			}
		};
		notifyResizeEvent(e);
	}

	@Override
	public void translatePrimitive(Point translation) {
		figureRenderingController.setRequestedEnvelope(this.initialBounds.translate(translation));
		figureRenderingController.generateFigureDefinition();
		notifyTranslation(this.initialBounds, translation);
	}

	private void notifyTranslation(final Envelope oldBounds, final Point translation) {
		IFeedbackNodeTranslationEvent e = new IFeedbackNodeTranslationEvent() {
			
			@Override
			public IFeedbackNode getNode() {
				return FeedbackNode.this;
			}

			@Override
			public Point getTranslation() {
				return translation;
			}

			@Override
			public Envelope oldBounds() {
				return oldBounds;
			}
		};
		notifyTranslationEvent(e);
	}

	private void notifyTranslationEvent(IFeedbackNodeTranslationEvent e) {
		for(IFeedbackNodeListener l : this.listeners){
			l.nodeTranslationEvent(e);
		}
	}

	private void notifyResizeEvent(IFeedbackNodeResizeEvent e) {
		for(IFeedbackNodeListener l : this.listeners){
			l.nodeResizeEvent(e);
		}
	}

	@Override
	public Envelope getBounds() {
		return this.getFigureController().getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.getFigureController().getConvexHull();
	}

	@Override
	public IFigureRenderingController getFigureController() {
		return this.figureRenderingController;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementIdentifier;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FeedbackNode))
			return false;
		FeedbackNode other = (FeedbackNode) obj;
		if (elementIdentifier != other.elementIdentifier)
			return false;
		return true;
	}

	@Override
	public void addFeedbackNodeListener(IFeedbackNodeListener srcFeedbackNodeListener) {
		this.listeners.add(srcFeedbackNodeListener);
	}

	@Override
	public void removeFeedbackNodeListener(IFeedbackNodeListener srcFeedbackNodeListener) {
		this.listeners.remove(srcFeedbackNodeListener);
	}

	@Override
	public List<IFeedbackNodeListener> getFeedbackNodeListeners() {
		return new ArrayList<IFeedbackNodeListener>(this.listeners);
	}

	@Override
	public void setFillColour(RGB colour) {
		this.figureRenderingController.setFillColour(colour);
		this.figureRenderingController.generateFigureDefinition();
	}

	@Override
	public void setLineColour(RGB colour) {
		this.figureRenderingController.setLineColour(colour);
		this.figureRenderingController.generateFigureDefinition();
	}

	@Override
	public void setLineStyle(LineStyle lineStyle) {
		this.figureRenderingController.setLineStyle(lineStyle);
		this.figureRenderingController.generateFigureDefinition();
	}

	@Override
	public void setLineWidth(double lineWidth) {
		this.figureRenderingController.setLineWidth(lineWidth);
		this.figureRenderingController.generateFigureDefinition();
	}

	@Override
	public void setLocation(Point newPosition) {
		Point delta = this.initialBounds.getOrigin().difference(newPosition);
		this.translatePrimitive(delta);
	}

}
