package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Point;

public class CreateBendPointCommand implements ICommand {
	private final ILinkAttribute linkAttribute;
	private final int lineSegmentIdx;
	private Point position;
	
	public CreateBendPointCommand(ILinkAttribute drawingElement, int lineSegmentIdx, Point position) {
		this.linkAttribute = drawingElement;
		this.lineSegmentIdx = lineSegmentIdx;
		this.position = position;
	}

	@Override
	public void execute() {
		this.redo();
	}

	@Override
	public void redo() {
		this.linkAttribute.createNewBendPoint(this.lineSegmentIdx, this.position);
	}

	@Override
	public void undo() {
		this.linkAttribute.removeBendPoint(this.lineSegmentIdx);
	}

}
