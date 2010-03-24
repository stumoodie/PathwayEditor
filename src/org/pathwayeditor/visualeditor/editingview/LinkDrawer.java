package org.pathwayeditor.visualeditor.editingview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.IGraphicalLinkTerminusDefinition;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkDrawer  {
	private final Logger logger = Logger.getLogger(this.getClass()); 
	private ILinkPointDefinition linkEdge;
	private Point startPosition = Point.ORIGIN;
	private Point endPosition = Point.ORIGIN;
	
	public LinkDrawer(ILinkPointDefinition linkEdge){
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


	private Stroke createStroke(LineStyle lineStyle, double lineWidth){
		Stroke stroke = null;
		if(lineStyle.equals(LineStyle.SOLID)){
			stroke = new BasicStroke((float)lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		}
		else{
			float dash[] = new float[] {};
			if(lineStyle.equals(LineStyle.DASHED)){
				dash = new float[] { 10.0f };
			}
			else if(lineStyle.equals(LineStyle.DASH_DOT)){
				dash = new float[] { 10.0f, 10.0f, 3.0f, 3.0f };
			}
			else if(lineStyle.equals(LineStyle.DOT)){
				dash = new float[] { 2.0f, 4.0f };
			}
			else if(lineStyle.equals(LineStyle.DASH_DOT_DOT)){
				dash = new float[] { 10.0f, 10.0f, 3.0f, 3.0f, 3.0f, 3.0f };
			}
			stroke = new BasicStroke((float)lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
		}
		return stroke;
	}

	public void paint(Graphics2D g2d){
		drawLineSegments(g2d);
		IGraphicalLinkTerminusDefinition srcTermDefaults = this.linkEdge.getSourceTerminusDefinition();
		IGraphicalLinkTerminusDefinition tgtTermDefaults = this.linkEdge.getTargetTerminusDefinition();
		
		AffineTransform before = g2d.getTransform();
		ILinkPointDefinition linkDefinition = this.linkEdge;
		double srcLineAngle = linkDefinition.getSourceLineSegment().angle();
		Point srcPosn = linkDefinition.getSrcEndPoint();
		g2d.translate(srcPosn.getX(), srcPosn.getY());
		g2d.rotate(srcLineAngle);
		g2d.translate(srcTermDefaults.getGap(), 0);
		Dimension srcEndSize = srcTermDefaults.getEndSize();
		g2d.scale(srcEndSize.getWidth(), srcEndSize.getHeight());
		drawEndDecorator(g2d, linkEdge.getLineColour(), linkEdge.getLineWidth(), srcTermDefaults.getEndDecoratorType(),
				linkDefinition.getSrcEndPoint(), srcTermDefaults.getEndSize());
		g2d.setTransform(before);
		double tgtLineAngle = linkDefinition.getTargetLineSegment().angle();
		Point tgtPosn = linkDefinition.getTgtEndPoint();
		g2d.translate(tgtPosn.getX(), tgtPosn.getY());
		g2d.rotate(tgtLineAngle);
		g2d.translate(tgtTermDefaults.getGap(), 0);
		Dimension tgtEndSize = tgtTermDefaults.getEndSize();
		g2d.scale(tgtEndSize.getWidth(), tgtEndSize.getHeight());
		drawEndDecorator(g2d, linkEdge.getLineColour(), linkEdge.getLineWidth(), tgtTermDefaults.getEndDecoratorType(),
				linkDefinition.getSrcEndPoint(), tgtTermDefaults.getEndSize());
		g2d.setTransform(before);
	}
	
	
	private void drawLineSegments(Graphics2D g2d){
		ILinkPointDefinition linkDefinition = this.linkEdge;
		RGB lineCol = this.linkEdge.getLineColour();
		g2d.setColor(new Color(lineCol.getRed(), lineCol.getGreen(), lineCol.getBlue()));
		double lineWidth = this.linkEdge.getLineWidth();
		LineStyle lineStyle = this.linkEdge.getLineStyle();
		g2d.setStroke(this.createStroke(lineStyle, lineWidth));

		Iterator<LineSegment> lineIterator = linkDefinition.drawnLineSegIterator();
		while(lineIterator.hasNext()){
			LineSegment lineSegment = lineIterator.next();
			Line2D.Double line = new Line2D.Double(lineSegment.getOrigin().getX(), lineSegment.getOrigin().getY(),
					lineSegment.getTerminus().getX(), lineSegment.getTerminus().getY());
			logger.debug("Drawing line: " + lineSegment);
			g2d.draw(line);
		}
	}
	
	private void drawEndDecorator(Graphics2D g2d, RGB lineColour, double lineWidth,
			LinkEndDecoratorShape endShape, Point startPos, Dimension size){
		logger.debug("Draw End Dec: startPos=" + startPos + ",size=" + size + ",type=" + endShape);
		if(!endShape.equals(LinkEndDecoratorShape.NONE)){
			g2d.setColor(new Color(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue()));
			if(endShape.equals(LinkEndDecoratorShape.ARROW)){
				GeneralPath path = createTriangle();
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.BAR)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createBar();
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DIAMOND)){
				GeneralPath path = createDiamond();
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DOUBLE_ARROW)){
				GeneralPath path = createDoubleTriangle();
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DOUBLE_BAR)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createDoubleBar();
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_CIRCLE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				Shape circle = createCircle();
				g2d.draw(circle);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_DIAMOND)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createDiamond();
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_SQUARE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				g2d.draw(new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0));
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_TRIANGLE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createTriangle();
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.SQUARE)){
				g2d.fill(new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0));
			
			}
			else if(endShape.equals(LinkEndDecoratorShape.TRIANGLE)){
				GeneralPath path = createTriangle();
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.TRIANGLE_BAR)){
				GeneralPath path = createTriangleBar();
				g2d.draw(path);
			}
		}
	}
	
	private Shape createCircle(){
		double xPos = 0.0;
		double yPos = 0.5;
		Ellipse2D circle = new Ellipse2D.Double(xPos, yPos, 1.0, 1.0);
		return circle;
	}
	
	private GeneralPath createTriangle(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0, 0.0);
		path.lineTo(1.0, 0.5);
		path.lineTo(1.0, -0.5);
		path.closePath();
		return path;
	}
	private GeneralPath createTriangleBar(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0, 0.5);
		path.lineTo(0.0, -0.5);
		path.moveTo(0.0, 0.0);
		path.lineTo(1.0, 0.5);
		path.lineTo(1.0, -0.5);
		path.closePath();
		return path;
	}
	private GeneralPath createDoubleTriangle(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0, 0.0);
		path.lineTo(0.5, 0.5);
		path.lineTo(0.5, -0.5);
		path.closePath();
		path.moveTo(0.5, 0.0);
		path.lineTo(1.0, 0.5);
		path.lineTo(1.0, -0.5);
		path.closePath();
		return path;
	}
	
	private GeneralPath createBar(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0, 0.5);
		path.lineTo(0.0, -0.5);
		path.moveTo(0.0, 0.0);
		path.lineTo(1.0, 0.0);
		return path;
	}
	
	private GeneralPath createDoubleBar(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0, 0.5);
		path.lineTo(0.0, -0.5);
		path.moveTo(0.5, 0.5);
		path.lineTo(0.5, -0.5);
		path.moveTo(0.5, 0.0);
		path.lineTo(1.0, 0.0);
		return path;
	}
	
	private GeneralPath createDiamond(){
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(0.5, 0.5);
		path.lineTo(1.0, 0.0);
		path.lineTo(0.5, -0.5);
		path.closePath();
		return path;
	}
}
