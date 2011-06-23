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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.visualeditor.commands.ChangeAnnotationPropertyCommand;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.LabelCreationCommand;
import org.pathwayeditor.visualeditor.commands.LabelDeletionCommand;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;
import org.pathwayeditor.visualeditor.layout.LabelPositionCalculator;

public class PropertyChangeDialog extends JDialog implements ActionListener, FocusListener {
	private static final long serialVersionUID = 1L;
	private static final String OK_CMD = "ok_cmd";
	private static final String CANCEL_CMD = "cancel_cmd";
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	private ILabelPositionCalculator labelPosCalc;
	private ICompoundCommand latestCommand;

	public PropertyChangeDialog(JFrame frame){
		super(frame, true);
		this.labelPosCalc = new LabelPositionCalculator();
		setTitle("Format Shape");
		this.setLayout(new BorderLayout());
		this.okButton.addActionListener(this);
		this.okButton.setActionCommand(OK_CMD);
		this.okButton.setEnabled(false);
		this.cancelButton.addActionListener(this);
		this.cancelButton.setActionCommand(CANCEL_CMD);
		this.buttonPanel.add(okButton);
		this.buttonPanel.add(cancelButton);
		this.add(tabbedPane, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.PAGE_END);
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
		this.setPreferredSize(new Dimension(400, 300));
		this.pack();
	}
	

	private void addCommand(IAnnotationProperty prop, Object newValue){
		latestCommand.addCommand(new ChangeAnnotationPropertyCommand(prop, newValue));
		this.okButton.setEnabled(true);
	}
	
	private void createLabelCommand(IPlainTextAnnotationProperty prop) {
		latestCommand.addCommand(new LabelCreationCommand(prop, this.labelPosCalc));
		this.okButton.setEnabled(true);
	}

	private void deleteLabelCommand(IPlainTextAnnotationProperty prop) {
		latestCommand.addCommand(new LabelDeletionCommand(prop));
		this.okButton.setEnabled(true);
	}

	public void setSelectedShape(IShapeController shape) {
		Iterator<IAnnotationProperty> iter = shape.getDrawingElement().getAttribute().propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty annotProp = iter.next();
			annotProp.visit(new IAnnotationPropertyVisitor() {
				
				@Override
				public void visitPlainTextAnnotationProperty(final IPlainTextAnnotationProperty prop) {
					StringAnnotationPropPanel panel = new StringAnnotationPropPanel(prop);
					tabbedPane.addTab(prop.getDefinition().getDisplayName(), panel);
					panel.addPropertyChangeListener(StringAnnotationPropPanel.CURR_VALUE, new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							addCommand(prop, evt.getNewValue());
						}
					});
					panel.addPropertyChangeListener(StringAnnotationPropPanel.IS_VISUALISABLE, new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							Boolean isVisualised = (Boolean)evt.getNewValue();
							if(isVisualised.booleanValue()){
								createLabelCommand(prop);
							}
							else{
								deleteLabelCommand(prop);
							}
						}

					});
				}
				
				@Override
				public void visitNumberAnnotationProperty(final INumberAnnotationProperty prop) {
					NumberAnnotationPropPanel panel = new NumberAnnotationPropPanel(prop);
					tabbedPane.addTab(prop.getDefinition().getDisplayName(), panel);
					panel.addPropertyChangeListener(NumberAnnotationPropPanel.CURR_VALUE, new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							addCommand(prop, evt.getNewValue());
						}
					});
				}
				
				@Override
				public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				}
				
				@Override
				public void visitIntegerAnnotationProperty(final IIntegerAnnotationProperty prop) {
					IntegerAnnotationPropPanel panel = new IntegerAnnotationPropPanel(prop);
					tabbedPane.addTab(prop.getDefinition().getDisplayName(), panel);
					panel.addPropertyChangeListener(IntegerAnnotationPropPanel.CURR_VALUE, new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							addCommand(prop, evt.getNewValue());
						}
					});
				}
				
				@Override
				public void visitBooleanAnnotationProperty(final IBooleanAnnotationProperty prop) {
					BooleanAnnotationPropPanel panel = new BooleanAnnotationPropPanel(prop);
					tabbedPane.addTab(prop.getDefinition().getDisplayName(), panel);
					panel.addPropertyChangeListener(BooleanAnnotationPropPanel.CURR_VALUE, new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							addCommand(prop, evt.getNewValue());
						}
					});
				}
			});
		}
		this.tabbedPane.validate();
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
			this.setVisible(false);
		}
		else if(e.getActionCommand().equals(CANCEL_CMD)){
			this.latestCommand = new CompoundCommand();
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
