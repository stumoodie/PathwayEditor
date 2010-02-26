package org.pathwayeditor.visualeditor.feedback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;

public class FeedbackNode implements IFeedbackNode {
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
	private IFigureController figureController;
	private final Envelope initialBounds;
	private final int elementIdentifier;
	private final List<IFeedbackNodeListener> listeners;

	public FeedbackNode(IDrawingNodeAttribute nodeAttribute){
		this.listeners = new LinkedList<IFeedbackNodeListener>();
		this.elementIdentifier = nodeAttribute.getCreationSerial();
		this.initialBounds = nodeAttribute.getBounds();
		if(nodeAttribute instanceof IShapeAttribute){
			this.figureController = createShapeController((IShapeAttribute)nodeAttribute);
		}
		else if(nodeAttribute instanceof ILabelAttribute){
			this.figureController = createLabelController((ILabelAttribute)nodeAttribute);
		}
		else{
			throw new IllegalArgumentException("Cannot deal with node of unknown type: " + nodeAttribute);
		}
	}
	
	public FeedbackNode(int uniqueId, Envelope initialBounds){
		this.listeners = new LinkedList<IFeedbackNodeListener>();
		this.elementIdentifier = uniqueId;
		this.initialBounds = initialBounds;
		this.figureController = createDefaultController();
	}
	
	public int getElementIdentifier() {
		return elementIdentifier;
	}

	private IFigureController createDefaultController(){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(DEFAULT_DEFINITION);
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		figureController.setRequestedEnvelope(this.initialBounds);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private IFigureController createLabelController(ILabelAttribute attribute){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(LABEL_DEFINITION);
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
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
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(attribute.getShapeDefinition());
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
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

	@Override
	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
		figureController.setRequestedEnvelope(this.initialBounds.resize(originDelta, resizeDelta));
		figureController.generateFigureDefinition();
		notifyResize(this.initialBounds, originDelta, resizeDelta);
	}

	private void notifyResize(final Envelope initialBounds, final Point originDelta, final Dimension resizeDelta) {
		IFeedbackNodeResizeEvent e = new IFeedbackNodeResizeEvent() {
			
			@Override
			public Dimension getSizeDelta() {
				return resizeDelta;
			}
			
			@Override
			public Envelope getOriginalBounds() {
				return initialBounds;
			}
			
			@Override
			public Point getOriginDelta() {
				return originDelta;
			}
			
			@Override
			public IFeedbackNode getNode() {
				return FeedbackNode.this;
			}
		};
		notifyResizeEvent(e);
	}

	@Override
	public void translatePrimitive(Point translation) {
		figureController.setRequestedEnvelope(this.initialBounds.translate(translation));
		figureController.generateFigureDefinition();
		notifyTranslation(this.initialBounds, translation);
	}

	private void notifyTranslation(final Envelope oldBounds, final Point translation) {
		IFeedbackNodeTranslationEvent e = new IFeedbackNodeTranslationEvent() {
			
			@Override
			public IFeedbackNode getNode() {
				return FeedbackNode.this;
			}

			@Override
			public Point getTranslation() {
				return translation;
			}

			@Override
			public Envelope oldBounds() {
				return oldBounds;
			}
		};
		notifyTranslationEvent(e);
	}

	private void notifyTranslationEvent(IFeedbackNodeTranslationEvent e) {
		for(IFeedbackNodeListener l : this.listeners){
			l.nodeTranslationEvent(e);
		}
	}

	private void notifyResizeEvent(IFeedbackNodeResizeEvent e) {
		for(IFeedbackNodeListener l : this.listeners){
			l.nodeResizeEvent(e);
		}
	}

	@Override
	public Envelope getBounds() {
		return this.getFigureController().getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.getFigureController().getConvexHull();
	}

	@Override
	public IFigureController getFigureController() {
		return this.figureController;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementIdentifier;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FeedbackNode))
			return false;
		FeedbackNode other = (FeedbackNode) obj;
		if (elementIdentifier != other.elementIdentifier)
			return false;
		return true;
	}

	@Override
	public void addFeedbackNodeListener(IFeedbackNodeListener srcFeedbackNodeListener) {
		this.listeners.add(srcFeedbackNodeListener);
	}

	@Override
	public void removeFeedbackNodeListener(IFeedbackNodeListener srcFeedbackNodeListener) {
		this.listeners.remove(srcFeedbackNodeListener);
	}

	@Override
	public List<IFeedbackNodeListener> getFeedbackNodeListeners() {
		return new ArrayList<IFeedbackNodeListener>(this.listeners);
	}

	@Override
	public void setFillColour(RGB colour) {
		this.figureController.setFillColour(colour);
		this.figureController.generateFigureDefinition();
	}

	@Override
	public void setLineColour(RGB colour) {
		this.figureController.setLineColour(colour);
		this.figureController.generateFigureDefinition();
	}

	@Override
	public void setLineStyle(LineStyle lineStyle) {
		this.figureController.setLineStyle(lineStyle);
		this.figureController.generateFigureDefinition();
	}

	@Override
	public void setLineWidth(double lineWidth) {
		this.figureController.setLineWidth(lineWidth);
		this.figureController.generateFigureDefinition();
	}

}
