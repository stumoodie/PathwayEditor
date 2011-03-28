package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuListener;
import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionStateBehaviourController;

public class PopupMenuListener implements IPopupMenuListener {
	private final ISelectionStateBehaviourController controller;
	private final IControllerResponses responses;
	private boolean isActive;

	public PopupMenuListener(ISelectionStateBehaviourController controller,	IControllerResponses reponses){
		this.controller = controller;
		this.responses = reponses;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isPopupTrigger()){
//			SelectionHandleType popupSelectionHandle = SelectionHandleType.None;
//			Point location = controller.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
			this.controller.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			
//			ISelectionLayer selectionLayer = shapePane.getLayer(LayerType.SELECTION);
//			IDrawingElementController nodeController = findDrawingElementAt(location);
//			if(nodeController != null){
//				if(!selectionLayer.getSelectionRecord().isNodeSelected(nodeController)){
//					// not selected so select first before do anything else
//					selectionLayer.getSelectionRecord().setPrimarySelection(nodeController);
//				}
//			}
//			ISelectionHandle currSelectionHandle = selectionLayer.getSelectionRecord().findSelectionModelAt(location);
			IPopupMenuResponse response = controller.getPopupMenuResponse();
			JPopupMenu popup = response.getPopupMenu();
			if(popup != null){
				controller.showPopupMenus(popup);
//				popup.show(shapePane, e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void activate() {
        Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
        while(iter.hasNext()){
        	IPopupMenuResponse popupResponse = iter.next();
        	popupResponse.activate();
        }
        this.isActive = true;
	}

	@Override
	public void deactivate() {
        Iterator<IPopupMenuResponse> iter = this.responses.popResponseIterator();
        while(iter.hasNext()){
        	IPopupMenuResponse popupResponse = iter.next();
        	popupResponse.deactivate();
        }
        this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}
	

}
