package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {

	void nodePrimarySelection(INodePrimitive nodeController);

	void addSecondarySelection(INodePrimitive nodeController);

	void clearSelection();

	void moveFinished(Point delta);

	void moveStarted();

	void moveOngoing(Point delta);

	boolean isNodeSelected(INodePrimitive nodeController);

}
