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

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;
import org.pathwayeditor.visualeditor.commands.ChangeAnnotationPropertyValue;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class SelectionOperation implements ISelectionOperation {
	private enum SelectionType { PRIMARY, SECONDARY, DOUBLE };
	private Point location;
	private SelectionType selectionType;
	private final ISelectionRecord selectionRecord;
	private final IIntersectionCalculator intersectionCal;
	private final LabelPropValueDialog propValueDialog;
	private final ICommandStack cmdStack;
	private final IShapePane shapePane;
	
	public SelectionOperation(ISelectionRecord selectionRecord, IIntersectionCalculator intersectionCal,
			LabelPropValueDialog propValueDialog, ICommandStack cmdStack, IShapePane shapePane){
		this.shapePane = shapePane;
		this.cmdStack = cmdStack;
		this.selectionRecord = selectionRecord;
		this.intersectionCal = intersectionCal;
		this.selectionType = null;
		this.propValueDialog = propValueDialog;
	}
	
	@Override
	public void setCurrentLocation(Point location) {
		this.location = location;
	}

	@Override
	public void setPrimaryClick() {
		this.selectionType = SelectionType.PRIMARY;
	}

	@Override
	public void handleClick() {
		intersectionCal.setFilter(null);
		SortedSet<IDrawingElementController> selectedControllers = intersectionCal.findDrawingPrimitivesAt(location);
		if(!selectedControllers.isEmpty()){
			if(this.selectionType == SelectionType.PRIMARY){
				this.selectionRecord.setPrimarySelection(selectedControllers.first());
			}
			else if(this.selectionType == SelectionType.SECONDARY){
				this.selectionRecord.addSecondarySelection(selectedControllers.first());
			}
			else if(this.selectionType == SelectionType.DOUBLE){
				IDrawingElementController selectedController = selectedControllers.first();
				if(selectedController instanceof ILabelController){
					propValueDialog.setLabelController((ILabelController)selectedController);
					Object oldValue = ((ILabelController)selectedController).getDrawingElement().getAttribute().getProperty().getValue();
					String labelValue = propValueDialog.getLabelValue();
					if(labelValue != null && !oldValue.equals(labelValue)){
						IAnnotationProperty prop = ((ILabelController) selectedController).getDrawingElement().getAttribute().getProperty();
						cmdStack.execute(new ChangeAnnotationPropertyValue(prop, labelValue));
						shapePane.updateView();
					}
				}
			}
		}
		else{
			this.selectionRecord.clear();
		}
	}

	@Override
	public void setSecondaryClick() {
		this.selectionType = SelectionType.SECONDARY;
	}

	@Override
	public void setDoubleClick() {
		this.selectionType = SelectionType.DOUBLE;
	}

}
