package org.pathwayeditor.visualeditor.controller;

public interface IViewControllerNodeStructureChangeEvent {
	enum ViewControllerStructureChangeType { NODE_ADDED, NODES_REMOVED };
	
	ViewControllerStructureChangeType getChangeType();
	
	INodeController getChangedNode();
	
}
