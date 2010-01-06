package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;

public interface ISelectionRecord {

	void setPrimarySelection(IDrawingElement drawingElement);
	
	void addSecondarySelection(IDrawingElement drawingElement);
	
	IDrawingElement getPrimarySelection();
	
	Iterator<IDrawingElement> secondarySelectionIterator();
	
	Iterator<IDrawingElement> selectionIterator();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingElement testElement);
}
