package org.pathwayeditor.visualeditor.geometry;

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;
import org.pathwayeditor.figure.rendering.IAnchorLocator;

public class Link2LinkAnchorLocator implements IAnchorLocator {
	private Point requestedPoint;
	private Point otherEndPoint;
	private final ILinkPointDefinition linkDefn;
	
	public Link2LinkAnchorLocator(ILinkPointDefinition linkDefn) {
		this.linkDefn = linkDefn;
	}
	
	@Override
	public void setOtherEndPoint(Point otherEndPoint) {
		this.otherEndPoint = otherEndPoint;
	}

	@Override
	public Point getOtherEndPoint() {
		return this.otherEndPoint;
	}

	@Override
	public boolean canCalcAnchorPosition() {
		return this.requestedPoint != null;
	}

	@Override
	public Point calcAnchorPosition() {
		boolean found = false;
		Point closestPoint = Point.ORIGIN;
		Iterator<LineSegment> lsIter = this.linkDefn.drawnLineSegIterator();
		while(lsIter.hasNext() && !found){
			LineSegment line = lsIter.next(); 
			Point start = line.getOrigin();
			Point end = line.getTerminus();
			Vector ap = new Vector(this.requestedPoint.getX()-start.getX(), this.requestedPoint.getY()-start.getY(), 0.0);
			Vector ab = new Vector(end.getX()-start.getX(), end.getY()-start.getY(), 0.0);
			double aqMag = ap.scalarProduct(ab)/ab.magnitude();
			Vector aq = ab.unitVector().scale(aqMag);
			Point q = start.translate(aq.getIMagnitude(), aq.getJMagnitude());
			//double sqrDist = q.getSqrDistance(anchorPoint);
			if(line.containsPoint(q)){
				closestPoint = q;
				found = true;
			}
		}
		return closestPoint;
	}

	@Override
	public void setRequestedPoint(Point requestedPoint) {
		this.requestedPoint = requestedPoint;
	}

	@Override
	public Point getRequestedPoint() {
		return this.requestedPoint;
	}

}
