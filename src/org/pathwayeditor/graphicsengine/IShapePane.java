package org.pathwayeditor.graphicsengine;

import org.pathwayeditor.visualeditor.controller.IViewModel;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface IShapePane {

	void repaint();
	
	IViewModel getViewModel();
	
	ISelectionRecord getSelectionRecord();
	
}