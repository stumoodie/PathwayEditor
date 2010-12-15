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
import y.view.Graph2D;
import y.view.EdgeRealizer;
import y.base.Edge;
import y.base.Node;
import y.view.YRenderingHints;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


/**
 * This class is an implementation of {@link GenericNodeRealizer.Painter} that draws the annotation symbol of flowchart diagrams.
 */
public class FlowchartAnnotationPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest, FlowchartRealizerConstants {
  private GeneralPath shape;
  private Rectangle2D outline;


  public FlowchartAnnotationPainter() {
    outline = new Rectangle2D.Double();
    shape = new GeneralPath();
  }

  /**
   * Callback method to be overwritten by subclasses called by {@link #paint(y.view.NodeRealizer , java.awt.Graphics2D)}.
   * This method is responsible for drawing the actual node only - neither the hotspots, nor the labels, should be
   * painted by this method.
   * Furthermore the method calls:
   * <ul>
   *  <li> {@link #updateShape} </li>
   *  <li> {@link #updateOutline} </li>
   * </ul>
   * @param context  the context node
   * @param graphics the graphics context to use
   * @param sloppy   whether to draw the node sloppily
   * @see #initializeLine(y.view.NodeRealizer , java.awt.Graphics2D)
   * @see #initializeFill(y.view.NodeRealizer , java.awt.Graphics2D)
   */
  protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
    updateShape(context);
    updateOutline(context);
    final boolean useSelectionStyle = selected(context, graphics);
    graphics.setStroke(getLineStroke(context, useSelectionStyle));
    Color fillColor1 = getFillColor(context, useSelectionStyle);
    Color fillColor2 = getFillColor2(context, useSelectionStyle);
    Color lineColor = getLineColor(context, useSelectionStyle);

