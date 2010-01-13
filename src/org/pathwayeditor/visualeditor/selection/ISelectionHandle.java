package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionHandle extends Comparable<ISelectionHandle> {
	enum SelectionRegion { Central, N, NE, E, SE, S, SW, W, NW };
	
	ISelection getSelection();
	
	Envelope getBounds();
	
	SelectionRegion getRegion();
	
	INodeController getNodeController();

	boolean containsPoint(Point point);

	void translate(Point delta);
}
