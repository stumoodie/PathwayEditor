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
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitive;
import org.pathwayeditor.visualeditor.controller.ILinkPrimitive;
import org.pathwayeditor.visualeditor.controller.INodePrimitive;
import org.pathwayeditor.visualeditor.controller.IViewModel;

public class SelectionRecord implements ISelectionRecord {
	private Set<IDrawingPrimitive> secondarySelections;
	private IDrawingPrimitive primarySelection;
	private final List<ISelectionChangeListener> listeners;
	private IDrawingElementSelection selection;
	private ISelectionFactory selectionFactory;
	
	public SelectionRecord(){
		this.primarySelection = null;
		this.secondarySelections = new TreeSet<IDrawingPrimitive>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
	}
	
	public void addSecondarySelection(IDrawingPrimitive drawingElement) {
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

	public IDrawingPrimitive getPrimarySelection() {
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

	public Iterator<IDrawingPrimitive> secondarySelectionIterator() {
		return this.secondarySelections.iterator();
	}

	public Iterator<IDrawingPrimitive> selectionIterator() {
		LinkedList<IDrawingPrimitive> retVal = new LinkedList<IDrawingPrimitive>(this.secondarySelections);
		if(this.primarySelection != null){
			retVal.addFirst(this.primarySelection);
		}
		return retVal.iterator();
	}

	public void setPrimarySelection(IDrawingPrimitive drawingElement) {
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
	
	
	private void addElementToGraphSelection(IDrawingPrimitive drawingElement) {
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

	public boolean isNodeSelected(IDrawingPrimitive testElement) {
		return this.primarySelection != null && testElement != null
			&& (this.primarySelection.equals(testElement) || this.secondarySelections.contains(testElement));
	}

	@Override
	public Iterator<ILinkPrimitive> selectedLinksIterator() {
		List<ILinkPrimitive> retVal = new LinkedList<ILinkPrimitive>();
		Iterator<IDrawingPrimitive> iter = this.selectionIterator();
		while(iter.hasNext()){
			IDrawingPrimitive prim = iter.next();
			if(prim instanceof ILinkPrimitive){
				retVal.add((ILinkPrimitive)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodePrimitive> selectedNodesIterator() {
		List<INodePrimitive> retVal = new LinkedList<INodePrimitive>();
		Iterator<IDrawingPrimitive> iter = this.selectionIterator();
		while(iter.hasNext()){
			IDrawingPrimitive prim = iter.next();
			if(prim instanceof INodePrimitive){
				retVal.add((INodePrimitive)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodePrimitive> getTopNodeSelection() {
		if(this.selection == null){
			this.selection = this.selectionFactory.createEdgeExcludedSelection();
		}
		final IViewModel viewModel = this.primarySelection.getViewModel();
		final Iterator<IDrawingNode> iter = this.selection.topDrawingNodeIterator();
		Iterator<INodePrimitive> retVal = new Iterator<INodePrimitive>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodePrimitive next() {
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
