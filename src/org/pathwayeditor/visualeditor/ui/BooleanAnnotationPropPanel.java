package org.pathwayeditor.visualeditor.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;

public class BooleanAnnotationPropPanel extends VisualisablePanel {
	private static final long serialVersionUID = 1L;
	public static final String CURR_VALUE = "currValue";
	private final JCheckBox propValueTextField = new JCheckBox();
	private IBooleanAnnotationProperty annotProp;
	private Boolean currStringValue;
	private JLabel annotationLabel;

	public BooleanAnnotationPropPanel(IBooleanAnnotationProperty annotProp) {
		super(annotProp);
		this.annotProp = annotProp;
		currStringValue = this.annotProp.getValue();
		propValueTextField.setSelected(currStringValue);
		annotationLabel = new JLabel(annotProp.getDefinition().getDisplayName());
		propValueTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentStringValue(propValueTextField.isSelected());
			}
		});
		this.layoutPanel();
	}
	
	
	private void setCurrentStringValue(Boolean newValue){
		Boolean oldValue = this.currStringValue;
		if(!this.currStringValue.equals(newValue)){
			this.currStringValue = newValue;	
			this.firePropertyChange(CURR_VALUE, oldValue, this.currStringValue);
		}
	}
	
	
	public Boolean getCurrentStringValue(){
		return this.currStringValue;
	}
	
	public IBooleanAnnotationProperty getIPlainTextAnnotationProperty(){
		return this.annotProp;
	}


	@Override
	protected Component getValueLabel() {
		return this.annotationLabel;
	}


	@Override
	protected Component getValueField() {
		return this.propValueTextField;
	}
}
