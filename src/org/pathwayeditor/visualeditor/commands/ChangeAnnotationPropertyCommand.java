package org.pathwayeditor.visualeditor.commands;

import java.math.BigDecimal;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class ChangeAnnotationPropertyCommand implements ICommand {
	private final IAnnotationProperty prop;
	private final Object newValue;
	private Object oldValue;
	
	public ChangeAnnotationPropertyCommand(IAnnotationProperty prop, Object newValue) {
		this.prop = prop;
		this.newValue = newValue;
	}

	@Override
	public void execute() {
		this.oldValue = this.prop.getValue();
		redo();
	}

	@Override
	public void undo() {
		this.prop.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)oldValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)oldValue);
			}
			
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				throw new UnsupportedOperationException("Not used!");
				
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
		this.prop.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)newValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)newValue);
			}
			
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				throw new UnsupportedOperationException("Not used!");
				
			}
			
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				prop.setValue((Integer)newValue);
			}
			
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				prop.setValue((Boolean)newValue);
			}
		});
	}

}
