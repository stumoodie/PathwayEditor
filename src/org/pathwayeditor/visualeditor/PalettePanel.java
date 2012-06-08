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
package org.pathwayeditor.visualeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.typedefn.IAnchorNodeObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourModeChangeEvent;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourModeChangeEvent.ModeType;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourModeChangeListener;

public class PalettePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final double PREF_ICON_HEIGHT = 16.0;
	private JScrollPane shapeScrollPane;
	private JScrollPane anchorScrollPane;
	private JScrollPane linkScrollPanel;
	private JSplitPane splitPane;

	public PalettePanel(INotationSubsystem notationSubsystem, final IViewBehaviourController editBehaviourController) {
		final ButtonGroup paletteGroup = new ButtonGroup();
		JPanel selectionButtonPanel = new JPanel();
		selectionButtonPanel.setLayout(new BoxLayout(selectionButtonPanel, BoxLayout.PAGE_AXIS));
		final JButton selectionButton = new JButton("Selection");
		ImageIcon selectionIcon = createImageIcon("images/cursor_arrow16.png");
		selectionButton.setIcon(selectionIcon);
		paletteGroup.add(selectionButton);
		selectionButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				editBehaviourController.setSelectionMode();
				paletteGroup.setSelected(selectionButton.getModel(), true);
			}
			
		});
//		selectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectionButtonPanel.add(selectionButton);
		this.add(selectionButtonPanel);
		
		createShapePane(paletteGroup, notationSubsystem, editBehaviourController);

		createAnchorPane(paletteGroup, notationSubsystem, editBehaviourController);
		
		createLinkPane(paletteGroup, notationSubsystem, editBehaviourController);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JSplitPane topPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.shapeScrollPane, this.anchorScrollPane);
		topPane.setOneTouchExpandable(true);
		topPane.setDividerLocation(250);
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPane, this.linkScrollPanel);
		this.splitPane.setOneTouchExpandable(true);
		this.splitPane.setDividerLocation(250);
		this.add(this.splitPane);

		editBehaviourController.addViewBehaviourModeChangeListener(new IViewBehaviourModeChangeListener() {
			@Override
			public void viewModeChange(IViewBehaviourModeChangeEvent e) {
				if(e.getNewModeType().equals(ModeType.SELECTION)){
					paletteGroup.setSelected(selectionButton.getModel(), true);
				}
			}
		});
		paletteGroup.setSelected(selectionButton.getModel(), true);
	}
	

	private void createLinkPane(final ButtonGroup paletteGroup, INotationSubsystem notationSubsystem, final IViewBehaviourController editBehaviourController){
		JPanel linkButtonPanel = new JPanel();
		linkButtonPanel.setLayout(new BoxLayout(linkButtonPanel, BoxLayout.PAGE_AXIS));
		
		this.linkScrollPanel = new JScrollPane(linkButtonPanel);
		this.linkScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		LinkIconGenerator linkIconGenerator = new LinkIconGenerator();
		Iterator<ILinkObjectType> linkTypeIterator = notationSubsystem.getSyntaxService().linkTypeIterator();
		linkIconGenerator.setBounds(new Envelope(0.0, 0.0, 64.0, 16.0));
		while(linkTypeIterator.hasNext()){
			final ILinkObjectType linkType = linkTypeIterator.next();
			linkIconGenerator.setObjectType(linkType);
			linkIconGenerator.generateImage();
			linkIconGenerator.generateIcon();
			final JButton linkButton = new JButton(linkIconGenerator.getIcon());
			linkButton.setText(linkType.getName());
			linkButton.setToolTipText(linkType.getDescription());
//			linkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			linkButtonPanel.add(linkButton);
			linkButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					editBehaviourController.setLinkCreationMode(linkType);
					paletteGroup.setSelected(linkButton.getModel(), true);
				}
				
			});
		}
	}
	
	private void createShapePane(final ButtonGroup paletteGroup, INotationSubsystem notationSubsystem, final IViewBehaviourController editBehaviourController){
		JPanel shapeButtonPanel = new JPanel();
		this.shapeScrollPane = new JScrollPane(shapeButtonPanel);
		this.shapeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		shapeButtonPanel.setLayout(new BoxLayout(shapeButtonPanel, BoxLayout.PAGE_AXIS));
		ShapeIconGenerator iconGenerator = new ShapeIconGenerator();
		Iterator<IShapeObjectType> shapeTypeIterator = notationSubsystem.getSyntaxService().shapeTypeIterator();
		while(shapeTypeIterator.hasNext()){
			final IShapeObjectType shapeType = shapeTypeIterator.next();
			Dimension defSize = shapeType.getDefaultAttributes().getSize();
			double widToHRatio = defSize.getWidth()/defSize.getHeight();
			iconGenerator.setBounds(new Envelope(0, 0, PREF_ICON_HEIGHT*widToHRatio, PREF_ICON_HEIGHT));
			iconGenerator.setObjectType(shapeType);
			iconGenerator.generateImage();
			iconGenerator.generateIcon();
			final JButton shapeButton = new JButton(iconGenerator.getIcon());
			shapeButton.setText(shapeType.getName());
			shapeButton.setToolTipText(shapeType.getDescription());
//			shapeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			shapeButtonPanel.add(shapeButton);
			paletteGroup.add(shapeButton);
			shapeButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					editBehaviourController.setShapeCreationMode(shapeType);
					paletteGroup.setSelected(shapeButton.getModel(), true);
				}
				
			});
		}
		
	}

	private void createAnchorPane(final ButtonGroup paletteGroup, INotationSubsystem notationSubsystem, final IViewBehaviourController editBehaviourController){
		JPanel buttonPanel = new JPanel();
		this.anchorScrollPane = new JScrollPane(buttonPanel);
		this.anchorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		AnchorNodeIconGenerator iconGenerator = new AnchorNodeIconGenerator();
		Iterator<IAnchorNodeObjectType> shapeTypeIterator = notationSubsystem.getSyntaxService().anchorNodeTypeIterator();
		while(shapeTypeIterator.hasNext()){
			final IAnchorNodeObjectType shapeType = shapeTypeIterator.next();
			Dimension defSize = shapeType.getDefaultAttributes().getSize();
			double widToHRatio = defSize.getWidth()/defSize.getHeight();
			iconGenerator.setBounds(new Envelope(0, 0, PREF_ICON_HEIGHT*widToHRatio, PREF_ICON_HEIGHT));
			iconGenerator.setObjectType(shapeType);
			iconGenerator.generateImage();
			iconGenerator.generateIcon();
			final JButton nodeButton = new JButton(iconGenerator.getIcon());
			nodeButton.setText(shapeType.getName());
			nodeButton.setToolTipText(shapeType.getDescription());
//			shapeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			buttonPanel.add(nodeButton);
			paletteGroup.add(nodeButton);
			nodeButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					editBehaviourController.setAnchorNodeCreationMode(shapeType);
					paletteGroup.setSelected(nodeButton.getModel(), true);
				}
				
			});
		}
		
	}

    private ImageIcon createImageIcon(String path) {
        URL imgURL = this.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            throw new IllegalArgumentException("Couldn't find file: " + path);
        }
    }
}
