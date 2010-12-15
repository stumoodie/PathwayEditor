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
import demo.view.DemoDefaults;
import y.algo.GraphConnectivity;
import y.algo.Trees;
import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.AbstractLayoutStage;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.LayoutTool;
import y.layout.Layouter;
import y.layout.NodeLayout;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.TreeLayouter;
import y.layout.tree.XCoordComparator;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.util.DataProviders;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NavigationMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ViewMode;
import y.view.Graph2DLayoutExecutor;
import y.view.hierarchy.GroupNodeRealizer;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.HashSet;

/**
 * This demo shows how to collapse and expand sub trees by simply clicking on
 * a root node. Several different layout algorithms can be chosen:
 * {@link y.layout.tree.TreeLayouter},
 * {@link y.layout.tree.BalloonLayouter},
 * {@link y.layout.organic.SmartOrganicLayouter} and
 * {@link y.layout.hierarchic.IncrementalHierarchicLayouter}.
 */
public class CollapsibleTreeDemo extends DemoBase {
  public static final byte STYLE_TREE = 1;
  public static final byte STYLE_BALLOON = 2;
  private static final byte STYLE_ORGANIC = 3;
  private static final byte STYLE_HIERARCHIC = 4;

  private byte style = STYLE_TREE;
  private TreeLayouter treeLayouter;
  private BalloonLayouter balloonLayouter;
  private SmartOrganicLayouter organicLayouter;
  private IncrementalHierarchicLayouter hierarchicLayouter;
  private CollapsibleTreeDemo.CollapseExpandViewMode viewMode;
  private DataMap ihlHintMap;
  private IncrementalHintsFactory hintsFactory;

  public CollapsibleTreeDemo() {
    Graph2D graph = view.getGraph2D();
    
    //create a sample tree structure
    createTree(graph);

    //collapse/expand some nodes
    viewMode.collapseSubtree(graph, Trees.getRoot(graph));
    Node root = Trees.getRoot(graph);
    viewMode.expandSubtree(graph, root, 2);

    //configure layouters
    treeLayouter = new TreeLayouter();
    treeLayouter.setComparator(new XCoordComparator()); //important to keep node order of collapsed/expanded items.
    treeLayouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    treeLayouter.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);

    balloonLayouter = new BalloonLayouter();
    balloonLayouter.setFromSketchModeEnabled(true);
    balloonLayouter.setCompactnessFactor(0.1);
    balloonLayouter.setAllowOverlaps(true);

    organicLayouter = new SmartOrganicLayouter();
    organicLayouter.setScope(SmartOrganicLayouter.SCOPE_MAINLY_SUBSET);
    organicLayouter.setMinimalNodeDistance(20);

    hierarchicLayouter = new IncrementalHierarchicLayouter();
    hierarchicLayouter.setOrthogonallyRouted(true);
    hierarchicLayouter.setLayoutOrientation(LayoutOrientation.TOP_TO_BOTTOM);
    // read the "old" nodes from the sketch
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    ((SimplexNodePlacer) hierarchicLayouter.getNodePlacer()).setBaryCenterModeEnabled(true);

