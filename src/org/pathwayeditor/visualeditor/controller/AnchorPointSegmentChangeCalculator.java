package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegmentVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.IStraightLineCurveSegment;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public class AnchorPointSegmentChangeCalculator {
	private List<ICurveSegment> origCurveSegments;
	private List<ICurveSegment> replacementCurveSegments;
	private Point newAnchorPosn = Point.ORIGIN;
	private ICurveSegment newAssociatedCurveSegment;
	private double lengthRatio;
	private boolean calculated;
	
	
	public AnchorPointSegmentChangeCalculator(List<ICurveSegment> originalSegments,	List<ICurveSegment> replacementSegments) {
		this.origCurveSegments = originalSegments;
		this.replacementCurveSegments = replacementSegments;
	}

	public void calculateNewCurveAssociation(final Point anchorLocation) {
		this.lengthRatio = 0;
		calculated = false;
		for(final ICurveSegment seg : this.origCurveSegments){
			seg.visit(new ICurveSegmentVisitor() {
				@Override
				public void visitStraightLineCurveSegment(IStraightLineCurveSegment v) {
					LineSegment line = v.getLineSegment();
					if(line.containsPoint(anchorLocation)){
						LineSegment toPt = new LineSegment(line.getOrigin(), anchorLocation);
						double toPtlen = toPt.getLength();
						double segLen = line.getLength();
						lengthRatio += toPtlen/segLen;
						calculated = true;
					}
					else if(!calculated){
						lengthRatio += 1;
					}
				}
			});
		}
		if(this.replacementCurveSegments.size() == 2){
			if(lengthRatio < 0.5){
				ICurveSegment firstSeg = this.replacementCurveSegments.get(0);
				final double calculatedRatio = lengthRatio*2;
				firstSeg.visit(new NewAnchorCalc(calculatedRatio));
			}
			else{
				final double calculatedRatio = (lengthRatio - 0.5) * 2;
				ICurveSegment secondSeg = this.replacementCurveSegments.get(1);
				secondSeg.visit(new NewAnchorCalc(calculatedRatio));
			}
		}
		else{
			final double calculatedRatio = lengthRatio/2.0;
			ICurveSegment seg = this.replacementCurveSegments.get(0);
			seg.visit(new NewAnchorCalc(calculatedRatio));
		}
	}

	public Point getNewAnchorPosn() {
		return this.newAnchorPosn ;
	}

	public ICurveSegment getNewAssociatedCurveSegment() {
		return this.newAssociatedCurveSegment;
	}
	
	private class NewAnchorCalc implements ICurveSegmentVisitor{
		private final double calculatedRatio;


		public NewAnchorCalc(double ratio){
			this.calculatedRatio = ratio;
		}
		
		
		@Override
		public void visitStraightLineCurveSegment(IStraightLineCurveSegment v) {
			LineSegment line = v.getLineSegment();
			LineSegment toAnchor = line.newLineSegment(calculatedRatio*line.length());
			newAnchorPosn = toAnchor.getTerminus();
			newAssociatedCurveSegment = v;
		}
		
	}

}
