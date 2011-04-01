package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;



public interface IMouseStateBehaviourController {

	void setMousePosition(double x, double y);
	
	Point getDiagramLocation();
	

}
