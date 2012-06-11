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
package org.pathwayeditor.visualeditor.behaviour.creation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pathwayeditor.businessobjects.typedefn.INodeObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.behaviour.selection.DefaultPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.KeyboardResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.LinkBendpointPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.LinkPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.ShapePopupMenuResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class CreationControllerResponses <T extends INodeObjectType> implements IControllerResponses {
	private final IDragResponse dragResponse;
	private final ISelectionResponse selectionResponse;
	private final IMouseFeedbackResponse feedbackResponse;
	private final IKeyboardResponse keyboardResponse;
	private final Map<SelectionHandleType, IPopupMenuResponse> popupMenuMap; 
	
	public CreationControllerResponses(IOperationFactory opFactory, IShapeCreationOperation<T> creationOp, IShapeTypeInspector<T> shapeTypeInspector) {
		this.dragResponse = new CreationDragResponse<T>(creationOp, shapeTypeInspector);
        this.selectionResponse = new SelectionCreationResponse();
        this.feedbackResponse = new MouseCreationFeedbackResponse();
        this.keyboardResponse = new KeyboardResponse(new IEditingOperation() {
			
			@Override
			public void moveStarted() {
			}
			
			@Override
			public void moveOngoing(Point delta) {
			}
			
			@Override
			public void moveFinished(Point delta, ReparentingStateType reparentingState) {
			}
			
			@Override
			public ReparentingStateType getReparentingState(Point delta) {
				return ReparentingStateType.FORBIDDEN;
			}

			@Override
			public void deleteSelection() {
			}

			@Override
			public void setCopyOnMove(boolean isSelected) {
			}
		});
        this.popupMenuMap = new HashMap<SelectionHandleType, IPopupMenuResponse>();
        initialisePopupMenuResponse(opFactory);
	}

	private void initialisePopupMenuResponse(IOperationFactory opFactory){
		this.popupMenuMap.put(SelectionHandleType.Central, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.N, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.NE, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.E, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.SE, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.S, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.SW, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.W, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.NW, new ShapePopupMenuResponse(opFactory.getShapePopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.Link, new LinkPopupMenuResponse(opFactory.getLinkPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.LinkMidPoint, new LinkPopupMenuResponse(opFactory.getLinkPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.LinkBendPoint, new LinkBendpointPopupMenuResponse(opFactory.getLinkBendpointPopupMenuResponse()));
		this.popupMenuMap.put(SelectionHandleType.None, new DefaultPopupMenuResponse(opFactory.getDefaultPopupMenuResponse()));
	}

	@Override
	public IDragResponse getDragResponse(ISelectionHandle type) {
		return this.dragResponse;
	}

	@Override
	public IMouseFeedbackResponse getFeedbackResponse(ISelectionHandle type) {
		return this.feedbackResponse;
	}

	@Override
	public ISelectionResponse getSelectionResponse() {
		return this.selectionResponse;
	}

	@Override
	public IKeyboardResponse getKeyboardResponse() {
		return this.keyboardResponse;
	}

	@Override
	public IPopupMenuResponse getPopupMenuResponse(ISelectionHandle popupSelectionHandle) {
		return this.popupMenuMap.get(popupSelectionHandle);
	}

	@Override
	public Iterator<IPopupMenuResponse> popResponseIterator() {
		return popupMenuMap.values().iterator();
	}

	@Override
	public int numPopupResponses() {
		return popupMenuMap.size();
	}

}
