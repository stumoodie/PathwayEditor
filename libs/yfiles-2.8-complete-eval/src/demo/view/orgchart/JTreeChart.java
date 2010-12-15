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
package demo.view.orgchart;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeModel;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YInsets;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.layout.GraphLayout;
import y.layout.NormalizingGraphElementOrderStage;
import y.layout.Layouter;
import y.layout.tree.GenericTreeLayouter;
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.NavigationMode;
import y.view.NodeRealizer;
import y.view.Overview;
import y.view.Selections;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.Graph2DLayoutExecutor;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Component that visualizes tree data structures.
 */
public class JTreeChart extends Graph2DView {
  
  private boolean viewLocalHierarchy = false;
  private boolean siblingViewEnabled = false;
  private boolean groupViewEnabled = false;   
  private DataProvider groupIdDP;  
  private DataProvider userObjectDP;  
  private TreeModel model;
  private Map graph2TreeMap;
  private Map tree2GraphMap;
  private NodeList allNodes = new NodeList();
  private EdgeList allEdges = new EdgeList();
  private HashMap idToGroupNodeMap;
  private HashMap groupNodeToIdMap;
  private Object lastUserObject;

  /**
   * Creates a new <code>JTreeChart</code>.
   * @param model   the data model which determines the tree structure to
   * visualize.
   * @param userObjectDP   a mapping from model data to business data.
   * @param groupIdDP   a mapping from business data to grouping ids. Business
   * data items that share a grouping id are considered a business unit.
   * Business units may be visualized by a group node containing all nodes
   * representing the appropriate business data items.
   */
  public JTreeChart(TreeModel model, DataProvider userObjectDP, DataProvider groupIdDP) {
    super();
    
    this.groupIdDP = groupIdDP;
    this.userObjectDP = userObjectDP;    
    this.model = model;
    
    new HierarchyManager(getGraph2D());
    
    setRealizerDefaults();
    updateChart();
    addMouseInteraction();    
    addKeyboardInteraction();
  }

  /**
   * Registers handlers for mouse events.
   */
  protected void addMouseInteraction() {
    ViewMode vm = createTreeChartViewMode();
    if(vm != null) {
      addViewMode(vm);
    }
    
    MouseWheelListener mwl = createMouseWheelListener();
    if(mwl != null) {
      getCanvasComponent().addMouseWheelListener(mwl);
    } 
  }

