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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the "start1" symbol of flowchart diagrams.
 **/
public class FlowchartStart1Painter extends AbstractFlowchartPainter{
  
  public FlowchartStart1Painter() {
    this.outline = new Ellipse2D.Double();
  }

  protected void updateOutline(NodeRealizer context) {
    Ellipse2D shape = (Ellipse2D) getOutline();
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double height = context.getHeight();
    shape.setFrame(x, y, width, height);
  }
}
