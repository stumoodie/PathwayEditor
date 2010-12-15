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

import y.layout.organic.SmartOrganicLayouter;
import y.layout.organic.OutputRestriction;
import y.layout.ComponentLayouter;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.DoubleOptionItem;
import y.option.IntOptionItem;
import y.option.OptionHandler;
import y.option.OptionGroup;
import y.option.OptionItem;
import y.view.Graph2D;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;
import y.base.NodeCursor;
import y.base.Node;
import y.base.NodeMap;
import y.util.Maps;

import java.awt.Rectangle;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.organic.SmartOrganicLayouter}.
 *
 */
public class SmartOrganicLayoutModule extends LayoutModule
{
  private static final String ACTIVATE_DETERMINISTIC_MODE = "ACTIVATE_DETERMINISTIC_MODE";
  private static final String VISUAL = "VISUAL";
  private static final String ALGORITHM = "ALGORITHM";
  private static final String COMPACTNESS = "COMPACTNESS";
  private static final String MAXIMAL_DURATION = "MAXIMAL_DURATION";
  private static final String OBEY_NODE_SIZES = "OBEY_NODE_SIZES";
  private static final String CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  private static final String ALLOW_NODE_OVERLAPS = "ALLOW_NODE_OVERLAPS";
  private static final String MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  private static final String SCOPE = "SCOPE";
  private static final String PREFERRED_EDGE_LENGTH = "PREFERRED_EDGE_LENGTH";
  private static final String SMARTORGANIC = "SMARTORGANIC";
  private static final String SCOPE_SUBSET = "SUBSET";
  private static final String SCOPE_MAINLY_SUBSET = "MAINLY_SUBSET";
  private static final String SCOPE_ALL = "ALL";
  private static final String QUALITY_TIME_RATIO = "QUALITY_TIME_RATIO";
  private static final String RESTRICT_OUTPUT = "RESTRICT_OUTPUT";
  private static final String NONE = "NONE";
  private static final String OUTPUT_CAGE = "OUTPUT_CAGE";
  private static final String OUTPUT_CIRCULAR_CAGE = "OUTPUT_CIRCULAR_CAGE";
  private static final String OUTPUT_ELLIPTICAL_CAGE = "OUTPUT_ELLIPTICAL_CAGE";
  private static final String OUTPUT_AR = "OUTPUT_AR";
  private static final String CAGE_X = "CAGE_X";
  private static final String CAGE_Y = "CAGE_Y";
  private static final String CAGE_WIDTH = "CAGE_WIDTH";
  private static final String CAGE_HEIGHT = "CAGE_HEIGHT";
  private static final String ELLIPTICAL_CAGE_X = "ELLIPTICAL_CAGE_X";
  private static final String ELLIPTICAL_CAGE_Y = "ELLIPTICAL_CAGE_Y";
  private static final String ELLIPTICAL_CAGE_WIDTH = "ELLIPTICAL_CAGE_WIDTH";
  private static final String ELLIPTICAL_CAGE_HEIGHT = "ELLIPTICAL_CAGE_HEIGHT";
  private static final String CAGE_CENTER_X = "CAGE_CENTER_X";
  private static final String CAGE_CENTER_Y = "CAGE_CENTER_Y";
  private static final String CAGE_RADIUS = "CAGE_RADIUS";
  private static final String CAGE_RATIO = "CAGE_RATIO";
  private static final String AR_CAGE_USE_VIEW = "AR_CAGE_USE_VIEW";
  private static final String RECT_CAGE_USE_VIEW = "RECT_CAGE_USE_VIEW";
  private static final String CIRC_CAGE_USE_VIEW = "CIRC_CAGE_USE_VIEW";
  private static final String ELL_CAGE_USE_VIEW = "ELL_CAGE_USE_VIEW";
  private static final String RESTRICTIONS = "RESTRICTIONS";
  private static final String AVOID_NODE_EDGE_OVERLAPS = "AVOID_NODE_EDGE_OVERLAPS";
  private static final String GROUPING      = "GROUPING";
  private static final String GROUP_LAYOUT_POLICY = "GROUP_LAYOUT_POLICY";
  private static final String IGNORE_GROUPS = "IGNORE_GROUPS";
  private static final String LAYOUT_GROUPS = "LAYOUT_GROUPS";
  private static final String FIX_GROUP_BOUNDS    = "FIX_GROUP_BOUNDS";
  private static final String FIX_GROUP_CONTENTS = "FIX_GROUP_CONTENTS";
  private static final String USE_AUTO_CLUSTERING = "USE_AUTO_CLUSTERING";
  private static final String AUTO_CLUSTERING_QUALITY = "AUTO_CLUSTERING_QUALITY";

// for the option handler
  private final static String[] SCOPES =
  {
    SCOPE_ALL,
    SCOPE_MAINLY_SUBSET,
    SCOPE_SUBSET,
  };

// for the option handler
  private final String[] GROUPING_POLICIES = new String[]{
      LAYOUT_GROUPS,
      FIX_GROUP_CONTENTS,
      FIX_GROUP_BOUNDS,
      IGNORE_GROUPS,
  };

