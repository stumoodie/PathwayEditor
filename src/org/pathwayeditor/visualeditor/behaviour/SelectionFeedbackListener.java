package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;

public class SelectionFeedbackListener implements MouseListener {
	private final ISelectionStateBehaviourController mouseBehaviourController;
	
	public SelectionFeedbackListener(ISelectionStateBehaviourController mouseBehaviourController) {
		this.mouseBehaviourController = mouseBehaviourController;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			if(!e.isShiftDown() && !e.isAltDown()){
				Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
				IDrawingElementController nodeController = this.mouseBehaviourController.findDrawingElementAt(location);
				if(nodeController != null){
					this.mouseBehaviourController.getSelectionRecord().setPrimarySelection(nodeController);
				}
				else{
					this.mouseBehaviourController.getSelectionRecord().clear();
				}
			}
			else if(e.isShiftDown() && !e.isAltDown()){
				Point location = this.mouseBehaviourController.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
				IDrawingElementController nodeController = this.mouseBehaviourController.findDrawingElementAt(location);
				if(nodeController != null){
					this.mouseBehaviourController.getSelectionRecord().addSecondarySelection(nodeController);
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
