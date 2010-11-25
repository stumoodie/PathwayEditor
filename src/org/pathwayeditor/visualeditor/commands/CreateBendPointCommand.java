package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
import org.pathwayeditor.figure.geometry.Point;

public class CreateBendPointCommand implements ICommand {
	private final IBendPointContainer linkAttribute;
	private final int lineSegmentIdx;
	private final Point position;
	
	public CreateBendPointCommand(IBendPointContainer drawingElement, int lineSegmentIdx, Point position) {
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
