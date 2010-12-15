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
package demo.io.graphml;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;

import demo.view.DemoDefaults;

/**
 * A simple customization of {@link y.view.ShapeNodeRealizer} that holds additional
 * fields.
 * GraphML serialization of this realizer and its additional fields is handled by
 * {@link CustomNodeRealizerSerializer}.
 */
public class CustomNodeRealizer extends ShapeNodeRealizer {
  // Custom value
  private int customValue;
  // Custom attribute
  private String customAttribute;

  /** Creates a new instance of CustomNodeRealizer. */
  public CustomNodeRealizer() {
    setSize(60, 40);
    setCustomAttribute("v1.0");
    setCustomValue(333);
    setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
  }

  /** Creates a new instance of CustomNodeRealizer. */
  public CustomNodeRealizer(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof CustomNodeRealizer) {
      CustomNodeRealizer fnr = (CustomNodeRealizer) nr;
      // Copy the values of custom attributes. 
      setCustomValue(fnr.customValue);
      setCustomAttribute(fnr.customAttribute);
    }
  }

  public NodeRealizer createCopy(NodeRealizer nr) {
    return new CustomNodeRealizer(nr);
  }

  public void paintText(Graphics2D gfx) {
    super.paintText(gfx);
    gfx.setColor(Color.blue);
    gfx.drawString("value: " + getCustomValue(), (float) getX() + 4, (float) getY() + 12);
    gfx.drawString("attr:  " + getCustomAttribute(), (float) getX() + 4, (float) (getY() + getHeight() - 2));
  }

  public int getCustomValue() {
    return customValue;
  }

  public void setCustomValue(int customValue) {
    this.customValue = customValue;
  }

  public String getCustomAttribute() {
    return customAttribute;
  }

  public void setCustomAttribute(String customAttribute) {
    this.customAttribute = customAttribute;
  }
}
