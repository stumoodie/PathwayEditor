package org.pathwayeditor.visualeditor;

import java.util.Iterator;

public interface IViewModelLocationEvent {

	Iterator<IDrawingPrimitive> getModifiedPrimitives();
	
}
