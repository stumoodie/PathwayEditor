package org.pathwayeditor.visualeditor.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;

public class LabelPrimitive implements ILabelPrimitive {
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
	private final ILabelNode domainNode;
	private final ICanvasAttributePropertyChangeListener drawingNodePropertyChangeListener;
	private ICanvasAttributePropertyChangeListener parentDrawingNodePropertyChangeListener;
	private final List<INodePrimitiveChangeListener> listeners;
	private IViewModel viewModel;
	private INodePrimitiveChangeListener nodePrimitiveChangeListener;
	private IFigureController controller;
	
	public LabelPrimitive(IViewModel viewModel, ILabelNode node) {
		this.viewModel = viewModel;
		this.domainNode = node;
		this.listeners = new LinkedList<INodePrimitiveChangeListener>();
		drawingNodePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.SIZE)
						|| e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					IDrawingNodeAttribute attribute = (IDrawingNodeAttribute)e.getAttribute();
					controller.setRequestedEnvelope(attribute.getBounds());
				}
			}
		};
		this.nodePrimitiveChangeListener = new INodePrimitiveChangeListener(){

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
		this.controller = createController(node);
	}

	private IFigureController createController(ILabelNode node){
		ILabelAttribute attribute = node.getAttribute();
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

	@Override
	public ILabelNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public Envelope getBounds() {
		return this.controller.getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.controller.getConvexHull();
	}

	@Override
	public void translatePrimitive(Point translation) {
		Envelope currBounds = this.domainNode.getAttribute().getBounds();
		controller.setRequestedEnvelope(currBounds.translate(translation));
		controller.generateFigureDefinition();
		this.notifyTranslation(translation);
	}

	@Override
	public void resyncToModel() {
		this.controller.setRequestedEnvelope(domainNode.getAttribute().getBounds());
	}

	@Override
	public int compareTo(IDrawingPrimitive o) {
		Integer otherIndex = o.getDrawingElement().getAttribute().getCreationSerial();
		return Integer.valueOf(this.domainNode.getAttribute().getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	public void dispose() {
		this.domainNode.getAttribute().removeChangeListener(drawingNodePropertyChangeListener);
		this.domainNode.getParentNode().getAttribute().removeChangeListener(parentDrawingNodePropertyChangeListener);
		if(this.viewModel.containsDrawingElement(this.domainNode.getParentNode())){
			INodePrimitive parentNode = this.viewModel.getNodePrimitive(this.domainNode.getParentNode());
			parentNode.removeNodePrimitiveChangeListener(this.nodePrimitiveChangeListener);
		}
		this.listeners.clear();
		this.viewModel = null;
	}

	private void notifyTranslation(final Point delta){
		INodePrimitiveTranslationEvent e = new INodePrimitiveTranslationEvent(){

			@Override
			public INodePrimitive getChangedNode() {
				return LabelPrimitive.this;
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

	@Override
	public void activate() {
		this.domainNode.getAttribute().addChangeListener(this.drawingNodePropertyChangeListener);
		this.domainNode.getParentNode().getAttribute().addChangeListener(parentDrawingNodePropertyChangeListener);
		INodePrimitive parentNode = this.viewModel.getNodePrimitive(this.domainNode.getParentNode());
		parentNode.addNodePrimitiveChangeListener(this.nodePrimitiveChangeListener);
	}

	@Override
	public IFigureController getFigureController() {
		return this.controller;
	}
}
