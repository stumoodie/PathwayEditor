package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.Point;


public interface ILinkBendPointHandleShape extends IHandleShape {

	Iterator<Point> pointIterator();

}
