package org.pathwayeditor.visualeditor.geometry;

import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;

public interface IDrawingElementSelector {

	SortedSet<IDrawingPrimitiveController> findElementAt(Point p);

}
