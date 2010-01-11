package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

public class SelectionRecord implements ISelectionRecord {
	private Set<IDrawingPrimitiveController> secondarySelections;
	private IDrawingPrimitiveController primarySelection;
	private final List<ISelectionChangeListener> listeners;
	private IDrawingElementSelection selection;
	private ISelectionFactory selectionFactory;
	
	public SelectionRecord(){
		this.primarySelection = null;
		this.secondarySelections = new TreeSet<IDrawingPrimitiveController>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
	}
	
	public void addSecondarySelection(IDrawingPrimitiveController drawingElement) {
		if(this.primarySelection == null) throw new IllegalStateException("Cannot add a secondary selection before a primary selection");

		if(this.secondarySelections.add(drawingElement)){
			// only do something if selection is not already recorded
			addElementToGraphSelection(drawingElement);
			notifySelectionChange();
		}
	}

	public void addSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.add(listener);
	}

	public void clear() {
		this.secondarySelections.clear();
		this.primarySelection = null;
		this.selectionFactory = null;
		this.selection = null;
		notifySelectionChange();
	}

	public IDrawingPrimitiveController getPrimarySelection() {
		return this.primarySelection;
	}

	public List<ISelectionChangeListener> getSelectionChangeListeners() {
		return new ArrayList<ISelectionChangeListener>(this.listeners);
	}

	public int numSelected() {
		int retVal = 0;
		if(this.primarySelection != null){
			retVal = this.secondarySelections.size() + 1;
		}
		return retVal;
	}

	public void removeSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.remove(listeners);
	}

	public Iterator<IDrawingPrimitiveController> secondarySelectionIterator() {
		return this.secondarySelections.iterator();
	}

	public Iterator<IDrawingPrimitiveController> selectionIterator() {
		LinkedList<IDrawingPrimitiveController> retVal = new LinkedList<IDrawingPrimitiveController>(this.secondarySelections);
		if(this.primarySelection != null){
			retVal.addFirst(this.primarySelection);
		}
		return retVal.iterator();
	}

	public void setPrimarySelection(IDrawingPrimitiveController drawingElement) {
		if(this.primarySelection == null || !this.primarySelection.equals(drawingElement)){
			// a change in primary selection clears the secondary selection
			this.primarySelection = drawingElement;
			this.selectionFactory = drawingElement.getDrawingElement().getModel().newSelectionFactory();
			this.selection = null;
			addElementToGraphSelection(drawingElement);
			this.secondarySelections.clear();
			notifySelectionChange();
		}
	}
	
	
	private void addElementToGraphSelection(IDrawingPrimitiveController drawingElement) {
		if(drawingElement.getDrawingElement() instanceof IDrawingNode){
			this.selectionFactory.addDrawingNode((IDrawingNode)drawingElement.getDrawingElement());
		}
		else if(drawingElement.getDrawingElement() instanceof ILinkEdge){
			this.selectionFactory.addLink((ILinkEdge)drawingElement.getDrawingElement());
		}
		else{
			throw new RuntimeException("Cannot handle drawing element: " + drawingElement);
		}
		this.selection = null;
	}

	private void notifySelectionChange(){
		ISelectionChangeEvent event = new ISelectionChangeEvent(){

			public ISelectionRecord getSelectionRecord() {
				return SelectionRecord.this;
			}
			
		};
		fireEvent(event);
	}
	
	private void fireEvent(ISelectionChangeEvent event){
		for(ISelectionChangeListener listener : this.listeners){
			listener.selectionChanged(event);
		}
	}

	public boolean isNodeSelected(IDrawingPrimitiveController testElement) {
		return this.primarySelection != null && testElement != null
			&& (this.primarySelection.equals(testElement) || this.secondarySelections.contains(testElement));
	}

	@Override
	public Iterator<ILinkController> selectedLinksIterator() {
		List<ILinkController> retVal = new LinkedList<ILinkController>();
		Iterator<IDrawingPrimitiveController> iter = this.selectionIterator();
		while(iter.hasNext()){
			IDrawingPrimitiveController prim = iter.next();
			if(prim instanceof ILinkController){
				retVal.add((ILinkController)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodeController> selectedNodesIterator() {
		List<INodeController> retVal = new LinkedList<INodeController>();
		Iterator<IDrawingPrimitiveController> iter = this.selectionIterator();
		while(iter.hasNext()){
			IDrawingPrimitiveController prim = iter.next();
			if(prim instanceof INodeController){
				retVal.add((INodeController)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodeController> getTopNodeSelection() {
		if(this.selection == null){
			this.selection = this.selectionFactory.createEdgeExcludedSelection();
		}
		final IViewControllerStore viewModel = this.primarySelection.getViewModel();
		final Iterator<IDrawingNode> iter = this.selection.topDrawingNodeIterator();
		Iterator<INodeController> retVal = new Iterator<INodeController>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeController next() {
				return viewModel.getNodePrimitive(iter.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

}
