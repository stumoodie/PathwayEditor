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
package org.pathwayeditor.visualeditor.layout;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public class ShapeCentreLabelPositionPolicy implements IShapeLabelLocationPolicy {
	private IFigureRenderingController shape;
	private IShapeAttribute shapeNode;
	
	public ShapeCentreLabelPositionPolicy(){
		this.shape = null;
	}
	
	@Override
	public IFigureRenderingController getShapeFigure() {
		return this.shape;
	}

	@Override
	public Point nextLabelLocation() {
		IConvexHull hull = this.shape.getConvexHull();
		return hull.getCentre();
	}

	@Override
	public void setShapeFigure(IFigureRenderingController shape) {
		this.shape = shape;
	}

	@Override
	public void setOwningShape(IShapeAttribute shapeNode) {
		this.shapeNode = shapeNode;
	}

	@Override
	public IShapeAttribute getOwningShape() {
		return this.shapeNode;
	}

}
