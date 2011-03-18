package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface IDragResponse {

	/**
	 * Is a drag operation underway in this drag response.
	 * @return true if it is, false otherwise.
	 */
	boolean isDragOngoing();
	
	/**
	 * Can the drag operation continue if the change in the mouse position is applied to this drag response?
	 * @param delta the change in mouse position to be applied to this drag response.
	 * @return true if the drag can continue with this change, false otherwise.
	 */
	boolean canContinueDrag(Point delta);
	
	/**
	 * Start a drag operation.
	 * @param selectionHandle the selection handle that the drag is being initiated from. 
	 * @param startLocation the initial location of this drag.
	 */
	void dragStarted(ISelectionHandle selectionHandle, Point startLocation);
	
	/**
	 * Tells this drag response whether the shift key has been selected.  
	 * @param isSelected true if it has, false otherwise.
	 */
	void shiftSelected(boolean isSelected);
	
	/**
	 * Tells this drag response whether the cmd/ctrl key has been selected.  
	 * @param isSelected true if it has, false otherwise.
	 */
	void cmdSelected(boolean isSelected);
	
	/**
	 * Tells this drag response whether the alt/option key has been selected.  
	 * @param isSelected true if it has, false otherwise.
	 */
	void altSelected(boolean isSelected);
	
	/**
	 * Tells this drag response that the drag is continuing at the given location.
	 * @param newLocation rhe new location of the mouse pointer. 
	 */
	void dragContinuing(Point newLocation);

	/**
	 * Tells the drag response that the drag has completed. Any operations associated wit the drag should
	 * complete. 
	 */
	void dragFinished();

	/**
	 * Tests is a reparenting operation would succeed if this drag response completed in its
	 * current state.
	 * @return true if it will succeed, false otherwise.
	 */
	boolean canReparent();
	
	/**
	 * Tests is the operation would succeed if this drag response completed in its
	 * current state.
	 * @return true if is will succeed, false otherwise.
	 */
	boolean canOperationSucceed();
	
}
