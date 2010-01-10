package org.pathwayeditor.visualeditor;

public interface IViewModelStructureEvent {
	enum ModelStructureChangeType { ADDED, REMOVED };
	
	ModelStructureChangeType getChangeType();
	
	IDrawingPrimitive getChangedDrawingPrimitive();
}
