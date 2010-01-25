package org.pathwayeditor.visualeditor.controller;

import java.util.LinkedList;
import java.util.List;

public abstract class DrawingPrimitiveController implements IDrawingPrimitiveController {
	private final List<IDrawingPrimitiveControllerListener> listeners;
	private IViewControllerStore viewModel;
	
	protected DrawingPrimitiveController(IViewControllerStore viewModel){
		this.listeners = new LinkedList<IDrawingPrimitiveControllerListener>();
		this.viewModel = viewModel;
	}
	
	@Override
	public final void dispose() {
		this.listeners.clear();
		this.disposeRedefinition();
		this.viewModel = null;
	}

	protected abstract void disposeRedefinition();
	
	@Override
	public final IViewControllerStore getViewModel() {
		return this.viewModel;
	}

//	protected abstract void redefinedSyncroniseToModel();
	
//	@Override
//	public final void resyncToModel(){
//		this.redefinedSyncroniseToModel();
//	}
}
