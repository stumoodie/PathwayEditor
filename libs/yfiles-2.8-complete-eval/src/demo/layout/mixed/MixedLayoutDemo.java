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
package demo.layout.mixed;

import y.base.Node;
import y.base.NodeMap;
import y.base.YList;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;
import y.view.hierarchy.GroupLayoutConfigurator;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.grouping.Grouping;
import y.layout.Layouter;
import y.layout.PortCandidate;
import y.layout.LayoutOrientation;
import y.layout.LayoutMultiplexer;
import y.layout.LayoutGraph;
import y.layout.tree.TreeReductionStage;
import y.layout.tree.TreeLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.GivenLayersLayerer;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.HierarchicLayouter;
import y.layout.hierarchic.incremental.LayerConstraintFactory;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.util.Maps;

import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.ActionMap;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;

import demo.view.hierarchy.GroupingDemo;

/**
 * This demo extends the GroupingDemo and shows how to use the recursive group layouter to apply different layouts to the contents of group nodes.
 * The demo shows the following two use cases:
 * <p>
 * -Table Layout: demonstrates how to realize a table node structure, i.e., each group node in the drawing represents a
 * table and the nodes within the groups the table rows. Edges are connected to specific rows.
 * The rows are sorted according to their y-coordinate in the initial drawing.
 * </p><p>
 * -Three-Tier Layout: demonstrates how to use the recursive group layout to realize different layouts of elements
 * assigned to different tiers. Each group node can be assigned to the left, right or middle tier (depending on the
 * group node label). All group nodes labeled "left" are placed on the left side. Their content is drawn using a
 * TreeLayouter with layout orientation left-to-right. Analogously, all group nodes labeled "right" are placed on the
 * right side. Their content is drawn using a TreeLayouter with layout orientation right-to-left. Elements not assigned
 * to "left" or "right" group nodes are always lay out in the middle using the IncrementalHierarchicLayouter with layout
 * orientation left-to-right. Note that group nodes not labeled "left" or "right" are handled non-recursive.
 * </p><p>
 * To recalculate the layout press the "Auto-Layout Graph" button or fold/unfold a group node.
 * If the "Use Sketch" option is enabled the IncrementalHierarchicLayouter uses the from sketch mode, i.e.,
 * it uses the current drawing as sketch.
 * </p>
 */
public class MixedLayoutDemo extends GroupingDemo {
  private static final byte TABLE_MODE = 0;
  private static final byte THREE_TIER_MODE = 1;

  private byte mode = TABLE_MODE;
  private boolean fromSketch = false;

  public MixedLayoutDemo() {
    this(null);
  }

  public MixedLayoutDemo( final String helpFilePath ) {
    super();
    addHelpPane(helpFilePath);
    loadGraph();
  }

