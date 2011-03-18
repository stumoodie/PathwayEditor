package org.pathwayeditor.visualeditor.behaviour.creation;

import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.selection.DefaultMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class CreationControllerResponses implements IControllerResponses {
	private final IDragResponse dragResponse;
	private final ISelectionResponse selectionResponse;
	private final IMouseFeedbackResponse feedbackResponse;
	
	public CreationControllerResponses(IOperationFactory opFactory, IShapeTypeInspector shapeTypeInspector) {
		this.dragResponse = new CreationDragResponse(opFactory.getShapeCreationOperation(), shapeTypeInspector);
        this.selectionResponse = new SelectionResponse(opFactory.getSelectionOperation());
        this.feedbackResponse = new DefaultMouseFeedbackResponse();
	}

	@Override
	public IDragResponse getDragResponse(SelectionHandleType type) {
		return this.dragResponse;
	}

	@Override
	public IMouseFeedbackResponse getFeedbackResponse(SelectionHandleType type) {
		return this.feedbackResponse;
	}

	@Override
	public ISelectionResponse getSelectionResponse() {
		return this.selectionResponse;
	}

}
