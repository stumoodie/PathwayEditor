package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public class ResizeNodeCommand implements ICommand {
	private final IDrawingNode node;
	private Point originDelta;
	private Dimension sizeDelta;
	
	
	public ResizeNodeCommand(IDrawingNode drawingElement, Point originDelta, Dimension sizeDelta) {
		this.node = drawingElement;
		this.originDelta = originDelta;
		this.sizeDelta = sizeDelta;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		this.node.getAttribute().resize(this.originDelta, this.sizeDelta);
	}

	@Override
	public void undo() {
		this.node.getAttribute().resize(this.originDelta.negate(), this.sizeDelta.negate());
	}

}
