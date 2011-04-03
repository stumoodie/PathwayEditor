package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IHitCalculator;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourOperationCompletionEvent;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourStateHandler;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourStateHandlerChangeListener;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class LinkCreationBehaviourStateHandler implements IViewBehaviourStateHandler, MouseMotionListener, MouseListener {
	private ILinkCreationOperation linkCreationResponse;
	private final IHitCalculator mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkTypeInspector objectTypeInspector;
	private boolean active;
	private final List<IViewBehaviourStateHandlerChangeListener> listeners;
	
	public LinkCreationBehaviourStateHandler(IHitCalculator locationCalculator, ILinkCreationOperation linkCreationResponse,
			ILinkTypeInspector objectTypeInspector) {
		this.mouseBehaviourController = locationCalculator;
		this.linkCreationResponse = linkCreationResponse;
		this.objectTypeInspector = objectTypeInspector;
		this.listeners = new LinkedList<IViewBehaviourStateHandlerChangeListener>();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(this.linkCreationResponse.isLinkCreationStarted()){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(logger.isTraceEnabled()){
				logger.trace("Link creation ongoing posn=" + this.mouseBehaviourController.getDiagramLocation());
			}
			IShapeController potentialTarget = this.mouseBehaviourController.getShapeAtCurrentLocation();
			this.linkCreationResponse.setPotentialTarget(potentialTarget);
			this.linkCreationResponse.creationOngoing(this.mouseBehaviourController.getDiagramLocation());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(this.linkCreationResponse.isLinkCreationStarted()){
				IShapeController finalShape = this.mouseBehaviourController.getShapeAtCurrentLocation();
				this.linkCreationResponse.setPotentialTarget(finalShape);
				if(this.linkCreationResponse.canFinishCreation()){
					this.linkCreationResponse.finishCreation();
					notifyComplete(true);
				}
				else {
					Point intermediatePoint = this.mouseBehaviourController.getDiagramLocation();
					if(this.linkCreationResponse.canCreateIntermediatePoint(intermediatePoint)){
						this.linkCreationResponse.createIntermediatePoint(intermediatePoint);
					}
				}
			}
			else{
				IShapeController startShape = this.mouseBehaviourController.getShapeAtCurrentLocation();
				this.linkCreationResponse.setPotentialSourceNode(startShape);
				this.linkCreationResponse.setLinkObjectType(this.objectTypeInspector.getCurrentLinkType());
				if(this.linkCreationResponse.canStartCreation()){
					this.linkCreationResponse.startCreation();
				}
			}
		}
		else if(e.getButton() == MouseEvent.BUTTON3){
			this.linkCreationResponse.cancel();
			notifyComplete(false);
		}
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	private void notifyComplete(final boolean successfulCompletion) {
		IViewBehaviourOperationCompletionEvent e = new IViewBehaviourOperationCompletionEvent(){

			@Override
			public boolean wasCompletedSuccessfully() {
				return successfulCompletion;
			}
			
		};
		for(IViewBehaviourStateHandlerChangeListener l : this.listeners){
			l.operationCompletionEvent(e);
		}
	}

	@Override
	public void activate(IShapePane shapePane) {
		shapePane.addMouseListener(this);
		shapePane.addMouseMotionListener(this);
		this.linkCreationResponse.cancel();
		this.active = true;
	}

	@Override
	public void deactivate(IShapePane shapePane) {
		this.linkCreationResponse.cancel();
		shapePane.removeMouseListener(this);
		shapePane.removeMouseMotionListener(this);
		this.active = false;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void addViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeViewBehaviourStateHandlerChangeListener(IViewBehaviourStateHandlerChangeListener l) {
		this.listeners.remove(l);
	}

	@Override
	public List<IViewBehaviourStateHandlerChangeListener> getViewBehaviourStateHandlerChangeListener() {
		return new ArrayList<IViewBehaviourStateHandlerChangeListener>(this.listeners);
	}

}
