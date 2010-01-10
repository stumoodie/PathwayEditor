package org.pathwayeditor.visualeditor;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;

public class RootPrimitive implements IRootPrimitive {
	private final IRootNode domainNode;
	private final IConvexHull hull;
	private IViewModel viewModel;
	
	
	public RootPrimitive(IViewModel viewModel, IRootNode node) {
		this.viewModel = viewModel;
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

	@Override
	public void resyncToModel() {
		// do nothing
	}

	@Override
	public int compareTo(IDrawingPrimitive o) {
		Integer otherIndex = o.getDrawingElement().getAttribute().getCreationSerial();
		return Integer.valueOf(this.domainNode.getAttribute().getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	public void dispose() {
		// do nothing
	}

	@Override
	public void addNodePrimitiveChangeListener(
			INodePrimitiveChangeListener listener) {
	}

	@Override
	public List<INodePrimitiveChangeListener> getNodePrimitiveChangeListeners() {
		return null;
	}

	@Override
	public void removeNodePrimitiveChangeListener(
			INodePrimitiveChangeListener listener) {
		
	}

	@Override
	public IViewModel getViewModel() {
		return this.viewModel;
	}

	@Override
	public void activate() {
		
	}

}
