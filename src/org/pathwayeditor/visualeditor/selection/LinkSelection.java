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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkSelection extends Selection implements ILinkSelection {
	private final ILinkController controller;
	private final List<ISelectionHandle> handles;
	
	public LinkSelection(SelectionType selectionType, ILinkController drawingElement) {
		super(selectionType);
		this.controller = drawingElement;
		this.handles = new LinkedList<ISelectionHandle>();
		addHandles();
	}
	
	private void addHandles(){
		int cntr = 0;
		Iterator<LineSegment> iter = this.controller.getLinkDefinition().lineSegIterator();
		while(iter.hasNext()){
			LineSegment lineSeg = iter.next();
			this.handles.add(SelectionHandle.createLinkMidpointRegion(this, controller, lineSeg, cntr));
			cntr++;
		}
		Iterator<Point> bpIter = this.controller.getDrawingElement().getAttribute().getBendPointContainer().bendPointIterator();
		int bpIdx = 0;
		while(bpIter.hasNext()){
			Point bp = bpIter.next();
			this.handles.add(SelectionHandle.createLinkBendpointRegion(this, controller, bp, bpIdx));
			bpIdx++;
		}
		this.handles.add(SelectionHandle.createLinkRegion(this, controller));
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		ISelectionHandle retVal = null;
		for(ISelectionHandle handle : this.handles){
			if(handle.containsPoint(point)){
				retVal = handle;
				break;
			}
		}
		return retVal;
	}

	@Override
	public ILinkController getPrimitiveController() {
		return this.controller;
	}

	@Override
	public List<ISelectionHandle> getSelectionHandle(SelectionHandleType region) {
		List<ISelectionHandle> retVal = new LinkedList<ISelectionHandle>();
		for(ISelectionHandle handle : this.handles){
			if(region.equals(handle.getType())){
				retVal.add(handle);
			}
		}
		return retVal;
	}
}
