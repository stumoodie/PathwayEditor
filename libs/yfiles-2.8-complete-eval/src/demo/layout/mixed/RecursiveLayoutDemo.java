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
package demo.layout.mixed;

import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.router.GroupNodeRouterStage;
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.Layouter;
import y.layout.LayoutGraph;
import y.layout.LayoutTool;
import y.layout.ParallelEdgeLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.util.DataProviderAdapter;
import y.base.EdgeMap;
import y.base.EdgeCursor;
import y.base.DataProvider;
import y.base.EdgeList;
import y.base.Edge;

import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.ActionMap;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.Dimension;

import demo.view.hierarchy.GroupingDemo;

/**
 * This demo extends the GroupingDemo and shows how to use the recursive group layouter. The demo shows the following use case:
 * <p/>
 * The content of each group node is recursively laid out with the specified layouter, i.e., the layouter is applied to each group node separately.
 * Note that the {@link y.layout.grouping.RecursiveGroupLayouter} also supports to specify different layout algorithms for different group nodes, see {@link MixedLayoutDemo}.
 * If the "Route Inter-Edges" option is enabled an edge router is used for routing the inter-edges (the green edges), i.e., edges which traverse the boundary of group nodes.
 * Due to the recursive layout fashion the core layout often doesn't produce satisfiable edge routes for such edges. 
 * <p/>
 * To recalculate the layout press the "Auto-Layout Graph" button or fold/unfold a group node.
 */
public class RecursiveLayoutDemo extends GroupingDemo {
  private static final byte MODE_ORTHOGONAL = 0;
  private static final byte MODE_HIERARCHIC = 1;
  private static final byte MODE_CIRCULAR = 2;
  private static final byte MODE_ORGANIC = 3;

  private byte mode = MODE_ORTHOGONAL;
  private boolean useInterEdgeRouter;

  public RecursiveLayoutDemo() {
    this(null);
  }

  public RecursiveLayoutDemo( final String helpFilePath ) {
    super();
    addHelpPane(helpFilePath);
    loadGraph();
  }

