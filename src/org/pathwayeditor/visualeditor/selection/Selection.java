package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.INodePrimitiveChangeListener;
import org.pathwayeditor.visualeditor.controller.INodeTranslationEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class Selection implements ISelection {
	private static final int NUM_REGIONS = 9;
	private final INodeController nodeController;
	private final List<ISelectionHandle> selectionModels;
	private final boolean isPrimary;
	private Envelope selectionBounds;
	private INodePrimitiveChangeListener nodeControllerListener;
//	private IDrawingPrimitiveControllerListener drawingPrimitiveListener;
	
	public Selection(boolean isPrimary, INodeController controller){
		this.isPrimary = isPrimary;
		this.nodeController = controller;
		this.selectionModels = new ArrayList<ISelectionHandle>(NUM_REGIONS);
		buildHandles();
		this.nodeControllerListener = new INodePrimitiveChangeListener(){

			@Override
			public void nodeTranslated(INodeTranslationEvent e) {
//				Point delta = e.getTranslationDelta();
//				for(ISelectionHandle handle : selectionModels){
//					handle.translate(delta);
//				}
				buildHandles();
			}
			
		};
		this.nodeController.addNodePrimitiveChangeListener(this.nodeControllerListener);
//		this.drawingPrimitiveListener = new IDrawingPrimitiveControllerListener() {
//			
//			@Override
//			public void resyncronised(IDrawingPrimitiveControllerEvent e) {
//				buildHandles();
//			}
//		};
//		this.nodeController.addDrawingPrimitiveControllerListener(drawingPrimitiveListener);
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
	public IDrawingPrimitiveController getPrimitiveController() {
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

	@Override
	public boolean isPrimary() {
		return this.isPrimary;
	}

	@Override
	public boolean isSecondary() {
		return !this.isPrimary;
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

	@Override
	public int compareTo(ISelection o) {
		int retVal = this.isPrimary() && o.isPrimary() ? 0 : (this.isPrimary() && o.isSecondary() ? -1 : 1); 
		if(retVal == 0){
			retVal = this.getPrimitiveController().compareTo(o.getPrimitiveController());
		}
		return retVal;
	}

}
