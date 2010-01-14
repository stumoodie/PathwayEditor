package org.pathwayeditor.visualeditor.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;


public abstract class NodeController extends DrawingPrimitiveController implements INodeController {
	private final List<INodePrimitiveChangeListener> listeners;

	protected NodeController(IViewControllerStore viewController){
		super(viewController);
		this.listeners = new LinkedList<INodePrimitiveChangeListener>();
	}

	protected final void notifyTranslation(final Point delta){
		INodeTranslationEvent e = new INodeTranslationEvent(){

			@Override
			public INodeController getChangedNode() {
				return NodeController.this;
			}

			@Override
			public Point getTranslationDelta() {
				return delta;
			}
			
		};
		for(INodePrimitiveChangeListener listener : this.listeners){
			listener.nodeTranslated(e);
		}
	}
	
	@Override
	public final void addNodePrimitiveChangeListener(INodePrimitiveChangeListener listener) {
		this.listeners.add(listener);
		
	}

	@Override
	public final List<INodePrimitiveChangeListener> getNodePrimitiveChangeListeners() {
		return new ArrayList<INodePrimitiveChangeListener>(this.listeners);
	}

	@Override
	public final void removeNodePrimitiveChangeListener(INodePrimitiveChangeListener listener) {
		this.listeners.remove(listener);
	}

	protected final void notifyResize(final Point originDelta, final Dimension resizeDelta) {
		INodeResizeEvent e = new INodeResizeEvent(){

			@Override
			public INodeController getChangedNode() {
				return NodeController.this;
			}

			@Override
			public Point getOriginDelta() {
				return originDelta;
			}
			
			
			@Override
			public Dimension getSizeDelta(){
				return resizeDelta;
			}
			
		};
		for(INodePrimitiveChangeListener listener : this.listeners){
			listener.nodeResized(e);
		}
	}
	
	@Override
	protected final void disposeRedefinition(){
		this.listeners.clear();
	}
	
	protected abstract void nodeDisposalHook();

}
