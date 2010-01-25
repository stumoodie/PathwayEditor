package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeBoundsChangeEvent;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.INodeControllerChangeListener;
import org.pathwayeditor.visualeditor.controller.INodeResizeEvent;
import org.pathwayeditor.visualeditor.controller.INodeTranslationEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class NodeSelection extends Selection implements INodeSelection {
	private static final int NUM_REGIONS = 9;
	private INodeControllerChangeListener nodeControllerListener;
	private final INodeController nodeController;
	private final List<ISelectionHandle> selectionModels;
	private Envelope selectionBounds;

	public NodeSelection(boolean primaryFlag, INodeController nodeController){
		super(primaryFlag);
		this.nodeController = nodeController;
		this.selectionModels = new ArrayList<ISelectionHandle>(NUM_REGIONS);
		buildHandles();
		this.nodeControllerListener = new INodeControllerChangeListener(){

			@Override
			public void nodeTranslated(INodeTranslationEvent e) {
				buildHandles();
			}

			@Override
			public void nodeResized(INodeResizeEvent e) {
				buildHandles();
			}

			@Override
			public void changedBounds(INodeBoundsChangeEvent e) {
				buildHandles();
			}
			
		};
		this.nodeController.addNodePrimitiveChangeListener(this.nodeControllerListener);
	}
	
	@Override
	public INodeController getPrimitiveController() {
		return this.nodeController;
	}

	@Override
	public ISelectionHandle getSelectionModel(SelectionRegion region) {
		ISelectionHandle retVal = null;
		Iterator<ISelectionHandle> iter = this.selectionModels.iterator();
		while(iter.hasNext() && retVal == null){
			ISelectionHandle curr = iter.next();
			if(curr.getRegion().equals(region)){
				retVal = curr;
			}
		}
		return retVal;
	}

	private void buildHandles() {
		this.selectionModels.clear();
		this.selectionModels.add(SelectionHandle.createCentralRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSWRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createWRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNWRegion(this, nodeController));
		calcSelectionBounds();
	}

	private void calcSelectionBounds() {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for(ISelectionHandle model : this.selectionModels){
			Envelope bounds = model.getBounds();
			minX = Math.min(minX, bounds.getOrigin().getX());
			minY = Math.min(minY, bounds.getOrigin().getY());
			maxX = Math.max(maxX, bounds.getDiagonalCorner().getX());
			maxY = Math.max(maxY, bounds.getDiagonalCorner().getY());
		}
		this.selectionBounds = new Envelope(minX, minY, maxX-minX, maxY-minY);
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		ISelectionHandle retVal = null;
		// check if in this selection at all.
		if(this.selectionBounds.containsPoint(point)){
			ISelectionHandle centralModel = null;
			Iterator<ISelectionHandle> iter = this.selectionModels.iterator();
			while(iter.hasNext() && retVal == null){
				ISelectionHandle model = iter.next();
				if(!model.getRegion().equals(SelectionRegion.Central)){
					if(model.containsPoint(point)){
						retVal = model;
					}
				}
				else{
					centralModel = model;
				}
			}
			// central model is check last as the other blocks take precedence
			if(retVal == null && centralModel.containsPoint(point)){
				retVal = centralModel;
			}
		}
		return retVal;
	}

}
