package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public class CentralSelectionHandle extends SelectionHandle {
	
	public CentralSelectionHandle(ISelection selection, INodeController nodeController){
		super(nodeController, SelectionRegion.Central, selection);
	}
	
	
	@Override
	public boolean containsPoint(Point point) {
		return this.getNodeController().getBounds().containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.getNodeController().getBounds();
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getNodeController().compareTo(o.getNodeController());
	}

	@Override
	public void translate(Point delta) {
		// do nothing as this uses the node controller which will also be moved
	}
}
