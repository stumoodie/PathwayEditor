package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public interface ILinkOperation {

	void newBendPointStarted(int lineSegmentIdx);
	
	void newBendPointOngoing(int lineSegmentIdx, Point position);
	
	void newBendPointFinished(int lineSegmentIdx, Point position);
	
	void moveBendPointStated(int bendPointIdx);

	void moveBendPointOngoing(int bendPointIdx, Point delta);
	
	void moveBendPointFinished(int bendPointIdx, Point delta);
	
}
