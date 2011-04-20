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

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class LabelPropValueDialog extends JDialog implements ActionListener, FocusListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	private static final String QUERY_CMD = "query";
	private static final String CLOSE_CMD = "close";
	private static final String TERM1_CMD = "term1";
//	private static final String TERM2_CMD = "term2";
//	private static final String CONF_SCORE_CMD = "confScore";
	private final JPanel queryPanel = new JPanel();
	private final JTextField nodeSearchTerm = new JTextField();
	private final JButton queryButton = new JButton("OK");
	private final JButton dismissButton = new JButton("Cancel");
	private IAnnotationProperty labelController;
	private Object finalValue;
	private Object term1;
	private InputVerifier formatterFactory;

	public LabelPropValueDialog(Dialog frame){
		super(frame, true);
		this.labelController = null;
		layoutQueryPanel();
		this.queryButton.addActionListener(this);
		this.queryButton.setActionCommand(QUERY_CMD);
		this.dismissButton.addActionListener(this);
		this.dismissButton.setActionCommand(CLOSE_CMD);
		this.add(queryPanel);
		this.pack();
	}

	public void setLabelController(final IAnnotationProperty annotationProp){
		this.labelController = annotationProp;
		annotationProp.visit(new IAnnotationPropertyVisitor() {
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				formatterFactory = new AbstractValidator(LabelPropValueDialog.this, nodeSearchTerm, "Validation failed") {
					
					@Override
					protected boolean validationCriteria(JComponent c) {
						return true;
					}
				};
			}
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				NumberFormatter numberFormat = new NumberFormatter();
				DecimalFormat f = (DecimalFormat)NumberFormat.getNumberInstance();
				f.setParseBigDecimal(true);
				numberFormat.setFormat(f);
				if(logger.isDebugEnabled()){
					logger.debug("Max fraction digits=" + f.getMaximumFractionDigits());
					logger.debug("Min fraction digits=" + f.getMinimumFractionDigits());
					logger.debug("Max integer digits=" + f.getMaximumIntegerDigits());
					logger.debug("Min integer digits=" + f.getMinimumIntegerDigits());
				}
//				formatterFactory = new DefaultFormatterFactory(numberFormat);
			}
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				
			}
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				
			}
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				
			}
		});
		this.nodeSearchTerm.setInputVerifier(formatterFactory);
		refreshView();
	}
	
	
	public IAnnotationProperty getLabelController(){
		return this.labelController;
	}
	
	private void layoutQueryPanel(){
		GridBagLayout gbl_queryPanel = new GridBagLayout();
		gbl_queryPanel.rowHeights = new int[]{0, 0, 0};
		this.queryPanel.setLayout(gbl_queryPanel);
		JLabel nodeSearchLabel = new JLabel("Property Value");
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		this.queryPanel.add(nodeSearchLabel, c);
		GridBagConstraints c_1 = new GridBagConstraints();
		c_1.fill = GridBagConstraints.HORIZONTAL;
		c_1.gridx = 1;
		c_1.gridy = 0;
		c_1.gridwidth = 2;
		this.queryPanel.add(this.nodeSearchTerm, c_1);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		this.queryPanel.add(queryButton, c);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		this.queryPanel.add(dismissButton, c);
		refreshView();
	}

	private void refreshView(){
		this.finalValue = null;
		Object val = null;
		if(this.labelController != null){
			val = this.labelController.getValue();
		}
//		refreshField(this.nodeSearchTerm, val);
	}

	private void refreshField(JFormattedTextField termField, Object term) {
//		String fieldValue = "";
//		if(term != null){
//			fieldValue = term.toString();
//		}
//		if(termField.getValue() == null || !termField.getValue().equals(fieldValue)){
//			termField.setValue(fieldValue);
//		}
		termField.setValue(term);
	}

	public Object getLabelValue(){
		this.setVisible(true);
		return this.finalValue;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(QUERY_CMD)){
			updateQueryData();
			this.finalValue = this.term1;
//			setVisible(false);
		}
		else if(e.getActionCommand().equals(CLOSE_CMD)){
			this.finalValue = null;
			setVisible(false);
		}
		else if(e.getActionCommand().equals(TERM1_CMD)){
			updateQueryData();
		}
	}

	private void updateQueryData(){
		updateTerm1();
	}
	
	private void updateTerm1() {
//		term1 = this.nodeSearchTerm.getValue();
	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		updateQueryData();
	}

}
