package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.visualeditor.behaviour.IDragResponse;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface ISelectionDragResponse extends IDragResponse {

	/**
	 * Sets the selection handle to use with this drag
	 * @param selectionHandle the selection handle that the drag is being initiated from. 
	 */
	void setSelectionHandle(ISelectionHandle selectionHandle);
	
}
