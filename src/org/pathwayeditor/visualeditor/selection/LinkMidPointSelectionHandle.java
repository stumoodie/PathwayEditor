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
package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.ConvexHullCalculator;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.IConvexHullCalculator;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;
import org.pathwayeditor.visualeditor.controller.ILinkController;

public class LinkMidPointSelectionHandle extends SelectionHandle implements ILinkMidLineHandleShape {
	private static final double HANDLE_OFFSET = 6.0;
//	private static final double HANDLE_WIDTH = HANDLE_OFFSET*2;
//	private static final double HANDLE_HEIGHT = HANDLE_OFFSET*2;
	
	private IConvexHull diamondHull;

	public LinkMidPointSelectionHandle(ISelection selection, ILinkController nodeController, LineSegment lineSegment, int lineSegmentIdx){
		super(nodeController, SelectionHandleType.LinkMidPoint, selection, lineSegmentIdx);
		Point bisection = lineSegment.getMidPoint();
		IConvexHullCalculator builder = new ConvexHullCalculator();
		Vector leftNormal = lineSegment.getVector();
		Vector scaledLeftNormal = leftNormal.scale(HANDLE_OFFSET/leftNormal.magnitude());
		builder.addPoint(bisection.translate(scaledLeftNormal.getIMagnitude(), scaledLeftNormal.getJMagnitude()));
		builder.addPoint(bisection.translate(-scaledLeftNormal.getJMagnitude(), scaledLeftNormal.getIMagnitude()));
		builder.addPoint(bisection.translate(-scaledLeftNormal.getIMagnitude(), -scaledLeftNormal.getJMagnitude()));
		builder.addPoint(bisection.translate(scaledLeftNormal.getJMagnitude(), -scaledLeftNormal.getIMagnitude()));
		builder.calculate();
		this.diamondHull = builder.getConvexHull();
	}
	
	@Override
	public boolean containsPoint(Point point) {
		return this.diamondHull.containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.diamondHull.getEnvelope();
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public void translate(Point delta) {
		Envelope newEnvelope = this.diamondHull.getEnvelope().translate(delta);
		this.diamondHull = this.diamondHull.changeEnvelope(newEnvelope);
	}

	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}

	@Override
	public Iterator<Point> pointIterator() {
		return this.diamondHull.pointIterator();
	}

}
