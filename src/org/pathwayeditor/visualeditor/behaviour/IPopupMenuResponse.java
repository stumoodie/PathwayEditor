package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface IPopupMenuResponse {

	void setSelectionHandle(ISelectionHandle selectionHandle);
	
	ISelectionHandle getSelectionHandle();
	
	JPopupMenu getPopupMenu();

	void activate();

	void deactivate();

}
