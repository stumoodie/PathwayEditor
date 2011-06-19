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

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public interface ILinkPointDefinition {

	void setSrcAnchorPosition(Point newPosn);

	void setTgtAnchorPosition(Point newPosn);

	void setBendPointPosition(int bpIdx, Point newPosn);

	Point getSrcAnchorPosition();

	Point getTgtAnchorPosition();

	Point getBendPointPosition(int bpIdx);

	LineSegment getSourceLineSegment();

	/**
	 * Gets the line segment starting at the tgt anchor point and ending at the last bendpoint or the src
	 * anchor point if there are no bend-points.
	 * @return
	 */
	LineSegment getTargetLineSegment();

	Iterator<Point> pointIterator();

	int numPoints();

	Iterator<LineSegment> lineSegIterator();

	Iterator<LineSegment> drawnLineSegIterator();

	void addNewBendPoint(int bpIdx, Point bpPosn);

	void addNewBendPoint(Point bpPosn);

	int numBendPoints();

	Iterator<Point> bendPointIterator();
	
	void removeBendPoint(int bpIdx);

	/**
	 * Provides the line that defines the direction of the link, going from the src end-point to the
	 * target end-point. 
	 * @return the line segment defining the direction of the line from src to tgt.
	 */
	LineSegment getLinkDirection();

	boolean containsPoint(Point p);

	void translate(Point translation);

	IGraphicalLinkTerminusDefinition getSourceTerminusDefinition();

	IGraphicalLinkTerminusDefinition getTargetTerminusDefinition();

	LineStyle getLineStyle();

	Colour getLineColour();

	double getLineWidth();

	void setLineColour(Colour lineColour);

	void setLineStyle(LineStyle lineStyle);

	void setLineWidth(double lineWidth);

	Envelope getBounds();

	ILinkPointDefinition getCopy();
}