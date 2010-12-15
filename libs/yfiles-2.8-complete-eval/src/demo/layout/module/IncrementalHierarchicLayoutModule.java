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


import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.NodeCursor;
import y.layout.CanonicMultiStageLayouter;
import y.layout.LabelLayoutConstants;
import y.layout.LabelRanking;
import y.layout.OrientationLayouter;
import y.layout.hierarchic.AsIsLayerer;
import y.layout.hierarchic.BFSLayerer;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.OldLayererWrapper;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.hierarchic.incremental.TopLevelGroupToSwimlaneStage;
import y.layout.labeling.GreedyMISLabeling;
import y.option.ConstraintManager;
import y.option.ConstraintManager.Condition;
import y.option.DefaultEditorFactory;
import y.option.DoubleOptionItem;
import y.option.EnumOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.Maps;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Selections;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.hierarchic.IncrementalHierarchicLayouter}.
 *
 */
public class IncrementalHierarchicLayoutModule extends LayoutModule {
  private static final String INCREMENTAL_HIERARCHIC = "INCREMENTAL_HIERARCHIC";

  private static final String GENERAL = "GENERAL";
  private static final String INTERACTION = "INTERACTION";
  private static final String SELECTED_ELEMENTS_INCREMENTALLY = "SELECTED_ELEMENTS_INCREMENTALLY";
  private static final String USE_DRAWING_AS_SKETCH = "USE_DRAWING_AS_SKETCH";
  private static final String ORIENTATION = "ORIENTATION";
  private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  private static final String BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  private static final String LAYOUT_COMPONENTS_SEPARATELY = "LAYOUT_COMPONENTS_SEPARATELY";
  private static final String SYMMETRIC_PLACEMENT = "SYMMETRIC_PLACEMENT";
  private static final String MINIMUM_DISTANCES = "MINIMUM_DISTANCES";
  private static final String NODE_TO_NODE_DISTANCE = "NODE_TO_NODE_DISTANCE";
  private static final String NODE_TO_EDGE_DISTANCE = "NODE_TO_EDGE_DISTANCE";
  private static final String EDGE_TO_EDGE_DISTANCE = "EDGE_TO_EDGE_DISTANCE";
  private static final String MINIMUM_LAYER_DISTANCE = "MINIMUM_LAYER_DISTANCE";
  private static final String MAXIMAL_DURATION = "MAXIMAL_DURATION";

  private static final String EDGE_SETTINGS = "EDGE_SETTINGS";
  private static final String EDGE_ROUTING = "EDGE_ROUTING";
  private static final String EDGE_ROUTING_ORTHOGONAL = "EDGE_ROUTING_ORTHOGONAL";
  private static final String EDGE_ROUTING_POLYLINE = "EDGE_ROUTING_POLYLINE";
  private static final String BACKLOOP_ROUTING = "BACKLOOP_ROUTING";
  private static final String MINIMUM_FIRST_SEGMENT_LENGTH = "MINIMUM_FIRST_SEGMENT_LENGTH";
  private static final String MINIMUM_LAST_SEGMENT_LENGTH = "MINIMUM_LAST_SEGMENT_LENGTH";
  private static final String MINIMUM_EDGE_LENGTH = "MINIMUM_EDGE_LENGTH";
  private static final String MINIMUM_EDGE_DISTANCE = "MINIMUM_EDGE_DISTANCE";
  private static final String MINIMUM_SLOPE = "MINIMUM_SLOPE";
  private static final String PC_OPTIMIZATION_ENABLED = "PC_OPTIMIZATION_ENABLED";
  private static final String AUTOMATIC_EDGE_GROUPING_ENABLED = "AUTOMATIC_EDGE_GROUPING_ENABLED";

