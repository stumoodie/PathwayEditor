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

import demo.view.DemoBase;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2D;
import y.view.EdgeRealizer;
import y.view.ViewMode;
import y.view.LineType;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.Layouter;
import y.layout.LayoutGraph;
import y.base.EdgeCursor;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeList;
import y.base.EdgeList;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.TableEditorFactory;
import y.option.Editor;
import y.util.Maps;
import y.util.GraphHider;
import y.algo.ShortestPaths;
import y.algo.Paths;
import y.algo.Cycles;

import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URL;

/**
 * This demo presents the critical path feature of the hierarchic layouter. The layouter tries to vertically align each node pair
 * that is connected by an edge marked as "critical". This feature can be utilized to highlight different edge paths that are relevant for a user.
 * <p>
 * The demo allows to manually mark/unmark critical edges by selecting some edges and, then, pressing button "Mark Selected Edges"/"Unmark Selected Edges".
 * Critical edges are colored red, common edges are colored black. The current state of selected edges can be toggled by double-clicking.
 * </p><p>
 * Pressing the "Apply Layout" button calculates a new layout of the current graph.
 * </p><p>
 * Pressing button "Mark Longest Path" allows to automatically select all edges that belong to a longest path of the graph.
 * If two nodes of the graph are marked as selected, pressing button "Mark Path Between Two Nodes" selects all edges
 * of the shortest-path between this nodes.
 * </p>
 *
 */
public class CriticalPathDemo extends DemoBase {
  private static final Color COLOR_CRITICAL_EDGE = Color.RED;
  private static final Color COLOR_COMMON_EDGE = Color.BLACK;

  private boolean backloopRoutingEnabled;
  private boolean edgeStraighteningOptimizationEnabled;
  private boolean useOrthogonalEdgeRoutes;
  private int minimalNodeDistance;
  private int minimalLayerDistance;

  public CriticalPathDemo() {
    this(null);
  }

  public CriticalPathDemo(final String helpFilePath) {
    JComponent helpPane = null;
    if (helpFilePath != null) {
      final URL url = getClass().getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        helpPane = createHelpPane(url);
      }
    }

