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
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.layout.LayoutGraph;
import y.layout.SequentialLayouter;
import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.layout.partial.PartialLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.Layouter;
import y.layout.LayoutTool;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Selections;
import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.Edge;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.partial.PartialLayouter}.
 *
 */
public class PartialLayoutModule extends LayoutModule {
  private static final String PARTIAL = "PARTIAL";
  private static final String GENERAL = "GENERAL";
  public static final String SUBGRAPH_LAYOUTER = "SUBGRAPH_LAYOUTER";
  public static final String SUBGRAPH_LAYOUTER_IHL = "SUBGRAPH_LAYOUTER_IHL";
  public static final String SUBGRAPH_LAYOUTER_ORGANIC = "SUBGRAPH_LAYOUTER_ORGANIC";
  public static final String SUBGRAPH_LAYOUTER_CIRCULAR = "SUBGRAPH_LAYOUTER_CIRCULAR";
  public static final String SUBGRAPH_LAYOUTER_ORTHOGONAL = "SUBGRAPH_LAYOUTER_ORTHOGONAL";
  private static final String SUBGRAPH_LAYOUTER_NO_LAYOUT = "SUBGRAPH_LAYOUTER_NO_LAYOUT";
  private static final String MIN_NODE_DIST = "MIN_NODE_DIST";

  public static String SUBGRAPH_POSITION_STRATEGY = "SUBGRAPH_POSITION_STRATEGY";
  public static final String SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER = "SUBGRAPH_POSITION_STRATEGY_BARYCENTER";
  public static final String SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH = "SUBGRAPH_POSITION_STRATEGY_FROM_SKETCH";

  public static final String ROUTING_TO_SUBGRAPH = "ROUTING_TO_SUBGRAPH";
  public static final String ROUTING_TO_SUBGRAPH_STRAIGHT_LINE = "ROUTING_TO_SUBGRAPH_STRAIGHT_LINE";
  public static final String ROUTING_TO_SUBGRAPH_POLYLINE = "ROUTING_TO_SUBGRAPH_POLYLINE";
  public static final String ROUTING_TO_SUBGRAPH_ORTHOGONALLY = "ROUTING_TO_SUBGRAPH_ORTHOGONALLY";
  public static final String ROUTING_TO_SUBGRAPH_ORGANIC = "ROUTING_TO_SUBGRAPH_ORGANIC";
  public static final String ROUTING_TO_SUBGRAPH_AUTO = "ROUTING_TO_SUBGRAPH_AUTO";

  public static final String MODE_COMPONENT_ASIGNMENT = "MODE_COMPONENT_ASIGNMENT";
  public static final String MODE_COMPONENT_CLUSTERING = "MODE_COMPONENT_CLUSTERING";
  public static final String MODE_COMPONENT_CONNECTED = "MODE_COMPONENT_CONNECTED";
  public static final String MODE_COMPONENT_CUSTOMIZED = "MODE_COMPONENT_CUSTOMIZED";
  public static final String MODE_COMPONENT_SINGLE = "MODE_COMPONENT_SINGLE";

  public static final String HIERARCHY_REORGANIZATION = "HIERARCHY_REORGANIZATION";

  public static final String ORIENTATION_MAIN_GRAPH = "ORIENTATION_MAIN_GRAPH";
  public static final String ORIENTATION_MAIN_GRAPH_NONE = "ORIENTATION_MAIN_GRAPH_NONE";
  public static final String ORIENTATION_MAIN_GRAPH_AUTO_DETECT = "ORIENTATION_MAIN_GRAPH_AUTO_DETECT";
  public static final String ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN = "ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN";
  public static final String ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP = "ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP";
  public static final String ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT = "ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT";
  public static final String ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT = "ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT";

  public static final String CONSIDER_SNAPLINES = "CONSIDER_SNAPLINES";

  public static final String SCOPE = "SCOPE";
  public static final String SCOPE_NODES_AND_EDGES = "SCOPE_NODES_AND_EDGES";
  public static final String SCOPE_EDGES = "SCOPE_EDGES";

