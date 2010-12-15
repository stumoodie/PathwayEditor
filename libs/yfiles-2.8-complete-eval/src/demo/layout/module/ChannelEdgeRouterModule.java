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

import y.base.Edge;
import y.layout.Layouter;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.OrthogonalPatternEdgeRouter;
import y.layout.router.OrthogonalSegmentDistributionStage;
import y.option.ConstraintManager;
import y.option.DoubleOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Graph2D;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.router.ChannelEdgeRouter}.
 *
 */
public class ChannelEdgeRouterModule extends LayoutModule {
  private static final String NAME = "CHANNEL_EDGE_ROUTER";

  private ChannelEdgeRouter router;
  private static final String PATHFINDER = "PATHFINDER";

  private static final String SCOPE = "SCOPE";
  private static final String SCOPE_AT_SELECTED_NODES = "SCOPE_AT_SELECTED_NODES";
  private static final String SCOPE_SELECTED_EDGES = "SCOPE_SELECTED_EDGES";
  private static final String LAYOUT_OPTIONS = "LAYOUT_OPTIONS";
  private static final String SCOPE_ALL_EDGES = "SCOPE_ALL_EDGES";
  private static final String COST = "COST";
  private static final String EDGE_CROSSING_COST = "EDGE_CROSSING_COST";
  private static final String NODE_CROSSING_COST = "NODE_CROSSING_COST";
  private static final String BEND_COST = "BEND_COST_FACTOR";
  private static final String MINIMUM_DISTANCE = "MINIMUM_DISTANCE";
  private static final String ACTIVATE_GRID_ROUTING = "ACTIVATE_GRID_ROUTING";
  private static final String GRID_SPACING = "GRID_SPACING";
  private static final String ORTHOGONAL_PATTERN_PATH_FINDER = "ORTHOGONAL_PATTERN_PATH_FINDER";
  private static final String ORTHOGONAL_SHORTESTPATH_PATH_FINDER = "ORTHOGONAL_SHORTESTPATH_PATH_FINDER";


  /**
   * Creates a new Instance of this Module.
   */
  public ChannelEdgeRouterModule() {
    super(NAME, "yFiles Layout Team", "Routes edges orthogonally.");
    setPortIntersectionCalculatorEnabled(true);
  }


