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

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

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
}
