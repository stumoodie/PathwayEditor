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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.rendering.GenericFont;
import org.pathwayeditor.figure.rendering.IFont;
import org.pathwayeditor.visualeditor.commands.ChangeFontColourPropertyChange;
import org.pathwayeditor.visualeditor.commands.ChangeLabelFillPropertyChange;
import org.pathwayeditor.visualeditor.commands.ChangeLabelFontCommand;
import org.pathwayeditor.visualeditor.commands.ChangeLabelLinePropertyChange;
import org.pathwayeditor.visualeditor.commands.ChangeLabelLineWidth;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.controller.ILabelController;

public class LabelFormatDialog extends JDialog implements ActionListener, FocusListener {
	private static final long serialVersionUID = 1L;
	private static final String OK_CMD = "ok_cmd";
	private static final String CANCEL_CMD = "cancel_cmd";
	private static final Object[] LINE_WIDTH_OPTION = new Integer[] { new Integer(1), new Integer(2), new Integer(4), new Integer(6), new Integer(8), new Integer(10) };
	private static final int MIN_FONT_HEIGHT = 4;
	private static final int MAX_FONT_HEIGHT = 20;
	private static final double MIN_LINE_WIDTH = 1.0;
	private static final int MIN_TRANS = 0;
	private static final int MAX_TRANS = 100;
	private static final int TRANS_INIT = MAX_TRANS;
	private final JPanel fillPanel = new JPanel();
	private final JPanel fontPanel = new JPanel();
	private final JPanel linePanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private ILabelController selectedShape;
	private JPanel lineColourLabel;
	private JPanel fillColourLabel;
	private JPanel fontColourLabel;
	private ICompoundCommand latestCommand;
	private JComboBox lineWidthCombo;
	private JComboBox fontSizeCombo;
	private JSlider lineTransSlider;
	private JSlider fillTransSlider;
	private JSlider fontTransSlider;
	private JComboBox fontStyleCombo;
	private final Map<String, EnumSet<IFont.Style>> fontStyleMap;

