package org.pathwayeditor.visualeditor.controller;

public interface IDrawingPrimitiveListener {

	/**
	 * Called when a drawing primitive is about to be disposed. This gives objects listening to this object
	 * a change to clean up.
	 * @param e the disposal notification event
	 */
	void drawingPrimitiveDisposal(IDrawingPrimitiveDisposalEvent e);
	
}