  /** Adds an extra layout action to the toolbar */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    final JComboBox comboBox = new JComboBox(
        new Object[]{"Orthogonal Layout", "Hierarchic Layout", "Circular Layout", "Organic Layout"});
    comboBox.setMaximumSize(new Dimension(200, 100));
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (comboBox.getSelectedIndex()) {
          default:
          case 0:
            mode = MODE_ORTHOGONAL;
            break;
          case 1:
            mode = MODE_HIERARCHIC;
            break;
          case 2:
            mode = MODE_CIRCULAR;
            break;
          case 3:
            mode = MODE_ORGANIC;
            break;
        }        
      }
    });
    bar.add(comboBox);
    useInterEdgeRouter = true;
    final JCheckBox checkBox = new JCheckBox("Route Inter-Edges", useInterEdgeRouter);
    checkBox.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        useInterEdgeRouter = checkBox.isSelected();
      }
    });
    bar.add(checkBox);
    bar.addSeparator();
    bar.add(new LayoutAction());
    return bar;
  }

  private void loadGraph() {
    loadGraph("resource/recursive.graphml");
  }

  /** Register key bindings for our custom actions. */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.CLOSE_GROUPS, new MyCloseGroupsAction());
    actionMap.put(Graph2DViewActions.OPEN_FOLDERS, new MyOpenFoldersAction());
  }

  //action performs common behavior and applies a layout afterwards
  class MyCloseGroupsAction extends Graph2DViewActions.CloseGroupsAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  //action performs common behavior and applies a layout afterwards
  class MyOpenFoldersAction extends Graph2DViewActions.OpenFoldersAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  /** Layout action that configures and launches a layout algorithm. */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Auto-Layout Graph");
    }

    public void actionPerformed(ActionEvent e) {
      doLayout();
    }
  }

  private void doLayout() {
    Graph2D graph = view.getGraph2D();

    if (mode == MODE_ORTHOGONAL) {
      doOrthogonalLayout(graph);
    } else if (mode == MODE_HIERARCHIC) {
      doHierarchicLayout(graph);
    } else if (mode == MODE_ORGANIC) {
      doOrganicLayout(graph);
    } else {
      doCircularLayout(graph);
    }

    view.updateView();
    view.fitContent();
  }

  /** Configures and invokes the orthogonal layout algorithm */
  void doOrthogonalLayout(Graph2D graph) {
    final OrthogonalGroupLayouter ogl = new OrthogonalGroupLayouter();

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return ogl;
      }
    });

    //do recursive layout
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(ogl) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          DataProvider selectedEdges = graph.getDataProvider(Layouter.SELECTED_EDGES); //backup selected edges

          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for (EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            edge2IsInterEdge.setBool(ec.edge(), true);
          }
          graph.addDataProvider(Layouter.SELECTED_EDGES, edge2IsInterEdge);

          //route inter-edges
          OrthogonalEdgeRouter oer = createOrthogonalEdgeRouter();
          new GroupNodeRouterStage(oer).doLayout(graph);

          //restore originally selected edges
          if (selectedEdges != null) {
            graph.addDataProvider(Layouter.SELECTED_EDGES, selectedEdges);
          } else {
            graph.removeDataProvider(Layouter.SELECTED_EDGES);
          }
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl);

    //dispose
    graph.removeDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY);
  }

  private ParallelEdgeLayouter createParallelEdgeLayouter() {
    ParallelEdgeLayouter pel = new ParallelEdgeLayouter();
    pel.setLineDistance(10);
    pel.setJoinEndsEnabled(true);
    return pel;
  }

  /** Configures and invokes the organic layout algorithm */
  void doOrganicLayout(Graph2D graph) {
    final SmartOrganicLayouter sol = new SmartOrganicLayouter();
    sol.setMinimalNodeDistance(30);
    sol.setParallelEdgeLayouter(createParallelEdgeLayouter());

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return sol;
      }
    });

    //do recursive layout
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(sol) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          //reset paths of inter-edges
          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for (EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            Edge e = ec.edge();
            edge2IsInterEdge.setBool(e, true);
            LayoutTool.resetPath(graph, e);
          }

          //layout parallel edges
          graph.addDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY, edge2IsInterEdge);
          createParallelEdgeLayouter().doLayout(graph);
          graph.removeDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY);
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl);

    //dispose
    graph.removeDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY);
  }

  /** Configures and invokes the circular layout algorithm */
  void doCircularLayout(Graph2D graph) {
    final CircularLayouter cl = new CircularLayouter();    
    cl.setParallelEdgeLayouter(createParallelEdgeLayouter());

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return cl;
      }
    });

    //do recursive layout
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(cl) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          //reset paths of inter-edges
          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for (EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            Edge e = ec.edge();
            edge2IsInterEdge.setBool(e, true);
            LayoutTool.resetPath(graph, e);
          }

          //layout parallel edges
          graph.addDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY, edge2IsInterEdge);
          createParallelEdgeLayouter().doLayout(graph);
          graph.removeDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY);
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl);

    //dispose
    graph.removeDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY);
  }

  /** Configures and invokes the hierarchic layout algorithm */
  void doHierarchicLayout(Graph2D graph) {
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setOrthogonallyRouted(true);

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return ihl;
      }
    });

    //configure recursive layout
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(ihl) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          DataProvider selectedEdges = graph.getDataProvider(Layouter.SELECTED_EDGES); //backup selected edges

          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for(EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            edge2IsInterEdge.setBool(ec.edge(), true);
          }
          graph.addDataProvider(Layouter.SELECTED_EDGES, edge2IsInterEdge);

          //route inter-edges
          OrthogonalEdgeRouter oer = createOrthogonalEdgeRouter();
          oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_VERTICAL);
          new GroupNodeRouterStage(oer).doLayout(graph);

          //restore originally selected edges
          if (selectedEdges != null) {
            graph.addDataProvider(Layouter.SELECTED_EDGES, selectedEdges);
          } else {
            graph.removeDataProvider(Layouter.SELECTED_EDGES);
          }
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };
    rgl.setAutoAssignPortCandidatesEnabled(true);
    rgl.setConsiderSketchEnabled(true);
    new Graph2DLayoutExecutor().doLayout(graph, rgl); //do layout

    //dispose
    graph.removeDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY);
  }

  private OrthogonalEdgeRouter createOrthogonalEdgeRouter() {
    OrthogonalEdgeRouter oer = new OrthogonalEdgeRouter();
    oer.setCrossingCost(2);
    oer.setLocalCrossingMinimizationEnabled(true);
    oer.setReroutingEnabled(true);
    oer.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
    return oer;
  }

  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new RecursiveLayoutDemo("resource/recursivelayouthelp.html")).start();
      }
    });
  }
}
