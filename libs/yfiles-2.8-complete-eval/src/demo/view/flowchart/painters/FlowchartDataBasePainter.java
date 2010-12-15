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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the data base symbol of flowchart diagrams.
 **/
public class FlowchartDataBasePainter extends AbstractFlowchartPainter {

  double X_OFFSET_1 = 0.03;
  double Y_OFFSET_1 = 0.2;

  public FlowchartDataBasePainter() {
    super();
    this.outline = new GeneralPath();
    this.innerShape = new GeneralPath();
  }


  protected void updateOutline(NodeRealizer context) {

    GeneralPath shapePath = (GeneralPath) getOutline();
    shapePath.reset();

    double x = context.getX();
    double y = context.getY();
    double height = context.getHeight();
    double width = context.getWidth();
    
    shapePath.moveTo((float)x, (float)(y + Y_OFFSET_1 *height));
    shapePath.curveTo((float)(x+ X_OFFSET_1 *width), (float)y, (float)(x+width- X_OFFSET_1 *width), (float)y, (float) (x+width), (float)(y + Y_OFFSET_1 *height));
    shapePath.lineTo((float)(x + width), (float)(y + height - Y_OFFSET_1 *height));
    shapePath.curveTo((float)(x + width - X_OFFSET_1 *width), (float)(y+height), (float)(x + X_OFFSET_1 *width), (float) (y+height), (float)x, (float)(y + height - Y_OFFSET_1 *height));
    shapePath.closePath();
  }

  public void updateInsideShape(NodeRealizer context) {
    GeneralPath shapePath = (GeneralPath) getInnerShape();
    shapePath.reset();
    double x = context.getX();
    double y = context.getY();
    double height = context.getHeight();
    double width = context.getWidth();
    
    shapePath.moveTo((float) (x+width), (float)(y + Y_OFFSET_1 *height));
    shapePath.curveTo((float)(x + width- X_OFFSET_1 *width), (float) (y+2* Y_OFFSET_1 *height), (float)(x + X_OFFSET_1 *width), (float) (y+2* Y_OFFSET_1 *height), (float)x, (float)(y + Y_OFFSET_1 *height));
  }

}