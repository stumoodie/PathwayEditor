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
package demo.layout.router;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.PortConstraintConfigurator;
import y.layout.PortConstraintKeys;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.module.ChannelEdgeRouterModule;
import y.module.OrthogonalEdgeRouterModule;
import y.module.PortConstraintModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BendList;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.PolyLineEdgeRealizer;
import y.view.PortAssignmentMoveSelectionMode;
import y.view.Selections;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A demo that shows some of the capabilities of the yFiles Orthogonal Edge Router implementations.
 * <br> The following aspects of using
 * the edge routers are demonstrated. <ol> <li>How to use OrthogonalEdgeRouterModule or
 * ChannelEdgeRouterModule as a convenient means to launch and
 * configure the edge routers.</li> <li>How to modify the yFiles EditMode in order to trigger the orthogonal edge router
 * whenever <ul> <li>new edges get created</li> <li>nodes get resized</li> <li>selected nodes will be moved</li>
 * </ul></li> <li>How to specify port constraints for the edge router. With the help of port constraints it is possible
 * to tell the orthogonal edge router on which side of a node or on which exact coordinate a start or endpoint of an
 * edge should connect to a node.</li> </ol>
 * <br/>
 * Usage: Create nodes. Create edges crossing other nodes. The edges will be routed immediately.
 * To reroute all edges use the toolbar button "Route Edges".
 */
public class EdgeRouterDemo extends DemoBase {
  RouterStrategy strategy;
  PortAssignmentMoveSelectionMode paMode;

  // two available strategies
  private ChannelEdgeRouterStrategy channelEdgeRouterStrategy = new ChannelEdgeRouterStrategy();
  private OrthogonalEdgeRouterStrategy orthogonalEdgeRouterStrategy = new OrthogonalEdgeRouterStrategy();

  public EdgeRouterDemo() {
    strategy = orthogonalEdgeRouterStrategy;

    PolyLineEdgeRealizer er = (PolyLineEdgeRealizer) view.getGraph2D().getDefaultEdgeRealizer();
    er.setSmoothedBends(true);
    Graph2D graph = view.getGraph2D();
    EdgeMap sourcePortMap = graph.createEdgeMap();
    EdgeMap targetPortMap = graph.createEdgeMap();
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);
    paMode.setSpc(sourcePortMap);
    paMode.setTpc(targetPortMap);
    
