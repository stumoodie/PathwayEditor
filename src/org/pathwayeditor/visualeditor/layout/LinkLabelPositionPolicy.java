package org.pathwayeditor.visualeditor.layout;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkLabelPositionPolicy implements ILabelLocationPolicy {
	private ILinkPointDefinition att;

	@Override
	public Point nextLabelLocation() {
		LineSegment line = att.getLinkDirection();
		Vector v = line.getRightHandNormal();
		Point midPoint = line.getMidPoint();
		LineSegment tangent = new LineSegment(midPoint, midPoint.translate(v.getIMagnitude(), v.getJMagnitude()));
		Point retVal = findIntersection(tangent);
		return retVal;
	}

	@Override
	public void setOwner(ILinkPointDefinition att) {
		this.att = att;
	}

	@Override
	public ILinkPointDefinition getOwner() {
		return this.att;
	}

	private Point findIntersection(LineSegment tangent){
		Iterator<LineSegment> lineIterator = this.att.lineSegIterator();
//		Point firstP = this.srcEndPoint;
//		Point lastP = null;
		Point retVal = null;
//		Iterator<IBendPoint> bpIter = this.att.bendPointIterator();
//		while(bpIter.hasNext() && retVal == null){
//			IBendPoint bp = bpIter.next();
//			lastP = PixelConverter.getInstance().convertLocationToPoint(bp.getLocation());
		while(lineIterator.hasNext() && retVal == null){
			retVal = calcIntersection(tangent, lineIterator.next());
		}
		return retVal;
	}
	
	private Point calcIntersection(LineSegment tangent, LineSegment l){
		Point retVal = null;
		List<Point> intn = tangent.getLinesIntersections(l);
		if(intn.size() > 0 && l.containsPoint(intn.get(0))){
			retVal = intn.get(0);
		}
		return retVal;
	}
	
}