  private final static String[] OUTPUT_RESTRICTIONS =
  {
    NONE,
    OUTPUT_CAGE,
    OUTPUT_CIRCULAR_CAGE,
    OUTPUT_AR,
    OUTPUT_ELLIPTICAL_CAGE,
  };

  private SmartOrganicLayouter organic;

  public SmartOrganicLayoutModule()
  {
    super (SMARTORGANIC,
           "yWorks Graph Layout Team",
           "Wrapper for SmartOrganicLayouter");
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Factory method. Responsible for creating and initializing
   * the OptionHandler for this module.
   */
  protected OptionHandler createOptionHandler()
  {
    createOrganic();

    OptionHandler op = new OptionHandler(getModuleName());
    ConstraintManager cm = new ConstraintManager(op);

    op.useSection(VISUAL);
    op.addEnum(SCOPE,SCOPES,
               organic.getScope());
    op.addInt(PREFERRED_EDGE_LENGTH, (int)organic.getPreferredEdgeLength(), 5, 500);
    op.addBool(CONSIDER_NODE_LABELS,organic.isConsiderNodeLabelsEnabled());
    op.addBool(ALLOW_NODE_OVERLAPS,organic.isNodeOverlapsAllowed());
    ConstraintManager.Condition condition = cm.createConditionValueEquals(ALLOW_NODE_OVERLAPS, Boolean.FALSE).or(
        cm.createConditionValueEquals(CONSIDER_NODE_LABELS, Boolean.TRUE));
    cm.setEnabledOnCondition(condition, op.addDouble(MINIMAL_NODE_DISTANCE,organic.getMinimalNodeDistance(),0,100,0));
    op.addBool(AVOID_NODE_EDGE_OVERLAPS, false);
    cm.setEnabledOnValueEquals(CONSIDER_NODE_LABELS, Boolean.FALSE, ALLOW_NODE_OVERLAPS);

    op.addDouble(COMPACTNESS,organic.getCompactness(),0,1);

    op.addBool(USE_AUTO_CLUSTERING, organic.isAutoClusteringEnabled());
    op.addDouble(AUTO_CLUSTERING_QUALITY, organic.getAutoClusteringQuality(), 0, 1);
    cm.setEnabledOnValueEquals(USE_AUTO_CLUSTERING, Boolean.TRUE, AUTO_CLUSTERING_QUALITY);

    op.useSection(RESTRICTIONS);

    final Object ctrId = new Object();
    op.addEnum(RESTRICT_OUTPUT, OUTPUT_RESTRICTIONS, 0).setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );

    OptionGroup og;
    og = new OptionGroup();
    cm.setEnabledOnValueEquals( RESTRICT_OUTPUT, OUTPUT_CAGE, og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, OUTPUT_CAGE );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, OUTPUT_CAGE );
    og.addItem( op.addBool(RECT_CAGE_USE_VIEW, true));

