package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.controller.INodeController;

public abstract class SelectionHandle implements ISelectionHandle {
	private final INodeController controller;
	private final SelectionHandleType region;
	private final ISelection selection;
	
	protected SelectionHandle(INodeController node, SelectionHandleType region, ISelection selection){
		this.controller = node;
		this.region = region;
		this.selection = selection;
	}

	
	@Override
	public final INodeController getNodeController(){
		return this.controller;
	}
	
	@Override
	public final SelectionHandleType getType(){
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
		return new MidPointSelectionHandle(selection, nodeController, bounds.getOrigin(), bounds.getHorizontalCorner(), SelectionHandleType.N);
	}

	public static ISelectionHandle createNERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getHorizontalCorner(), SelectionHandleType.NE);
	}

	public static ISelectionHandle createSERegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getDiagonalCorner(), SelectionHandleType.SE);
	}

	public static ISelectionHandle createSRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getDiagonalCorner(), bounds.getVerticalCorner(), SelectionHandleType.S);
	}

	public static ISelectionHandle createSWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getVerticalCorner(), SelectionHandleType.SW);
	}

	public static ISelectionHandle createWRegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getVerticalCorner(), bounds.getOrigin(), SelectionHandleType.W);
	}

	public static ISelectionHandle createNWRegion(ISelection selection, INodeController nodeController) {
		return new CornerSelectionHandle(selection, nodeController, nodeController.getBounds().getOrigin(), SelectionHandleType.NW);
	}

	public static ISelectionHandle createERegion(ISelection selection, INodeController nodeController) {
		Envelope bounds = nodeController.getBounds(); 
		return new MidPointSelectionHandle(selection, nodeController, bounds.getHorizontalCorner(), bounds.getDiagonalCorner(), SelectionHandleType.E);
	}
}
