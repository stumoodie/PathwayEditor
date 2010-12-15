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
import demo.view.hierarchy.GroupingDemo;
import y.base.DataMap;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.Maps;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;
import java.awt.Frame;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;


/**
 * This demo showcases how IncrementalHierarchicLayouter can be used to fully or incrementally
 * layout hierarchically nested graphs. The demo supports automatic relayout after expanding folder nodes,
 * collapsing group nodes. Furthermore it provides toolbar buttons that
 * trigger full layout and incremental relayout. A settings dialog for group layout options is provided as well.
 * In incremental layout mode all selected elements are added incrementally to the existing layout.
 */
public class IncrementalHierarchicGroupDemo extends GroupingDemo {

  IncrementalHierarchicLayouter layouter;
  OptionHandler groupLayoutOptions;

  public IncrementalHierarchicGroupDemo() {

    //configure layout algorithm
    layouter = new IncrementalHierarchicLayouter();
    layouter.setOrthogonallyRouted(true);
    layouter.setRecursiveGroupLayeringEnabled(false);

    //prepare option handler for group layout options
    Object[] groupStrategyEnum = {"Global Layering", "Recursive Layering"};
    Object[] groupAlignmentEnum = {"Top", "Center", "Bottom"};
    groupLayoutOptions = new OptionHandler("Grouplayout Options");
    ConstraintManager cm = new ConstraintManager(groupLayoutOptions);
    OptionItem gsi = groupLayoutOptions.addEnum("Group Layering Strategy", groupStrategyEnum, 0);
    OptionItem eci = groupLayoutOptions.addBool("Enable Compact Layering", true);
    OptionItem gai = groupLayoutOptions.addEnum("Group Alignment", groupAlignmentEnum, 0);
    cm.setEnabledOnValueEquals(gsi, "Recursive Layering", eci);
    cm.setEnabledOnValueEquals(gsi, "Recursive Layering", gai);
    cm.setEnabledOnCondition(cm.createConditionValueEquals(gsi, "Recursive Layering").and(
        cm.createConditionValueEquals(eci, Boolean.TRUE).inverse()), gai);

    view.fitContent();
  }

  /**
   * Register custom open folder and close group actions that adjust the layout of the graph.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    view.getCanvasComponent().getActionMap().put(Graph2DViewActions.CLOSE_GROUPS, new CloseGroupsAndLayoutAction());
    view.getCanvasComponent().getActionMap().put(Graph2DViewActions.OPEN_FOLDERS, new OpenFoldersAndLayoutAction());
  }

  /**
   * Expand a folder node. After expanding the folder node, an incremental layout is automatically triggered.
   * For this, the expanded node and all of its descendants will be treated as incremental elements.
   */
  class OpenFoldersAndLayoutAction extends Graph2DViewActions.OpenFoldersAction {

    public OpenFoldersAndLayoutAction() {
      super(IncrementalHierarchicGroupDemo.this.view);
    }

    public void openFolder(Node groupNode, Graph2D graph) {
      NodeList children = new NodeList(graph.getHierarchyManager().getInnerGraph(groupNode).nodes());
      super.openFolder(groupNode, graph);
      graph.unselectAll();
      graph.setSelected(groupNode, true);
      for (NodeCursor nc = children.nodes(); nc.ok(); nc.next()) {
        graph.setSelected(nc.node(), true);
      }

      layoutIncrementally();

      graph.unselectAll();
      graph.setSelected(groupNode, true);
      graph.updateViews();
     
    }
  }

  /**
   * Collapse a group node. After collapsing the group node, an incremental layout is automatically triggered.
   * For this, the collapsed node is treated as an incremental element.
   */
  class CloseGroupsAndLayoutAction extends Graph2DViewActions.CloseGroupsAction {
    public CloseGroupsAndLayoutAction() {
      super(IncrementalHierarchicGroupDemo.this.view);
    }
    public void closeGroup(Node groupNode, Graph2D graph) {
      super.closeGroup(groupNode, graph);
      graph.unselectAll();
      graph.setSelected(groupNode, true);
      for (EdgeCursor ec = groupNode.edges(); ec.ok(); ec.next()) {
        graph.setSelected(ec.edge(), true);
      }

      layoutIncrementally();
      graph.unselectAll();

      graph.updateViews();      
    }
  }
  
  
  /**
   * Loads the initial graph
   */
  protected void loadInitialGraph() {
    loadGraph("resource/IncrementalHierarchicGroupDemo.graphml");
  }

  /**
   * Creates the toolbar for the demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();    
    addLayoutActions(toolBar);    
    return toolBar;
  }

  protected void addLayoutActions(JToolBar toolBar) {
    toolBar.addSeparator();
    toolBar.add(new AbstractAction("Incremental Layout") {
      public void actionPerformed(ActionEvent e) {
        layoutIncrementally();
      }
    });
    toolBar.add(new AbstractAction("New Layout") {
      public void actionPerformed(ActionEvent e) {
        layout();
      }
    });
    toolBar.add(new AbstractAction("Group Layout Options...") {
      public void actionPerformed(ActionEvent e) {
        groupLayoutOptions.showEditor((Frame) view.getTopLevelAncestor());
        configureGroupLayout();
      }
    });
  }

  /**
   * Configures the layouter options relevant for grouping.
   */
  void configureGroupLayout() {
    Object gsi = groupLayoutOptions.get("Group Layering Strategy");
    if ("Recursive Layering".equals(gsi)) {
      layouter.setRecursiveGroupLayeringEnabled(true);
    } else if ("Global Layering".equals(gsi)) {
      layouter.setRecursiveGroupLayeringEnabled(false);
    }

    layouter.setGroupCompactionEnabled(groupLayoutOptions.getBool("Enable Compact Layering"));

    Object gai = groupLayoutOptions.get("Group Alignment");
    if ("Top".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP);
    } else if ("Center".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER);
    }
    if ("Bottom".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM);
    }
  }

  /**
   * Performs incremental layout. All selected elements will be treated incrementally.
   */
  void layoutIncrementally() {
    Graph2D graph = view.getGraph2D();

    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);

    // create storage for both nodes and edges
    DataMap incrementalElements = Maps.createHashedDataMap();
    // configure the mode
    final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
    }

    for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
      incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
    }
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
    try {
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
      layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
      layoutExecutor.doLayout(view, layouter);
    } finally {
      graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
    }
  }

  /**
   * Performs global layout. The new layout can strongly differ from the existing layout.
   */
  void layout() {
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
    layoutExecutor.doLayout(view, layouter);
  }

  /**
   * Launches this demo.
   * @param args ignored.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new IncrementalHierarchicGroupDemo()).start();
      }
    });
  }
}
