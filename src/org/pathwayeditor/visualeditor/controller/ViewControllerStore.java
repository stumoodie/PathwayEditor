package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelEdgeChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IModelNodeChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ModelStructureChangeType;

public class ViewControllerStore implements IViewControllerStore {
	private final IModel domainModel;
	private final SortedMap<IDrawingElement, IDrawingPrimitiveController> domainToViewMap;
	private final SortedSet<IDrawingPrimitiveController> drawingPrimitives;
	private IModelChangeListener modelListener;
	private IRootController rootPrimitive;
	
	public ViewControllerStore(IModel domainModel){
		this.domainModel = domainModel;
		this.domainToViewMap = new TreeMap<IDrawingElement, IDrawingPrimitiveController>();
		this.drawingPrimitives = new TreeSet<IDrawingPrimitiveController>();
		buildFromDomainModel();
		initialiseDomainListeners();
	}
	
	private void initialiseDomainListeners() {
		modelListener = new IModelChangeListener(){

			public void edgeStructureChange(IModelEdgeChangeEvent event) {
				// do nothing
			}

			public void nodeStructureChange(IModelNodeChangeEvent event) {
				if(event.getChangeType().equals(ModelStructureChangeType.DELETED)){
					if(domainToViewMap.containsKey(event.getChangedItem())){
						IDrawingNode shapeNode = event.getChangedItem();
						INodeController nodePrimitive = (INodeController)domainToViewMap.get(shapeNode);
						nodePrimitive.dispose();
						domainToViewMap.remove(shapeNode);
						drawingPrimitives.remove(nodePrimitive);
					}
				}
				else if(event.getChangeType().equals(ModelStructureChangeType.ADDED)){
					IDrawingNode shapeNode = event.getChangedItem();
					createNodePrimitive(shapeNode);
				}
			}
			
		};
		this.domainModel.addModelChangeListener(modelListener);
	}

	private void buildFromDomainModel(){
		Iterator<IDrawingNode> nodeIter = this.domainModel.drawingNodeIterator();
		while(nodeIter.hasNext()){
			IDrawingNode node = nodeIter.next();
			createNodePrimitive(node);
		}
	}
	
	private void createNodePrimitive(IDrawingNode node){
		INodeController viewNode = null;
		if(node instanceof IShapeNode){
			viewNode = new ShapeController(this, (IShapeNode)node);
		}
		else if(node instanceof ILabelNode){
			viewNode = new LabelController(this, (ILabelNode)node);
		}
		else if(node instanceof IRootNode){
			this.rootPrimitive = new RootController(this, (IRootNode)node);
			viewNode = this.rootPrimitive;
		}
		else{
			throw new RuntimeException("node is of unknown type");
		}
		if(viewNode != null){
			this.domainToViewMap.put(node, viewNode);
			this.drawingPrimitives.add(viewNode);
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
	public INodeController getNodePrimitive(IDrawingNode draggedNode) {
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

//	@Override
//	public void synchroniseWithDomainModel() {
//		for(IDrawingPrimitiveController primitive : this.drawingPrimitives){
//			primitive.resyncToModel();
//		}
//	}

	@Override
	public boolean containsDrawingElement(IDrawingElement testPrimitive) {
		return this.domainToViewMap.containsKey(testPrimitive);
	}

	@Override
	public void activate() {
        // now activate all the drawing primitives
		for(IDrawingPrimitiveController prim : this.drawingPrimitives){
        	prim.activate();
        }
	}

}
