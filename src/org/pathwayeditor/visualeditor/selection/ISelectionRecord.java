package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

public interface ISelectionRecord {

	ISelectionHandle findSelectionModelAt(Point point);
	
	void setPrimarySelection(IDrawingPrimitiveController drawingElement);
	
	void addSecondarySelection(IDrawingPrimitiveController drawingElement);
	
	ISelection getPrimarySelection();
	
	Iterator<ISelection> secondarySelectionIterator();
	
	Iterator<ISelection> selectionIterator();
	
	Iterator<INodeSelection> selectedNodesIterator();
	
	Iterator<ILinkSelection> selectedLinksIterator();
	
//	Iterator<INodeSelection> getTopNodeSelection();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingPrimitiveController testElement);

	ISubgraphSelection getSubgraphSelection();

	ILinkSelection getUniqueLinkSelection();

	ILinkSelection getLinkSelection(ILinkController next);

	INodeSelection getNodeSelection(INodeController next);

	ISelection getSelection(IDrawingPrimitiveController next);

	boolean containsSelection(IDrawingPrimitiveController controller);
}
