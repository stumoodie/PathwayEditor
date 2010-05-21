package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;

public interface IFeedbackNodeBuilder {

	IFeedbackModel getFeedbackModel();

	IFeedbackNode createFromDrawingNodeObjectType(IShapeObjectType objectType, Envelope initialBounds);

	IFeedbackNode createFromDrawingNodeAttribute(IDrawingNodeAttribute nodeAttribute);

	IFeedbackNode createDefaultNode(Envelope initialBounds);

}