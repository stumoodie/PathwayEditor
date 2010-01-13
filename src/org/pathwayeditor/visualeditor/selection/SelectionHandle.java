package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.controller.INodeController;

public abstract class SelectionHandle implements ISelectionHandle {
	private final INodeController controller;
	private final SelectionRegion region;
	private final ISelection selection;
	
	protected SelectionHandle(INodeController node, SelectionRegion region, ISelection selection){
		this.controller = node;
		this.region = region;
		this.selection = selection;
	}

	
	@Override
	public final INodeController getNodeController(){
		return this.controller;
	}
	
	@Override
	public final SelectionRegion getRegion(){
		return this.region;
	}
	
	
	@Override
	public final ISelection getSelection(){
		return this.selection;
	}
	
	public static ISelectionHandle createCentralRegion(ISelection selection, INodeController nodeController) {
		return new CentralSelectionHandle(selection, nodeController);
	}

	public static ISelectionHandle createNRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getOrigin(), bounds.getHorizontalCorner(), SelectionRegion.N);
	}

	public static ISelectionHandle createNERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getHorizontalCorner(), SelectionRegion.NE);
	}

	public static ISelectionHandle createSERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getDiagonalCorner(), SelectionRegion.SE);
	}

	public static ISelectionHandle createSRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getDiagonalCorner(), bounds.getVerticalCorner(), SelectionRegion.S);
	}

	public static ISelectionHandle createSWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getVerticalCorner(), SelectionRegion.SW);
	}

	public static ISelectionHandle createWRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getVerticalCorner(), bounds.getOrigin(), SelectionRegion.W);
	}

	public static ISelectionHandle createNWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getOrigin(), SelectionRegion.NW);
	}

	public static ISelectionHandle createERegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getHorizontalCorner(), bounds.getDiagonalCorner(), SelectionRegion.E);
	}
}
