package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public interface IMarqueeOperation {

	void selectionContinuing(Point originDelta, Dimension resizeDelta);

	void selectionFinished(Point originDelta, Dimension resizeDelta);

	void selectionStarted(Point initialPosnm);

}
