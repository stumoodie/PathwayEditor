package org.pathwayeditor.visualeditor.controller;

public interface IViewControllerNodeStructureChangeEvent {
	enum ViewControllerStructureChangeType { NODE_ADDED, NODE_REMOVED, LINK_ADDED, LINK_REMOVED };
	
	ViewControllerStructureChangeType getChangeType();
	
	IDrawingPrimitiveController getChangedElement();
	
}
