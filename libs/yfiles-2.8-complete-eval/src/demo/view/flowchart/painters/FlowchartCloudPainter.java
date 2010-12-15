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
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the cloud symbol of flowchart diagrams.
 **/
public class FlowchartCloudPainter extends AbstractFlowchartPainter {



  public FlowchartCloudPainter() {
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
    double asymetrConstY = 0.03 * height;
    double asymetrConstX = 0.05 * width;
    double xOffset1 = 0.125;
    double yOffset1 = 0.250;
    double yOffset2 = 0.18;
    shapePath.moveTo((float) (x+ xOffset1 *width), (float)(y+0.5*height+asymetrConstY));
    shapePath.curveTo((float) x, (float) (y+ yOffset1 *height), (float) (x+ 0.125 *width), (float) y, (float) (x + 0.33*width), (float) (y + yOffset2 *height));
    shapePath.curveTo((float) (x + 0.33*width), (float) y, (float) (x+width-0.33*width), (float) y, (float) (x+width-0.33*width), (float) (y + yOffset2 *height));
    shapePath.curveTo((float) (x + width- 0.125 *width), (float) y, (float) (x+width), (float) (y+ yOffset1 *height), (float) (x+width- xOffset1 *width), (float) (y + 0.5*height - asymetrConstY));

    shapePath.curveTo((float) (x+width), (float) (y+height- yOffset1 *height), (float) (x + width- 0.125 *width), (float) (y+height), (float) (x+width-0.33*width+asymetrConstX), (float) (y + height - yOffset2 *height));
    shapePath.curveTo((float) (x+width-0.33*width), (float) (y+height), (float) (x + 0.33*width), (float) (y+height), (float) (x + 0.33*width+asymetrConstX), (float) (y + height- yOffset2 *height));
    shapePath.curveTo((float) (x+ 0.125 *width), (float) (y+height), (float)x, (float) (y+height- yOffset1 *height), (float) (x+ xOffset1 *width), (float)(y+0.5*height+asymetrConstY));
    shapePath.closePath();

  }
}