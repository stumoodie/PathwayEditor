package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Point;

public interface IShapeCreationOperation {

	void createShape(Point origin);

	void setShapeObjectType(IShapeObjectType shapeType);

	IShapeObjectType getShapeObjectType();

	void startCreationDrag(Point adjustedMousePosition);

	void ongoingCreationDrag(Point newLocation);

	void finishCreationDrag(Point newLocation);
	
}
