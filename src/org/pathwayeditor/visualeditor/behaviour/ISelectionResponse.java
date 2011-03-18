package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface ISelectionResponse {

	void primarySelection(Point location);

	void secondarySelection(Point location);

}
