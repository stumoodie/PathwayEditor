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

import y.base.Node;
import y.base.NodeList;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.hierarchy.HierarchyManager;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;

/**
 * This demo shows how to implement actions for navigation between different hierarchy levels.
 * <p>
 * This demo creates two actions (along with corresponding key bindings):
 * </p>
 * <ul>
 * <li>EnterGroupAction navigates to the content of a group or folder node. This action is bound to <code>CTRL+PAGE_DOWN</code>.</li>
 * <li>NavigateToParentAction navigates to the parent graph of an inner graph. This action is bound to <code>CTRL+PAGE_UP</code>.</li>
 * </ul>
 * <p>
 * In addition, both the "Grouping" menu and the context menu provide these actions.
 * </p>
 */
public class GroupNavigationDemo extends GroupingDemo {
  private static final String ENTER_GROUP_ACTION = "ENTER_GROUP_ACTION";
  private static final String NAVIGATE_TO_PARENT_ACTION = "NAVIGATE_TO_PARENT_ACTION";


  protected void registerViewActions() {
    super.registerViewActions();

    //Register custom navigation actions and corresponding key bindings...
    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(ENTER_GROUP_ACTION, new EnterGroupAction(ENTER_GROUP_ACTION, view));
    actionMap.put(NAVIGATE_TO_PARENT_ACTION, new NavigateToParentAction(NAVIGATE_TO_PARENT_ACTION, view));
    InputMap inputMap = view.getCanvasComponent().getInputMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), ENTER_GROUP_ACTION);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), NAVIGATE_TO_PARENT_ACTION);
  }

  static {
    actionNames.put(ENTER_GROUP_ACTION, "Enter Group");
    actionNames.put(NAVIGATE_TO_PARENT_ACTION, "Navigate to Parent");
  }

  protected void populateGroupingPopup(JPopupMenu pm, double x, double y, final Node node, boolean selected) {
    super.populateGroupingPopup(pm, x, y, node, selected);
    pm.addSeparator();
    //Actions for navigation actions
    //We want to enter only the group oder folder node for which the popup has been triggered
    //so we extend the action
    JMenuItem item = new JMenuItem(new EnterGroupAction(ENTER_GROUP_ACTION, view){
      protected Node getGroupOrFolderNode(Graph2D g) {
        if(node != null) {
          return node;
        }
        return super.getGroupOrFolderNode(g);
      }
    });
    item.setText("Enter Group");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK));
    if((node != null && !getHierarchyManager().isNormalNode(node)) || selected) {
      item.setEnabled(true);
    }
    pm.add(item);
    //We enable this context menu action only if we are not at root graph level
    registerAction(pm, NAVIGATE_TO_PARENT_ACTION, view.getGraph2D() != getHierarchyManager().getRootGraph());
  }

  protected void populateGroupingMenu(JMenu hierarchyMenu) {
    super.populateGroupingMenu(hierarchyMenu);
    hierarchyMenu.addSeparator();
    //Actions for navigation actions
    registerAction(hierarchyMenu, ENTER_GROUP_ACTION, true);
    registerAction(hierarchyMenu, NAVIGATE_TO_PARENT_ACTION, true);
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new GroupNavigationDemo()).start();
      }
    });
  }

  private final Set openGroupNodes = new HashSet();//All group nodes with state "open" are saved within

  /**
   * Create custom action that allow to navigate to the inner graph of a group/folder node.
   *
   * If the node is a group node, it is closed first, since separate inner graph instances exist only
   * for folder nodes.
   */
  class EnterGroupAction extends Graph2DViewActions.AbstractGroupingAction {
    public EnterGroupAction(String name, Graph2DView view) {
      super(name, view);
    }

    /**
     * Returns the selected node if exactly one node is selected
     */
    protected Node getGroupOrFolderNode(Graph2D g) {
      NodeList selectedNodes = new NodeList(g.selectedNodes());
      if (selectedNodes.size() == 1) {
        return selectedNodes.firstNode();
      } else {
        return null;
      }
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DView view = getView(e);
      Graph2D g = view.getGraph2D();
      Node groupNode = getGroupOrFolderNode(g);
      if (groupNode != null) {
        enterGroup(groupNode, view);
      }
    }

    /**
     * Enter the group.
     *
     * @param groupOrFolderNode the group or folder node that is opened
     */
    public void enterGroup(Node groupOrFolderNode, Graph2DView view) {
      Graph2D graph = view.getGraph2D();
      HierarchyManager hierarchyManager = getHierarchyManager(graph);
      if (hierarchyManager.isNormalNode(groupOrFolderNode)) {
        //Do nothing for normal nodes
        return;
      }

      //Close (if it is a group), since actual inner graphs only exist for folder nodes.
      if (hierarchyManager.isGroupNode(groupOrFolderNode)) {
        //Remember state for later
        openGroupNodes.add(groupOrFolderNode);
        preNodeStateChange(groupOrFolderNode, graph);
        getHierarchyManager(graph).closeGroup(groupOrFolderNode);
        postNodeStateChange(groupOrFolderNode, graph);
      }

      Graph2D folderGraph = (Graph2D) hierarchyManager.getInnerGraph(groupOrFolderNode);
      //We change the graph in the view to the inner graph.
      view.setGraph2D(folderGraph);

      // adapt view
      view.fitContent();
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Create custom action that allow to navigate to parent graph of the current graph (if any).
   *
   * If the node was a group node before the inner graph has been entered with {@link EnterGroupAction}, it
   * will be reopened.
   */
  class NavigateToParentAction extends Graph2DViewActions.AbstractGroupingAction {
    public NavigateToParentAction(String name, Graph2DView view) {
      super(name, view);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DView view = getView(e);
      Graph2D g = view.getGraph2D();
      HierarchyManager hm = getHierarchyManager(g);
      if (g == hm.getRootGraph()) {
        //Do nothing if already at root level...
        return;
      }
      navigateToParent(view);
    }

    /**
     * Navigate to the parent graph
     */
    public void navigateToParent(Graph2DView view) {
      Graph2D graph = view.getGraph2D();
      HierarchyManager hierarchyManager = getHierarchyManager(graph);

      //Retrieve the folder node that represents the inner 
      Node folderNode = hierarchyManager.getAnchorNode(graph);

      Graph2D parentGraph = (Graph2D) hierarchyManager.getParentGraph(graph);
      //We change the graph in the view to the parent graph.
      this.view.setGraph2D(parentGraph);

      //Restore the original state of the node if it has been closed by EnterGroupAction
      if (openGroupNodes.contains(folderNode)) {
        preNodeStateChange(folderNode, graph);
        getHierarchyManager(graph).openFolder(folderNode);
        postNodeStateChange(folderNode, graph);
      }
      openGroupNodes.remove(folderNode);

      view.focusView(1.0, new Point2D.Double(graph.getCenterX(folderNode), graph.getCenterY(folderNode)), false);
      view.getGraph2D().setSelected(folderNode, true);
      view.getGraph2D().updateViews();
    }
  }
}
