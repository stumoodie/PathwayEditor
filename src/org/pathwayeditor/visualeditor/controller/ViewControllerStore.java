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

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.FastShapeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.IElementAttribute;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeAction.GraphStructureChangeType;
import uk.ac.ed.inf.graph.compound.IGraphStructureChangeListener;
import uk.ac.ed.inf.graph.compound.IRootCompoundNode;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;

public class ViewControllerStore implements IViewControllerModel {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICompoundGraph domainModel;
	private final SortedMap<ICompoundGraphElement, IDrawingElementController> domainToViewMap;
	private final SortedSet<IDrawingElementController> drawingPrimitives;
	private IGraphStructureChangeListener modelListener;
	private IRootController rootPrimitive;
	private boolean isActive = false;
	private final List<IViewControllerChangeListener> listeners;
	private int indexCounter = 0;
	private final IIntersectionCalculator intersectionCalculator;
	
	public ViewControllerStore(ICompoundGraph domainModel){
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
	}
	
	private void initialiseDomainListeners() {
		modelListener = new IGraphStructureChangeListener(){

			@Override
			public void graphStructureChange(IGraphStructureChangeAction event) {
				if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_REMOVED)){
//					rebuildModel();
					inactivateSelection(event.originalSubgraph());
					removeSelection(event.originalSubgraph());
				}
				else if(event.getChangeType().equals(GraphStructureChangeType.SUBGRAPH_MOVED)){
					// reinitialise the nodes
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
			}

		};
		this.domainModel.addGraphStructureChangeListener(modelListener);
//		Iterator<ICompoundNode> nodeIter = this.domainModel.nodeIterator();
//		while(nodeIter.hasNext()){
//			ICompoundNode node = nodeIter.next();
//			node.getChildCompoundGraph().addModelChangeListener(modelListener);
//		}
	}
	
	private void addSelection(ISubCompoundGraph selection){
		Iterator<ICompoundNode> nodeIter = selection.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			INodeController newNode = createNodePrimitive(node);
			notifyAddedNode(newNode);
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController newLinkCont = createLinkPrimitive(link);
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
			INodeController nodePrimitive = getNodeController(node);
			nodePrimitive.activate();
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController linkController = getLinkController(link);
			linkController.activate();
		}
	}

	private void inactivateSelection(ISubCompoundGraph selection) {
		Iterator<ICompoundNode> nodeIter = selection.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			INodeController nodePrimitive = getNodeController(node);
			nodePrimitive.inactivate();
		}
		Iterator<ICompoundEdge> linkIter = selection.edgeIterator();
		while(linkIter.hasNext()){
			ICompoundEdge link = linkIter.next();
			ILinkController linkController = getLinkController(link);
			linkController.inactivate();
		}
	}

	private void removeNodeSelection(ISubCompoundGraph selection){
		Iterator<ICompoundNode> iter = selection.nodeIterator();
		while(iter.hasNext()){
			ICompoundNode node = iter.next();
			INodeController nodeController = getNodeController(node); 
			notifyRemovedNode(nodeController);
			removeNodeController(nodeController);
		}
	}
	
	private void removeEdgeSelection(ISubCompoundGraph selection){
		Iterator<ICompoundEdge> iter = selection.edgeIterator();
		while(iter.hasNext()){
			ICompoundEdge link = iter.next();
			ILinkController linkController = getLinkController(link); 
			notifyRemovedLink(linkController);
			removeLinkController(linkController);
		}
	}
	
	private void removeLinkController(ILinkController linkController) {
		domainToViewMap.remove(linkController.getDrawingElement());
		drawingPrimitives.remove(linkController);
	}

	private void removeNodeController(INodeController shapeNode){
		domainToViewMap.remove(shapeNode.getDrawingElement());
		drawingPrimitives.remove(shapeNode);
	}

	private void buildFromDomainModel(){
		Iterator<ICompoundNode> nodeIter = this.domainModel.nodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			createNodePrimitive(node);
		}
		Iterator<ICompoundEdge> edgeIter = this.domainModel.edgeIterator();
		while(edgeIter.hasNext()){
			ICompoundEdge link = edgeIter.next();
			createLinkPrimitive(link);
		}
	}
	
	private INodeController createNodePrimitive(ICompoundNode compoundNode){
		INodeController viewNode = null;
		IElementAttribute node = compoundNode.getAttribute();
		if(node instanceof IShapeAttribute){
			viewNode = new ShapeController(this, compoundNode, indexCounter++);
		}
		else if(node instanceof ILabelAttribute){
			viewNode = new LabelController(this, compoundNode, indexCounter++);
		}
		else if(node instanceof IRootAttribute){
			this.rootPrimitive = new RootController(this, (IRootCompoundNode)compoundNode, indexCounter++);
			viewNode = this.rootPrimitive;
		}
		else{
			throw new RuntimeException("node is of unknown type");
		}
		if(viewNode != null){
			this.domainToViewMap.put(compoundNode, viewNode);
			this.drawingPrimitives.add(viewNode);
		}
		return viewNode;
	}
	
	
	private ILinkController createLinkPrimitive(ICompoundEdge linkAtt){
		ILinkController linkCtlr = new LinkController(this, linkAtt, indexCounter++);
		this.domainToViewMap.put(linkAtt, linkCtlr);
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
	public ICompoundGraph getDomainModel() {
		return this.domainModel;
	}

	@Override
	public INodeController getNodeController(ICompoundNode draggedNode) {
		IDrawingElementController retVal = this.domainToViewMap.get(draggedNode);
		if(retVal == null){
			throw new IllegalArgumentException("domain node is not present in this view model");
		}
		return (INodeController)retVal;
	}

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
	public boolean containsDrawingElement(ICompoundGraphElement testPrimitive) {
		return this.domainToViewMap.containsKey(testPrimitive);
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

	@Override
	public ILinkController getLinkController(ICompoundEdge attribute) {
		return (ILinkController)this.domainToViewMap.get(attribute);
	}

	@Override
	public IShapeController getShapeController(ICompoundNode attribute) {
		return (IShapeController)this.domainToViewMap.get(attribute);
	}

	@Override
	public Envelope getCanvasBounds() {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		Iterator<INodeController> nodeIter = this.nodeControllerIterator();
		while(nodeIter.hasNext()){
			INodeController nodeController = nodeIter.next();
			if(!(nodeController instanceof IRootController)){
				// ignore the root as this doesn't have real bounds values - we are only intersted in shapes and labels
				Point nodeOrigin = nodeController.getConvexHull().getEnvelope().getOrigin();
				minX = Math.min(minX, nodeOrigin.getX());
				minY = Math.min(minY, nodeOrigin.getY());
				Point nodeDiagonal = nodeController.getConvexHull().getEnvelope().getDiagonalCorner();
				maxX = Math.max(maxX, nodeDiagonal.getX());
				maxY = Math.max(maxY, nodeDiagonal.getY());
			}
		}
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
		return new Envelope(minX, minY, maxX-minX, maxY-minY);
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
//	public IViewControllerCollection getLastOperationResult() {
//		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException("Not implemented yet!");
//		
//	}

	@Override
	public IDrawingElementController getDrawingPrimitiveController(ICompoundGraphElement testAttribute) {
		return this.domainToViewMap.get(testAttribute);
	}
}
