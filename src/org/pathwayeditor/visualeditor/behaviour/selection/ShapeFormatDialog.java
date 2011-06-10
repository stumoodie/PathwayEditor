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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.visualeditor.commands.ChangeShapeFillPropertyChange;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public class ShapeFormatDialog extends JDialog implements ActionListener, FocusListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	private static final String OK_CMD = "ok_cmd";
	private static final String CANCEL_CMD = "cancel_cmd";
	private final JPanel fillPanel = new JPanel();
	private final JPanel linePanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private IShapeController selectedShape;
	private JLabel lineColourLabel;
	private JLabel fillColourLabel;
	private ICompoundCommand latestCommand;
	private boolean hasFormatChanged = false;

	public ShapeFormatDialog(JFrame frame){
		super(frame, true);
		setTitle("Format Shape");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		layoutLineColourPanel();
		layoutFillColourPanel();
		this.okButton.addActionListener(this);
		this.okButton.setActionCommand(OK_CMD);
		this.cancelButton.addActionListener(this);
		this.cancelButton.setActionCommand(CANCEL_CMD);
		this.buttonPanel.add(okButton);
		this.buttonPanel.add(cancelButton);
		this.add(linePanel);
		this.add(fillPanel);
		this.add(buttonPanel);
		this.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent e) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				hasFormatChanged = false;
				latestCommand = new CompoundCommand();
			}
		});
		this.pack();
	}

	
	private void layoutLineColourPanel(){
		linePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Line"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel colourLabel = new JLabel("Colour");
		lineColourLabel = new JLabel();
		lineColourLabel.setOpaque(true);
		lineColourLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color lineColour = JColorChooser.showDialog(ShapeFormatDialog.this, "Line Colour", lineColourLabel.getBackground());
				lineColourLabel.setBackground(lineColour);
			}
		});
		linePanel.add(colourLabel);
		linePanel.add(lineColourLabel);
		linePanel.add(colorDialogButton);
	}


	private void layoutFillColourPanel(){
		fillPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Fill"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel colourLabel = new JLabel("Colour");
		fillColourLabel = new JLabel();
		fillColourLabel.setOpaque(true);
		fillColourLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color fillColour = JColorChooser.showDialog(ShapeFormatDialog.this, "Fill Colour", fillColourLabel.getBackground());
				fillColourLabel.setBackground(fillColour);
			}
		});
		fillPanel.add(colourLabel);
		fillPanel.add(fillColourLabel);
		fillPanel.add(colorDialogButton);
	}


	public void setSelectedShape(IShapeController shape) {
		this.selectedShape = shape;
		RGB fillRGB = this.selectedShape.getDrawingElement().getAttribute().getFillColour();
		Color fillColour = new Color(fillRGB.getRed(), fillRGB.getGreen(), fillRGB.getBlue());
		fillColourLabel.setBackground(fillColour);
		RGB lineRGB = this.selectedShape.getDrawingElement().getAttribute().getLineColour();
		Color lineColour = new Color(lineRGB.getRed(), lineRGB.getGreen(), lineRGB.getBlue());
		lineColourLabel.setBackground(lineColour);
	}


	@Override
	public void focusGained(FocusEvent arg0) {
	}


	@Override
	public void focusLost(FocusEvent arg0) {
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(OK_CMD)){
			Color fillColour = this.fillColourLabel.getBackground();
			RGB fillRGB = new RGB(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue());
			if(!this.selectedShape.getDrawingElement().getAttribute().getFillColour().equals(fillRGB)){
				this.latestCommand.addCommand(new ChangeShapeFillPropertyChange(this.selectedShape.getDrawingElement().getAttribute(), fillRGB));
				this.hasFormatChanged = true;
			}
			this.setVisible(false);
		}
		else if(e.getActionCommand().equals(CANCEL_CMD)){
			this.setVisible(false);
		}
	}
	
	public ICommand getCommand(){
		return this.latestCommand;
	}


	public boolean hasFormatChanged() {
		return this.hasFormatChanged ;
	}

}