  private static final String[] completeLayoutOrRoutingOnlyEnum = {
      SCOPE_NODES_AND_EDGES, SCOPE_EDGES
  };
  private static final String[] subgraphLayouterEnum = {SUBGRAPH_LAYOUTER_IHL, SUBGRAPH_LAYOUTER_ORGANIC,
      SUBGRAPH_LAYOUTER_CIRCULAR, SUBGRAPH_LAYOUTER_ORTHOGONAL,
      SUBGRAPH_LAYOUTER_NO_LAYOUT};
  private static final String[] subgraphPositionStrategyEnum = {SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER,
      SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH};
  private static final String[] routerToSubgraphEnum = {ROUTING_TO_SUBGRAPH_AUTO, ROUTING_TO_SUBGRAPH_STRAIGHT_LINE,
      ROUTING_TO_SUBGRAPH_POLYLINE, ROUTING_TO_SUBGRAPH_ORTHOGONALLY
      , ROUTING_TO_SUBGRAPH_ORGANIC};
  private static final String[] modeComponentAsignmentEnum = {MODE_COMPONENT_CONNECTED,
      MODE_COMPONENT_SINGLE, MODE_COMPONENT_CUSTOMIZED};
  private static final String[] orientationMainGraphEnum = {ORIENTATION_MAIN_GRAPH_AUTO_DETECT,
      ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN, ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP, ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT,
      ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT, ORIENTATION_MAIN_GRAPH_NONE};


  private static final byte EDGE_ROUTING_ORTHOGONAL = 0;
  private static final byte EDGE_ROUTING_STRAIGHTLINE = 1;
  private static final byte EDGE_ROUTING_AUTOMATIC = 2;
  private static final byte EDGE_ROUTING_ORGANIC = 3;
  private static final byte EDGE_ROUTING_POLYLINE = 4;


  private Graph2D graph;


  public PartialLayoutModule() {
    super(PARTIAL, "yFiles Layout Team", "An algorithm that lays out only a subset of a graph.");
  }

