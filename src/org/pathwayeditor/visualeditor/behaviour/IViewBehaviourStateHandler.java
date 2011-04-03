package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.visualeditor.editingview.IShapePane;

public interface IViewBehaviourStateHandler {
	
	void activate(IShapePane shapePane);
	
	void deactivate(IShapePane shapePane);

	boolean isActive();
	
}
