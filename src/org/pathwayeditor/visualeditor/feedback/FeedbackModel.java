/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.feedback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ITypedDrawingNodeAttribute;
import org.pathwayeditor.figure.rendering.IAnchorLocatorFactory;
import org.pathwayeditor.visualeditor.controller.IConnectingNodeController;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.geometry.ILinkDefinitionAnchorCalculator;
import org.pathwayeditor.visualeditor.geometry.LinkDefinitionAnchorCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

import uk.ac.ed.inf.graph.compound.CompoundNodePair;
import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.util.IFilterCriteria;
import uk.ac.ed.inf.graph.util.impl.FilteredIterator;

public class FeedbackModel implements IFeedbackModel {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final Set<IFeedbackNode> nodes;
	private final Set<IFeedbackLink> links;
	private final Map<IDrawingElementController, IFeedbackElement> selectionMapping;
	private final ISelectionRecord selectionRecord;
	private final IFeedbackNodeBuilder builder;
	private final IFeedbackLinkBuilder linkBuilder;
	
	public FeedbackModel(ISelectionRecord selectionRecord){
		this.nodes = new HashSet<IFeedbackNode>();
		this.links = new HashSet<IFeedbackLink>();
		this.selectionMapping = new HashMap<IDrawingElementController, IFeedbackElement>();
		this.selectionRecord = selectionRecord;
		this.builder = new FeedbackNodeBuilder(this);
		this.linkBuilder = new FeedbackLinkBuilder(this);
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
			if(selectedNode instanceof IConnectingNodeController){
				ICompoundNode shapeNode = ((IConnectingNodeController)selectedNode).getAssociatedAttribute().getCurrentElement();
				Iterator<ICompoundEdge> incidentEdgeIterator = shapeNode.getOutEdgeIterator();
				while(incidentEdgeIterator.hasNext()){
					ICompoundEdge linkAtt = incidentEdgeIterator.next();
					ILinkController linkController = selectedNode.getViewModel().getController(linkAtt.getAttribute()); 
					incidentEdgeSet.add(linkController);
				}
				Iterator<ICompoundEdge> incidentTgtEdgeIterator = shapeNode.getInEdgeIterator();
				while(incidentTgtEdgeIterator.hasNext()){
					ICompoundEdge linkAtt = incidentTgtEdgeIterator.next();
					ILinkController linkController= selectedNode.getViewModel().getController(linkAtt.getAttribute()); 
					incidentEdgeSet.add(linkController);
				}
			}					
			IFeedbackNode feedbackNode = builder.createFromDrawingNodeAttribute(selectedNode.getAssociatedAttribute());
			this.selectionMapping.put(selectedNode, feedbackNode);
		}
	}
	
	private void buildGraphSelectionLinks(Iterator<ILinkSelection> iter, Set<ILinkController> incidentEdgeSet){
		while(iter.hasNext()){
			ILinkSelection selectedLink = iter.next();
			ILinkController linkController = selectedLink.getPrimitiveController(); 
			incidentEdgeSet.remove(linkController);
			IFeedbackLink feedbackLink = createFeedbackLink(selectedLink.getPrimitiveController());
			this.links.add(feedbackLink);
			this.selectionMapping.put(linkController, feedbackLink);
			buildLinkLabels(linkController);
		}
		for(ILinkController linkEdge : incidentEdgeSet){
			IFeedbackLink feedbackLink = createFeedbackLink(linkEdge);
			this.links.add(feedbackLink);
			buildLinkLabels(linkEdge);
		}
	}
	
	
	private void buildLinkLabels(ILinkController linkController){
		FilteredIterator<ICompoundNode> labelIter = new FilteredIterator<ICompoundNode>(linkController.getAssociatedAttribute().getCurrentElement().getChildCompoundGraph().nodeIterator(),
				new IFilterCriteria<ICompoundNode>() {
					@Override
					public boolean matched(ICompoundNode testObj) {
						return testObj.getAttribute() instanceof ILabelAttribute;
					}
				});
//		linkController.getDrawingElement().getGraphElement().getChildCompoundGraph()); 
//		Iterator<ICompoundNode> labelIter = linkSubmodel.labelIterator();
		while(labelIter.hasNext()){
			ICompoundNode label = labelIter.next();
			INodeController labelController = linkController.getViewModel().getController(label.getAttribute()); 
			if(!this.selectionMapping.containsKey(labelController)){
				IFeedbackNode feedbackNode = builder.createFromDrawingNodeAttribute(labelController.getAssociatedAttribute());
				this.selectionMapping.put(labelController, feedbackNode);
			}
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
		final Iterator<INodeSelection> selectionNodeIter = this.selectionRecord.selectedNodeIterator();
		buildGraphSelectionNodes(selectionNodeIter, incidentEdgeSet);
		final Iterator<ILinkSelection> linkIter = this.selectionRecord.selectedLinkIterator();
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

	private static IAnchorLocatorFactory getCorrectAnchorFactory(IFeedbackNode feedbackNode, IConnectingNodeController nodeController){
		IAnchorLocatorFactory srcAnchorLocatorFact = null;
		if(feedbackNode != null){
			srcAnchorLocatorFact = feedbackNode.getFigureController().getAnchorLocatorFactory();
		}
		else{
			srcAnchorLocatorFact = nodeController.getFigureController().getAnchorLocatorFactory();
		}
		return srcAnchorLocatorFact;
	}
	
	private IFeedbackLink createFeedbackLink(ILinkController linkEdge){
		CompoundNodePair pair = linkEdge.getAssociatedAttribute().getCurrentElement().getConnectedNodes();
		ICompoundNode srcShape = pair.getOutNode();
		IViewControllerModel viewControllerStore = linkEdge.getViewModel();
		IConnectingNodeController srcShapeController = viewControllerStore.getController((ITypedDrawingNodeAttribute)srcShape.getAttribute()); 
//		IAnchorLocator srcAnchorLocator = srcShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		ILinkAttribute linkAtt = linkEdge.getAssociatedAttribute();
		ICompoundNode tgtShape = pair.getInNode();
		IConnectingNodeController tgtShapeController = viewControllerStore.getController((ITypedDrawingNodeAttribute)tgtShape.getAttribute()); 
//		IAnchorLocator tgtAnchorLocator = tgtShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		final IFeedbackLink retVal = this.linkBuilder.createFromAttribute(linkAtt);
		final IFeedbackNode srcNode = (IFeedbackNode)this.selectionMapping.get(srcShapeController);
		final IAnchorLocatorFactory srcAnchorLocatorFact = getCorrectAnchorFactory(srcNode, srcShapeController);
		final IFeedbackNode tgtNode = (IFeedbackNode)this.selectionMapping.get(tgtShapeController);
		final IAnchorLocatorFactory tgtAnchorLocatorFact = getCorrectAnchorFactory(tgtNode, tgtShapeController);
		if(srcNode != null){
			srcNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
				@Override
				public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
					ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(retVal.getLinkDefinition());
					anchorCalc.setSrcLocation(e.getNode().getFigureController().getAnchorLocatorFactory().createAnchorLocator());
					anchorCalc.setTgtLocation(tgtAnchorLocatorFact.createAnchorLocator());
					anchorCalc.recalculateBothAnchors();
				}
				@Override
				public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
					ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(retVal.getLinkDefinition());
					anchorCalc.setSrcLocation(e.getNode().getFigureController().getAnchorLocatorFactory().createAnchorLocator());
					anchorCalc.setTgtLocation(tgtAnchorLocatorFact.createAnchorLocator());
					anchorCalc.recalculateBothAnchors();
				}
			});
		}
		if(tgtNode != null){
			tgtNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
				@Override
				public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
					ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(retVal.getLinkDefinition());
					anchorCalc.setSrcLocation(srcAnchorLocatorFact.createAnchorLocator());
					anchorCalc.setTgtLocation(e.getNode().getFigureController().getAnchorLocatorFactory().createAnchorLocator());
					anchorCalc.recalculateBothAnchors();
				}
				@Override
				public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
					ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(retVal.getLinkDefinition());
					anchorCalc.setSrcLocation(srcAnchorLocatorFact.createAnchorLocator());
					anchorCalc.setTgtLocation(e.getNode().getFigureController().getAnchorLocatorFactory().createAnchorLocator());
					anchorCalc.recalculateBothAnchors();
				}
			});
		}
		retVal.addFeedbackLinkListener(new IFeedbackLinkListener() {
			@Override
			public void linkChangeEvent(IFeedbackLinkChangeEvent e) {
				ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(e.getNewLinkDefintion());
				anchorCalc.setSrcLocation(srcAnchorLocatorFact.createAnchorLocator());
				anchorCalc.setTgtLocation(tgtAnchorLocatorFact.createAnchorLocator());
				anchorCalc.recalculateBothAnchors();
				if(logger.isTraceEnabled()){
					logger.trace("Feedback link changed: recalculated link end points");
				}
			}
		});
		return retVal;
	}


	@Override
	public void rebuildOnLinkSelection(ILinkSelection selection) {
		this.nodes.clear();
		this.links.clear();
		this.selectionMapping.clear();
		IFeedbackLink feedbackLink = createFeedbackLink(selection.getPrimitiveController());
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

	void addNode(IFeedbackNode newNode) {
		this.nodes.add(newNode);
	}

	void addEdge(IFeedbackLink newEdge) {
		this.links.add(newEdge);
	}

	@Override
	public IFeedbackLinkBuilder getFeedbackLinkBuilder() {
		return this.linkBuilder;
	}
}
