package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface ISelectionOperation {

	void setCurrentLocation(Point location);

	void setPrimaryClick();

	void handleClick();

	void setSecondaryClick();

}