    createInitialGraph();
  }

  protected void createInitialGraph() {
    Graph2D graph = view.getGraph2D();
    graph.createEdge(graph.createNode(100,100,"1"), graph.createEdge(graph.createNode(200,200,"2"), graph.createNode(300,100,"3")).source());
  }

  /** Returns ViewActionDemo toolbar plus actions to trigger some layout algorithms */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    
    final JComboBox comboBox = new JComboBox(new Object[]{"Orthogonal Edge Router", "Channel Edge Router"});
    
    comboBox.setMaximumSize(new Dimension(200, 100));
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (comboBox.getSelectedIndex()) {
          default:
          case 0:
            strategy = orthogonalEdgeRouterStrategy;
            break;
          case 1:
            strategy = channelEdgeRouterStrategy;
            break;
        }
      }
    });
    toolBar.add(comboBox);
    
    toolBar.add(new OptionAction());
    toolBar.add(new ConfigurePortConstraints());
    toolBar.add(new LayoutAction());
    
    return toolBar;
  }


  /** Provides configuration options  for the edge router */
  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Router Options...");
    }

    public void actionPerformed(ActionEvent e) {
      //display the option handler
      OptionHandler op = strategy.getModule().getOptionHandler();
      if (op != null) {
        op.showEditor();
      }
    }
  }

  /** Launches the Orthogonal Edge Router */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Route Edges");
    }

    public void actionPerformed(ActionEvent e) {
      //update preferredEdgeLengthData before launching the module
      Graph2D graph = view.getGraph2D();

      //start the module
      strategy.getModule().start(graph);
    }
  }

  /**
   * Configuration Utility for Port Constraints.  With the help of port constraints it is possible to tell the
   * orthogonal edge router on which side of a node or on which exact coordinate a start or endpoint of an edge should
   * connect to a node.
   */
  class ConfigurePortConstraints extends AbstractAction {
    YModule module;

    ConfigurePortConstraints() {
      super("Port Constraints...");
      module = new PortConstraintModule();
    }

    public void actionPerformed(ActionEvent e) {
      //display the option handler
      OptionHandler op = module.getOptionHandler();
      if (op != null) {
        if (!op.showEditor()) {
          return;
        }
      }
      module.start(view.getGraph2D());
    }
  }

  /**
   * Adds a specially configured EditMode that will automatically route all newly created edges orthogonally. The
   * orthogonal edge router will also be activated on some edges, when nodes get resized or a node selection gets
   * moved.
   */
  protected void registerViewModes() {
    EditMode mode = new EditMode();
    mode.setMoveSelectionMode(paMode = new MyMoveSelectionMode());
    mode.setCreateEdgeMode(new MyCreateEdgeMode());
    mode.setHotSpotMode(new MyHotSpotMode());
    view.addViewMode(mode);
  }

  /** A special mode for creating edges. */
  class MyCreateEdgeMode extends CreateEdgeMode {
    MyCreateEdgeMode() {
      super();
      allowSelfloopCreation(false);
    }

    protected void edgeCreated(final Edge e) {
      final Graph2D graph = view.getGraph2D();

      strategy.routeNewEdge(e);


      graph.updateViews();
    }
  }


  /** A special mode for resizing nodes. */
  class MyHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);

      final Graph2D graph = view.getGraph2D();

      DataProvider selectedNodes = Selections.createSelectionDataProvider(graph);
      strategy.rerouteAdjacentEdges(selectedNodes, graph);
      graph.updateViews();
    }
  }

  /** A special mode for moving a selection of the graph. */
  class MyMoveSelectionMode extends PortAssignmentMoveSelectionMode {

    MyMoveSelectionMode() {
      super(null, null);
    }

    private boolean routeEdgesOnMove = true;

    protected BendList getBendsToBeMoved() {
      BendList bends = super.getBendsToBeMoved();

      //add all bends from edges, whose source and target nodes are selected, since they will not be routed. 
      for (NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        for(EdgeCursor edgeCursor = node.outEdges(); edgeCursor.ok(); edgeCursor.next()) {
          Edge edge = edgeCursor.edge();
          if(getGraph2D().isSelected(edge.target())){
            for(BendCursor bendCursor = getGraph2D().getRealizer(edge).bends(); bendCursor.ok(); bendCursor.next()){
              Bend bend = bendCursor.bend();
              bends.add(bend);
            }
          }
        }
      }
      return bends;
    }

    protected void selectionOnMove(double dx, double dy, double x, double y) {
      super.selectionOnMove(dx, dy, x, y);
      if (routeEdgesOnMove) {
        routeEdgesToSelection();
      }
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      super.selectionMovedAction(dx, dy, x, y);
      routeEdgesToSelection();
    }

    void routeEdgesToSelection() {
      final Graph2D graph = view.getGraph2D();
      if (graph.selectedNodes().ok()) {
        strategy.routeEdgesToSelection(graph);
        graph.updateViews();
      }
    }
  }

  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new EdgeRouterDemo()).start("Orthogonal Edge Router Demo");
      }
    });
  }

  abstract static class RouterStrategy {
    abstract YModule getModule();

    abstract void routeNewEdge(Edge e);

    abstract void rerouteAdjacentEdges(DataProvider selectedNodes, LayoutGraph graph);

    abstract void routeEdgesToSelection(Graph2D graph);

    abstract void route(Layouter router, LayoutGraph graph);

    protected void routeNewEdge(Layouter router, final Edge e, Graph2D graph) {
      EdgeMap spc = (EdgeMap) graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      EdgeMap tpc = (EdgeMap) graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);

      PortConstraintConfigurator pcc = new PortConstraintConfigurator();
      if (spc != null && tpc != null) {
        spc.set(e, pcc.createPortConstraintFromSketch(graph, e, true, false));
        tpc.set(e, pcc.createPortConstraintFromSketch(graph, e, false, false));
        route(router, graph);
        spc.set(e, null);
        tpc.set(e, null);
      } else {
        route(router, graph);
      }
    }

    protected void routeEdgesToSelection(final Graph2D graph, Layouter router, Object affectedEdgesKey) {
      graph.addDataProvider(affectedEdgesKey, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return graph.isSelected(((Edge) dataHolder).source()) ^ graph.isSelected(((Edge) dataHolder).target());
        }
      });
      route(router, graph);
      graph.removeDataProvider(affectedEdgesKey);
    }

    protected void routeNewEdge(final Edge e, Graph2D graph, Layouter router, Object selectedEdgesKey) {
      DataProvider activeEdges = new DataProviderAdapter() {
        public boolean getBool(Object o) {
          return e == o;
        }
      };
      graph.addDataProvider(selectedEdgesKey, activeEdges);
      routeNewEdge(router, e, graph);
      graph.removeDataProvider(selectedEdgesKey);
    }
  }

  static class OrthogonalEdgeRouterStrategy extends RouterStrategy {
    private OrthogonalEdgeRouterModule module = new OrthogonalEdgeRouterModule();

    public YModule getModule() {
      return module;
    }

    public void routeNewEdge(final Edge e) {
      Graph2D graph = (Graph2D) e.getGraph();
      OrthogonalEdgeRouter router = new OrthogonalEdgeRouter();
      module.configure(router);
      router.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
      routeNewEdge(e, graph, router, Layouter.SELECTED_EDGES);
    }

    public void rerouteAdjacentEdges(DataProvider selectedNodes, LayoutGraph graph) {
      OrthogonalEdgeRouter router = new OrthogonalEdgeRouter();
      module.configure(router);
      router.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
      graph.addDataProvider(Layouter.SELECTED_NODES, selectedNodes);
      this.route(router, graph);
      graph.removeDataProvider(Layouter.SELECTED_NODES);
    }

    public void routeEdgesToSelection(final Graph2D graph) {
      OrthogonalEdgeRouter router = new OrthogonalEdgeRouter();
      module.configure(router);
      router.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
      routeEdgesToSelection(graph, router, Layouter.SELECTED_EDGES);
    }

    void route(Layouter router, LayoutGraph graph) {
      router.doLayout(graph);
    }
  }

  static class ChannelEdgeRouterStrategy extends RouterStrategy {
    private ChannelEdgeRouterModule module = new ChannelEdgeRouterModule();

    public YModule getModule() {
      return module;
    }

    public void routeNewEdge(final Edge e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      ChannelEdgeRouter router = new ChannelEdgeRouter();
      module.configure(router);
      routeNewEdge(e, graph, router, ChannelEdgeRouter.AFFECTED_EDGES);
    }

    public void rerouteAdjacentEdges(final DataProvider selectedNodes, LayoutGraph graph) {
      ChannelEdgeRouter router = new ChannelEdgeRouter();
      module.configure(router);
      graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedNodes.getBool((((Edge) dataHolder).source())) || selectedNodes.getBool(((Edge) dataHolder).target());
        }
      });
      this.route(router, graph);
      graph.removeDataProvider(ChannelEdgeRouter.AFFECTED_EDGES);
    }

    public void routeEdgesToSelection(final Graph2D graph) {
      ChannelEdgeRouter router = new ChannelEdgeRouter();
      module.configure(router);
      routeEdgesToSelection(graph, router, ChannelEdgeRouter.AFFECTED_EDGES);
    }

    void route(Layouter router, LayoutGraph graph) {
      router.doLayout(graph);
    }
  }
}


      
