package org.pathwayeditor.visualeditor.controller;

public interface IDrawingPrimitiveControllerEvent {
	enum EventType { DRAWN_BOUNDS_CHANGED };
	
	IDrawingPrimitiveController getController();
	
	Object getOldValue();
	
	Object getCurrentValue();
	
}
