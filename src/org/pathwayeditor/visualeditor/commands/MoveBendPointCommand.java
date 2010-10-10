package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
import org.pathwayeditor.figure.geometry.Point;

public class MoveBendPointCommand implements ICommand {
	private final IBendPointContainer bp;
	private final int idx;
	private final Point newLocation;
	
	public MoveBendPointCommand(IBendPointContainer bendPoint, int idx, Point translation) {
		this.bp = bendPoint;
		this.newLocation = translation;
		this.idx = idx;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		this.bp.translateBendPoint(idx, newLocation);
	}

	@Override
	public void undo() {
		this.bp.translateBendPoint(idx, newLocation.negate());
	}

}
