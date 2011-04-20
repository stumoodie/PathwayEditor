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
package org.pathwayeditor.visualeditor.behaviour;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.editingview.IDomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

public class HitCalculator implements IHitCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private Point mousePosition;

	public HitCalculator(IShapePane shapePane){
		this.shapePane = shapePane;
	}
	
	
	@Override
	public void setMousePosition(double x, double y) {
		this.mousePosition = new Point(x, y);
	}

	@Override
	public IShapeController getShapeAtCurrentLocation() {
		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
		IIntersectionCalculator intnCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
		intnCalc.setFilter(new IIntersectionCalcnFilter() {
			@Override
			public boolean accept(IDrawingElementController node) {
				return node instanceof IShapeController;
			}
		});
		SortedSet<IDrawingElementController> hits = intnCalc.findDrawingPrimitivesAt(getDiagramLocation());
		IShapeController retVal = null;
		if(!hits.isEmpty()){
			retVal = (IShapeController)hits.first();
		}
		return retVal;
	}

	@Override
	public Point getDiagramLocation() {
		Point retVal = this.shapePane.getPaneBounds().getOrigin();
		retVal = retVal.translate(this.mousePosition);
		if(logger.isTraceEnabled()){
			logger.trace("Adjust position. orig=" + this.mousePosition + " : adjustedPoint=" + retVal + ", paneBounds=" + shapePane.getPaneBounds());
		}
		return retVal;  
	}


	@Override
	public Point getMousePosition() {
		return this.mousePosition;
	}

}
