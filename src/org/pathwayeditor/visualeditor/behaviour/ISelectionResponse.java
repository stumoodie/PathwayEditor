package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface ISelectionResponse {

	void primaryClick(Point location);

	void secondaryClick(Point location);

	void doubleClick(Point location);

}