    final JPanel propertiesPanel = new JPanel(new BorderLayout());
    propertiesPanel.add(createOptionTable(createOptionHandler()), BorderLayout.NORTH);
    if (helpPane != null) {
      helpPane.setPreferredSize(new Dimension(400, 400));
      propertiesPanel.add(helpPane);
    }

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view, propertiesPanel);
    splitPane.setResizeWeight(0.95);
    splitPane.setContinuousLayout(false);
    contentPane.add(splitPane, BorderLayout.CENTER);
    loadInitialGraph();
  }

  /** Adds an extra layout action to the toolbar */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();   
    bar.addSeparator();
    bar.add(new LayoutAction());
    bar.addSeparator();
    bar.add(new MarkSelectionAction());
    bar.add(new UnmarkSelectionAction());
    bar.addSeparator();
    bar.add(new MarkLongestPath());
    bar.add(new MarkShortestPathBetweenNodes());
    return bar;
  }

  private void loadInitialGraph() {
    loadGraph("resource/critical_path.graphml");    
  }


  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    minimalLayerDistance = 60;
    OptionItem minimalLayerDistanceItem = layoutOptionHandler.addInt("Minimal Layer Distance", minimalLayerDistance);
    minimalLayerDistanceItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        minimalLayerDistance = layoutOptionHandler.getInt("Minimal Layer Distance");
      }
    });

    minimalNodeDistance = 30;
    OptionItem minimalNodeDistanceItem = layoutOptionHandler.addInt("Minimal Node Distance", minimalNodeDistance);
    minimalNodeDistanceItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        minimalNodeDistance = layoutOptionHandler.getInt("Minimal Node Distance");
      }
    });

    useOrthogonalEdgeRoutes = true;
    OptionItem useOrthogonalEdgeRoutesItem = layoutOptionHandler.addBool("Use Orthogonal Edge Routes", useOrthogonalEdgeRoutes);
    useOrthogonalEdgeRoutesItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        useOrthogonalEdgeRoutes = layoutOptionHandler.getBool("Use Orthogonal Edge Routes");
      }
    });

    backloopRoutingEnabled = true;
    OptionItem backloopRoutingEnabledItem = layoutOptionHandler.addBool("Enable Backloop Routing", backloopRoutingEnabled);
    backloopRoutingEnabledItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        backloopRoutingEnabled = layoutOptionHandler.getBool("Enable Backloop Routing");
      }
    });

    edgeStraighteningOptimizationEnabled = true;
    OptionItem edgeStraighteningOptimizationEnabledItem = layoutOptionHandler.addBool("Enable Edge Straightening", edgeStraighteningOptimizationEnabled);
    edgeStraighteningOptimizationEnabledItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        edgeStraighteningOptimizationEnabled = layoutOptionHandler.getBool("Enable Edge Straightening");
      }
    });

    return layoutOptionHandler;
  }

  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super(" Apply Layout ");
    }

    public void actionPerformed(ActionEvent e) {
      //determine critical edges
      Graph2D g = view.getGraph2D();
      EdgeMap edge2CriticalValue = Maps.createHashedEdgeMap();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();        
        if (isCritical(edge, g)) {
          edge2CriticalValue.setDouble(edge, 1.0);
        } else {
          edge2CriticalValue.setDouble(edge, 0.0);
        }
      }

      //register critical edges
      g.addDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY, edge2CriticalValue);

      try {
        Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        executor.getLayoutMorpher().setSmoothViewTransform(true);
        executor.getLayoutMorpher().setEasedExecution(true);
        executor.doLayout(view, getHierarchicLayouter());
      } finally {

        g.removeDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);
      }
    }
  }

  private void markAsCriticalEdge(Edge e, Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    eRealizer.setLineColor(COLOR_CRITICAL_EDGE);
    eRealizer.setLineType(LineType.LINE_2);
  }

  private void unmarkEdge(Edge e,Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    eRealizer.setLineColor(COLOR_COMMON_EDGE);
    eRealizer.setLineType(LineType.LINE_1);
  }

  private boolean isCritical(Edge e, Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    return (eRealizer.getLineColor() == COLOR_CRITICAL_EDGE);
  }

  class MarkSelectionAction extends AbstractAction {
    MarkSelectionAction() {
      super(" Mark Selected Edges ");
    }

     public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();
        if (g.isSelected(edge)) {
          markAsCriticalEdge(edge, g);
        }
      }
      g.updateViews();
    }
  }

  class UnmarkSelectionAction extends AbstractAction {
    UnmarkSelectionAction() {
      super(" Unmark Selected Edges ");
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();
        if (g.isSelected(edge)) {
          unmarkEdge(edge, g);
        }
      }
      g.updateViews();
    }
  }

  class MarkShortestPathBetweenNodes extends AbstractAction {
    MarkShortestPathBetweenNodes() {
      super(" Mark Path Between Two Nodes ");
    }

    public void actionPerformed(ActionEvent ae) {
      Graph2D g = view.getGraph2D();
      NodeList selectedNodes = new NodeList(g.selectedNodes());
      if (!selectedNodes.isEmpty()) {
        EdgeMap path = Maps.createHashedEdgeMap();
        Node n1 = selectedNodes.firstNode();
        Node n2 = selectedNodes.lastNode();
        ShortestPaths.findShortestUniformPaths(g, n1, n2, true, path);
        if (!foundPath(g, path)) {
          ShortestPaths.findShortestUniformPaths(g, n2, n1, true, path);
        }        
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
          Edge e = ec.edge();         
          if (path.getBool(e)) {
            markAsCriticalEdge(e, g);
          } else {
            unmarkEdge(e, g);
          }
        }
        g.updateViews();
      }
    }
  }

  class MarkLongestPath extends AbstractAction {
    MarkLongestPath() {
      super(" Mark Longest Path ");
    }

    public void actionPerformed(ActionEvent ae) {
      Graph2D g = view.getGraph2D();

      //reset marks
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        unmarkEdge(ec.edge(), g);
      }

      //make acyclic
      EdgeList cycleEdges = Cycles.findAllCycleEdges(g, true);
      GraphHider hider = new GraphHider(g);
      hider.hide(cycleEdges);

      //mark edges of longest path
      for (EdgeCursor ec = Paths.findLongestPath(g).edges(); ec.ok(); ec.next()) {
        markAsCriticalEdge(ec.edge(), g);
      }
      hider.unhideAll();
      g.updateViews();
    }
  }

  private boolean foundPath(LayoutGraph g, EdgeMap path) {
    for(EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
      if(path.getBool(ec.edge())) {
        return true;
      }
    }
    return false;
  }
  
  private JComponent createOptionTable(OptionHandler oh) {
    //Create editor and add associate Option Handler with the editor
    TableEditorFactory tef = new TableEditorFactory();
    oh.setAttribute(TableEditorFactory.ATTRIBUTE_INFO_POSITION, TableEditorFactory.InfoPosition.NONE);
    final Editor editor = tef.createEditor(oh);

    JComponent optionPane = editor.getComponent();
    optionPane.setPreferredSize(new Dimension(400, 100));
    optionPane.setMaximumSize(new Dimension(400, 100));
    return optionPane;
  }

  private Layouter getHierarchicLayouter() {
    IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setBackloopRoutingEnabled(backloopRoutingEnabled);
    if(layouter.getNodePlacer() instanceof SimplexNodePlacer) {
      ((SimplexNodePlacer) layouter.getNodePlacer()).setEdgeStraighteningOptimizationEnabled(edgeStraighteningOptimizationEnabled);
    }
    layouter.setOrthogonallyRouted(useOrthogonalEdgeRoutes);
    layouter.setMinimumLayerDistance(minimalLayerDistance);
    layouter.setNodeToNodeDistance(minimalNodeDistance);
    return layouter;
  }

   protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new ViewMode() {
      /** A mouse button get clicked */
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Graph2D g = view.getGraph2D();
        if (e.getClickCount() == 2) {
          final EdgeCursor selectedEdges = g.selectedEdges();
          if (selectedEdges != null && selectedEdges.size() > 0) {
            //Toggle color for all selected edges
            for (; selectedEdges.ok(); selectedEdges.next()) {              
              if(isCritical(selectedEdges.edge(), g)) {
                unmarkEdge(selectedEdges.edge(), g);
              } else {
                markAsCriticalEdge(selectedEdges.edge(), g);
              }
            }
          }
          view.updateView();
        }
      }
    });
  }

  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new CriticalPathDemo("resource/criticalpathhelp.html")).start("Critical Path Demo");
      }
    });
  }
}