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

import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the predefined process symbol of flowchart diagrams.
 **/
public class FlowchartPredefinedProcessPainter extends AbstractFlowchartPainter {

  public FlowchartPredefinedProcessPainter() {
    super();
    outline = new Rectangle2D.Double();
    innerShape = new GeneralPath();
  }

  protected void updateOutline(NodeRealizer context) {
    Rectangle2D shape = (Rectangle2D) getOutline();
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double height = context.getHeight();
    shape.setFrame(x, y, width, height);

  }

  public void updateInsideShape(NodeRealizer context) {
    GeneralPath shape = (GeneralPath) getInnerShape();
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double height = context.getHeight();
    double borderDistance;
    if (cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE) != null) {
      borderDistance = ((Double) cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE)).doubleValue();
    } else {
      borderDistance = Math.min(FLOWCHART_DEFAULT_PREDEFINED_PROCESS_BORDER_DISTANCE, width / 2);
    }
    shape.reset();
    shape.moveTo((float) (x + borderDistance), (float) y);
    shape.lineTo((float) (x + borderDistance), (float) (y + height));
    shape.moveTo((float) (x + width - borderDistance), (float) y);
    shape.lineTo((float) (x + width - borderDistance), (float) (y + height));
    shape.moveTo((float) (x + borderDistance), (float) y);
    shape.closePath();
  }
}
