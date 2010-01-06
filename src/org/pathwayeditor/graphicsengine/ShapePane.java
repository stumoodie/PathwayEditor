package org.pathwayeditor.graphicsengine;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.figuredefn.FigureDrawer;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.figuredefn.IFigureGeometryFactory;
import org.pathwayeditor.figure.figuredefn.IGraphicsEngine;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class ShapePane extends Canvas {
	private static final long serialVersionUID = -7580080598416351849L;

	private final ICanvas boCanvas;
	private final IFigureGeometryFactory geomFactory;
	private final ISelectionRecord selections;
	
	public ShapePane(IFigureGeometryFactory geomFactory, ICanvas boCanvas, ISelectionRecord selectionRecord){
		super();
		this.geomFactory = geomFactory;
		this.boCanvas = boCanvas;
		this.selections = selectionRecord;
	}
	
	
	
	@Override
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine((Graphics2D)g);
		Iterator<IShapeNode> shapeIter = this.boCanvas.getModel().shapeNodeIterator();
		while(shapeIter.hasNext()){
			IShapeNode shapeNode = shapeIter.next();
			IFigureController controller = geomFactory.getFigureController(shapeNode);
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		Iterator<ILinkEdge> linkEdgeIter = this.boCanvas.getModel().linkEdgeIterator();
		while(linkEdgeIter.hasNext()){
			ILinkEdge edge = linkEdgeIter.next();
			LinkDrawer linkDrawer = new LinkDrawer(edge);
			linkDrawer.paint(g2d);
		}
		paintSelections(g2d);
	}



	private void paintSelections(Graphics2D g2d) {
		Iterator<IDrawingElement> selectionIter = this.selections.selectionIterator();
		while(selectionIter.hasNext()){
			IDrawingNode node = (IDrawingNode)selectionIter.next();
			Envelope bounds = node.getAttribute().getBounds();
			Rectangle2D rect = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(), bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
			g2d.setColor(Color.red);
			g2d.draw(rect);
		}
	}
}
