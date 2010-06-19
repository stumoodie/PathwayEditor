package org.pathwayeditor.visualeditor.selection;

import java.util.List;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public interface ISelection extends Comparable<ISelection> {
	enum SelectionType { PRIMARY, SECONDARY, SUBGRAPH };
	
	SelectionType getSelectionType();
	
	List<ISelectionHandle> getSelectionHandle(SelectionHandleType region);
	
	IDrawingElementController getPrimitiveController();

	ISelectionHandle findSelectionModelAt(Point point);
}
