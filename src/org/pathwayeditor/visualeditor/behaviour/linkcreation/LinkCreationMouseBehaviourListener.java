package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IHitCalculator;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourListener;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class LinkCreationMouseBehaviourListener implements IMouseBehaviourListener, MouseMotionListener, MouseListener {
	private ILinkCreationOperation linkCreationResponse;
	private final IHitCalculator mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkTypeInspector objectTypeInspector;
	private boolean active;
	
	public LinkCreationMouseBehaviourListener(IHitCalculator locationCalculator, ILinkCreationOperation linkCreationResponse,
			ILinkTypeInspector objectTypeInspector) {
		this.mouseBehaviourController = locationCalculator;
		this.linkCreationResponse = linkCreationResponse;
		this.objectTypeInspector = objectTypeInspector;
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
}
