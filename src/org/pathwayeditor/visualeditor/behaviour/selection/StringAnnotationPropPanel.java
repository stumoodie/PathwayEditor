package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class StringAnnotationPropPanel extends JPanel {
	public static final String CURR_VALUE = "currValue";
	public static final String IS_VISUALISABLE = "isVisualisable";

	private static final long serialVersionUID = 1L;
	private static final int DEF_NUM_COLS = 20;
	private final JTextField propValueTextField = new JTextField();
	private IPlainTextAnnotationProperty annotProp;
	private String currStringValue;
	private JCheckBox isVisualisedCheckbox = new JCheckBox();

	public StringAnnotationPropPanel(IPlainTextAnnotationProperty annotProp) {
		super();
		this.annotProp = annotProp;
		currStringValue = this.annotProp.getValue();
		propValueTextField.setText(currStringValue);
		propValueTextField.setColumns(DEF_NUM_COLS);
		JLabel annotationLabel = new JLabel(annotProp.getDefinition().getDisplayName());
		JLabel isVisualisedLabel = new JLabel("Is Visualised");
		ICanvasElementAttribute element = (ICanvasElementAttribute) annotProp.getOwner().getCurrentElement().getAttribute();
		isVisualisedCheckbox.setSelected(element.getModel().hasLabelForProperty(annotProp));
		isVisualisedCheckbox.setEnabled(element.getModel().getNotationSubsystem().getSyntaxService().isVisualisableProperty(annotProp.getDefinition()));
		this.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridy = 0;
		this.add(annotationLabel, c1);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridwidth = 2;
		this.add(propValueTextField, c2);
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
		propValueTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentStringValue(propValueTextField.getText());
			}
		});
		propValueTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				setCurrentStringValue(propValueTextField.getText());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		propValueTextField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
					propValueTextField.setText(currStringValue);
				}
			}
		});
	}
	
	
	private void setVisualisationLabel(boolean isVisualised) {
		this.firePropertyChange(IS_VISUALISABLE, !isVisualised, isVisualised);
	}


	private void setCurrentStringValue(String newValue){
		String oldValue = this.currStringValue;
		if(!this.currStringValue.equals(newValue)){
			this.currStringValue = newValue;	
			this.firePropertyChange(CURR_VALUE, oldValue, this.currStringValue);
		}
	}
	
	
	public String getCurrentStringValue(){
		return this.currStringValue;
	}
	
	public IPlainTextAnnotationProperty getIPlainTextAnnotationProperty(){
		return this.annotProp;
	}
}
