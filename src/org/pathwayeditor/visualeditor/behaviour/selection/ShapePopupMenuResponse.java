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
import org.pathwayeditor.visualeditor.behaviour.operation.INodePopupActions;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class ShapePopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener deleteListener;
	private final JMenuItem deleteShapeItem;
	private final JMenuItem formatMenuItem;
	private final JMenuItem propMenuItem;
	private final ActionListener formatListener;
	private final ActionListener propListener;
	private final JMenuItem toBackMenuItem;
	private final JMenuItem toFrontMenuItem;
	private final JMenuItem forwardOneMenuItem;
	private final JMenuItem backOneMenuItem;
	private final ActionListener toBackListener;
	private final ActionListener toFrontListener;
	private final ActionListener forwardOneListener;
	private final ActionListener backOneListener;

	
	public ShapePopupMenuResponse(final INodePopupActions popupActions){
		popup = new JPopupMenu();
		deleteShapeItem = new JMenuItem("Delete");
		formatMenuItem = new JMenuItem("Format");
		propMenuItem = new JMenuItem("Properties");
		toBackMenuItem = new JMenuItem("Send To Back");
		toFrontMenuItem = new JMenuItem("Bring To Front");
		forwardOneMenuItem = new JMenuItem("Bring Forward");
		backOneMenuItem = new JMenuItem("Send Backward");
		
		this.deleteListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.delete();
			}
			
		};
		this.formatListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.changeNodeFormat();
			}
			
		};
		this.propListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.properties();
			}
			
		};
		this.toBackListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.toBack();
			}
		};
		this.toFrontListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.toFront();
			}
		};
		this.forwardOneListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.forwardOne();
			}
		};
		this.backOneListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.backwardOne();
			}
		};
		popup.add(formatMenuItem);
		popup.add(propMenuItem);
		popup.add(deleteShapeItem);
		popup.add(toBackMenuItem);
		popup.add(toFrontMenuItem);
		popup.add(forwardOneMenuItem);
		popup.add(backOneMenuItem);

	}
	
	
	@Override
	public void activate(){
		deleteShapeItem.addActionListener(deleteListener);
		formatMenuItem.addActionListener(formatListener);
		propMenuItem.addActionListener(propListener);
		toBackMenuItem.addActionListener(toBackListener);
		toFrontMenuItem.addActionListener(toFrontListener);
		forwardOneMenuItem.addActionListener(forwardOneListener);
		backOneMenuItem.addActionListener(backOneListener);
	}
	
	@Override
	public void deactivate(){
		deleteShapeItem.removeActionListener(deleteListener);
		formatMenuItem.removeActionListener(formatListener);
		propMenuItem.removeActionListener(propListener);
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
