package org.pathwayeditor.visualeditor.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;

public abstract class AnnotationPropPanel<T> extends JPanel {
	public static final String CURR_VALUE = "currValue";
	public static final String IS_VISUALISABLE = "isVisualisable";

	private static final long serialVersionUID = 1L;
	private T currValue;
	private JCheckBox isVisualisedCheckbox = new JCheckBox();

	public AnnotationPropPanel(IAnnotationProperty annotProp) {
		super();
		JPanel valuePanel = getValuePanel();
		JLabel isVisualisedLabel = new JLabel("Is Visualised");
		ICanvasElementAttribute element = (ICanvasElementAttribute) annotProp.getOwner().getCurrentElement().getAttribute();
		isVisualisedCheckbox.setSelected(element.getModel().hasLabelForProperty(annotProp));
		isVisualisedCheckbox.setEnabled(element.getModel().getNotationSubsystem().getSyntaxService().isVisualisableProperty(annotProp.getDefinition()));
		this.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 3;
		this.add(valuePanel, c1);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 0;
		c3.gridy = 1;
		this.add(isVisualisedLabel, c3);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridx = 1;
		c4.gridy = 1;
		this.add(this.isVisualisedCheckbox, c4);
		this.isVisualisedCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisualisationLabel(isVisualisedCheckbox.isSelected());
			}
		});
	}
	
	
	protected abstract JPanel getValuePanel();


	protected final void setCurrentValue(T newValue){
		T oldValue = this.currValue;
		if(!this.currValue.equals(newValue)){
			this.currValue = newValue;	
			this.firePropertyChange(CURR_VALUE, oldValue, this.currValue);
		}
	}

	private void setVisualisationLabel(boolean isVisualised) {
		this.firePropertyChange(IS_VISUALISABLE, !isVisualised, isVisualised);
	}


	
	public T getCurrentValue(){
		return this.currValue;
	}
	
}
