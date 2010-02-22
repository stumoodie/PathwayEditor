package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPoint;
import org.pathwayeditor.figure.geometry.Point;

public class MoveBendPointCommand implements ICommand {
	private final IBendPoint bp;
	private final Point newLocation;
	private Point oldLocation;
	
	public MoveBendPointCommand(IBendPoint bendPoint, Point position) {
		this.bp = bendPoint;
		this.newLocation = position;
	}

	@Override
	public void execute() {
		this.oldLocation = this.bp.getLocation();
		redo();
	}

	@Override
	public void redo() {
		this.bp.setLocation(newLocation);
	}

	@Override
	public void undo() {
		this.bp.setLocation(oldLocation);
	}

}