  private static final String RANKS = "RANKS";
  private static final String RANKING_POLICY = "RANKING_POLICY";
  private static final String HIERARCHICAL_OPTIMAL = "HIERARCHICAL_OPTIMAL";
  private static final String HIERARCHICAL_TIGHT_TREE_HEURISTIC = "HIERARCHICAL_TIGHT_TREE_HEURISTIC";
  private static final String HIERARCHICAL_TOPMOST = "HIERARCHICAL_TOPMOST";
  private static final String BFS_LAYERS = "BFS_LAYERS";
  private static final String FROM_SKETCH = "FROM_SKETCH";
  private static final String LAYER_ALIGNMENT = "LAYER_ALIGNMENT";
  private static final String TOP = "TOP";
  private static final String CENTER = "CENTER";
  private static final String BOTTOM = "BOTTOM";
  private static final String FROM_SKETCH_PROPERTIES = "FROM_SKETCH_PROPERTIES";
  private static final String SCALE = "SCALE";
  private static final String HALO = "HALO";
  private static final String MINIMUM_SIZE = "MINIMUM_SIZE";
  private static final String MAXIMUM_SIZE = "MAXIMUM_SIZE";
  private static final String COMPONENT_ARRANGEMENT_POLICY = "COMPONENT_ARRANGEMENT_POLICY";
  private static final String POLICY_TOPMOST = "POLICY_TOPMOST";
  private static final String POLICY_COMPACT = "POLICY_COMPACT";

  private static final String LABELING = "LABELING";
  private static final String NODE_PROPERTIES = "NODE_PROPERTIES";
  private static final String CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  private static final String EDGE_PROPERTIES = "EDGE_PROPERTIES";
  private static final String EDGE_LABELING = "EDGE_LABELING";
  private static final String EDGE_LABELING_NONE = "EDGE_LABELING_NONE";
  private static final String EDGE_LABELING_HIERARCHIC = "EDGE_LABELING_HIERARCHIC";
  private static final String EDGE_LABELING_GENERIC = "EDGE_LABELING_GENERIC";
  private static final String EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  private static final String EDGE_LABEL_MODEL_FREE = "EDGE_LABEL_MODEL_FREE";
  private static final String EDGE_LABEL_MODEL_BEST = "EDGE_LABEL_MODEL_BEST";
  private static final String EDGE_LABEL_MODEL_AS_IS = "EDGE_LABEL_MODEL_AS_IS";
  private static final String EDGE_LABEL_MODEL_SIDE_SLIDER = "EDGE_LABEL_MODEL_SIDE_SLIDER";
  private static final String EDGE_LABEL_MODEL_CENTER_SLIDER = "EDGE_LABEL_MODEL_CENTER_SLIDER";

  private static final String GROUPING = "GROUPING";
  private static final String SWIMLANES = "SWIMLANES";
  private static final String TREAT_ROOT_GROUPS_AS_SWIMLANES = "TREAT_ROOT_GROUPS_AS_SWIMLANES";
  private static final String USE_ORDER_FROM_SKETCH = "USE_ORDER_FROM_SKETCH";
  private static final String SWIMLANE_SPACING = "SWIMLANE_SPACING";
  private static final String GROUP_LAYERING_STRATEGY = "GROUP_LAYERING_STRATEGY";
  private static final String GLOBAL_LAYERING = "GLOBAL_LAYERING";
  private static final String RECURSIVE_LAYERING = "RECURSIVE_LAYERING";
  private static final String GROUP_ALIGNMENT = "GROUP_ALIGNMENT";
  private static final String GROUP_ALIGN_TOP = "GROUP_ALIGN_TOP";
  private static final String GROUP_ALIGN_CENTER = "GROUP_ALIGN_CENTER";
  private static final String GROUP_ALIGN_BOTTOM = "GROUP_ALIGN_BOTTOM";

  private static final String GROUP_ENABLE_COMPACTION = "GROUP_ENABLE_COMPACTION";
  private static final String GROUP_HORIZONTAL_COMPACTION = "GROUP_HORIZONTAL_COMPACTION";
  private static final String GROUP_HORIZONTAL_COMPACTION_NONE = "GROUP_HORIZONTAL_COMPACTION_NONE";
  private static final String GROUP_HORIZONTAL_COMPACTION_MAX = "GROUP_HORIZONTAL_COMPACTION_MAX";

