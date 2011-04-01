package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;

public interface IFeedbackLinkBuilder {

	IFeedbackModel getFeedbackModel();

	IFeedbackLink createFromObjectType(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkObjectType objectType);

	IFeedbackLink createFromAttribute(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkAttribute linkAttribute,
			Point point, IAnchorLocator srcAnchorLocator, Point point2, IAnchorLocator tgtAnchorLocator);

	IFeedbackLink createNodelessLinkFromObjectType(Point srcPosn, Point tgtPosn, ILinkObjectType linkObjectType);

}
