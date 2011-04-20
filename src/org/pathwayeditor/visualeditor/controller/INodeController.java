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

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface INodeController extends IDrawingElementController {

	/**
	 * Returns the bounds of the drawn node, which may be different from that of the underlying model.
	 * @return the envelope of the drawn node.
	 */
	Envelope getBounds();
	
	/**
	 * Returns the hull of the drawn node primitive. 
	 * @return the hull of the drawn node primitive.
	 */
	IConvexHull getConvexHull();

	/**
	 * Get the drawing element. 
	 */
	@Override
	IDrawingNode getDrawingElement();
	
	/**
	 * Tests if the convex hull intersects this shape.
	 * @param queryHull the queryHull to test/
	 * @return true if it intersects, false otherwise.
	 */
	boolean intersectsHull(IConvexHull queryHull);

	/**
	 * Tests id this node can resize to the new settings provided. 
	 * @param originDelta the change to the origin of the shape's bounds. 
	 * @param resizeDelta the change to the size of the shapes bounds.
	 * @return true of the resize will succeed, false otherwise.
	 */
	boolean canResize(Point originDelta, Dimension resizeDelta);
	
}
