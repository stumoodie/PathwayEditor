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
package org.pathwayeditor.visualeditor.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttributeVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.FastShapeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.IElementAttribute;
import uk.ac.ed.inf.graph.compound.IGraphRestoreStateAction;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction.GraphStructureChangeType;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeListener;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;

public class ViewControllerStore implements IViewControllerModel {
	private static final Envelope DEFAULT_CANVAS_BOUNDS = new Envelope(0.0, 0.0, 600.0, 600.0);
//	private final Logger logger = Logger.getLogger(this.getClass());
	private final IModel domainModel;
	private final SortedMap<ICompoundGraphElement, IDrawingElementController> domainToViewMap;
	private final SortedSet<IDrawingElementController> drawingPrimitives;
	private IGraphStructureChangeListener modelListener;
	private IRootController rootPrimitive;
	private boolean isActive = false;
	private final List<IViewControllerChangeListener> listeners;
	private int indexCounter = 0;
	private final IIntersectionCalculator intersectionCalculator;
	
	public ViewControllerStore(IModel domainModel){
		this.domainModel = domainModel;
		this.domainToViewMap = new TreeMap<ICompoundGraphElement, IDrawingElementController>(new Comparator<ICompoundGraphElement>(){

			@Override
			public int compare(ICompoundGraphElement o1, ICompoundGraphElement o2) {
//				return Integer.valueOf(o1.getAttribute().getCreationSerial()).compareTo(Integer.valueOf(o2.getAttribute().getCreationSerial()));
//				return o1.compareTo(o2);
				int retVal = o1.getLevel() < o2.getLevel() ? -1 : (o1.getLevel() > o2.getLevel() ? 1 : 0);
				if(retVal == 0){
					retVal = o1.getIndex() < o2.getIndex() ? -1 : (o1.getIndex() > o2.getIndex() ? 1 : 0);
				}
//				if(logger.isTraceEnabled()){
//					StringBuilder buf = new StringBuilder("retVal=");
//					buf.append(retVal);
//					buf.append(",o1Lvl=");
//					buf.append(o1.getLevel());
//					buf.append(",o1Uid=");
//					buf.append(o1.getUniqueIndex());
//					buf.append(",o2Lvl=");
//					buf.append(o2.getLevel());
//					buf.append(",o2Uid=");
//					buf.append(o2.getUniqueIndex());
//					buf.append(",o1=");
//					buf.append(o1);
//					buf.append(",o2=");
//					buf.append(o2);
//					logger.trace(buf.toString());
//				}
				return retVal;
			}
			
		});
		this.drawingPrimitives = new TreeSet<IDrawingElementController>();
		this.listeners = new LinkedList<IViewControllerChangeListener>();
		buildFromDomainModel();
		this.intersectionCalculator = new FastShapeIntersectionCalculator(this);
		initialiseDomainListeners();
		if(this.domainModel.numDrawingElements() != this.domainToViewMap.size()){
			throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
		}
		if(this.domainModel.numDrawingElements() != this.drawingPrimitives.size()){
			throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
		}
	}
	
