package org.pathwayeditor.visualeditor;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;

import org.pathwayeditor.graphicsengine.CanvasDrawer;
import org.pathwayeditor.graphicsengine.ICanvasDrawer;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class ShapePane extends Canvas implements IShapePane {
	private static final long serialVersionUID = -7580080598416351849L;

	private final ISelectionRecord selections;
	private final ICanvasDrawer canvasDrawer;
	private final IFeedbackDrawer feedbackDrawer;
	
	public ShapePane(IViewControllerStore geomFactory, ISelectionRecord selectionRecord, IFeedbackModel feedbackModel){
		super();
		this.selections = selectionRecord;
		this.canvasDrawer = new CanvasDrawer(geomFactory);
		this.feedbackDrawer = new FeedbackDrawer(feedbackModel);
	}
	
	@Override
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		this.canvasDrawer.paint(g2d);
		paintSelections(g2d);
		this.feedbackDrawer.paint(g2d);
	}

	private void paintSelections(Graphics2D g2d) {
		Iterator<INodeSelection> selectionIter = this.selections.selectedNodesIterator();
		while(selectionIter.hasNext()){
			INodeSelection node = selectionIter.next();
			SelectionShape selection = new SelectionShape(node);
			selection.paint(g2d);
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



	@Override
	public IFeedbackModel getFeedbackModel() {
		return this.feedbackDrawer.getFeedbackModel();
	}
}
