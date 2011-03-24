package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;

public class SelectionResponse implements ISelectionResponse {
	private final ISelectionOperation selectionOperation;
	
	public SelectionResponse(ISelectionOperation selectionOperation){
		this.selectionOperation = selectionOperation;
	}
	
	@Override
	public void primaryClick(Point location) {
		selectionOperation.setCurrentLocation(location);
		selectionOperation.setPrimaryClick();
		selectionOperation.handleClick();
	}

	@Override
	public void secondaryClick(Point location) {
		selectionOperation.setCurrentLocation(location);
		selectionOperation.setSecondaryClick();
		selectionOperation.handleClick();
	}

}
