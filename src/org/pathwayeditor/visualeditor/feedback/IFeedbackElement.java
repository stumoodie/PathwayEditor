package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.figure.geometry.Point;

public interface IFeedbackElement {

	int getElementIdentifier();

	void translatePrimitive(Point translation);

}
