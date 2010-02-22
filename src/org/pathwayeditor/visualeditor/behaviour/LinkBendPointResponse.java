package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class LinkBendPointResponse extends HandleResponse {
	private static final int DEFAULT_SEGMENT_IDX = 0;
	private final ILinkOperation linkOperation;
	private int segmentIndex = DEFAULT_SEGMENT_IDX;
	private Point lastLocation;
	
	public LinkBendPointResponse(ILinkOperation linkOperation){
		super();
		this.linkOperation = linkOperation;
	}
	
	@Override
	public boolean canContinueDrag(Point delta) {
		return false;
	}

	@Override
	public boolean canMove() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.linkOperation.moveBendPointOngoing(segmentIndex, newLocation);
		this.lastLocation = newLocation;
	}

	@Override
	public void dragFinished() {
		this.linkOperation.moveBendPointFinished(segmentIndex, this.lastLocation);
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point startLocation) {
		this.lastLocation = startLocation;
		this.segmentIndex = selectionHandle.getHandleIndex();
	}
}
