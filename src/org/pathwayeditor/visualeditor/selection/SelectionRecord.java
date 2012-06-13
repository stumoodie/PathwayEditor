/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraphFactory;

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
			notifySelectionChange(SelectionChangeType.SECONDARY_SELCTION_CHANGED, oldSelection.iterator(), this.selections.iterator());
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
			ICanvasElementAttribute att = selection.getPrimitiveController().getAssociatedAttribute();
			IDrawingElementController newController = this.viewModel.findControllerByAttribute(att);
			if(selection.getSelectionType().equals(SelectionType.PRIMARY)){
				this.setPrimarySelection(newController);
			}
			else{
				this.addSecondarySelection(newController);
			}
		}
	}
	
	private void reset(){
		this.selections.clear();
		this.controllerMapping.clear();
		this.builder = null;
	}

	@Override
	public void clear() {
		List<ISelection> oldSelection = new ArrayList<ISelection>(this.selections);
		reset();
		notifySelectionChange(SelectionChangeType.SELECTION_CLEARED, oldSelection.iterator(), this.selections.iterator());
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
			notifySelectionChange(SelectionChangeType.PRIMARY_SELECTION_CHANGED, oldSelection.iterator(), this.selections.iterator());
		}
	}
	
	private void updateSubgraphSelection(ISubCompoundGraphFactory selectionFactory, ISelection newSelection) {
		selectionFactory.addElement(newSelection.getPrimitiveController().getAssociatedAttribute().getCurrentElement());
//		if(newSelection instanceof INodeSelection){
//			INodeSelection nodeSelection = (INodeSelection)newSelection;
//			selectionFactory.addElement(nodeSelection.getPrimitiveController().getAssociatedAttribute().getCurrentElement());
//		}
//		else if(newSelection instanceof ILinkSelection){
//			ILinkSelection linkSelection = (ILinkSelection)newSelection;
//			selectionFactory.addElement(linkSelection.getPrimitiveController().getDrawingElement());
//		}
//		else{
//			throw new RuntimeException("Unknown selection type");
//		}
	}

	private void notifySelectionChange(final SelectionChangeType type, final Iterator<ISelection> oldSelectionIter, final Iterator<ISelection> newSelectionIter){
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

			@Override
			public SelectionChangeType getSelectionChange() {
				return type;
			}

			@Override
			public ISelection getPrimarySelection() {
				ISelection retVal = null;
				if(!selections.isEmpty()){
					retVal = selections.first();
				}
				return retVal;
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
	public Iterator<ILinkSelection> selectedLinkIterator() {
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
	public Iterator<INodeSelection> selectedNodeIterator() {
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
		ISubCompoundGraphFactory selectionFactory = this.viewModel.getDomainModel().getGraph().subgraphFactory();
//		ISelectionFactory selectionFactory = new SelectionFactoryFacade(this.viewModel.getDomainModel().getGraph().subgraphFactory());
		for(ISelection selection : this.selections){
			updateSubgraphSelection(selectionFactory, selection);
		}
		ISubCompoundGraph currentSelectionSubgraph = selectionFactory.createInducedSubgraph();
//		IDrawingElementSelection currentSelectionSubgraph = selectionFactory.createEdgeExcludedSelection();
		SubgraphSelection retVal = new SubgraphSelection(this, this.viewModel, currentSelectionSubgraph);
		
		return retVal;
	}

	@Override
	public ISubgraphSelection getEdgeIncludedSelection() {
		ISubCompoundGraphFactory selectionFactory = this.viewModel.getDomainModel().getGraph().subgraphFactory();
		for(ISelection selection : this.selections){
			updateSubgraphSelection(selectionFactory, selection);
		}
		ISubCompoundGraph currentSelectionSubgraph = selectionFactory.createPermissiveInducedSubgraph();
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
	}

	@Override
	public void selectAll() {
		reset();
		Iterator<IDrawingElementController> primIter = this.viewModel.drawingPrimitiveIterator();
		boolean firstTime = true;
		while(primIter.hasNext()){
			IDrawingElementController controller = primIter.next();
			if(!(controller instanceof IRootController)){
				if(firstTime){
					this.setPrimarySelection(controller);
					firstTime = false;
				}
				else{
					this.addSecondarySelection(controller);
				}
			}
		}
	}

}
