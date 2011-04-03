package org.pathwayeditor.visualeditor.behaviour;

import java.util.List;

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;

public interface IViewBehaviourController {

	void activate();

	boolean isActivated();
	
	void deactivate();

	void setShapeCreationMode(IShapeObjectType shapeType);

	void setLinkCreationMode(ILinkObjectType linkType);

	void setSelectionMode();

	void addViewBehaviourModeChangeListener(IViewBehaviourModeChangeListener l);
	
	void removeViewBehaviourModeChangeListener(IViewBehaviourModeChangeListener l);

	List<IViewBehaviourModeChangeListener> getViewBehaviourModeChangeListeners();

}
