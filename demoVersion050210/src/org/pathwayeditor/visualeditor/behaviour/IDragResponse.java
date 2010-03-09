package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface IDragResponse {

	boolean isDragOngoing();
	
	boolean canContinueDrag(Point delta);
	
	void dragStarted(Point startLocation);
	
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
