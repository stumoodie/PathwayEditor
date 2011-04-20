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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.behaviour.IPopupMenuResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IDefaultPopupActions;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class DefaultPopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener selectAllListener;
	private final JMenuItem selectShapeItem;
	private final JMenuItem deleteShapeItem;
	private final ActionListener deleteListener;
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
	public JPopupMenu getPopupMenu() {
		deleteShapeItem.setEnabled(this.popupActions.isDeleteActionValid());
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
