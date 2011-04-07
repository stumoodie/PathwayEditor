package org.pathwayeditor.visualeditor.commands;

import java.math.BigDecimal;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class ChangeAnnotationPropertyValue implements ICommand {
	private IAnnotationProperty property;
	private Object labelValue;
	private Object oldValue;

	public ChangeAnnotationPropertyValue(IAnnotationProperty prop, Object labelValue) {
		this.property = prop;
		this.labelValue = labelValue;
		this.oldValue = null;
	}

	@Override
	public void execute() {
		this.oldValue = this.property.getValue();
		this.redo();
	}

	@Override
	public void undo() {
		this.property.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)oldValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)oldValue);
			}
			
			@SuppressWarnings({ "unchecked" })
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				prop.getValue().addAll((List<? extends String>)oldValue);
			}
			
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				prop.setValue((Integer)oldValue);
			}
			
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				prop.setValue((Boolean)oldValue);
			}
		});
	}

	@Override
	public void redo() {
		this.property.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)labelValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)labelValue);
			}
			
			@SuppressWarnings({ "unchecked" })
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				prop.getValue().addAll((List<? extends String>)labelValue);
			}
			
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				prop.setValue((Integer)labelValue);
			}
			
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				prop.setValue((Boolean)labelValue);
			}
		});
	}

}
