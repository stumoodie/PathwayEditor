package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;

public interface ISubgraphSelection {

	Iterator<INodeSelection> selectedNodeIterator();

	Iterator<ILinkSelection> selectedLinkIterator();

	Iterator<INodeSelection> topSelectedNodeIterator();

	ISelectionRecord getSelectionRecord();

	ISubCompoundGraph getDrawingElementSelection();

	int numTopDrawingNodes();

}
