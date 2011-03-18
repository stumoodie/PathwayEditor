package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.figure.geometry.Point;

public interface ISelectionOperation {

	void setCurrentLocation(Point location);

	void setPrimaryClick();

	void handleClick();

	void setSecondaryClick();

}
