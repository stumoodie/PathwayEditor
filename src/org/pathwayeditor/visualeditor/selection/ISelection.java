package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public interface ISelection extends Comparable<ISelection> {
	
	boolean isPrimary();
	
	boolean isSecondary();
	
	ISelectionHandle getSelectionModel(SelectionRegion region);
	
	IDrawingPrimitiveController getPrimitiveController();

	ISelectionHandle findSelectionModelAt(Point point);
}
