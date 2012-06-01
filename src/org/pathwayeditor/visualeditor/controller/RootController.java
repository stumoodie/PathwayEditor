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

import java.awt.Graphics2D;

import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.figure.rendering.IAnchorLocatorFactory;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;

public class RootController extends NodeController implements IRootController {
	private final IRootNode domainNode;
	private final IConvexHull hull;
	private boolean isActive;
	
	
	public RootController(IViewControllerModel viewModel, IRootNode node, int index) {
		super(viewModel, index);
		this.domainNode = node;
		this.hull = new RectangleHull(domainNode.getAttribute().getBounds());
		this.isActive = false;
	}

	@Override
	public Envelope getBounds() {
		return hull.getEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.hull;
	}

	@Override
	public IRootNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public void activate() {
		this.isActive = true;
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		return false;
	}

	@Override
	public void inactivate() {
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.getBounds();
	}

	@Override
	public boolean containsPoint(Point p) {
		return true;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return true;
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		return true;
	}

	@Override
	public IMiniCanvas getMiniCanvas() {
		return new IMiniCanvas() {
			
			@Override
			public void paint(Graphics2D g) {
			}
			
			@Override
			public Envelope getBounds() {
				return hull.getEnvelope();
			}
		};
	}

//	@Override
//	public IAnchorLocatorFactory getAnchorLocatorFactory() {
//		throw new UnsupportedOperationException("Not in use. This should not be called!");
//	}
}
