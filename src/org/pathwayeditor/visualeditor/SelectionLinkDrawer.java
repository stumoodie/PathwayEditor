package org.pathwayeditor.visualeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkTerminus;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class SelectionLinkDrawer  {
	private static final double SELN_RADIUS = 5.0;
	private ILinkController linkEdge;
	private Point startPosition = Point.ORIGIN;
	private Point endPosition = Point.ORIGIN;
	
	public SelectionLinkDrawer(ILinkController linkEdge){
		this.linkEdge = linkEdge;
	}
	
	
	public double getLineLength() {
		return startPosition.getDistance(endPosition);
	}
	
	
	public Point getStartPosition() {
		return startPosition;
	}


	public void setStartPosition(Point startPosition) {
		this.startPosition = startPosition;
	}


	public Point getEndPosition() {
		return endPosition;
	}


	public void setEndPosition(Point endPosition) {
		this.endPosition = endPosition;
	}


	public void paint(Graphics2D g2d){
		drawLineSegments(g2d);
		ILinkTerminus srcTermDefaults = this.linkEdge.getDrawingElement().getSourceTerminus();
		ILinkTerminus tgtTermDefaults = this.linkEdge.getDrawingElement().getTargetTerminus();
		AffineTransform before = g2d.getTransform();
		Point srcPosn = srcTermDefaults.getLocation();
		g2d.translate(srcPosn.getX()-SELN_RADIUS, srcPosn.getY()-SELN_RADIUS);
		Ellipse2D srcSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.setColor(Color.BLACK);
		g2d.draw(srcSeln);
		g2d.setColor(Color.RED);
		g2d.fill(srcSeln);
		g2d.setTransform(before);
		Point tgtPosn = tgtTermDefaults.getLocation();
		g2d.translate(tgtPosn.getX()-SELN_RADIUS, tgtPosn.getY()-SELN_RADIUS);
		Ellipse2D tgtSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.setColor(Color.BLACK);
		g2d.draw(tgtSeln);
		g2d.setColor(Color.RED);
		g2d.fill(tgtSeln);
		g2d.setTransform(before);
	}
	
	private void drawLineSegments(Graphics2D g2d){
		ILinkPointDefinition linkDefinition = this.linkEdge.getLinkDefinition();
		g2d.setColor(Color.RED);
		g2d.setStroke(this.createStroke(2.0));

		Iterator<LineSegment> lineIterator = linkDefinition.drawnLineSegIterator();
		while(lineIterator.hasNext()){
			LineSegment lineSegment = lineIterator.next();
			Line2D.Double line = new Line2D.Double(lineSegment.getOrigin().getX(), lineSegment.getOrigin().getY(),
					lineSegment.getTerminus().getX(), lineSegment.getTerminus().getY());
			g2d.draw(line);
		}
	}

	private Stroke createStroke(double lineWidth){
		Stroke stroke = new BasicStroke((float)lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		return stroke;
	}

}
