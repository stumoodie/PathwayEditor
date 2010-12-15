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
package demo.layout.hierarchic;

import demo.view.DemoDefaults;

import y.base.DataMap;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YInsets;
import y.layout.grouping.Grouping;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.util.Maps;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.GenericNodeRealizer.Factory;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.HitInfo;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShinyPlateNodePainter;
import y.view.ViewMode;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.GroupLayoutConfigurator;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Table;
import y.view.tabular.TableNodePainter;
import y.view.tabular.TableStyle;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;

/**
 * This demo shows the effect of combining
 * <code>IncrementalHierarchicLayouter</code>'s support for grouping and
 * swim lanes.
 * <p>
 * Things to try:
 * </p>
 * <ul>
 *   <li>
 *     Drag a node or set of nodes into another swim lane.
 *     This will automatically trigger an incremental layout calculation.
 *   </li>
 *   <li>
 *     Create a new node. It will be assigned to either a new swim lane if
 *     created to the left or right of the existing lanes or to the lane in
 *     which the node's center lies.
 *     This will automatically trigger an incremental layout calculation.
 *   </li>
 *   <li>
 *     Open/close folder/group nodes. Upon closing a group node, the resulting
 *     folder node will be assigned to the minimum swim lane of the group's
 *     child nodes.
 *     This will automatically trigger an incremental layout calculation.
 *   </li>
 * </ul>
 *
 */
public class SwimlaneGroupDemo extends IncrementalHierarchicGroupDemo {
  private static final Color NODE_COLOR = DemoDefaults.DEFAULT_NODE_COLOR;
  private static final Color NODE_GRADIENT_COLOR = Color.WHITE;
  private static final Color NODE_LINE_COLOR = DemoDefaults.DEFAULT_NODE_LINE_COLOR;
  private static final Color GROUP_NODE_COLOR = new Color(255, 255, 255, 127);
  private static final Color GROUP_NODE_LINE_COLOR = DemoDefaults.DEFAULT_NODE_COLOR;
  private static final Color GROUP_NODE_LABEL_COLOR = DemoDefaults.DEFAULT_NODE_COLOR;
  private static final Color ODD_LANE_COLOR = DemoDefaults.DEFAULT_CONTRAST_COLOR;
  private static final Color EVEN_LANE_COLOR = new java.awt.Color(237, 247, 247);

  private static final String NODE_CONFIGURATION = "NODE_CONFIGURATION";
  private static final String SWIMLANE_CONFIGURATION = "SWIMLANE_CONFIGURATION";

  static {
    initConfigurations();
  }


  public SwimlaneGroupDemo() {
    view.addViewMode(new TriggerIncrementalLayout());
    configureRealizers(view.getGraph2D());
    loadInitialGraph();
  }

