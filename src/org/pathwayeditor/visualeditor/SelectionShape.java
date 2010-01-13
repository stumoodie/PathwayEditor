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
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class SelectionShape {
	private static final float HANDLE_LINE_WIDTH = 1.0f;
	private final ISelection selection;
	
	public SelectionShape(ISelection bounds){
		this.selection = bounds;
	}
	
	
	public void paint(Graphics2D g2d){
		g2d.setColor(Color.red);
		Envelope bounds = this.selection.getSelectionModel(SelectionRegion.Central).getBounds();
		Rectangle2D selectionShape = new Rectangle2D.Double(bounds.getOrigin().getX(), bounds.getOrigin().getY(),
				bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
		g2d.draw(selectionShape);
		drawCornerHandle(g2d, this.selection.getSelectionModel(SelectionRegion.NW));
		drawCornerHandle(g2d, this.selection.getSelectionModel(SelectionRegion.NE));
		drawCornerHandle(g2d, this.selection.getSelectionModel(SelectionRegion.SE));
		drawCornerHandle(g2d, this.selection.getSelectionModel(SelectionRegion.SW));
		drawMidLineHandle(g2d, this.selection.getSelectionModel(SelectionRegion.N));
		drawMidLineHandle(g2d, this.selection.getSelectionModel(SelectionRegion.E));
		drawMidLineHandle(g2d, this.selection.getSelectionModel(SelectionRegion.S));
		drawMidLineHandle(g2d, this.selection.getSelectionModel(SelectionRegion.W));
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
