package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.behaviour.IHitCalculator;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourListener;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public class LinkCreationMouseBehaviourListener implements IMouseBehaviourListener {
	private ILinkCreationOperation linkCreationResponse;
	private final IHitCalculator mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	private ILinkTypeInspector objectTypeInspector;
	
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

}
