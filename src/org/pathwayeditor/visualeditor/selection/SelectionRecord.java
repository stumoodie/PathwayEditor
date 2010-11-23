package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.businessobjects.impl.facades.SelectionFactoryFacade;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

public class SelectionRecord implements ISelectionRecord {
	private final SortedSet<ISelection> selections;
	private final List<ISelectionChangeListener> listeners;
	private final IViewControllerModel viewModel;
	private final Map<IDrawingElementController, ISelection> controllerMapping;
	private EnvelopeBuilder builder;
	
	public SelectionRecord(IViewControllerModel viewModel){
		this.selections = new TreeSet<ISelection>();
		this.listeners = new LinkedList<ISelectionChangeListener>();
		this.viewModel = viewModel;
		this.controllerMapping = new HashMap<IDrawingElementController, ISelection>();
	}
	
	@Override
	public void addSecondarySelection(IDrawingElementController drawingElement) {
		if(drawingElement == null || drawingElement instanceof IRootController) throw new IllegalArgumentException("drawing element cannot be null or the root node");
		if(this.selections.isEmpty()) throw new IllegalStateException("Cannot add a secondary selection before a primary selection");

		if(!this.controllerMapping.containsKey(drawingElement)){
			// only do something if selection is not already recorded
			List<ISelection> oldSelection = new ArrayList<ISelection>(this.selections);
			ISelection seln = createSelection(SelectionType.SECONDARY, drawingElement);
			this.builder.union(seln.getPrimitiveController().getDrawnBounds());
			notifySelectionChange(oldSelection.iterator(), this.selections.iterator());
		}
	}
	
	private ISelection createSelection(SelectionType selectionType, IDrawingElementController drawingElement){
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

	@Override
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
			IDrawingElement att = selection.getPrimitiveController().getDrawingElement();
			IDrawingElementController newController = this.viewModel.getDrawingPrimitiveController(att);
			if(selection.getSelectionType().equals(SelectionType.PRIMARY)){
				this.setPrimarySelection(newController);
			}
			else{
				this.addSecondarySelection(newController);
			}
		}
	}

	@Override
	public void clear() {
		List<ISelection> oldSelection = new ArrayList<ISelection>(this.selections);
		this.selections.clear();
		this.controllerMapping.clear();
		this.builder = null;
		notifySelectionChange(oldSelection.iterator(), this.selections.iterator());
	}

	@Override
	public ISelection getPrimarySelection() {
		return this.selections.first();
	}

	@Override
	public List<ISelectionChangeListener> getSelectionChangeListeners() {
		return new ArrayList<ISelectionChangeListener>(this.listeners);
	}

	@Override
	public int numSelected() {
		return this.selections.size();
	}

	@Override
	public void removeSelectionChangeListener(ISelectionChangeListener listener) {
		this.listeners.remove(listeners);
	}

	@Override
	public Iterator<ISelection> secondarySelectionIterator() {
		Iterator<ISelection> retVal = this.selections.iterator();
		// skip primary selection - if there is one
		if(retVal.hasNext()) retVal.next();
		return retVal;
	}

	@Override
	public Iterator<ISelection> selectionIterator() {
		return this.selections.iterator();
	}

	@Override
	public void setPrimarySelection(IDrawingElementController drawingElement) {
		if(drawingElement == null || drawingElement instanceof IRootController) throw new IllegalArgumentException("drawing element cannot be null or the root node");
		
		if(this.selections.isEmpty() || !this.selections.first().equals(drawingElement)){
			// a change in primary selection clears the secondary selection
			List<ISelection> oldSelection = new ArrayList<ISelection>(this.selections);
			this.selections.clear();
			ISelection newSeln = createSelection(SelectionType.PRIMARY, drawingElement);
			this.builder = new EnvelopeBuilder(newSeln.getPrimitiveController().getDrawnBounds());
			notifySelectionChange(oldSelection.iterator(), this.selections.iterator());
		}
	}
	
	private void updateSubgraphSelection(ISelectionFactory selectionFactory, ISelection newSelection) {
		if(newSelection instanceof INodeSelection){
			INodeSelection nodeSelection = (INodeSelection)newSelection;
			selectionFactory.addDrawingNode(nodeSelection.getPrimitiveController().getDrawingElement());
		}
		else if(newSelection instanceof ILinkSelection){
			ILinkSelection linkSelection = (ILinkSelection)newSelection;
			selectionFactory.addLink(linkSelection.getPrimitiveController().getDrawingElement());
		}
		else{
			throw new RuntimeException("Unknown selection type");
		}
	}

	private void notifySelectionChange(final Iterator<ISelection> oldSelectionIter, final Iterator<ISelection> newSelectionIter){
		ISelectionChangeEvent event = new ISelectionChangeEvent(){

			@Override
			public ISelectionRecord getSelectionRecord() {
				return SelectionRecord.this;
			}

			@Override
			public Iterator<ISelection> newSelectionIter() {
				return newSelectionIter;
			}

			@Override
			public Iterator<ISelection> oldSelectionIter() {
				return oldSelectionIter;
			}
			
		};
		fireEvent(event);
	}
	
	private void fireEvent(ISelectionChangeEvent event){
		for(ISelectionChangeListener listener : this.listeners){
			listener.selectionChanged(event);
		}
	}

	@Override
	public boolean isNodeSelected(IDrawingElementController testElement) {
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
		ISelectionFactory selectionFactory = new SelectionFactoryFacade(this.viewModel.getDomainModel().getGraph().subgraphFactory());
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
	public ISelection getSelection(IDrawingElementController next) {
		return this.controllerMapping.get(next);
	}

	@Override
	public boolean containsSelection(IDrawingElementController controller) {
		return this.controllerMapping.containsKey(controller);
	}

	@Override
	public Envelope getTotalSelectionBounds() {
		Envelope retVal = Envelope.NULL_ENVELOPE;
		if(this.builder != null){
			retVal = this.builder.getEnvelope();
		}
		return retVal;
//		double minX = Double.MAX_VALUE;
//		double maxX = Double.MIN_VALUE;
//		double minY = Double.MAX_VALUE;
//		double maxY = Double.MIN_VALUE;
//		for(ISelection sel : this.selections){
//			Point o = sel.getPrimitiveController().getDrawnBounds().getOrigin();
//			Point d = sel.getPrimitiveController().getDrawnBounds().getDiagonalCorner();
//			minX = Math.min(minX, o.getX());
//			maxX = Math.max(maxX, o.getX());
//			minY = Math.min(minY, o.getY());
//			maxY = Math.max(maxY, o.getY());
//			minX = Math.min(minX, d.getX());
//			maxX = Math.max(maxX, d.getX());
//			minY = Math.min(minY, d.getY());
//			maxY = Math.max(maxY, d.getY());
//		}
//		return new Envelope(minX, minY, maxX-minX, maxY-minY);
	}

}
