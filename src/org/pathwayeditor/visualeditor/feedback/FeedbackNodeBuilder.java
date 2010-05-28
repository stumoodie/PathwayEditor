package org.pathwayeditor.visualeditor.feedback;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;
import org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;

public class FeedbackNodeBuilder implements IFeedbackNodeBuilder {
	private final String DEFAULT_DEFINITION =
		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
		"0.0 xoffset 0.0 yoffset w h rect\n";
	private final String LABEL_DEFINITION =
		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
		"gsave\n" +
		"null setfillcol cpx xoffset cpy yoffset (C) card cvs text\n" +
		"grestore\n" +
		"} def\n" +
		"gsave\n" +
		":noborderFlag \n{" +
		"null setlinecol\n" +
		"} if\n" +
		"0.0 xoffset 0.0 yoffset w h rect\n" +
		"grestore\n" +
		"0.5 0.5 :labelFontSize :labelText cardinalityBox\n";

	private final Logger logger = Logger.getLogger(this.getClass());
	private final FeedbackModel feedbackModel;
	private static int idCount = 0;
	
	FeedbackNodeBuilder(FeedbackModel feedbackModel){
		this.feedbackModel = feedbackModel;
	}
	
	public IFeedbackModel getFeedbackModel(){
		return this.feedbackModel;
	}
	
	public IFeedbackNode createFromDrawingNodeObjectType(IShapeObjectType objectType, Envelope initialBounds){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(objectType.getDefaultAttributes().getShapeDefinition());
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		figureController.setRequestedEnvelope(initialBounds);
		IShapeAttributeDefaults attribute = objectType.getDefaultAttributes();
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToPropertyDefaults(attribute, figureController);
		figureController.generateFigureDefinition();
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureController, initialBounds);
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	
	private int nextCounter(){
		return idCount++;
	}
	
	public IFeedbackNode createFromDrawingNodeAttribute(IDrawingNodeAttribute nodeAttribute){
		IFigureController figureController = null;
		if(nodeAttribute instanceof IShapeAttribute){
			figureController = createShapeController((IShapeAttribute)nodeAttribute);
		}
		else if(nodeAttribute instanceof ILabelAttribute){
			figureController = createLabelController((ILabelAttribute)nodeAttribute);
		}
		else{
			throw new IllegalArgumentException("Cannot deal with node of unknown type: " + nodeAttribute);
		}
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureController, nodeAttribute.getBounds());
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	
	public IFeedbackNode createDefaultNode(Envelope initialBounds){
		IFigureController figureController = createDefaultController(initialBounds);
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureController, initialBounds);
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	

	private IFigureController createDefaultController(Envelope initialBounds){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(DEFAULT_DEFINITION);
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		figureController.setRequestedEnvelope(initialBounds);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private IFigureController createLabelController(ILabelAttribute attribute){
//		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(LABEL_DEFINITION);
//		compiler.compile();
//		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		IFigureController figureController = new FigureController(FigureCompilationCache.getInstance().lookup(LABEL_DEFINITION));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getBackgroundColor());
		figureController.setLineColour(attribute.getForegroundColor());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		figureController.setBindDouble("labelFontSize", 10.0);
		figureController.setBindString("labelText", attribute.getProperty().getValue().toString());
		figureController.setBindBoolean("noborderFlag", attribute.hasNoBorder());
		figureController.generateFigureDefinition();
		return figureController;
	}

	private IFigureController createShapeController(IShapeAttribute attribute){
//		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(attribute.getShapeDefinition());
//		compiler.compile();
//		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		IFigureController figureController = new FigureController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private void assignBindVariablesToProperties(IShapeAttribute att, final IFigureController figureController) {
		for(final String varName : figureController.getBindVariableNames()){
			if(att.containsProperty(varName)){
				IAnnotationProperty prop = att.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureController.setBindBoolean(varName, prop.getValue());
					}

					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureController.setBindInteger(varName, prop.getValue());
					}

					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}

	private void assignBindVariablesToPropertyDefaults(IShapeAttributeDefaults att, final IFigureController figureController) {
		for(final String varName : figureController.getBindVariableNames()){
			if(att.containsPropertyDefinition(varName)){
				IPropertyDefinition prop = att.getPropertyDefinition(varName);
				if(prop instanceof IBooleanPropertyDefinition){
					figureController.setBindBoolean(varName, ((IBooleanPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IIntegerPropertyDefinition){
					figureController.setBindInteger(varName, ((IIntegerPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IListAnnotationProperty){
					logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
				}
				else if(prop instanceof INumberAnnotationProperty){
					figureController.setBindDouble(varName, ((INumberPropertyDefinition)prop).getDefaultValue().doubleValue());
				}
				else if(prop instanceof IPlainTextPropertyDefinition){
					figureController.setBindString(varName, ((IPlainTextPropertyDefinition)prop).getDefaultValue());
				}
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}
}
