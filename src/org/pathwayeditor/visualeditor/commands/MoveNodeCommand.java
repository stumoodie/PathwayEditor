package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.Point;

public class MoveNodeCommand implements ICommand {
	private final IDrawingNodeAttribute node;
	private final Point locationDelta;
	
	public MoveNodeCommand(IDrawingNodeAttribute node, Point locationDelta) {
		this.node = node;
		this.locationDelta = locationDelta;
	}

	@Override
	public void execute() {
		this.redo();
	}

	@Override
	public void redo() {
		this.node.translate(this.locationDelta);
	}

	@Override
	public void undo() {
		this.node.translate(this.locationDelta.negate());
	}

}
