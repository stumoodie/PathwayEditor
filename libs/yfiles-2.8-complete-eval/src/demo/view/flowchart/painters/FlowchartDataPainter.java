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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the data symbol of flowchart diagrams.
 **/
public class FlowchartDataPainter extends AbstractFlowchartPainter implements FlowchartRealizerConstants{

  public FlowchartDataPainter() {
    super();
    this.outline = new GeneralPath();
  }

  protected void updateOutline(NodeRealizer context) {

    GeneralPath shapePath = (GeneralPath) getOutline();
    shapePath.reset();
    double x = context.getX();
    double y = context.getY();
    double height = context.getHeight();
    double width = context.getWidth();
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    double inclination;
    if (cast_gnr.getStyleProperty(PROPERTY_INCLINATION) != null) {
      inclination = ((Double) cast_gnr.getStyleProperty(PROPERTY_INCLINATION)).doubleValue();
      inclination = Math.min(inclination, 0.5);
    } else {
      inclination = FLOWCHART_DEFAULT_DATA_INCLINATION;
    }

    double borderDistance = inclination * Math.min(width, height);

    shapePath.moveTo((float)(x + borderDistance), (float)y);
    shapePath.lineTo((float)(x + width), (float)y);
    shapePath.lineTo((float)(x + width - borderDistance), (float)(y + height));
    shapePath.lineTo((float)x, (float)(y + height));
    shapePath.closePath();
  }
}
