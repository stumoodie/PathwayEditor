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
package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

public abstract class SelectionHandle implements ISelectionHandle {
	private static final int DEFAULT_HANDLE_IDX = 0;
	private final IDrawingElementController controller;
	private final SelectionHandleType region;
	private final ISelection selection;
	private final int handleIndex;
	
	protected SelectionHandle(IDrawingElementController node, SelectionHandleType region, ISelection selection){
		this.controller = node;
		this.region = region;
		this.selection = selection;
		this.handleIndex = DEFAULT_HANDLE_IDX;
	}

	
	protected SelectionHandle(IDrawingElementController node, SelectionHandleType region, ISelection selection, int handleIndex){
		this.controller = node;
		this.region = region;
		this.selection = selection;
		this.handleIndex = handleIndex;
	}

	
	@Override
	public final IDrawingElementController getDrawingPrimitiveController(){
		return this.controller;
	}
	
	@Override
	public final SelectionHandleType getType(){
		return this.region;
	}
	

	@Override
	public final int getHandleIndex() {
		return this.handleIndex;
	}
	
	@Override
	public final ISelection getSelection(){
		return this.selection;
	}
	
	public static ISelectionHandle createCentralRegion(ISelection selection, INodeController nodeController) {
		return new CentralSelectionHandle(selection, nodeController);
	}

	public static ISelectionHandle createNRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getDrawnBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getOrigin(), bounds.getHorizontalCorner(), SelectionHandleType.N);
	}

	public static ISelectionHandle createNERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getDrawnBounds().getHorizontalCorner(), SelectionHandleType.NE);
	}

	public static ISelectionHandle createSERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getDrawnBounds().getDiagonalCorner(), SelectionHandleType.SE);
	}

	public static ISelectionHandle createSRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getDrawnBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getDiagonalCorner(), bounds.getVerticalCorner(), SelectionHandleType.S);
	}

	public static ISelectionHandle createSWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getDrawnBounds().getVerticalCorner(), SelectionHandleType.SW);
	}

	public static ISelectionHandle createWRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getDrawnBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getVerticalCorner(), bounds.getOrigin(), SelectionHandleType.W);
	}

	public static ISelectionHandle createNWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getDrawnBounds().getOrigin(), SelectionHandleType.NW);
	}

	public static ISelectionHandle createERegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getDrawnBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getHorizontalCorner(), bounds.getDiagonalCorner(), SelectionHandleType.E);
	}

	public static ISelectionHandle createLinkMidpointRegion(ISelection selection, ILinkController nodeController, LineSegment lineSeg, int lineSegmentIdx) {
		return new LinkMidPointSelectionHandle(selection, nodeController, lineSeg, lineSegmentIdx);
	}


	public static ISelectionHandle createLinkBendpointRegion(ISelection linkSelection, ILinkController controller, Point bp, int bpIdx) {
		return new LinkBendPointSelectionHandle(linkSelection, controller, bp, bpIdx);
	}


	public static ISelectionHandle createLinkRegion(LinkSelection linkSelection, ILinkController controller) {
		return new LinkSelectionHandle(linkSelection, controller);
	}
}
