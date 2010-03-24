package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;
import java.util.Iterator;

import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class SelectionLayer implements ISelectionLayer {
	private final ISelectionRecord selections;
	
	public SelectionLayer(ISelectionRecord selectionRecord){
		this.selections = selectionRecord;
	}
	
	
	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selections;
	}

	@Override
	public void paint(Graphics2D g2d) {
		Iterator<INodeSelection> selectionIter = this.selections.selectedNodesIterator();
		while(selectionIter.hasNext()){
			INodeSelection node = selectionIter.next();
			SelectionShape selection = new SelectionShape(node);
			selection.paint(g2d);
		}
		Iterator<ILinkSelection> linkSelectionIter = this.selections.selectedLinksIterator();
		while(linkSelectionIter.hasNext()){
			ILinkSelection link = linkSelectionIter.next();
			SelectionLinkDrawer selection = new SelectionLinkDrawer(link);
			selection.paint(g2d);
		}
	}


	@Override
	public LayerType getLayerType() {
		return LayerType.SELECTION;
	}

}
