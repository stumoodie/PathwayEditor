package org.pathwayeditor.visualeditor.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;

public abstract class DrawingPrimitiveController implements IDrawingPrimitiveController {
	private final List<IDrawingPrimitiveControllerListener> listeners;
	private IViewControllerStore viewModel;
	private final int index;
	
	protected DrawingPrimitiveController(IViewControllerStore viewModel, int index){
		this.listeners = new LinkedList<IDrawingPrimitiveControllerListener>();
		this.viewModel = viewModel;
		this.index = index;
	}
	
	@Override
	public final IViewControllerStore getViewModel() {
		return this.viewModel;
	}

	@Override
	public final void addDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public final List<IDrawingPrimitiveControllerListener> getDrawingPrimitiveControllerListeners() {
		return new ArrayList<IDrawingPrimitiveControllerListener>(this.listeners);
	}

	@Override
	public final void removeDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener) {
		this.listeners.remove(listeners);
	}
	
	protected final void notifyDrawnBoundsChanged(final Envelope oldBounds, final Envelope newBounds){
		IDrawingPrimitiveControllerEvent e = new IDrawingPrimitiveControllerEvent(){

			@Override
			public IDrawingPrimitiveController getController() {
				return DrawingPrimitiveController.this;
			}

			@Override
			public Object getCurrentValue() {
				return newBounds;
			}

			@Override
			public Object getOldValue() {
				return oldBounds;
			}
			
		};
		notifyDrawingPrimitiveControllerEvent(e);
	}

	protected final void notifyDrawingPrimitiveControllerEvent(IDrawingPrimitiveControllerEvent e) {
		for(IDrawingPrimitiveControllerListener l : this.listeners){
			l.drawnBoundsChanged(e);
		}
	}
	
	@Override
	public final int compareTo(IDrawingPrimitiveController o) {
		return this.index == o.getIndex() ? 0 : (this.index < o.getIndex() ? -1 : 1);
	}

	
	@Override
	public final int getIndex(){
		return this.index;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DrawingPrimitiveController))
			return false;
		DrawingPrimitiveController other = (DrawingPrimitiveController) obj;
		if (index != other.index)
			return false;
		return true;
	}
}
