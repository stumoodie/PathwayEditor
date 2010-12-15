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

import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import y.view.Graph2D;
import y.base.Node;
import y.base.EdgeCursor;
import y.base.Edge;

/**
 * The {@link y.view.GenericNodeRealizer.LayerHandler} implementation for flowchart annotation node.
 * The implementation puts the annotation symbol in the {@link y.view.Graph2DView#FG_LAYER foreground layer},
 * if the edges of the annotation node are located in the foreground layer. This ensures the repainting of the symbol,
 * if edges (or neighbour nodes) has been moved.
 */
public class FlowchartAnnotationLayerHandler implements GenericNodeRealizer.LayerHandler {

  private static final String LAYER_STYLE_PROPERTY_KEY = "LAYER_STYLE_PROPERTY_KEY";

  /**
   * Sets the logical graphical layer for this realizer. Layer information can be used by viewers to optimize redraws.
   *
   * @see y.view.Graph2DView#FG_LAYER
   * @see y.view.Graph2DView#BG_LAYER
   */
  public void setLayer(NodeRealizer context, byte l) {
    if (l == y.view.Graph2DView.FG_LAYER) {
      ((GenericNodeRealizer) context).removeStyleProperty(LAYER_STYLE_PROPERTY_KEY);
    } else {
      ((GenericNodeRealizer)context).setStyleProperty(LAYER_STYLE_PROPERTY_KEY, new Byte(l));
    }
  }

  /**
   * Returns the logical graphical layer for this realizer. Layer information can be used by viewers to optimize
   * redraws.
   *
   * @see y.view.Graph2DView#FG_LAYER
   * @see y.view.Graph2DView#BG_LAYER
   */
  public byte getLayer(NodeRealizer context) {
    final Node node = context.getNode();
    final Graph2D graph2D = (Graph2D) node.getGraph();
    for (EdgeCursor edgeCursor = node.edges(); edgeCursor.ok(); edgeCursor.next()) {
      Edge edge = edgeCursor.edge();
      if (graph2D.getRealizer(edge).getLayer() == y.view.Graph2DView.FG_LAYER) {
        return y.view.Graph2DView.FG_LAYER;
      }
    }
    if (context instanceof GenericNodeRealizer && ((GenericNodeRealizer)context).getStyleProperty(LAYER_STYLE_PROPERTY_KEY) instanceof Byte){
      Byte layer = (Byte) ((GenericNodeRealizer)context).getStyleProperty(LAYER_STYLE_PROPERTY_KEY);
      return layer.byteValue();
    } else {
      return y.view.Graph2DView.FG_LAYER;
    }
  }
}
