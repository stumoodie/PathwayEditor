package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public interface IPopupMenuResponse {

	JPopupMenu getPopupMenu(ISelectionHandle selectionHandle);

	void activate();

	void deactivate();

}
