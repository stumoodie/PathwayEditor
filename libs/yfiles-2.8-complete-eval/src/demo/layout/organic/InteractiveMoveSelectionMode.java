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
package demo.layout.organic;

import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2DView;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.layout.organic.InteractiveOrganicLayouter;

/**
 * This moveSelection mode allows the user to easily drag nodes around.
 */
public class InteractiveMoveSelectionMode extends MoveSelectionMode {
  private InteractiveOrganicLayouter layouter;

  public InteractiveMoveSelectionMode( InteractiveOrganicLayouter layouter ) {
    if ( layouter == null ) throw new IllegalArgumentException( "layouter must not be null" );
    this.layouter = layouter;
  }

  /**
   * Called when the dragging has started.
   * The node is locked and the position is updated.
   */
  protected void selectionMoveStarted( double x, double y ) {
    view.setDrawingMode( Graph2DView.NORMAL_MODE );

    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );

      layouter.setInertia( node, 1 );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * Called while the node is dragged.
   */
  protected void selectionOnMove( double dx, double dy, double x, double y ) {
    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * When the dragging ends.
   * The lock on the node is removed
   */
  protected void selectionMovedAction( double dx, double dy, double x, double y ) {
    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );

      layouter.setInertia( node, 0 );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * Increases the neighbors heat
   * @param originalNode
   */
  protected void increaseNeighborsHeat( Node originalNode ) {
    //Increase Heat of neighbors
    for ( NodeCursor cursor = originalNode.neighbors(); cursor.ok(); cursor.next() ) {
      Node neighbor = cursor.node();

      double oldStress = layouter.getStress( neighbor );
      layouter.setStress( neighbor, Math.min( 1, oldStress + 0.5 ) );
    }
  }
}
