package org.pathwayeditor.visualeditor.controller;



public abstract class NodeController extends DrawingPrimitiveController implements INodeController {
//	private final List<INodeControllerChangeListener> listeners;

	protected NodeController(IViewControllerStore viewController, int index){
		super(viewController, index);
//		this.listeners = new LinkedList<INodeControllerChangeListener>();
	}

//	protected final void notifyTranslation(final Point delta){
//		INodeTranslationEvent e = new INodeTranslationEvent(){
//
//			@Override
//			public INodeController getChangedNode() {
//				return NodeController.this;
//			}
//
//			@Override
//			public Point getTranslationDelta() {
//				return delta;
//			}
//			
//		};
//		for(INodeControllerChangeListener listener : this.listeners){
//			listener.nodeTranslated(e);
//		}
//	}
	
//	@Override
//	public final void addNodePrimitiveChangeListener(INodeControllerChangeListener listener) {
//		this.listeners.add(listener);
//		
//	}
//
//	@Override
//	public final List<INodeControllerChangeListener> getNodePrimitiveChangeListeners() {
//		return new ArrayList<INodeControllerChangeListener>(this.listeners);
//	}
//
//	@Override
//	public final void removeNodePrimitiveChangeListener(INodeControllerChangeListener listener) {
//		this.listeners.remove(listener);
//	}

//	protected final void notifyResize(final Point originDelta, final Dimension resizeDelta) {
//		INodeResizeEvent e = new INodeResizeEvent(){
//
//			@Override
//			public INodeController getChangedNode() {
//				return NodeController.this;
//			}
//
//			@Override
//			public Point getOriginDelta() {
//				return originDelta;
//			}
//			
//			
//			@Override
//			public Dimension getSizeDelta(){
//				return resizeDelta;
//			}
//			
//		};
//		for(INodeControllerChangeListener listener : this.listeners){
//			listener.nodeResized(e);
//		}
//	}
	
//	protected final void notifyChangedBounds(final Envelope originalBounds, final Envelope changedBounds) {
//		INodeBoundsChangeEvent e = new INodeBoundsChangeEvent() {
//			
//			@Override
//			public Envelope getOriginBounds() {
//				return originalBounds;
//			}
//			
//			@Override
//			public Envelope getNewBounds() {
//				return changedBounds;
//			}
//			
//			@Override
//			public INodeController getChangedNode() {
//				return NodeController.this;
//			}
//		};
//		for(INodeControllerChangeListener listener : this.listeners){
//			listener.changedBounds(e);
//		}
//	}
	
}
