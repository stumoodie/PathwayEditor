package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {
	enum ReparentingStateType { CAN_REPARENT, CAN_MOVE, FORBIDDEN }
	
	
	ReparentingStateType getReparentingState(Point delta);
	
	void moveStarted();

	void moveOngoing(Point delta);

	void moveFinished(Point delta, ReparentingStateType reparentingState);

	void deleteSelection();

	/**
	 * Indicates whether the move operation should actually copy the selected
	 * elements.
	 * @param isSelected true if it is a copy, false otherwise.
	 */
	void setCopyOnMove(boolean isSelected);

}
