package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;

public interface IFeedbackNodeResizeEvent {

	IFeedbackNode getNode();
	
	Envelope getOriginalBounds();

	Point getOriginDelta();

	Dimension getSizeDelta();

}
