package org.pathwayeditor.visualeditor.feedback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

import uk.ac.ed.inf.graph.compound.CompoundNodePair;
import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundNode;

public class FeedbackModel implements IFeedbackModel {
	private final Set<IFeedbackNode> nodes;
	private final Set<IFeedbackLink> links;
	private final Map<IDrawingElementController, IFeedbackElement> selectionMapping;
	private final ISelectionRecord selectionRecord;
	private final IFeedbackNodeBuilder builder;
	
	public FeedbackModel(ISelectionRecord selectionRecord){
		this.nodes = new HashSet<IFeedbackNode>();
		this.links = new HashSet<IFeedbackLink>();
		this.selectionMapping = new HashMap<IDrawingElementController, IFeedbackElement>();
		this.selectionRecord = selectionRecord;
		this.builder = new FeedbackNodeBuilder(this);
	}
	
	@Override
	public void rebuildIncludingHierarchy(){
		this.nodes.clear();
		this.links.clear();
		this.selectionMapping.clear();
		Set<ILinkController> incidentEdgeSet = new HashSet<ILinkController>();
		buildGraphSelectionNodes(this.selectionRecord.getSubgraphSelection().selectedNodeIterator(), incidentEdgeSet);
		buildGraphSelectionLinks(this.selectionRecord.getSubgraphSelection().selectedLinkIterator(), incidentEdgeSet);
	}
	
	private void buildGraphSelectionNodes(Iterator<INodeSelection> iter, Set<ILinkController> incidentEdgeSet){
		while(iter.hasNext()){
			INodeSelection nodeSelection = iter.next();
			INodeController selectedNode = nodeSelection.getPrimitiveController();
			if(selectedNode instanceof IShapeController){
				ICompoundNode shapeNode = ((IShapeController)selectedNode).getDrawingElement();
				Iterator<ICompoundEdge> incidentEdgeIterator = shapeNode.getOutEdgeIterator();
				while(incidentEdgeIterator.hasNext()){
					ICompoundEdge linkAtt = incidentEdgeIterator.next();
					ILinkController linkController = selectedNode.getViewModel().getLinkController(linkAtt); 
					incidentEdgeSet.add(linkController);
				}
				Iterator<ICompoundEdge> incidentTgtEdgeIterator = shapeNode.getInEdgeIterator();
				while(incidentTgtEdgeIterator.hasNext()){
					ICompoundEdge linkAtt = incidentTgtEdgeIterator.next();
					ILinkController linkController= selectedNode.getViewModel().getLinkController(linkAtt); 
					incidentEdgeSet.add(linkController);
				}
			}					
			IFeedbackNode feedbackNode = builder.createFromDrawingNodeAttribute((IDrawingNodeAttribute)selectedNode.getDrawingElement().getAttribute());
//			this.nodes.add(feedbackNode);
			this.selectionMapping.put(selectedNode, feedbackNode);
		}
	}
	
	private void buildGraphSelectionLinks(Iterator<ILinkSelection> iter, Set<ILinkController> incidentEdgeSet){
		while(iter.hasNext()){
			ILinkSelection selectedLink = iter.next();
			ILinkController linkController = selectedLink.getPrimitiveController(); 
			incidentEdgeSet.remove(linkController);
			IFeedbackLink feedbackLink = createFeedbackLink(selectedLink.getPrimitiveController(), selectedLink.getPrimitiveController().getViewModel());
			this.links.add(feedbackLink);
			this.selectionMapping.put(linkController, feedbackLink);
		}
		IViewControllerModel viewController = this.selectionRecord.getPrimarySelection().getPrimitiveController().getViewModel();
		for(ILinkController linkEdge : incidentEdgeSet){
			IFeedbackLink feedbackLink = createFeedbackLink(linkEdge, viewController);
			this.links.add(feedbackLink);
		}
	}
	
	@Override
	public void clear(){
		this.nodes.clear();
		this.links.clear();
		this.selectionMapping.clear();
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
		this.links.clear();
		this.selectionMapping.clear();
		Set<ILinkController> incidentEdgeSet = new HashSet<ILinkController>();
		final Iterator<INodeSelection> selectionNodeIter = this.selectionRecord.selectedNodesIterator();
		buildGraphSelectionNodes(selectionNodeIter, incidentEdgeSet);
		final Iterator<ILinkSelection> linkIter = this.selectionRecord.selectedLinksIterator();
		buildGraphSelectionLinks(linkIter, incidentEdgeSet);
//		buildStrictSelectionNodes();
//		buildStrictSelectionLinks();
	}
	
//	private void buildStrictSelectionNodes(){
//		Iterator<INodeSelection> iter = this.selectionRecord.selectedNodesIterator();
//		while(iter.hasNext()){
//			IDrawingNodeAttribute selectedNode = iter.next().getPrimitiveController().getDrawingElement();
//			IFeedbackNode feedbackNode = new FeedbackNode(selectedNode);
//			this.nodes.add(feedbackNode);
//		}
//	}
//
//	private void buildStrictSelectionLinks(){
//		Iterator<ILinkSelection> iter = this.selectionRecord.selectedLinksIterator();
//		while(iter.hasNext()){
//			ILinkController controller = iter.next().getPrimitiveController();
////			ILinkAttribute selectedLink = iter.next().getPrimitiveController().getDrawingElement();
//			IFeedbackLink feedbackLink = createFeedbackLink(controller.getDrawingElement().getCurrentDrawingElement(), controller.getViewModel());
//			this.links.add(feedbackLink);
//		}
//	}