    if (fillColor1 != null) {
      if (fillColor2 != null) {
        double x = context.getX();
        double y = context.getY();
        double width = context.getWidth();
        double height = context.getHeight();
        GradientPaint gp = new GradientPaint((float) x, (float) y, fillColor1, (float) x + (float) width,
            (float) y + (float) height, fillColor2);
        graphics.setPaint(gp);
      } else {
        graphics.setColor(fillColor1);
      }
      graphics.fill(outline);
      graphics.setColor(lineColor);
      graphics.setStroke(new BasicStroke(2));
      graphics.draw(shape);
    }
  }

  static boolean selected(final NodeRealizer context, final Graphics2D gfx) {
    return context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
  }
  /**
   * Called from {@link #paintNode(y.view.NodeRealizer, java.awt.Graphics2D, boolean)}, in order to update the outline shape.
   * @param context
   */
  protected void updateOutline(NodeRealizer context) {
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double height = context.getHeight();
    outline.setFrame(x, y, width, height);
  }
    /**
   * A callback method, called from {@link #paintNode(y.view.NodeRealizer, java.awt.Graphics2D, boolean)}, in order to update the node shape.
   * @param context The node context
   */
  protected void updateShape(NodeRealizer context) {
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double hight = context.getHeight();
    byte orientation;
    shape.reset();
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    if (cast_gnr.getStyleProperty(PROPERTY_ORIENTATION) != null) {
      orientation = ((Byte) cast_gnr.getStyleProperty(PROPERTY_ORIENTATION)).byteValue();
    } else {
      orientation = PROPERTY_ORIENTATION_VALUE_LEFT;
    }
    if (orientation == PROPERTY_ORIENTATION_VALUE_AUTO) {
      // draw automatically
      switch (drawingMode(context)) {
        case 1: {
          drawDown(x, y, width, hight);
          break;
        }
        case 2: {
          drawRight(x, y, width, hight);
          break;
        }
        case 3: {
          drawTop(x, y, width, hight);
          break;
        }
        case 4: {
          drawLeft(x, y, width, hight);
          break;
        }
        default: {
          drawLeft(x, y, width, hight);
        }
      }
    } else {
      // draw manually
      switch (orientation) {
        //PROPERTY_ORIENTATION_VALUE_DOWN
        case 1: {
          drawDown(x, y, width, hight);
          break;
        }
        //PROPERTY_ORIENTATION_VALUE_RIGHT
        case 2: {
          drawRight(x, y, width, hight);
          break;
        }
        //PROPERTY_ORIENTATION_VALUE_TOP
        case 3: {
          drawTop(x, y, width, hight);
          break;
        }
        //PROPERTY_ORIENTATION_VALUE_LEFT
        case 4: {
          drawLeft(x, y, width, hight);
          break;
        }
        //PROPERTY_ORIENTATION_VALUE_LEFT
        default: {
          drawLeft(x, y, width, hight);
        }
      }
    }
  }

  private void drawLeft(double x, double y, double width, double height) {
    shape.moveTo((float)(x + 0.125 * width), (float) y);
    shape.lineTo((float)x, (float)y);
    shape.lineTo((float)x, (float)(y + height));
    shape.lineTo((float)(x + 0.125 * width), (float)(y + height));
  }

  private void drawRight(double x, double y, double width, double height) {
    shape.moveTo((float)(x + 0.875 * width),(float) y);
    shape.lineTo((float)(x + width), (float)y);
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)(x + 0.875 * width), (float)(y + height));
  }

  private void drawTop(double x, double y, double width, double height) {
    shape.moveTo((float)x, (float)(y + 0.125 * height));
    shape.lineTo((float)x, (float)y);
    shape.lineTo((float)(x + width), (float)y);
    shape.lineTo((float)(x + width), (float)(y + 0.125 * height));
  }

  private void drawDown(double x, double y, double width, double height) {
    shape.moveTo((float)x, (float)(y + 0.875 * height));
    shape.lineTo((float)x, (float)(y + height));
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)(x + width), (float)(y + 0.875 * height));
  }

  /*returns draw direction:
   1 = down  _
   2 = right ]
   3 = top   -
   4 = left  [
  */

  private byte drawingMode(NodeRealizer context) {
    int numberOfEdges = 0;
    final Node node = context.getNode();
    if (node != null) {
      numberOfEdges = node.degree();
    }

    if (numberOfEdges == 1) {

      // looks, how to draw depending on edge coords
      Edge edge = node.inDegree() == 1 ? node.firstInEdge() : node.firstOutEdge();
      Graph2D edgeGraph = (Graph2D) edge.getGraph();
      EdgeRealizer edgeRealizer = edgeGraph.getRealizer(edge);
      //initiate angle def. area
      double x = context.getX();
      double y = context.getY();
      double width = context.getWidth();
      double height = context.getHeight();

      double x_intersec;
      double y_intersec;
      if (edge.source() == node) {
        x_intersec = edgeRealizer.getSourceIntersection().getX();
        y_intersec = edgeRealizer.getSourceIntersection().getY();
      } else {
        x_intersec = edgeRealizer.getTargetIntersection().getX();
        y_intersec = edgeRealizer.getTargetIntersection().getY();
      }

      double epsilon = 0.1;
      //draw left
      if ((x_intersec + epsilon) > x && (x_intersec - epsilon) < x) {
        return 4;
      } else {
        // draw right
        if (((x_intersec + epsilon) > (x + width) && ((x_intersec - epsilon) < (x + width)))) {
          return 2;
        } else {
          //draw top
          if ((y_intersec + epsilon) > y && (y_intersec - epsilon) < y) {
            return 3;
          } else {
            //draw down
            if (((y_intersec + epsilon) > (y + height) && ((y_intersec - epsilon) < (y + height)))) {
              return 1;
            }
          }
        }
      }
    }
    //default draw left
    return 4;
  }

  /** Used as a callback for the {@link y.view.GenericNodeRealizer#contains(double, double)} method. */
  public boolean contains(NodeRealizer context, double x, double y) {
    updateOutline(context);
    return outline != null && outline.contains(x, y);
  }
}
