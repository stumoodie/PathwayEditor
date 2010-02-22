package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.Point;

public interface ILinkMidLineHandleShape extends IHandleShape {

	Iterator<Point> pointIterator();

}
