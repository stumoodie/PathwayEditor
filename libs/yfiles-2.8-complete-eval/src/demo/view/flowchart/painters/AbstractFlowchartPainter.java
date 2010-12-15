/****************************************************************************
 **
 ** This file is part of yFiles-2.8. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package demo.view.flowchart.painters;

import y.view.AbstractCustomNodePainter;
import y.view.NodeRealizer;
import y.view.GenericNodeRealizer;
import y.view.YRenderingHints;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.GradientPaint;

/**
 * Abstract painter class for flowchart symbols
 */
public abstract class
    AbstractFlowchartPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest, FlowchartRealizerConstants {
  protected Shape outline, innerShape;


  protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
    boolean useSelectionStyle = selected(context, graphics);
    graphics.setStroke(getLineStroke(context, useSelectionStyle));
    if (context instanceof GenericNodeRealizer) {
      paintShape((GenericNodeRealizer) context, graphics, sloppy, useSelectionStyle);
    }
  }

  /**
   * Paints the outline and the inside of the shape (if exists)
   * The method calls:
   * <ul>
   *  <li> {@link #updateOutline} </li>
   *  <li> {@link #updateInsideShape} </li>
   * </ul>
   * @param context the context node
   * @param graphics the graphics context to use
   * @param sloppy whether to draw the node sloppily
   * @param selected whether the node selected or not
  **/
  protected void paintShape(GenericNodeRealizer context, Graphics2D graphics, boolean sloppy, boolean selected) {
    updateOutline(context);
    updateInsideShape(context);

    Color fillColor1 = getFillColor(context, selected);
    Color fillColor2 = getFillColor2(context, selected);
    Color lineColor = getLineColor(context, selected);

    if (fillColor1 != null) {
      if (fillColor2 != null) {
        double x = context.getX();
        double y = context.getY();
        double width = context.getWidth();
        double heigh = context.getHeight();
        GradientPaint gp = new GradientPaint((float) x, (float) y, fillColor1, (float) x + (float) width,
            (float) y + (float) heigh, fillColor2);
        graphics.setPaint(gp);
      } else {
        graphics.setColor(fillColor1);
      }
      if (outline != null) {
        graphics.fill(outline);
      }
    }
    if (lineColor!=null){
      graphics.setColor(lineColor);
      if (outline != null){
        graphics.draw(outline);
      }
      if (innerShape!=null){
        graphics.draw(innerShape);
      }
    }
  }

  static boolean selected(final NodeRealizer context, final Graphics2D gfx) {
    return context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
  }

  /**
   * Gets the outline
   * @return The outline shape
   */
  Shape getOutline() {
    return outline;
  }
  /**
   * Gets the current inner shape
   * @return The inner shape
   */
  public Shape getInnerShape() {
    return innerShape;
  }

  /**
   * Called from {@link #paintShape(y.view.GenericNodeRealizer, java.awt.Graphics2D, boolean, boolean)}, in order to update the outline shape.
   * @param context
   */
  protected abstract void updateOutline(NodeRealizer context);

  /**
   * A callback method, called from {@link #paintShape(y.view.GenericNodeRealizer, java.awt.Graphics2D, boolean, boolean)}, in order to update the inner shape.
   * @param context The node context
   */
  protected void updateInsideShape(NodeRealizer context) {

  }

  public boolean contains(NodeRealizer context, double x, double y) {
    updateOutline(context);
    return outline != null && outline.contains(x, y);
  }
}
