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
package demo.layout.tree;

import demo.view.DemoBase;
import y.base.Edge;
import y.base.Node;
import y.layout.tree.DendrogramPlacer;
import y.layout.tree.GenericTreeLayouter;
import y.view.Arrow;
import y.view.CreateChildEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.PortAssignmentMoveSelectionMode;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;

/**
 * This demo shows how to dynamically maintain a tree as a dendrogram, i.e. all subtrees of a single
 * local root align at their bottom border. It uses {@link GenericTreeLayouter}, {@link DendrogramPlacer}
 * and {@link CreateChildEdgeMode}.
 * <br/>
 * Usage: Add new nodes by dragging an edge from the parent node of the new node. In this demo the
 * create edge gesture does not need to be completed at a target node. It can end anywhere. The target
 * node is created as a child node of the source node for the gesture. The target location of the gesture
 * determines the relative position of the new child node among the children of its parent node. Adding
 * a node, moving a set of selected nodes or changing their sizes triggers a new layout which restores
 * the dendrogram style of the tree.
 */
public class DendrogramLayouterDemo extends DemoBase
{
  private static final Color[] layerColors = {Color.red, Color.orange, Color.yellow, Color.cyan, Color.green,
      Color.blue};

  private GenericTreeLayouter treeLayouter;

  public DendrogramLayouterDemo()
  {
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);
    ((PolyLineEdgeRealizer)defaultER).setSmoothedBends(true);
    defaultER.setLineType(LineType.LINE_2);

    treeLayouter = new GenericTreeLayouter();

    DendrogramPlacer dendrogramPlacer = new DendrogramPlacer();
    treeLayouter.setDefaultNodePlacer(dendrogramPlacer);
    treeLayouter.setDefaultChildComparator(dendrogramPlacer.createComparator());

    createSampleGraph(graph);
  }

  private void createSampleGraph(Graph2D graph){
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer(root).setFillColor(layerColors[0]);
    createChildren(graph, root, 3, 1, 2);
    calcLayout();
  }

  private void createChildren(Graph2D graph, Node root, int children, int layer, int layers){
    if (graph.nodeCount() % 3 == 2){
      // do not create nodes for every subtree
      return;
    }
    for (int i = 0; i < children; i++){
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[layer % layerColors.length]);
      if (layers > 0){
        createChildren(graph, child, children, layer+1, layers-1);
      }
    }
  }

  protected boolean isDeletionEnabled(){
    return false;
  }

  protected void registerViewModes() {
    EditMode editMode = new TreeCreateEditMode();
    view.addViewMode( editMode );
  }

  public void calcLayout(){
    if (!view.getGraph2D().isEmpty()){
      Cursor oldCursor = view.getViewCursor();
      try {
        view.setViewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.applyLayoutAnimated(treeLayouter);
      } finally {
        view.setViewCursor(oldCursor);
      }
    }
  }

  final class TreeCreateChildEdgeMode extends CreateChildEdgeMode {
    protected void edgeCreated(Edge e){
      int depth = 1;
      for (Node n = e.source(); n.inDegree() > 0; n = n.firstInEdge().source()){
        depth++;
      }
      Graph2D g = getGraph2D();
      g.getRealizer(e.target()).setFillColor(layerColors[depth % layerColors.length]);
      g.unselectAll();
      calcLayout();
      g.setSelected(e.target(), true);
    }

    protected NodeRealizer createChildNodeRealizer()
    {
      NodeRealizer retValue;
      retValue = super.createChildNodeRealizer();
      retValue.setLabelText("");
      return retValue;
    }

  }

  final class TreeHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y)
    {
      super.mouseReleasedLeft(x, y);
      calcLayout();
    }
  }

  final class TreeCreateEditMode extends EditMode {
    TreeCreateEditMode(){
      super();
      setMoveSelectionMode(new TreeMoveSelectionMode());
      setCreateEdgeMode(new TreeCreateChildEdgeMode());
      setHotSpotMode(new TreeHotSpotMode());
    }

    public boolean doAllowNodeCreation()
    {
      return getGraph2D().N() == 0;
    }
  }

  final class TreeMoveSelectionMode extends PortAssignmentMoveSelectionMode {
    TreeMoveSelectionMode(){
      super(null, null);
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y)
    {
      super.selectionMovedAction(dx, dy, x, y);
      calcLayout();
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new DendrogramLayouterDemo()).start("Dendrogram Demo");
      }
    });
  }
}
