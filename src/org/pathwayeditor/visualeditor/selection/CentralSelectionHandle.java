package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public class CentralSelectionHandle extends SelectionHandle implements ICentralSelectionHandleShape {
	
	public CentralSelectionHandle(ISelection selection, INodeController nodeController){
		super(nodeController, SelectionHandleType.Central, selection);
	}
	
	
	@Override
	public boolean containsPoint(Point point) {
		return this.getDrawingPrimitiveController().containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.getDrawingPrimitiveController().getDrawnBounds();
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public void translate(Point delta) {
		// do nothing as this uses the node controller which will also be moved
	}


	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}
}
