package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface IDragResponse {

	boolean isDragOngoing();
	
	boolean canContinueDrag(Point delta);
	
	void dragStarted(ISelectionHandle selectionHandle, Point startLocation);
	
	void shiftSelected(boolean isSelected);
	
	void cmdSelected(boolean isSelected);
	
	void altSelected(boolean isSelected);
	
	void dragContinuing(Point newLocation);

	void dragFinished();

	boolean canReparent();
	
	boolean canMove();
	
//	void reparentEnabled(boolean reparent);
//	
//	boolean isReparentEnabled();
	
}