	public LabelFormatDialog(JFrame frame){
		super(frame, true);
		fontStyleMap = new HashMap<String, EnumSet<IFont.Style>>();
		fontStyleMap.put("Normal", EnumSet.of(IFont.Style.NORMAL));
		fontStyleMap.put("Bold", EnumSet.of(IFont.Style.BOLD));
		fontStyleMap.put("Italic", EnumSet.of(IFont.Style.ITALIC));
		fontStyleMap.put("Bold+Italic", EnumSet.of(IFont.Style.BOLD, IFont.Style.ITALIC));
		setTitle("Format Label");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		layoutLinePanel();
		layoutFillColourPanel();
		layoutFontColourPanel();
		this.okButton.addActionListener(this);
		this.okButton.setActionCommand(OK_CMD);
		this.cancelButton.addActionListener(this);
		this.cancelButton.setActionCommand(CANCEL_CMD);
		this.buttonPanel.add(okButton);
		this.buttonPanel.add(cancelButton);
		this.add(linePanel);
		this.add(fillPanel);
		this.add(fontPanel);
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
				Color lineColour = JColorChooser.showDialog(LabelFormatDialog.this, "Line Colour", lineColourLabel.getBackground());
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
				Color fillColour = JColorChooser.showDialog(LabelFormatDialog.this, "Fill Colour", fillColourLabel.getBackground());
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


	private void layoutFontColourPanel(){
		fontPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Font"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		fontPanel.setLayout(new GridBagLayout());
		JLabel colourLabel = new JLabel("Colour");
		fontColourLabel = new JPanel();
		fontColourLabel.setPreferredSize(new Dimension(100, 20));
		JButton colorDialogButton = new JButton("...");
		
		colorDialogButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color fontColour = JColorChooser.showDialog(LabelFormatDialog.this, "Font Colour", fontColourLabel.getBackground());
				fontColourLabel.setBackground(new Color(fontColour.getRed(), fontColour.getGreen(), fontColour.getBlue(), fontColourLabel.getBackground().getAlpha()));
				fontPanel.repaint();
			}
		});
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
		c1.ipadx = 6;
		fontPanel.add(colourLabel, c1);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		fontPanel.add(fontColourLabel, c2);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 2;
		c3.gridy = 0;
		c3.ipadx = 6;
		fontPanel.add(colorDialogButton, c3);
		JLabel transparencyLabel = new JLabel("Transparency");
		this.fontTransSlider = new JSlider(JSlider.HORIZONTAL, MIN_TRANS, MAX_TRANS, TRANS_INIT);
		this.fontTransSlider.setMajorTickSpacing(25);
		this.fontTransSlider.setMinorTickSpacing(10);
		this.fontTransSlider.setPaintTicks(true);
		this.fontTransSlider.setPaintLabels(true);
		this.fontTransSlider.setPaintTrack(true);
		this.fontTransSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()){
					float alpha = source.getValue();
					int currentAlpha = Math.round((alpha/100.0f) * (float)Colour.OPAQUE);
					Color col = fontColourLabel.getBackground();
					Color newCol = new Color(col.getRed(), col.getGreen(), col.getBlue(), currentAlpha);
					fontColourLabel.setBackground(newCol);
					fontPanel.repaint();
				}
			}
		});
		
		GridBagConstraints c6 = new GridBagConstraints();
		c6.gridx = 0;
		c6.gridy = 1;
		fontPanel.add(transparencyLabel, c6);
		GridBagConstraints c7 = new GridBagConstraints();
		c7.gridx = 1;
		c7.gridy = 1;
		c7.gridwidth = GridBagConstraints.REMAINDER;
		fontPanel.add(fontTransSlider, c7);
		JLabel fontSizeLabel = new JLabel("Font Size");
		List<Integer> fontHeightOptions = new LinkedList<Integer>();
		for(int i = MIN_FONT_HEIGHT; i <= MAX_FONT_HEIGHT; i++){
			fontHeightOptions.add(i);
		}
		fontSizeCombo = new JComboBox(fontHeightOptions.toArray());
		GridBagConstraints c8 = new GridBagConstraints();
		c8.gridx = 0;
		c8.gridy = 2;
		fontPanel.add(fontSizeLabel, c8);
		GridBagConstraints c9 = new GridBagConstraints();
		c9.gridx = 1;
		c9.gridy = 2;
		c9.fill = GridBagConstraints.HORIZONTAL;
		fontPanel.add(fontSizeCombo, c9);
		JLabel fontStyleLabel = new JLabel("Font Style");
		fontStyleCombo = new JComboBox(this.fontStyleMap.keySet().toArray());
		GridBagConstraints c10 = new GridBagConstraints();
		c10.gridx = 0;
		c10.gridy = 3;
		fontPanel.add(fontStyleLabel, c10);
		GridBagConstraints c11 = new GridBagConstraints();
		c11.gridx = 1;
		c11.gridy = 3;
		c11.fill = GridBagConstraints.HORIZONTAL;
		fontPanel.add(fontStyleCombo, c11);
	}


	public void setSelectedLabel(ILabelController shape) {
		this.selectedShape = shape;
		Colour fillCol = this.selectedShape.getAssociatedAttribute().getFillColour();
		RGB fillRGB = fillCol.getRgb();
		Color fillColour = new Color(fillRGB.getRed(), fillRGB.getGreen(), fillRGB.getBlue(), fillCol.getAlpha());
		fillColourLabel.setBackground(fillColour);
		setFillTransparency(fillColour);
		Colour lineCol = this.selectedShape.getAssociatedAttribute().getLineColour();
		RGB lineRGB = lineCol.getRgb();
		Color lineColour = new Color(lineRGB.getRed(), lineRGB.getGreen(), lineRGB.getBlue(), lineCol.getAlpha());
		lineColourLabel.setBackground(lineColour);
		setLineTransparency(lineColour);
		double lineWidth = Math.max(MIN_LINE_WIDTH, this.selectedShape.getAssociatedAttribute().getLineWidth());
		this.lineWidthCombo.setSelectedItem(new Integer((int)Math.round(lineWidth)));
		Colour fontCol = this.selectedShape.getAssociatedAttribute().getFontColour();
		RGB fontRGB = fontCol.getRgb();
		Color fontColour = new Color(fontRGB.getRed(), fontRGB.getGreen(), fontRGB.getBlue(), fontCol.getAlpha());
		fontColourLabel.setBackground(fontColour);
		setFontTransparency(fontColour);
		double fontSize = this.selectedShape.getAssociatedAttribute().getFont().getFontSize();
		this.fontSizeCombo.setSelectedItem(new Integer((int)Math.round(fontSize)));
		EnumSet<IFont.Style> s = this.selectedShape.getAssociatedAttribute().getFont().getStyle();
		if(s.contains(IFont.Style.NORMAL)){
			this.fontStyleCombo.setSelectedItem("Normal");
		}
		else if(s.contains(IFont.Style.BOLD)){
			if(s.contains(IFont.Style.ITALIC)){
				this.fontStyleCombo.setSelectedItem("Bold+Italic");
			}
			else{
				this.fontStyleCombo.setSelectedItem("Bold");
			}
		}
		else if(s.contains(IFont.Style.ITALIC)){
			this.fontStyleCombo.setSelectedItem("Italic");
		}
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

	private void setFontTransparency(Color fontColour) {
		float alpha = fontColour.getAlpha();
		float opaque = Colour.OPAQUE;
		this.fontTransSlider.setValue(Math.round((alpha/opaque)*(float)MAX_TRANS));
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
			if(!this.selectedShape.getAssociatedAttribute().getFillColour().equals(fillCol)){
				this.latestCommand.addCommand(new ChangeLabelFillPropertyChange(this.selectedShape.getAssociatedAttribute(), fillCol));
			}
			Color lineColour = this.lineColourLabel.getBackground();
			Colour lineCol = new Colour(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue(), lineColour.getAlpha());
			if(!this.selectedShape.getAssociatedAttribute().getLineColour().equals(lineCol)){
				this.latestCommand.addCommand(new ChangeLabelLinePropertyChange(this.selectedShape.getAssociatedAttribute(), lineCol));
			}
			Integer selectedLineWidth = (Integer)this.lineWidthCombo.getSelectedItem();
			if(!(this.selectedShape.getAssociatedAttribute().getLineWidth() == selectedLineWidth.doubleValue())){
				this.latestCommand.addCommand(new ChangeLabelLineWidth(this.selectedShape.getAssociatedAttribute(), selectedLineWidth.doubleValue()));
			}
			Colour fontCol = toDomColour(this.fontColourLabel.getBackground());
			if(!this.selectedShape.getAssociatedAttribute().getFontColour().equals(fontCol)){
				this.latestCommand.addCommand(new ChangeFontColourPropertyChange(this.selectedShape.getAssociatedAttribute(), fontCol));
			}
			Integer selectedFontSize = (Integer)this.fontSizeCombo.getSelectedItem();
			if(!(this.selectedShape.getAssociatedAttribute().getFont().getFontSize() == selectedFontSize.doubleValue())){
				GenericFont newFont = this.selectedShape.getAssociatedAttribute().getFont().newSize(selectedFontSize);
				this.latestCommand.addCommand(new ChangeLabelFontCommand(this.selectedShape.getAssociatedAttribute(), newFont));
			}
			EnumSet<IFont.Style> selectedFontStyle = this.fontStyleMap.get(this.fontStyleCombo.getSelectedItem());
			if(!(this.selectedShape.getAssociatedAttribute().getFont().getStyle().containsAll(selectedFontStyle))){
				GenericFont newFont = this.selectedShape.getAssociatedAttribute().getFont().newStyle(selectedFontStyle);
				this.latestCommand.addCommand(new ChangeLabelFontCommand(this.selectedShape.getAssociatedAttribute(), newFont));
			}
			this.setVisible(false);
		}
		else if(e.getActionCommand().equals(CANCEL_CMD)){
			this.setVisible(false);
		}
	}
	
	private static Colour toDomColour(Color col){
		return new Colour(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()); 
	}
	
	public ICommand getCommand(){
		return this.latestCommand;
	}


	public boolean hasFormatChanged() {
		return !this.latestCommand.isEmpty();
	}

}
