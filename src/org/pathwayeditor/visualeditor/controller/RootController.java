package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;

public class RootController extends NodeController implements IRootController {
	private final IRootAttribute domainNode;
	private final IConvexHull hull;
	private boolean isActive;
	
	
	public RootController(IViewControllerStore viewModel, IRootAttribute node, int index) {
		super(viewModel, index);
		this.domainNode = node;
		this.hull = new RectangleHull(domainNode.getBounds());
		this.isActive = false;
	}

	@Override
	public Envelope getBounds() {
		return hull.getEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.hull;
	}

	@Override
	public IRootAttribute getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public void activate() {
		this.isActive = true;
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		return false;
	}

	@Override
	public void inactivate() {
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.getBounds();
	}

	@Override
	public boolean containsPoint(Point p) {
		return true;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return true;
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		return true;
	}
}
