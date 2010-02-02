package org.pathwayeditor.visualeditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkTerminus;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;

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
		ILinkTerminus srcTermDefaults = this.linkEdge.getDrawingElement().getSourceTerminus();
		ILinkTerminus tgtTermDefaults = this.linkEdge.getDrawingElement().getTargetTerminus();
		AffineTransform before = g2d.getTransform();
		Point srcPosn = srcTermDefaults.getLocation();
		g2d.translate(srcPosn.getX()-SELN_RADIUS, srcPosn.getY()-SELN_RADIUS);
		g2d.setColor(Color.RED);
		Ellipse2D srcSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.draw(srcSeln);
		g2d.fill(srcSeln);
		g2d.setTransform(before);
		Point tgtPosn = tgtTermDefaults.getLocation();
		g2d.translate(tgtPosn.getX()-SELN_RADIUS, tgtPosn.getY()-SELN_RADIUS);
		Ellipse2D tgtSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.draw(tgtSeln);
		g2d.fill(tgtSeln);
		g2d.setTransform(before);
	}
	
	
}
