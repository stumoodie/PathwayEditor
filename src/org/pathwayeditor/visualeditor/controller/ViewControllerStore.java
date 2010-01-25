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
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelStructureChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ModelStructureChangeType;

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
//					rebuildModel();
					// reinitialise the nodes
//					Iterator<IDrawingNode> nodeIter = event.getChangedSelection().drawingNodeIterator();
//					while(nodeIter.hasNext()){
//						IDrawingNodeAttribute att = nodeIter.next().getAttribute();
//						IDrawingPrimitiveController nodeCont = domainToViewMap.get(att);
//						nodeCont.inactivate();
//						nodeCont.activate();
//					}
					removeSelection(event.getOriginalSelection());
					addSelection(event.getChangedSelection());
				}
				else if(event.getChangeType().equals(ModelStructureChangeType.SELCTION_COPIED)){
//					rebuildModel();
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
	
//	private void rebuildModel(){
//		for(IDrawingPrimitiveController controller : this.drawingPrimitives){
//			controller.dispose();
//		}
//		this.domainToViewMap.clear();
//		this.drawingPrimitives.clear();
//		buildFromDomainModel();
//	}
	
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
		INodeController nodePrimitive = (INodeController)domainToViewMap.get(shapeNode.getDrawingElement());
		domainToViewMap.remove(shapeNode.getDrawingElement());
		drawingPrimitives.remove(nodePrimitive);
		notifyRemovedNode(shapeNode);
		nodePrimitive.dispose();
	}

	private void buildFromDomainModel(){
		Iterator<IDrawingNode> nodeIter = this.domainModel.drawingNodeIterator();
		while(nodeIter.hasNext()){
			IDrawingNode node = nodeIter.next();
			createNodePrimitive(node.getAttribute());
		}
	}
	
	private void createNodePrimitive(IDrawingNodeAttribute node){
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

//	@Override
//	public void synchroniseWithDomainModel() {
//		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
//			primitive.resyncToModel();
//		}
//	}

//	@Override
//	public void addViewControllerChangeListener(IViewControllerChangeListener listener) {
//		this.listeners.add(listener);
//	}

	@Override
	public void deactivate() {
		for(IDrawingPrimitiveController prim : this.drawingPrimitives){
        	prim.inactivate();
        }
		this.isActive = false;
	}

//	@Override
//	public List<IViewControllerChangeListener> getViewControllerChangeListeners() {
//		return new ArrayList<IViewControllerChangeListener>(this.listeners);
//	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

//	@Override
//	public void removeViewControllerChangeListener(IViewControllerChangeListener listener) {
//		this.listeners.remove(listener);
//	}
}
