package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;

public interface ILinkCreationOperation {

	void startCreationDrag(IShapeNode startNode);

	void setLinkObjectType(ILinkObjectType currentLinkType);

	ILinkObjectType getLinkObjectType();

	void ongoingCreationDrag(Point lastDelta);

	void finishCreationDrag(Point lastDelta);

	boolean canCreationSucceed();

}
