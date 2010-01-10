package org.pathwayeditor.visualeditor;

import org.pathwayeditor.figure.geometry.Point;

public interface INodePrimitiveTranslationEvent {

	INodePrimitive getChangedNode();
	
	Point getTranslationDelta();
	
}