	private void initialiseDomainListeners() {
		modelListener = new IGraphStructureChangeListener(){
			@Override
			public void graphStructureChange(IGraphStructureChangeAction event) {
				if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_REMOVED)){
					inactivateSelection(event.originalSubgraph());
					removeSelection(event.originalSubgraph());
				}
				else if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_MOVED)){
					inactivateSelection(event.originalSubgraph());
					removeSelection(event.originalSubgraph());
					addSelection(event.changedSubgraph());
					activateSelection(event.changedSubgraph());
				}
				else if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_COPIED)){
					addSelection(event.changedSubgraph());
					activateSelection(event.changedSubgraph());
				}
				else if(event.getChangeType().equals(GraphStructureChangeType.ELEMENT_ADDED)){
					addSelection(event.changedSubgraph());
					activateSelection(event.changedSubgraph());
				}
				if(domainModel.numDrawingElements() != domainToViewMap.size()){
					throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
				}
				if(domainModel.numDrawingElements() != drawingPrimitives.size()){
					throw new IllegalStateException("Inconsistent number of controllers and drawing elements");
				}
			}
			@Override
			public void notifyRestoreCompleted(IGraphRestoreStateAction event) {
				inactivateSelection(event.getRemovedElements());
				removeSelection(event.getRemovedElements());
				addSelection(event.getRestoredElements());
				activateSelection(event.getRestoredElements());
			}
		};
		this.domainModel.getGraph().addGraphStructureChangeListener(modelListener);
	}
	
	private void addSelection(ISubCompoundGraph selection){
		Iterator<ICompoundNode> nodeIter = selection.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			INodeController newNode = createNodePrimitive((IDrawingNodeAttribute)node.getAttribute());
			notifyAddedNode(newNode);
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController newLinkCont = createLinkPrimitive((ILinkAttribute)link.getAttribute());
			notifyAddedLink(newLinkCont);
		}
	}
		
	private void removeSelection(ISubCompoundGraph selection){
		removeNodeSelection(selection);
		removeEdgeSelection(selection);
	}
	
	private void activateSelection(ISubCompoundGraph selection) {
		Iterator<ICompoundNode> nodeIter = selection.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			INodeController nodePrimitive = getController((IDrawingNodeAttribute)node.getAttribute());
			nodePrimitive.activate();
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController linkController = getController((ILinkAttribute)link.getAttribute());
			linkController.activate();
		}
	}

	private void inactivateSelection(ISubCompoundGraph selection) {
		Iterator<ICompoundNode> nodeIter = selection.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			INodeController nodePrimitive = getController((IDrawingNodeAttribute)node.getAttribute());
			nodePrimitive.inactivate();
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController linkController = getController((ILinkAttribute)link.getAttribute());
			linkController.inactivate();
		}
	}

	private void removeNodeSelection(ISubCompoundGraph selection){
		Iterator<ICompoundNode> iter = selection.nodeIterator();
		while(iter.hasNext()){
			ICompoundNode node = iter.next();
			INodeController nodeController = getController((IDrawingNodeAttribute)node.getAttribute()); 
			notifyRemovedNode(nodeController);
			removeNodeController(nodeController);
		}
	}
	
	private void removeEdgeSelection(ISubCompoundGraph selection){
		Iterator<ICompoundEdge> iter = selection.edgeIterator();
		while(iter.hasNext()){
			ICompoundEdge link = iter.next();
			ILinkController linkController = getController((ILinkAttribute)link.getAttribute()); 
			notifyRemovedLink(linkController);
			removeLinkController(linkController);
		}
	}
	
	private void removeLinkController(ILinkController linkController) {
		domainToViewMap.remove(linkController.getAssociatedAttribute().getCurrentElement());
		drawingPrimitives.remove(linkController);
	}

	private void removeNodeController(INodeController shapeNode){
		domainToViewMap.remove(shapeNode.getAssociatedAttribute().getCurrentElement());
		drawingPrimitives.remove(shapeNode);
	}

	private void buildFromDomainModel(){
		Iterator<IDrawingNodeAttribute> nodeIter = this.domainModel.drawingNodeAttributeIterator();
		while(nodeIter.hasNext()){
			IDrawingNodeAttribute node = nodeIter.next();
			createNodePrimitive(node);
		}
		Iterator<ILinkAttribute> edgeIter = this.domainModel.linkAttributeIterator();
		while(edgeIter.hasNext()){
			ILinkAttribute link = edgeIter.next();
			createLinkPrimitive(link);
		}
	}
	
	private INodeController createNodePrimitive(final IDrawingNodeAttribute node){
//		ICanvasElementAttribute node = (ICanvasElementAttribute)graphNode.getAttribute();
		ElementControllerFactory factory = new ElementControllerFactory(this); 
		node.visit(factory);
		INodeController viewNode = factory.getCreatedNodeController(); 
		if(viewNode != null){
			this.domainToViewMap.put(node.getCurrentElement(), viewNode);
			this.drawingPrimitives.add(viewNode);
		}
		return viewNode;
	}
	
	private ILinkController createLinkPrimitive(ILinkAttribute linkEdge){
//		ICompoundEdge linkEdge = graphLink;
		ILinkController linkCtlr = new LinkController(this, linkEdge, indexCounter++);
		this.domainToViewMap.put(linkEdge.getCurrentElement(), linkCtlr);
		this.drawingPrimitives.add(linkCtlr);
		return linkCtlr;
	}
	
	
	private void notifyAddedNode(final INodeController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.NODE_ADDED;
			}

			@Override
			public INodeController getChangedElement() {
				return viewNode;
			}
			
		};
		notifyEvent(e);
	}

	private void notifyRemovedNode(final INodeController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.NODE_REMOVED;
			}

			@Override
			public INodeController getChangedElement() {
				return viewNode;
			}
			
		};
		notifyEvent(e);
	}

	private void notifyAddedLink(final ILinkController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.LINK_ADDED;
			}

			@Override
			public ILinkController getChangedElement() {
				return viewNode;
			}
			
		};
		notifyEvent(e);
	}

	private void notifyRemovedLink(final ILinkController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.LINK_REMOVED;
			}

			@Override
			public ILinkController getChangedElement() {
				return viewNode;
			}
			
		};
		notifyEvent(e);
	}

	private void notifyEvent(IViewControllerNodeStructureChangeEvent e) {
		for(IViewControllerChangeListener listener : this.listeners){
			listener.nodeStructureChangeEvent(e);
		}
	}

	@Override
	public Iterator<IDrawingElementController> drawingPrimitiveIterator() {
		return this.drawingPrimitives.iterator();
	}

	@Override
	public IModel getDomainModel() {
		return this.domainModel;
	}

