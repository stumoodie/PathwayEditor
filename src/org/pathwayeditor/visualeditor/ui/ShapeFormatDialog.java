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
package org.pathwayeditor.visualeditor.ui;

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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private static final int MIN_TRANS = 0;
	private static final int MAX_TRANS = 100;
	private static final int TRANS_INIT = MAX_TRANS;
	private final JPanel fillPanel = new JPanel();
	private final JPanel linePanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private IShapeController selectedShape;
	private JPanel lineColourLabel;
	private JPanel fillColourLabel;
	private ICompoundCommand latestCommand;
	private JComboBox lineWidthCombo;
	private JSlider lineTransSlider;
	private JSlider fillTransSlider;

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
		lineColourLabel = new JPanel();
		lineColourLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color lineColour = JColorChooser.showDialog(ShapeFormatDialog.this, "Line Colour", lineColourLabel.getBackground());
				Color origColour = lineColourLabel.getBackground();
				lineColourLabel.setBackground(new Color(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue(), origColour.getAlpha()));
				linePanel.repaint();
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
		JLabel transparencyLabel = new JLabel("Transparency");
		this.lineTransSlider = new JSlider(JSlider.HORIZONTAL, MIN_TRANS, MAX_TRANS, TRANS_INIT);
		this.lineTransSlider.setMajorTickSpacing(25);
		this.lineTransSlider.setMinorTickSpacing(10);
		this.lineTransSlider.setPaintTicks(true);
		this.lineTransSlider.setPaintLabels(true);
		this.lineTransSlider.setPaintTrack(true);
		this.lineTransSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()){
					float alpha = source.getValue();
					int currentAlpha = Math.round((alpha/100.0f) * (float)Colour.OPAQUE);
					Color col = lineColourLabel.getBackground();
					Color newCol = new Color(col.getRed(), col.getGreen(), col.getBlue(), currentAlpha);
					lineColourLabel.setBackground(newCol);
					linePanel.repaint();
				}
			}
		});
		
		GridBagConstraints c6 = new GridBagConstraints();
		c6.gridx = 0;
		c6.gridy = 1;
		linePanel.add(transparencyLabel, c6);
		GridBagConstraints c7 = new GridBagConstraints();
		c7.gridx = 1;
		c7.gridy = 1;
		c7.gridwidth = GridBagConstraints.REMAINDER;
		linePanel.add(lineTransSlider, c7);
		JLabel lineWidthLabel = new JLabel("Width");
		lineWidthCombo = new JComboBox(LINE_WIDTH_OPTION);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridx = 0;
		c4.gridy = 2;
		linePanel.add(lineWidthLabel, c4);
		GridBagConstraints c5 = new GridBagConstraints();
		c5.gridx = 1;
		c5.gridy = 2;
		c5.fill = GridBagConstraints.HORIZONTAL;
		linePanel.add(lineWidthCombo, c5);
		lineWidthCombo.setEditable(false);
	}


	private void layoutFillColourPanel(){
		fillPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Fill"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		fillPanel.setLayout(new GridBagLayout());
		JLabel colourLabel = new JLabel("Colour");
		fillColourLabel = new JPanel();
		fillColourLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color fillColour = JColorChooser.showDialog(ShapeFormatDialog.this, "Fill Colour", fillColourLabel.getBackground());
				fillColourLabel.setBackground(new Color(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue(), fillColourLabel.getBackground().getAlpha()));
				fillPanel.repaint();
			}
		});
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
		c1.ipadx = 6;
		fillPanel.add(colourLabel, c1);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		fillPanel.add(fillColourLabel, c2);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 2;
		c3.gridy = 0;
		c3.ipadx = 6;
		fillPanel.add(colorDialogButton, c3);
		JLabel transparencyLabel = new JLabel("Transparency");
		this.fillTransSlider = new JSlider(JSlider.HORIZONTAL, MIN_TRANS, MAX_TRANS, TRANS_INIT);
		this.fillTransSlider.setMajorTickSpacing(25);
		this.fillTransSlider.setMinorTickSpacing(10);
		this.fillTransSlider.setPaintTicks(true);
		this.fillTransSlider.setPaintLabels(true);
		this.fillTransSlider.setPaintTrack(true);
		this.fillTransSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()){
					float alpha = source.getValue();
					int currentAlpha = Math.round((alpha/100.0f) * (float)Colour.OPAQUE);
					Color col = fillColourLabel.getBackground();
					Color newCol = new Color(col.getRed(), col.getGreen(), col.getBlue(), currentAlpha);
					fillColourLabel.setBackground(newCol);
					fillPanel.repaint();
				}
			}
		});
		
		GridBagConstraints c6 = new GridBagConstraints();
		c6.gridx = 0;
		c6.gridy = 1;
		fillPanel.add(transparencyLabel, c6);
		GridBagConstraints c7 = new GridBagConstraints();
		c7.gridx = 1;
		c7.gridy = 1;
		c7.gridwidth = GridBagConstraints.REMAINDER;
		fillPanel.add(fillTransSlider, c7);
	}


	public void setSelectedShape(IShapeController shape) {
		this.selectedShape = shape;
		Colour fillCol = this.selectedShape.getDrawingElement().getAttribute().getFillColour();
		RGB fillRGB = fillCol.getRgb();
		Color fillColour = new Color(fillRGB.getRed(), fillRGB.getGreen(), fillRGB.getBlue(), fillCol.getAlpha());
		fillColourLabel.setBackground(fillColour);
		setFillTransparency(fillColour);
//		fillColourLabel.setVisible(false);
//		fillColourLabel.setVisible(true);
		Colour lineCol = this.selectedShape.getDrawingElement().getAttribute().getLineColour();
		RGB lineRGB = lineCol.getRgb();
		Color lineColour = new Color(lineRGB.getRed(), lineRGB.getGreen(), lineRGB.getBlue(), lineCol.getAlpha());
		lineColourLabel.setBackground(lineColour);
		setLineTransparency(lineColour);
//		lineColourLabel.setVisible(false);
//		lineColourLabel.setVisible(true);
		double lineWidth = Math.max(MIN_LINE_WIDTH, this.selectedShape.getDrawingElement().getAttribute().getLineWidth());
		this.lineWidthCombo.setSelectedItem(new Integer((int)Math.round(lineWidth)));
	}


	private void setLineTransparency(Color lineColour) {
		float alpha = lineColour.getAlpha();
		float opaque = Colour.OPAQUE;
		this.lineTransSlider.setValue(Math.round((alpha/opaque)*(float)MAX_TRANS));
	}


	private void setFillTransparency(Color fillColour) {
		float alpha = fillColour.getAlpha();
		float opaque = Colour.OPAQUE;
		this.fillTransSlider.setValue(Math.round((alpha/opaque)*(float)MAX_TRANS));
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
			Colour fillCol = new Colour(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue(), fillColour.getAlpha());
			if(!this.selectedShape.getDrawingElement().getAttribute().getFillColour().equals(fillCol)){
				this.latestCommand.addCommand(new ChangeShapeFillPropertyChange(this.selectedShape.getDrawingElement().getAttribute(), fillCol));
			}
			Color lineColour = this.lineColourLabel.getBackground();
			Colour lineCol = new Colour(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue(), lineColour.getAlpha());
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
