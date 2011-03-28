package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

/**
 * ISelectionRecord is an interface that defines how selections are managed in the graphical editor.
 * The selection will always have a primary selection. If the primary selection if not a shape or label node
 * or link then it is the root node. When a selection record is cleared then the root node becomes the primary
 * selection.   
 * 
 * @author smoodie
 *
 */
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