    condition = cm.createConditionValueEquals(RESTRICT_OUTPUT, OUTPUT_CAGE).and(
        cm.createConditionValueEquals(RECT_CAGE_USE_VIEW, Boolean.FALSE));

    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_X, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_Y, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_WIDTH, 1000.0d) ));
    op.getItem(RESTRICTIONS, CAGE_WIDTH)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_HEIGHT, 1000.0d) ));
    op.getItem(RESTRICTIONS, CAGE_HEIGHT)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));

    og = new OptionGroup();
    cm.setEnabledOnValueEquals( RESTRICT_OUTPUT, OUTPUT_CIRCULAR_CAGE, og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, OUTPUT_CIRCULAR_CAGE );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, OUTPUT_CIRCULAR_CAGE );
    og.addItem( op.addBool(CIRC_CAGE_USE_VIEW, true));
    condition = cm.createConditionValueEquals(RESTRICT_OUTPUT, OUTPUT_CIRCULAR_CAGE).and(
        cm.createConditionValueEquals(CIRC_CAGE_USE_VIEW, Boolean.FALSE));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_CENTER_X, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_CENTER_Y, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_RADIUS, 1000.0d) ));
    op.getItem(RESTRICTIONS,CAGE_RADIUS)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));

    og = new OptionGroup();
    cm.setEnabledOnValueEquals( RESTRICT_OUTPUT, OUTPUT_ELLIPTICAL_CAGE, og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, OUTPUT_ELLIPTICAL_CAGE );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, OUTPUT_ELLIPTICAL_CAGE );
    og.addItem( op.addBool(ELL_CAGE_USE_VIEW, true));
    condition = cm.createConditionValueEquals(RESTRICT_OUTPUT, OUTPUT_ELLIPTICAL_CAGE).and(
        cm.createConditionValueEquals(ELL_CAGE_USE_VIEW, Boolean.FALSE));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(ELLIPTICAL_CAGE_X, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(ELLIPTICAL_CAGE_Y, 0.0d) ));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(ELLIPTICAL_CAGE_WIDTH, 1000.0d) ));
    op.getItem(RESTRICTIONS, ELLIPTICAL_CAGE_WIDTH)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(ELLIPTICAL_CAGE_HEIGHT, 1000.0d) ));
    op.getItem(RESTRICTIONS, ELLIPTICAL_CAGE_HEIGHT)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));

    og = new OptionGroup();
    cm.setEnabledOnValueEquals( RESTRICT_OUTPUT, OUTPUT_AR, og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, OUTPUT_AR );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, OUTPUT_AR );
    og.addItem( op.addBool(AR_CAGE_USE_VIEW, true));
    condition = cm.createConditionValueEquals(RESTRICT_OUTPUT, OUTPUT_AR).and(
        cm.createConditionValueEquals(AR_CAGE_USE_VIEW, Boolean.FALSE));
    cm.setEnabledOnCondition(condition, og.addItem( op.addDouble(CAGE_RATIO, 1.0d) ));

    op.useSection(GROUPING);
    op.addEnum(GROUP_LAYOUT_POLICY, GROUPING_POLICIES, 0);
