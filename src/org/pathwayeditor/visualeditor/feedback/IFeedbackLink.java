package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface IFeedbackLink extends IFeedbackElement {

	ILinkPointDefinition getLinkDefinition();
	
	void moveBendPoint(int bpIdx, Point newLocation);

	void newBendPoint(int lineSegmentIdx, Point initialLocation);

	void translateBendPoint(int bpIdx, Point delta);
	
}
