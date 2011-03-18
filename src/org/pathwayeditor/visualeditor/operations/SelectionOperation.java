package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class SelectionOperation implements ISelectionOperation {
	private Point location;
	private boolean primarySelection;
	private final ISelectionRecord selectionRecord;
	private final IIntersectionCalculator intersectionCal;
	
	public SelectionOperation(ISelectionRecord selectionRecord, IIntersectionCalculator intersectionCal){
		this.selectionRecord = selectionRecord;
		this.intersectionCal = intersectionCal;
	}
	
	@Override
	public void setCurrentLocation(Point location) {
		this.location = location;
	}

	@Override
	public void setPrimaryClick() {
		this.primarySelection = true;
	}

	@Override
	public void handleClick() {
		intersectionCal.setFilter(null);
		SortedSet<IDrawingElementController> selectedControllers = intersectionCal.findDrawingPrimitivesAt(location);
		if(!selectedControllers.isEmpty()){
			if(this.primarySelection){
				this.selectionRecord.setPrimarySelection(selectedControllers.first());
			}
			else{
				this.selectionRecord.addSecondarySelection(selectedControllers.first());
			}
		}
		else{
			this.selectionRecord.clear();
		}
	}

	@Override
	public void setSecondaryClick() {
		this.primarySelection = false;
	}

}
