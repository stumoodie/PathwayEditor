package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionRecord {

	ISelectionHandle findSelectionModelAt(Point point);
	
	void setPrimarySelection(IDrawingElementController drawingElement);
	
	void addSecondarySelection(IDrawingElementController drawingElement);
	
	ISelection getPrimarySelection();
	
	Iterator<ISelection> secondarySelectionIterator();
	
	Iterator<ISelection> selectionIterator();
	
	Iterator<INodeSelection> selectedNodeIterator();
	
	Iterator<ILinkSelection> selectedLinkIterator();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingElementController testElement);

	ISubgraphSelection getSubgraphSelection();

	ILinkSelection getUniqueLinkSelection();

	ILinkSelection getLinkSelection(ILinkController next);

	INodeSelection getNodeSelection(INodeController next);

	ISelection getSelection(IDrawingElementController next);

	boolean containsSelection(IDrawingElementController controller);

	void restoreSelection();

	Envelope getTotalSelectionBounds();
}
