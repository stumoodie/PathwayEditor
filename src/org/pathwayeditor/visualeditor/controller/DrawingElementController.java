package org.pathwayeditor.visualeditor.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;

public abstract class DrawingElementController implements IDrawingElementController {
	private final List<IDrawingElementControllerListener> listeners;
	private final IViewControllerModel viewModel;
	private final IViewControllerSubModel viewSubModel;
	private final int index;
	
	protected DrawingElementController(IViewControllerModel viewModel, int index){
		this.listeners = new LinkedList<IDrawingElementControllerListener>();
		this.viewModel = viewModel;
		this.index = index;
		this.viewSubModel = new ViewControllerSubModel(viewModel, this);
	}
	
	@Override
	public final IViewControllerModel getViewModel() {
		return this.viewModel;
	}

	/**
	 * Get the view controller sub-model for this node
	 * @return the view controller sub-model, which cannot be null.
	 */
	@Override
	public IViewControllerSubModel getViewControllerSubModel(){
		return this.viewSubModel;
	}
	
	@Override
	public final void addDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public final List<IDrawingElementControllerListener> getDrawingPrimitiveControllerListeners() {
		return new ArrayList<IDrawingElementControllerListener>(this.listeners);
	}

	@Override
	public final void removeDrawingPrimitiveControllerListener(IDrawingElementControllerListener listener) {
		this.listeners.remove(listeners);
	}
	
	protected final void notifyDrawnBoundsChanged(final Envelope oldBounds, final Envelope newBounds){
		IDrawingPrimitiveControllerEvent e = new IDrawingPrimitiveControllerEvent(){

			@Override
			public IDrawingElementController getController() {
				return DrawingElementController.this;
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
		for(IDrawingElementControllerListener l : this.listeners){
			l.drawnBoundsChanged(e);
		}
	}
	
//	@Override
//	public final int compareTo(IDrawingElementController o) {
//		return this.index == o.getIndex() ? 0 : (this.index < o.getIndex() ? -1 : 1);
//	}

	
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
		if (!(obj instanceof DrawingElementController))
			return false;
		DrawingElementController other = (DrawingElementController) obj;
		if (index != other.index)
			return false;
		return true;
	}
	
	@Override
	public final String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(index=");
		buf.append(this.index);
		buf.append(",drawingElement=");
		buf.append(this.getDrawingElement());
		buf.append(",drawnBounds=");
		buf.append(this.getDrawnBounds());
		buf.append(",active=");
		buf.append(this.isActive());
		buf.append(")");
		return buf.toString();
	}
	
	@Override
	public int compareTo(IDrawingElementController other){
		int retVal = this.getDrawingElement().getGraphElement().getLevel() < other.getDrawingElement().getGraphElement().getLevel() ? -1 :
			(this.getDrawingElement().getGraphElement().getLevel() > other.getDrawingElement().getGraphElement().getLevel() ? 1 : 0);
		if(retVal == 0){
			retVal = this.getDrawingElement().getGraphElement().getIndex() < other.getDrawingElement().getGraphElement().getIndex() ? -1 :
				(this.getDrawingElement().getGraphElement().getIndex() > other.getDrawingElement().getGraphElement().getIndex() ? 1 : 0);
		}
		return retVal;
	}
}
