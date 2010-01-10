package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Point;

public interface INodePrimitiveTranslationEvent {

	INodePrimitive getChangedNode();
	
	Point getTranslationDelta();
	
}