  private static final Object[] edgeRoutingEnum = new Object[]{EDGE_ROUTING_ORTHOGONAL, EDGE_ROUTING_POLYLINE};

  private static final Object[] orientEnum = {TOP_TO_BOTTOM, LEFT_TO_RIGHT, BOTTOM_TO_TOP, RIGHT_TO_LEFT};

  private static final Object[] alignmentEnum = {TOP, CENTER, BOTTOM};
  private static final Object[] componentAlignmentEnum = {POLICY_COMPACT, POLICY_TOPMOST};
  private static final String[] rankingPolicies = {HIERARCHICAL_OPTIMAL, HIERARCHICAL_TIGHT_TREE_HEURISTIC, BFS_LAYERS,
      FROM_SKETCH, HIERARCHICAL_TOPMOST};

  private static final String[] edgeLabeling = {EDGE_LABELING_NONE, EDGE_LABELING_GENERIC, EDGE_LABELING_HIERARCHIC};

  private static final String[] edgeLabelModel = {
      EDGE_LABEL_MODEL_BEST,
      EDGE_LABEL_MODEL_AS_IS,
      EDGE_LABEL_MODEL_CENTER_SLIDER,
      EDGE_LABEL_MODEL_SIDE_SLIDER,
      EDGE_LABEL_MODEL_FREE,
  };

  private static final Object[] groupStrategyEnum = {GLOBAL_LAYERING, RECURSIVE_LAYERING};
  private static final Object[] groupAlignmentEnum = {GROUP_ALIGN_TOP, GROUP_ALIGN_CENTER, GROUP_ALIGN_BOTTOM};
  private static final Object[] groupHorizCompactionEnum = {GROUP_HORIZONTAL_COMPACTION_NONE, GROUP_HORIZONTAL_COMPACTION_MAX};

  public IncrementalHierarchicLayoutModule() {
    super(INCREMENTAL_HIERARCHIC, "yFiles Layout Team", "A sophisticated hierarchic layout algorithm");
    setPortIntersectionCalculatorEnabled(true);
  }

