package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class DefaultPopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener selectAllListener;
	private JMenuItem selectShapeItem;
	private JMenuItem deleteShapeItem;
	private ActionListener deleteListener;
	private final IDefaultPopupActions popupActions;
	
	public DefaultPopupMenuResponse(final IDefaultPopupActions localPopupActions){
		this.popupActions = localPopupActions;
		popup = new JPopupMenu();
		selectShapeItem = new JMenuItem("Select All");
		this.selectAllListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.selectAll();
			}
			
		};
		popup.add(this.selectShapeItem);
		deleteShapeItem = new JMenuItem("Delete");
		this.deleteListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.delete();
			}
			
		};
		popup.add(deleteShapeItem);
	}
	
	
	@Override
	public void activate(){
		selectShapeItem.addActionListener(selectAllListener);
		deleteShapeItem.addActionListener(deleteListener);
	}
	
	@Override
	public void deactivate(){
		selectShapeItem.removeActionListener(selectAllListener);
		deleteShapeItem.removeActionListener(deleteListener);
	}
	
	@Override
	public JPopupMenu getPopupMenu(ISelectionHandle selectionHandle) {
		deleteShapeItem.setEnabled(this.popupActions.isDeleteActionValid());
		return this.popup;
	}
}
