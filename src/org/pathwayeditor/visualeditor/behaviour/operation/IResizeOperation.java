package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public interface IResizeOperation {

	void resizeStarted();

	void resizeFinished(Point originDelta, Dimension resizeDelta);

	void resizeContinuing(Point originDelta, Dimension resizeDelta);

	boolean canResize(Point originDelta, Dimension resizeDelta);

}
