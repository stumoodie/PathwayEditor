package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Point;

public class MoveLinkCommand implements ICommand {
	private final ILinkAttribute linkAttribute;
	private final Point delta;
	
	public MoveLinkCommand(ILinkAttribute nodePrimitive, Point delta) {
		this.linkAttribute = nodePrimitive;
		this.delta = delta;
	}

	@Override
	public void execute() {
		this.redo();
	}

	@Override
	public void undo() {
		this.linkAttribute.translate(delta.negate());
	}

	@Override
	public void redo() {
		this.linkAttribute.translate(delta);
	}

}
