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
package demo.view.advanced.ports;

import y.base.Node;
import y.view.CreateEdgeMode;

/**
 * Custom <code>CreateEdgeMode</code> implementation that restricts edge
 * creation to nodes with node ports. 
 *
 */
class PortCreateEdgeMode extends CreateEdgeMode {
  /**
   * Overwritten to accept only nodes with node ports.
   * @param source the node to check.
   * @param x the x-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @param y the y-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @return <code>true<code> if the specified node has node ports;
   * <code>false</code> otherwise.
   */
  protected boolean acceptSourceNode( final Node source, final double x, final double y ) {
    return hasPorts(source);
  }

  /**
   * Overwritten to accept only nodes with node ports.
   * @param target the node to check.
   * @param x the x-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @param y the y-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @return <code>true<code> if the specified node has node ports;
   * <code>false</code> otherwise.
   */
  protected boolean acceptTargetNode( final Node target, final double x, final double y ) {
    return hasPorts(target);
  }

  private boolean hasPorts( final Node node ) {
    return view.getGraph2D().getRealizer(node).portCount() > 0;
  }
}
