package org.pathwayeditor.visualeditor.geometry;

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public interface ILinkPointDefinition {

	void setSrcAnchorPosition(Point newPosn);

	void setTgtAnchorPosition(Point newPosn);

	void setBendPointPosition(int bpIdx, Point newPosn);

	Point getSrcEndPoint();

	Point getTgtEndPoint();

	Point getBendPointPosition(int bpIdx);

	LineSegment getSourceLineSegment();

	/**
	 * Gets the line segment starting at the tgt anchor point and ending at the last bendpoint or the src
	 * anchor point if there are no bend-points.
	 * @return
	 */
	LineSegment getTargetLineSegment();

	Iterator<Point> pointIterator();

	Iterator<LineSegment> lineSegIterator();

	Iterator<LineSegment> drawnLineSegIterator();

	void addNewBendPoint(int bpIdx, Point bpPosn);

	void addNewBendPoint(Point bpPosn);

	int numPoints();

	int numBendPoints();

	void removeBendPoint(int bpIdx);

	/**
	 * Provides the line that defines the direction of the link, going from the src end-point to the
	 * target end-point. 
	 * @return the line segment defining the direction of the line from src to tgt.
	 */
	LineSegment getLinkDirection();

//	Envelope getBounds();
	
	boolean containsPoint(Point p, double lineWidthTolerance);
}