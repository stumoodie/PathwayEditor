package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;

public interface IFeedbackNodeTranslationEvent {

	IFeedbackNode getNode();

	Envelope oldBounds();
	
	Point getTranslation();
	
}
