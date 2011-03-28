package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkBendPointResponse extends HandleResponse implements ISelectionDragResponse {
//	private static final int DEFAULT_SEGMENT_IDX = 0;
	private final ILinkOperation linkOperation;
	private ISelectionHandle selectionHandle = null;
	private Point lastDelta;
	
	public LinkBendPointResponse(ILinkOperation linkOperation){
		super();
		this.linkOperation = linkOperation;
	}
	
	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public boolean canOperationSucceed() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = calculateLocationDelta(newLocation);
		this.linkOperation.moveBendPointOngoing(selectionHandle, this.lastDelta);
	}

	@Override
	public void dragFinished() {
		exitDragOngoingState();
		this.linkOperation.moveBendPointFinished(selectionHandle, this.lastDelta);
	}

	@Override
	public void dragStarted(Point startLocation) {
		if(!selectionHandle.getType().equals(SelectionHandleType.LinkBendPoint)) throw new IllegalArgumentException("Only expect to respond to link bend point handles");
		
		this.setStartLocation(startLocation);
		this.lastDelta = calculateLocationDelta(startLocation);
		this.linkOperation.moveBendPointStated(selectionHandle);
		this.enterDragOngoingState();
	}

	@Override
	public void setSelectionHandle(ISelectionHandle selectionHandle) {
		this.selectionHandle = selectionHandle;
	}
}
