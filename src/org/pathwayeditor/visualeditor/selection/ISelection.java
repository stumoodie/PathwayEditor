package org.pathwayeditor.visualeditor.selection;

import java.util.List;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface ISelection extends Comparable<ISelection> {
	
	boolean isPrimary();
	
	boolean isSecondary();
	
	List<ISelectionHandle> getSelectionHandle(SelectionHandleType region);
	
	IDrawingPrimitiveController getPrimitiveController();

	ISelectionHandle findSelectionModelAt(Point point);
}
