package org.pathwayeditor.visualeditor;

public interface IDrawingPrimitiveDisposalEvent {

	/**
	 * Gets the drawing primitive that is about to be disposed of. It will not be disposed yet
	 * so it is valid to query its state and remove listeners
	 * @return the drawing primitive that is about to be disposed.
	 */
	IDrawingPrimitive getDrawingPrimitive();
	
}
