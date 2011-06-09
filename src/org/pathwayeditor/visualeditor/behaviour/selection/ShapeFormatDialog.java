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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public class ShapeFormatDialog extends JDialog implements ActionListener, FocusListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	private static final String OK_CMD = "ok_cmd";
	private static final String CANCEL_CMD = "cancel_cmd";
	private final JPanel fillPanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private IShapeController selectedShape;
	private Color fillColour;
	private JLabel colourDisplayLabel;

	public ShapeFormatDialog(JFrame frame){
		super(frame, true);
		fillColour = Color.BLACK;
		setTitle("Format Shape");
		this.setLayout(new BorderLayout());
		layoutFillColourPanel();
		this.okButton.addActionListener(this);
		this.okButton.setActionCommand(OK_CMD);
		this.cancelButton.addActionListener(this);
		this.cancelButton.setActionCommand(CANCEL_CMD);
		this.buttonPanel.add(okButton);
		this.buttonPanel.add(cancelButton);
		this.add(fillPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.PAGE_END);
		this.pack();
	}

	
	private void layoutFillColourPanel(){
		fillPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Fill"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel colourLabel = new JLabel("Colour");
		colourDisplayLabel = new JLabel();
		colourDisplayLabel.setBackground(fillColour);
		colourDisplayLabel.setOpaque(true);
		colourDisplayLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				fillColour = JColorChooser.showDialog(ShapeFormatDialog.this, "Fill Colour", colourDisplayLabel.getBackground());
				colourDisplayLabel.setBackground(fillColour);
			}
		});
		fillPanel.add(colourLabel);
		fillPanel.add(colourDisplayLabel);
		fillPanel.add(colorDialogButton);
	}


	public void setSelectedShape(IShapeController shape) {
		this.selectedShape = shape;
		RGB fillRGB = this.selectedShape.getDrawingElement().getAttribute().getFillColour();
		fillColour = new Color(fillRGB.getRed(), fillRGB.getGreen(), fillRGB.getBlue());
		colourDisplayLabel.setBackground(fillColour);
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
			RGB fillRGB = new RGB(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue());
			this.selectedShape.getDrawingElement().getAttribute().setFillColour(fillRGB);
			this.setVisible(false);
		}
		else if(e.getActionCommand().equals(CANCEL_CMD)){
			this.setVisible(false);
		}
	}

}
