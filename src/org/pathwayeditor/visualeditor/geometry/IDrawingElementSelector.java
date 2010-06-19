package org.pathwayeditor.visualeditor.geometry;

import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;

public interface IDrawingElementSelector {

	SortedSet<IDrawingElementController> findElementAt(Point p);

}
