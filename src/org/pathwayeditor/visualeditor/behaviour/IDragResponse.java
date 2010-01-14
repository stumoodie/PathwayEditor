package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface IDragResponse {

	void dragStarted();
	
	void dragContinuing(Point delta);

	void dragFinished(Point delta);
}
