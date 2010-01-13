package org.pathwayeditor.visualeditor;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;

import org.pathwayeditor.graphicsengine.CanvasDrawer;
import org.pathwayeditor.graphicsengine.ICanvasDrawer;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class ShapePane extends Canvas implements IShapePane {
	private static final long serialVersionUID = -7580080598416351849L;

	private final ISelectionRecord selections;
	private final ICanvasDrawer canvasDrawer;
	
	public ShapePane(IViewControllerStore geomFactory, ISelectionRecord selectionRecord){
		super();
		this.selections = selectionRecord;
		this.canvasDrawer = new CanvasDrawer(geomFactory);
	}
	
	
	
	@Override
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		this.canvasDrawer.paint(g2d);
		paintSelections(g2d);
	}



	private void paintSelections(Graphics2D g2d) {
		Iterator<ISelection> selectionIter = this.selections.selectedNodesIterator();
		while(selectionIter.hasNext()){
			ISelection node = selectionIter.next();
			SelectionShape selection = new SelectionShape(node);
			selection.paint(g2d);
//			Rectangle2D rect = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(), bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
//			g2d.setColor(Color.red);
//			g2d.draw(rect);
		}
	}



	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selections;
	}



	@Override
	public IViewControllerStore getViewModel() {
		return this.canvasDrawer.getViewControllerStore();
	}
}
