package org.pathwayeditor.graphicsengine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPoint;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public class LinkPointDefinition {
	private static final int SRC_IDX = 0;
	private final List<Point> pointList;
	private final ILinkAttribute link;
	
	public LinkPointDefinition(ILinkAttribute link){
		this.link = link;
		this.pointList = new ArrayList<Point>(link.numBendPoints()+2);
		this.pointList.add(link.getSourceTerminus().getLocation());
		Iterator<IBendPoint> iter = link.bendPointIterator();
		while(iter.hasNext()){
			IBendPoint bp = iter.next();
			this.pointList.add(bp.getLocation());
		}
		this.pointList.add(link.getTargetTerminus().getLocation());
	}
	
	public void setSrcAnchorPosition(Point newPosn){
		this.pointList.set(SRC_IDX, newPosn);
	}

	public void setTgtAnchorPosition(Point newPosn){
		this.pointList.set(this.pointList.size()-1, newPosn);
	}

	public void setBendPointPosition(int bpIdx, Point newPosn){
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		// adjust for fact the src anchor is 1st element
		this.pointList.set(bpIdx+1, newPosn);
	}
	
	public Point getSrcEndPoint(){
		return this.pointList.get(SRC_IDX);
	}
	
	public Point getTgtEndPoint(){
		return this.pointList.get(this.pointList.size()-1);
	}
	
	public Point getBendPointPosition(int bpIdx){
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		// adjust for fact the src anchor is 1st element
		return this.pointList.get(bpIdx+1);
	}
	
	public LineSegment getSourceLineSegment(){
		return new LineSegment(this.pointList.get(SRC_IDX), this.pointList.get(SRC_IDX+1));
	}

	private static Point calcFirstDrawnPoint(LineSegment original, Dimension decoratorSize, double gapLen){
		double totalOffset = gapLen + decoratorSize.getWidth();
		double theta = original.angle();
		double x = totalOffset * Math.cos(theta);
		double y = totalOffset * Math.sin(theta);
		return original.getOrigin().translate(x, y);
	}
	
	/**
	 * Gets the line segment starting at the tgt anchor point and ending at the last bendpoint or the src
	 * anchor point if there are no bend-points.
	 * @return
	 */
	public LineSegment getTargetLineSegment(){
		int lastIdx = this.pointList.size()-1;
		return new LineSegment(this.pointList.get(lastIdx), this.pointList.get(lastIdx-1));
	}
	
	
	public Iterator<Point> pointIterator(){
		return this.pointList.iterator();
	}
	
	public Iterator<LineSegment> lineSegIterator(){
		List<LineSegment> retVal = new LinkedList<LineSegment>();
		Point firstP = this.getSrcEndPoint();
		Point lastP = null;
		for(int i = 1; i < this.pointList.size(); i++){
			lastP = this.pointList.get(i);
			retVal.add(new LineSegment(firstP, lastP));
			firstP = lastP;
		} 
		return retVal.iterator();
	}

	
	public Iterator<LineSegment> drawnLineSegIterator(){
		List<LineSegment> retVal = new LinkedList<LineSegment>();
		Point firstP = calcFirstDrawnPoint(this.getSourceLineSegment(), this.link.getSourceTerminus().getEndSize(), this.link.getSourceTerminus().getGap());
		Point lastP = null;
		for(int i = 1; i < this.pointList.size() -1; i++){
			lastP = this.pointList.get(i);
			retVal.add(new LineSegment(firstP, lastP));
			firstP = lastP;
		}
		lastP = calcFirstDrawnPoint(this.getTargetLineSegment(), this.link.getTargetTerminus().getEndSize(), this.link.getTargetTerminus().getGap());
		retVal.add(new LineSegment(firstP, lastP));
		return retVal.iterator();
	}

	public void addNewBendPoint(int bpIdx, Point bpPosn) {
		if(bpIdx > this.numBendPoints()) throw new IllegalArgumentException("Bendpoint index is outseide permitted range: " + bpIdx);
		this.pointList.add(bpIdx+1, bpPosn);
	}
	
	public void addNewBendPoint(Point bpPosn) {
		this.pointList.add(this.numBendPoints()+1, bpPosn);
	}
	
	public int numPoints(){
		return this.pointList.size();
	}
	
	public int numBendPoints(){
		return this.pointList.size()-2;
	}

	public void removeBendPoint(int bpIdx) {
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		this.pointList.remove(bpIdx+1);
	}

	/**
	 * Provides the line that defines the direction of the link, going from the src end-point to the
	 * target end-point. 
	 * @return the line segment defining the direction of the line from src to tgt.
	 */
	public LineSegment getLinkDirection() {
		return new LineSegment(this.getSrcEndPoint(), this.getTgtEndPoint());
	}
}
