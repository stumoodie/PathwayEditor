package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;

public interface IMouseBehaviourController {

	void activate();

	boolean isActivated();
	
	void deactivate();

//	void updateView();

	void setShapeCreationMode(IShapeObjectType shapeType);

	void setLinkCreationMode(ILinkObjectType linkType);

	void setSelectionMode();
	
}