  protected void init() {
    instantiateRouter();
    configure(router);

    final Graph2D graph = getGraph2D();
    OptionHandler oh = getOptionHandler();

    //set affected edges
    if (oh.get(SCOPE).equals(SCOPE_ALL_EDGES)) {
      graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return true;
        }
      });
    } else if (oh.get(SCOPE).equals(SCOPE_SELECTED_EDGES)) {
      graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return graph.isSelected((Edge) dataHolder);
        }
      });
    } else {
      graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return graph.isSelected(((Edge) dataHolder).source()) || graph.isSelected(((Edge) dataHolder).target());
        }
      });
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //// Own stuff
  //////////////////////////////////////////////////////////////////////////////
  private void instantiateRouter() {
    if (router != null) {
      return;
    }
    router = new ChannelEdgeRouter();
  }

  /**
   * Configures an instance of ChannelEdgeRouter. The values provided by this module's option handler are being used for
   * this purpose.
   *
   * @param layouter the layouter to be configured.
   */
  public void configure(Layouter layouter) {
    if (layouter instanceof ChannelEdgeRouter) {
      ChannelEdgeRouter edgeRouter = (ChannelEdgeRouter) layouter;
      OptionHandler oh = getOptionHandler();


      Layouter pathFinder;
      if (oh.get(PATHFINDER).equals(ORTHOGONAL_PATTERN_PATH_FINDER)) {
        OrthogonalPatternEdgeRouter orthogonalPatternEdgeRouter = new OrthogonalPatternEdgeRouter();
        orthogonalPatternEdgeRouter.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
        orthogonalPatternEdgeRouter.setMinimumDistance(oh.getDouble(MINIMUM_DISTANCE));

        orthogonalPatternEdgeRouter.setGridRoutingEnabled(oh.getBool(ACTIVATE_GRID_ROUTING));
        orthogonalPatternEdgeRouter.setGridWidth(oh.getDouble(GRID_SPACING));

        orthogonalPatternEdgeRouter.setBendCost(oh.getDouble(BEND_COST));
        orthogonalPatternEdgeRouter.setEdgeCrossingCost(oh.getDouble(EDGE_CROSSING_COST));
        orthogonalPatternEdgeRouter.setNodeCrossingCost(oh.getDouble(NODE_CROSSING_COST));

        //disable edge overlap costs when Edge distribution will run afterwards anyway
        orthogonalPatternEdgeRouter.setEdgeOverlapCost(0.0);
        pathFinder = orthogonalPatternEdgeRouter;
      } else {
        ChannelEdgeRouter.OrthogonalShortestPathPathFinder orthogonalShortestPathPathFinder = new ChannelEdgeRouter.OrthogonalShortestPathPathFinder();
        orthogonalShortestPathPathFinder.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
        orthogonalShortestPathPathFinder.setMinimumDistance((int) oh.getDouble(MINIMUM_DISTANCE));

        orthogonalShortestPathPathFinder.setGridRoutingEnabled(oh.getBool(ACTIVATE_GRID_ROUTING));
        orthogonalShortestPathPathFinder.setGridSpacing((int) oh.getDouble(GRID_SPACING));

        orthogonalShortestPathPathFinder.setCrossingCost(oh.getDouble(EDGE_CROSSING_COST));
        pathFinder = orthogonalShortestPathPathFinder;
      }
      edgeRouter.setPathFinderStrategy(pathFinder);

      OrthogonalSegmentDistributionStage segmentDistributionStage = new OrthogonalSegmentDistributionStage();
      segmentDistributionStage.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
      segmentDistributionStage.setPreferredDistance(oh.getDouble(MINIMUM_DISTANCE));
      segmentDistributionStage.setGridEnabled(oh.getBool(ACTIVATE_GRID_ROUTING));
      segmentDistributionStage.setGridWidth(oh.getDouble(GRID_SPACING));

      edgeRouter.setEdgeDistributionStrategy(segmentDistributionStage);
    } else {
      throw new IllegalArgumentException("argument must be of type y.layout.router.ChannelEdgeRouter");
    }
  }

  /**
   * Initializes the option handler of this module with the properties of the given router.
   *
   * @param layouter an instance of {@link y.layout.router.ChannelEdgeRouter}.
   */
  public void initOptionHandler(Layouter layouter) {
    OptionHandler oh = getOptionHandler();
    initOptionHandler(oh, layouter);
  }

  void initOptionHandler(OptionHandler oh, Layouter layouter) {
    oh.clear();
    if (layouter == null || ! (layouter instanceof ChannelEdgeRouter) ) {
      layouter = new ChannelEdgeRouter();
    }
    ChannelEdgeRouter cer = (ChannelEdgeRouter) layouter;

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, LAYOUT_OPTIONS);
    String[] pathFinderEnum = {ORTHOGONAL_PATTERN_PATH_FINDER, ORTHOGONAL_SHORTESTPATH_PATH_FINDER};

    if(cer.getPathFinderStrategy() instanceof OrthogonalPatternEdgeRouter){
      OrthogonalPatternEdgeRouter oper = (OrthogonalPatternEdgeRouter) cer.getPathFinderStrategy();
      og.addItem(oh.addEnum(PATHFINDER, pathFinderEnum, 0));

      String[] affectedEnum = {SCOPE_ALL_EDGES, SCOPE_SELECTED_EDGES, SCOPE_AT_SELECTED_NODES};
      og.addItem(oh.addEnum(SCOPE, affectedEnum, 0));

      og.addItem(oh.addDouble(MINIMUM_DISTANCE, oper.getMinimumDistance()));
      og.addItem(oh.addBool(ACTIVATE_GRID_ROUTING, oper.isGridRoutingEnabled()));
      og.addItem(oh.addDouble(GRID_SPACING, oper.getGridWidth()));
      oh.getItem(GRID_SPACING)
        .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(2.0));

      ConstraintManager cm = new ConstraintManager(oh);
      cm.setEnabledOnValueEquals(ACTIVATE_GRID_ROUTING, Boolean.TRUE, GRID_SPACING);

      og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, COST);
      og.addItem(oh.addDouble(BEND_COST, oper.getBendCost()));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, BEND_COST);
      og.addItem(oh.addDouble(EDGE_CROSSING_COST, oper.getEdgeCrossingCost()));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, EDGE_CROSSING_COST);
      og.addItem(oh.addDouble(NODE_CROSSING_COST, oper.getNodeCrossingCost()));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, NODE_CROSSING_COST);
    } else if(cer.getPathFinderStrategy() instanceof ChannelEdgeRouter.OrthogonalShortestPathPathFinder){
      ChannelEdgeRouter.OrthogonalShortestPathPathFinder osppf =
          (ChannelEdgeRouter.OrthogonalShortestPathPathFinder) cer.getPathFinderStrategy();
      og.addItem(oh.addEnum(PATHFINDER, pathFinderEnum, 1));

      String[] affectedEnum = {SCOPE_ALL_EDGES, SCOPE_SELECTED_EDGES, SCOPE_AT_SELECTED_NODES};
      og.addItem(oh.addEnum(SCOPE, affectedEnum, 0));

      og.addItem(oh.addDouble(MINIMUM_DISTANCE, osppf.getMinimumDistance()));
      oh.getItem(MINIMUM_DISTANCE)
        .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(4.0));
      og.addItem(oh.addBool(ACTIVATE_GRID_ROUTING, osppf.isGridRoutingEnabled()));
      og.addItem(oh.addDouble(GRID_SPACING, osppf.getGridSpacing()));
      oh.getItem(GRID_SPACING)
        .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(2.0));

      ConstraintManager cm = new ConstraintManager(oh);
      cm.setEnabledOnValueEquals(ACTIVATE_GRID_ROUTING, Boolean.TRUE, GRID_SPACING);

      og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, COST);
      og.addItem(oh.addDouble(BEND_COST, 1));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, BEND_COST);
      og.addItem(oh.addDouble(EDGE_CROSSING_COST, 5));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, EDGE_CROSSING_COST);
      og.addItem(oh.addDouble(NODE_CROSSING_COST, 50));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, NODE_CROSSING_COST);
    } else { //use other settings
      og.addItem(oh.addEnum(PATHFINDER, pathFinderEnum, 0));

      String[] affectedEnum = {SCOPE_ALL_EDGES, SCOPE_SELECTED_EDGES, SCOPE_AT_SELECTED_NODES};
      og.addItem(oh.addEnum(SCOPE, affectedEnum, 0));

      og.addItem(oh.addDouble(MINIMUM_DISTANCE, 10.0));
      og.addItem(oh.addBool(ACTIVATE_GRID_ROUTING, true));
      og.addItem(oh.addDouble(GRID_SPACING, 20.0));
      ConstraintManager cm = new ConstraintManager(oh);
      cm.setEnabledOnValueEquals(ACTIVATE_GRID_ROUTING, Boolean.TRUE, GRID_SPACING);

      og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, COST);
      og.addItem(oh.addDouble(BEND_COST, 1.0));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, BEND_COST);
      og.addItem(oh.addDouble(EDGE_CROSSING_COST, 5.0));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, EDGE_CROSSING_COST);
      og.addItem(oh.addDouble(NODE_CROSSING_COST, 50.0));
      cm.setEnabledOnValueEquals(PATHFINDER, ORTHOGONAL_PATTERN_PATH_FINDER, NODE_CROSSING_COST);
    }
  }

  protected void dispose() {
    router = null;
  }

  /**
   * Creates and initializes the Option Handler so that a convenient way for manipulating the parameters is at the
   * user's hand.
   */
  protected OptionHandler createOptionHandler() {
    OptionHandler oh = new OptionHandler(getModuleName());
    initOptionHandler(oh, null);
    return oh;
  }

  protected void mainrun() {
    launchLayouter(router);
  }
}
