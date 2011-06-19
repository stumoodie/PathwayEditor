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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.visualeditor.commands.ChangeShapeFillPropertyChange;
import org.pathwayeditor.visualeditor.commands.ChangeShapeLinePropertyChange;
import org.pathwayeditor.visualeditor.commands.ChangeShapeLineWidth;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public class ShapeFormatDialog extends JDialog implements ActionListener, FocusListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	private static final String OK_CMD = "ok_cmd";
	private static final String CANCEL_CMD = "cancel_cmd";
	private static final Object[] LINE_WIDTH_OPTION = new Integer[] { new Integer(1), new Integer(2), new Integer(4), new Integer(6), new Integer(8), new Integer(10) };
	private static final double MIN_LINE_WIDTH = 1.0;
	private final JPanel fillPanel = new JPanel();
	private final JPanel linePanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private IShapeController selectedShape;
	private JLabel lineColourLabel;
	private JLabel fillColourLabel;
	private ICompoundCommand latestCommand;
	private JComboBox lineWidthCombo;

	public ShapeFormatDialog(JFrame frame){
		super(frame, true);
		setTitle("Format Shape");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		layoutLinePanel();
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
				latestCommand = new CompoundCommand();
			}
		});
		this.pack();
	}

	
	private void layoutLinePanel(){
		linePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Line"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		linePanel.setLayout(new GridBagLayout());
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
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
		c1.ipadx = 6;
		linePanel.add(colourLabel, c1);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		linePanel.add(lineColourLabel, c2);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 2;
		c3.gridy = 0;
		c3.ipadx = 6;
		linePanel.add(colorDialogButton, c3);
		JLabel lineWidthLabel = new JLabel("Width");
		lineWidthCombo = new JComboBox(LINE_WIDTH_OPTION);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridx = 0;
		c4.gridy = 1;
		linePanel.add(lineWidthLabel, c4);
		GridBagConstraints c5 = new GridBagConstraints();
		c5.gridx = 1;
		c5.gridy = 1;
		c5.fill = GridBagConstraints.HORIZONTAL;
		linePanel.add(lineWidthCombo, c5);
		lineWidthCombo.setEditable(false);
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
		Colour fillCol = this.selectedShape.getDrawingElement().getAttribute().getFillColour();
		RGB fillRGB = fillCol.getRgb();
		Color fillColour = new Color(fillRGB.getRed(), fillRGB.getGreen(), fillRGB.getBlue(), fillCol.getAlpha());
		fillColourLabel.setBackground(fillColour);
		Colour lineCol = this.selectedShape.getDrawingElement().getAttribute().getLineColour();
		RGB lineRGB = lineCol.getRgb();
		Color lineColour = new Color(lineRGB.getRed(), lineRGB.getGreen(), lineRGB.getBlue(), lineCol.getAlpha());
		lineColourLabel.setBackground(lineColour);
		double lineWidth = Math.max(MIN_LINE_WIDTH, this.selectedShape.getDrawingElement().getAttribute().getLineWidth());
		this.lineWidthCombo.setSelectedItem(new Integer((int)Math.round(lineWidth)));
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
			Colour fillCol = new Colour(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue());
			if(!this.selectedShape.getDrawingElement().getAttribute().getFillColour().equals(fillCol)){
				this.latestCommand.addCommand(new ChangeShapeFillPropertyChange(this.selectedShape.getDrawingElement().getAttribute(), fillCol));
			}
			Color lineColour = this.lineColourLabel.getBackground();
			Colour lineCol = new Colour(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue());
			if(!this.selectedShape.getDrawingElement().getAttribute().getLineColour().equals(lineCol)){
				this.latestCommand.addCommand(new ChangeShapeLinePropertyChange(this.selectedShape.getDrawingElement().getAttribute(), lineCol));
			}
			Integer selectedLineWidth = (Integer)this.lineWidthCombo.getSelectedItem();
			if(!(this.selectedShape.getDrawingElement().getAttribute().getLineWidth() == selectedLineWidth.doubleValue())){
				this.latestCommand.addCommand(new ChangeShapeLineWidth(this.selectedShape.getDrawingElement().getAttribute(), selectedLineWidth.doubleValue()));
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
		return !this.latestCommand.isEmpty();
	}

}
