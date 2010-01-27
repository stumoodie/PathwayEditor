package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {
	enum ReparentingStateType { CAN_REPARENT, CAN_MOVE, FORBIDDEN }
	
	
	ReparentingStateType getReparentingState(Point delta);
	
	void moveStarted();

	void moveOngoing(Point delta);

	void moveFinished(Point delta, ReparentingStateType reparentingState);

}
