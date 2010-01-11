package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionRecord {

	void setPrimarySelection(IDrawingPrimitiveController drawingElement);
	
	void addSecondarySelection(IDrawingPrimitiveController drawingElement);
	
	IDrawingPrimitiveController getPrimarySelection();
	
	Iterator<IDrawingPrimitiveController> secondarySelectionIterator();
	
	Iterator<IDrawingPrimitiveController> selectionIterator();
	
	Iterator<INodeController> selectedNodesIterator();
	
	Iterator<ILinkController> selectedLinksIterator();
	
	Iterator<INodeController> getTopNodeSelection();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingPrimitiveController testElement);
}
