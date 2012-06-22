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

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

/**
 * An interface that define a controller model. This is a collection of controllers, but with the
 * added ability to manage the creation and removal of controllers via listeners that are registered with this interface.
 * Note that the model must be associated with the {@code ICompoundGraphElement} rather than instances of {@code ICanvasElementAttribute} because
 * an attribute can be associated with one graph element, for example when a shape becomes a child of another shape, in which case the attribute points
 * to the new node, but this controller model needs to remove the old controller associated with the old node as well as create a new controller for the new node. 
 * @author Stuart Moodie
 *
 */

public interface IViewControllerModel extends IViewControllerCollection {

	IModel getDomainModel();
	
	IRootController getRootNode();

	void activate();
	
	void deactivate();
	
	boolean isActive();

	Envelope getCanvasBounds();
	
	IIntersectionCalculator getIntersectionCalculator();
	
//	/**
//	 * Returns the collection of controllers that were the result of the last operation performed that changed the structure of the
//	 * domain model compound graph.
//	 * @return the view controller collection, which cannot be null.
//	 */
//	IViewControllerCollection getLastOperationResult();

	void addViewControllerChangeListener(IViewControllerChangeListener listener);
	
	void removeViewControllerChangeListener(IViewControllerChangeListener listener);
	
	List<IViewControllerChangeListener> getViewControllerChangeListeners();

	/**
	 * Get the controllers associated with this model in Z-order from back to front. This should
	 * be used then the correct rendering order of the drawing elements is required. 
	 * @return
	 */
	Iterator<IDrawingElementController> zOrderIterator();
}
