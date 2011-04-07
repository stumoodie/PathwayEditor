package org.pathwayeditor.visualeditor.controller;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public abstract class CommonLabelController extends NodeController implements ILabelController {
	private static final String LABEL_TEXT = "labelText";
	private final Logger logger = Logger.getLogger(this.getClass());
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
	private final IFigureRenderingController controller;
	private boolean isActive;
	private final ICanvasAttributeChangeListener drawingNodePropertyChangeListener;
	private IAnnotationPropertyChangeListener propertyValueChangeListener;
	
	protected CommonLabelController(IViewControllerModel viewModel, ILabelNode node, int index) {
		super(viewModel, index);
		this.domainNode = node;
		this.isActive = false;
		this.controller = createController(node.getAttribute());
		drawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					Envelope oldDrawnBounds = getFigureController().getConvexHull().getEnvelope();
					IDrawingNodeAttribute attribute = (IDrawingNodeAttribute)e.getAttribute();
					getFigureController().setRequestedEnvelope(attribute.getBounds());
					getFigureController().generateFigureDefinition();
					notifyDrawnBoundsChanged(oldDrawnBounds, getFigureController().getConvexHull().getEnvelope());
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		propertyValueChangeListener = new IAnnotationPropertyChangeListener() {
			
			@Override
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				getFigureController().setBindString(LABEL_TEXT, domainNode.getAttribute().getDisplayedContent());
				getFigureController().generateFigureDefinition();
			}
		};
	}

	private IFigureRenderingController createController(ILabelAttribute attribute){
		IFigureRenderingController figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(LABEL_DEFINITION));
		figureRenderingController.setRequestedEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getBackgroundColor());
		figureRenderingController.setLineColour(attribute.getForegroundColor());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		figureRenderingController.setBindDouble("labelFontSize", 10.0);
		figureRenderingController.setBindString(LABEL_TEXT, attribute.getDisplayedContent());
		figureRenderingController.setBindBoolean("noborderFlag", attribute.hasNoBorder());
		figureRenderingController.generateFigureDefinition();
		return figureRenderingController;
	}

	@Override
	public final ILabelNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public final Envelope getBounds() {
		return this.controller.getRequestedEnvelope();
	}

	@Override
	public final IConvexHull getConvexHull() {
		return this.controller.getConvexHull();
	}

	@Override
	public void inactivate() {
		this.getDrawingElement().getAttribute().removeChangeListener(drawingNodePropertyChangeListener);
		this.getDrawingElement().getAttribute().getProperty().removeChangeListener(propertyValueChangeListener);
		inactivateOverride();
		this.isActive = false;
	}


	protected abstract void inactivateOverride();

	@Override
	public final void activate() {
		this.getDrawingElement().getAttribute().addChangeListener(this.drawingNodePropertyChangeListener);
		this.getDrawingElement().getAttribute().getProperty().addChangeListener(propertyValueChangeListener);
		activateOverride();
		this.isActive = true;
	}

	protected abstract void activateOverride();

	@Override
	public final IFigureRenderingController getFigureController() {
		return this.controller;
	}

	@Override
	public final boolean canResize(Point originDelta, Dimension resizeDelta) {
		Envelope newBounds = this.getBounds().resize(originDelta, resizeDelta);
		return (newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0);
	}
	
	@Override
	public final boolean isActive() {
		return this.isActive;
	}

	@Override
	public final Envelope getDrawnBounds() {
		return this.controller.getEnvelope();
	}

	@Override
	public final boolean containsPoint(Point p) {
		IConvexHull attributeHull = this.getConvexHull();
		boolean retVal = attributeHull.containsPoint(p); 
		if(logger.isTraceEnabled()){
			logger.trace("Testing contains node:" + this + ",retVal=" + retVal + ", hull=" + attributeHull + ", point=" + p);
		}
		return retVal;
	}

	@Override
	public final boolean intersectsHull(IConvexHull queryHull) {
		return this.controller.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public final boolean intersectsBounds(Envelope drawnBounds) {
		IConvexHull otherHull = new RectangleHull(drawnBounds);
		return intersectsHull(otherHull);
	}
}
