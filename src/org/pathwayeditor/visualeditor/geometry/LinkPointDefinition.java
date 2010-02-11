package org.pathwayeditor.visualeditor.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPoint;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public class LinkPointDefinition implements ILinkPointDefinition {
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
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setSrcAnchorPosition(org.pathwayeditor.figure.geometry.Point)
	 */
	public void setSrcAnchorPosition(Point newPosn){
		this.pointList.set(SRC_IDX, newPosn);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setTgtAnchorPosition(org.pathwayeditor.figure.geometry.Point)
	 */
	public void setTgtAnchorPosition(Point newPosn){
		this.pointList.set(this.pointList.size()-1, newPosn);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setBendPointPosition(int, org.pathwayeditor.figure.geometry.Point)
	 */
	public void setBendPointPosition(int bpIdx, Point newPosn){
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		// adjust for fact the src anchor is 1st element
		this.pointList.set(bpIdx+1, newPosn);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getSrcEndPoint()
	 */
	public Point getSrcEndPoint(){
		return this.pointList.get(SRC_IDX);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getTgtEndPoint()
	 */
	public Point getTgtEndPoint(){
		return this.pointList.get(this.pointList.size()-1);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getBendPointPosition(int)
	 */
	public Point getBendPointPosition(int bpIdx){
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		// adjust for fact the src anchor is 1st element
		return this.pointList.get(bpIdx+1);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getSourceLineSegment()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getTargetLineSegment()
	 */
	public LineSegment getTargetLineSegment(){
		int lastIdx = this.pointList.size()-1;
		return new LineSegment(this.pointList.get(lastIdx), this.pointList.get(lastIdx-1));
	}
	
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#pointIterator()
	 */
	public Iterator<Point> pointIterator(){
		return this.pointList.iterator();
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#lineSegIterator()
	 */
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

	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#drawnLineSegIterator()
	 */
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

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#addNewBendPoint(int, org.pathwayeditor.figure.geometry.Point)
	 */
	public void addNewBendPoint(int bpIdx, Point bpPosn) {
		if(bpIdx > this.numBendPoints()) throw new IllegalArgumentException("Bendpoint index is outseide permitted range: " + bpIdx);
		this.pointList.add(bpIdx+1, bpPosn);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#addNewBendPoint(org.pathwayeditor.figure.geometry.Point)
	 */
	public void addNewBendPoint(Point bpPosn) {
		this.pointList.add(this.numBendPoints()+1, bpPosn);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#numPoints()
	 */
	public int numPoints(){
		return this.pointList.size();
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#numBendPoints()
	 */
	public int numBendPoints(){
		return this.pointList.size()-2;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#removeBendPoint(int)
	 */
	public void removeBendPoint(int bpIdx) {
		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
		
		this.pointList.remove(bpIdx+1);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getLinkDirection()
	 */
	public LineSegment getLinkDirection() {
		return new LineSegment(this.getSrcEndPoint(), this.getTgtEndPoint());
	}

	@Override
	public boolean containsPoint(Point p, double lineWidthTolerence) {
		boolean retVal = false;
		Iterator<LineSegment> lineSegIter = this.drawnLineSegIterator();
		while(lineSegIter.hasNext() && !retVal){
			LineSegment seg = lineSegIter.next();
			retVal = seg.containsPoint(p, lineWidthTolerence);
		}
		return retVal;
	}

//	@Override
//	public Envelope getBounds() {
//		double minX = Double.MAX_VALUE;
//		double maxX = Double.MIN_VALUE;
//		double minY = Double.MAX_VALUE;
//		double maxY = Double.MIN_VALUE;
//		for(Point p : this.pointList){
//			minX = Math.min(minX, p.getX());
//			maxX = Math.max(maxX, p.getX());
//			minY = Math.min(minY, p.getY());
//			maxY = Math.max(maxY, p.getY());
//		}
//		return new Envelope(minX, minY, maxX-minX, maxY-minY);
//	}
}
