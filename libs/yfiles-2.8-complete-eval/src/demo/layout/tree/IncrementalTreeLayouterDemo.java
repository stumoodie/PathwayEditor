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
import y.algo.Bfs;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.tree.DefaultNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.NodePlacer;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.CreateChildEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.HotSpotMode;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.PopupMode;
import y.view.PortAssignmentMoveSelectionMode;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This demo shows how GenericTreeLayouter can handle port constraints and multiple 
 * different NodePlacer instances and implementations at the same time. 
 * <br>
 * On another note, it also demonstrates how different ViewModes can be subclassed 
 * or replaced to achieve a completely different application feel.
 * <br>
 * Usage: Use the panel on the left to change the layout settings such as port constraints
 * or node placers for all nodes at a certain level of the tree simultaneously by pressing
 * the "Apply" button. The panel in the lower left is a preview for the currently displayed
 * settings. You can also change the settings for individual nodes by using their context
 * menus.
 */
public class IncrementalTreeLayouterDemo extends DemoBase {
  private static final Color[] layerColors = {Color.red, Color.orange, Color.yellow, Color.cyan, Color.green,
      Color.blue};

  private EdgeMap targetPortMap;
  private NodeMap nodePlacerMap;

  private PortAssignmentMoveSelectionMode paMode;
  private double hDistance = 40.0;

  private double vDistance = 40.0;

  private GenericTreeLayouter treeLayouter;

  private DefaultNodePlacerConfigPanel configPanel;

  private List layerStyles = new ArrayList();