    // create a map to store the hints for the incremental layout mechanism
    ihlHintMap = Maps.createHashedDataMap();
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, ihlHintMap);
    // get a reference to a hints factory
    hintsFactory = hierarchicLayouter.createIncrementalHintsFactory();

    //layout the graph
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        layout(view.getGraph2D(), null, true);
      }
    });
  }
  
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    NodeRealizer nr = view.getGraph2D().getDefaultNodeRealizer();
    NodeLabel nl = nr.createNodeLabel();
    nr.addLabel(nl);
    nr.setLineColor(null);
    nl.setIcon(GroupNodeRealizer.defaultOpenGroupIcon);
    nl.setIconTextGap((byte) 0);
    nl.setPosition(NodeLabel.TOP_RIGHT);
    nl.setInsets(new Insets(4, 4, 4, 4));
    nl.setDistance(0);    
  }
  
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);
    Graph2D graph = view.getGraph2D();
    viewMode.collapseSubtree(graph, Trees.getRoot(graph));
    Node root = Trees.getRoot(graph);
    viewMode.expandSubtree(graph, root, 2);
    layout(graph, null, true);
  }
  
  protected void initialize() {  
    super.initialize();
    view.setPreferredSize(new Dimension(900,600));
  }
  
  /** EditMode not supported by this demo. */
  protected EditMode createEditMode() {
    return null;
  }

  /** Register CollapseExpandViewMode and NavigationMode to support panning */
  protected void registerViewModes() {
    viewMode = new CollapseExpandViewMode();    
    view.addViewMode(viewMode); 
    NavigationMode navigationMode = new NavigationMode();
    view.addViewMode(navigationMode);
  }

  /**
   * A ViewMode that allows to expand and collapse the subtrees rooted at a node by simply clicking on the node.
   * Clicking on a node, while the CTRL modifier key is pushed, will expand/collapse all nodes in the subtree. Note that this view mode
   * is also responsible to keeping track of the expansion state of each node.
   */
  class CollapseExpandViewMode extends ViewMode {
    NodeMap collapsedEdges = Maps.createNodeMap(new WeakHashMap());
    NodeMap collapsedState = Maps.createNodeMap(new WeakHashMap());

    public void mouseClicked(MouseEvent ev) {
      //if (ev.getClickCount() != 2) return;
      Node node = getHitInfo(ev).getHitNode();

      if (node != null) {
        prepareForLayout(view.getGraph2D(), node);
        if (collapsedState.getBool(node)) {
          if (ev.isControlDown()) {//ctrl is pressed expand whole subtree (max depth of 10000) of current node
            expandSubtree(getGraph2D(), node, 10000);
          } else {//ctrl is not pressed expand only current node
            expandNode(getGraph2D(), node);
          }
        } else {
          if (ev.isControlDown()) {//ctrl is pressed collapse whole subtree of current node
            collapseSubtree(getGraph2D(), node);
          } else {//ctrl is not pressed collapse only current node
            collapseNode(getGraph2D(), node);
          }
        }
        layout(getGraph2D(), node, false);
      } 
    }

    /**
     * Collapses the given node and it's whole subtree.
     *
     * @param graph the graph, the root node belongs to.
     * @param root  the node whose subtree is to be collapsed.
     */
    public void collapseSubtree(Graph2D graph, Node root) {
      NodeList list = GraphConnectivity.getSuccessors(graph, new NodeList(root), graph.N());
      NodeCursor nodeCursor = list.nodes();
      for (nodeCursor.toLast(); nodeCursor.ok(); nodeCursor.prev()) {
        Node node = nodeCursor.node();
        if (!collapsedState.getBool(node) && node != root) {
          collapseNode(graph, node);
        }
      }
      collapseNode(graph, root);
    }

    /**
     * collapses the given node.
     *
     * @param graph the graph, the root node belongs to.
     * @param root  the node which is to be collapsed.
     */
    public void collapseNode(Graph2D graph, final Node root) {
      EdgeList edgeList = collapsedEdges.get(root) != null ? (EdgeList) collapsedEdges.get(root) : new EdgeList();
      edgeList.addAll(root.outEdges());
      NodeList collapsedNodes = GraphConnectivity.getSuccessors(graph, new NodeList(root), graph.N());

      for (NodeCursor nc = collapsedNodes.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        edgeList.addAll(n.outEdges());
        double x = graph.getCenterX(n) - graph.getCenterX(root);
        double y = graph.getCenterY(n) - graph.getCenterY(root);

        // store relative location to root
        graph.getRealizer(n).setLocation(0.01 * x, 0.01 * y);

        //remove node from graph
        graph.hide(n);
      }
      collapsedState.setBool(root, true);
      collapsedEdges.set(root, edgeList);
      
      if (!edgeList.isEmpty()) {
        NodeRealizer rootR = getGraph2D().getRealizer(root);
        if(rootR.labelCount() > 1) {
          getGraph2D().getRealizer(root).getLabel(1).setIcon(GroupNodeRealizer.defaultClosedGroupIcon);     
        }
      }
    }

    /**
     * Expands a node and it's subtree to a given depth.
     *
     * @param graph the graph, the root node belongs to.
     * @param root  the node whose subtree is to be expanded.
     * @param depth determines the depth (how many layers) till which the subtree should be expanded.
     */
    public void expandSubtree(Graph2D graph, Node root, int depth) {
      if (depth <= 0) {
        return;
      }
      //expand the root
      expandNode(graph, root);
      NodeList list = GraphConnectivity.getSuccessors(graph, new NodeList(root), depth);
      for (NodeCursor nodeCursor = list.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        if (collapsedState.getBool(node)) {
          //expand the subtree
          expandSubtree(graph, node, depth - 1);
        }
      }
    }

    /**
     * Expands a single node.
     *
     * @param graph the graph, the root node belongs to.
     * @param root  the node which is to be expanded.
     */
    public void expandNode(Graph2D graph, Node root) {
      final EdgeList edgeList = (EdgeList) collapsedEdges.get(root);
      if (edgeList != null) {
        for (EdgeCursor ec = edgeList.edges(); ec.ok(); ec.next()) {
          Edge e = ec.edge();
          if (!graph.contains(e.source())) {
            graph.unhide(e.source());
            graph.setLocation(e.source(), graph.getX(root) + graph.getX(e.source()),
                graph.getY(root) + graph.getY(e.source()));
          }
          if (!graph.contains(e.target())) {
            graph.unhide(e.target());
            graph.setLocation(e.target(), graph.getX(root) + graph.getX(e.target()),
                graph.getY(root) + graph.getY(e.target()));
          }
          //inserts the node into the graph
          graph.unhide(e);
          //cosmetics
          graph.getRealizer(e).clearBends();
        }
        collapsedEdges.set(root, null);
      }
      collapsedState.setBool(root, false);

      if (root.outDegree() > 0) {
        NodeRealizer rootR = getGraph2D().getRealizer(root);
        if(rootR.labelCount() > 1) {
          rootR.getLabel(1).setIcon(GroupNodeRealizer.defaultOpenGroupIcon);
        }
      }
    }
  }

  /**
   * Layout the tree according to the set layout style.
   *
   * @param graph2D    the graph, which will be laid out.
   * @param focusNode  the current focus.
   * @param fitContent determines whether to fit the content to the current view. Should be prevented, if layout. is
   *                   started due to a mouse click on a node.
   */
  void layout(Graph2D graph2D, final Node focusNode, boolean fitContent) {
    //calculate layout according to chosen style
    Layouter layouter = null;
    switch (style) {
      case CollapsibleTreeDemo.STYLE_TREE:
        layouter = treeLayouter;
        break;
      case CollapsibleTreeDemo.STYLE_BALLOON:
        layouter = balloonLayouter;
        break;
      case CollapsibleTreeDemo.STYLE_ORGANIC:
        prepareForLayout(graph2D, focusNode);
        layouter = organicLayouter;
        break;
      case CollapsibleTreeDemo.STYLE_HIERARCHIC:
        prepareForLayout(graph2D, focusNode);
        layouter = hierarchicLayouter;
        break;
      default:
        layouter = treeLayouter;
    }
    
    graph2D.addDataProvider(FocusNodeLayoutStage.FOCUS_NODE_DPKEY, FocusNodeLayoutStage.createFocusNodeDataProvider(focusNode));

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    if (fitContent) {
      layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
    } else {
      layoutExecutor.getLayoutMorpher().setKeepZoomFactor(true);
    }
    layoutExecutor.getLayoutMorpher().setEasedExecution(true);
    layoutExecutor.doLayout(view, new FocusNodeLayoutStage(layouter));
  }

  public static class FocusNodeLayoutStage extends AbstractLayoutStage {
    
    public static final Object FOCUS_NODE_DPKEY = "FocusNodeStage#FOCUS_NODE_DPKEY";
    
    public FocusNodeLayoutStage(Layouter coreLayouter) {
      super(coreLayouter);
    }
    
    public boolean canLayout(LayoutGraph graph) {
      return canLayoutCore(graph);
    }

    public void doLayout(LayoutGraph graph) {
      DataProvider dp = graph.getDataProvider(FOCUS_NODE_DPKEY);
      if(dp != null) {
        Node focusNode = null;
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          Node n = nc.node();
          if(dp.getBool(n)) {
            focusNode = n;
            break;
          }
        }
        YPoint oldFocus = null;
        if(focusNode != null) {
          oldFocus = graph.getCenter(focusNode);
          doLayoutCore(graph);
          NodeLayout nl = graph.getNodeLayout(focusNode);
          YPoint newFocus = new YPoint(nl.getX() + 0.5 * nl.getWidth(), nl.getY() + 0.5 * nl.getHeight());
          double dx = newFocus.x - oldFocus.x;
          double dy = newFocus.y - oldFocus.y;
          LayoutTool.moveSubgraph(graph, graph.nodes(), -dx, -dy);            
        } 
        else {
          doLayoutCore(graph);
        }
      }
      else {
        doLayoutCore(graph);
      }  
    }
  
    public static DataProvider createFocusNodeDataProvider(final Node focusNode) {
      return new DataProviderAdapter() {
        public boolean getBool(Object obj) {
          return obj == focusNode;
        }
      };
    }    
  }
  
  private void prepareForLayout(Graph2D graph2D, Node node) {
    if (node != null){
      NodeList incrementalNodes = GraphConnectivity.getSuccessors(graph2D, new NodeList(node), graph2D.N());
      final HashSet incrementalNodesSet = new HashSet(incrementalNodes);
      // mark nodes as "new"
      for (NodeCursor nodeCursor = incrementalNodes.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        ihlHintMap.set(nodeCursor.node(), hintsFactory.createLayerIncrementallyHint(nodeCursor.node()));
      }
      graph2D.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return incrementalNodesSet.contains(dataHolder);
        }
      });
      graph2D.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, ihlHintMap);
      organicLayouter.setScope(SmartOrganicLayouter.SCOPE_MAINLY_SUBSET);
    } else {
      graph2D.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
      graph2D.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA, DataProviders.createConstantDataProvider(Boolean.FALSE));
      organicLayouter.setScope(SmartOrganicLayouter.SCOPE_ALL);
    }
  }


  /** Adds some buttons to the toolbar, to choose the layout style from. */
  protected JToolBar createToolBar() {
    JToolBar toolbar = super.createToolBar();
    ButtonGroup group = new ButtonGroup();
    JToggleButton b1 = new JToggleButton(new AbstractAction("Tree") {
      public void actionPerformed(ActionEvent e) {
        style = CollapsibleTreeDemo.STYLE_TREE;
        layout(view.getGraph2D(), null, true);
      }
    });
    b1.setSelected(true);
    group.add(b1);
    toolbar.add(b1);


    JToggleButton b2 = new JToggleButton(new AbstractAction("Balloon") {
      public void actionPerformed(ActionEvent e) {
        style = CollapsibleTreeDemo.STYLE_BALLOON;
        layout(view.getGraph2D(), null, true);
      }
    });
    group.add(b2);
    toolbar.add(b2);

    JToggleButton b3 = new JToggleButton(new AbstractAction("Organic") {
      public void actionPerformed(ActionEvent e) {
        style = CollapsibleTreeDemo.STYLE_ORGANIC;
        layout(view.getGraph2D(), null, true);
      }
    });
    group.add(b3);
    toolbar.add(b3);

    JToggleButton b4 = new JToggleButton(new AbstractAction("Hierarchic") {
      public void actionPerformed(ActionEvent e) {
        style = CollapsibleTreeDemo.STYLE_HIERARCHIC;
        Graph2D graph = view.getGraph2D();
        layout(graph, Trees.getRoot(graph), true);
      }
    });
    group.add(b4);
    toolbar.add(b4);

    return toolbar;
  }

  void createTree(Graph2D graph) {
    NodeList queue = new NodeList();
    queue.add(graph.createNode(0, 0, 80, 30, "Root"));
    for (int i = 0; i < 50; i++) {
      Node root = queue.popNode();
      Node c1 = graph.createNode(0, 0, 80, 30, String.valueOf(graph.N()));
      Edge e1 = graph.createEdge(root, c1);
      Node c2 = graph.createNode(0, 0, 80, 30, String.valueOf(graph.N()));
      Edge e2 = graph.createEdge(root, c2);
      queue.add(c2);
      queue.add(c1);
      if (i == 25 || i == 40) {
        for (int j = 0; j < 20; j++) {
          Node c3 = graph.createNode(0, 0, 80, 30, String.valueOf(graph.N()));
          Edge e3 = graph.createEdge(root, c3);
          queue.add(c3);
        }
      }
    }
    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      if (node.outDegree() == 0) {
        graph.getRealizer(node).getLabel(1).setIcon(null);
      }
    }
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new CollapsibleTreeDemo()).start();
      }
    });
  }
}