//    op.addDouble(GROUP_NODE_COMPACTNESS, organic.getGroupNodeCompactness(), 0, 1);


    op.useSection(ALGORITHM);
    OptionItem qualityItem = op.addDouble( QUALITY_TIME_RATIO, organic.getQualityTimeRatio(), 0, 1 );
    qualityItem.setAttribute( DefaultEditorFactory.ATTRIBUTE_MIN_VALUE_LABEL_TEXT, "SPEED" );
    qualityItem.setAttribute( DefaultEditorFactory.ATTRIBUTE_MAX_VALUE_LABEL_TEXT, "QUALITY" );

    op.addInt(MAXIMAL_DURATION,(int)(organic.getMaximumDuration()/1000))
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(0));
    op.addBool(ACTIVATE_DETERMINISTIC_MODE,organic.isDeterministic());
    return op;
  }

  /**
   * Module initialization routine. Typically this method is used to 
   * configure the underlying algorithm with the options found in the
   * options handler of this module.
   */
  protected void init()
  {
    createOrganic();

    OptionHandler op = getOptionHandler();
    organic.setPreferredEdgeLength(op.getInt(VISUAL, PREFERRED_EDGE_LENGTH));
    boolean considerNodeLabels = op.getBool(VISUAL,CONSIDER_NODE_LABELS);
    organic.setConsiderNodeLabelsEnabled(considerNodeLabels);
    organic.setNodeOverlapsAllowed(op.getBool(VISUAL, ALLOW_NODE_OVERLAPS) && !considerNodeLabels);
    organic.setMinimalNodeDistance(op.getDouble(VISUAL, MINIMAL_NODE_DISTANCE));
    organic.setScope(OptionHandler.getIndex(SCOPES, op.getString(VISUAL,SCOPE)));
    organic.setCompactness(op.getDouble(VISUAL,COMPACTNESS));    
    //Doesn't really make sense to ignore node sizes (for certain configurations, this setting
    //doesn't have an effect anyway)
    organic.setNodeSizeAware(true);
    organic.setAutoClusteringEnabled(op.getBool(USE_AUTO_CLUSTERING));
    organic.setAutoClusteringQuality(op.getDouble(AUTO_CLUSTERING_QUALITY));
    organic.setNodeEdgeOverlapAvoided(op.getBool(AVOID_NODE_EDGE_OVERLAPS));
    organic.setDeterministic(op.getBool(ALGORITHM, ACTIVATE_DETERMINISTIC_MODE));
    organic.setMaximumDuration(1000*op.getInt(ALGORITHM, MAXIMAL_DURATION));
    organic.setQualityTimeRatio(op.getDouble(ALGORITHM,QUALITY_TIME_RATIO));
    switch (op.getEnum(RESTRICT_OUTPUT)){
      case 0:
        organic.setComponentLayouterEnabled(true);
        organic.setOutputRestriction( OutputRestriction.NONE);
        break;
      case 1: {
        double x;
        double y;
        double w;
        double h;
        if (op.getBool(RECT_CAGE_USE_VIEW) && getGraph2DView() != null) {
          Rectangle visibleRect = getGraph2DView().getVisibleRect();
          x = visibleRect.x;
          y = visibleRect.y;
          w = visibleRect.width;
          h = visibleRect.height;
        } else {
          x = op.getDouble(CAGE_X);
          y = op.getDouble(CAGE_Y);
          w = op.getDouble(CAGE_WIDTH);
          h = op.getDouble(CAGE_HEIGHT);
        }
        organic.setOutputRestriction(
            OutputRestriction.createRectangularCageRestriction(x, y, w, h));
        organic.setComponentLayouterEnabled(false);
        break;
      }
      case 2:
      {
        double x;
        double y;
        double radius;
        if (op.getBool(CIRC_CAGE_USE_VIEW) && getGraph2DView() != null) {
          Rectangle visibleRect = getGraph2DView().getVisibleRect();
          x = visibleRect.getCenterX();
          y = visibleRect.getCenterY();
          radius = Math.min(visibleRect.width, visibleRect.height) * 0.5d;
        } else {
          x = op.getDouble(CAGE_CENTER_X);
          y = op.getDouble(CAGE_CENTER_Y);
          radius = op.getDouble(CAGE_RADIUS);
        }
        organic.setOutputRestriction( OutputRestriction.createCircularCageRestriction(x, y, radius));
        organic.setComponentLayouterEnabled(false);
        break;
      }
      case 3:
      {
        double ratio;
        if (op.getBool(AR_CAGE_USE_VIEW) && getGraph2DView() != null) {
          Rectangle visibleRect = getGraph2DView().getVisibleRect();
          ratio = visibleRect.getWidth()/visibleRect.getHeight();
        } else {
          ratio = op.getDouble(CAGE_RATIO);
        }
        organic.setOutputRestriction( OutputRestriction.createAspectRatioRestriction(ratio));
        organic.setComponentLayouterEnabled(true);
        ((ComponentLayouter) organic.getComponentLayouter()).setPreferredLayoutSize(ratio * 100, 100);
        break;
      }
      case 4:
      {
        double x;
        double y;
        double w;
        double h;
        if (op.getBool(ELL_CAGE_USE_VIEW) && getGraph2DView() != null) {
          Rectangle visibleRect = getGraph2DView().getVisibleRect();
          x = visibleRect.x;
          y = visibleRect.y;
          w = visibleRect.width;
          h = visibleRect.height;
        } else {
          x = op.getDouble(ELLIPTICAL_CAGE_X);
          y = op.getDouble(ELLIPTICAL_CAGE_Y);
          w = op.getDouble(ELLIPTICAL_CAGE_WIDTH);
          h = op.getDouble(ELLIPTICAL_CAGE_HEIGHT);
        }
        organic.setOutputRestriction(
            OutputRestriction.createEllipticalCageRestriction(x, y, w, h));
        organic.setComponentLayouterEnabled(false);
        break;
      }
    }
  }

  /**
   * Main module execution routine. launches the hierarchic layouter.
   */
  protected void mainrun()
  {
    final OptionHandler handler = getOptionHandler();
    final int policy = handler.getEnum(GROUP_LAYOUT_POLICY);
    createOrganic();
    final Graph2D graph = getGraph2D();
    try {
      final boolean grouping = policy != 3 && HierarchyManager.containsGroupNodes(graph);
      getLayoutExecutor().setConfiguringGrouping(grouping);
      if (policy == 2 && grouping) {
        NodeMap nodeMap = Maps.createHashedNodeMap();
        for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
          Node node = nodeCursor.node();
          if (HierarchyManager.getInstance(graph).isGroupNode(node)){
            nodeMap.set(node, SmartOrganicLayouter.GROUP_NODE_MODE_FIX_BOUNDS);
          }
        }
        graph.addDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA, nodeMap);
      } else if (policy == 1 && grouping) {
        NodeMap nodeMap = Maps.createHashedNodeMap();
        for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
          Node node = nodeCursor.node();
          if (HierarchyManager.getInstance(graph).isGroupNode(node)){
            nodeMap.set(node, SmartOrganicLayouter.GROUP_NODE_MODE_FIX_CONTENTS);
          }
        }
        graph.addDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA, nodeMap);
      }
      graph.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA,
          Selections.createSelectionNodeMap(graph));
      launchLayouter(organic);
    } finally {
      graph.removeDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA);
      if (policy == 2 || policy == 1) {
        graph.removeDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA);
      }
      getLayoutExecutor().setConfiguringGrouping(true);
    }
  }

  /**
   * clean up the module, clear temporarily bound data providers and
   * references to the wrapped algorithm.
   */
  protected void dispose()
  {
    organic = null;
  }

  private void createOrganic()
  {
    if(organic == null)
    {
      organic = new SmartOrganicLayouter();      
    }
  }
}
