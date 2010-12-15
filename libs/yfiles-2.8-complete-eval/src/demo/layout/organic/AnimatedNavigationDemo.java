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

import demo.view.DemoDefaults;
import y.algo.Bfs;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YList;
import y.geom.YPoint;
import y.util.DefaultMutableValue2D;
import y.util.Maps;
import y.view.Drawable;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import javax.swing.SwingUtilities;

/**
 * This demo is an extension of {@link demo.layout.organic.NavigationDemo}.
 * <br>
 * In this demo, changing the visible subgraph is now accompanied by animated
 * fade-in and fade-out effects for appearing and disappearing nodes and edges.
 * Additionally, the initial positions of nodes that newly appear in the graph
 * are set to the position of their already visible parent node.
 */
public class AnimatedNavigationDemo extends NavigationDemo {
  private NodeMap hiddenNodesMap;
  private EdgeMap hiddenEdgesMap;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        final AnimatedNavigationDemo navigationDemo = new AnimatedNavigationDemo();
        navigationDemo.start("Animated Navigation Demo");
        navigationDemo.moveFirstNodeToCenter();
      }
    });
  }

  public AnimatedNavigationDemo() {
    // remember which nodes are hidden
    hiddenNodesMap = view.getGraph2D().createNodeMap();
    hiddenEdgesMap = view.getGraph2D().createEdgeMap();
  }

  /**
   * This list contains the history of the center nodes.
   */
  private YList history = new YList();

  protected void moveToCenter(final Node newCenterNode, boolean animated) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("not in dispatch thread");
    }
    this.centerNode = newCenterNode;

    // The new centered node is "pinned" It will no longer be moved by the layouter.
    layouter.setInertia(newCenterNode, 1);
    if (history.size() < 4) {
      history.addFirst(newCenterNode);
    } else {
      ListCell lastCell = history.lastCell();
      // The "older" centered nodes are moveable again.
      layouter.setInertia((Node) lastCell.getInfo(), 0);
      history.removeCell(lastCell);
      lastCell.setInfo(newCenterNode);
      history.addFirstCell(lastCell);
    }

    //The elements that will change state
    NodeList fadeInNodes = new NodeList();
    NodeList fadeOutNodes = new NodeList();
    EdgeList fadeInEdges = new EdgeList();
    EdgeList fadeOutEdges = new EdgeList();

    // the elements that will be hidden finally
    final EdgeList hiddenEdges = new EdgeList();
    final NodeList hiddenNodes = new NodeList();

    final Graph2D graph = view.getGraph2D();

    // unhide the whole graph to perform the calculation
    graphHider.unhideAll();

    // do a Bfs run
    // prepare a NodeMap
    final int[] data = new int[graph.N()];
    NodeMap layerMap = Maps.createIndexNodeMap(data);
    // calculate the first 4 layers
    NodeList[] layerLists = Bfs.getLayers(graph, new NodeList(newCenterNode), false, layerMap, 4);

    // get the new nodes to display
    for (int i = 0; i < Math.min(layerLists.length, 3); i++) {
      for (NodeCursor nc = layerLists[i].nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        final boolean wasHidden = hiddenNodesMap.getBool(node);
        if (wasHidden) {
          fadeInNodes.add(node);
        }
      }
    }

    // update the visibility marker and add the nodes to hide
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      final boolean wasHidden = hiddenNodesMap.getBool(node);
      final int layer = layerMap.getInt(node);
      if (layer >= 0 && layer < 3) {
        // should be visible
        if (wasHidden) {
          hiddenNodesMap.setBool(node, false);
        }
      } else {
        hiddenNodes.add(node);
        // should be invisible
        if (!wasHidden) {
          fadeOutNodes.add(node);
          hiddenNodesMap.setBool(node, true);
        }
      }
    }

    // update the visibility of the edges and sort out which ones to hide and which ones to insert
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final boolean wasHidden = hiddenEdgesMap.getBool(edge);
      final boolean shouldBeHidden = hiddenNodesMap.getBool(edge.source()) || hiddenNodesMap.getBool(edge.target());
      if (shouldBeHidden) {
        hiddenEdges.add(edge);
        if (!wasHidden) {
          fadeOutEdges.add(edge);
          hiddenEdgesMap.setBool(edge, true);
        }
      } else {
        if (wasHidden) {
          fadeInEdges.add(edge);
          hiddenEdgesMap.setBool(edge, false);
        }
      }
    }

    //calculate the camera movement.
    double x;
    double y;

    //If we have valid informations from the layouter, use them.
    YPoint location = layouter.getCenter(newCenterNode);
    if (location != null) {
      x = location.getX();
      y = location.getY();
    } else { //Fallback: Use the informations from the realizers (e.g. at the beginning)
      NodeRealizer realizer = view.getGraph2D().getRealizer(newCenterNode);
      x = realizer.getX();
      y = realizer.getY();
    }

    if (animated) {
      // now perform the animations
      // ... for camera
      animateCamera(x, y);

      // for the elements that go away....
      fadeOutEdges(fadeOutEdges);
      fadeOutNodes(fadeOutNodes);

      // and for the new elements
      fadeInEdges(fadeInEdges);
      fadeInNodes(graph, layerMap, fadeInNodes);
    } else {
      view.setCenter(x, y);
    }

    //Hide the edges that have been marked
    graphHider.hide(hiddenEdges);

    //Now hide the nodes (and edges) that shall be hidden
    graphHider.hide(hiddenNodes);

    // make sure the layout algorithm starts its work
    this.layouter.syncStructure();
    this.layouter.wakeUp();
  }

  private void animateCamera(double x, double y) {
    //An AnimationObject representing the movement of the camera is created
    AnimationObject animationObject = factory.moveCamera(DefaultMutableValue2D.create(x, y), PREFERRED_DURATION);
    //The movement is eased (in and out)
    AnimationObject easedAnimation = AnimationFactory.createEasedAnimation(animationObject, 0.15, 0.25);
    animationPlayer.animate(easedAnimation);
  }

  private void fadeOutNodes(NodeList fadeOutNodes) {
    for (NodeCursor nc = fadeOutNodes.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      //Fade out visible node
      NodeRealizer realizer = view.getGraph2D().getRealizer(node);
      final Drawable nodeDrawable = ViewAnimationFactory.createDrawable(realizer);
      animationPlayer.animate(factory.fadeOut(nodeDrawable, PREFERRED_DURATION));
    }
  }

  private void fadeInNodes(Graph2D graph, NodeMap layerMap, NodeList fadeInNodes) {
    for (NodeCursor nc = fadeInNodes.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();

      // calculate the new position for the node
      final int myLayer = layerMap.getInt(node);
      double posX = 0;
      double posY = 0;

      // determine the barycenter of all parent neighbour nodes
      int count = 0;
      for (NodeCursor nc2 = node.neighbors(); nc2.ok(); nc2.next()) {
        final Node neighbour = nc2.node();
        if (layerMap.getInt(neighbour) < myLayer) {
          count++;
          posX += graph.getCenterX(neighbour);
          posY += graph.getCenterY(neighbour);
        }
      }

      // get the realizer
      NodeRealizer nodeRealizer = view.getGraph2D().getRealizer(node);

      // update its position
      if (count > 0) {
        posX /= count;
        posY /= count;
        //copy the coords of the parent...
        //.. to the realizer...
        nodeRealizer.setCenter(posX, posY);
        //... and to the data structure
        layouter.setCenter(node, posX, posY);
      }

      //Fade the node in
      animationPlayer.animate(factory.fadeIn(nodeRealizer, PREFERRED_DURATION)); //<-------- FADE IN
    }
  }

  private void fadeOutEdges(EdgeList fadeOutEdges) {
    for (EdgeCursor ec = fadeOutEdges.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      EdgeRealizer realizer = view.getGraph2D().getRealizer(edge);
      final Drawable edgeDrawable = ViewAnimationFactory.createDrawable(realizer);
      animationPlayer.animate(factory.fadeOut(edgeDrawable, PREFERRED_DURATION));//<--------   FADE OUT
    }
  }

  private void fadeInEdges(EdgeList fadeInEdges) {
    for (EdgeCursor ec = fadeInEdges.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      EdgeRealizer edgeRealizer = view.getGraph2D().getRealizer(edge);
      animationPlayer.animate(factory.fadeIn(edgeRealizer, PREFERRED_DURATION));  //<-------- FADE IN
    }
  }
}