  /**
   * Creates a sample graph to display initially.
   */
  protected void loadInitialGraph() {
    final Graph2D graph = view.getGraph2D();
    graph.clear();
   
    HierarchyManager hierarchy = getHierarchyManager();
    
    if (layouter != null && hierarchy != null) {
      // create a dummy node that visualizes swim lanes
      final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
      tgnr.setConfiguration(SWIMLANE_CONFIGURATION);

      tgnr.setLabelText("Swimlane Pool");
      tgnr.getLabel().setPosition(NodeLabel.TOP);
      // "removes" the label from the graph view,
      // but keeps it in the tree component
      tgnr.getLabel().setVisible(false);

      // configure swim lane colors
      final TableStyle.SimpleStyle oddLane =
          new TableStyle.SimpleStyle(null, null, ODD_LANE_COLOR);
      tgnr.setStyleProperty(TableNodePainter.COLUMN_STYLE_ID, oddLane);
      tgnr.setStyleProperty(TableNodePainter.COLUMN_SELECTION_STYLE_ID, oddLane);

      final TableStyle.SimpleStyle evenLane =
          new TableStyle.SimpleStyle(null, null, EVEN_LANE_COLOR);
      tgnr.setStyleProperty(TableNodePainter.ALTERNATE_COLUMN_STYLE_ID, evenLane);
      tgnr.setStyleProperty(TableNodePainter.ALTERNATE_COLUMN_SELECTION_STYLE_ID, evenLane);

      final TableStyle.SimpleStyle none = new TableStyle.SimpleStyle();
      tgnr.setStyleProperty(TableNodePainter.ROW_STYLE_ID, none);
      tgnr.setStyleProperty(TableNodePainter.ROW_SELECTION_STYLE_ID, none);
      tgnr.setStyleProperty(TableNodePainter.TABLE_STYLE_ID, none);
      tgnr.setStyleProperty(TableNodePainter.TABLE_SELECTION_STYLE_ID, none);

      // configure swim lane insets and minimum size
      tgnr.setDefaultColumnInsets(new YInsets(25, 5, 0, 5));
      tgnr.setDefaultMinimumColumnWidth(50);
      tgnr.setDefaultRowInsets(new YInsets(15, 0, 15, 0));

      // label swim lanes
      final Column[] columns = new Column[9];
      final Table table = tgnr.getTable();
      for (int i = 0; i < columns.length; ++i) {
        columns[i] = i == 0 ? table.getColumn(0) : table.addColumn();

        final NodeLabel nl = tgnr.createNodeLabel();
        nl.setText("Lane " + (i + 1));
        tgnr.configureColumnLabel(nl, columns[i], true, 0);
        tgnr.addLabel(nl);
      }

      tgnr.updateTableBounds();


      final Node pool = hierarchy.createGroupNode(graph);
      graph.setRealizer(pool, tgnr);

      final Node n00 = graph.createNode();
      final Node n01 = graph.createNode();
      final Node g03 = hierarchy.createGroupNode(graph);
      final Node g04 = hierarchy.createGroupNode(graph);
      final Node n05 = graph.createNode();
      final Node n06 = graph.createNode();
      final Node n07 = graph.createNode();
      final Node g08 = hierarchy.createGroupNode(graph);
      final Node n09 = graph.createNode();
      final Node n10 = graph.createNode();
      final Node n11 = graph.createNode();
      final Node g12 = hierarchy.createGroupNode(graph);
      final Node n13 = graph.createNode();
      final Node n14 = graph.createNode();
      final Node n15 = graph.createNode();
      final Node n16 = graph.createNode();
      final Node n17 = graph.createNode();
      final Node g18 = hierarchy.createGroupNode(graph);
      final Node n19 = graph.createNode();
      final Node n20 = graph.createNode();
      final Node n21 = graph.createNode();
      final Node n22 = graph.createNode();
      final Node n23 = graph.createNode();

      // configure node nesting hierarchy
      hierarchy.setParentNode(n00, pool);
      hierarchy.setParentNode(n01, pool);

      hierarchy.setParentNode(g03, pool);
      hierarchy.setParentNode(g04, pool);
      hierarchy.setParentNode(n05, pool);
      hierarchy.setParentNode(n06, pool);
      hierarchy.setParentNode(n07, pool);

      hierarchy.setParentNode(g08, g03);
      hierarchy.setParentNode(n09, g03);
      hierarchy.setParentNode(n10, g03);
      hierarchy.setParentNode(n11, g03);

      hierarchy.setParentNode(g12, g08);
      hierarchy.setParentNode(n13, g08);
      hierarchy.setParentNode(n14, g08);

      hierarchy.setParentNode(n15, g12);
      hierarchy.setParentNode(n16, g12);
      hierarchy.setParentNode(n17, g12);

      hierarchy.setParentNode(g18, g04);
      hierarchy.setParentNode(n19, g04);
      hierarchy.setParentNode(n20, g04);

      hierarchy.setParentNode(n21, g18);
      hierarchy.setParentNode(n22, g18);
      hierarchy.setParentNode(n23, g18);


      hierarchy.createEdge(n00, n01);
      hierarchy.createEdge(n01, n06);
      hierarchy.createEdge(n06, n07);
      hierarchy.createEdge(n06, n05);
      hierarchy.createEdge(n06, n20);
      hierarchy.createEdge(n07, n11);
      hierarchy.createEdge(n09, n05);
      hierarchy.createEdge(n10, n05);
      hierarchy.createEdge(n11, n09);
      hierarchy.createEdge(n11, n14);
      hierarchy.createEdge(n13, n09);
      hierarchy.createEdge(n14, n13);
      hierarchy.createEdge(n14, n15);
      hierarchy.createEdge(n15, n13);
      hierarchy.createEdge(n15, n17);
      hierarchy.createEdge(n16, n13);
      hierarchy.createEdge(n17, n16);
      hierarchy.createEdge(n19, n05);
      hierarchy.createEdge(n20, n19);
      hierarchy.createEdge(n20, n21);
      hierarchy.createEdge(n21, n22);
      hierarchy.createEdge(n21, n23);
      hierarchy.createEdge(n21, n05);
      hierarchy.createEdge(n22, n05);
      hierarchy.createEdge(n23, n05);

      // create initial swim lane affiliations for nodes
      table.moveToColumn(n00, columns[8]);
      table.moveToColumn(n01, columns[5]);
      // g02
      // g03
      // g04
      table.moveToColumn(n05, columns[5]);
      table.moveToColumn(n06, columns[5]);
      table.moveToColumn(n07, columns[1]);
      // g08
      table.moveToColumn(n09, columns[1]);
      table.moveToColumn(n10, columns[0]);
      table.moveToColumn(n11, columns[1]);
      // g12
      table.moveToColumn(n13, columns[2]);
      table.moveToColumn(n14, columns[2]);
      table.moveToColumn(n15, columns[3]);
      table.moveToColumn(n16, columns[2]);
      table.moveToColumn(n17, columns[4]);
      // g18
      table.moveToColumn(n19, columns[7]);
      table.moveToColumn(n20, columns[6]);
      table.moveToColumn(n21, columns[6]);
      table.moveToColumn(n22, columns[6]);
      table.moveToColumn(n23, columns[6]);

      // update node labels to display swim lane affiliation
      initLabels(graph);

      layout();
    }

    view.fitContent();
    view.getGraph2D().updateViews();
  }

