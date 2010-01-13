package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public class CornerSelectionHandle extends SelectionHandle {
	private static final double RADIUS = 4.0;
	private Point centre;
	private final Point initialCentre;
	
	CornerSelectionHandle(ISelection selection, INodeController node, Point centre, SelectionRegion region){
		super(node, region, selection);
		this.centre = centre;
		this.initialCentre = centre;
	}
	
	@Override
	public boolean containsPoint(Point point) {
		return centre.getDistance(point) < RADIUS;
	}

	@Override
	public Envelope getBounds() {
		return new Envelope(centre.getX()-RADIUS, centre.getY()-RADIUS, RADIUS*2, RADIUS*2);
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getNodeController().compareTo(o.getNodeController());
	}

	@Override
	public void translate(Point delta) {
		this.centre = this.initialCentre.translate(delta);
	}
}
