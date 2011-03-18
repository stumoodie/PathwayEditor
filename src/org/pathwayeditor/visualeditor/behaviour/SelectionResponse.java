package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

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
