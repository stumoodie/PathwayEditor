package org.pathwayeditor.visualeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;

public class ShapePrimitive implements IShapePrimitive {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapeNode domainNode;
	private final ICanvasAttributePropertyChangeListener shapePropertyChangeListener;
	private final IAnnotationPropertyChangeListener annotPropChangeListener;
	private IFigureController figureController;
	private final List<INodePrimitiveChangeListener> listeners;
	private IViewModel viewModel;
	private final INodePrimitiveChangeListener nodePrimitivateChangeListener;
	private ICanvasAttributePropertyChangeListener parentDrawingNodePropertyChangeListener;
	
	public ShapePrimitive(IViewModel viewModel, IShapeNode node) {
		if(node.isRemoved()) throw new IllegalArgumentException("Node cannot be removed when creating a new drawing primitive");
		this.viewModel = viewModel;
		this.domainNode = node;
		this.listeners = new LinkedList<INodePrimitiveChangeListener>();
		shapePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					figureController.setLineColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					figureController.setFillColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					figureController.setLineWidth(newLineWidth);
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.SIZE)
						|| e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.setRequestedEnvelope(attribute.getBounds());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.setLineStyle(attribute.getLineStyle());
					figureController.generateFigureDefinition();
				}
			}
		};
		annotPropChangeListener = new IAnnotationPropertyChangeListener() {
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				IAnnotationProperty prop = e.getPropertyDefinition();
				IShapeNode node = ((IShapeAttribute)prop.getOwner()).getCurrentDrawingElement();
				assignBindVariablesToProperties(node.getAttribute(), figureController);
				figureController.generateFigureDefinition();
			}	
		};
		nodePrimitivateChangeListener = new INodePrimitiveChangeListener(){

			@Override
			public void nodeTranslated(INodePrimitiveTranslationEvent e) {
				translatePrimitive(e.getTranslationDelta());
			}
			
		};
		parentDrawingNodePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				// if parent moves then move by the same amount
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					Point oldPosition = (Point)e.getOldValue();
					Point newPosition = (Point)e.getNewValue();
					Point delta = oldPosition.difference(newPosition);
					Point currShapeLocation = domainNode.getAttribute().getLocation();
					domainNode.getAttribute().setLocation(currShapeLocation.translate(delta));
				}
			}
		};
		this.figureController = createController(domainNode);
	}

	public void activate(){
		addListeners(this.domainNode, figureController);
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

	private IFigureController createController(IShapeNode node){
		IShapeAttribute attribute = node.getAttribute();
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

	private void addListeners(final IShapeNode node, final IFigureController figureController) {
		final IShapeAttribute attribute = node.getAttribute();
		attribute.addChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.addChangeListener(annotPropChangeListener);
		}
		this.domainNode.getParentNode().getAttribute().addChangeListener(parentDrawingNodePropertyChangeListener);
		INodePrimitive parentNode = this.viewModel.getNodePrimitive(this.domainNode.getParentNode());
		parentNode.addNodePrimitiveChangeListener(this.nodePrimitivateChangeListener);
	}
	
	private void removeListeners() {
		final IShapeAttribute attribute = this.domainNode.getAttribute();
		attribute.removeChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.removeChangeListener(annotPropChangeListener);
		}
		this.domainNode.getParentNode().getAttribute().removeChangeListener(parentDrawingNodePropertyChangeListener);
		if(this.viewModel.containsDrawingElement(this.domainNode.getParentNode())){
			INodePrimitive parentNode = this.viewModel.getNodePrimitive(this.domainNode.getParentNode());
			parentNode.removeNodePrimitiveChangeListener(this.nodePrimitivateChangeListener);
		}
	}
		
	@Override
	public IShapeNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public IFigureController getFigureController() {
		return this.figureController;
	}

	@Override
	public Envelope getBounds() {
		return this.figureController.getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.figureController.getConvexHull();
	}

	@Override
	public void translatePrimitive(Point translation) {
		Envelope currBounds = this.domainNode.getAttribute().getBounds();
		figureController.setRequestedEnvelope(currBounds.translate(translation));
		figureController.generateFigureDefinition();
		notifyTranslation(translation);
	}

	@Override
	public void resyncToModel() {
		IShapeAttribute attribute = this.domainNode.getAttribute();
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
	}

	@Override
	public int compareTo(IDrawingPrimitive o) {
		Integer otherIndex = o.getDrawingElement().getAttribute().getCreationSerial();
		return Integer.valueOf(this.domainNode.getAttribute().getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	public void dispose() {
		removeListeners();
		// clear all listeners to this instance too.
		this.listeners.clear();
		this.viewModel = null;
		this.figureController = null;
		this.domainNode = null;
	}

	private void notifyTranslation(final Point delta){
		INodePrimitiveTranslationEvent e = new INodePrimitiveTranslationEvent(){

			@Override
			public INodePrimitive getChangedNode() {
				return ShapePrimitive.this;
			}

			@Override
			public Point getTranslationDelta() {
				return delta;
			}
			
		};
		for(INodePrimitiveChangeListener listener : this.listeners){
			listener.nodeTranslated(e);
		}
	}
	
	@Override
	public void addNodePrimitiveChangeListener(INodePrimitiveChangeListener listener) {
		this.listeners.add(listener);
		
	}

	@Override
	public List<INodePrimitiveChangeListener> getNodePrimitiveChangeListeners() {
		return new ArrayList<INodePrimitiveChangeListener>(this.listeners);
	}

	@Override
	public void removeNodePrimitiveChangeListener(INodePrimitiveChangeListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public IViewModel getViewModel() {
		return this.viewModel;
	}
}
