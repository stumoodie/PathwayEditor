package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeTranslationEvent;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;

public class LabelController extends NodeController implements ILabelController {
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
	private IDrawingNodeAttributeListener parentDrawingNodePropertyChangeListener;
	private INodePrimitiveChangeListener parentNodePrimitiveChangeListener;
	private IFigureController controller;
	
	public LabelController(IViewControllerStore viewModel, ILabelNode node) {
		super(viewModel);
		this.domainNode = node;
		drawingNodePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.SIZE)
						|| e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					IDrawingNodeAttribute attribute = (IDrawingNodeAttribute)e.getAttribute();
					controller.setRequestedEnvelope(attribute.getBounds());
				}
			}
		};
		this.parentNodePrimitiveChangeListener = new INodePrimitiveChangeListener(){

			@Override
			public void nodeTranslated(INodeTranslationEvent e) {
				translatePrimitive(e.getTranslationDelta());
			}

			@Override
			public void nodeResized(INodeResizeEvent e) {
			}
			
		};
		parentDrawingNodePropertyChangeListener = new IDrawingNodeAttributeListener() {
			
			@Override
			public void nodeTranslated(IDrawingNodeAttributeTranslationEvent e) {
				domainNode.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(IDrawingNodeAttributeResizedEvent e) {
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

//	@Override
//	public void redefinedSyncroniseToModel() {
//		this.controller.setRequestedEnvelope(domainNode.getAttribute().getBounds());
//	}

	@Override
	public int compareTo(IDrawingPrimitiveController o) {
		Integer otherIndex = o.getDrawingElement().getAttribute().getCreationSerial();
		return Integer.valueOf(this.domainNode.getAttribute().getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	protected void nodeDisposalHook() {
		this.domainNode.getAttribute().removeChangeListener(drawingNodePropertyChangeListener);
		this.domainNode.getParentNode().getAttribute().removeDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
		if(this.getViewModel().containsDrawingElement(this.domainNode.getParentNode())){
			INodeController parentNode = this.getViewModel().getNodePrimitive(this.domainNode.getParentNode());
			parentNode.removeNodePrimitiveChangeListener(this.parentNodePrimitiveChangeListener);
		}
	}


	@Override
	public void activate() {
		this.domainNode.getAttribute().addChangeListener(this.drawingNodePropertyChangeListener);
		this.domainNode.getParentNode().getAttribute().addDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
		INodeController parentNode = this.getViewModel().getNodePrimitive(this.domainNode.getParentNode());
		parentNode.addNodePrimitiveChangeListener(this.parentNodePrimitiveChangeListener);
	}

	@Override
	public IFigureController getFigureController() {
		return this.controller;
	}

	@Override
	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
		Envelope currBounds = this.domainNode.getAttribute().getBounds();
		controller.setRequestedEnvelope(currBounds.translate(originDelta).changeDimension(currBounds.getDimension().resize(resizeDelta.getWidth(), resizeDelta.getHeight())));
		controller.generateFigureDefinition();
		this.notifyResize(originDelta, resizeDelta);
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		Envelope newBounds = this.getBounds().resize(originDelta, resizeDelta);
		return (newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0);
	}
}
