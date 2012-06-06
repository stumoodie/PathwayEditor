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

import java.util.Comparator;
import java.util.SortedSet;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;

/**
 * @author smoodie
 *
 */
public interface IIntersectionCalculator {

	IViewControllerModel getModel();

	void setFilter(IIntersectionCalcnFilter filter);

	void setComparator(Comparator<IDrawingElementController> comparator);
	
	SortedSet<IDrawingElementController> findIntersectingParentNodes(IConvexHull queryHull, IDrawingElementController queryNode);

	SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull, IDrawingElementController queryNode);

	SortedSet<IDrawingElementController> findDrawingPrimitivesAt(Point p);

	SortedSet<IDrawingElementController> findIntersectingController(Envelope bounds);

	/**
	 * Find nodes that intersect this hull.
	 * @param queryHull the hull to test
	 * @return the nodes, including the root node that intersect this hull.
	 */
	SortedSet<IDrawingElementController> findIntersectingNodes(IConvexHull queryHull);

	SortedSet<IDrawingElementController> findIntersectingElements(IConvexHull convexHull);
	
//	SortedSet<IDrawingElementController> findIntersectingControllerBounds(Envelope bounds);
}