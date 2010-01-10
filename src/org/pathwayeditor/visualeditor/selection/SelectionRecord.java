package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pathwayeditor.visualeditor.IDrawingPrimitive;
import org.pathwayeditor.visualeditor.ILinkPrimitive;
import org.pathwayeditor.visualeditor.INodePrimitive;

public class SelectionRecord implements ISelectionRecord {
	private Set<IDrawingPrimitive> secondarySelections;
	private IDrawingPrimitive primarySelection;
	private final List<ISelectionChangeListener> listeners;
	
	public SelectionRecord(){
		this.primarySelection = null;
		this.secondarySelections = new TreeSet<IDrawingPrimitive>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
	}
	
	public void addSecondarySelection(IDrawingPrimitive drawingElement) {
		if(this.primarySelection == null) throw new IllegalStateException("Cannot add a secondary selection before a primary selection");

		if(this.secondarySelections.add(drawingElement)){
			// only notify if selection is not already recorded
			notifySelectionChange();
		}
	}

	public void addSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.add(listener);
	}

	public void clear() {
		this.secondarySelections.clear();
		this.primarySelection = null;
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
			this.secondarySelections.clear();
			notifySelectionChange();
		}
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

}
