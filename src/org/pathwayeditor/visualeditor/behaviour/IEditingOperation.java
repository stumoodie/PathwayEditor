package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {

	void moveFinished(Point delta);

	void copyFinished(Point delta);

	void moveStarted();

	void moveOngoing(Point delta);

	void copyOngoing(Point delta);

}
