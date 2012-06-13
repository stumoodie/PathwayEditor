package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegmentVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.IStraightLineCurveSegment;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public class AnchorPointChangeCalculator implements ICurveSegmentVisitor {
	private final Logger logger = Logger.getLogger(this.getClass());
	private Point anchorPosn = Point.ORIGIN;
	private List<Point> oldPositions;
	private List<Point> newPositions;
	private Point newAnchorPosn;

	public AnchorPointChangeCalculator(List<Point> oldPosition, List<Point> newPosition) {
		this.oldPositions = oldPosition;
		this.newPositions = newPosition;
	}

	@Override
	public void visitStraightLineCurveSegment(IStraightLineCurveSegment v) {
		LineSegment origSeg = new LineSegment(this.oldPositions.get(0), this.oldPositions.get(1));
		if(logger.isTraceEnabled() && !origSeg.containsPoint(anchorPosn)){
			logger.error("This anchor point does not lie on the line segment!. pt=" + this.anchorPosn +", LineSeg=" + origSeg);
		}
		LineSegment origToAnchor = new LineSegment(this.oldPositions.get(0), this.anchorPosn);
		double scaleFactor = origToAnchor.getLength()/origSeg.length();
		if(logger.isTraceEnabled()){
			logger.trace("origSeg=" + this.oldPositions + ",origToAnchor=" + origToAnchor + ",scaleFactor=" + scaleFactor);
		}
		LineSegment newSeg = new LineSegment(this.newPositions.get(0), this.newPositions.get(1));
		double newLength = scaleFactor * newSeg.length();
		LineSegment newToAnchor = newSeg.newLineSegment(newLength);
		this.newAnchorPosn = newToAnchor.getTerminus();
		if(logger.isTraceEnabled()){
			logger.trace("newSeg=" + this.newPositions + ",newToAnchor=" + newToAnchor + ",newAnchorPos=" + this.newAnchorPosn);
		}
		if(logger.isTraceEnabled() && !newSeg.containsPoint(newAnchorPosn)){
			logger.error("The recalculated anchor point does not lie on the line segment!. pt=" + this.newAnchorPosn +", LineSeg=" + newSeg);
		}
	}

	public void setAnchorPosn(Point centre) {
		this.anchorPosn = centre;
	}

	public Point getNewAnchorPosn() {
		return newAnchorPosn;
	}

	public void setNewAnchorPosn(Point newAnchorPosn) {
		this.newAnchorPosn = newAnchorPosn;
	}

	public Point getAnchorPosn() {
		return anchorPosn;
	}

}
