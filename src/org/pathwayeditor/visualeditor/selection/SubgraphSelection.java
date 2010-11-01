package org.pathwayeditor.visualeditor.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;

public class SubgraphSelection implements ISubgraphSelection {
	private final ISubCompoundGraph subgraphSelection;
	private final SelectionRecord selectionRecord;
	private final IViewControllerModel viewControllerStore;
	private final Map<IDrawingElementController, ISelection> subGraphSelections = new HashMap<IDrawingElementController, ISelection>();
	
	public SubgraphSelection(SelectionRecord selectionRecord, IViewControllerModel viewControllerStore, ISubCompoundGraph currentSelectionSubgraph) {
		this.subgraphSelection = currentSelectionSubgraph;
		this.selectionRecord = selectionRecord;
		this.viewControllerStore = viewControllerStore;
		Iterator<ICompoundNode> nodeIter = currentSelectionSubgraph.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode drawingNode = nodeIter.next();
			INodeController nodeController = this.viewControllerStore.getNodeController(drawingNode);
			if(!this.selectionRecord.containsSelection(nodeController)){
				this.subGraphSelections.put(nodeController, new NodeSelection(SelectionType.SUBGRAPH, nodeController));
			}
		}
		Iterator<ICompoundEdge> linkIter = currentSelectionSubgraph.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge drawingNode = linkIter.next();
			ILinkController linkController = this.viewControllerStore.getLinkController(drawingNode);
			if(!this.selectionRecord.containsSelection(linkController)){
				this.subGraphSelections.put(linkController, new LinkSelection(SelectionType.SUBGRAPH, linkController));
			}
		}
	}

	@Override
	public ISubCompoundGraph getDrawingElementSelection() {
		return this.subgraphSelection;
	}

	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selectionRecord;
	}

	@Override
	public int numTopDrawingNodes() {
		return this.subgraphSelection.getNumTopNodes();
	}

	@Override
	public Iterator<ILinkSelection> selectedLinkIterator() {
		final Iterator<ICompoundEdge> iter = this.subgraphSelection.edgeIterator();
		Iterator<ILinkSelection> retVal = new Iterator<ILinkSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ILinkSelection next() {
				ILinkController linkController = viewControllerStore.getLinkController(iter.next());
				ILinkSelection retVal = selectionRecord.getLinkSelection(linkController);
				if(retVal == null){
					retVal = (ILinkSelection)subGraphSelections.get(linkController);
				}
				return retVal;
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
		final Iterator<ICompoundNode> iter = this.subgraphSelection.nodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next());
				INodeSelection retVal = selectionRecord.getNodeSelection(nodeController);
				if(retVal == null){
					retVal = (INodeSelection)subGraphSelections.get(nodeController);
				}
				return retVal;
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
		final Iterator<ICompoundNode> iter = this.subgraphSelection.topNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next());
				INodeSelection retVal = selectionRecord.getNodeSelection(nodeController);
				if(retVal == null){
					retVal = (INodeSelection)subGraphSelections.get(nodeController);
				}
				return retVal;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

}
