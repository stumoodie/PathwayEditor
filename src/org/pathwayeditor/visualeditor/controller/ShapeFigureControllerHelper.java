package org.pathwayeditor.visualeditor.controller;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public class ShapeFigureControllerHelper implements IFigureControllerHelper {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapeAttribute attribute;
	private IFigureController figureController; 

	public ShapeFigureControllerHelper(IShapeAttribute nodeAttribute){
		this.attribute = nodeAttribute; 
	}

	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.controller.IFigureControllerFactory#createFigureController(org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute)
	 */
	@Override
	public void createFigureController(){
		figureController = new FigureController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		refreshBoundProperties();
		figureController.generateFigureDefinition();
	}
	
	@Override
	public IFigureController getFigureController(){
		return this.figureController;
	}
	
	@Override
	public void refreshBoundProperties() {
		assignBoundVariablesToProperties();
		figureController.generateFigureDefinition();
	}
	
	
	private void assignBoundVariablesToProperties(){
		for(final String varName : figureController.getBindVariableNames()){
			if(attribute.containsProperty(varName)){
				IAnnotationProperty prop = attribute.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					@Override
					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureController.setBindBoolean(varName, prop.getValue());
					}

					@Override
					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureController.setBindInteger(varName, prop.getValue());
					}

					@Override
					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					@Override
					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					@Override
					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.warn("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}


	@Override
	public void refreshGraphicalAttributes() {
		this.figureController.generateFigureDefinition();
	}


	@Override
	public void refreshAll() {
		this.refreshBoundProperties();
	}
}
