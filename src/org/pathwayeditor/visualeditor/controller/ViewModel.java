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

public class ViewModel implements IViewModel {
	private final IModel domainModel;
	private final SortedMap<IDrawingElement, IDrawingPrimitive> domainToViewMap;
	private final SortedSet<IDrawingPrimitive> drawingPrimitives;
	private IModelChangeListener modelListener;
	private IRootPrimitive rootPrimitive;
	
	public ViewModel(IModel domainModel){
		this.domainModel = domainModel;
		this.domainToViewMap = new TreeMap<IDrawingElement, IDrawingPrimitive>();
		this.drawingPrimitives = new TreeSet<IDrawingPrimitive>();
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
						INodePrimitive nodePrimitive = (INodePrimitive)domainToViewMap.get(shapeNode);
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
		INodePrimitive viewNode = null;
		if(node instanceof IShapeNode){
			viewNode = new ShapePrimitive(this, (IShapeNode)node);
		}
		else if(node instanceof ILabelNode){
			viewNode = new LabelPrimitive(this, (ILabelNode)node);
		}
		else if(node instanceof IRootNode){
			this.rootPrimitive = new RootPrimitive(this, (IRootNode)node);
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
	public Iterator<IDrawingPrimitive> drawingPrimitiveIterator() {
		return this.drawingPrimitives.iterator();
	}

	@Override
	public IModel getDomainModel() {
		return this.domainModel;
	}

	@Override
	public INodePrimitive getNodePrimitive(IDrawingNode draggedNode) {
		IDrawingPrimitive retVal = this.domainToViewMap.get(draggedNode);
		if(retVal == null){
			throw new IllegalArgumentException("domain node is not present in this view model");
		}
		return (INodePrimitive)retVal;
	}

	@Override
	public IRootPrimitive getRootNode() {
		return this.rootPrimitive;
	}

	@Override
	public Iterator<ILabelPrimitive> labelPrimitiveIterator() {
		List<ILabelPrimitive> retList = new LinkedList<ILabelPrimitive>();
		for(IDrawingPrimitive primitive : this.drawingPrimitives){
			if(primitive instanceof ILabelPrimitive){
				retList.add((ILabelPrimitive)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<ILinkPrimitive> linkPrimitiveIterator() {
		List<ILinkPrimitive> retList = new LinkedList<ILinkPrimitive>();
		for(IDrawingPrimitive primitive : this.drawingPrimitives){
			if(primitive instanceof ILinkPrimitive){
				retList.add((ILinkPrimitive)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<INodePrimitive> nodePrimitiveIterator() {
		List<INodePrimitive> retList = new LinkedList<INodePrimitive>();
		for(IDrawingPrimitive primitive : this.drawingPrimitives){
			if(primitive instanceof INodePrimitive){
				retList.add((INodePrimitive)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public Iterator<IShapePrimitive> shapePrimitiveIterator() {
		List<IShapePrimitive> retList = new LinkedList<IShapePrimitive>();
		for(IDrawingPrimitive primitive : this.drawingPrimitives){
			if(primitive instanceof IShapePrimitive){
				retList.add((IShapePrimitive)primitive);
			}
		}
		return retList.iterator();
	}

	@Override
	public void synchroniseWithDomainModel() {
		for(IDrawingPrimitive primitive : this.drawingPrimitives){
			primitive.resyncToModel();
		}
	}

	@Override
	public boolean containsDrawingElement(IDrawingElement testPrimitive) {
		return this.domainToViewMap.containsKey(testPrimitive);
	}

	@Override
	public void activate() {
        // now activate all the drawing primitives
		for(IDrawingPrimitive prim : this.drawingPrimitives){
        	prim.activate();
        }
	}

}