  public OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());
    ConstraintManager cm = new ConstraintManager(op);
    op.useSection(GENERAL);
    op.addEnum(SCOPE, completeLayoutOrRoutingOnlyEnum, 0);
    op.addEnum(ROUTING_TO_SUBGRAPH, routerToSubgraphEnum, 0);
    op.addEnum(MODE_COMPONENT_ASIGNMENT, modeComponentAsignmentEnum, 0);
    op.addEnum(SUBGRAPH_LAYOUTER, subgraphLayouterEnum, 0);
    cm.setEnabledOnValueEquals(MODE_COMPONENT_ASIGNMENT, MODE_COMPONENT_SINGLE, SUBGRAPH_LAYOUTER, true);
    op.addEnum(SUBGRAPH_POSITION_STRATEGY, subgraphPositionStrategyEnum, 0);
    op.addInt(MIN_NODE_DIST, 30, 1, 100);
    final EnumOptionItem orientatinOI = op.addEnum(ORIENTATION_MAIN_GRAPH, orientationMainGraphEnum, 0);
    op.addBool(CONSIDER_SNAPLINES, true);

    cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, MODE_COMPONENT_ASIGNMENT);
    cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, SUBGRAPH_LAYOUTER);
    cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, SUBGRAPH_POSITION_STRATEGY);
    cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, MIN_NODE_DIST);
    //cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, ORIENTATION_MAIN_GRAPH);
    final ConstraintManager.Condition orientationItemCondition = cm.createConditionValueEquals(SCOPE,
        SCOPE_NODES_AND_EDGES).or(
        cm.createConditionValueEquals(ROUTING_TO_SUBGRAPH, ROUTING_TO_SUBGRAPH_ORTHOGONALLY)).or(
        cm.createConditionValueEquals(ROUTING_TO_SUBGRAPH, ROUTING_TO_SUBGRAPH_AUTO));
    cm.setEnabledOnCondition(orientationItemCondition, orientatinOI);
    cm.setEnabledOnValueEquals(SCOPE, SCOPE_NODES_AND_EDGES, CONSIDER_SNAPLINES);
    return op;
  }

  protected void mainrun() {
    OptionHandler op = getOptionHandler();
    graph = getGraph2D();

    //register dp for selected nodes/edges
    graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, Selections.createSelectionDataProvider(graph));
    graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, Selections.createSelectionDataProvider(graph));


    byte routingToSubGraph = routingToSubGraphAsByte(routerToSubgraphEnum[op.getEnum(ROUTING_TO_SUBGRAPH)]);
    DataProvider considerEdges = new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        Edge e = (Edge) dataHolder;
        DataProvider node2IsPartial = graph.getDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
        DataProvider edge2IsPartial = graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
        if ((node2IsPartial == null || (!node2IsPartial.getBool(e.source()) && !node2IsPartial.getBool(e.target())))
            && (edge2IsPartial == null || !edge2IsPartial.getBool(e))) {
          return true;
        } else {
          return false;
        }
      }
    };
    if (PartialLayouter.EDGE_ROUTING_STRATEGY_AUTOMATIC == routingToSubGraph) {
      //Try to determine the router automatically : only for orthogonal or not orthogonal
      if (LayoutTool.isUsingOrthogonalEdgeRoutes(graph, considerEdges)) {
        routingToSubGraph = PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL;
      } else {
        routingToSubGraph = PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE;
      }
    }

    final boolean subgraphPositioningEnabled =
        SCOPE_NODES_AND_EDGES.equals(completeLayoutOrRoutingOnlyEnum[op.getEnum(SCOPE)]);
    final byte subgraphPositioningStrategy = subgraphPositioningStrategyAsByte(
        subgraphPositionStrategyEnum[op.getEnum(SUBGRAPH_POSITION_STRATEGY)]);

    final byte componentAssignment = componentAssignmentAsByte(
        modeComponentAsignmentEnum[op.getEnum(MODE_COMPONENT_ASIGNMENT)]);

    //Determine the subgraph layouter
    final String subgraphLayouterString = subgraphLayouterEnum[op.getEnum(SUBGRAPH_LAYOUTER)];
    Layouter subgraphLayouter = null;
    if (PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE == componentAssignment) {
      //Trivial case. For single nodes components it should be no subgraph layouter
      subgraphLayouter = null;
    } else if (SUBGRAPH_LAYOUTER_IHL.equals(subgraphLayouterString)) {
      subgraphLayouter = new IncrementalHierarchicLayouter();
      if (PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL == routingToSubGraph) {
        ((IncrementalHierarchicLayouter) subgraphLayouter).setOrthogonallyRouted(true);
      } else {
        ((IncrementalHierarchicLayouter) subgraphLayouter).setOrthogonallyRouted(false);
      }
    } else if (SUBGRAPH_LAYOUTER_ORGANIC.equals(subgraphLayouterString)) {
      subgraphLayouter = new SmartOrganicLayouter();
      ((SmartOrganicLayouter) subgraphLayouter).setDeterministic(true);
    } else if (SUBGRAPH_LAYOUTER_CIRCULAR.equals(subgraphLayouterString)) {
      subgraphLayouter = new CircularLayouter();
    } else if (SUBGRAPH_LAYOUTER_ORTHOGONAL.equals(subgraphLayouterString)) {
      subgraphLayouter = new OrthogonalLayouter();
    } else if (SUBGRAPH_LAYOUTER_NO_LAYOUT.equals(subgraphLayouterString)) {
      subgraphLayouter = null;
    }


    final byte mainGraphOrientation = graphOrientationAsByte(
        orientationMainGraphEnum[op.getEnum(ORIENTATION_MAIN_GRAPH)]);


    //run the partial layouter
    if (graph.selectedNodes().size() > 0 || graph.selectedEdges().size() > 0) {
      final SequentialLayouter layouter = new SequentialLayouter();

      if (subgraphPositioningEnabled) {
        final PartialLayouter partialLayouter = new PartialLayouter(subgraphLayouter);
        partialLayouter.setPositioningStrategy(subgraphPositioningStrategy);
        partialLayouter.setEdgeRoutingStrategy(
            routingToSubGraph == EDGE_ROUTING_POLYLINE ? EDGE_ROUTING_STRAIGHTLINE : routingToSubGraph);
        partialLayouter.setComponentAssignmentStrategy(componentAssignment);
        partialLayouter.setLayoutOrientation(mainGraphOrientation);
        partialLayouter.setMinimalNodeDistance(op.getInt(MIN_NODE_DIST));
        partialLayouter.setConsiderNodeAlignment(op.getBool(CONSIDER_SNAPLINES));

        layouter.appendLayouter(partialLayouter);
      }

      Object selectedEdgesDpKey = "y.module.PartialLayoutModule.DUMMY_KEY";
      switch (routingToSubGraph) {
        case PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE:
          layouter.appendLayouter(new StraightLineRouter());
          selectedEdgesDpKey = Layouter.SELECTED_EDGES;
          break;
        case PartialLayouter.EDGE_ROUTING_STRATEGY_ORGANIC:
          layouter.appendLayouter(new OrganicEdgeRouter());
          selectedEdgesDpKey = OrganicEdgeRouter.ROUTE_EDGE_DPKEY;
          break;
        case PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL:
          if ((!SUBGRAPH_LAYOUTER_IHL.equals(subgraphLayouterString) &&
              !SUBGRAPH_LAYOUTER_ORTHOGONAL.equals(subgraphLayouterString)) ||
              !subgraphPositioningEnabled) {
            layouter.appendLayouter(configuredOrthogonalRouter(mainGraphOrientation, considerEdges));
            selectedEdgesDpKey = Layouter.SELECTED_EDGES;
          }
          break;
      }


      DataProvider oldSelectedEdges = graph.getDataProvider(selectedEdgesDpKey);
      graph.addDataProvider(selectedEdgesDpKey, graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY));

      getLayoutExecutor().setConfiguringTableNodeRealizers(true);
      boolean horizontalLayout = (mainGraphOrientation == PartialLayouter.ORIENTATION_LEFT_TO_RIGHT)
          || (mainGraphOrientation == PartialLayouter.ORIENTATION_RIGHT_TO_LEFT);
      getLayoutExecutor().getTableLayoutConfigurator().setHorizontalLayoutConfiguration(horizontalLayout);

      try {        
        launchLayouter(layouter);
      } finally {

        graph.removeDataProvider(selectedEdgesDpKey);
        if (oldSelectedEdges != null) {
          graph.addDataProvider(selectedEdgesDpKey, oldSelectedEdges);
        }        
      }
    }
  }

  private Layouter configuredOrthogonalRouter(final byte mainGraphOrientation, DataProvider considerEdges) {
    OrthogonalEdgeRouter oer = new OrthogonalEdgeRouter();
    oer.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
    oer.setCrossingCost(2.0);
    oer.setLocalCrossingMinimizationEnabled(true);
    oer.setMinimumDistanceToNode(5);
    oer.setMinimumDistance(5);
    oer.setReroutingEnabled(true);

    //Determine routing direction
    if (PartialLayouter.ORIENTATION_TOP_TO_BOTTOM == mainGraphOrientation ||
        PartialLayouter.ORIENTATION_BOTTOM_TO_TOP == mainGraphOrientation) {
      oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_VERTICAL);
      oer.setEnforceMonotonicPathRestrictions(true);
    } else if (PartialLayouter.ORIENTATION_LEFT_TO_RIGHT == mainGraphOrientation ||
        PartialLayouter.ORIENTATION_RIGHT_TO_LEFT == mainGraphOrientation) {
      oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_HORIZONTAL);
      oer.setEnforceMonotonicPathRestrictions(true);
    } else if (PartialLayouter.ORIENTATION_AUTO_DETECTION == mainGraphOrientation) {
      final byte edgeFlowDir = LayoutTool.determineEdgeFlowDirection(graph, considerEdges);
      if (edgeFlowDir == LayoutTool.FLOW_UP || edgeFlowDir == LayoutTool.FLOW_DOWN) {
        oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_VERTICAL);
        oer.setEnforceMonotonicPathRestrictions(true);
      } else if (edgeFlowDir == LayoutTool.FLOW_LEFT || edgeFlowDir == LayoutTool.FLOW_RIGHT) {
        oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_HORIZONTAL);
        oer.setEnforceMonotonicPathRestrictions(true);
      } else {
        oer.setEnforceMonotonicPathRestrictions(false);
        oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_NONE);
      }
    } else {
      oer.setEnforceMonotonicPathRestrictions(false);
      oer.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_NONE);
    }

    return oer;
  }


  /** Determines the routing of inter edges (main graph to subgraph). */
  private static byte routingToSubGraphAsByte(final String routingToSubGraphString) {
    if (ROUTING_TO_SUBGRAPH_ORTHOGONALLY.equals(routingToSubGraphString)) {
      return EDGE_ROUTING_ORTHOGONAL;
    } else if (ROUTING_TO_SUBGRAPH_STRAIGHT_LINE.equals(routingToSubGraphString)) {
      return EDGE_ROUTING_STRAIGHTLINE;
    } else if (ROUTING_TO_SUBGRAPH_AUTO.equals(routingToSubGraphString)) {
      return EDGE_ROUTING_AUTOMATIC;
    } else if (ROUTING_TO_SUBGRAPH_ORGANIC.equals(routingToSubGraphString)) {
      return EDGE_ROUTING_ORGANIC;
    } else if (ROUTING_TO_SUBGRAPH_POLYLINE.equals(routingToSubGraphString)) {
      return EDGE_ROUTING_POLYLINE;
    } else {
      return PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE;
    }
  }

  /** Determines the subgraph position strategy. */
  private static byte subgraphPositioningStrategyAsByte(final String subgraphPositioningStrategyString) {
    if (SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH.equals(subgraphPositioningStrategyString)) {
      return PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH;
    } else {
      return PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER;
    }
  }

  /** Determines component by: {Clustering, Connected graph, Particular}. */
  private static byte componentAssignmentAsByte(final String componentAssignmentString) {
    if (MODE_COMPONENT_SINGLE.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE;
    } else if (MODE_COMPONENT_CONNECTED.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED;
    } else if (MODE_COMPONENT_CLUSTERING.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CLUSTERING;
    } else {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CUSTOMIZED;
    }
  }

  /** Determines the main graph Orientation. */
  private static byte graphOrientationAsByte(final String graphOrientationString) {
    if (ORIENTATION_MAIN_GRAPH_AUTO_DETECT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_AUTO_DETECTION;
    } else if (ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_TOP_TO_BOTTOM;
    } else if (ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_BOTTOM_TO_TOP;
    } else if (ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_LEFT_TO_RIGHT;
    } else if (ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_RIGHT_TO_LEFT;
    } else if (ORIENTATION_MAIN_GRAPH_NONE.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_NONE;
    } else {
      return PartialLayouter.ORIENTATION_AUTO_DETECTION;
    }
  }


  private static final class StraightLineRouter implements Layouter {
    public boolean canLayout(final LayoutGraph graph) {
      return true;
    }

    public void doLayout(final LayoutGraph graph) {
      final DataProvider dp = graph.getDataProvider(Layouter.SELECTED_EDGES);
      if (dp == null) {
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          LayoutTool.resetPath(graph, ec.edge());
        }
      } else {
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          if (dp.getBool(ec.edge())) {
            LayoutTool.resetPath(graph, ec.edge());
          }
        }
      }
    }
  }
}
