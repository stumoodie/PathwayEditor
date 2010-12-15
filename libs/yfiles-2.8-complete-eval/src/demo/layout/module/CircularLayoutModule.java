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

import y.layout.circular.CircularLayouter;
import y.layout.circular.SingleCycleLayouter;
import y.layout.tree.BalloonLayouter;
import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.option.OptionItem;
import y.view.Graph2D;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.circular.CircularLayouter}.
 * 
 */
public class CircularLayoutModule extends LayoutModule {
  private static final String CIRCULAR = "CIRCULAR";
  private static final String ALLOW_OVERLAPS = "ALLOW_OVERLAPS";
  private static final String COMPACTNESS_FACTOR = "COMPACTNESS_FACTOR";
  private static final String MAXIMAL_DEVIATION_ANGLE = "MAXIMAL_DEVIATION_ANGLE";
  private static final String MINIMAL_EDGE_LENGTH = "MINIMAL_EDGE_LENGTH";
  private static final String PREFERRED_CHILD_WEDGE = "PREFERRED_CHILD_WEDGE";
  private static final String TREE = "TREE";
  private static final String FIXED_RADIUS = "FIXED_RADIUS";
  private static final String CHOOSE_RADIUS_AUTOMATICALLY = "CHOOSE_RADIUS_AUTOMATICALLY";
  private static final String MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  private static final String MINIMAL_TREE_NODE_DISTANCE = "MINIMAL_TREE_NODE_DISTANCE";
  private static final String CYCLE = "CYCLE";
  private static final String ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  private static final String LAYOUT_STYLE = "LAYOUT_STYLE";
  private static final String GENERAL = "GENERAL";
  private static final String SINGLE_CYCLE = "SINGLE_CYCLE";
  private static final String BCC_ISOLATED = "BCC_ISOLATED";
  private static final String BCC_COMPACT = "BCC_COMPACT";
  private static final String CIRCULAR_CUSTOM_GROUPS = "CIRCULAR_CUSTOM_GROUPS";
  private static final String FROM_SKETCH = "FROM_SKETCH";  
  private static final String HANDLE_NODE_LABELS = "HANDLE_NODE_LABELS";
  private static final String PLACE_CHILDREN_ON_COMMON_RADIUS = "PLACE_CHILDREN_ON_COMMON_RADIUS";

  private final static String[] layoutStyles = {BCC_COMPACT, BCC_ISOLATED, CIRCULAR_CUSTOM_GROUPS, SINGLE_CYCLE};
  private final static String PARTITION_LAYOUT_STYLE = "PARTITION_LAYOUT_STYLE";
  private final static String PARTITION_LAYOUTSTYLE_CYCLIC = "PARTITION_LAYOUTSTYLE_CYCLIC";
  private final static String PARTITION_LAYOUTSTYLE_DISK = "PARTITION_LAYOUTSTYLE_DISK";
  private final static String PARTITION_LAYOUTSTYLE_ORGANIC = "PARTITION_LAYOUTSTYLE_ORGANIC";

  private final static String[] partitionLayoutStyles = {PARTITION_LAYOUTSTYLE_CYCLIC, PARTITION_LAYOUTSTYLE_DISK, PARTITION_LAYOUTSTYLE_ORGANIC};

  public CircularLayoutModule() {
    super( CIRCULAR, "yFiles Layout Team",
        "Circular Layout" );
  }

