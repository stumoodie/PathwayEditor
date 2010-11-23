package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
import org.pathwayeditor.figure.geometry.Point;

public class MoveBendPointCommand implements ICommand {
	private final IBendPointContainer bpContainer;
	private final Point translation;
	private final int idx;
	
	public MoveBendPointCommand(IBendPointContainer bpContainer, int idx, Point translation) {
		this.bpContainer = bpContainer;
		this.translation = translation;;
		this.idx = idx;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		this.bpContainer.translateBendPoint(idx, this.translation.negate());
	}

	@Override
	public void undo() {
		this.bpContainer.translateBendPoint(idx, this.translation);
	}

}
