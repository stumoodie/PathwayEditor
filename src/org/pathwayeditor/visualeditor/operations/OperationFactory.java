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

import org.pathwayeditor.visualeditor.behaviour.operation.IDefaultPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkBendPointPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.IMarqueeOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapePopupActions;
import org.pathwayeditor.visualeditor.commands.DeleteBendPointCommand;
import org.pathwayeditor.visualeditor.commands.DeleteSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.CommonParentCalculator;
import org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator;
import org.pathwayeditor.visualeditor.layout.LabelPositionCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class OperationFactory implements IOperationFactory {
	private final IEditingOperation editOperation;
	private final IResizeOperation resizeOperation;
	private final ILinkOperation linkOperation;
	private final IMarqueeOperation marqueeOperation;
	private IShapePopupActions shapePopupMenuResponse;
	private ILinkPopupActions linkPopupMenuResponse;
	private ILinkBendPointPopupActions linkBendpointPopupResponse;
	private IDefaultPopupActions defaultPopupMenuResponse;
	private final IShapePane shapePane;
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
//	private final IViewControllerModel viewModel;
	private final IShapeCreationOperation shapeCreationOperation;
	private final ISelectionOperation selectionOperations;
	private final ILinkCreationOperation linkCreationOperation;

	public OperationFactory(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord, IViewControllerModel viewModel,
			ICommandStack commandStack, LabelPropValueDialog labelDialog){
		ICommonParentCalculator newParentCalc = new CommonParentCalculator(viewModel.getIntersectionCalculator());
		this.shapePane = shapePane;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
//		this.viewModel = viewModel;
        editOperation = new EditingOperation(shapePane, feedbackModel, selectionRecord, newParentCalc, commandStack);
        resizeOperation = new ResizeOperation(shapePane, feedbackModel, selectionRecord, commandStack);
		linkOperation = new LinkOperation(shapePane, feedbackModel, selectionRecord, commandStack);
		marqueeOperation = new MarqueeOperation(shapePane, feedbackModel, selectionRecord, viewModel.getIntersectionCalculator());
		initResponses();
		shapeCreationOperation = new ShapeCreationOperation(shapePane, feedbackModel, viewModel, commandStack, new LabelPositionCalculator());
		this.selectionOperations = new SelectionOperation(selectionRecord, viewModel.getIntersectionCalculator(), labelDialog, this.commandStack, shapePane);
		this.linkCreationOperation = new LinkCreationOperation(shapePane, feedbackModel, commandStack);
	}
	
	private void initResponses(){
		this.shapePopupMenuResponse = new IShapePopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

			@Override
			public IShapeController getSelectedShape() {
				return (IShapeController)selectionRecord.getPrimarySelection().getPrimitiveController();
			}
		};
		this.linkPopupMenuResponse = new ILinkPopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}
		};
		this.linkBendpointPopupResponse = new ILinkBendPointPopupActions() {
			
			@Override
			public void deleteBendPoint(int bpIdx) {
				deleteBendpoint(bpIdx);
				selectionRecord.restoreSelection();
				shapePane.updateView();
			}
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

		};
		this.defaultPopupMenuResponse = new IDefaultPopupActions() {
			
			@Override
			public void selectAll() {
				selectAllElements();
				shapePane.updateView();
			}

			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

			@Override
			public boolean isDeleteActionValid() {
				return selectionRecord.numSelected() > 0;
			}
		};
	}
	
	@Override
	public IDefaultPopupActions getDefaultPopupMenuResponse() {
		return this.defaultPopupMenuResponse;
	}

	@Override
	public ILinkBendPointPopupActions getLinkBendpointPopupMenuResponse() {
		return this.linkBendpointPopupResponse;
	}

	@Override
	public ILinkOperation getLinkOperation() {
		return this.linkOperation;
	}

	@Override
	public ILinkPopupActions getLinkPopupMenuResponse() {
		return this.linkPopupMenuResponse;
	}

	@Override
	public IMarqueeOperation getMarqueeOperation() {
		return this.marqueeOperation;
	}

	@Override
	public IEditingOperation getMoveOperation() {
		return this.editOperation;
	}

	@Override
	public IResizeOperation getResizeOperation() {
		return this.resizeOperation;
	}

	@Override
	public IShapePopupActions getShapePopupMenuResponse() {
		return this.shapePopupMenuResponse;
	}

	private void selectAllElements() {
		selectionRecord.selectAll();
	}

	private void deleteBendpoint(int bpIdx) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new DeleteBendPointCommand(linkSelection.getPrimitiveController().getDrawingElement().getAttribute().getBendPointContainer(), bpIdx);
		this.commandStack.execute(cmd);
	}
	
	private void deleteSelection() {
		this.commandStack.execute(new DeleteSelectionCommand(this.selectionRecord.getSubgraphSelection()));
	}

	@Override
	public IShapeCreationOperation getShapeCreationOperation() {
		return this.shapeCreationOperation;
	}

	@Override
	public ISelectionOperation getSelectionOperation() {
		return this.selectionOperations;
	}

	@Override
	public ILinkCreationOperation getLinkCreationOperation() {
		return this.linkCreationOperation;
	}
}
