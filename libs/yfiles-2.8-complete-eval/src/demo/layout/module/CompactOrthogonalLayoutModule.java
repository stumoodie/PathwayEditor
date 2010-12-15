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

import y.layout.ComponentLayouter;
import y.layout.Layouter;
import y.layout.PartitionLayouter;
import y.layout.grouping.GroupNodeHider;
import y.layout.orthogonal.CompactOrthogonalLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.OrthogonalPatternEdgeRouter;
import y.option.ConstraintManager;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;

import java.awt.Dimension;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.orthogonal.CompactOrthogonalLayouter}.
 *
 */
public class CompactOrthogonalLayoutModule extends LayoutModule {
  private static final String NAME = "COMPACT_ORTHOGONAL";

  private static final String ORTHOGONAL_LAYOUT_STYLE = "ORTHOGONAL_LAYOUT_STYLE";
  private static final String GRID = "GRID";

  private static final String NORMAL = "NORMAL";
  private static final String NORMAL_TREE = "NORMAL_TREE";
  private static final String FIXED_MIXED = "FIXED_MIXED";
  private static final String FIXED_BOX_NODES = "FIXED_BOX_NODES";

  private static final String ASPECT_RATIO = "ASPECT_RATIO";
  private static final String USE_VIEW_ASPECT_RATIO = "USE_VIEW_ASPECT_RATIO";

  private static final String PLACEMENT_STRATEGY = "PLACEMENT_STRATEGY";
  private static final String STYLE_ROWS = "STYLE_ROWS";
  private static final String STYLE_PACKED_COMPACT_RECTANGLE = "STYLE_PACKED_COMPACT_RECTANGLE";

  // ChannelInterEdgeRouter stuff
  private static final String PATH_FINDER = "PATH_FINDER";
  private static final String ORTHOGONAL_PATTERN_PATH_FINDER = "ORTHOGONAL_PATTERN_PATH_FINDER";
  private static final String ORTHOGONAL_SHORTESTPATH_PATH_FINDER = "ORTHOGONAL_SHORTESTPATH_PATH_FINDER";
  private static final String INTER_EDGE_ROUTER = "INTER_EDGE_ROUTER";
  private static final String ROUTE_ALL_EDGES = "ROUTE_ALL_EDGES";

  // ChannelEdgeRouter stuff
  private static final String MINIMUM_DISTANCE = "MINIMUM_DISTANCE";
  private static final String CENTER_TO_SPACE_RATIO = "SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH";
  private static final String EDGE_CROSSING_COST = "EDGE_CROSSING_COST";
  private static final String NODE_CROSSING_COST = "NODE_CROSSING_COST";
  private static final String BEND_COST = "BEND_COST";

  // for the option handler
  private static final String[] COMPONENT_STYLE_ENUM = {
    STYLE_ROWS,
    STYLE_PACKED_COMPACT_RECTANGLE
  };
  private static final String[] STYLE_ENUM = {
    NORMAL, NORMAL_TREE, FIXED_MIXED, FIXED_BOX_NODES
  };
  private static final String[] PATH_FINDER_ENUM = {
    ORTHOGONAL_PATTERN_PATH_FINDER,
    ORTHOGONAL_SHORTESTPATH_PATH_FINDER
  };


  public CompactOrthogonalLayoutModule() {
    super (NAME,"yFiles Layout Team",
           "Compact Orthogonal Layouter");
    setPortIntersectionCalculatorEnabled(true);
  }

