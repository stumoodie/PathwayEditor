package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {

//	void nodePrimarySelection(INodeController nodeController);
//
//	void addSecondarySelection(INodeController nodeController);

//	void clearSelection();

	void moveFinished(Point delta);

	void moveStarted();

	void moveOngoing(Point delta);

//	boolean isNodeSelected(INodeController nodeController);

}
