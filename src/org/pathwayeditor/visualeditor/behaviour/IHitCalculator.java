package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public interface IHitCalculator {

	void setMousePosition(double x, double y);

	IShapeController getShapeAtCurrentLocation();

	Point getDiagramLocation();

	Point getMousePosition();

}
