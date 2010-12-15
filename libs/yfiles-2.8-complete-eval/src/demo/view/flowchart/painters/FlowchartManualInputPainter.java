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
import y.view.GenericNodeRealizer;

import java.awt.geom.GeneralPath;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the manual input symbol of flowchart diagrams.
 **/
public class FlowchartManualInputPainter extends AbstractFlowchartPainter {


  public FlowchartManualInputPainter() {
    super();
    outline = new GeneralPath();
  }

  protected void updateOutline(NodeRealizer context) {
    GeneralPath shape = (GeneralPath) getOutline();
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    double borderDistance;
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    if (cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE) != null) {
      borderDistance = ((Double) cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE)).doubleValue();
    } else {
      borderDistance = FLOWCHART_DEFAULT_MANUAL_INPUT_BORDER_DISTANCE;
    }
    borderDistance = Math.min(Math.min(borderDistance, width/2), height/2);

    shape.reset();
    shape.moveTo((float)x, (float)(y + borderDistance));
    shape.lineTo((float)(x + width), (float)y);
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)x, (float)(y + height));
    shape.closePath();
  }
}
