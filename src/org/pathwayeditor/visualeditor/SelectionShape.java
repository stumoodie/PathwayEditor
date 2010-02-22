package org.pathwayeditor.visualeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionShape {
	private static final float HANDLE_LINE_WIDTH = 1.0f;
	private final ISelection selection;
	
	public SelectionShape(ISelection bounds){
		this.selection = bounds;
	}
	
	
	public void paint(Graphics2D g2d){
		g2d.setColor(Color.red);
		Envelope bounds = this.selection.getSelectionHandle(SelectionHandleType.Central).get(0).getBounds();
		Rectangle2D selectionShape = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(),
				bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
		g2d.draw(selectionShape);
		drawCornerHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.NW).get(0));
		drawCornerHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.NE).get(0));
		drawCornerHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.SE).get(0));
		drawCornerHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.SW).get(0));
		drawMidLineHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.N).get(0));
		drawMidLineHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.E).get(0));
		drawMidLineHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.S).get(0));
		drawMidLineHandle(g2d, this.selection.getSelectionHandle(SelectionHandleType.W).get(0));
	}
	
	/**
	 * Draw a corner handle at the given centre-point.
	 * @param x
	 * @param y
	 */
	private void drawMidLineHandle(Graphics2D g2d, ISelectionHandle handle){
		g2d.setColor(Color.black);
		Stroke stroke = new BasicStroke(HANDLE_LINE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		Envelope bounds = handle.getBounds();
		Rectangle2D cornerHandle = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(), bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
		g2d.draw(cornerHandle);
		g2d.setColor(Color.red);
		g2d.fill(cornerHandle);
	}

	/**
	 * Draw a corner handle at the given centre-point.
	 * @param x
	 * @param y
	 */
	private void drawCornerHandle(Graphics2D g2d, ISelectionHandle handle){
		g2d.setColor(Color.black);
		Stroke stroke = new BasicStroke(HANDLE_LINE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		Envelope bounds = handle.getBounds();
		Ellipse2D cornerHandle = new Ellipse2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(), bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
		g2d.draw(cornerHandle);
		g2d.setColor(Color.red);
		g2d.fill(cornerHandle);
	}
}
