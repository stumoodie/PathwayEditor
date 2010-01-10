package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.figure.geometry.Point;

public class MoveNodeCommand implements ICommand {
	private final IDrawingNode node;
	private final Point newLocation;
	private Point oldLocation;
	
	public MoveNodeCommand(IDrawingNode node, Point newLocation) {
		this.node = node;
		this.newLocation = newLocation;
	}

	@Override
	public void execute() {
		this.oldLocation = this.node.getAttribute().getLocation(); 
		this.redo();
	}

	@Override
	public void redo() {
		this.node.getAttribute().setLocation(this.newLocation);
	}

	@Override
	public void undo() {
		this.node.getAttribute().setLocation(oldLocation);
	}

}
