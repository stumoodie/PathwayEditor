package org.pathwayeditor.visualeditor.feedback;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.figuredefn.IFigureRenderingController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface IFeedbackNode extends IFeedbackElement {

	/**
	 * Returns the bounds of the drawn node, which may be different from that of the underlying model.
	 * @return the envelope of the drawn node.
	 */
	Envelope getBounds();
	
	/**
	 * Returns the hull of the drawn node primitive. 
	 * @return the hull of the drawn node primitive.
	 */
	IConvexHull getConvexHull();
	
	/**
	 * Get the figure controller for this figure
	 * @return
	 */
	IFigureRenderingController getFigureController();

	void resizePrimitive(Point originDelta, Dimension sizeDelta);

	void addFeedbackNodeListener(IFeedbackNodeListener feedbackNodeListener);

	void removeFeedbackNodeListener(IFeedbackNodeListener feedbackNodeListener);

	List<IFeedbackNodeListener> getFeedbackNodeListeners();

	void setFillColour(RGB blue);

	void setLineColour(RGB red);

	void setLineStyle(LineStyle solid);

	void setLineWidth(double d);
}
