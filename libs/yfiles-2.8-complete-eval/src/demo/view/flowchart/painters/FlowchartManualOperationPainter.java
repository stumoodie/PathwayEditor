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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the manual operation symbol of flowchart diagrams.
 **/
public class FlowchartManualOperationPainter extends AbstractFlowchartPainter {


  public FlowchartManualOperationPainter() {
    super();
    outline = new GeneralPath();

  }

  protected void updateOutline(NodeRealizer context) {
    GeneralPath shapePath = (GeneralPath) getOutline();
    shapePath.reset();
    
    double x = context.getX();
    double y = context.getY();
    double height = context.getHeight();
    double width = context.getWidth();
    double borderDistance;
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    if (cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE) != null) {
      borderDistance = ((Double) cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE)).doubleValue();
    } else {
      borderDistance = FLOWCHART_DEFAULT_MANUAL_OPERATION_BORDER_DISTANCE;
    }
    borderDistance = Math.min(borderDistance, width/2);
    shapePath.moveTo((float)x,(float)y);
    shapePath.lineTo((float)(x + width), (float)y);
    shapePath.lineTo((float)(x + width - borderDistance), (float)(y + height));
    shapePath.lineTo((float)(x + borderDistance), (float)(y + height));
    shapePath.closePath();
    
  }
}
