package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;


public interface IViewModelLocationEvent {

	Iterator<IDrawingPrimitive> getModifiedPrimitives();
	
}
