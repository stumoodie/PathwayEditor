/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
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
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.IGraphicalLinkTerminusDefinition;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkDrawer  implements IMiniCanvas {
	private final Logger logger = Logger.getLogger(this.getClass()); 
	private final ILinkPointDefinition linkEdge;
//	private Point startPosition = Point.ORIGIN;
//	private Point endPosition = Point.ORIGIN;
	
	public LinkDrawer(ILinkPointDefinition linkEdge){
		this.linkEdge = linkEdge;
	}
	
	
//	public double getLineLength() {
//		return startPosition.getDistance(endPosition);
//	}
//	
//	
//	public Point getStartPosition() {
//		return startPosition;
//	}
//
//
//	public void setStartPosition(Point startPosition) {
//		this.startPosition = startPosition;
//	}
//
//
//	public Point getEndPosition() {
//		return endPosition;
//	}
//
//
//	public void setEndPosition(Point endPosition) {
//		this.endPosition = endPosition;
//	}


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

	@Override
	public void paint(Graphics2D g2d){
		drawLineSegments(g2d);
		IGraphicalLinkTerminusDefinition srcTermDefaults = this.linkEdge.getSourceTerminusDefinition();
		IGraphicalLinkTerminusDefinition tgtTermDefaults = this.linkEdge.getTargetTerminusDefinition();
		
		AffineTransform before = g2d.getTransform();
		ILinkPointDefinition linkDefinition = this.linkEdge;
		double srcLineAngle = linkDefinition.getSourceLineSegment().angle();
		Point srcPosn = linkDefinition.getSrcAnchorPosition();
		g2d.translate(srcPosn.getX(), srcPosn.getY());
		g2d.rotate(srcLineAngle);
		g2d.translate(srcTermDefaults.getGap(), 0);
//		Dimension srcEndSize = srcTermDefaults.getEndSize();
//		g2d.scale(srcEndSize.getWidth(), srcEndSize.getHeight());
		logger.trace("Draw src end decorator");
		drawEndDecorator(g2d, linkEdge.getLineColour(), linkEdge.getLineWidth(), srcTermDefaults.getEndDecoratorType(),
				linkDefinition.getSrcAnchorPosition(), srcTermDefaults.getEndSize());
		g2d.setTransform(before);
		double tgtLineAngle = linkDefinition.getTargetLineSegment().angle();
		Point tgtPosn = linkDefinition.getTgtAnchorPosition();
		g2d.translate(tgtPosn.getX(), tgtPosn.getY());
		g2d.rotate(tgtLineAngle);
		g2d.translate(tgtTermDefaults.getGap(), 0);
//		Dimension tgtEndSize = tgtTermDefaults.getEndSize();
//		g2d.scale(tgtEndSize.getWidth(), tgtEndSize.getHeight());
		logger.trace("Draw tgt end decorator");
		drawEndDecorator(g2d, linkEdge.getLineColour(), linkEdge.getLineWidth(), tgtTermDefaults.getEndDecoratorType(),
				linkDefinition.getTgtAnchorPosition(), tgtTermDefaults.getEndSize());
		g2d.setTransform(before);
	}
	
	
	private void drawLineSegments(Graphics2D g2d){
		ILinkPointDefinition linkDefinition = this.linkEdge;
		Colour lineCol = this.linkEdge.getLineColour();
		g2d.setColor(new Color(lineCol.getRgb().getRed(), lineCol.getRgb().getGreen(), lineCol.getRgb().getBlue(), lineCol.getAlpha()));
		double lineWidth = this.linkEdge.getLineWidth();
		LineStyle lineStyle = this.linkEdge.getLineStyle();
		g2d.setStroke(this.createStroke(lineStyle, lineWidth));

		Iterator<LineSegment> lineIterator = linkDefinition.drawnLineSegIterator();
		while(lineIterator.hasNext()){
			LineSegment lineSegment = lineIterator.next();
			Line2D.Double line = new Line2D.Double(lineSegment.getOrigin().getX(), lineSegment.getOrigin().getY(),
					lineSegment.getTerminus().getX(), lineSegment.getTerminus().getY());
			if(logger.isTraceEnabled()){
				logger.trace("Drawing line: " + lineSegment);
			}
			g2d.draw(line);
		}
	}
	
	private void drawEndDecorator(Graphics2D g2d, Colour lineColour, double lineWidth,
			LinkEndDecoratorShape endShape, Point startPos, Dimension size){
		if(logger.isTraceEnabled()){
			logger.trace("Draw End Dec: startPos=" + startPos + ",size=" + size + ",type=" + endShape);
		}
		if(!endShape.equals(LinkEndDecoratorShape.NONE)){
			g2d.setColor(new Color(lineColour.getRgb().getRed(), lineColour.getRgb().getGreen(), lineColour.getRgb().getBlue(), lineColour.getAlpha()));
			if(endShape.equals(LinkEndDecoratorShape.ARROW)){
				GeneralPath path = createTriangle(size);
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.BAR)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createBar(size);
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DIAMOND)){
				GeneralPath path = createDiamond(size);
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DOUBLE_ARROW)){
				GeneralPath path = createDoubleTriangle(size);
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.DOUBLE_BAR)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createDoubleBar(size);
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_CIRCLE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				Shape circle = createCircle(size);
				g2d.draw(circle);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_DIAMOND)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createDiamond(size);
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_SQUARE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				double wid = size.getWidth();
				double height = size.getHeight();
				g2d.draw(new Rectangle2D.Double(0.0, 0.0, 1.0*wid, 1.0*height));
			}
			else if(endShape.equals(LinkEndDecoratorShape.EMPTY_TRIANGLE)){
				g2d.setStroke(this.createStroke(LineStyle.SOLID, lineWidth));
				GeneralPath path = createTriangle(size);
				g2d.draw(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.SQUARE)){
				double wid = size.getWidth();
				double height = size.getHeight();
				g2d.fill(new Rectangle2D.Double(0.0, 0.0, 1.0*wid, 1.0*height));
			
			}
			else if(endShape.equals(LinkEndDecoratorShape.TRIANGLE)){
				GeneralPath path = createTriangle(size);
				g2d.fill(path);
			}
			else if(endShape.equals(LinkEndDecoratorShape.TRIANGLE_BAR)){
				GeneralPath path = createTriangleBar(size);
				g2d.draw(path);
			}
		}
	}
	
	private Shape createCircle(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		double xPos = 0.0*x;
		double yPos = -0.5*y;
		Ellipse2D circle = new Ellipse2D.Double(xPos, yPos, 1.0*x, 1.0*y);
		return circle;
	}
	
	private GeneralPath createTriangle(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0*x, 0.0*y);
		path.lineTo(1.0*x, 0.5*y);
		path.lineTo(1.0*x, -0.5*y);
		path.closePath();
		return path;
	}
	private GeneralPath createTriangleBar(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(1.0*x, 0.5*y);
		path.lineTo(1.0*x, -0.5*y);
		path.closePath();
		path.moveTo(0.0*x, 0.0*y);
		path.lineTo(0.5*x, 0.5*y);
		path.lineTo(0.5*x, -0.5*y);
		path.closePath();
		return path;
	}
	private GeneralPath createDoubleTriangle(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0*x, 0.0*y);
		path.lineTo(0.5*x, 0.5*y);
		path.lineTo(0.5*x, -0.5*y);
		path.closePath();
		path.moveTo(0.5*x, 0.0*y);
		path.lineTo(1.0*x, 0.5*y);
		path.lineTo(1.0*x, -0.5*y);
		path.closePath();
		return path;
	}
	
	private GeneralPath createBar(Dimension size){
		double wid = size.getWidth();
		double height = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0*wid, 0.5*height);
		path.lineTo(0.0*wid, -0.5*height);
		path.moveTo(0.0*wid, 0.0*height);
		path.lineTo(1.0*wid, 0.0*height);
		return path;
	}
	
	private GeneralPath createDoubleBar(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0*x, 0.5*y);
		path.lineTo(0.0*x, -0.5*y);
		path.moveTo(0.5*x, 0.5*y);
		path.lineTo(0.5*x, -0.5*y);
		path.moveTo(0.5*x, 0.0*y);
		path.lineTo(1.0*x, 0.0*y);
		return path;
	}
	
	private GeneralPath createDiamond(Dimension size){
		double x = size.getWidth();
		double y = size.getHeight();
		GeneralPath path = new GeneralPath();
		path.moveTo(0*x, 0*y);
		path.lineTo(0.5*x, 0.5*y);
		path.lineTo(1.0*x, 0.0*y);
		path.lineTo(0.5*x, -0.5*y);
		path.closePath();
		return path;
	}


	@Override
	public Envelope getBounds() {
		return this.linkEdge.getBounds();
	}
}
