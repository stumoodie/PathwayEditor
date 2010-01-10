package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.visualeditor.controller.IDrawingPrimitive;
import org.pathwayeditor.visualeditor.controller.ILinkPrimitive;
import org.pathwayeditor.visualeditor.controller.INodePrimitive;

public interface ISelectionRecord {

	void setPrimarySelection(IDrawingPrimitive drawingElement);
	
	void addSecondarySelection(IDrawingPrimitive drawingElement);
	
	IDrawingPrimitive getPrimarySelection();
	
	Iterator<IDrawingPrimitive> secondarySelectionIterator();
	
	Iterator<IDrawingPrimitive> selectionIterator();
	
	Iterator<INodePrimitive> selectedNodesIterator();
	
	Iterator<ILinkPrimitive> selectedLinksIterator();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingPrimitive testElement);
}
