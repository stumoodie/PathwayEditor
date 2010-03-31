package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

public class SelectionRecord implements ISelectionRecord {
	private SortedSet<ISelection> selections;
	private final List<ISelectionChangeListener> listeners;
	private final IViewControllerStore viewModel;
	private final Map<IDrawingPrimitiveController, ISelection> controllerMapping;
	
	public SelectionRecord(IViewControllerStore viewModel){
		this.selections = new TreeSet<ISelection>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
		this.viewModel = viewModel;
		this.controllerMapping = new HashMap<IDrawingPrimitiveController, ISelection>();
	}
	
	public void addSecondarySelection(IDrawingPrimitiveController drawingElement) {
		if(drawingElement == null || drawingElement instanceof IRootController) throw new IllegalArgumentException("drawing element cannot be null or the root node");
		if(this.selections.isEmpty()) throw new IllegalStateException("Cannot add a secondary selection before a primary selection");

		if(!this.controllerMapping.containsKey(drawingElement)){
			// only do something if selection is not already recorded
			createSelection(SelectionType.SECONDARY, drawingElement);
			notifySelectionChange();
		}
	}
	
	private ISelection createSelection(SelectionType selectionType, IDrawingPrimitiveController drawingElement){
		ISelection retVal = null;
		if(drawingElement instanceof INodeController){
			retVal = new NodeSelection(selectionType, (INodeController)drawingElement);
		}
		else if(drawingElement instanceof ILinkController){
			retVal = new LinkSelection(selectionType, (ILinkController)drawingElement);
		}
		else{
			throw new RuntimeException("Cannot create controller of type: " + retVal);
		}
		this.selections.add(retVal);
		this.controllerMapping.put(drawingElement, retVal);
		return retVal;
	}

	public void addSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void restoreSelection() {
		Iterator<ISelection> selectionIter = this.selections.iterator();
		SortedSet<ISelection> selectedSet = new TreeSet<ISelection>();
		while(selectionIter.hasNext()){
			ISelection selection = selectionIter.next();
			selectedSet.add(selection);
		}
		this.clear();
		for(ISelection selection : selectedSet){
			ICanvasAttribute att = selection.getPrimitiveController().getDrawingElement();
			IDrawingPrimitiveController newController = this.viewModel.getDrawingPrimitiveController(att);
			if(selection.getSelectionType().equals(SelectionType.PRIMARY)){
				this.setPrimarySelection(newController);
			}
			else{
				this.addSecondarySelection(newController);
			}
		}
	}

	public void clear() {
		this.selections.clear();
		this.controllerMapping.clear();
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
		if(drawingElement == null || drawingElement instanceof IRootController) throw new IllegalArgumentException("drawing element cannot be null or the root node");
		
		if(this.selections.isEmpty() || !this.selections.first().equals(drawingElement)){
			// a change in primary selection clears the secondary selection
			this.selections.clear();
			createSelection(SelectionType.PRIMARY, drawingElement);
			notifySelectionChange();
		}
	}
	
	private void updateSubgraphSelection(ISelectionFactory selectionFactory, ISelection newSelection) {
		if(newSelection instanceof INodeSelection){
			INodeSelection nodeSelection = (INodeSelection)newSelection;
			selectionFactory.addDrawingNode(nodeSelection.getPrimitiveController().getDrawingElement().getCurrentDrawingElement());
		}
		else if(newSelection instanceof ILinkSelection){
			ILinkSelection linkSelection = (ILinkSelection)newSelection;
			selectionFactory.addLink(linkSelection.getPrimitiveController().getDrawingElement().getCurrentDrawingElement());
		}
		else{
			throw new RuntimeException("Unknown selection type");
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

	public boolean isNodeSelected(IDrawingPrimitiveController testElement) {
		Iterator<ISelection> iter = this.selections.iterator();
		boolean retVal = false;
		while(!retVal && iter.hasNext()){
			ISelection sel = iter.next();
			retVal = sel.getPrimitiveController().equals(testElement);
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
	public ISubgraphSelection getSubgraphSelection() {
		ISelectionFactory selectionFactory = this.viewModel.getDomainModel().newSelectionFactory();
		for(ISelection selection : this.selections){
			updateSubgraphSelection(selectionFactory, selection);
		}
		IDrawingElementSelection currentSelectionSubgraph = selectionFactory.createEdgeExcludedSelection();
		SubgraphSelection retVal = new SubgraphSelection(this, this.viewModel, currentSelectionSubgraph);
		
		return retVal;
	}

	@Override
	public ILinkSelection getUniqueLinkSelection() {
		ISelection retVal = this.selections.first();
		return (ILinkSelection)retVal;
	}

	@Override
	public ILinkSelection getLinkSelection(ILinkController next) {
		return (ILinkSelection)this.controllerMapping.get(next);
	}

	@Override
	public INodeSelection getNodeSelection(INodeController next) {
		return (INodeSelection)this.controllerMapping.get(next);
	}

	@Override
	public ISelection getSelection(IDrawingPrimitiveController next) {
		return this.controllerMapping.get(next);
	}

	@Override
	public boolean containsSelection(IDrawingPrimitiveController controller) {
		return this.controllerMapping.containsKey(controller);
	}

}
