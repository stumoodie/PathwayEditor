package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public interface ILinkCreationOperation {

	void startCreation();

	void setLinkObjectType(ILinkObjectType currentLinkType);

	ILinkObjectType getLinkObjectType();

	void creationOngoing(Point lastDelta);

	void finishCreation();

	void setPotentialTarget(IShapeController potentialTarget);

	boolean isLinkCreationStarted();

	boolean canFinishCreation();

	void setPotentialSourceNode(IShapeController potentialSource);

	boolean canStartCreation();

}
