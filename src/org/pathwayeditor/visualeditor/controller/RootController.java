package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;

public class RootController extends NodeController implements IRootController {
	private final IRootNode domainNode;
	private final IConvexHull hull;
	
	
	public RootController(IViewControllerStore viewModel, IRootNode node) {
		super(viewModel);
		this.domainNode = node;
		this.hull = new RectangleHull(domainNode.getAttribute().getBounds());
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
	public IDrawingNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public void translatePrimitive(Point translation) {
		// do nothing
	}

//	@Override
//	public void redefinedSyncroniseToModel() {
//		// do nothing
//	}

	@Override
	public int compareTo(IDrawingPrimitiveController o) {
		Integer otherIndex = o.getDrawingElement().getAttribute().getCreationSerial();
		return Integer.valueOf(this.domainNode.getAttribute().getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	protected void nodeDisposalHook() {
		// do nothing
	}

	@Override
	public void activate() {
		
	}

	@Override
	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		return false;
	}

}
