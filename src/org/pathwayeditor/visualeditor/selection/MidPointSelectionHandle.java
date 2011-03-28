package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Scale;
import org.pathwayeditor.visualeditor.controller.INodeController;

public class MidPointSelectionHandle extends SelectionHandle implements IMidPointSelectionHandleShape {
	private static final double HANDLE_OFFSET = 4.0;
	private static final double HANDLE_WIDTH = HANDLE_OFFSET*2;
	private static final double HANDLE_HEIGHT = HANDLE_OFFSET*2;
	
	private Envelope bounds;
	private final Point initialOrigin; 

	public MidPointSelectionHandle(ISelection selection, INodeController nodeController, Point corner1, Point corner2, SelectionHandleType region){
		super(nodeController, region, selection);
		Point bisection = corner1.translate(corner1.difference(corner2).scale(new Scale(0.5, 0.5)));
		this.bounds = new Envelope(bisection.getX()-HANDLE_OFFSET, bisection.getY()-HANDLE_OFFSET,	HANDLE_WIDTH, HANDLE_HEIGHT);
		this.initialOrigin = this.bounds.getOrigin();
	}
	
	@Override
	public boolean containsPoint(Point point) {
		return this.bounds.containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.bounds;
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public void translate(Point delta) {
		this.bounds = this.bounds.changeOrigin(initialOrigin.translate(delta));
	}

	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}
}
