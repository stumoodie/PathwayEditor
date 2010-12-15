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
import java.awt.geom.Rectangle2D;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the internal storage symbol of flowchart diagrams.
 **/
public class FlowchartInternalStoragePainter extends AbstractFlowchartPainter {

  public FlowchartInternalStoragePainter() {
    super();
    outline = new Rectangle2D.Double();
    innerShape = new GeneralPath();
  }


  protected void updateOutline(NodeRealizer context) {
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    Rectangle2D shape = (Rectangle2D) getOutline();
    shape.setFrame(x, y, width, height);
  }

  public void updateInsideShape(NodeRealizer context) {
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    double borderDistance;
    if (cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE) != null) {
      borderDistance = ((Double) cast_gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE)).doubleValue();
    } else {
      borderDistance = Math.min (Math.min(FLOWCHART_DEFAULT_INTERNAL_STORAGE_BORDER_DISTANCE, width/2), height /2);
    }

    GeneralPath shape = (GeneralPath) getInnerShape();
    shape.reset();
    shape.moveTo((float) (x + borderDistance), (float) y);
    shape.lineTo((float) (x + borderDistance), (float) (y + height));
    shape.moveTo((float) x, (float) (y + borderDistance));
    shape.lineTo((float) (x + width), (float) (y + borderDistance));
    shape.moveTo((float) (x + borderDistance), (float) y);
    shape.closePath();
  }
}
