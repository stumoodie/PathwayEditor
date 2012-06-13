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
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkTerminus;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.selection.ICentralSelectionHandleShape;
import org.pathwayeditor.visualeditor.selection.ICornerSelectionHandleShape;
import org.pathwayeditor.visualeditor.selection.IHandleShapeDrawer;
import org.pathwayeditor.visualeditor.selection.ILinkBendPointHandleShape;
import org.pathwayeditor.visualeditor.selection.ILinkMidLineHandleShape;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.ILinkSelectionHandleShape;
import org.pathwayeditor.visualeditor.selection.IMidPointSelectionHandleShape;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionLinkDrawer implements IMiniCanvas {
	private static final double SELN_RADIUS = 5.0;
	private final ILinkSelection selection;
	private Point startPosition = Point.ORIGIN;
	private Point endPosition = Point.ORIGIN;
	
	public SelectionLinkDrawer(ILinkSelection selection){
		this.selection = selection;
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


	@Override
	public void paint(Graphics2D g2d){
		IHandleShapeDrawer handleDrawer = new HandleDrawer(g2d);
		for(ISelectionHandle handle : this.selection.getSelectionHandle(SelectionHandleType.Link)){
			handle.drawShape(handleDrawer);
		}
		ILinkController linkEdge = this.selection.getPrimitiveController();
		ILinkTerminus srcTermDefaults = linkEdge.getAssociatedAttribute().getSourceTerminus();
		ILinkTerminus tgtTermDefaults = linkEdge.getAssociatedAttribute().getTargetTerminus();
		AffineTransform before = g2d.getTransform();
		Point srcPosn = srcTermDefaults.getLocation();
		g2d.translate(srcPosn.getX()-SELN_RADIUS, srcPosn.getY()-SELN_RADIUS);
		Ellipse2D srcSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.setColor(Color.RED);
		g2d.fill(srcSeln);
		g2d.setColor(Color.BLACK);
		g2d.setStroke(this.createStroke(1.0));
		g2d.draw(srcSeln);
		g2d.setTransform(before);
		Point tgtPosn = tgtTermDefaults.getLocation();
		g2d.translate(tgtPosn.getX()-SELN_RADIUS, tgtPosn.getY()-SELN_RADIUS);
		Ellipse2D tgtSeln = new Ellipse2D.Double(0, 0, SELN_RADIUS*2.0, SELN_RADIUS*2.0); 
		g2d.setColor(Color.RED);
		g2d.fill(tgtSeln);
		g2d.setColor(Color.BLACK);
		g2d.setStroke(this.createStroke(1.0));
		g2d.draw(tgtSeln);
		g2d.setTransform(before);
		for(ISelectionHandle handle : this.selection.getSelectionHandle(SelectionHandleType.LinkMidPoint)){
			handle.drawShape(handleDrawer);
		}
		for(ISelectionHandle handle : this.selection.getSelectionHandle(SelectionHandleType.LinkBendPoint)){
			handle.drawShape(handleDrawer);
		}
	}
	
//	private void drawLineSegments(Graphics2D g2d){
//		ILinkController linkEdge = this.selection.getPrimitiveController();
//		ILinkPointDefinition linkDefinition = linkEdge.getLinkDefinition();
//		g2d.setColor(Color.RED);
//		g2d.setStroke(this.createStroke(1.0));
//
//		Iterator<LineSegment> lineIterator = linkDefinition.drawnLineSegIterator();
//		while(lineIterator.hasNext()){
//			LineSegment lineSegment = lineIterator.next();
//			Line2D.Double line = new Line2D.Double(lineSegment.getOrigin().getX(), lineSegment.getOrigin().getY(),
//					lineSegment.getTerminus().getX(), lineSegment.getTerminus().getY());
//			g2d.draw(line);
//		}
//	}

	private Stroke createStroke(double lineWidth){
		Stroke stroke = new BasicStroke((float)lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		return stroke;
	}

//	private void drawMidLineHandle(final Graphics2D g2d, ISelectionHandle handle){
//		handle.drawShape(new IHandleShapeDrawer() {
//			
//			@Override
//			public void drawHandle(ILinkMidLineHandleShape shape) {
//				Iterator<Point> pointIter = shape.pointIterator();
//			}
//		});
//	}
	
	
	private class HandleDrawer implements IHandleShapeDrawer{
		private final Graphics2D g2d;
		
		public HandleDrawer(Graphics2D g2d){
			this.g2d = g2d;
		}
		
		@Override
		public void drawHandle(ILinkMidLineHandleShape shape) {
			drawPolygon(shape.pointIterator(), Color.black, Color.yellow);
		}

		@Override
		public void drawHandle(ILinkBendPointHandleShape shape) {
			drawPolygon(shape.pointIterator(), Color.black, Color.red);
		}
	
		
		private void drawPolygon(Iterator<Point> pointIter, Color lineCol, Color fillCol){
			Path2D pg = new Path2D.Double();
			Point firstPoint = pointIter.next();
			pg.moveTo(firstPoint.getX(), firstPoint.getY());
			while(pointIter.hasNext()){
				Point p = pointIter.next();
				pg.lineTo(p.getX(), p.getY());
			}
			pg.closePath();
			g2d.setColor(fillCol);
			g2d.fill(pg);
			g2d.setColor(lineCol);
			g2d.setStroke(createStroke(1.0));
			g2d.draw(pg);
		}

		@Override
		public void drawHandle(ILinkSelectionHandleShape linkSelectionHandle) {
			ILinkPointDefinition linkDefinition = linkSelectionHandle.getLinkDefinition();
			g2d.setColor(Color.RED);
			g2d.setStroke(createStroke(1.0));

			Iterator<LineSegment> lineIterator = linkDefinition.drawnLineSegIterator();
			while(lineIterator.hasNext()){
				LineSegment lineSegment = lineIterator.next();
				Line2D.Double line = new Line2D.Double(lineSegment.getOrigin().getX(), lineSegment.getOrigin().getY(),
						lineSegment.getTerminus().getX(), lineSegment.getTerminus().getY());
				g2d.draw(line);
			}
		}

		@Override
		public void drawHandle(ICentralSelectionHandleShape centralSelectionHandleShape) {
		}

		@Override
		public void drawHandle(ICornerSelectionHandleShape cornerSelectionHandleShape) {
		}

		@Override
		public void drawHandle(IMidPointSelectionHandleShape midPointSelectionHandleShape) {
		}
	}

	@Override
	public Envelope getBounds() {
		return this.selection.getPrimitiveController().getDrawnBounds();
	}
}
