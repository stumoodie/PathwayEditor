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
package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.IMarqueeOperation;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class MarqueeOperation implements IMarqueeOperation {
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final IIntersectionCalculator intersectionCal;

	public MarqueeOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			IIntersectionCalculator intersectionCal){
		this.shapePane = shapePane;
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.intersectionCal = intersectionCal;
	}

	@Override
	public void selectionStarted(Point initialPosn) {
		feedbackModel.clear();
		selectionRecord.clear();
		IFeedbackNode node = feedbackModel.getFeedbackNodeBuilder().createDefaultNode(new Envelope(initialPosn, new Dimension(0.1, 0.1)));
		node.setFillColour(RGB.BLUE);
		node.setLineColour(RGB.RED);
		node.setLineStyle(LineStyle.SOLID);
		node.setLineWidth(1.0);
	}

	@Override
	public void selectionFinished(Point originDelta, Dimension sizeDelta) {
		IFeedbackNode node = feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		makeSelectionFromMarquee(node.getBounds());
		feedbackModel.clear();
		shapePane.updateView(node.getBounds());
	}
	
	@Override
	public void selectionContinuing(Point originDelta, Dimension sizeDelta) {
		IFeedbackNode node = feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		shapePane.updateView(node.getBounds());
	}

	private void makeSelectionFromMarquee(Envelope bounds) {
		SortedSet<IDrawingElementController> selectedController = intersectionCal.findIntersectingController(bounds);
		boolean firstOne = true;
		for(IDrawingElementController controller : selectedController){
			if(firstOne){
				this.selectionRecord.setPrimarySelection(controller);
				firstOne = false;
			}
			else{
				this.selectionRecord.addSecondarySelection(controller);
			}
		}
	}
}