  {
    layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD,
        DefaultNodePlacer.ALIGNMENT_MEDIAN, 40.0, 40.0));
    layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_VERTICAL_TO_RIGHT,
        DefaultNodePlacer.ALIGNMENT_LEADING_OFFSET, 20.0, 40.0));
    layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD,
        DefaultNodePlacer.ALIGNMENT_LEADING_OFFSET, DefaultNodePlacer.ROUTING_FORK_AT_ROOT, 10.0, 20.0));
    layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD,
        DefaultNodePlacer.ALIGNMENT_MEDIAN, 40.0, 40.0));
  }

  public IncrementalTreeLayouterDemo() {
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);
    ((PolyLineEdgeRealizer) defaultER).setSmoothedBends(true);
    defaultER.setLineType(LineType.LINE_2);

    EdgeMap sourcePortMap = graph.createEdgeMap();
    targetPortMap = graph.createEdgeMap();
    NodeMap portAssignmentMap = graph.createNodeMap();
    nodePlacerMap = graph.createNodeMap();
    graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
    graph.addDataProvider(GenericTreeLayouter.PORT_ASSIGNMENT_DPKEY, portAssignmentMap);
    graph.addDataProvider(GenericTreeLayouter.CHILD_COMPARATOR_DPKEY, new ChildEdgeComparatorProvider());
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);


    paMode.setSpc(sourcePortMap);
    paMode.setTpc(targetPortMap);

    treeLayouter = new GenericTreeLayouter();

    configPanel = new DefaultNodePlacerConfigPanel();
    configPanel.adoptPlacerValues((NodePlacer) layerStyles.get(0));

    JPanel layerChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    layerChooserPanel.add(new JLabel("Layer: "));
    final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        final int layer = ((Number) spinner.getValue()).intValue() - 1;
        while (layer >= layerStyles.size()) {
          layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, 40.0, 40.0));
        }
        NodePlacer placer = (NodePlacer) layerStyles.get(layer);
        configPanel.adoptPlacerValues(placer);
      }
    }
    );
    layerChooserPanel.add(spinner);

    configPanel.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        final int layer = ((Number) spinner.getValue()).intValue() - 1;
        while (layer >= layerStyles.size()) {
          layerStyles.add(new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, 40.0, 40.0));
        }
        layerStyles.set(layer, configPanel.createPlacerCopy());
      }
    }
    );

    JButton button = new JButton(new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent ae) {
        final int layer = ((Number) spinner.getValue()).intValue() - 1;
        NodePlacer placer = (NodePlacer) layerStyles.get(layer);
        NodeList[] layers = Bfs.getLayers(graph, new NodeList(graph.firstNode()));
        if (layer < layers.length) {
          for (NodeCursor nc = layers[layer].nodes(); nc.ok(); nc.next()) {
            nodePlacerMap.set(nc.node(), placer);
          }
          calcLayout();
        }
      }
    });

    graph.addGraph2DSelectionListener(new Graph2DSelectionListener() {
      public void onGraph2DSelectionEvent(Graph2DSelectionEvent ev) {
        if (ev.isNodeSelection() && graph.isSelectionSingleton()) {
          Node n = (Node) ev.getSubject();
          int depth = 1;
          while (n.inDegree() > 0) {
            n = n.firstInEdge().source();
            depth++;
          }
          spinner.setValue(new Integer(depth));
        }
      }
    }
    );
    layerChooserPanel.add(button);

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(configPanel, BorderLayout.CENTER);
    rightPanel.add(layerChooserPanel, BorderLayout.NORTH);

    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rightPanel, view);
    sp.setOneTouchExpandable(true);
    sp.setContinuousLayout(false);
    contentPane.add(sp, BorderLayout.CENTER);
    createSampleGraph(graph);
  }

  final class ChildEdgeComparatorProvider extends DataProviderAdapter {
    public Object get(Object forRootNode) {
      NodePlacer placer = (NodePlacer) nodePlacerMap.get(forRootNode);
      if (placer instanceof DefaultNodePlacer) {
        return ((DefaultNodePlacer) placer).createComparator();
      }
      return null;
    }
  }

  private void createSampleGraph(Graph2D graph) {
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer(root).setFillColor(layerColors[0]);
    nodePlacerMap.set(root, layerStyles.get(0));
    createChildren(graph, root, 4, 1, 2);
    calcLayout();
  }

  private void createChildren(Graph2D graph, Node root, int children, int layer, int layers) {
    for (int i = 0; i < children; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[layer % layerColors.length]);
      if (layerStyles.size() > layer) {
        nodePlacerMap.set(child, layerStyles.get(layer));
      }
      if (layers > 0) {
        createChildren(graph, child, children, layer + 1, layers - 1);
      }
    }
  }

  protected boolean isDeletionEnabled() {
    return false;
  }

  protected void registerViewModes() {
    EditMode editMode = new TreeCreateEditMode();
    view.addViewMode(editMode);
  }


  public void calcLayout() {
    if (!view.getGraph2D().isEmpty()) {
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
    protected void edgeCreated(Edge e) {
      int depth = 1;
      for (Node n = e.source(); n.inDegree() > 0; n = n.firstInEdge().source()) {
        depth++;
      }
      Graph2D g = getGraph2D();
      g.getRealizer(e.target()).setFillColor(layerColors[depth % layerColors.length]);
      EdgeRealizer er = g.getRealizer(e);
      if (nodePlacerMap.get(e.source()) == null) {
        parseNodePlaceMent(g, e, er);
      }
      if (layerStyles.size() > depth) {
        nodePlacerMap.set(e.target(), layerStyles.get(depth));
      }
      parseTargetPort(g, e, er);
      g.unselectAll();
      calcLayout();
      g.setSelected(e.target(), true);
    }

    private void parseNodePlaceMent(Graph2D g, Edge e, EdgeRealizer er) {
      YPoint firstPoint = er.bendCount() > 0 ? new YPoint(er.firstBend().getX(), er.firstBend().getY()) :
          g.getTargetPointAbs(e);
      NodeRealizer source = g.getRealizer(e.source());
      double dx = firstPoint.x - source.getCenterX();
      double dy = firstPoint.y - source.getCenterY();
      final byte placement;
      final byte alignment = DefaultNodePlacer.ALIGNMENT_MEDIAN;
      final byte routing = DefaultNodePlacer.ROUTING_FORK;
      if (Math.abs(dx) > Math.abs(dy)) {
        if (dx > 0.0) {
          placement = DefaultNodePlacer.PLACEMENT_VERTICAL_TO_RIGHT;
        } else {
          placement = DefaultNodePlacer.PLACEMENT_VERTICAL_TO_LEFT;
        }
      } else {
        if (dy > 0.0) {
          placement = DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD;
        } else {
          placement = DefaultNodePlacer.PLACEMENT_HORIZONTAL_UPWARD;
        }
      }
      nodePlacerMap.set(e.source(), new DefaultNodePlacer(placement, alignment, routing, hDistance, vDistance));
    }

    private void parseTargetPort(Graph2D g, Edge e, EdgeRealizer er) {
      if (er.bendCount() > 0) {
        YPoint lastPoint = new YPoint(er.lastBend().getX(), er.lastBend().getY());
        NodeRealizer target = g.getRealizer(e.target());
        double dx = lastPoint.x - target.getCenterX();
        double dy = lastPoint.y - target.getCenterY();
        byte side = PortConstraint.ANY_SIDE;
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
      NodeRealizer retValue;
      retValue = super.createChildNodeRealizer();
      retValue.setLabelText("");
      return retValue;
    }

  }


  final class TreeLayouterPopupMode extends PopupMode {
    private JPopupMenu nodePlacementMenu;

    TreeLayouterPopupMode() {
      nodePlacementMenu = new JPopupMenu();
      JMenu alignment = new JMenu("Root node Alignment");
      JMenu placement = new JMenu("Child Placement");
      JMenu routing = new JMenu("Routing Style");

      nodePlacementMenu.add(placement);
      nodePlacementMenu.add(alignment);
      nodePlacementMenu.add(routing);

      placement.add(new PlacementAction("Horizontally Downwards", DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD));
      placement.add(new PlacementAction("Horizontally Upwards", DefaultNodePlacer.PLACEMENT_HORIZONTAL_UPWARD));
      placement.add(new PlacementAction("Vertically to Left", DefaultNodePlacer.PLACEMENT_VERTICAL_TO_LEFT));
      placement.add(new PlacementAction("Vertically to right", DefaultNodePlacer.PLACEMENT_VERTICAL_TO_RIGHT));

      alignment.add(new AlignmentAction("Offset Leading", DefaultNodePlacer.ALIGNMENT_LEADING_OFFSET));
      alignment.add(new AlignmentAction("Leading", DefaultNodePlacer.ALIGNMENT_LEADING));
      alignment.add(new AlignmentAction("Centered", DefaultNodePlacer.ALIGNMENT_CENTER));
      alignment.add(new AlignmentAction("Median", DefaultNodePlacer.ALIGNMENT_MEDIAN));
      alignment.add(new AlignmentAction("Trailing", DefaultNodePlacer.ALIGNMENT_TRAILING));
      alignment.add(new AlignmentAction("Offset Trailing", DefaultNodePlacer.ALIGNMENT_TRAILING_OFFSET));

      routing.add(new RoutingAction("Fork", DefaultNodePlacer.ROUTING_FORK));
      routing.add(new RoutingAction("Fork at Root", DefaultNodePlacer.ROUTING_FORK_AT_ROOT));
      routing.add(new RoutingAction("Poly Line", DefaultNodePlacer.ROUTING_POLY_LINE));
      routing.add(new RoutingAction("Straight", DefaultNodePlacer.ROUTING_STRAIGHT));
    }

    public JPopupMenu getNodePopup(final Node v) {
      return nodePlacementMenu;
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      if (getGraph2D().selectedNodes().ok()) {
        return nodePlacementMenu;
      } else {
        return null;
      }
    }

  }

  abstract class AssignLayouterAction extends AbstractAction {

    protected AssignLayouterAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      NodeList selectedNodes = new NodeList(IncrementalTreeLayouterDemo.this.view.getGraph2D().selectedNodes());

      NodePlacer placer = (NodePlacer) nodePlacerMap.get(selectedNodes.firstNode());
      placer = getPlacer(placer);

      DefaultNodePlacer dnp = (DefaultNodePlacer) placer;
      for (NodeCursor nc = selectedNodes.nodes(); nc.ok(); nc.next()) {
        nodePlacerMap.set(nc.node(), new DefaultNodePlacer(
            dnp.getChildPlacement(),
            dnp.getRootAlignment(),
            dnp.getRoutingStyle(),
            dnp.getHorizontalDistance(),
            dnp.getVerticalDistance()));
      }
      calcLayout();
    }

    protected abstract NodePlacer getPlacer(NodePlacer placer);
  }

  final class PlacementAction extends AssignLayouterAction {

    private byte newPlacement;

    public PlacementAction(String name, byte newPlacement) {
      super(name);
      this.newPlacement = newPlacement;
    }

    protected NodePlacer getPlacer(NodePlacer placer) {
      if (placer instanceof DefaultNodePlacer) {
        placer = (DefaultNodePlacer) ((DefaultNodePlacer) placer).clone();
        ((DefaultNodePlacer) placer).setChildPlacement(newPlacement);
      } else {
        placer = new DefaultNodePlacer(newPlacement, DefaultNodePlacer.ALIGNMENT_MEDIAN, hDistance, vDistance);
      }
      return placer;
    }
  }

  final class AlignmentAction extends AssignLayouterAction {
    private byte newAlignment;

    public AlignmentAction(String name, byte newAlignment) {
      super(name);
      this.newAlignment = newAlignment;
    }

    protected NodePlacer getPlacer(NodePlacer placer) {
      if (placer instanceof DefaultNodePlacer) {
        placer = (DefaultNodePlacer) ((DefaultNodePlacer) placer).clone();
        ((DefaultNodePlacer) placer).setRootAlignment(newAlignment);
      } else {
        placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, newAlignment, hDistance,
            vDistance);
      }
      return placer;
    }
  }

  final class RoutingAction extends AssignLayouterAction {
    private byte newRouting;

    public RoutingAction(String name, byte newRouting) {
      super(name);
      this.newRouting = newRouting;
    }

    protected NodePlacer getPlacer(NodePlacer placer) {
      if (placer instanceof DefaultNodePlacer) {
        placer = (DefaultNodePlacer) ((DefaultNodePlacer) placer).clone();
        ((DefaultNodePlacer) placer).setRoutingStyle(newRouting);
      } else {
        placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD,
            DefaultNodePlacer.ALIGNMENT_CENTER, newRouting, hDistance, vDistance);
      }
      return placer;
    }
  }

  final class TreeHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);
      calcLayout();
    }
  }

  final class TreeCreateEditMode extends EditMode {
    TreeCreateEditMode() {
      super();
      setMoveSelectionMode(paMode = new TreePortAssignmentMode());
      setCreateEdgeMode(new TreeCreateChildEdgeMode());
      setHotSpotMode(new TreeHotSpotMode());
      setPopupMode(new TreeLayouterPopupMode());
    }

    public boolean doAllowNodeCreation() {
      return getGraph2D().N() == 0;
    }

    protected void nodeCreated(Node v) {
      super.nodeCreated(v);
      nodePlacerMap.set(v, configPanel.createPlacerCopy());
    }

  }

  final class TreePortAssignmentMode extends PortAssignmentMoveSelectionMode {
    TreePortAssignmentMode() {
      super(null, null);
    }

    protected boolean isPortReassignmentAllowed(Edge edge, boolean source) {
      return !source;
    }

//    protected void portConstraintsUpdated(Edge onEdge)
//    {
//      calcLayout();
//    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
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
        (new IncrementalTreeLayouterDemo()).start("Incremental Tree Layouter Demo");
      }
    });
  }
}
