package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public interface INewPositionCalculator {

	void calculateDeltas(Point newLocation);

	Point getLastDelta();
	
	Dimension getResizedDelta();
	
	Point getResizedOrigin();
}
