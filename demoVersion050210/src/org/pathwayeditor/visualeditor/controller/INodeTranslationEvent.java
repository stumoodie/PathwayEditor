package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Point;

public interface INodeTranslationEvent {

	INodeController getChangedNode();
	
	Point getTranslationDelta();
	
}
