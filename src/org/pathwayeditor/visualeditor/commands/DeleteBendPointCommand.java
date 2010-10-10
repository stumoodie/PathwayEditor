package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Point;

public class DeleteBendPointCommand implements ICommand {
	private final ILinkAttribute linkAttribute;
	private final int bpIdx;
	private Point originalLocation;
	
	public DeleteBendPointCommand(ILinkAttribute linkAttribute, int bpIdx) {
		this.linkAttribute = linkAttribute;
		this.bpIdx = bpIdx;
	}

	@Override
	public void execute() {
		this.originalLocation = linkAttribute.getBendPointContainer().getBendPoint(bpIdx);
		this.redo();
	}

	@Override
	public void redo() {
		linkAttribute.getBendPointContainer().removeBendPoint(bpIdx);
	}

	@Override
	public void undo() {
		linkAttribute.getBendPointContainer().createNewBendPoint(bpIdx, originalLocation);
	}

}
