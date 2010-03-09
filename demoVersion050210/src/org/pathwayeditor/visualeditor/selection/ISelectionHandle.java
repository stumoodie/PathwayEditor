package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionHandle extends Comparable<ISelectionHandle> {
	enum SelectionHandleType { Central, N, NE, E, SE, S, SW, W, NW, None };
	
	ISelection getSelection();
	
	Envelope getBounds();
	
	SelectionHandleType getType();
	
	INodeController getNodeController();

	boolean containsPoint(Point point);

	void translate(Point delta);
}
