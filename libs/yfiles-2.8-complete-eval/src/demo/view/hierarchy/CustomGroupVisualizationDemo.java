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

import y.view.ImageNodePainter;
import y.view.GenericNodeRealizer;
import y.view.HitInfo;
import y.view.Graph2DViewActions;
import y.view.ViewMode;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.NodeLabel;
import y.view.ShapeNodePainter;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.HierarchyManager;
import y.base.Node;
import y.geom.YInsets;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Map;
import javax.swing.ActionMap;
import javax.swing.Action;

import demo.view.DemoBase;

/**
 * Demonstrates how to customize the visual representation of group and folder
 * nodes using {@link y.view.hierarchy.GenericGroupNodeRealizer}.
 *
 */
public class CustomGroupVisualizationDemo extends GroupingDemo {
  /**
   * The name of the configuration used for group nodes.
   */
  private static final String CONFIGURATION_GROUP =
          "CustomGroupVisualizationDemo_GROUP_NODE";
  /**
   * The name of the configuration used for folder nodes.
   */
  private static final String CONFIGURATION_FOLDER =
          "CustomGroupVisualizationDemo_FOLDER_NODE";

  protected void loadInitialGraph() {
    loadGraph(getClass().getResource("resource/CustomGroupVisualizationDemo.graphml"));
  }

