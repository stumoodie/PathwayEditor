package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class FeedbackModel implements IFeedbackModel {
	private final List<IFeedbackNode> nodes;
	private final ISelectionRecord selectionRecord;
	
	public FeedbackModel(ISelectionRecord selectionRecord){
		this.nodes = new LinkedList<IFeedbackNode>();
		this.selectionRecord = selectionRecord;
	}
	
	@Override
	public void rebuildIncludingHierarchy(){
		this.nodes.clear();
		Iterator<IDrawingNode> iter = this.selectionRecord.getGraphSelection().drawingNodeIterator();
		while(iter.hasNext()){
			IDrawingNode selectedNode = iter.next();
			IFeedbackNode feedbackNode = new FeedbackNode(selectedNode.getAttribute());
			this.nodes.add(feedbackNode);
		}
	}
	
	@Override
	public void clear(){
		this.nodes.clear();
	}
	
	@Override
	public Iterator<IFeedbackNode> nodeIterator() {
		return this.nodes.iterator();
	}

	@Override
	public ISelectionRecord getSelectionRecord(){
		return this.selectionRecord;
	}

	@Override
	public void rebuildWithStrictSelection() {
		this.nodes.clear();
		Iterator<INodeSelection> iter = this.selectionRecord.selectedNodesIterator();
		while(iter.hasNext()){
			IDrawingNodeAttribute selectedNode = iter.next().getPrimitiveController().getDrawingElement();
			IFeedbackNode feedbackNode = new FeedbackNode(selectedNode);
			this.nodes.add(feedbackNode);
		}
	}
}
