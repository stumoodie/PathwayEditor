package org.pathwayeditor.visualeditor.behaviour;

import java.util.List;

import org.pathwayeditor.visualeditor.editingview.IShapePane;

public interface IViewBehaviourStateHandler {
	
	void activate(IShapePane shapePane);
	
	void deactivate(IShapePane shapePane);

	boolean isActive();
	
	void addViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l);
	
	void removeViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l);

	List<IViewBehaviourStateHandlerChangeListener> getViewBehaviourStateHandlerChangeListener();
}
