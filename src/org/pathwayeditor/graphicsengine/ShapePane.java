package org.pathwayeditor.graphicsengine;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.pathwayeditor.figure.figuredefn.FigureDrawer;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.figuredefn.IGraphicsEngine;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class ShapePane extends Canvas implements IShapePane {
	private static final long serialVersionUID = -7580080598416351849L;

	private final IViewControllerStore geomFactory;
	private final ISelectionRecord selections;
	
	public ShapePane(IViewControllerStore geomFactory, ISelectionRecord selectionRecord){
		super();
		this.geomFactory = geomFactory;
		this.selections = selectionRecord;
	}
	
	
	
	@Override
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine((Graphics2D)g);
		Iterator<IShapeController> shapeIter = this.geomFactory.shapePrimitiveIterator();
		while(shapeIter.hasNext()){
			IShapeController shapeNode = shapeIter.next();
			IFigureController controller = shapeNode.getFigureController();
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		Iterator<ILabelController> labelIter = this.geomFactory.labelPrimitiveIterator();
		while(labelIter.hasNext()){
			ILabelController labelNode = labelIter.next();
			IFigureController controller = labelNode.getFigureController();
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		Iterator<ILinkController> linkEdgeIter = this.geomFactory.linkPrimitiveIterator();
		while(linkEdgeIter.hasNext()){
			ILinkController edge = linkEdgeIter.next();
			LinkDrawer linkDrawer = new LinkDrawer(edge);
			linkDrawer.paint(g2d);
		}
		paintSelections(g2d);
	}



	private void paintSelections(Graphics2D g2d) {
		Iterator<INodeController> selectionIter = this.selections.selectedNodesIterator();
		while(selectionIter.hasNext()){
			INodeController node = selectionIter.next();
			Envelope bounds = node.getConvexHull().getEnvelope();
			Rectangle2D rect = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(), bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
			g2d.setColor(Color.red);
			g2d.draw(rect);
		}
	}



	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selections;
	}



	@Override
	public IViewControllerStore getViewModel() {
		return this.geomFactory;
	}
}
