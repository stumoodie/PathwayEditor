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
package demo.view.hierarchy;

import demo.view.DemoBase;
import y.base.Node;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.ProxyShapeNodeRealizer;
import y.view.ViewMode;
import y.view.NodeLabel;
import y.view.LineType;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates the basic use of <b>Nested Graph Hierarchy</b> technology
 * and also <b>Node Grouping</b>.
 * <p>
 * So-called folder nodes are used to nest graphs within them, so-called group nodes
 * are used to group a set of nodes.
 * <br>
 * Both these types of node look similar but represent different concepts: while
 * grouped nodes still belong to the same graph as their enclosing group node, the
 * graph that is contained within a folder node is a separate entity.
 * </p>
 * <p>
 * There are several ways provided to create, modify, and navigate a graph hierarchy:
 * <ul>
 * <li>
 * By means of popup menu actions and actions from the "Grouping" submenu selected nodes can be grouped and also nested.
 * Reverting these operations is also supported. For these actions, predefined key bindings are provided in class
 * {@link y.view.Graph2DViewActions}.
 * </li>
 * <li>.
 * </li>
 * <li>
 * By Shift-dragging nodes they can be moved into and out of group nodes.
 * </li>
 * <li>
 * Folder node and group node both allow switching to the other type by either using
 * popup menu actions or clicking the icon in their upper-left corner.
 * </li>
 * </ul>
 * </p>
 * <p>
 * Note that the size of group nodes is determined by the space requirements of
 * their content, i.e., their resizing behavior is restricted.
 * </p>
 */
public class GroupingDemo extends DemoBase {

  /** The name of the configuration for {@link GenericGroupNodeRealizer} */
  public static final String CONFIGURATION_GROUP = "GroupingDemo_GROUP_NODE";

  protected static final Map actionNames;
  static {
    actionNames = new HashMap();
    actionNames.put(Graph2DViewActions.CLOSE_GROUPS, "Close Selected Groups");
    actionNames.put(Graph2DViewActions.OPEN_FOLDERS, "Open Selected Folders");
    actionNames.put(Graph2DViewActions.GROUP_SELECTION, "Group Selection");
    actionNames.put(Graph2DViewActions.UNGROUP_SELECTION, "Ungroup Selection");
    actionNames.put(Graph2DViewActions.FOLD_SELECTION, "Fold Selection");

    actionNames.put("CREATE_NEW_GROUP_NODE_ACTION", "Create Empty Group");
    actionNames.put("CREATE_NEW_FOLDER_NODE_ACTION", "Create Empty Folder");
  }

  /**
   * Instantiates this demo. Builds the GUI.
   */
  public GroupingDemo() {
    Graph2D rootGraph = view.getGraph2D();
    //create a hierarchy manager with the given root graph
    createHierarchyManager(rootGraph);
    configureDefaultGroupNodeRealizers();
    loadInitialGraph();
  }

  protected void loadInitialGraph() {
    loadGraph("resource/grouping.graphml");    
  }
  
  protected HierarchyManager createHierarchyManager(Graph2D rootGraph) {
    return new HierarchyManager(rootGraph);
  }

  protected void configureDefaultGroupNodeRealizers() {
    //Create additional configuration for default group node realizers
    DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory) getHierarchyManager().getGraphFactory();

    Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    factory.addConfiguration(CONFIGURATION_GROUP, map);
    Object abf = factory.getImplementation(CONFIGURATION_GROUP,
        GenericGroupNodeRealizer.GenericAutoBoundsFeature.class);
    if (abf instanceof DefaultGenericAutoBoundsFeature) {
      ((DefaultGenericAutoBoundsFeature) abf).setConsiderNodeLabelSize(true);
    }

    GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();

    //Register first, since this will also configure the node label
    gnr.setConfiguration(CONFIGURATION_GROUP);

