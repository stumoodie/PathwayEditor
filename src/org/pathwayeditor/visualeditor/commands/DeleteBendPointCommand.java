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
		this.originalLocation = linkAttribute.getBendPoint(bpIdx).getLocation();
		this.redo();
	}

	@Override
	public void redo() {
		linkAttribute.removeBendPoint(bpIdx);
	}

	@Override
	public void undo() {
		linkAttribute.createNewBendPoint(bpIdx, originalLocation);
	}

}
