package org.pathwayeditor.visualeditor;

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotatedObject;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextPropertyDefinition;
import org.pathwayeditor.notationsubsystem.toolkit.definition.NumberPropertyDefinition;
import org.pathwayeditor.notationsubsystem.toolkit.definition.PlainTextPropertyDefinition;

public class LabelPropValueDialogTest extends JFrame implements ActionListener {
	private static final String NUMBER_DIALOG = "numberDialog";
	private static final String TEXT_DIALOG = "textDialog";
	private static final long serialVersionUID = 1L;
	private LabelPropValueDialog labelPropValueDialog;
	private LabelPropValueDialog numberLabelPropValueDialog;

	public LabelPropValueDialogTest(){
		super();
		this.setLayout(new GridLayout(2, 1));
		JButton textDialogButton = new JButton("Text Dialog");
		textDialogButton.addActionListener(this);
		textDialogButton.setActionCommand(TEXT_DIALOG);
		Dialog dialog = new Dialog(this);
		labelPropValueDialog = new LabelPropValueDialog(dialog);
		this.add(textDialogButton);
		JButton numberDialogButton = new JButton("Number Dialog");
		numberDialogButton.addActionListener(this);
		numberDialogButton.setActionCommand(NUMBER_DIALOG);
		numberLabelPropValueDialog = new LabelPropValueDialog(dialog);
		this.add(numberDialogButton);
		this.pack();
		this.setVisible(true);
	}
	
	
	private void setTextAnnotationProp(IAnnotationProperty prop) {
		labelPropValueDialog.setLabelController(prop);
	}

	private void setNumberAnnotationProp(IAnnotationProperty prop) {
		this.numberLabelPropValueDialog.setLabelController(prop);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object value = null;
		if(e.getActionCommand().equals(TEXT_DIALOG)){
			value = this.labelPropValueDialog.getLabelValue();
		}
		else if(e.getActionCommand().equals(NUMBER_DIALOG)){
			value = this.numberLabelPropValueDialog.getLabelValue();
		}
		System.out.println("Value=" + value);
	}

	public static final void main(String args[]){
		LabelPropValueDialogTest test = new LabelPropValueDialogTest();
		IPlainTextPropertyDefinition defn = new PlainTextPropertyDefinition("testTextProp", "foo");
		IAnnotationProperty prop = new StubTextAnnotationProperty(defn);
		test.setTextAnnotationProp(prop);
		INumberPropertyDefinition numDefn = new NumberPropertyDefinition("testTextProp", new BigDecimal(1.0));
		IAnnotationProperty numProp = new StubNumberAnnotationProperty(numDefn);
		test.setNumberAnnotationProp(numProp);
	}

	public static class StubTextAnnotationProperty implements IPlainTextAnnotationProperty {
		private IPlainTextPropertyDefinition propDefn;
		private String value;

		public StubTextAnnotationProperty(IPlainTextPropertyDefinition propDefn){
			this.propDefn = propDefn;
			this.value = propDefn.getDefaultValue();
		}
		
		@Override
		public IAnnotatedObject getOwner() {
			return null;
		}

		@Override
		public void visit(IAnnotationPropertyVisitor visitor) {
			visitor.visitPlainTextAnnotationProperty(this);
		}

		@Override
		public void addChangeListener(IAnnotationPropertyChangeListener listener) {
		}

		@Override
		public void removeChangeListener(IAnnotationPropertyChangeListener listener) {
		}

		@Override
		public Iterator<IAnnotationPropertyChangeListener> listenerIterator() {
			List<IAnnotationPropertyChangeListener> l = Collections.emptyList();
			return l.iterator();
		}

		@Override
		public IPlainTextPropertyDefinition getDefinition() {
			return this.propDefn;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public void setValue(String textValue) {
			this.value = textValue;
		}
		
	}

	public static class StubNumberAnnotationProperty implements INumberAnnotationProperty {
		private INumberPropertyDefinition propDefn;
		private BigDecimal value;

		public StubNumberAnnotationProperty(INumberPropertyDefinition propDefn){
			this.propDefn = propDefn;
			this.value = propDefn.getDefaultValue();
		}
		
		@Override
		public IAnnotatedObject getOwner() {
			return null;
		}

		@Override
		public void visit(IAnnotationPropertyVisitor visitor) {
			visitor.visitNumberAnnotationProperty(this);
		}

		@Override
		public void addChangeListener(IAnnotationPropertyChangeListener listener) {
		}

		@Override
		public void removeChangeListener(IAnnotationPropertyChangeListener listener) {
		}

		@Override
		public Iterator<IAnnotationPropertyChangeListener> listenerIterator() {
			List<IAnnotationPropertyChangeListener> l = Collections.emptyList();
			return l.iterator();
		}

		@Override
		public INumberPropertyDefinition getDefinition() {
			return this.propDefn;
		}

		@Override
		public BigDecimal getValue() {
			return this.value;
		}

		@Override
		public void setValue(BigDecimal textValue) {
			this.value = textValue;
		}
		
	}
}