  public OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());

    OptionGroup og;

    op.useSection(GENERAL);

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, INTERACTION);

    og.addItem(op.addBool(SELECTED_ELEMENTS_INCREMENTALLY, false));
    OptionItem useDrawingItem = og.addItem(op.addBool(USE_DRAWING_AS_SKETCH, false));

    op.addEnum(ORIENTATION, orientEnum, 0);

    op.addBool(LAYOUT_COMPONENTS_SEPARATELY, false);
    op.addBool(SYMMETRIC_PLACEMENT, true);
    op.addInt(MAXIMAL_DURATION, 5);

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, MINIMUM_DISTANCES);
    og.addItem(op.addDouble(NODE_TO_NODE_DISTANCE, 30.0d));
    og.addItem(op.addDouble(NODE_TO_EDGE_DISTANCE, 15.0d));
    og.addItem(op.addDouble(EDGE_TO_EDGE_DISTANCE, 15.0d));
    og.addItem(op.addDouble(MINIMUM_LAYER_DISTANCE, 10.0d));

    op.useSection(EDGE_SETTINGS);

    EnumOptionItem eoi = op.addEnum(EDGE_ROUTING, edgeRoutingEnum, 0);
    eoi.setAttribute(DefaultEditorFactory.ATTRIBUTE_ENUM_STYLE,
        DefaultEditorFactory.STYLE_RADIO_BUTTONS);
    eoi.setAttribute(DefaultEditorFactory.ATTRIBUTE_ENUM_ALIGNMENT,
        DefaultEditorFactory.ALIGNMENT_VERTICAL);

    op.addBool(BACKLOOP_ROUTING, false);
    op.addBool(AUTOMATIC_EDGE_GROUPING_ENABLED, false);
    op.addDouble(MINIMUM_FIRST_SEGMENT_LENGTH, 10.0d);
    op.addDouble(MINIMUM_LAST_SEGMENT_LENGTH, 15.0d);
    op.addDouble(MINIMUM_EDGE_LENGTH, 20.0d);
    op.addDouble(MINIMUM_EDGE_DISTANCE, 15.0d);

    ConstraintManager cm = new ConstraintManager(op);
    cm.setEnabledOnValueEquals(eoi, EDGE_ROUTING_POLYLINE,
        op.addDouble(MINIMUM_SLOPE, 0.25d, 0.0d, 5.0d, 2));

    op.addBool(PC_OPTIMIZATION_ENABLED, false);

    op.useSection(RANKS);
    op.addEnum(RANKING_POLICY, rankingPolicies, 0);
    op.addEnum(LAYER_ALIGNMENT, alignmentEnum, 1);
    op.addEnum(COMPONENT_ARRANGEMENT_POLICY, componentAlignmentEnum, 1);

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, FROM_SKETCH_PROPERTIES);
    og.addItem(op.addDouble(SCALE, 1.0d, 0.0d, 5.0d, 1));
    og.addItem(op.addDouble(HALO, 0.0d));
    og.addItem(op.addDouble(MINIMUM_SIZE, 0.0d));
    op.getItem(RANKS, MINIMUM_SIZE)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
    og.addItem(op.addDouble(MAXIMUM_SIZE, 1000.0d));
    op.getItem(RANKS, MAXIMUM_SIZE)
      .setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));

    Condition c =
        cm.createConditionValueEquals(USE_DRAWING_AS_SKETCH, Boolean.FALSE).and(
            cm.createConditionValueEquals(SELECTED_ELEMENTS_INCREMENTALLY, Boolean.FALSE));
    cm.setEnabledOnCondition(c, op.getItem(RANKING_POLICY));

    c = c.inverse().or(cm.createConditionValueEquals(RANKING_POLICY, FROM_SKETCH));
    cm.setEnabledOnCondition(c, og);
      
    op.useSection(LABELING);
    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, NODE_PROPERTIES);
    og.addItem(op.addBool(CONSIDER_NODE_LABELS, true));
    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, EDGE_PROPERTIES);
    og.addItem(op.addEnum(EDGE_LABELING, edgeLabeling, 0));
    cm.setEnabledOnValueEquals(op.getItem(EDGE_LABELING), EDGE_LABELING_NONE,
        og.addItem(op.addEnum(EDGE_LABEL_MODEL, edgeLabelModel, 0)), true);

    op.useSection(GROUPING);
    OptionItem groupLayeringItem = op.addEnum(GROUP_LAYERING_STRATEGY, groupStrategyEnum, 0);
    OptionItem layerCompactionItem = op.addBool(GROUP_ENABLE_COMPACTION, true);
    OptionItem groupAlignementItem = op.addEnum(GROUP_ALIGNMENT, groupAlignmentEnum, 0);
    cm.setEnabledOnValueEquals(useDrawingItem, Boolean.FALSE, groupLayeringItem);
    cm.setEnabledOnValueEquals(groupLayeringItem, RECURSIVE_LAYERING, layerCompactionItem);
    cm.setEnabledOnValueEquals(groupLayeringItem, RECURSIVE_LAYERING, groupAlignementItem);
    cm.setEnabledOnCondition(cm.createConditionValueEquals(groupLayeringItem, RECURSIVE_LAYERING).and(
        cm.createConditionValueEquals(layerCompactionItem, Boolean.TRUE).inverse()), groupAlignementItem);

    cm.setEnabledOnCondition(cm.createConditionValueEquals(groupLayeringItem, RECURSIVE_LAYERING).and(
        cm.createConditionValueEquals(useDrawingItem, Boolean.FALSE)), layerCompactionItem);
    op.addEnum(GROUP_HORIZONTAL_COMPACTION, groupHorizCompactionEnum, 1);

    op.useSection(SWIMLANES);
    final OptionItem swimlaneOption = op.addBool(TREAT_ROOT_GROUPS_AS_SWIMLANES, false);
    final OptionItem fromSketchOption = op.addBool(USE_ORDER_FROM_SKETCH, false);
    final OptionItem spacingOption = op.addDouble(SWIMLANE_SPACING, 0.0d);
    spacingOption.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
    cm.setEnabledOnValueEquals(swimlaneOption, Boolean.TRUE, fromSketchOption);
    cm.setEnabledOnValueEquals(swimlaneOption, Boolean.TRUE, spacingOption);
    return op;
  }

  public void mainrun() {
    CanonicMultiStageLayouter layouter = null;
    Graph2D graph = getGraph2D();

    OptionHandler op = getOptionHandler();

    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    layouter = ihl;
    
    //  mark incremental elements if required
    DataMap incrementalElements = null;
    boolean fromSketch = op.getBool(USE_DRAWING_AS_SKETCH);
    boolean incrementalLayout = op.getBool(SELECTED_ELEMENTS_INCREMENTALLY);
    boolean selectedElements = !Selections.isEdgeSelectionEmpty(graph) || !Selections.isNodeSelectionEmpty(graph);

    if (incrementalLayout && selectedElements) {
      // create storage for both nodes and edges
      incrementalElements = Maps.createHashedDataMap();
      // configure the mode
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
      final IncrementalHintsFactory ihf = ihl.createIncrementalHintsFactory();

      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
        incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
      }

      for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
        incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
      }
      graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
    } else if (fromSketch) {
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    } else {
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    }

    // cast to implementation simplex
    ((SimplexNodePlacer) ihl.getNodePlacer()).setBaryCenterModeEnabled(op.getBool(SYMMETRIC_PLACEMENT));    

    if (GROUP_HORIZONTAL_COMPACTION_NONE.equals(op.getString(GROUP_HORIZONTAL_COMPACTION))) {
      ((SimplexNodePlacer) ihl.getNodePlacer()).setGroupCompactionStrategy(SimplexNodePlacer.GROUP_COMPACTION_NONE);
    } else if (GROUP_HORIZONTAL_COMPACTION_MAX.equals(op.getString(GROUP_HORIZONTAL_COMPACTION))) {
      ((SimplexNodePlacer) ihl.getNodePlacer()).setGroupCompactionStrategy(SimplexNodePlacer.GROUP_COMPACTION_MAX);
    }


    ihl.setComponentLayouterEnabled(op.getBool(LAYOUT_COMPONENTS_SEPARATELY));

    ihl.setMinimumLayerDistance(op.getDouble(MINIMUM_LAYER_DISTANCE));
    ihl.setNodeToEdgeDistance(op.getDouble(NODE_TO_EDGE_DISTANCE));
    ihl.setNodeToNodeDistance(op.getDouble(NODE_TO_NODE_DISTANCE));
    ihl.setEdgeToEdgeDistance(op.getDouble(EDGE_TO_EDGE_DISTANCE));
    ihl.setAutomaticEdgeGroupingEnabled(op.getBool(AUTOMATIC_EDGE_GROUPING_ENABLED));

    final NodeLayoutDescriptor nld = ihl.getNodeLayoutDescriptor();
    final EdgeLayoutDescriptor eld = ihl.getEdgeLayoutDescriptor();

    eld.setOrthogonallyRouted(op.getEnum(EDGE_ROUTING) == 0);
    eld.setMinimumFirstSegmentLength(op.getDouble(MINIMUM_FIRST_SEGMENT_LENGTH));
    eld.setMinimumLastSegmentLength(op.getDouble(MINIMUM_LAST_SEGMENT_LENGTH));

    eld.setMinimumDistance(op.getDouble(MINIMUM_EDGE_DISTANCE));
    eld.setMinimumLength(op.getDouble(MINIMUM_EDGE_LENGTH));

    eld.setMinimumSlope(op.getDouble(MINIMUM_SLOPE));

    eld.setSourcePortOptimizationEnabled(op.getBool(PC_OPTIMIZATION_ENABLED));
    eld.setTargetPortOptimizationEnabled(op.getBool(PC_OPTIMIZATION_ENABLED));

    nld.setMinimumDistance(Math.min(ihl.getNodeToNodeDistance(), ihl.getNodeToEdgeDistance()));
    nld.setMinimumLayerHeight(0);

    if (op.get(LAYER_ALIGNMENT).equals(TOP)) {
      nld.setLayerAlignment(0.0);
    } else if (op.get(LAYER_ALIGNMENT).equals(CENTER)) {
      nld.setLayerAlignment(0.5);
    } else if (op.get(LAYER_ALIGNMENT).equals(BOTTOM)) {
      nld.setLayerAlignment(1.0);
    }

    final OrientationLayouter ol = (OrientationLayouter) ihl.getOrientationLayouter();
    if (op.get(ORIENTATION).equals(TOP_TO_BOTTOM)) {
      ol.setOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    } else if (op.get(ORIENTATION).equals(LEFT_TO_RIGHT)) {
      ol.setOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    } else if (op.get(ORIENTATION).equals(BOTTOM_TO_TOP)) {
      ol.setOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    } else if (op.get(ORIENTATION).equals(RIGHT_TO_LEFT)) {
      ol.setOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    }

    final String el = op.getString(EDGE_LABELING);
    if (!el.equals(EDGE_LABELING_NONE)) {
      setupEdgeLabelModel(el, op.getString(EDGE_LABEL_MODEL));
      if (el.equals(EDGE_LABELING_GENERIC)) {
        GreedyMISLabeling la = new GreedyMISLabeling();
        la.setPlaceNodeLabels(false);
        la.setPlaceEdgeLabels(true);
        la.setProfitModel(new LabelRanking());
        ihl.setLabelLayouter(la);
        ihl.setLabelLayouterEnabled(true);
      } else if (el.equals(EDGE_LABELING_HIERARCHIC)) {
        ihl.setIntegratedEdgeLabelingEnabled(true);
      }
    } else {
      ihl.setIntegratedEdgeLabelingEnabled(false);
    }

    if (op.getBool(CONSIDER_NODE_LABELS)) {
      ihl.setConsiderNodeLabelsEnabled(true);
      ihl.getNodeLayoutDescriptor().setNodeLabelMode(NodeLayoutDescriptor.NODE_LABEL_MODE_CONSIDER_FOR_DRAWING);
    } else {
      ihl.setConsiderNodeLabelsEnabled(false);
    }


    final String rp = op.getString(RANKING_POLICY);

    if (rp.equals(FROM_SKETCH)) {
      ihl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_FROM_SKETCH);
    } else if (rp.equals(HIERARCHICAL_OPTIMAL)) {
      ihl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL);
    } else if (rp.equals(HIERARCHICAL_TIGHT_TREE_HEURISTIC)) {
      ihl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TIGHT_TREE);
    } else if (rp.equals(HIERARCHICAL_TOPMOST)) {
      ihl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST);
    } else if (rp.equals(BFS_LAYERS)) {
      ihl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_BFS);
      getGraph2D().addDataProvider(BFSLayerer.CORE_NODES, Selections.createSelectionNodeMap(getGraph2D()));
    }

    if (op.getString(COMPONENT_ARRANGEMENT_POLICY).equals(POLICY_COMPACT)) {
      ihl.setComponentArrangementPolicy(IncrementalHierarchicLayouter.COMPONENT_ARRANGEMENT_COMPACT);
    } else {
      ihl.setComponentArrangementPolicy(IncrementalHierarchicLayouter.COMPONENT_ARRANGEMENT_TOPMOST);
    }

    //configure AsIsLayerer
    Object layerer = (ihl.getLayoutMode() == IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH) ?
        ihl.getFromScratchLayerer() : ihl.getFixedElementsLayerer();

    if (layerer instanceof OldLayererWrapper) {
      layerer = ((OldLayererWrapper) layerer).getOldLayerer();
    }
    if (layerer instanceof AsIsLayerer) {
      AsIsLayerer ail = (AsIsLayerer) layerer;
      ail.setNodeHalo(op.getDouble(HALO));
      ail.setNodeScalingFactor(op.getDouble(SCALE));
      ail.setMinimumNodeSize(op.getDouble(MINIMUM_SIZE));
      ail.setMaximumNodeSize(op.getDouble(MAXIMUM_SIZE));
    }

    if (!fromSketch && op.getString(GROUP_LAYERING_STRATEGY).equals(RECURSIVE_LAYERING)) {
      byte alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP;
      if (op.getString(GROUP_ALIGNMENT).equals(GROUP_ALIGN_CENTER)) {
        alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER;
      } else if (op.getString(GROUP_ALIGNMENT).equals(GROUP_ALIGN_BOTTOM)) {
        alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM;
      }
      ihl.setGroupCompactionEnabled(op.getBool(GROUP_ENABLE_COMPACTION));
      ihl.setGroupAlignmentPolicy(alignmentPolicy);
      ihl.setRecursiveGroupLayeringEnabled(true);
    } else {
      ihl.setRecursiveGroupLayeringEnabled(false);
    }

    if (op.getBool(SWIMLANES, TREAT_ROOT_GROUPS_AS_SWIMLANES)){
      final TopLevelGroupToSwimlaneStage stage = new TopLevelGroupToSwimlaneStage();
      stage.setFromSketchSwimlaneOrderingEnabled(op.getBool(SWIMLANES, USE_ORDER_FROM_SKETCH));
      stage.setSpacing(op.getDouble(SWIMLANES, SWIMLANE_SPACING));
      ihl.appendStage(stage);
    }

    ihl.setBackloopRoutingEnabled(op.getBool(BACKLOOP_ROUTING));
    ihl.setMaximalDuration(op.getInt(MAXIMAL_DURATION) * 1000);

    try {
      // launch layouter in buffered mode
      launchLayouter(layouter);
    } finally {
      // remove the registered DataProvider instances
      if (incrementalElements != null) {
        graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
        incrementalElements = null;
      }

      }
    }

  void setupEdgeLabelModel(String edgeLabeling, String edgeLabelModel) {
    if (edgeLabeling.equals(EDGE_LABELING_NONE) || edgeLabelModel.equals(EDGE_LABEL_MODEL_AS_IS)) {
      return; //nothing to do
    }

    if (edgeLabelModel.equals(EDGE_LABEL_MODEL_BEST)) {
      if (edgeLabeling.equals(EDGE_LABELING_GENERIC)) {
        edgeLabelModel = EDGE_LABEL_MODEL_SIDE_SLIDER;
      } else if (edgeLabeling.equals(EDGE_LABELING_HIERARCHIC)) {
        edgeLabelModel = EDGE_LABEL_MODEL_FREE;
      }
    }

    byte model = EdgeLabel.SIDE_SLIDER;
    int preferredSide = LabelLayoutConstants.PLACE_RIGHT_OF_EDGE;
    if (edgeLabelModel.equals(EDGE_LABEL_MODEL_CENTER_SLIDER)) {
      model = EdgeLabel.CENTER_SLIDER;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    } else if (edgeLabelModel.equals(EDGE_LABEL_MODEL_FREE)) {
      model = EdgeLabel.FREE;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    }

    Graph2D graph = getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        EdgeLabel el = er.getLabel(i);
        el.setModel(model);
        if (!edgeLabelModel.equals(EDGE_LABEL_MODEL_FREE)) {          
          int prefAlongEdge = el.getPreferredPlacement() & LabelLayoutConstants.PLACEMENT_ALONG_EDGE_MASK;
          el.setPreferredPlacement((byte) (preferredSide | prefAlongEdge));
        }
      }
    }
  }
}
