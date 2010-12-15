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

import y.view.Graph2D;
import y.view.hierarchy.DefaultNodeChangePropagator;
import y.view.hierarchy.HierarchyJTree;
import y.view.hierarchy.HierarchyManager;
import y.view.hierarchy.HierarchyTreeModel;
import y.view.hierarchy.HierarchyTreeTransferHandler;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

/**
 * This demo shows how to use class {@link y.view.hierarchy.HierarchyJTree} to display the hierarchical structure.
 *
 * HierarchyJTree provides a different view on the graph structure, as well as (optionally) navigational actions
 * and support for changes in the hierarchical structure.
 */
public class HierarchyJTreeDemo extends GroupingDemo {

  /**
   * Instantiates this demo. Builds the GUI.
   */
  public HierarchyJTreeDemo() {
    JTree tree = configureHierarchyJTree();

    //plug the gui elements together and add them to the pane
    JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setPreferredSize(new Dimension(150, 0));
    scrollPane.setMinimumSize(new Dimension(150, 0));
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, view);
    view.fitContent();
    contentPane.add(splitPane, BorderLayout.CENTER);
  }

  protected JTree configureHierarchyJTree() {
    Graph2D rootGraph = view.getGraph2D();

    //propagates text label changes on nodes as change events
    //on the hierarchy.
    rootGraph.addGraph2DListener(new DefaultNodeChangePropagator());

    //create a TreeModel, that represents the hierarchy of the nodes.
    HierarchyManager hierarchy = getHierarchyManager();
    HierarchyTreeModel htm = new HierarchyTreeModel(hierarchy);

    //use a convenience comparator that sorts the elements in the tree model
    //folder/group nodes will come before normal nodes
    htm.setChildComparator(HierarchyTreeModel.createNodeStateComparator(hierarchy));

    //display the graph hierarchy in a special JTree using the given TreeModel
    JTree tree = new HierarchyJTree(hierarchy, htm);

    //add a navigational action to the tree - when double clicking a node in the tree,
    //it will be centered in the view (if necessary navigating into an inner graph of a folder node)
    tree.addMouseListener(new HierarchyJTreeDoubleClickListener(view));

    //add drag and drop functionality to HierarchyJTree. The drag and drop gesture
    //will allow to reorganize the group structure using HierarchyJTree.
    tree.setDragEnabled(true);
    tree.setTransferHandler(new HierarchyTreeTransferHandler(hierarchy));
    return tree;
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new HierarchyJTreeDemo()).start();
      }
    });
  }
}
