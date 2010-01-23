package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

public class SelectionRecord implements ISelectionRecord {
	private SortedSet<ISelection> selections;
	private final List<ISelectionChangeListener> listeners;
	private IDrawingElementSelection selection;
	private ISelectionFactory selectionFactory;
	
	public SelectionRecord(){
		this.selections = new TreeSet<ISelection>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
	}
	
	public void addSecondarySelection(IDrawingPrimitiveController drawingElement) {
		if(this.selections.isEmpty()) throw new IllegalStateException("Cannot add a secondary selection before a primary selection");

		ISelection newSelection = createSelection(false, drawingElement);
		if(this.selections.add(newSelection)){
			// only do something if selection is not already recorded
			addElementToGraphSelection(drawingElement);
			notifySelectionChange();
		}
	}
	
	private ISelection createSelection(boolean isPrimtive, IDrawingPrimitiveController drawingElement){
		ISelection retVal = null;
		if(drawingElement instanceof INodeController){
			retVal = new NodeSelection(isPrimtive, (INodeController)drawingElement);
		}
		else if(drawingElement instanceof ILinkController){
			retVal = new LinkSelection(isPrimtive, (ILinkController)drawingElement);
		}
		else{
			throw new RuntimeException("Cannot create controller of type: " + retVal);
		}
		return retVal;
	}

	public void addSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.add(listener);
	}

	public void clear() {
		this.selections.clear();
		this.selectionFactory = null;
		this.selection = null;
		notifySelectionChange();
	}

	public ISelection getPrimarySelection() {
		return this.selections.first();
	}

	public List<ISelectionChangeListener> getSelectionChangeListeners() {
		return new ArrayList<ISelectionChangeListener>(this.listeners);
	}

	public int numSelected() {
		return this.selections.size();
	}

	public void removeSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.remove(listeners);
	}

	public Iterator<ISelection> secondarySelectionIterator() {
		Iterator<ISelection> retVal = this.selections.iterator();
		// skip primary selection - if there is one
		if(retVal.hasNext()) retVal.next();
		return retVal;
	}

	public Iterator<ISelection> selectionIterator() {
		return this.selections.iterator();
	}

	public void setPrimarySelection(IDrawingPrimitiveController drawingElement) {
		if(this.selections.isEmpty() || !this.selections.first().equals(drawingElement)){
			// a change in primary selection clears the secondary selection
			this.selections.clear();
			ISelection primSel = createSelection(true, drawingElement);
			this.selections.add(primSel);
			this.selectionFactory = drawingElement.getDrawingElement().getModel().newSelectionFactory();
			this.selection = null;
			addElementToGraphSelection(drawingElement);
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
		Iterator<ISelection> iter = this.selections.iterator();
		boolean retVal = false;
		while(!retVal && iter.hasNext()){
			ISelection sel = iter.next();
			retVal = sel.getPrimitiveController().equals(testElement);
		}
		return retVal;
	}

	private ISelection findSelection(IDrawingPrimitiveController testElement) {
		Iterator<ISelection> iter = this.selections.iterator();
		ISelection retVal = null;
		while(retVal == null && iter.hasNext()){
			ISelection sel = iter.next();
			if(sel.getPrimitiveController().equals(testElement)){
				retVal = sel;
			}
		}
		return retVal;
	}

	@Override
	public Iterator<ILinkSelection> selectedLinksIterator() {
		List<ILinkSelection> retVal = new LinkedList<ILinkSelection>();
		Iterator<ISelection> iter = this.selectionIterator();
		while(iter.hasNext()){
			ISelection prim = iter.next();
			if(prim instanceof ILinkSelection){
				retVal.add((ILinkSelection)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodeSelection> selectedNodesIterator() {
		List<INodeSelection> retVal = new LinkedList<INodeSelection>();
		Iterator<ISelection> iter = this.selectionIterator();
		while(iter.hasNext()){
			ISelection prim = iter.next();
			if(prim instanceof INodeSelection){
				retVal.add((INodeSelection)prim);
			}
		}
		return retVal.iterator();
	}

	@Override
	public Iterator<INodeSelection> getTopNodeSelection() {
		// ensure that this graph selection is initialised
		getGraphSelection();
		final IViewControllerStore viewModel = this.selections.first().getPrimitiveController().getViewModel();
		final Iterator<IDrawingNode> iter = this.selection.topDrawingNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				return (INodeSelection)findSelection(viewModel.getNodePrimitive(iter.next()));
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		SortedSet<ISelectionHandle> matches = new TreeSet<ISelectionHandle>();
		for(ISelection selection : this.selections){
			ISelectionHandle model = selection.findSelectionModelAt(point); 
			if(model != null){
				matches.add(model);
			}
		}
		ISelectionHandle retVal = null;
		if(!matches.isEmpty()){
			retVal = matches.first();
		}
		return retVal;
	}

	@Override
	public IDrawingElementSelection getGraphSelection() {
		if(this.selection == null){
			this.selection = this.selectionFactory.createEdgeExcludedSelection();
		}
		return this.selection;
	}

}
