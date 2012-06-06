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

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;

public interface IDrawingElementController extends Comparable<IDrawingElementController> {

	int getIndex();
	
	IViewControllerModel getViewModel();
	
	IMiniCanvas getMiniCanvas();
	
	/**
	 * Gets the drawing element that is the domain model for this primitive.
	 * @return the drawing element, which cannot be null.
	 */
	IDrawingElement getDrawingElement();
	
	Envelope getDrawnBounds();
	
	/**
	 * Enables listeners  
	 */
	void activate();
	
	/**
	 * Is this instance in an active state, i.e. with listeners enabled.
	 * @return
	 */
	boolean isActive();
	
	/**
	 * Turns off listeners.
	 */
	void inactivate();

	void addDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener);
	
	void removeDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener);
		
	List<IDrawingElementControllerListener> getDrawingPrimitiveControllerListeners();
	
	boolean containsPoint(Point p);

	boolean intersectsBounds(Envelope drawnBounds);

	boolean intersectsHull(IConvexHull queryHull);
}
