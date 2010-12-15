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

import java.awt.geom.GeneralPath;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the loop limit symbol of flowchart diagrams.
 **/
public class FlowchartLoopLimitPainter extends AbstractFlowchartPainter {
  public FlowchartLoopLimitPainter() {
    super();
    outline = new GeneralPath();
  }

  protected void updateOutline(NodeRealizer context) {
    GeneralPath shape = (GeneralPath) getOutline();
    shape.reset();
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    double borderDistance = Math.min(Math.min(10, width/2), height/2);
    shape.moveTo((float)(x + borderDistance), (float)y);
    shape.lineTo((float)(x + width - borderDistance), (float)y);
    shape.lineTo((float)(x+ width), (float)(y + borderDistance));
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)x, (float)(y + height));
    shape.lineTo((float)x, (float)(y + borderDistance));
    shape.closePath();
  }

}