  /**
   * Registers handlers for keyboard events.
   */
  protected void addKeyboardInteraction() {
    Graph2DViewActions actions = new Graph2DViewActions(this);
    
    ActionMap actionMap = actions.createActionMap();
    actionMap.put(Graph2DViewActions.FOCUS_BOTTOM_NODE, new SelectRootWrapperAction(actionMap.get(Graph2DViewActions.FOCUS_BOTTOM_NODE),this));
    actionMap.put(Graph2DViewActions.FOCUS_TOP_NODE, new SelectRootWrapperAction(actionMap.get(Graph2DViewActions.FOCUS_TOP_NODE),this));
    actionMap.put(Graph2DViewActions.FOCUS_LEFT_NODE, new SelectRootWrapperAction(actionMap.get(Graph2DViewActions.FOCUS_LEFT_NODE),this));
    actionMap.put(Graph2DViewActions.FOCUS_RIGHT_NODE, new SelectRootWrapperAction(actionMap.get(Graph2DViewActions.FOCUS_RIGHT_NODE),this));
    actionMap.put("NODE_ACTION", new NodeAction());

    final JComponent canvas = getCanvasComponent();
    InputMap inputMap =  new ComponentInputMap(canvas); 
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.CTRL_MASK), Graph2DViewActions.FOCUS_LEFT_NODE);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_MASK), Graph2DViewActions.FOCUS_RIGHT_NODE);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_MASK), Graph2DViewActions.FOCUS_TOP_NODE);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_MASK), Graph2DViewActions.FOCUS_BOTTOM_NODE);
    
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "NODE_ACTION");
    
    canvas.setActionMap(actionMap);
    canvas.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);

    KeyboardNavigation kNav = new KeyboardNavigation(this);
    canvas.addKeyListener(kNav.createZoomInKeyListener(KeyEvent.VK_ADD, KeyEvent.VK_PLUS));
    canvas.addKeyListener(kNav.createZoomOutKeyListener(KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS));    
    canvas.addKeyListener(kNav.createMoveViewportUpKeyListener(KeyEvent.VK_UP));
    canvas.addKeyListener(kNav.createMoveViewportDownKeyListener(KeyEvent.VK_DOWN));
    canvas.addKeyListener(kNav.createMoveViewportLeftKeyListener(KeyEvent.VK_LEFT));
    canvas.addKeyListener(kNav.createMoveViewportRightKeyListener(KeyEvent.VK_RIGHT));
  }

  /**
   * Creates a handler for mouse wheel events.
   * @return a handler for mouse wheel events.
   */
  protected MouseWheelListener createMouseWheelListener() {
    return new Graph2DViewMouseWheelZoomListener();
  }

  /**
   * Creates a <code>JTreeChartViewMode</code> suitable for use with this
   * component.
   * @return a <code>JTreeChartViewMode</code> suitable for use with this
   * component.
   */
  protected JTreeChartViewMode createTreeChartViewMode() {
    return new JTreeChartViewMode();
  }

  public Action createNodeAction() {
    return new NodeAction();
  }

  public Action createZoomInAction() {
    return new AnimatedZoomAction(true);
  }

  public Action createZoomOutAction() {
    return new AnimatedZoomAction(false);
  }

  public Action createFitContentAction() {
    return new FitContentAction();
  }

  public Overview createOverview() {
    return new Overview(this);
  }

  /**
   * Callback method to set up the default {@link y.view.NodeRealizer}s and
   * {@link y.view.EdgeRealizer}s.
   * Note, this method is called from <code>JTreeChart</code>'s constructor.
   */
  protected void setRealizerDefaults() {
  }

  /**
   * Callback method that is used to configure {@link y.view.NodeRealizer}s
   * for nodes representing business data.
   * @param n   a node representing business data.
   */
  protected void configureNodeRealizer(Node n) {
  }

  /**
   * Callback method that is used to configure {@link y.view.NodeRealizer}s
   * for nodes representing business units.
   * @param node   a node representing a business unit.
   * @param groupId   the id of the business unit.
   * @param collapsed   the current state of the business units.
   * If <code>true</code> the business unit is represented as a folder, i.e.
   * the nodes representing the business data associated to the unit are
   * not being displayed; if <code>false</code> the business unit is represented
   * as a group node containing the nodes representing the business data
   * associated to the unit.
   */
  protected void configureGroupRealizer(Node node, Object groupId, boolean collapsed) {
    NodeRealizer nr = getGraph2D().getRealizer(node);
    if(nr instanceof GroupNodeRealizer) {
      GroupNodeRealizer gnr = (GroupNodeRealizer) nr;
      gnr.setGroupClosed(collapsed);
      gnr.setBorderInsets(new YInsets(0,0,0,0));      
    }
  }

  /**
   * Callback method that is used to configure {@link y.view.EdgeRealizer}s for
   * all edges.
   * @param e   an edge for which the realizer has to be configured.
   */
  protected void configureEdgeRealizer(Edge e) {
  }

  /**
   * Calls the appropriate <code>configureXXXRealizer</code> method for each
   * element in the chart.
   */
  private void configureRealizers() {
    Graph2D graph = getGraph2D();
    HierarchyManager hm = graph.getHierarchyManager();
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if(groupNodeToIdMap == null || groupNodeToIdMap.get(n) == null) {
        configureNodeRealizer(n);
      }
      else {
        configureGroupRealizer(n, groupNodeToIdMap.get(n), hm.isFolderNode(n));
      }
    }
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      configureEdgeRealizer(ec.edge());
    }
  }

  /**
   * Calculates and applies a new layout to the chart.
   */
  private void layoutGraph() {
    new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED).doLayout(getGraph2D(), createLayouter());
    fitContent();
    updateView();
  }

  /**
   * Returns the business data represented by the specified node.
   * @param node   the node for which the business data should be retrieved.
   * @return the business data represented by the specified node.
   */
  public Object getUserObject(Node node) {
    Object treeNode = graph2TreeMap.get(node);
    if(treeNode == null) {
      return null;
    } else {
      return getUserObject(treeNode);
    }
  }

  /**
   * Returns the <code>Node</code> representing the model data root.
   * @return the <code>Node</code> representing the model data root.
   */
  public Node getRootNode() {
    Object treeNode = model.getRoot();
    Object userObject = getUserObject(treeNode);
    return getNodeForUserObject(userObject);
  }

  /**
   * Returns the business data corresponding to the specified model data.
   * @param treeNode   the model data for which the business data should be
   * retrieved.
   * @return the business data corresponding to the specified model data.
   */
  private Object getUserObject(Object treeNode) {
    return userObjectDP == null ? null : userObjectDP.get(treeNode);
  }

  /**
   * Returns the grouping id (or business unit id) for the specified business
   * data.
   * Business data items that share a grouping id are considered a business
   * unit. Business units may be visualized by a group node containing all nodes
   * representing the appropriate business data items.
   * @param userObject   the business data for which the grouping id should be
   * retrieved.
   * @return the grouping id (or business unit id) for the specified business
   * data.
   */
  public Object getGroupId(Object userObject) {
    return groupIdDP == null ? null : groupIdDP.get(userObject);
  }

  /**
   * Returns the node representing the specified business data or
   * <code>null</code> if there is no such node.
   * @param userObject   the business data for which the representative node
   * should be retrieved.
   * @return  the node representing the specified business data or
   * <code>null</code> if there is no such node.
   */
  public Node getNodeForUserObject(Object userObject) {
    for (NodeCursor nc = getGraph2D().nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if(getUserObject(n) == userObject) {
        return n;
      }        
    }
    return null;
  }

  /**
   * Returns the model data represented by the specified node.
   * @param node   the node for which the model data should be retrieved.
   * @return the model data represented by the specified node.
   */
  public Object getTreeNode(Node node) {
    return graph2TreeMap.get(node);
  }

  /**
   * Updates the component to visualize all of the model/business data.
   */
  public void showGlobalHierarchy() {
    viewLocalHierarchy = false;

    final NodeCursor nc = getGraph2D().selectedNodes();
    final Object selected = nc.ok() ? getUserObject(nc.node()) : null;

    buildGlobalGraph();
    configureRealizers();

    if (selected != null) {
      getGraph2D().setSelected(getNodeForUserObject(selected), true);
    }

    layoutGraph();
    getGraph2D().updateViews();
  }
  
  /**
   * Updates the component to visualize the neighborhood of the specified
   * business data. In this context, the neighborhood of a business data item
   * is defined as follows: Let <code>m</code> be the model data corresponding
   * to business data <code>b</code>. Then business data <code>bn</code>
   * is said to be a <em>neighbor</em> of <code>b</code>, iff the model data
   * <code>mn</code> corresponding to <code>bn</code> is either the parent or
   * one of the children of <code>m</code> in the tree model of this component.
   * The <em>neighborhood</em> of <code>b</code> consists of all neighbors of
   * <code>b</code>.
   * <p>
   * If the specified business data is <code>null</code>, the neighborhood
   * of the business data corresponding to the model root is displayed.
   * </p>
   * @param userObject   the business data.
   */
  public void showLocalHierarchy(Object userObject) {
    viewLocalHierarchy = true;
    Graph2D graph = getGraph2D();
        
    if (userObject == null) {
      Node root = null;
      final NodeCursor selected = graph.selectedNodes();
      if (selected.ok()) {
        root = selected.node();
      } else {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          if(nc.node().inDegree() == 0) {
            root = nc.node();
            break;
          }
        }
      }   
      userObject = getUserObject(root);
    }
    
    lastUserObject = userObject;
    
    boolean incrChange = getNodeForUserObject(userObject) != null;
    
    final NodeList addedNodes = new NodeList();
    NodeList removedNodes = new NodeList();
    EdgeList removedEdges = new EdgeList();
    final EdgeList addedEdges = new EdgeList();
    
    buildLocalView(userObject, removedNodes, addedNodes, removedEdges, addedEdges);       
    
    if (!incrChange) {
      configureRealizers();
      // TODO - refactor
      new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED).doLayout(graph, createLayouter());
      fitContent();
    } else {
      for(NodeCursor nc = removedNodes.nodes(); nc.ok(); nc.next()) {
        graph.reInsertNode(nc.node());
      }
      for(EdgeCursor ec = removedEdges.edges(); ec.ok(); ec.next()) {
        graph.reInsertEdge(ec.edge());
      }      
      
      configureRealizers();

      for(EdgeCursor ec = addedEdges.edges(); ec.ok(); ec.next()) {
        graph.removeEdge(ec.edge());
      }
      for(NodeCursor nc = addedNodes.nodes(); nc.ok(); nc.next()) {
        graph.removeNode(nc.node());      
      }    
      
      ViewAnimationFactory factory = new ViewAnimationFactory(this);
      AnimationPlayer player = factory.createConfiguredPlayer();
      player.setBlocking(true);
      
      AnimationObject deleteAnim = createDeleteAnimation(graph, removedNodes, removedEdges, factory, 200);
      
      player.animate(deleteAnim);
      
      for(NodeCursor nc = addedNodes.nodes(); nc.ok(); nc.next()) {
        graph.reInsertNode(nc.node());           
        graph.getRealizer(nc.node()).setVisible(false);
      }

      for(EdgeCursor ec = addedEdges.edges(); ec.ok(); ec.next()) {
        graph.reInsertEdge(ec.edge());
        graph.getRealizer(ec.edge()).setVisible(false);
      }
             
      if(isGroupViewEnabled()) {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          Node n = nc.node();
          Object obj = getUserObject(n);
          if(obj != null && getGroupId(obj) != null) {
            HierarchyManager hm = getGraph2D().getHierarchyManager();
            Node groupNode = (Node) idToGroupNodeMap.get(getGroupId(obj));
            if(hm.isNormalNode(groupNode)) {
              hm.convertToGroupNode(groupNode);
            }
            hm.setParentNode(n, groupNode);
          }
        }
      }

      new Graph2DLayoutExecutor(){
        protected AnimationObject createAnimation(Graph2DView view, Graph2D graph, GraphLayout graphLayout) {
          for(NodeCursor nc = addedNodes.nodes(); nc.ok(); nc.next()) {
            graph.getRealizer(nc.node()).setVisible(false);
          }
          for(EdgeCursor ec = addedEdges.edges(); ec.ok(); ec.next()) {
            graph.getRealizer(ec.edge()).setVisible(false);
          }
          return super.createAnimation(view, graph, graphLayout);
        }
      }.doLayout(this, createLayouter());

      for(NodeCursor nc = addedNodes.nodes(); nc.ok(); nc.next()) {
        graph.getRealizer(nc.node()).setVisible(true);
      }
      for(EdgeCursor ec = addedEdges.edges(); ec.ok(); ec.next()) {
        graph.getRealizer(ec.edge()).setVisible(true);
      }
      
      AnimationObject fadeInAnim = createFadeInAnimation(graph, addedNodes, addedEdges, factory, 500);           
      player.animate(fadeInAnim);
    }
    
    
    graph.updateViews();
  }

  protected Layouter createLayouter() {
    GenericTreeLayouter layouter = new GenericTreeLayouter();

    // hiding/removing and unhiding/reinserting graph elements which is done
    // e.g. when switching from displaying the whole chart to displaying a
    // local excerpt may change the order of elements in the chart's graph
    // however, the order of elements in a graph usually affects the results
    // produced by a layout algorithm which in turn means that the above
    // mentioned hide/unhide operations could lead to different layouts for
    // a given set of displayed data
    // NormalizingGraphElementOrderStage prevents that from happening by
    // enforcing an externally specified, fixed graph element order
    // see the usage of
    // NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY
    // and
    // NormalizingGraphElementOrderStage.COMPARABLE_NODE_DPKEY
    // in buildGlobalGraph
    return new NormalizingGraphElementOrderStage(layouter);
  }

  /**
   * Updates the displayed chart to either show the neighborhood of the
   * currently selected item or to show the whole business data at once.
   * @see #showGlobalHierarchy
   * @see #showLocalHierarchy(Object)
   */
  public void updateChart() {
    if(isLocalViewEnabled()) {
      buildGlobalGraph();
      showLocalHierarchy(lastUserObject);
    } else {
      showGlobalHierarchy();
    }
  }

  /**
   * Determines whether or not siblings are included when displaying
   * the neighborhood of a business data item.
   * @return <code>true</code> if siblings are included when displaying
   * the neighborhood of a business data item; <code>false</code> otherwise.
   */
  public boolean isSiblingViewEnabled() {
    return siblingViewEnabled;
  }

  /**
   * Specifies whether or not siblings should be included when displaying
   * the neighborhood of a business data item.
   * In this context, siblings are defined as follows: Let <code>m</code> be
   * the model data corresponding to business data <code>b</code>. Let
   * <code>mp</code> be the parent of <code>m</code> in the tree model of this
   * component. Then business data <code>bs</code> is said to be a
   * <em>sibling</em> of <code>b</code>, iff the model data <code>ms</code>
   * corresponding to <code>bs</code> is a child of <code>mp</code> in the tree
   * model of this component.
   * @param siblingViewEnabled   if <code>true</code>, siblings will be
   * displayed.
   */
  public void setSiblingViewEnabled(boolean siblingViewEnabled) {
    this.siblingViewEnabled = siblingViewEnabled;
  }

  /**
   * Determines whether all of the business data or only a local excerpt
   * is displayed.
   * @return <code>false</code> if all of the business data is displayed;
   * <code>true</code> otherwise.
   */
  public boolean isLocalViewEnabled() {
    return viewLocalHierarchy;
  }

  /**
   * Returns whether or not business units are displayed using group nodes.
   * @return whether or not business units are displayed using group nodes.
   */
  public boolean isGroupViewEnabled() {
    return groupViewEnabled && groupIdDP != null;
  }

  /**
   * Specifies whether or not business units should be displayed using
   * group nodes.
   * @param enabled   if <code>true</code> business units will be displayed.
   */
  public void setGroupViewEnabled(boolean enabled) {
    groupViewEnabled = enabled;
  }

  /**
   * Focuses on the specified node by moving the node into the center of
   * this component.
   * @param node   the node to focus on.
   */
  public void focusNode(Node node) {
    YPoint p = getGraph2D().getCenter(node);
    focusView(getZoom(), new Point2D.Double(p.x, p.y), false);
    updateView();
  }

  /**
   * Focuses on the specified node.
   * If this component currently displays the whole chart, the specified node
   * will become its center and the component's zoom level will be adjusted to
   * prominently display the specified node.
   * If this component currently displays a local excerpt of the chart, the
   * displayed excerpt will be changed to the specified node's neighborhood,
   * see also {@link #showLocalHierarchy(Object)}.
   * @param node   the node to focus on.
   */
  public void performNodeAction(Node node) {
    if(getGraph2D().getHierarchyManager().isNormalNode(node)) {
      if(viewLocalHierarchy) {
        showLocalHierarchy(getUserObject(node));
      }
      else {
        Point2D center = new Point2D.Double(getGraph2D().getCenterX(node), getGraph2D().getCenterY(node));
        YRectangle nodeSize = getGraph2D().getRectangle(node);
        Dimension viewSize = getViewSize();
        double zoom;
        if(viewSize.width/nodeSize.width < viewSize.height/nodeSize.height) {
          zoom = viewSize.width/nodeSize.width;
        } else {
          zoom = viewSize.height/nodeSize.height;
        }
        zoom *= 0.5;
        focusView(zoom, center, true);
      }
    }
  }

  /**
   * Removes the business data of a business unit from the chart.
   * @param groupNode   a node representing a business unit that displays
   * its business data.
   */
  private void collapseGroup(Node groupNode) {
    Graph2D graph = getGraph2D();
    HierarchyManager hm = graph.getHierarchyManager();
    hm.closeGroup(groupNode);
    configureGroupRealizer(groupNode, groupNodeToIdMap.get(groupNode), true);
    layoutGraph();
  }

  /**
   * Reinserts the business data of a business unit from the char.
   * @param folderNode   a node representing a business unit that does not
   * display its business data.
   */
  private void expandGroup(Node folderNode) {
    Graph2D graph = getGraph2D();
    HierarchyManager hm = graph.getHierarchyManager();
    hm.openFolder(folderNode);
    configureGroupRealizer(folderNode, groupNodeToIdMap.get(folderNode), false);
    layoutGraph();
  }

  /**
   * Creates the chart from scratch including all business data. Business
   * units are included as appropriate for the return value of
   * {@link #isGroupViewEnabled()}.
   */
  private void buildGlobalGraph() {
    Graph2D graph = getGraph2D();
    graph.clear();
    tree2GraphMap = new HashMap();
    graph2TreeMap = new HashMap();
    Object treeNode = model.getRoot();
    Node graphNode = graph.createNode();
    tree2GraphMap.put(treeNode, graphNode);
    graph2TreeMap.put(graphNode, treeNode);
    buildGraph(treeNode, graphNode, tree2GraphMap, graph2TreeMap);

    if(isGroupViewEnabled()) {
      addGroupNodes();
    }

    allNodes = new NodeList(graph.nodes());
    allEdges = new EdgeList(graph.edges());

    DataMap comparableMap = Maps.createHashedDataMap();
    NormalizingGraphElementOrderStage.fillComparableMapFromGraph(graph,  comparableMap, comparableMap);
    graph.addDataProvider(NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY, comparableMap);
    graph.addDataProvider(NormalizingGraphElementOrderStage.COMPARABLE_NODE_DPKEY, comparableMap);
  }

  /**
   * Recursively builds the chart from the tree model.
   * @param treeNode   the model root of the subtree to build.
   * @param graphNode   the node representing the model root.
   * @param tree2GraphMap   an output parameter to store a mapping from model
   * items to graph nodes.
   * @param graph2TreeMap   an output parameter to store a mapping from graph
   * nodes to model items.
   */
  private void buildGraph(Object treeNode, Node graphNode, Map tree2GraphMap, Map graph2TreeMap) {
    Graph2D graph = getGraph2D();
    int count = model.getChildCount(treeNode);
    for(int i = 0; i < count; i++) {
      Object treeChild = model.getChild(treeNode, i);
      Node graphChild = graph.createNode();
      tree2GraphMap.put(treeChild, graphChild);
      graph2TreeMap.put(graphChild, treeChild);
      //configureNode(graphChild);
      graph.createEdge(graphNode, graphChild);
      buildGraph(treeChild, graphChild, tree2GraphMap, graph2TreeMap);
    }
  }

  /**
   * Adds group nodes representing business units to the chart.
   */
  private void addGroupNodes() {
    Graph2D graph = getGraph2D();
    idToGroupNodeMap = new HashMap();
    groupNodeToIdMap = new HashMap();
    HierarchyManager hm = graph.getHierarchyManager();
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      Object obj = getUserObject(n);
      if(obj != null && getGroupId(obj) != null) {
        Node groupNode = (Node) idToGroupNodeMap.get(getGroupId(obj));
        if(groupNode == null) {
          groupNode = hm.createGroupNode(graph);
          idToGroupNodeMap.put(getGroupId(obj), groupNode);
          groupNodeToIdMap.put(groupNode, getGroupId(obj));
        }
        hm.setParentNode(n, groupNode);
      }
    }
  }

  /**
   * Builds a local excerpt for the neighborhood of the specified business data.
   * @param userObject   the business data.
   * @param removedNodes   output parameter containing the nodes that should not
   * be part of the chart anymore.
   * @param addedNodes   output parameter containing the nodes that need to
   * be added to the chart.
   * @param removedEdges   output parameter containing the edges that should not
   * be part of the chart anymore.
   * @param addedEdges   output parameter containing the edges that need to
   * be added to the chart.
   */
  private void buildLocalView(Object userObject, NodeList removedNodes, NodeList addedNodes, EdgeList removedEdges, EdgeList addedEdges) {
    expandAll();
    Graph2D graph = getGraph2D();
    NodeMap prevNodeMap = Maps.createHashedNodeMap();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      prevNodeMap.setBool(n, true);
    }
    EdgeMap prevEdgeMap = Maps.createHashedEdgeMap();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      prevEdgeMap.setBool(e, true);
    }

    rebuildGlobalGraph();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if(getUserObject(n).equals(userObject)) {
        NodeList nodes = new NodeList(n);
        if(n.inDegree() == 1) {
          Node parent = n.firstInEdge().source();
          nodes.add(parent);
          if(isSiblingViewEnabled()) {
            nodes.pop();
            nodes.addAll(parent.successors());
          }
        }
        nodes.addAll(n.successors());

        NodeList nodesToRemove = new NodeList(graph.nodes());

        if(isGroupViewEnabled()) {
          HashSet requiredGroups = new HashSet();
          //iterate over local view elements marking required groups
          for(NodeCursor ncc = nodes.nodes(); ncc.ok(); ncc.next()) {
            Node node = ncc.node();
            requiredGroups.add(graph.getHierarchyManager().getParentNode(node));
          }
          nodesToRemove.removeAll(requiredGroups);
        }
        nodesToRemove.removeAll(nodes);

        while(!nodesToRemove.isEmpty()) {
          graph.removeNode(nodesToRemove.popNode());
        }
        break;
      }
    }

    for(NodeCursor nc = allNodes.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if(n.getGraph() != null) {
        //node currently present
        if(prevNodeMap.getBool(n)) {
          //was present before - no delta
        } else {
          //was not present before - added node
          addedNodes.add(n);
        }
      } else {
        //node currently not present
        if(prevNodeMap.getBool(n)) {
          //was present before - removed node
          removedNodes.add(n);
        } else {
          //was not present before - no delta
        }
      }
    }
    for(EdgeCursor ec = allEdges.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      if(e.getGraph() != null) {
        //edge currently present
        if(prevEdgeMap.getBool(e)) {
          //was present before - no delta
        } else {
          //was not present before - added edge
          addedEdges.add(e);
        }
      } else {
        //edge currently not present
        if(prevEdgeMap.getBool(e)) {
          //was present before - removed edge
          removedEdges.add(e);
        } else {
          //was not present before - no delta
        }
      }
    }

    Node employeeNode = getNodeForUserObject(userObject);
    if(employeeNode != null) {
      graph.setSelected(employeeNode, true);
    }
  }

  /**
   * Expands all folder nodes to group nodes. That is for all business units
   * that currently do not display their business data reinsert said data.
   */
  private void expandAll() {
    HierarchyManager hm = getGraph2D().getHierarchyManager();
    for (NodeCursor nc = allNodes.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if(hm.isFolderNode(n)) {
        hm.openFolder(n);
      }
    }
  }

  private void rebuildGlobalGraph() {
    Graph2D graph = getGraph2D();
    graph.clear();
    for (NodeCursor nc = allNodes.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      graph.reInsertNode(n);
    }
    for (EdgeCursor ec = allEdges.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      graph.reInsertEdge(e);
    }

    //reestablish grouping structure
    if(isGroupViewEnabled()) {
      for (NodeCursor nc = allNodes.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        Object obj = getUserObject(n);
        if(obj != null && getGroupId(obj) != null) {
          HierarchyManager hm = getGraph2D().getHierarchyManager();
          Node groupNode = (Node) idToGroupNodeMap.get(getGroupId(obj));
          if(hm.isNormalNode(groupNode)) {
            hm.convertToGroupNode(groupNode);
          }
          hm.setParentNode(n, groupNode);
        }
      }
    }
  }

  /**
   * Creates an animation for retracting edges and fading out nodes.
   * As a side effect, this animation will result in said edges and nodes being
   * removed from the graph.
   * @param nodesToBeDeleted   the nodes to fade out
   * @param edgesToBeDeleted   the edges to retract
   * @return an animation for retracting edges and fading out nodes.
   */
  private AnimationObject createDeleteAnimation(
          Graph2D graph,
          final List nodesToBeDeleted,
          final List edgesToBeDeleted,
          ViewAnimationFactory factory,
          long preferredDuration
  ) {
    final CompositeAnimationObject deleteEdges = AnimationFactory.createConcurrency();
    for (Iterator it = edgesToBeDeleted.iterator(); it.hasNext();) {
      final EdgeRealizer er = graph.getRealizer((Edge) it.next());
      deleteEdges.addAnimation(factory.fadeOut(er, ViewAnimationFactory.APPLY_EFFECT, preferredDuration));
    }

    final CompositeAnimationObject deleteNodes = AnimationFactory.createConcurrency();
    for (Iterator it = nodesToBeDeleted.iterator(); it.hasNext();) {
      final NodeRealizer nr = graph.getRealizer((Node) it.next());
      deleteNodes.addAnimation(factory.fadeOut(nr, ViewAnimationFactory.APPLY_EFFECT, preferredDuration));
    }
    return AnimationFactory.createSequence(deleteEdges, deleteNodes);
  }

  /**
   * Creates an animation for fading in edges and nodes.
   * removed from the graph.
   * @param nodesToBeAdded   the nodes to fade in.
   * @param edgesToBeAdded   the edges to fade in.
   * @return an animation for fading in edges and nodes.
   */
  private AnimationObject createFadeInAnimation(
          Graph2D graph,
          final List nodesToBeAdded,
          final List edgesToBeAdded,
          ViewAnimationFactory factory,
          long preferredDuration
  ) {
    final CompositeAnimationObject addElems = AnimationFactory.createConcurrency();
    for (Iterator it = edgesToBeAdded.iterator(); it.hasNext();) {
      final EdgeRealizer er = graph.getRealizer((Edge) it.next());
      addElems.addAnimation(factory.fadeIn(
              er, preferredDuration));
    }

    for (Iterator it = nodesToBeAdded.iterator(); it.hasNext();) {
      final NodeRealizer nr = graph.getRealizer((Node) it.next());
      addElems.addAnimation(factory.fadeIn(nr, preferredDuration));
    }
    return addElems;
  }


  /**
   * <code>NavigationMode</code> that provides custom single and double mouse
   * click handling.
   * Single clicking a node representing business data (and not a business unit)
   * will select said node; single clicking anything else will unselect any
   * selected node.
   * Double clicking a node representing business data will invoke
   * {@link JTreeChart#performNodeAction(y.base.Node)} for that node, double
   * clicking a node representing a business unit will toggle the node's
   * collapsed/expanded state, and finally double clicking anything but a node
   * will trigger an animated fit content operation
   * (see {@link demo.view.orgchart.JTreeChart#fitContent(boolean)}).
   */
  public static class JTreeChartViewMode extends NavigationMode {
    public JTreeChart getJTreeChart() {
      return (JTreeChart) view;
    }

    public void mouseClicked(double x, double y) {
      if(lastClickEvent.getClickCount() > 1) {
        mouseDoubleClicked(x, y);
      }
      else {
        mouseSingleClicked(x,y);
      }
    }

    /**
     * Handles single mouse clicks for the specified world coordinates.
     * Single clicking a normal node (as opposed to a group or folder node)
     * will select said node (exclusively). Single clicking anything else will
     * unselect all previously selected items.
     * @param x   the x-coordinate in the associated view's world coordinate
     * system.
     * @param y   the y-coordinate in the associated view's world coordinate
     * system.
     * @see y.view.hierarchy.HierarchyManager#isFolderNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isGroupNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isNormalNode(y.base.Node)
     */
    protected void mouseSingleClicked(double x, double y) {
      view.getCanvasComponent().requestFocus();
      HitInfo info = getHitInfo(x,y);
      Node node = info.getHitNode();
      Graph2D graph = getGraph2D();
      if (node != null && getGraph2D().getHierarchyManager().isNormalNode(node)) {
        if (!graph.isSelected(node)) {
          graph.unselectAll();
          graph.setSelected(node, true);
        }
      } else {
        getGraph2D().unselectAll();
      }
      getGraph2D().updateViews();
    }

    /**
     * Handles double mouse clicks for the specified world coordinates.
     * Double clicking a normal node will invoke
     * {@link demo.view.orgchart.JTreeChart#performNodeAction(y.base.Node)} for
     * that node; double clicking a group node will collapse or close the group
     * (i.e. hide its content); double clicking a folder node will expand or
     * open the folder (i.e. unhide its content).
     * @param x   the x-coordinate in the associated view's world coordinate
     * system.
     * @param y   the y-coordinate in the associated view's world coordinate
     * system.
     * @see y.view.hierarchy.HierarchyManager#isFolderNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isGroupNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isNormalNode(y.base.Node)
     */
    protected void mouseDoubleClicked(double x, double y) {
      if (lastClickEvent.getClickCount() == 2) {
        HitInfo info = getHitInfo(x,y);
        Node node = info.getHitNode();
        if (node != null) {
          if (getGraph2D().getHierarchyManager().isGroupNode(node)) {
            getJTreeChart().collapseGroup(node);
          } else if (getGraph2D().getHierarchyManager().isFolderNode(node)) {
            getJTreeChart().expandGroup(node);
          } else {
            getJTreeChart().performNodeAction(node);
          }
        } else {
          getJTreeChart().fitContent(true);
        }
      }
    }
  }

  /**
   * <code>Action</code> for decorating {@link Graph2DViewActions}' focus node
   * actions such that triggering this action while no node is selected will
   * select either a node with indegree <code>0</code> or the first node in the
   * graph if there is no node with indegree <code>0</code>. In other words,
   * this <code>Action</code> will try to select the node representing
   * the model root of a {@link demo.view.orgchart.JTreeChart} component.
   */
  private static class SelectRootWrapperAction implements Action {
    Action delegateAction;
    Graph2DView view;

    SelectRootWrapperAction(Action delegateAction, Graph2DView view) {
      this.delegateAction = delegateAction;
      this.view = view;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      delegateAction.addPropertyChangeListener(listener);
    }

    public Object getValue(String key) {
      return delegateAction.getValue(key);
    }

    public boolean isEnabled() {
      return delegateAction.isEnabled();
    }

    public void putValue(String key, Object value) {
      delegateAction.putValue(key, value);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      delegateAction.removePropertyChangeListener(listener);
    }

    public void setEnabled(boolean b) {
      delegateAction.setEnabled(b);
    }

    /**
     * Selects a node in the associated view's graph. The node which is selected
     * is determined as follows: If there is currently no selected node then
     * either select a node with indegree <code>0</code> or (if there is no node
     * with indegree <code>0</code>) select the first node in the graph. If
     * there is a currently selected node, then call the decorated action's
     * <code>actionPerformed</code> method and let it handle node selection.
     */
    public void actionPerformed(ActionEvent e) {
      Graph2D graph = view.getGraph2D();
      boolean selectionEmpty = Selections.isNodeSelectionEmpty(graph);
      if(selectionEmpty) {
        //select root node
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          Node n = nc.node();
          if(n.inDegree() == 0) {
            graph.setSelected(n, true);
            selectionEmpty = false;
            break;
          }
        }
        if(graph.nodeCount() > 0 && selectionEmpty) {
          graph.setSelected(graph.firstNode(), true);
        }
      }
      else {
        delegateAction.actionPerformed(e);
      }
    }
  }

  /**
   * <code>Action</code> that changes this component's zoom level in an
   * animated fashion.
   */
  private class AnimatedZoomAction extends AbstractAction {
    private final boolean zoomIn;

    private ViewAnimationFactory factory;
    private AnimationPlayer player;

    AnimatedZoomAction( final boolean zoomIn ) {
      this.zoomIn = zoomIn;
    }

    /**
     * Changes the zoom level in an animated fashion.
     * @param e   the event that triggered the zom level change.
     */
    public void actionPerformed(ActionEvent e) {
      if (factory == null) {
        factory = new ViewAnimationFactory(JTreeChart.this);
        player = factory.createConfiguredPlayer();
      }

      if (!player.isPlaying()) {
        player.animate(AnimationFactory.createEasedAnimation(
                factory.zoom(calculateZoom(), ViewAnimationFactory.APPLY_EFFECT, 500)));
      }
    }

    /**
     * Calculates a new zoom level for the component.
     * @return  a new zoom level for the component.
     */
    double calculateZoom() {
      if (zoomIn) {
        return Math.min(4, getZoom()*2);
      } else {
        Point2D oldP = getViewPoint2D();
        double oldZoom = getZoom();
        fitContent();
        double fitContentZoom = getZoom();
        setZoom(oldZoom);
        setViewPoint2D(oldP.getX(), oldP.getY());

        return Math.max(fitContentZoom, getZoom()*0.5);
      }
    }
  }

  /**
   * <code>Action</code> that updates this COMPONENT to focus on the
   * currently selected chart item.
   */
  private class NodeAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if(!Selections.isNodeSelectionEmpty(getGraph2D())) {
        performNodeAction(getGraph2D().selectedNodes().node());
      }
    }
  }

  /**
   * <code>Action</code> that updates this component to adjust its zoom level
   * and view point such that all of the current chart is visible at once.
   */
  private class FitContentAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      fitContent(true);
    }
  }
}