  public OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());
    ConstraintManager cm =  new ConstraintManager(op);
    OptionGroup og;


    // use an instance of the layouter as a defaults provider
    CompactOrthogonalLayouter layouter = new CompactOrthogonalLayouter();
    prepare(layouter);

    OrthogonalLayouter cl = (OrthogonalLayouter) layouter.getCoreLayouter();

    int styleIndex = 0;
    switch (cl.getLayoutStyle()) {
      case OrthogonalLayouter.NORMAL_STYLE:
        styleIndex = 0;
        break;
      case OrthogonalLayouter.NORMAL_TREE_STYLE:
        styleIndex = 1;
        break;
      case OrthogonalLayouter.FIXED_MIXED_STYLE:
        styleIndex = 2;
        break;
      case OrthogonalLayouter.FIXED_BOX_STYLE:
        styleIndex = 3;
        break;
    }
    op.addEnum(ORTHOGONAL_LAYOUT_STYLE, STYLE_ENUM, styleIndex);


    PartitionLayouter.ComponentPartitionPlacer cpp = (PartitionLayouter.ComponentPartitionPlacer) layouter.getPartitionPlacer();

    int compStyleIndex = 0;
    switch (cpp.getComponentLayouter().getStyle()) {
      case ComponentLayouter.STYLE_ROWS:
        compStyleIndex = 0;
        break;
      case ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE:
        compStyleIndex = 1;
        break;
    }
    op.addEnum(PLACEMENT_STRATEGY, COMPONENT_STYLE_ENUM, compStyleIndex);

    op.addBool(USE_VIEW_ASPECT_RATIO, true);
    op.addDouble(ASPECT_RATIO, layouter.getAspectRatio());
    cm.setEnabledOnValueEquals(USE_VIEW_ASPECT_RATIO, Boolean.FALSE, ASPECT_RATIO);

    op.addInt(GRID, layouter.getGridSpacing())
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));


    // ChannelInterEdgeRouter stuff
    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, INTER_EDGE_ROUTER);

    og.addItem(op.addEnum(PATH_FINDER, PATH_FINDER_ENUM, 1));

    PartitionLayouter.ChannelInterEdgeRouter cier = (PartitionLayouter.ChannelInterEdgeRouter)layouter.getInterEdgeRouter();
    og.addItem(op.addBool(ROUTE_ALL_EDGES, !cier.isRouteInterEdgesOnly()));

    // ChannelEdgeRouter stuff
    OrthogonalPatternEdgeRouter oper = new OrthogonalPatternEdgeRouter();
    ChannelEdgeRouter.OrthogonalShortestPathPathFinder osppf = new ChannelEdgeRouter.OrthogonalShortestPathPathFinder();

    // path finding strategy properties
    og.addItem(op.addDouble(BEND_COST, oper.getBendCost()));
    og.addItem(op.addDouble(NODE_CROSSING_COST, oper.getNodeCrossingCost()));
    og.addItem(op.addInt(MINIMUM_DISTANCE, osppf.getMinimumDistance()));
    op.getItem(MINIMUM_DISTANCE)
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(4));
    og.addItem(op.addDouble(EDGE_CROSSING_COST, osppf.getCrossingCost()));
    og.addItem(op.addDouble(CENTER_TO_SPACE_RATIO, osppf.getCenterToSpaceRatio(), 0, 1));

    cm.setEnabledOnValueEquals(PATH_FINDER, ORTHOGONAL_PATTERN_PATH_FINDER, BEND_COST);
    cm.setEnabledOnValueEquals(PATH_FINDER, ORTHOGONAL_PATTERN_PATH_FINDER, NODE_CROSSING_COST);
    cm.setEnabledOnValueEquals(PATH_FINDER, ORTHOGONAL_SHORTESTPATH_PATH_FINDER, CENTER_TO_SPACE_RATIO);
    return op;
  }


  private void prepare( CompactOrthogonalLayouter layouter ) {
    PartitionLayouter.InterEdgeRouter ier = layouter.getInterEdgeRouter();
    if (!(ier instanceof PartitionLayouter.ChannelInterEdgeRouter)) {
      ier = new PartitionLayouter.ChannelInterEdgeRouter();
      layouter.setInterEdgeRouter(ier);
    }

    PartitionLayouter.PartitionPlacer pp = layouter.getPartitionPlacer();
    if (!(pp instanceof PartitionLayouter.ComponentPartitionPlacer)) {
      pp = new PartitionLayouter.ComponentPartitionPlacer();
      layouter.setPartitionPlacer(pp);
    }
    Layouter cl = layouter.getCoreLayouter();
    if (!(cl instanceof OrthogonalLayouter)) {
      cl = new OrthogonalLayouter();
      layouter.setCoreLayouter(cl);
    }
  }

  private void applyOptions( OptionHandler oh, PartitionLayouter.ChannelInterEdgeRouter router ) {
    router.setRouteInterEdgesOnly(!oh.getBool(ROUTE_ALL_EDGES));
    if (oh.getEnum(PATH_FINDER) == 0) {
      OrthogonalPatternEdgeRouter oper = new OrthogonalPatternEdgeRouter();
      oper.setMinimumDistance(oh.getInt(MINIMUM_DISTANCE));
      oper.setEdgeCrossingCost(oh.getDouble(EDGE_CROSSING_COST));
      oper.setNodeCrossingCost(oh.getDouble(NODE_CROSSING_COST));
      oper.setBendCost(oh.getDouble(BEND_COST));
      router.getChannelEdgeRouter().setPathFinderStrategy(oper);
    } else {
      ChannelEdgeRouter.OrthogonalShortestPathPathFinder osppf = new ChannelEdgeRouter.OrthogonalShortestPathPathFinder();
      osppf.setMinimumDistance(oh.getInt(MINIMUM_DISTANCE));
      osppf.setCrossingCost(oh.getDouble(EDGE_CROSSING_COST));
      osppf.setCenterToSpaceRatio(oh.getDouble(CENTER_TO_SPACE_RATIO));
      router.getChannelEdgeRouter().setPathFinderStrategy(osppf);
    }
  }

  private void applyOptions( OptionHandler oh, PartitionLayouter.ComponentPartitionPlacer placer ) {
    if (STYLE_PACKED_COMPACT_RECTANGLE.equals(oh.get(PLACEMENT_STRATEGY))) {
      placer.getComponentLayouter().setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE);
    }
    else if (STYLE_ROWS.equals(oh.get(PLACEMENT_STRATEGY))) {
      placer.getComponentLayouter().setStyle(ComponentLayouter.STYLE_ROWS);
    }
  }

  private void applyOptions( OptionHandler oh, OrthogonalLayouter layouter ) {
    switch (OptionHandler.getIndex(STYLE_ENUM, oh.getString(ORTHOGONAL_LAYOUT_STYLE))) {
      default:
      case 0:
        layouter.setLayoutStyle(OrthogonalLayouter.NORMAL_STYLE);
        break;
      case 1:
        layouter.setLayoutStyle(OrthogonalLayouter.NORMAL_TREE_STYLE);
        break;
      case 2:
        layouter.setLayoutStyle(OrthogonalLayouter.FIXED_MIXED_STYLE);
        break;
      case 3:
        layouter.setLayoutStyle(OrthogonalLayouter.FIXED_BOX_STYLE);
        break;
    }
  }

  private void applyOptions( OptionHandler oh, CompactOrthogonalLayouter layouter ) {
    layouter.setGridSpacing(oh.getInt(GRID));

    final double ar;
    if (oh.getBool(USE_VIEW_ASPECT_RATIO) && getGraph2DView() != null) {
      final Dimension dim = getGraph2DView().getSize();
      ar = dim.getWidth()/dim.getHeight();
    } else {
      ar = oh.getDouble(ASPECT_RATIO);
    }

    // this needs to be done as a final step since it will reconfigure
    // layout stages which support aspect ratio accordingly
    layouter.setAspectRatio(ar);
  }

  public void mainrun() {
    final OptionHandler op = getOptionHandler();

    CompactOrthogonalLayouter compactOrthogonal = new CompactOrthogonalLayouter();
    prepare(compactOrthogonal);

    PartitionLayouter.ChannelInterEdgeRouter router = (PartitionLayouter.ChannelInterEdgeRouter) compactOrthogonal.getInterEdgeRouter();
    applyOptions(op, router);

    PartitionLayouter.ComponentPartitionPlacer placer = (PartitionLayouter.ComponentPartitionPlacer) compactOrthogonal.getPartitionPlacer();
    applyOptions(op, placer);

    OrthogonalLayouter orthogonalCore = (OrthogonalLayouter) compactOrthogonal.getCoreLayouter();
    applyOptions(op, orthogonalCore);

    applyOptions(op, compactOrthogonal);


    // launch layouter in buffered mode
    GroupNodeHider groupNodeHider = new GroupNodeHider(compactOrthogonal);
    groupNodeHider.setHidingEmptyGroupNodes(false);
    launchLayouter(groupNodeHider);
  }
}
