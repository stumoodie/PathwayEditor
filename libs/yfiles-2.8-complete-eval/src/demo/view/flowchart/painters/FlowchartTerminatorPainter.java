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

import java.awt.geom.RoundRectangle2D;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the termination symbol of flowchart diagrams.
 **/
public class FlowchartTerminatorPainter extends AbstractFlowchartPainter {
  public FlowchartTerminatorPainter() {
    super();
    outline = new RoundRectangle2D.Double();
  }

  protected void updateOutline(NodeRealizer context) {
    RoundRectangle2D shape = (RoundRectangle2D) getOutline();
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();

    double arc = Math.min(height, width*2);
    shape.setRoundRect(x, y, width, height, arc, arc);
  }
}
