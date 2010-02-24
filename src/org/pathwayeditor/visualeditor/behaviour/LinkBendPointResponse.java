package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkBendPointResponse extends HandleResponse {
//	private static final int DEFAULT_SEGMENT_IDX = 0;
	private final ILinkOperation linkOperation;
	private ISelectionHandle selectionHandle = null;
	private Point lastLocation;
	
	public LinkBendPointResponse(ILinkOperation linkOperation){
		super();
		this.linkOperation = linkOperation;
	}
	
	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
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
		this.linkOperation.moveBendPointOngoing(selectionHandle, newLocation);
		this.lastLocation = newLocation;
	}

	@Override
	public void dragFinished() {
		exitDragOngoingState();
		this.linkOperation.moveBendPointFinished(selectionHandle, this.lastLocation);
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point startLocation) {
		if(!selectionHandle.getType().equals(SelectionHandleType.LinkBendPoint)) throw new IllegalArgumentException("Only expect to respond to link bend point handles");
		
		this.lastLocation = startLocation;
		this.selectionHandle = selectionHandle;
		this.linkOperation.moveBendPointStated(selectionHandle);
		this.enterDragOngoingState();
	}
}
