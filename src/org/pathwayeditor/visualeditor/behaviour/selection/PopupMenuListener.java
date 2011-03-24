package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionStateBehaviourController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class PopupMenuListener implements MouseListener {
	private final ISelectionStateBehaviourController controller;

	public PopupMenuListener(ISelectionStateBehaviourController controller){
		this.controller = controller;
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
			SelectionHandleType popupSelectionHandle = SelectionHandleType.None;
//			Point location = controller.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
			this.controller.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			ISelectionHandle currSelectionHandle = controller.getSelectionHandle();
			
//			ISelectionLayer selectionLayer = shapePane.getLayer(LayerType.SELECTION);
//			IDrawingElementController nodeController = findDrawingElementAt(location);
//			if(nodeController != null){
//				if(!selectionLayer.getSelectionRecord().isNodeSelected(nodeController)){
//					// not selected so select first before do anything else
//					selectionLayer.getSelectionRecord().setPrimarySelection(nodeController);
//				}
//			}
//			ISelectionHandle currSelectionHandle = selectionLayer.getSelectionRecord().findSelectionModelAt(location);
			if(currSelectionHandle != null){
				popupSelectionHandle = currSelectionHandle.getType();
			}
			IPopupMenuResponse response = controller.getPopupMenuResponse(popupSelectionHandle);
			JPopupMenu popup = response.getPopupMenu(currSelectionHandle);
			if(popup != null){
				controller.showPopupMenus(popup);
//				popup.show(shapePane, e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	

}