  /**
   * Overwritten to register a view mode that opens folders/closes groups on
   * double clicks.
   */
  protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new StateChangeViewMode());
  }

  /**
   * Creates and registers configured, customized group and folder node
   * representations.
   */
  protected void configureDefaultGroupNodeRealizers() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // configure folder nodes
    // for folders, simply display an image instead of the usual rectangle
    final String resource = "resource/yicon.png";
    final URL folderUrl = DemoBase.class.getResource(resource);
    if (folderUrl == null) {
      throw new IllegalStateException("Could not find \"" + resource + "\".");
    } else {
      final ImageNodePainter folderPainter = new ImageNodePainter(folderUrl);
      final Map map = factory.createDefaultConfigurationMap();
      map.put(GenericNodeRealizer.Painter.class, folderPainter);
      map.put(GenericNodeRealizer.ContainsTest.class, folderPainter);

      configureBoundsAndSizeHandling(map);

      factory.addConfiguration(CONFIGURATION_FOLDER, map);
    }

    final GenericGroupNodeRealizer fnr = new GenericGroupNodeRealizer();
    fnr.setConfiguration(CONFIGURATION_FOLDER);
    fnr.setGroupClosed(true);
    configureDefaultFolderLabel(fnr.getLabel());


    // configure group nodes
    // for groups, display a round rectangle with the default label to the left
    // and rotated 90 degrees counterclockwise
    {
      final ShapeNodePainter groupPainter =
              new ShapeNodePainter(ShapeNodePainter.ROUND_RECT);
      final Map map = factory.createDefaultConfigurationMap();
      map.put(GenericNodeRealizer.Painter.class, groupPainter);
      map.put(GenericNodeRealizer.ContainsTest.class, groupPainter);

      configureBoundsAndSizeHandling(map);

      factory.addConfiguration(CONFIGURATION_GROUP, map);
    }

    final GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();
    gnr.setFillColor(new Color(202, 227, 255));
    gnr.setConfiguration(CONFIGURATION_GROUP);
    gnr.setGroupClosed(false);
    configureDefaultGroupLabel(gnr.getLabel());


    // register the above configured group and folder node representations
    final DefaultHierarchyGraphFactory hgf =
            (DefaultHierarchyGraphFactory) getHierarchyManager().getGraphFactory();
    hgf.setProxyNodeRealizerEnabled(true);
    hgf.setDefaultGroupNodeRealizer(gnr);
    hgf.setDefaultFolderNodeRealizer(fnr);
  }

  /**
   * Configures the default label for folder node representations.
   * @param fnl   the default label of a folder node.
   */
  private void configureDefaultFolderLabel( final NodeLabel fnl ) {
    fnl.setFontSize(14);
    fnl.setFontStyle(Font.BOLD);
    fnl.setTextColor(Color.WHITE);
    fnl.setAutoSizePolicy(NodeLabel.AUTOSIZE_NODE_WIDTH);
    fnl.setBackgroundColor(new Color(62, 66, 69));
    fnl.setModel(NodeLabel.SANDWICH);
    fnl.setPosition(NodeLabel.S);
  }

  /**
   * Configures the default label for group node representations.
   * @param gnl   the default label of a group node.
   */
  private void configureDefaultGroupLabel( final NodeLabel gnl ) {
    gnl.setFontSize(14);
    gnl.setFontStyle(Font.BOLD);
    gnl.setTextColor(Color.WHITE);
    gnl.setAutoSizePolicy(NodeLabel.AUTOSIZE_CONTENT);
    gnl.setBackgroundColor(null);
    gnl.setRotationAngle(270);
    gnl.setPosition(NodeLabel.BOTTOM_LEFT);
  }

  /**
   * Registers custom auto bounds handling in the specified implementations
   * map for vertical labels.
   * @param map   a configuration map.
   */
  private void configureBoundsAndSizeHandling( final Map map ) {
    final DefaultGenericAutoBoundsFeature abf = new DefaultGenericAutoBoundsFeature() {
      /**
       * Overwritten to handle <code>GenericGroupNodeRealizer</code> instances
       * with vertical, left positioned default labels appropriately.
       * @param context   the group or folder node representation for which
       * insets have to be calculated.
       * @return the insets for the specified group or folder node
       * representation.
       */
      public YInsets getAutoBoundsInsets( final NodeRealizer context ) {
        if (accept(context)) {
          final YInsets insets =
                  ((GenericGroupNodeRealizer) context).getMinimalInsets();
          return new YInsets(
              insets.top,
              Math.max(insets.left, context.getLabel().getWidth() + 5),
              insets.bottom,
              insets.right
          );
        }

        return super.getInsets(context);
      }

      /**
       * Overwritten to handle <code>GenericGroupNodeRealizer</code> instances
       * with vertical, left positioned default labels appropriately.
       * @param context   the group or folder node representation for which
       * the minimal label size has to be calculated.
       * @return the minimum size that is to be reserved as label size for the
       * the specified group or folder node representation.
       */
      protected Dimension2D calculateMinimalLabelSize( final NodeRealizer context ) {
        if (accept(context)) {
          final NodeLabel label = context.getLabel();
          final double d = 2*label.getDistance();
          // since the label is rotated 90 degrees counterclockwise
          // switch its width and height
          return new Dimension(
                  (int) Math.ceil(label.getContentHeight() + d),
                  (int) Math.ceil(label.getContentWidth() + d));
        }

        return super.calculateMinimalLabelSize(context);
      }

      /**
       * Determines whether the specified realizer qualifies for custom
       * auto bounds handling due to a vertical, left positioned default label.
       * @param context   the <code>NodeRealizer</code> to check.
       * @return <code>true</code> if the specified realizer qualifies for
       * custom auto bounds handling; <code>false</code> otherwise.
       */
      private boolean accept( final NodeRealizer context ) {
        if (context instanceof GenericGroupNodeRealizer) {
          GenericGroupNodeRealizer ggnr = ((GenericGroupNodeRealizer) context);
          if (ggnr.labelCount() > 0) {
            final NodeLabel nl = ggnr.getLabel();
            if (nl.getModel() == NodeLabel.INTERNAL &&
                nl.getPosition() == NodeLabel.BOTTOM_LEFT &&
                nl.getRotationAngle() == 270) {
              return true;
            }
          }
        }

        return false;
      }
    };
    abf.setConsiderNodeLabelSize(true);
    map.put(GenericGroupNodeRealizer.GenericAutoBoundsFeature.class, abf);
    map.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, abf);
    map.put(GenericNodeRealizer.LabelBoundsChangedHandler.class, abf);
  }



  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new CustomGroupVisualizationDemo()).start();
      }
    });
  }


  /**
   * A {@link y.view.ViewMode} that handles double clicks for folder and
   * group nodes.
   */
  private static final class StateChangeViewMode extends ViewMode {
    public void mouseClicked( final double x, final double y ) {
      if (lastClickEvent.getClickCount() == 2 &&
          lastClickEvent.getButton() == 1) {
        final HitInfo hitInfo = getHitInfo(x, y);
        if (hitInfo.hasHitNodes()) {
          final Node node = hitInfo.getHitNode();
          final Graph2D graph = getGraph2D();
          final HierarchyManager manager = graph.getHierarchyManager();
          if (manager != null) {
            if (manager.isGroupNode(node)) {
              closeGroup(graph, node);
            } else if (manager.isFolderNode(node)) {
              openFolder(graph, node);
            }
          }
        }
      }
    }

    /**
     * Closes the specified group node.
     * @param graph   the specified node's associated graph.
     * @param node    the group node that has to be converted to a folder node.
     */
    protected void closeGroup( final Graph2D graph, final Node node ) {
      Action action = null;

      final ActionMap amap = view.getCanvasComponent().getActionMap();
      if (amap != null) {
        action = amap.get(Graph2DViewActions.CLOSE_GROUPS);
      }
      if (action == null) {
        action = new Graph2DViewActions.CloseGroupsAction();
      }

      view.getGraph2D().unselectAll();
      view.getGraph2D().setSelected(node, true);
      action.actionPerformed(new ActionEvent(view, ActionEvent.ACTION_PERFORMED, ""));
    }

    /**
     * Opens the specified folder node.
     * @param graph   the specified node's associated graph.
     * @param node    the folder node that has to be converted to a group node.
     */
    protected void openFolder( final Graph2D graph, final Node node ) {
      Action action = null;

      final ActionMap amap = view.getCanvasComponent().getActionMap();
      if (amap != null) {
        action = amap.get(Graph2DViewActions.OPEN_FOLDERS);
      }
      if (action == null) {
        action = new Graph2DViewActions.OpenFoldersAction();
      }

      view.getGraph2D().unselectAll();
      view.getGraph2D().setSelected(node, true);
      action.actionPerformed(new ActionEvent(view, ActionEvent.ACTION_PERFORMED, ""));
    }
  }
}
