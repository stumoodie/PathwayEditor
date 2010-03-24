package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class LinkBendpointPopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener deleteListener;
	private JMenuItem deleteShapeItem;
	private JMenuItem deleteBendPointItem;
	private ActionListener deleteBendPointItemListener;
	private ISelectionHandle selectionHandle;
	final ILinkBendPointPopupActions popupActions;
	
	public LinkBendpointPopupMenuResponse(ILinkBendPointPopupActions localPopupActions){
		this.popupActions = localPopupActions;
		popup = new JPopupMenu();
		deleteShapeItem = new JMenuItem("Delete");
		this.deleteListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.delete();
			}
			
		};
		popup.add(deleteShapeItem);
		deleteBendPointItem = new JMenuItem("Remove Bend Point");
		this.deleteBendPointItemListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.deleteBendPoint(selectionHandle.getHandleIndex());
			}
			
		};
		popup.add(deleteBendPointItem);
	}
	
	
	@Override
	public void activate(){
		deleteShapeItem.addActionListener(deleteListener);
		deleteBendPointItem.addActionListener(deleteBendPointItemListener);
	}
	
	@Override
	public void deactivate(){
		deleteShapeItem.removeActionListener(deleteListener);
		deleteBendPointItem.removeActionListener(deleteBendPointItemListener);
	}
	
	@Override
	public JPopupMenu getPopupMenu(ISelectionHandle selectionHandle) {
		this.selectionHandle = selectionHandle;
		return this.popup;
	}

}
