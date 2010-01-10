package org.pathwayeditor.visualeditor.controller;


public interface IViewModelStructureEvent {
	enum ModelStructureChangeType { ADDED, REMOVED };
	
	ModelStructureChangeType getChangeType();
	
	IDrawingPrimitive getChangedDrawingPrimitive();
}
