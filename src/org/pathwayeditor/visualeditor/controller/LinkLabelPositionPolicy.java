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
package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkLabelPositionPolicy implements ILabelLocationPolicy {
	private ILinkAttribute att;
	private ILinkPointDefinition defn = null;

	@Override
	public Point nextLabelLocation() {
		LineSegment line = defn.getLinkDirection();
		Vector v = line.getRightHandNormal();
		Point midPoint = line.getMidPoint();
		LineSegment tangent = new LineSegment(midPoint, midPoint.translate(v.getIMagnitude(), v.getJMagnitude()));
		Point retVal = findIntersection(tangent);
		return retVal;
	}

	@Override
	public void setOwner(ICanvasElementAttribute att) {
		this.att = (ILinkAttribute)att;
	}

	@Override
	public ILinkAttribute getOwner() {
		return this.att;
	}

	private Point findIntersection(LineSegment tangent){
		Iterator<LineSegment> lineIterator = this.defn.lineSegIterator();
		Point retVal = null;
		while(lineIterator.hasNext() && retVal == null){
			retVal = calcIntersection(tangent, lineIterator.next());
		}
		return retVal;
	}
	
	private Point calcIntersection(LineSegment tangent, LineSegment l){
		Point retVal = null;
		List<Point> intn = tangent.getLinesIntersections(l);
		retVal = intn.get(0);
		return retVal;
	}

	@Override
	public void setLinkEndPoints(ILinkPointDefinition defn) {
		this.defn = defn;
	}
	
}