  /** Adds an extra layout action to the toolbar */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    final JComboBox comboBox = new JComboBox(new Object[]{"Table Layout", "Three-Tier Layout"});
    comboBox.setMaximumSize(new Dimension(200, 100));
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (comboBox.getSelectedIndex()) {
          default:
          case 0:
            mode = TABLE_MODE;
            break;
          case 1:
            mode = THREE_TIER_MODE;
            break;
        }
        loadGraph();
      }
    });
    bar.add(comboBox);
    bar.addSeparator();
    bar.add(new LayoutAction());
    final JCheckBox checkBox = new JCheckBox("Use Sketch", fromSketch);
    checkBox.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fromSketch = checkBox.isSelected();
      }
    });
    bar.add(checkBox);
    return bar;
  }

  private void loadGraph() {
    if (mode == THREE_TIER_MODE) {
      loadGraph("resource/threetier.graphml");
    } else {
      loadGraph("resource/table.graphml");
    }

  }

  /**
   * Register key bindings for our custom actions.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.CLOSE_GROUPS, new MyCloseGroupsAction());
    actionMap.put(Graph2DViewActions.OPEN_FOLDERS, new MyOpenFoldersAction());
  }

  //action performs common behavior and applies a layout afterwards
  class MyCloseGroupsAction extends Graph2DViewActions.CloseGroupsAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  //action performs common behavior and applies a layout afterwards
  class MyOpenFoldersAction extends Graph2DViewActions.OpenFoldersAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  /**
   * Layout action that configures and launches a layout algorithm.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Auto-Layout Graph");
    }

    public void actionPerformed(ActionEvent e) {
      doLayout();
    }
  }

  private void doLayout() {
    if (mode == THREE_TIER_MODE) {
      applyThreeTierLayout();
    } else {
      applyTableLayout();
    }
  }

  /** Configures and invokes the table layout algorithm */
  void applyTableLayout() {
    Graph2D graph = view.getGraph2D();

    //set up port candidates for edges (edges should be attached to the left/right side of the corresponding nodes)
    YList candidates = new YList();
    candidates.add(PortCandidate.createCandidate(PortCandidate.WEST));
    candidates.add(PortCandidate.createCandidate(PortCandidate.EAST));
    graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));
    graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));

    //configure layout algorithms
    final RowLayouter rowLayouter = new RowLayouter(); //used for layouting the nodes (rows) within the group nodes (tables)

    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter(); //used for the core layout
    ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    if (fromSketch) {
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    }
    ihl.setOrthogonallyRouted(true);

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return rowLayouter;
      }
    });

    //prepare grouping information
    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph);
    glc.prepareAll();

    //do layout
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(ihl);
    rgl.setAutoAssignPortCandidatesEnabled(true);
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl);

    //dispose
    glc.restoreAll();
    graph.removeDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY);
    graph.removeDataProvider(PortCandidate.TARGET_PCLIST_DPKEY);
    graph.removeDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY);

    view.updateView();
    view.fitContent();
  }

  private static final byte COMMON_NODE = 0;
  private static final byte LEFT_TREE_GROUP_NODE = 1;
  private static final byte LEFT_TREE_CONTENT_NODE = 2;
  private static final byte RIGHT_TREE_GROUP_NODE = 3;
  private static final byte RIGHT_TREE_CONTENT_NODE = 4;

  //determines the type of a node (used for the subgraph layout demo)
  private byte getType(Node n, Grouping grouping, Graph2D graph) {
    if (grouping.isGroupNode(n)) {
      NodeRealizer realizer = graph.getRealizer(n);
      if (realizer.getLabelText().equals("left")) {
        return LEFT_TREE_GROUP_NODE;
      } else if (realizer.getLabelText().equals("right")) {
        return RIGHT_TREE_GROUP_NODE;
      } else {
        return COMMON_NODE;
      }
    } else {
      Node groupNode = grouping.getParent(n);
      if (groupNode != null) {
        NodeRealizer realizer = graph.getRealizer(groupNode);
        if (realizer.getLabelText().equals("left")) {
          return LEFT_TREE_CONTENT_NODE;
        } else if (realizer.getLabelText().equals("right")) {
          return RIGHT_TREE_CONTENT_NODE;
        } else {
          return COMMON_NODE;
        }
      } else {
        NodeRealizer realizer = graph.getRealizer(n);
        if (realizer.getLabelText().equals("left")) {
          return LEFT_TREE_GROUP_NODE;
        } else if (realizer.getLabelText().equals("right")) {
          return RIGHT_TREE_GROUP_NODE;
        } else {
          return COMMON_NODE;
        }
      }
    }
  }

  /**
   * Configures and invokes a layout algorithm
   */
  void applyThreeTierLayout() {
    final Graph2D graph = view.getGraph2D();

    //configure the different layout settings
    final TreeReductionStage leftToRightTreeLayouter = new TreeReductionStage();
    leftToRightTreeLayouter.setNonTreeEdgeRouter(leftToRightTreeLayouter.createStraightlineRouter());
    TreeLayouter tl1 = new TreeLayouter();
    tl1.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    tl1.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
    leftToRightTreeLayouter.setCoreLayouter(tl1);

    final TreeReductionStage rightToLeftTreeLayouter = new TreeReductionStage();
    rightToLeftTreeLayouter.setNonTreeEdgeRouter(rightToLeftTreeLayouter.createStraightlineRouter());
    TreeLayouter tl2 = new TreeLayouter();
    tl2.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    tl2.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
    rightToLeftTreeLayouter.setCoreLayouter(tl2);

    final IncrementalHierarchicLayouter partitionLayouter = new IncrementalHierarchicLayouter(); //configure the core layout
    partitionLayouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    if (fromSketch) {
      partitionLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    }

    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph); //prepare the grouping information
    glc.prepareAll();
    final Grouping grouping = new Grouping(graph);

    if (!fromSketch) {
      //insert layer constraints to guarantee the desired placement for "left" and "right" group nodes
      LayerConstraintFactory lcf = partitionLayouter.createLayerConstraintFactory(graph);
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        byte type = getType(n, grouping, graph);
        if (type == LEFT_TREE_GROUP_NODE) {
          lcf.addPlaceNodeAtTopConstraint(n);
        } else if (type == RIGHT_TREE_GROUP_NODE) {
          lcf.addPlaceNodeAtBottomConstraint(n);
        }
      }
    }

    //align tree group nodes within their layer
    NodeMap node2LayoutDescriptor = Maps.createHashedNodeMap();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      byte type = getType(n, grouping, graph);
      if (type == LEFT_TREE_GROUP_NODE) {
        NodeLayoutDescriptor nld = new NodeLayoutDescriptor();
        nld.setLayerAlignment(1.0d);
        node2LayoutDescriptor.set(n, nld);
      } else if (type == RIGHT_TREE_GROUP_NODE) {
        NodeLayoutDescriptor nld = new NodeLayoutDescriptor();
        nld.setLayerAlignment(0.0d);
        node2LayoutDescriptor.set(n, nld);
      }
    }
    graph.addDataProvider(HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY, node2LayoutDescriptor);

    //map each group node to the layout algorithm that should be used for its content
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        byte type = getType((Node) dataHolder, grouping, graph);
        if (type == LEFT_TREE_GROUP_NODE) {
          return leftToRightTreeLayouter;
        } else if (type == RIGHT_TREE_GROUP_NODE) {
          return rightToLeftTreeLayouter;
        } else {
          return null; //handled non-recursive
        }
      }
    });

    //each edge should be attached to the left or right side of the corresponding node
    final YList candidates = new YList();
    candidates.add(PortCandidate.createCandidate(PortCandidate.WEST));
    candidates.add(PortCandidate.createCandidate(PortCandidate.EAST));
    graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));
    graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));

    //launch layout algorithm
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(partitionLayouter);
    rgl.setAutoAssignPortCandidatesEnabled(true);
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl);

    //dispose
    grouping.dispose();
    glc.restoreAll();
    graph.removeDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY);
    graph.removeDataProvider(PortCandidate.TARGET_PCLIST_DPKEY);
    graph.removeDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY);
    graph.removeDataProvider(HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY);
    graph.removeDataProvider(GivenLayersLayerer.LAYER_ID_KEY);

    view.updateView();
    view.fitContent();
  }

  //used for layouting the nodes (rows) within the group nodes (tables)
  static class RowLayouter implements Layouter {
    private static final double distance = 5;

    public boolean canLayout(LayoutGraph graph) {
      return graph.edgeCount() == 0;
    }

    public void doLayout(final LayoutGraph graph) {
      Node[] rows = graph.getNodeArray();
      Arrays.sort(rows, new RowComparator(graph));

      double currentY = 0;
      for (int i = 0; i < rows.length; i++) {
        graph.setLocation(rows[i], 0, currentY); //set layout of row
        currentY += graph.getHeight(rows[i]) + distance;
      }
    }
  }

  static class RowComparator implements Comparator {
    private LayoutGraph graph;

    RowComparator(LayoutGraph graph) {
      this.graph = graph;
    }

    public int compare(Object o1, Object o2) {
      double delta = graph.getCenterY((Node) o1) - graph.getCenterY((Node) o2);
      if (delta < 0) {
        return -1;
      } else if (delta > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new MixedLayoutDemo("resource/mixedlayouthelp.html")).start();
      }
    });
  }
}
