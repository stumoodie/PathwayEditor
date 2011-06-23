package org.pathwayeditor.visualeditor.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;

public abstract class VisualisablePanel extends JPanel {
	public static final String IS_VISUALISABLE = "isVisualisable";
	private static final long serialVersionUID = 1L;

	private JCheckBox isVisualisedCheckbox = new JCheckBox();
	private JLabel isVisualisedLabel;
	
	public VisualisablePanel(IAnnotationProperty annotProp){
		isVisualisedLabel = new JLabel("Is Visualised");
		isVisualisedLabel.setLabelFor(isVisualisedCheckbox);
		ICanvasElementAttribute element = (ICanvasElementAttribute) annotProp.getOwner().getCurrentElement().getAttribute();
		isVisualisedCheckbox.setSelected(element.getModel().hasLabelForProperty(annotProp));
		isVisualisedCheckbox.setEnabled(element.getModel().getNotationSubsystem().getSyntaxService().isVisualisableProperty(annotProp.getDefinition()));
	}


	protected void layoutPanel(){
		this.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
//		c1.anchor = GridBagConstraints.LINE_START;
		this.add(getValueLabel(), c1);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridwidth = 2;
//		c2.anchor = GridBagConstraints.LINE_START;
		this.add(getValueField(), c2);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 0;
		c3.gridy = 1;
		c3.gridwidth = 2;
		c3.anchor = GridBagConstraints.LINE_START;
		this.add(isVisualisedLabel, c3);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridx = 2;
		c4.gridy = 1;
		c4.anchor = GridBagConstraints.LINE_START;
		this.add(this.isVisualisedCheckbox, c4);
		this.isVisualisedCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisualisationLabel(isVisualisedCheckbox.isSelected());
			}
		});
	}
	
	protected abstract Component getValueLabel();

	protected abstract Component getValueField();


	private void setVisualisationLabel(boolean isVisualised) {
		this.firePropertyChange(IS_VISUALISABLE, !isVisualised, isVisualised);
	}
}
