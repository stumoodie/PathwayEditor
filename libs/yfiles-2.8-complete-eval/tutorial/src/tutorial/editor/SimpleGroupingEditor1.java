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
package tutorial.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import y.view.Arrow;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

public class SimpleGroupingEditor1 {
  /** HierarchyManager provides support for graph hierarchies. */
  HierarchyManager hierarchy;

  public SimpleGroupingEditor1(Dimension size, String title) {
    view = createGraph2DView();
    graph = view.getGraph2D();
    // Instantiate a HierarchyManager for the graph.
    hierarchy = new HierarchyManager(graph);
    frame = createApplicationFrame(size, title, view);
    configureDefaultRealizers(graph);
    // Configure group node type and visual representation.
    configureDefaultGroupNodeRealizers(hierarchy);
  }

  protected void configureDefaultGroupNodeRealizers(HierarchyManager hierarchy) {
    // Get the graph factory that maintains the default group node/folder node realizer.
    DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory)hierarchy.getGraphFactory();

    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    // Get the default configuration for a GenericGroupNodeRealizer and add it to 
    // the factory that holds all configurations for generic node realizers.
    factory.addConfiguration("DefaultGroupNodeConfig", 
                             GenericGroupNodeRealizer.createDefaultConfigurationMap());

    GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();
    // Use the default configuration.
    gnr.setConfiguration("DefaultGroupNodeConfig");
    gnr.setFillColor(new Color(202, 236, 255, 132));
    gnr.setLineColor(new Color(102, 102, 153, 255));

    // Set the default group node/folder node realizer.
    hgf.setDefaultGroupNodeRealizer(gnr.createCopy());
    hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView();
    // Add a mouse wheel listener to zoom in and out of the view.
    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    // Add the central view mode for an editor type application.
    editMode = new EditMode();
    // Enables convenient switching between group node and folder node presentation.
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    view.addViewMode(editMode);
    // "Install" keyboard support and many predefined hierarchy-related actions.
    new Graph2DViewActions(view).install();
    return view;
  }

  /** Creates a toolbar for this demo. */
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.add(new FitContent(getView()));
    toolbar.add(new Zoom(getView(), 1.25));
    toolbar.add(new Zoom(getView(), 0.8));
    
    toolbar.addSeparator();
    // Get the action map from the Graph2DView's canvas component.
    ActionMap aMap = view.getCanvasComponent().getActionMap();
    // Add some of the hierarchy-related actions as toolbar buttons.
    toolbar.add(aMap.get(Graph2DViewActions.GROUP_SELECTION)).setText("Group Selection");
    toolbar.add(aMap.get(Graph2DViewActions.UNGROUP_SELECTION)).setText("Ungroup Selection");
    
    return toolbar;
  }

  // Code from "previous" demo. ////////////////////////////////////////////////
  
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;
  /** The yFiles graph type. */
  Graph2D graph;
  /** The yFiles view mode that handles editing. */
  EditMode editMode;
  
  public SimpleGroupingEditor1() {
    this(new Dimension(600, 300), "");
    frame.setTitle(getClass().getName());
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(size);
    // Add the given view to the panel.
    panel.add(view, BorderLayout.CENTER);
    // Add a toolbar with some actions to the panel, too.
    panel.add(createToolBar(), BorderLayout.NORTH);
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getRootPane().setContentPane(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  protected void configureDefaultRealizers(Graph2D graph) {
    // Add an arrowhead decoration to the target side of the edges.
    graph.getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);
    // Set the node size and some other graphical properties.
    NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
    defaultNodeRealizer.setSize(80, 30);
    defaultNodeRealizer.setFillColor(Color.ORANGE);
    defaultNodeRealizer.setLineType(LineType.DASHED_1);
  }

  public void show() {
    frame.setVisible(true);
  }

  public Graph2DView getView() {
    return view;
  }

  public Graph2D getGraph() {
    return graph;
  }

  /** Action that fits the content nicely inside the view. */
  protected static class FitContent extends AbstractAction {
    Graph2DView view;
    
    public FitContent(Graph2DView view) {
      super("Fit Content");
      this.view = view;
      this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }
    
    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }

  /** Action that applies a specified zoom level to the given view. */
  protected static class Zoom extends AbstractAction {
    Graph2DView view;
    double factor;

    public Zoom(Graph2DView view, double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      this.view = view;
      this.factor = factor;
      this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      // Adjusts the size of the view's world rectangle. The world rectangle 
      // defines the region of the canvas that is accessible by using the 
      // scrollbars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);
      view.updateView();
    }
  }

  public static void main(String[] args) {
    SimpleGroupingEditor1 sge = 
      new SimpleGroupingEditor1(new Dimension(600, 300), SimpleGroupingEditor1.class.getName());
    sge.show();
  }
}