  /**
   * Updates node labels to display either group or folder state or
   * for normal nodes the associated swim lane.
   */
  private void initLabels(final Graph2D graph) {
    Node tableNode = null;
    Table table = null;
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final NodeRealizer nr = graph.getRealizer(nc.node());
      if (nr instanceof TableGroupNodeRealizer) {
        tableNode = nc.node();
        table = ((TableGroupNodeRealizer) nr).getTable();
        break;
      }
    }

    HierarchyManager hierarchy = getHierarchyManager();
    
    int grp = 0;
    int fldr = 0;
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      if (nc.node() == tableNode) {
        continue;
      }
            
      if (hierarchy.isNormalNode(nc.node())) {
        final Column column = table == null ? null : table.getColumn(nc.node());
        if (column != null) {
          graph.getRealizer(nc.node()).setLabelText(
              Integer.toString(column.getIndex() + 1));
        } else {
          graph.getRealizer(nc.node()).setLabelText("");
        }
      } else if (hierarchy.isGroupNode(nc.node())) {
        graph.getRealizer(nc.node()).setLabelText(
            "Group " + (++grp));
      } else if (hierarchy.isFolderNode(nc.node())) {
        graph.getRealizer(nc.node()).setLabelText(
            "Folder " + (++fldr));
      }
    }
  }

  /*
  * #####################################################################
  * overriden methods
  * #####################################################################
  */

  /**
   * Overwritten to configure incremental layout to take swim lane pool nodes
   * into account.
   */
  void layoutIncrementally() {
    Graph2D graph = view.getGraph2D();

    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);

    // create storage for both nodes and edges
    DataMap incrementalElements = Maps.createHashedDataMap();
    // configure the mode
    final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

    //prepare grouping information
    final GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph);
    glc.prepareAll();
    final Grouping grouping = new Grouping(graph);

    //mark incremental elements
    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      incrementalElements.set(n, ihf.createLayerIncrementallyHint(nc.node()));
      if (grouping.isGroupNode(n)) {
        //also mark the group node's incoming/outgoing edges
        EdgeList markedEdges = grouping.getEdgesGoingIn(n);
        markedEdges.addAll(grouping.getEdgesGoingOut(n));
        for (EdgeCursor ec = markedEdges.edges(); ec.ok(); ec.next()) {
          incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
        }
      }
    }
    grouping.dispose();
    glc.restoreAll();

    for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
      incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
    }
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);

    try {
      final Graph2DLayoutExecutor layoutExecutor =
              new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED);
      layoutExecutor.setConfiguringTableNodeRealizers(true);
      layoutExecutor.doLayout(view, layouter);
    } finally {
      graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
    }

    // update node labels to display swim lane affiliation
    initLabels(graph);
    graph.updateViews();
  }

  /**
   * Overwritten to configure layout to take swim lane pool nodes into account.
   */
  void layout() {
    final Graph2D graph = view.getGraph2D();
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.setConfiguringTableNodeRealizers(true);
    layoutExecutor.doLayout(view, layouter);

    // update node labels to display swim lane affiliation
    initLabels(graph);
    graph.updateViews();
  }

  /*
  * #####################################################################
  * GUI
  * #####################################################################
  */

  protected void addLayoutActions(JToolBar toolBar) {
    toolBar.addSeparator();
    toolBar.add(new AbstractAction("New Layout") {
      public void actionPerformed(ActionEvent e) {
        layout();
      }
    });    
  }  
    
  protected EditMode createEditMode() {
    final EditMode editMode = new EditMode() {
      protected void nodeCreated(final Node v) {
        layoutIncrementally();
      }      
    };
    // listen for clicks on state icon +/- of group and folder nodes.  
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);

    // do not automatically create node labels as these are used to display
    // swim lane affiliation of nodes
    editMode.assignNodeLabel(false);

    // activate child node creation when clicking into group nodes
    editMode.setChildNodeCreationEnabled(true);
    
    return editMode;
  }

  void configureRealizers(final Graph2D graph) {
    graph.setDefaultNodeRealizer(createDefaultNodeRealizer());
    final DefaultHierarchyGraphFactory hgf =
        (DefaultHierarchyGraphFactory) graph.getHierarchyManager().getGraphFactory();
    hgf.setDefaultGroupNodeRealizer(createDefaultGroupNodeRealizer());
    hgf.setDefaultFolderNodeRealizer(createDefaultFolderNodeRealizer());
  }

  private NodeRealizer createDefaultNodeRealizer() {
    Factory factory = GenericNodeRealizer.getFactory();
    Map map = factory.createDefaultConfigurationMap();
    ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
    map.put(GenericNodeRealizer.Painter.class, painter);
    map.put(GenericNodeRealizer.ContainsTest.class, painter);
    factory.addConfiguration(NODE_CONFIGURATION, map);
    GenericNodeRealizer gnr = new GenericNodeRealizer(NODE_CONFIGURATION);
    gnr.setFillColor(NODE_COLOR);
    gnr.setLineColor(NODE_LINE_COLOR);
    gnr.setFillColor2(NODE_GRADIENT_COLOR);
    gnr.setLineType(LineType.LINE_1);
    return gnr;
  }

  private NodeRealizer createDefaultGroupNodeRealizer() {
    GenericGroupNodeRealizer defaultGroup = new GenericGroupNodeRealizer();
    defaultGroup.setConfiguration(CONFIGURATION_GROUP);
    defaultGroup.setSize(100, 60);
    defaultGroup.setFillColor(GROUP_NODE_COLOR);
    defaultGroup.setGroupClosed(false);
    defaultGroup.setLineType(LineType.LINE_2);
    defaultGroup.setLineColor(GROUP_NODE_LINE_COLOR);
    defaultGroup.getLabel().setBackgroundColor(GROUP_NODE_LABEL_COLOR);
    defaultGroup.getLabel().setTextColor(getBlackOrWhite(GROUP_NODE_LABEL_COLOR));   
    return defaultGroup;
  }

  private NodeRealizer createDefaultFolderNodeRealizer() {
    GenericGroupNodeRealizer defaultFolder = new GenericGroupNodeRealizer();
    defaultFolder.setConfiguration(CONFIGURATION_GROUP);    
    defaultFolder.setSize(100, 60);
    defaultFolder.setFillColor(GROUP_NODE_COLOR);
    defaultFolder.setGroupClosed(true);
    defaultFolder.setLineType(LineType.LINE_2);
    defaultFolder.setLineColor(GROUP_NODE_LINE_COLOR);
    defaultFolder.getLabel().setBackgroundColor(GROUP_NODE_LABEL_COLOR);
    defaultFolder.getLabel().setTextColor(getBlackOrWhite(GROUP_NODE_LABEL_COLOR));
    return defaultFolder;
  }

  private Color getBlackOrWhite(Color c) {
    if (c.getRed() + c.getGreen() + c.getBlue() > 3 * 127) {
      return Color.BLACK;
    } else {
      return Color.WHITE;
    }
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SwimlaneGroupDemo()).start();
      }
    });
  }


  /**
   * Registers the configuration for the <code>TableGroupNodeRealizer</code>
   * that is used to display swim lanes.
   */
  private static void initConfigurations() {
    // create a configuration that uses alternating colors for swim lanes
    final Map map = TableGroupNodeRealizer.createDefaultConfigurationMap();
    map.put(TableGroupNodeRealizer.Painter.class,
        TableNodePainter.newAlternatingColumnsInstance());
    map.put(TableGroupNodeRealizer.GenericMouseInputEditorProvider.class, null);

    // register the configuration
    TableGroupNodeRealizer.getFactory()
        .addConfiguration(SWIMLANE_CONFIGURATION, map);
  }


  /**
   * <code>ViewMode</code> that triggers an incremental layout calculation
   * after node drag operations.
   */
  class TriggerIncrementalLayout extends ViewMode {
    private boolean dragging;
    private boolean hasHitNodes;

    TriggerIncrementalLayout() {
      this.dragging = false;
      this.hasHitNodes = false;
    }

    public void mouseDraggedLeft(final double x, final double y) {
      dragging = true;
    }

    public void mousePressedLeft(final double x, final double y) {
      final HitInfo info = new HitInfo(view, x, y, true, HitInfo.NODE);
      hasHitNodes = info.hasHitNodes();
    }

    public void mouseReleasedLeft(final double x, final double y) {
      if (dragging && hasHitNodes) {
        layoutIncrementally();
      }
      hasHitNodes = false;
      dragging = false;
    }
  }
}
