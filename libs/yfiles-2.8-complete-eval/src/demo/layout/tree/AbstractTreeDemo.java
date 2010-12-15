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
import y.anim.AnimationPlayer;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.tree.AbstractRotatableNodePlacer;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.DefaultNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.util.DataProviderAdapter;
import y.view.CreateChildEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.PortAssignmentMoveSelectionMode;

import javax.swing.AbstractAction;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

/**
 * The AbstractTreeDemo is a base class for several tree demos.
 * It contains ViewModes and other helper methods for tree manipulation and visualization.
 **/
public abstract class AbstractTreeDemo extends DemoBase {
  protected GenericTreeLayouter treeLayouter = new GenericTreeLayouter();

  protected EdgeMap sourcePortMap;
  protected EdgeMap targetPortMap;
  protected NodeMap portAssignmentMap;
  protected NodeMap nodePlacerMap;
  protected PortAssignmentMoveSelectionMode portAssignmentMoveMode = new TreePortAssignmentMode();
  protected Color[] layerColors = {Color.red, Color.orange, Color.yellow, Color.blue, Color.cyan,
      Color.green};

  /**
   * Instantiates a new AbstractDemo.
   */
  protected AbstractTreeDemo() {
    view.addViewMode(new TreeCreateEditMode());

    AnimationPlayer animationPlayer = new AnimationPlayer();
    animationPlayer.addAnimationListener(view);

    Graph2D graph = view.getGraph2D();

    sourcePortMap = graph.createEdgeMap();
    targetPortMap = graph.createEdgeMap();
    portAssignmentMap = graph.createNodeMap();
    nodePlacerMap = graph.createNodeMap();
    graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
    graph.addDataProvider(GenericTreeLayouter.PORT_ASSIGNMENT_DPKEY, portAssignmentMap);
    graph.addDataProvider(GenericTreeLayouter.CHILD_COMPARATOR_DPKEY, new ChildEdgeComparatorProvider());
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);

    portAssignmentMoveMode.setSpc(sourcePortMap);
    portAssignmentMoveMode.setTpc(targetPortMap);
  }

  /**
   * Set the NodePlacer for the given node.
   * @param node
   * @param placer
   */
  public void setNodePlacer(Node node, NodePlacer placer) {
    nodePlacerMap.set(node, placer);
  }

  /**
   * Calculate the layout and update the view (using an animation).
   */
  public void calcLayout() {
    if (!view.getGraph2D().isEmpty()) {
      Cursor oldCursor = view.getCanvasComponent().getCursor();
      try {
        view.getCanvasComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.applyLayoutAnimated(treeLayouter);
      } finally {
        view.getCanvasComponent().setCursor(oldCursor);
      }
    }
  }

  /**
   * May be overridden by subclasses.
   */
  protected PopupMode createTreePopupMode() {
    return null;
  }

  /**
   * May be overridden by subclasses.
   */
  protected void registerViewModes() {
  }

  protected NodePlacer createDefaultNodePlacer() {
    return new SimpleNodePlacer();
  }

  protected final class ChildEdgeComparatorProvider extends DataProviderAdapter {
    public Object get(Object dataHolder) {
      NodePlacer placer = (NodePlacer) nodePlacerMap.get(dataHolder);
      if (placer instanceof AbstractRotatableNodePlacer) {
        return ((AbstractRotatableNodePlacer) placer).createComparator();
      }
      if (placer instanceof DefaultNodePlacer) {
        return ((DefaultNodePlacer) placer).createComparator();
      }
      return null;
    }
  }

  private final class TreeHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);
      calcLayout();
    }
  }

  protected final class TreeCreateEditMode extends EditMode {
    TreeCreateEditMode() {
      if (portAssignmentMoveMode == null) {
        throw new IllegalStateException("portAssignmentMoveMode is null");
      }
      setMoveSelectionMode(portAssignmentMoveMode);
      setCreateEdgeMode(new TreeCreateChildEdgeMode());
      setHotSpotMode(new TreeHotSpotMode());
      setPopupMode(AbstractTreeDemo.this.createTreePopupMode());
    }

    public boolean doAllowNodeCreation() {
      return getGraph2D().N() == 0;
    }

    protected void nodeCreated(Node v) {
      super.nodeCreated(v);
      setNodePlacer(v, createDefaultNodePlacer());
    }
  }

  private final class TreeCreateChildEdgeMode extends CreateChildEdgeMode {
    protected void edgeCreated(Edge edge) {
      int depth = 1;
      for (Node node = edge.source(); node.inDegree() > 0; node = node.firstInEdge().source()) {
        depth++;
      }
      Graph2D g = getGraph2D();
      g.getRealizer(edge.target()).setFillColor(layerColors[depth % layerColors.length]);
      EdgeRealizer er = g.getRealizer(edge);
      if (nodePlacerMap.get(edge.source()) == null) {
        parseNodePlaceMent(g, edge, er);
      }

      nodePlacerMap.set(edge.target(), new SimpleNodePlacer());
      parseTargetPort(g, edge, er);
      g.unselectAll();
      calcLayout();
      g.setSelected(edge.target(), true);
    }

    private void parseNodePlaceMent(Graph2D g, Edge e, EdgeRealizer er) {
      nodePlacerMap.set(e.source(), new SimpleNodePlacer());
    }

    private void parseTargetPort(Graph2D g, Edge e, EdgeRealizer er) {
      if (er.bendCount() > 0) {
        YPoint lastPoint = new YPoint(er.lastBend().getX(), er.lastBend().getY());
        NodeRealizer target = g.getRealizer(e.target());
        double dx = lastPoint.x - target.getCenterX();
        double dy = lastPoint.y - target.getCenterY();
        byte side;
        if (Math.abs(dx) > Math.abs(dy)) {
          if (dx > 0.0) {
            side = PortConstraint.EAST;
          } else {
            side = PortConstraint.WEST;
          }
        } else {
          if (dy > 0.0) {
            side = PortConstraint.SOUTH;
          } else {
            side = PortConstraint.NORTH;
          }
        }
        targetPortMap.set(e, PortConstraint.create(side));
      }
    }

    protected NodeRealizer createChildNodeRealizer() {
      NodeRealizer retValue = super.createChildNodeRealizer();
      retValue.setLabelText("");
      return retValue;
    }

  }

  protected class SetHorizontalAlignmentAction extends AbstractAction {
    private RootAlignment alignment;

    protected SetHorizontalAlignmentAction(String name, RootAlignment alignment) {
      super(name);
      this.alignment = alignment;
    }

    public void actionPerformed(ActionEvent e) {
      for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        NodePlacer nodePlacer = (NodePlacer) nodePlacerMap.get(node);

        try {
          Method method = nodePlacer.getClass().getMethod("setRootAlignment", new Class[]{RootAlignment.class});
          method.invoke(nodePlacer, new Object[]{alignment});
        } catch (Exception ex) {
        }
      }
      calcLayout();
    }
  }

  abstract class SetNodePlacerAction extends AbstractAction {
    protected SetNodePlacerAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        nodePlacerMap.set(node, createNodePlacer());
      }
      calcLayout();
    }

    protected abstract NodePlacer createNodePlacer();
  }

  private final class TreePortAssignmentMode extends PortAssignmentMoveSelectionMode {
    TreePortAssignmentMode() {
      super(null, null);
    }

    protected boolean isPortReassignmentAllowed(Edge edge, boolean source) {
      return !source;
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      super.selectionMovedAction(dx, dy, x, y);
      calcLayout();
    }

  }
}
