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
package org.pathwayeditor.visualeditor.geometry;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegmentVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.IStraightLineCurveSegment;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;

public class CurveSegmentAnchorCalculator  {
	private final Logger logger = Logger.getLogger(this.getClass());
	private ICurveSegment linkDefn;
	private Point anchorPoint;
	private Point adjustedPoint;

	public CurveSegmentAnchorCalculator(){
	}
	
	
	public void setCurveSegment(ICurveSegment curve){
		this.linkDefn = curve;
	}
	
	
	public ICurveSegment getCurveSegment(){
		return this.linkDefn;
	}
	
	public void setAnchorPoint(Point anchorPoint){
		this.anchorPoint = anchorPoint;
	}
	
	
	public Point getAnchorPoint(){
		return this.anchorPoint;
	}
	
	public Point adjustAnchorOnCurveSegment(){
		this.linkDefn.visit(new ICurveSegmentVisitor() {
			@Override
			public void visitStraightLineCurveSegment(IStraightLineCurveSegment v) {
				LineSegment line = v.getLineSegment(); 
				Point start = line.getOrigin();
				Point end = line.getTerminus();
				Vector ap = new Vector(anchorPoint.getX()-start.getX(), anchorPoint.getY()-start.getY(), 0.0);
				Vector ab = new Vector(end.getX()-start.getX(), end.getY()-start.getY(), 0.0);
				double aqMag = ap.scalarProduct(ab)/ab.magnitude();
				Vector aq = ab.unitVector().scale(aqMag);
				Point q = start.translate(aq.getIMagnitude(), aq.getJMagnitude());
				if(!line.containsPoint(q)){
					logger.error("Point does not lie on line. anchorPoint=" + anchorPoint + ",p=" + q + ",line=" + line);
				}
				adjustedPoint = q;
			}
		});
		return this.adjustedPoint;
	}

}
