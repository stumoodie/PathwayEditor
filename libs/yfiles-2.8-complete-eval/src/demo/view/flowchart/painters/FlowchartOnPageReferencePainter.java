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

import y.view.NodeRealizer;

import java.awt.geom.Ellipse2D;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the on page reference symbol of flowchart diagrams.
 **/
public class FlowchartOnPageReferencePainter extends AbstractFlowchartPainter {
  public FlowchartOnPageReferencePainter() {
    super();
    outline = new Ellipse2D.Double();
  }

  protected void updateOutline(NodeRealizer context) {
    Ellipse2D shape = (Ellipse2D) getOutline();

    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    double diameter = Math.min(height, width);
    double borderDistanceX = Math.max((width - diameter)/2, 0);
    double borderDistanceY = Math.max((height - diameter)/2, 0);
    shape.setFrame(x + borderDistanceX, y + borderDistanceY, diameter, diameter);
  }
}