    //Nicer colors
    gnr.setFillColor(new Color(202,236,255,132));
    gnr.setLineColor(new Color(102, 102,153,255));
    gnr.setLineType(LineType.DOTTED_1);
    NodeLabel label = gnr.getLabel();
    label.setBackgroundColor(new Color(153,204,255,255));
    label.setTextColor(Color.BLACK);
    label.setFontSize(15);

    hgf.setProxyNodeRealizerEnabled(true);

    hgf.setDefaultGroupNodeRealizer(gnr.createCopy());
    hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
  }

  protected JMenuBar createMenuBar() {
    JMenuBar mb = super.createMenuBar();
    JMenu menu = new JMenu("Grouping");
    populateGroupingMenu(menu);
    mb.add(menu);
    return mb;
  }

  /**
   * Populates the "Grouping" menu with grouping specific actions.
   *
   * These actions are provided by class {@link y.view.Graph2DViewActions} and
   * are already present in {@link Graph2DView}'s {@link ActionMap}.
   *
   */
  protected void populateGroupingMenu(JMenu hierarchyMenu) {
    // Predefined actions for open/close groups
    registerAction(hierarchyMenu, Graph2DViewActions.CLOSE_GROUPS, true);
    registerAction(hierarchyMenu, Graph2DViewActions.OPEN_FOLDERS, true);

    hierarchyMenu.addSeparator();

    // Predefined actions for group/fold/ungroup
    registerAction(hierarchyMenu, Graph2DViewActions.GROUP_SELECTION, true);
    registerAction(hierarchyMenu, Graph2DViewActions.UNGROUP_SELECTION, true);
    registerAction(hierarchyMenu, Graph2DViewActions.FOLD_SELECTION, true);
  }


  protected EditMode createEditMode() {
    EditMode mode = super.createEditMode();
    //add hierarchy actions to the views popup menu
    mode.setPopupMode(createPopupMode());
    mode.getMouseInputMode().setNodeSearchingEnabled(true);

    //Add a visual indicator for the target node of an edge creation - makes it easier to
    //see the target for nested graphs
    ViewMode createEdgeMode = mode.getCreateEdgeMode();
    if (createEdgeMode instanceof CreateEdgeMode) {
      ((CreateEdgeMode) createEdgeMode).setIndicatingTargetNode(true);
    }
    return mode;
  }

  protected PopupMode createPopupMode() {
    return new HierarchicPopupMode();
  }


  /**
   * Register key bindings for both predefined actions and our custom actions.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.DELETE_SELECTION, createDeleteSelectionActionImpl());
    actionMap.put("CREATE_NEW_GROUP_NODE_ACTION", new CreateNewGroupNodeAction());
    actionMap.put("CREATE_NEW_FOLDER_NODE_ACTION", new CreateNewFolderNodeAction());
    InputMap inputMap = view.getCanvasComponent().getInputMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "CREATE_NEW_GROUP_NODE_ACTION");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "CREATE_NEW_FOLDER_NODE_ACTION");
  }

  protected Action createDeleteSelectionAction() {
    final Action action = createDeleteSelectionActionImpl();
    final URL deleteIconUrl = DemoBase.class.getResource("resource/delete.png");
    if (deleteIconUrl != null) {
      action.putValue(Action.SMALL_ICON, new ImageIcon(deleteIconUrl));
    }
    action.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    return action;
  }

  /**
   * Creates a {@link y.view.Graph2DViewActions.DeleteSelectionAction} instance
   * that is configured to prevent group nodes from shrinking when child nodes
   * are deleted.
   * @return {@link y.view.Graph2DViewActions.DeleteSelectionAction} instance.
   */
  private Action createDeleteSelectionActionImpl() {
    final Graph2DViewActions.DeleteSelectionAction action =
            new Graph2DViewActions.DeleteSelectionAction(view);
    action.setKeepingParentGroupNodeSizes(true);
    return action;
  }


  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new GroupingDemo()).start();
      }
    });
  }

  /**
   * Populates the popup menu with grouping specific actions.
   *
   * These actions are provided by class {@link y.view.Graph2DViewActions} and
   * are already present in {@link Graph2DView}'s {@link ActionMap}.
   *
   */
  protected void populateGroupingPopup(JPopupMenu pm, final double x, final double y, Node node, boolean selected) {
    // Predefined actions for open/close groups
    registerAction(
        pm, Graph2DViewActions.CLOSE_GROUPS,
        node != null && getHierarchyManager().isGroupNode(node));
    registerAction(
        pm, Graph2DViewActions.OPEN_FOLDERS,
        node != null && getHierarchyManager().isFolderNode(node));

    pm.addSeparator();

    // Predefined actions for group/fold/ungroup
    registerAction(pm, Graph2DViewActions.GROUP_SELECTION, selected);
    registerAction(pm, Graph2DViewActions.UNGROUP_SELECTION, selected);
    registerAction(pm, Graph2DViewActions.FOLD_SELECTION, selected);

    pm.addSeparator();

    //We customize both "Create..." actions so that the newly created node lies at the coordinates of the mouse click
    //(for "Group Selection"/"Fold Selection", the location is determined by the content's location instead.
    JMenuItem item = new JMenuItem(new CreateNewGroupNodeAction(view){
      protected void setGroupNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
        graph.setLocation(groupNode, x, y);
      }
    });
    item.setText("Create Empty Group");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    pm.add(item);

    item = new JMenuItem(new CreateNewFolderNodeAction(view){
      protected void setFolderNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
        graph.setLocation(groupNode, x, y);
      }
    });
    item.setText("Create Empty Folder");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    pm.add(item);
  }

  /**
   * Register a single action that is already present in the view's ActionMap.
   *
   * @param key The key under which the Action is registered
   * @param enabled Whether to enable the action.
   */
  protected void registerAction(final Object menu, final Object key, final boolean enabled) {
    final ActionMap viewActions = view.getCanvasComponent().getActionMap();

    final Action action = viewActions.get(key);
    if (action != null) {
      final JMenuItem item = new JMenuItem(action);
      final String name = (String) actionNames.get(key);
      if (name != null) {
        item.setText(name);
      }
      item.setEnabled(enabled);

      // explicitly setting an accelerator for these menu items is actually
      // not necessary here because the actions are already registered in
      // DemoBase.registerViewActions
      // we do it nonetheless as a simple way to display the default
      // key bindings of each action
      final InputMap imap = view.getCanvasComponent().getInputMap();
      final KeyStroke[] keyStrokes = imap.allKeys();
      if (keyStrokes != null) {
        for (int i = 0; i < keyStrokes.length; ++i) {
          if (imap.get(keyStrokes[i]) == key) {
            item.setAccelerator(keyStrokes[i]);
            break;
          }
        }
      }

      if (menu instanceof JMenu) {
        ((JMenu) menu).add(item);
      } else if (menu instanceof JPopupMenu) {
        ((JPopupMenu) menu).add(item);
      }
    }
  }

  protected HierarchyManager getHierarchyManager() {
    return view.getGraph2D().getHierarchyManager();
  }

  //////////////////////////////////////////////////////////////////////////////
  // VIEW MODES ////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * provides the context sensitive popup menus
   */
  class HierarchicPopupMode extends PopupMode {
    public JPopupMenu getPaperPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      populateGroupingPopup(pm, x, y, null, false);
      return pm;
    }

    public JPopupMenu getNodePopup(Node v) {
      Graph2D graph = getGraph2D();
      JPopupMenu pm = new JPopupMenu();
      populateGroupingPopup(pm, graph.getCenterX(v), graph.getCenterY(v), v, true);
      return pm;
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      populateGroupingPopup(pm, x, y, null, getGraph2D().selectedNodes().ok());
      return pm;
    }
  }

  /**
   * Action that creates a new empty group node.
   */
  public static class CreateNewGroupNodeAction extends Graph2DViewActions.AbstractGroupingAction {

    public CreateNewGroupNodeAction() {
      this(null);
    }

    public CreateNewGroupNodeAction(final Graph2DView view) {
      super("CREATE_NEW_GROUP_NODE", view);
    }

    public void actionPerformed(ActionEvent e) {
      final Graph2DView graph2DView = getView(e);
      if (graph2DView != null) {
        createGroupNode(graph2DView);
        graph2DView.getGraph2D().updateViews();
      }
    }

    /**
     * Create an empty group node, assigns a name and sets the node bounds.
     */
    protected Node createGroupNode(Graph2DView view) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      Node groupNode;
      try {
        groupNode = createGroupNodeImpl(graph);
        assignGroupName(groupNode, view);
        setGroupNodeBounds(view, graph, groupNode);
      } finally {
        graph.firePostEvent();
      }
      return groupNode;
    }

    protected Node createGroupNodeImpl(Graph2D graph) {
      return getHierarchyManager(graph).createGroupNode(graph);
    }

    protected void setGroupNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
      double x = view.getCenter().getX();
      double y = view.getCenter().getY();
      graph.setCenter(groupNode, x, y);
    }

    protected void assignGroupName(Node groupNode, Graph2DView view) {
      NodeRealizer nr = view.getGraph2D().getRealizer(groupNode);
      if (nr instanceof ProxyShapeNodeRealizer) {
        ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
        pnr.getRealizer(0).setLabelText(createGroupName(groupNode, view));
        pnr.getRealizer(1).setLabelText(createFolderName(groupNode, view));
      } else {
        nr.setLabelText(createGroupName(groupNode, view));
      }
    }

    protected String createFolderName(Node folderNode, Graph2DView view) {
      return "Folder";
    }

    protected String createGroupName(Node groupNode, Graph2DView view) {
      return "Group";
    }
  }

  /**
   * Action that creates a new empty folder node.
   */
  public static class CreateNewFolderNodeAction extends Graph2DViewActions.AbstractGroupingAction {

    public CreateNewFolderNodeAction() {
      this(null);
    }

    public CreateNewFolderNodeAction(final Graph2DView view) {
      super("CREATE_NEW_FOLDER_NODE", view);
    }

    public void actionPerformed(ActionEvent e) {
      final Graph2DView graph2DView = getView(e);
      if (graph2DView != null) {
        createFolderNode(graph2DView);
        graph2DView.getGraph2D().updateViews();
      }
    }

    /**
     * Create an empty folder node, assigns a name and sets the node bounds.
     */
    protected Node createFolderNode(Graph2DView view) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      Node groupNode;
      try {
        groupNode = createFolderNodeImpl(graph);
        assignFolderName(groupNode, view);
        setFolderNodeBounds(view, graph, groupNode);
      } finally {
        graph.firePostEvent();
      }
      return groupNode;
    }

    protected Node createFolderNodeImpl(Graph2D graph) {
      return getHierarchyManager(graph).createFolderNode(graph);
    }

    protected void setFolderNodeBounds(Graph2DView view, Graph2D graph, Node folderNode) {
      double x = view.getCenter().getX();
      double y = view.getCenter().getY();
      graph.setCenter(folderNode, x, y);
    }

    protected void assignFolderName(Node groupNode, Graph2DView view) {
      NodeRealizer nr = view.getGraph2D().getRealizer(groupNode);
      if (nr instanceof ProxyShapeNodeRealizer) {
        ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
        pnr.getRealizer(0).setLabelText(createGroupName(groupNode, view));
        pnr.getRealizer(1).setLabelText(createFolderName(groupNode, view));
      } else {
        nr.setLabelText(createGroupName(groupNode, view));
      }
    }

    protected String createFolderName(Node folderNode, Graph2DView view) {
      return "Folder";
    }

    protected String createGroupName(Node groupNode, Graph2DView view) {
      return "Group";
    }
  }
}
