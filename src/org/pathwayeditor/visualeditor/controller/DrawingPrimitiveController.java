package org.pathwayeditor.visualeditor.controller;

import java.util.LinkedList;
import java.util.List;

public abstract class DrawingPrimitiveController implements IDrawingPrimitiveController {
	private final List<IDrawingPrimitiveControllerListener> listeners;
	
	protected DrawingPrimitiveController(){
		this.listeners = new LinkedList<IDrawingPrimitiveControllerListener>();
	}
	
//	@Override
//	public void addDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener) {
//		this.listeners.add(listener);
//	}

	@Override
	public void dispose() {
		this.listeners.clear();
		this.disposeRedefinition();
	}

	protected abstract void disposeRedefinition();
	
//	@Override
//	public List<IDrawingPrimitiveControllerListener> getDrawingPrimitiveControllerListeners() {
//		return new ArrayList<IDrawingPrimitiveControllerListener>(this.listeners);
//	}

//	@Override
//	public void removeDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener) {
//		this.listeners.remove(listeners);
//	}

	
	protected void notifyResyncronisation(){
		final IDrawingPrimitiveControllerEvent event = new IDrawingPrimitiveControllerEvent() {
			
			@Override
			public IDrawingPrimitiveController getController() {
				return DrawingPrimitiveController.this;
			}
		};
		for(IDrawingPrimitiveControllerListener listener : this.listeners){
			listener.resyncronised(event);
		}
	}
	
//	protected abstract void redefinedSyncroniseToModel();
	
//	@Override
//	public final void resyncToModel(){
//		this.redefinedSyncroniseToModel();
//		notifyResyncronisation();
//	}
}
