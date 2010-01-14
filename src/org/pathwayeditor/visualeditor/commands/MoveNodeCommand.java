package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.figure.geometry.Point;

public class MoveNodeCommand implements ICommand {
	private final IDrawingNode node;
	private final Point locationDelta;
	
	public MoveNodeCommand(IDrawingNode node, Point locationDelta) {
		this.node = node;
		this.locationDelta = locationDelta;
	}

	@Override
	public void execute() {
		this.redo();
	}

	@Override
	public void redo() {
		this.node.getAttribute().translate(this.locationDelta);
	}

	@Override
	public void undo() {
		this.node.getAttribute().translate(this.locationDelta.negate());
	}

}
