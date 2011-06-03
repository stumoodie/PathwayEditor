/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapePopupActions;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class ShapePopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener deleteListener;
	private final JMenuItem deleteShapeItem;
	private final JMenuItem formatMenuItem;
	private final ActionListener formatListener;
	private ShapeFormatDialog shapeFormatDialog;

	
	public ShapePopupMenuResponse(final IShapePopupActions popupActions){
		popup = new JPopupMenu();
		deleteShapeItem = new JMenuItem("Delete");
		formatMenuItem = new JMenuItem("Format");
		
		this.deleteListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.delete();
			}
			
		};
		this.formatListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem mi = (JMenuItem)e.getSource();
				JPopupMenu pm = (JPopupMenu)mi.getParent();
		        Component invoker = pm.getInvoker(); //this is the JMenu (in my code)  
		        JComponent invokerAsJComponent = (JComponent) invoker;  
		        JFrame topLevel = (JFrame)invokerAsJComponent.getTopLevelAncestor();  
				shapeFormatDialog = new ShapeFormatDialog(topLevel);
				IShapeController shape = popupActions.getSelectedShape();
				shapeFormatDialog.setSelectedShape(shape);
				shapeFormatDialog.setVisible(true);
			}
			
		};
		popup.add(formatMenuItem);
		popup.add(deleteShapeItem);
	}
	
	
	@Override
	public void activate(){
		deleteShapeItem.addActionListener(deleteListener);
		formatMenuItem.addActionListener(formatListener);
	}
	
	@Override
	public void deactivate(){
		deleteShapeItem.removeActionListener(deleteListener);
		formatMenuItem.removeActionListener(formatListener);
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		return this.popup;
	}


	@Override
	public void setSelectionHandle(ISelectionHandle selectionHandle) {
	}


	@Override
	public ISelectionHandle getSelectionHandle() {
		return null;
	}
}