//	@Override
//	public INodeController getNodeController(IDrawingNodeAttribute draggedNode) {
//		IDrawingElementController retVal = this.domainToViewMap.get(draggedNode);
//		if(retVal == null){
//			throw new IllegalArgumentException("domain node is not present in this view model");
//		}
//		return (INodeController)retVal;
//	}

	@Override
	public IRootController getRootNode() {
		return this.rootPrimitive;
	}

	@Override
	public Iterator<ILabelController> labelControllerIterator() {
		List<ILabelController> retList = new LinkedList<ILabelController>();
		for(IDrawingElementController primitive : this.drawingPrimitives){
			if(primitive instanceof ILabelController){
				retList.add((ILabelController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<ILinkController> linkControllerIterator() {
		List<ILinkController> retList = new LinkedList<ILinkController>();
		for(IDrawingElementController primitive : this.drawingPrimitives){
			if(primitive instanceof ILinkController){
				retList.add((ILinkController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<INodeController> nodeControllerIterator() {
		List<INodeController> retList = new LinkedList<INodeController>();
		for(IDrawingElementController primitive : this.drawingPrimitives){
			if(primitive instanceof INodeController){
				retList.add((INodeController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<IShapeController> shapeControllerIterator() {
		List<IShapeController> retList = new LinkedList<IShapeController>();
		for(IDrawingElementController primitive : this.drawingPrimitives){
			if(primitive instanceof IShapeController){
				retList.add((IShapeController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public boolean containsDrawingElement(IElementAttribute testPrimitive) {
		return this.domainToViewMap.containsKey(testPrimitive.getCurrentElement());
	}

	@Override
	public void activate() {
        // now activate all the drawing primitives
		for(IDrawingElementController prim : this.drawingPrimitives){
        	prim.activate();
        }
		this.isActive = true;
	}

	@Override
	public void deactivate() {
		for(IDrawingElementController prim : this.drawingPrimitives){
        	prim.inactivate();
        }
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

//	@Override
//	public ILinkController getLinkController(ILinkAttribute attribute) {
//		return (ILinkController)this.domainToViewMap.get(attribute);
//	}
//
//	@Override
//	public IShapeController getShapeController(IShapeAttribute attribute) {
//		return (IShapeController)this.domainToViewMap.get(attribute.getCurrentElement());
//	}

	@Override
	public Envelope getCanvasBounds() {
		Envelope retVal = DEFAULT_CANVAS_BOUNDS;
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		Iterator<INodeController> nodeIter = this.nodeControllerIterator();
		boolean onlyRoot = true;
		while(nodeIter.hasNext()){
			INodeController nodeController = nodeIter.next();
			if(!(nodeController instanceof IRootController)){
				onlyRoot = false;
				// ignore the root as this doesn't have real bounds values - we are only intersted in shapes and labels
				Point nodeOrigin = nodeController.getConvexHull().getEnvelope().getOrigin();
				minX = Math.min(minX, nodeOrigin.getX());
				minY = Math.min(minY, nodeOrigin.getY());
				Point nodeDiagonal = nodeController.getConvexHull().getEnvelope().getDiagonalCorner();
				maxX = Math.max(maxX, nodeDiagonal.getX());
				maxY = Math.max(maxY, nodeDiagonal.getY());
			}
		}
		if(!onlyRoot){
			Iterator<ILinkController> edgeIter = this.linkControllerIterator();
			while(edgeIter.hasNext()){
				ILinkController linkController = edgeIter.next();
				ILinkPointDefinition defn = linkController.getLinkDefinition();
				Iterator<Point> pointIter = defn.pointIterator();
				while(pointIter.hasNext()){
					Point linkPoint = pointIter.next();
					minX = Math.min(minX, linkPoint.getX());
					minY = Math.min(minY, linkPoint.getY());
					maxX = Math.max(maxX, linkPoint.getX());
					maxY = Math.max(maxY, linkPoint.getY());
				}
			}
			retVal = new Envelope(minX, minY, maxX-minX, maxY-minY);
		}
		return retVal;
	}

	@Override
	public void addViewControllerChangeListener(IViewControllerChangeListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public List<IViewControllerChangeListener> getViewControllerChangeListeners() {
		return new ArrayList<IViewControllerChangeListener>(this.listeners);
	}

	@Override
	public void removeViewControllerChangeListener(IViewControllerChangeListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public IIntersectionCalculator getIntersectionCalculator() {
		return this.intersectionCalculator;
	}


//	@Override
//	public IDrawingElementController getDrawingPrimitiveController(ICanvasElementAttribute testAttribute) {
//		return this.domainToViewMap.get(testAttribute);
//	}
	
	private class ElementControllerFactory implements ICanvasElementAttributeVisitor {
		private INodeController viewNode = null;
		private final IViewControllerModel viewController;
		
		public ElementControllerFactory(IViewControllerModel viewController){
			this.viewController = viewController;
		}
		
		public INodeController getCreatedNodeController(){
			return this.viewNode;
		}
		
		@Override
		public void visitRoot(IRootAttribute attribute) {
			this.viewNode = new RootController(viewController, attribute, indexCounter++);
			rootPrimitive = (IRootController)this.viewNode;
		}

		@Override
		public void visitShape(IShapeAttribute attribute) {
			viewNode = new ShapeController(viewController, attribute, indexCounter++);
		}

		@Override
		public void visitLink(ILinkAttribute attribute) {
			throw new RuntimeException("node is of unknown type");
		}

		@Override
		public void visitLabel(final ILabelAttribute attribute) {
			attribute.visit(new ICanvasElementAttributeVisitor() {
				
				@Override
				public void visitShape(IShapeAttribute a) {
					viewNode = new LabelController(viewController, attribute, indexCounter++);
				}
				
				@Override
				public void visitRoot(IRootAttribute a) {
				}
				
				@Override
				public void visitLink(ILinkAttribute a) {
					viewNode = new LinkLabelController(viewController, attribute, indexCounter++);
				}
				
				@Override
				public void visitLabel(ILabelAttribute a) {
				}
				
				@Override
				public void visitAnchorNode(IAnchorNodeAttribute a) {
				}
			});
		}

		@Override
		public void visitAnchorNode(IAnchorNodeAttribute anchorNodeAttribute) {
			this.viewNode = new AnchorNodeController(viewController, anchorNodeAttribute, indexCounter++);
		}
		
	}

	@Override
	public IDrawingElementController findControllerByAttribute(IElementAttribute testAttribute) {
		return this.domainToViewMap.get(testAttribute.getCurrentElement());
	}

	@Override
	public Iterator<IAnchorNodeController> anchorNodeControllerIterator() {
		List<IAnchorNodeController> retList = new LinkedList<IAnchorNodeController>();
		for(IDrawingElementController primitive : this.drawingPrimitives){
			if(primitive instanceof IAnchorNodeController){
				retList.add((IAnchorNodeController)primitive);
			}
		}
		return retList.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IDrawingElementController> T getController(IElementAttribute attribute) {
		return (T)this.domainToViewMap.get(attribute.getCurrentElement());
	}

//	@Override
//	public IAnchorNodeController getAnchorNodeController(IAnchorNodeAttribute attribute) {
//		return (IAnchorNodeController)this.domainToViewMap.get(attribute.getCurrentElement());
//	}
//
//	@Override
//	public IConnectingNodeController getConnectingNodeController(ITypedDrawingNodeAttribute att) {
//		return (IConnectingNodeController)this.domainToViewMap.get(att.getCurrentElement());
//	}
}
