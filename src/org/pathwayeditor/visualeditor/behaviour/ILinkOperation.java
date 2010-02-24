package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface ILinkOperation {

	void newBendPointStarted(ISelectionHandle handle);
	
	void newBendPointOngoing(ISelectionHandle handle, Point position);
	
	void newBendPointFinished(ISelectionHandle handle, Point position);
	
	void moveBendPointStated(ISelectionHandle handle);

	void moveBendPointOngoing(ISelectionHandle handle, Point delta);
	
	void moveBendPointFinished(ISelectionHandle handle, Point delta);
	
}
