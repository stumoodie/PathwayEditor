package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface ILabelLocationPolicy {

	ICanvasElementAttribute getOwner();
	
	void setOwner(ICanvasElementAttribute att);

	Point nextLabelLocation();

	void setLinkEndPoints(ILinkPointDefinition linkPoints);

}
