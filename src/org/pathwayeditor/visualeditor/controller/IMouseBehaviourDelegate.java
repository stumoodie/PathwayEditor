package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Point;

public interface IMouseBehaviourDelegate {

	INodePrimitive findDrawingNodeAt(Point location);

}
