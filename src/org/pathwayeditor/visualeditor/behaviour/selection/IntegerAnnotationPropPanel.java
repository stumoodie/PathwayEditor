package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;

public class IntegerAnnotationPropPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String CURR_VALUE = "currValue";
	private static final int DEF_NUM_COLS = 20;
	private final JTextField propValueTextField = new JTextField();
	private IIntegerAnnotationProperty annotProp;
	private Integer currStringValue;

	public IntegerAnnotationPropPanel(IIntegerAnnotationProperty annotProp) {
		super();
		this.annotProp = annotProp;
		currStringValue = this.annotProp.getValue();
		propValueTextField.setText(currStringValue.toString());
		propValueTextField.setColumns(DEF_NUM_COLS);
		JLabel annotationLabel = new JLabel(annotProp.getDefinition().getDisplayName());
		this.add(annotationLabel);
		this.add(propValueTextField);
		propValueTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentValue(propValueTextField.getText());
			}
		});
		propValueTextField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				verifyTextField();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				verifyTextField();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				verifyTextField();
			}
		});
		propValueTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				setCurrentValue(propValueTextField.getText());
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
					propValueTextField.setText(currStringValue.toString());
				}
			}
		});
	}
	
	private void verifyTextField(){
		try{
			Integer.parseInt(propValueTextField.getText());
			propValueTextField.setBackground(Color.GREEN);
		}
		catch(NumberFormatException ex){
			propValueTextField.setBackground(Color.RED);
		}
	}
	
	
	private void setCurrentValue(String newText){
		try{
			Integer newValue = Integer.valueOf(newText);
			Integer oldValue = this.currStringValue;
			if(!this.currStringValue.equals(newValue)){
				this.currStringValue = newValue;	
				this.firePropertyChange(CURR_VALUE, oldValue, this.currStringValue);
			}
		}
		catch(NumberFormatException ex){
			// ignore current value and don't set it as new 
		}
	}
	
	
	public Integer getCurrentValue(){
		return this.currStringValue;
	}
	
	public IIntegerAnnotationProperty getAnnotationProperty(){
		return this.annotProp;
	}
}
