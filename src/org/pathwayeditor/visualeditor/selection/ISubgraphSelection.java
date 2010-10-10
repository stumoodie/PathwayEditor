package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

public interface ISubgraphSelection {

	Iterator<INodeSelection> selectedNodeIterator();

	Iterator<ILinkSelection> selectedLinkIterator();

	Iterator<INodeSelection> topSelectedNodeIterator();

	ISelectionRecord getSelectionRecord();

	IDrawingElementSelection getDrawingElementSelection();

	int numTopDrawingNodes();

}
