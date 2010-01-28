package org.pathwayeditor.visualeditor.controller;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelStructureChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ModelStructureChangeType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class ViewControllerStore implements IViewControllerStore {
	private final IModel domainModel;
	private final SortedMap<ICanvasAttribute, IDrawingPrimitiveController> domainToViewMap;
	private final SortedSet<IDrawingPrimitiveController> drawingPrimitives;
	private IModelChangeListener modelListener;
	private IRootController rootPrimitive;
	private boolean isActive = false;
	private final List<IViewControllerChangeListener> listeners;
	
	public ViewControllerStore(IModel domainModel){
		this.domainModel = domainModel;
		this.domainToViewMap = new TreeMap<ICanvasAttribute, IDrawingPrimitiveController>(new Comparator<ICanvasAttribute>(){

			@Override
			public int compare(ICanvasAttribute o1, ICanvasAttribute o2) {
				return Integer.valueOf(o1.getCreationSerial()).compareTo(Integer.valueOf(o2.getCreationSerial()));
			}
			
		});
		this.drawingPrimitives = new TreeSet<IDrawingPrimitiveController>();
		this.listeners = new LinkedList<IViewControllerChangeListener>();
		buildFromDomainModel();
		initialiseDomainListeners();
	}
	
	private void initialiseDomainListeners() {
		modelListener = new IModelChangeListener(){

			@Override
			public void modelStructureChange(IModelStructureChangeEvent event) {
				if(event.getChangeType().equals(ModelStructureChangeType.SELECTION_REMOVED)){
//					rebuildModel();
					removeSelection(event.getOriginalSelection());
				}
				else if(event.getChangeType().equals(ModelStructureChangeType.SELECTION_MOVED)){
					// reinitialise the nodes
					removeSelection(event.getOriginalSelection());
					addSelection(event.getChangedSelection());
				}
				else if(event.getChangeType().equals(ModelStructureChangeType.SELCTION_COPIED)){
					addSelection(event.getChangedSelection());
				}
			}
			
		};
		this.domainModel.addModelChangeListener(modelListener);
		Iterator<IDrawingNode> nodeIter = this.domainModel.drawingNodeIterator();
		while(nodeIter.hasNext()){
			IDrawingNode node = nodeIter.next();
			node.getSubModel().addModelChangeListener(modelListener);
		}
	}
	
	private void addSelection(IDrawingElementSelection selection){
		Iterator<IDrawingNode> iter = selection.drawingNodeIterator();
		while(iter.hasNext()){
			IDrawingNode node = iter.next();
			createNodePrimitive(node.getAttribute());
		}
	}
		
	private void removeSelection(IDrawingElementSelection selection){
		Iterator<IDrawingNode> iter1 = selection.drawingNodeIterator();
		while(iter1.hasNext()){
			IDrawingNode node = iter1.next();
			INodeController nodePrimitive = getNodePrimitive(node.getAttribute());
			nodePrimitive.inactivate();
		}
		Iterator<IDrawingNode> iter = selection.drawingNodeIterator();
		while(iter.hasNext()){
			IDrawingNode node = iter.next();
			removeNodePrimitive(getNodePrimitive(node.getAttribute()));
		}
	}
	
	private void removeNodePrimitive(INodeController shapeNode){
//		INodeController nodePrimitive = (INodeController)domainToViewMap.get(shapeNode.getDrawingElement());
		domainToViewMap.remove(shapeNode.getDrawingElement());
		drawingPrimitives.remove(shapeNode);
		notifyRemovedNode(shapeNode);
		shapeNode.dispose();
	}

	private void buildFromDomainModel(){
		Iterator<IDrawingNode> nodeIter = this.domainModel.drawingNodeIterator();
		while(nodeIter.hasNext()){
			IDrawingNode node = nodeIter.next();
			createNodePrimitive(node.getAttribute());
		}
		Iterator<ILinkEdge> edgeIter = this.domainModel.linkEdgeIterator();
		while(edgeIter.hasNext()){
			ILinkEdge link = edgeIter.next();
			createLinkPrimitive(link.getAttribute());
		}
	}
	
	private INodeController createNodePrimitive(IDrawingNodeAttribute node){
		INodeController viewNode = null;
		if(node instanceof IShapeAttribute){
			viewNode = new ShapeController(this, (IShapeAttribute)node);
		}
		else if(node instanceof ILabelAttribute){
			viewNode = new LabelController(this, (ILabelAttribute)node);
		}
		else if(node instanceof IRootAttribute){
			this.rootPrimitive = new RootController(this, (IRootAttribute)node);
			viewNode = this.rootPrimitive;
		}
		else{
			throw new RuntimeException("node is of unknown type");
		}
		if(viewNode != null){
			this.domainToViewMap.put(node, viewNode);
			this.drawingPrimitives.add(viewNode);
			if(this.isActive()){
				notifyAddedNode(viewNode);
				viewNode.activate();
			}
		}
		return viewNode;
	}
	
	
	private ILinkController createLinkPrimitive(ILinkAttribute linkAtt){
		ILinkController linkCtlr = new LinkController(this, linkAtt);
		this.domainToViewMap.put(linkAtt, linkCtlr);
		this.drawingPrimitives.add(linkCtlr);
		if(this.isActive()){
			linkCtlr.activate();
		}
		return linkCtlr;
	}
	
	
	private void notifyAddedNode(final INodeController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.NODE_ADDED;
			}

			@Override
			public INodeController getChangedNode() {
				return viewNode;
			}
			
		};
		notifyEvent(e);
	}

	private void notifyRemovedNode(final INodeController viewNode) {
		IViewControllerNodeStructureChangeEvent e = new IViewControllerNodeStructureChangeEvent(){

			@Override
			public ViewControllerStructureChangeType getChangeType() {
				return ViewControllerStructureChangeType.NODES_REMOVED;
			}

			@Override
			public INodeController getChangedNode() {
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
	public Iterator<IDrawingPrimitiveController> drawingPrimitiveIterator() {
		return this.drawingPrimitives.iterator();
	}

	@Override
	public IModel getDomainModel() {
		return this.domainModel;
	}

	@Override
	public INodeController getNodePrimitive(IDrawingNodeAttribute draggedNode) {
		IDrawingPrimitiveController retVal = this.domainToViewMap.get(draggedNode);
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
	public Iterator<ILabelController> labelPrimitiveIterator() {
		List<ILabelController> retList = new LinkedList<ILabelController>();
		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
			if(primitive instanceof ILabelController){
				retList.add((ILabelController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<ILinkController> linkPrimitiveIterator() {
		List<ILinkController> retList = new LinkedList<ILinkController>();
		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
			if(primitive instanceof ILinkController){
				retList.add((ILinkController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<INodeController> nodePrimitiveIterator() {
		List<INodeController> retList = new LinkedList<INodeController>();
		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
			if(primitive instanceof INodeController){
				retList.add((INodeController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<IShapeController> shapePrimitiveIterator() {
		List<IShapeController> retList = new LinkedList<IShapeController>();
		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
			if(primitive instanceof IShapeController){
				retList.add((IShapeController)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public boolean containsDrawingElement(ICanvasAttribute testPrimitive) {
		return this.domainToViewMap.containsKey(testPrimitive);
	}

	@Override
	public void activate() {
        // now activate all the drawing primitives
		for(IDrawingPrimitiveController prim : this.drawingPrimitives){
        	prim.activate();
        }
		this.isActive = true;
	}

	@Override
	public void deactivate() {
		for(IDrawingPrimitiveController prim : this.drawingPrimitives){
        	prim.inactivate();
        }
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public ILinkController getLinkController(ILinkAttribute attribute) {
		return (ILinkController)this.domainToViewMap.get(attribute);
	}

	@Override
	public IShapeController getShapeController(IShapeAttribute attribute) {
		return (IShapeController)this.domainToViewMap.get(attribute);
	}

	@Override
	public Envelope getCanvasBounds() {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		Iterator<INodeController> nodeIter = this.nodePrimitiveIterator();
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
		Iterator<ILinkController> edgeIter = this.linkPrimitiveIterator();
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
}
