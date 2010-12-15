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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the off page reference symbol of flowchart diagrams.
 **/
public class FlowchartOffPageReferencePainter extends AbstractFlowchartPainter {
  public FlowchartOffPageReferencePainter() {
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
    double minLength = Math.min(height, width);
    
    double borderDistanceX = Math.max((width - minLength)/2, 0);
    double borderDistanceY = Math.max((height - minLength)/2, 0);
    shape.moveTo((float)(x + borderDistanceX), (float)(y + borderDistanceY));
    shape.lineTo((float)(x + minLength + borderDistanceX), (float)(y + borderDistanceY));
    shape.lineTo((float)(x + minLength + borderDistanceX), (float)(y + minLength/2 + borderDistanceY));
    shape.lineTo((float)(x + minLength/2 + borderDistanceX), (float)(y + minLength + borderDistanceY));
    shape.lineTo((float)(x + borderDistanceX), (float)(y + minLength/2 + borderDistanceY));
    shape.closePath();
  }
}
