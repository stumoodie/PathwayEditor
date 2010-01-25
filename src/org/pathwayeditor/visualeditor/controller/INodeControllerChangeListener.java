package org.pathwayeditor.visualeditor.controller;

public interface INodeControllerChangeListener {

	void nodeTranslated(INodeTranslationEvent e);

	void nodeResized(INodeResizeEvent e);
	
	void changedBounds(INodeBoundsChangeEvent e);
}
