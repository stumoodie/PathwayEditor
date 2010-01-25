package org.pathwayeditor.visualeditor;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Iterator;

import org.pathwayeditor.figure.figuredefn.FigureDrawer;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.figuredefn.IGraphicsEngine;
import org.pathwayeditor.graphicsengine.Java2DGraphicsEngine;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;

public class FeedbackDrawer implements IFeedbackDrawer {
	private final IFeedbackModel feedbackModel;
	
	
	public FeedbackDrawer(IFeedbackModel viewControllerStore) {
		this.feedbackModel = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	public void paint(Graphics2D g2d){
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(g2d);
		Iterator<IFeedbackNode> shapeIter = this.feedbackModel.nodeIterator();
		final Composite original = g2d.getComposite();
		final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g2d.setComposite(alpha);
		while(shapeIter.hasNext()){
			IFeedbackNode shapeNode = shapeIter.next();
			IFigureController controller = shapeNode.getFigureController();
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		g2d.setComposite(original);
	}

	@Override
	public IFeedbackModel getFeedbackModel() {
		return this.feedbackModel;
	}
}