  public OptionHandler createOptionHandler() {
    CircularLayouter layouter = new CircularLayouter();
    SingleCycleLayouter cycleLayouter = layouter.getSingleCycleLayouter();
    BalloonLayouter treeLayouter = layouter.getBalloonLayouter();


    OptionHandler op = new OptionHandler( getModuleName() );

    op.useSection( GENERAL );
    op.addEnum( LAYOUT_STYLE, layoutStyles, layouter.getLayoutStyle() );
    op.addBool( ACT_ON_SELECTION_ONLY, false );
    op.addBool( FROM_SKETCH, false );
    op.addBool(HANDLE_NODE_LABELS, false);

    op.useSection( CYCLE );
    op.addEnum( PARTITION_LAYOUT_STYLE, partitionLayoutStyles, layouter.getPartitionLayoutStyle() );
    OptionItem mndi = op.addInt( MINIMAL_NODE_DISTANCE, ( int ) cycleLayouter.getMinimalNodeDistance(), 0, 999 );
    OptionItem crai = op.addBool( CHOOSE_RADIUS_AUTOMATICALLY, cycleLayouter.getAutomaticRadius() );
    OptionItem fri = op.addInt( FIXED_RADIUS, ( int ) cycleLayouter.getFixedRadius(), 50, 800 );

    ConstraintManager cm = new ConstraintManager(op);
    cm.setEnabledOnValueEquals(crai, Boolean.FALSE, fri );
    cm.setEnabledOnValueEquals(crai, Boolean.TRUE, mndi );

    op.useSection( TREE );
    op.addInt( PREFERRED_CHILD_WEDGE, treeLayouter.getPreferredChildWedge(), 1, 359 );
    op.addInt( MINIMAL_EDGE_LENGTH, treeLayouter.getMinimalEdgeLength(), 5, 400 );
    op.addInt( MAXIMAL_DEVIATION_ANGLE, layouter.getMaximalDeviationAngle(), 10, 360 );
    op.addDouble( COMPACTNESS_FACTOR, treeLayouter.getCompactnessFactor(), 0.1, 0.9 );
    op.addInt( MINIMAL_TREE_NODE_DISTANCE, treeLayouter.getMinimalNodeDistance(), 0, 100 );
    op.addBool( ALLOW_OVERLAPS, treeLayouter.getAllowOverlaps() );
    op.addBool(PLACE_CHILDREN_ON_COMMON_RADIUS, true );
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, MINIMAL_TREE_NODE_DISTANCE), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, PLACE_CHILDREN_ON_COMMON_RADIUS), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, PREFERRED_CHILD_WEDGE), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, MINIMAL_EDGE_LENGTH), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, MAXIMAL_DEVIATION_ANGLE), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, COMPACTNESS_FACTOR), true);
    cm.setEnabledOnValueEquals( op.getItem( GENERAL, LAYOUT_STYLE), SINGLE_CYCLE, op.getItem( TREE, ALLOW_OVERLAPS), true);
    return op;
  }


  public void mainrun() {
    OptionHandler op = getOptionHandler();

    CircularLayouter layouter = new CircularLayouter();

    BalloonLayouter treeLayouter = layouter.getBalloonLayouter();

    if ( op.getString( LAYOUT_STYLE ).equals( BCC_COMPACT ) ) {
      layouter.setLayoutStyle( CircularLayouter.BCC_COMPACT );
    } else if ( op.getString( LAYOUT_STYLE ).equals( BCC_ISOLATED ) ) {
      layouter.setLayoutStyle( CircularLayouter.BCC_ISOLATED );
    } else if ( op.getString( LAYOUT_STYLE ).equals( CIRCULAR_CUSTOM_GROUPS ) ) {
      layouter.setLayoutStyle( CircularLayouter.CIRCULAR_CUSTOM_GROUPS );
    } else {
      layouter.setLayoutStyle( CircularLayouter.SINGLE_CYCLE );
    }

    layouter.setSubgraphLayouterEnabled( op.getBool( ACT_ON_SELECTION_ONLY ) );
    layouter.setMaximalDeviationAngle( op.getInt( MAXIMAL_DEVIATION_ANGLE ) );
    layouter.setFromSketchModeEnabled( op.getBool( FROM_SKETCH));   
    layouter.setPlaceChildrenOnCommonRadiusEnabled(op.getBool(PLACE_CHILDREN_ON_COMMON_RADIUS));
    layouter.setConsiderNodeLabelsEnabled(op.getBool(HANDLE_NODE_LABELS));
    
    if ( op.getString( PARTITION_LAYOUT_STYLE ).equals( PARTITION_LAYOUTSTYLE_CYCLIC ) ) {
      layouter.setPartitionLayoutStyle( CircularLayouter.PARTITION_LAYOUTSTYLE_CYCLIC );
    } else if ( op.getString( PARTITION_LAYOUT_STYLE ).equals( PARTITION_LAYOUTSTYLE_DISK ) ) {
      layouter.setPartitionLayoutStyle( CircularLayouter.PARTITION_LAYOUTSTYLE_DISK );
    }
    else if ( op.getString( PARTITION_LAYOUT_STYLE ).equals( PARTITION_LAYOUTSTYLE_ORGANIC ) ) {
      layouter.setPartitionLayoutStyle( CircularLayouter.PARTITION_LAYOUTSTYLE_ORGANIC );
    }

    SingleCycleLayouter cycleLayouter = layouter.getSingleCycleLayouter();
    cycleLayouter.setMinimalNodeDistance( op.getInt( MINIMAL_NODE_DISTANCE ) );
    cycleLayouter.setAutomaticRadius( op.getBool( CHOOSE_RADIUS_AUTOMATICALLY ) );
    cycleLayouter.setFixedRadius( op.getInt( FIXED_RADIUS ) );

    treeLayouter.setPreferredChildWedge( op.getInt( PREFERRED_CHILD_WEDGE ) );
    treeLayouter.setMinimalEdgeLength( op.getInt( MINIMAL_EDGE_LENGTH ) );
    treeLayouter.setCompactnessFactor( op.getDouble( COMPACTNESS_FACTOR ) );
    treeLayouter.setAllowOverlaps( op.getBool( ALLOW_OVERLAPS ) );
    treeLayouter.setMinimalNodeDistance(op.getInt(MINIMAL_TREE_NODE_DISTANCE));

    Graph2D graph = getGraph2D();
    if ( op.getString( LAYOUT_STYLE ).equals( CIRCULAR_CUSTOM_GROUPS ) ) {
      //Set up grouping key for custom layout style
      //This acts as an adapter for grouping structure to circular grouping keys
      if ( graph.getHierarchyManager() != null ) {
        graph.addDataProvider( CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY,
            graph.getHierarchyManager().getParentNodeIdDataProvider() );
      }
    }
    try {
      launchLayouter( layouter );
    } finally {
      // make sure the DataProviders will always be unregistered
      if ( op.getString( LAYOUT_STYLE ).equals( CIRCULAR_CUSTOM_GROUPS ) ) {
        //Remove temporary set up data providers from graph
        graph.removeDataProvider( CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY );
      }
    }
  }
}