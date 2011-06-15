package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class StringAnnotationPropPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String CURR_VALUE = "currValue";
	private static final int DEF_NUM_COLS = 30;
	private final JTextField propValueTextField = new JTextField();
	private IPlainTextAnnotationProperty annotProp;
	private String currStringValue;

	public StringAnnotationPropPanel(IPlainTextAnnotationProperty annotProp) {
		super();
		this.annotProp = annotProp;
		currStringValue = this.annotProp.getValue();
		propValueTextField.setText(currStringValue);
		propValueTextField.setColumns(DEF_NUM_COLS);
		JLabel annotationLabel = new JLabel(annotProp.getDefinition().getDisplayName());
		this.add(annotationLabel);
		this.add(propValueTextField);
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
