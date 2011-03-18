package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;

public class SelectionResponse implements ISelectionResponse {
	private final ISelectionOperation selectionRecord;
	
	public SelectionResponse(ISelectionOperation selectionOperation){
		this.selectionRecord = selectionOperation;
	}
	
	@Override
	public void primaryClick(Point location) {
		selectionRecord.setCurrentLocation(location);
		selectionRecord.setPrimaryClick();
		selectionRecord.handleClick();
	}

	@Override
	public void secondaryClick(Point location) {
		selectionRecord.setCurrentLocation(location);
		selectionRecord.setSecondaryClick();
		selectionRecord.handleClick();
	}

}
