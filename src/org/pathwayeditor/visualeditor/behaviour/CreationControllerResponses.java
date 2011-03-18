package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class CreationControllerResponses implements IControllerResponses {
	private final IDragResponse dragResponse;
	private final ISelectionResponse selectionResponse;
	private final IMouseFeedbackResponse feedbackResponse;
	
	public CreationControllerResponses(IOperationFactory opFactory, IShapeTypeInspector shapeTypeInspector) {
		this.dragResponse = new CreationResponse(opFactory.getShapeCreationOperation(), shapeTypeInspector);
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
