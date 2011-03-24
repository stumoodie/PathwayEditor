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
import org.pathwayeditor.figure.definition.FigureDefinitionCompiler;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public class FeedbackNodeBuilder implements IFeedbackNodeBuilder {
	private final String DEFAULT_DEFINITION =
		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
		"0.0 xoffset 0.0 yoffset w h rect\n" +
		"(C) setanchor\n";
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
	
	@Override
	public IFeedbackModel getFeedbackModel(){
		return this.feedbackModel;
	}
	
	@Override
	public IFeedbackNode createFromDrawingNodeObjectType(IShapeObjectType objectType, Envelope initialBounds){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(objectType.getDefaultAttributes().getShapeDefinition());
		compiler.compile();
		IFigureRenderingController figureRenderingController = new FigureRenderingController(compiler.getCompiledFigureDefinition());
		figureRenderingController.setRequestedEnvelope(initialBounds);
		IShapeAttributeDefaults attribute = objectType.getDefaultAttributes();
		figureRenderingController.setFillColour(attribute.getFillColour());
		figureRenderingController.setLineColour(attribute.getLineColour());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToPropertyDefaults(attribute, figureRenderingController);
		figureRenderingController.generateFigureDefinition();
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureRenderingController, initialBounds);
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	
	private int nextCounter(){
		return idCount++;
	}
	
	@Override
	public IFeedbackNode createFromDrawingNodeAttribute(IDrawingNodeAttribute nodeAttribute){
		IFigureRenderingController figureRenderingController = null;
		if(nodeAttribute instanceof IShapeAttribute){
			figureRenderingController = createShapeController((IShapeAttribute)nodeAttribute);
		}
		else if(nodeAttribute instanceof ILabelAttribute){
			figureRenderingController = createLabelController((ILabelAttribute)nodeAttribute);
		}
		else{
			throw new IllegalArgumentException("Cannot deal with node of unknown type: " + nodeAttribute);
		}
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureRenderingController, nodeAttribute.getBounds());
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	
	@Override
	public IFeedbackNode createDefaultNode(Envelope initialBounds){
		IFigureRenderingController figureRenderingController = createDefaultController(initialBounds);
		FeedbackNode retVal = new FeedbackNode(nextCounter(), figureRenderingController, initialBounds);
		this.feedbackModel.addNode(retVal);
		return retVal;
	}
	

	private IFigureRenderingController createDefaultController(Envelope initialBounds){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(DEFAULT_DEFINITION);
		compiler.compile();
		IFigureRenderingController figureRenderingController = new FigureRenderingController(compiler.getCompiledFigureDefinition());
		figureRenderingController.setRequestedEnvelope(initialBounds);
		figureRenderingController.generateFigureDefinition();
		return figureRenderingController;
	}

	private IFigureRenderingController createLabelController(ILabelAttribute attribute){
		IFigureRenderingController figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(LABEL_DEFINITION));
		figureRenderingController.setRequestedEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getBackgroundColor());
		figureRenderingController.setLineColour(attribute.getForegroundColor());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		figureRenderingController.setBindDouble("labelFontSize", 10.0);
		figureRenderingController.setBindString("labelText", attribute.getDisplayedContent());
		figureRenderingController.setBindBoolean("noborderFlag", attribute.hasNoBorder());
		figureRenderingController.generateFigureDefinition();
		return figureRenderingController;
	}

	private IFigureRenderingController createShapeController(IShapeAttribute attribute){
		IFigureRenderingController figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureRenderingController.setRequestedEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getFillColour());
		figureRenderingController.setLineColour(attribute.getLineColour());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureRenderingController);
		figureRenderingController.generateFigureDefinition();
		return figureRenderingController;
	}

	private void assignBindVariablesToProperties(IShapeAttribute att, final IFigureRenderingController figureRenderingController) {
		for(final String varName : figureRenderingController.getBindVariableNames()){
			if(att.containsProperty(varName)){
				IAnnotationProperty prop = att.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					@Override
					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureRenderingController.setBindBoolean(varName, prop.getValue());
					}

					@Override
					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureRenderingController.setBindInteger(varName, prop.getValue());
					}

					@Override
					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					@Override
					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureRenderingController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					@Override
					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureRenderingController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}

	private void assignBindVariablesToPropertyDefaults(IShapeAttributeDefaults att, final IFigureRenderingController figureRenderingController) {
		for(final String varName : figureRenderingController.getBindVariableNames()){
			if(att.containsPropertyDefinition(varName)){
				IPropertyDefinition prop = att.getPropertyDefinition(varName);
				if(prop instanceof IBooleanPropertyDefinition){
					figureRenderingController.setBindBoolean(varName, ((IBooleanPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IIntegerPropertyDefinition){
					figureRenderingController.setBindInteger(varName, ((IIntegerPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IListAnnotationProperty){
					logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
				}
				else if(prop instanceof INumberAnnotationProperty){
					figureRenderingController.setBindDouble(varName, ((INumberPropertyDefinition)prop).getDefaultValue().doubleValue());
				}
				else if(prop instanceof IPlainTextPropertyDefinition){
					figureRenderingController.setBindString(varName, ((IPlainTextPropertyDefinition)prop).getDefaultValue());
				}
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}
}
