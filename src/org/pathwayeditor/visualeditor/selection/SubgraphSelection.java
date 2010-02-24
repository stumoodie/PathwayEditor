package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

public class SubgraphSelection implements ISubgraphSelection {
	private final IDrawingElementSelection subgraphSelection;
	private final SelectionRecord selectionRecord;
	private final IViewControllerStore viewControllerStore;
	
	public SubgraphSelection(SelectionRecord selectionRecord, IViewControllerStore viewControllerStore, IDrawingElementSelection createEdgeExcludedSelection) {
		this.subgraphSelection = createEdgeExcludedSelection;
		this.selectionRecord = selectionRecord;
		this.viewControllerStore = viewControllerStore;
	}

	@Override
	public IDrawingElementSelection getDrawingElementSelection() {
		return this.subgraphSelection;
	}

	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selectionRecord;
	}

	@Override
	public int numTopDrawingNodes() {
		return this.subgraphSelection.numTopDrawingNodes();
	}

	@Override
	public Iterator<ILinkSelection> selectedLinkIterator() {
		final Iterator<ILinkEdge> iter = this.subgraphSelection.linkEdgeIterator();
		Iterator<ILinkSelection> retVal = new Iterator<ILinkSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ILinkSelection next() {
				ILinkController linkController = viewControllerStore.getLinkController(iter.next().getAttribute());
				if(!selectionRecord.containsSelection(linkController)){
					// no selection so create a new subgraph selection
					selectionRecord.createLinkSubgraphSelection(linkController);
				}
				return selectionRecord.getLinkSelection(linkController);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

	@Override
	public Iterator<INodeSelection> selectedNodeIterator() {
		final Iterator<IDrawingNode> iter = this.subgraphSelection.drawingNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next().getAttribute());
				if(!selectionRecord.containsSelection(nodeController)){
					// no selection so create a new subgraph selection
					selectionRecord.createNodeSubgraphSelection(nodeController);
				}
				return selectionRecord.getNodeSelection(nodeController);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

	@Override
	public Iterator<INodeSelection> topSelectedNodeIterator() {
		// ensure that this graph selection is initialised
		final Iterator<IDrawingNode> iter = this.subgraphSelection.topDrawingNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next().getAttribute());
				if(!selectionRecord.containsSelection(nodeController)){
					// no selection so create a new subgraph selection
					selectionRecord.createNodeSubgraphSelection(nodeController);
				}
				return selectionRecord.getNodeSelection(nodeController);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

}
