package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public interface INodeResizeEvent {

	INodeController getChangedNode();

	Point getOriginDelta();

	Dimension getSizeDelta();

}