	@Override
	public Iterator<IFeedbackElement> elementIterator() {
		return new ElementIterator();
	}

	@Override
	public Iterator<IFeedbackLink> linkIterator() {
		return this.links.iterator();
	}
	
	
	private class ElementIterator implements Iterator<IFeedbackElement> {
		private final Iterator<IFeedbackNode> nodeIter;
		private final Iterator<IFeedbackLink> linkIter;
		
		public ElementIterator(){
			this.nodeIter = nodes.iterator();
			this.linkIter = links.iterator();
		}
		
		@Override
		public boolean hasNext() {
			boolean retVal = nodeIter.hasNext();
			if(!retVal){
				retVal = linkIter.hasNext();
			}
			return retVal;
		}

		@Override
		public IFeedbackElement next() {
			IFeedbackElement retVal = null;
			if(nodeIter.hasNext()){
				retVal = nodeIter.next();
			}
			else{
				retVal = linkIter.next();
			}
			return retVal;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal not supported by this iterator");
		}
		
	}

	private FeedbackLink createFeedbackLink(ILinkController linkEdge, IViewControllerModel viewControllerStore){
		CompoundNodePair pair = linkEdge.getDrawingElement().getConnectedNodes();
		ICompoundNode srcShape = pair.getOutNode();
		IShapeController srcShapeController = viewControllerStore.getShapeController(srcShape); 
		IAnchorLocator srcAnchorLocator = srcShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		ILinkAttribute linkAtt = (ILinkAttribute)linkEdge.getDrawingElement().getAttribute();
		ICompoundNode tgtShape = pair.getInNode();
		IShapeController tgtShapeController = viewControllerStore.getShapeController(tgtShape); 
		IAnchorLocator tgtAnchorLocator = tgtShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		FeedbackLink retVal = new FeedbackLink((FeedbackNode)this.selectionMapping.get(srcShapeController),
				(FeedbackNode)this.selectionMapping.get(tgtShapeController), linkAtt.getCreationSerial(),
				linkAtt.getSourceTerminus().getLocation(), srcAnchorLocator, linkAtt.getTargetTerminus().getLocation(), tgtAnchorLocator);
		ILinkPointDefinition linkDefn = retVal.getLinkDefinition();
		Iterator<Point> bpIter = linkAtt.getBendPointContainer().bendPointIterator();
		while(bpIter.hasNext()){
			Point bp = bpIter.next();
			linkDefn.addNewBendPoint(bp);
		}
		linkDefn.setLineColour(linkAtt.getLineColour());
		linkDefn.setLineStyle(linkAtt.getLineStyle());
		linkDefn.setLineWidth(linkAtt.getLineWidth());
		linkDefn.getSourceTerminusDefinition().setEndDecoratorType(linkAtt.getSourceTerminus().getEndDecoratorType());
		linkDefn.getSourceTerminusDefinition().setGap(linkAtt.getSourceTerminus().getGap());
		linkDefn.getSourceTerminusDefinition().setEndSize(linkAtt.getSourceTerminus().getEndSize());
		linkDefn.getTargetTerminusDefinition().setEndDecoratorType(linkAtt.getTargetTerminus().getEndDecoratorType());
		linkDefn.getTargetTerminusDefinition().setGap(linkAtt.getTargetTerminus().getGap());
		linkDefn.getTargetTerminusDefinition().setEndSize(linkAtt.getTargetTerminus().getEndSize());
		return retVal;
	}


	@Override
	public void rebuildOnLinkSelection(ILinkSelection selection) {
		this.nodes.clear();
		this.links.clear();
		this.selectionMapping.clear();
		IFeedbackLink feedbackLink = createFeedbackLink(selection.getPrimitiveController(), selection.getPrimitiveController().getViewModel());
		this.links.add(feedbackLink);
	}

	@Override
	public IFeedbackLink uniqueFeedbackLink() {
		return this.links.iterator().next();
	}

	@Override
	public IFeedbackElement getFeedbackElement(IDrawingElementController controller) {
		return this.selectionMapping.get(controller);
	}

//	@Override
//	public IFeedbackNode createSingleNode(Envelope envelope) {
//		this.clear();
//		IFeedbackNode retVal = new FeedbackNode(0, envelope);
//		this.nodes.add(retVal);
//		return retVal;
//	}
	
	@Override
	public IFeedbackNodeBuilder getFeedbackNodeBuilder(){
		return this.builder;
	}

	@Override
	public IFeedbackNode uniqueFeedbackNode() {
		return this.nodes.iterator().next();
	}

	void addNode(FeedbackNode newNode) {
		this.nodes.add(newNode);
	}
}
