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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;

import y.layout.CompositeLayoutStage;
import y.layout.LabelLayoutConstants;
import y.layout.LabelLayoutDataRefinement;
import y.layout.LabelLayoutTranslator;
import y.layout.LabelRanking;
import y.layout.LayoutOrientation;
import y.layout.LayoutStage;
import y.layout.OrientationLayouter;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.grouping.FixedGroupLayoutStage;
import y.layout.grouping.GroupNodeHider;
import y.layout.hierarchic.BFSLayerer;
import y.layout.hierarchic.ClassicLayerSequencer;
import y.layout.hierarchic.HierarchicGroupLayouter;
import y.layout.hierarchic.HierarchicLayouter;
import y.layout.hierarchic.LayerSequencer;
import y.layout.labeling.GreedyMISLabeling;

import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;

import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.util.DataProviderAdapter;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.hierarchic.HierarchicLayouter}
 * and {@link y.layout.hierarchic.HierarchicGroupLayouter}.
 *
 */
public class HierarchicLayoutModule extends LayoutModule
{  
  private static final String EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  private static final String EDGE_LABELING = "EDGE_LABELING";
  private static final String LABELING = "LABELING";
  private static final String REMOVE_FALSE_CROSSINGS = "REMOVE_FALSE_CROSSINGS";
  private static final String USE_TRANSPOSITION = "USE_TRANSPOSITION";
  private static final String WEIGHT_HEURISTIC = "WEIGHT_HEURISTIC";
  private static final String NODE_ORDER = "NODE_ORDER";
  private static final String RANDOMIZATION_ROUNDS = "RANDOMIZATION_ROUNDS";
  private static final String RANKING_POLICY = "RANKING_POLICY";
  private static final String NODE_RANK = "NODE_RANK";
  private static final String ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  private static final String BACKLOOP_ROUTING = "BACKLOOP_ROUTING";
  private static final String EDGE_ROUTING = "EDGE_ROUTING";
  private static final String NODE_PLACEMENT = "NODE_PLACEMENT";
  private static final String ORIENTATION = "ORIENTATION";
  private static final String MAXIMAL_DURATION = "MAXIMAL_DURATION";
  private static final String MINIMAL_EDGE_DISTANCE = "MINIMAL_EDGE_DISTANCE";
  private static final String MINIMAL_FIRST_SEGMENT_LENGTH = "MINIMAL_FIRST_SEGMENT_LENGTH";
  private static final String MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  private static final String MINIMAL_LAYER_DISTANCE = "MINIMAL_LAYER_DISTANCE";
  private static final String LAYOUT = "LAYOUT";
  private static final String HIERARCHIC = "HIERARCHIC";
  private static final String FREE = "FREE";
  private static final String SIDE_SLIDER = "SIDE_SLIDER";
  private static final String CENTER_SLIDER = "CENTER_SLIDER";
  private static final String AS_IS = "AS_IS";
  private static final String BEST = "BEST";
  private static final String GENERIC = "GENERIC";
  private static final String NONE = "NONE";
  private static final String ORTHOGONAL = "ORTHOGONAL";
  private static final String POLYLINE = "POLYLINE";
  private static final String TREE = "TREE";
  private static final String LINEAR_SEGMENTS = "LINEAR_SEGMENTS";
  private static final String PENDULUM = "PENDULUM";
  private static final String MEDIAN = "MEDIAN";
  private static final String BARYCENTER = "BARYCENTER";
  private static final String SIMPLEX = "SIMPLEX";
  private static final String MEDIAN_SIMPLEX = "MEDIAN_SIMPLEX";
  private static final String TIGHT_TREE = "TIGHT_TREE";
  private static final String DOWNSHIFT_NODES = "DOWNSHIFT_NODES";
  private static final String NO_RERANKING = "NO_RERANKING";
  private static final String BFS          = "BFS";
  
  private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  private static final String BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  
  private static final String GROUPING      = "GROUPING";
  private static final String GROUP_POLICY  = "GROUP_LAYOUT_POLICY";
  private static final String IGNORE_GROUPS = "IGNORE_GROUPS";
  private static final String LAYOUT_GROUPS = "LAYOUT_GROUPS";
  private static final String FIX_GROUPS    = "FIX_GROUPS";
  private static final String ENABLE_GLOBAL_SEQUENCING = "ENABLE_GLOBAL_SEQUENCING";
  
  
  private static final  String[] orientEnum = {
    TOP_TO_BOTTOM, 
    LEFT_TO_RIGHT,
    BOTTOM_TO_TOP,
    RIGHT_TO_LEFT
  };
  
  private static final String[] topoLayerPolicy = {
    NO_RERANKING,
    DOWNSHIFT_NODES,
    TIGHT_TREE,
    SIMPLEX,
    AS_IS,
    BFS
  };
  
  private static final String[] weightHeuristic = { 
    BARYCENTER,
    MEDIAN
  };
  
  private static final String[] layoutStyles = {
    PENDULUM,
    LINEAR_SEGMENTS,
    POLYLINE,
    TREE,
    SIMPLEX,
    MEDIAN_SIMPLEX,
  };
  
  private static final String[] routingStyles = {
    POLYLINE,
    ORTHOGONAL
  };

  private static final String[] edgeLabeling = {
    NONE,
    HIERARCHIC,
    GENERIC
  };
  
  private static final String[] edgeLabelModel = {
    BEST,
    AS_IS,
    CENTER_SLIDER,
    SIDE_SLIDER,
    FREE,
  };
  
  
  private HierarchicGroupLayouter hierarchic;
  
  public HierarchicLayoutModule()
  {
    super (HIERARCHIC,"yFiles Layout Team",
           "Sugiyama based layout");
    setPortIntersectionCalculatorEnabled(true);
  }
  
  public OptionHandler createOptionHandler()
  {
    createHierarchic();
    
    OptionHandler op = new OptionHandler(getModuleName());

    op.useSection(LAYOUT);
    op.addInt(MINIMAL_LAYER_DISTANCE, (int)hierarchic.getMinimalLayerDistance());
    op.addInt(MINIMAL_NODE_DISTANCE, (int)hierarchic.getMinimalNodeDistance());
    op.addInt(MINIMAL_EDGE_DISTANCE, (int)hierarchic.getMinimalEdgeDistance());
    op.addInt(MINIMAL_FIRST_SEGMENT_LENGTH, (int)hierarchic.getMinimalFirstSegmentLength());
    op.addInt(MAXIMAL_DURATION,5);
    op.addEnum(ORIENTATION, orientEnum, 0);
    op.addEnum(NODE_PLACEMENT,layoutStyles ,hierarchic.getLayoutStyle());
    op.addEnum(EDGE_ROUTING  ,routingStyles,hierarchic.getRoutingStyle());
    op.addBool(BACKLOOP_ROUTING, false);
    op.addBool(ACT_ON_SELECTION_ONLY,false);
    
    op.useSection(NODE_RANK);
    op.addEnum(RANKING_POLICY, topoLayerPolicy, 2); 
    
    op.useSection(NODE_ORDER);
    ClassicLayerSequencer sequencer = new ClassicLayerSequencer();
    op.addEnum(WEIGHT_HEURISTIC, weightHeuristic, sequencer.getWeightHeuristic());
    op.addBool(USE_TRANSPOSITION, sequencer.getUseTransposition());
    op.addBool(REMOVE_FALSE_CROSSINGS, hierarchic.getRemoveFalseCrossings());
    op.addInt(RANDOMIZATION_ROUNDS, sequencer.getRandomizationRounds());

    op.useSection(LABELING);
    ConstraintManager cm = new ConstraintManager(op);
    cm.setEnabledOnValueEquals(op.addEnum(EDGE_LABELING, edgeLabeling, 0), NONE,
        op.addEnum(EDGE_LABEL_MODEL, edgeLabelModel, 0), true);

    op.useSection(GROUPING);
    String[] gEnum = { LAYOUT_GROUPS, FIX_GROUPS, IGNORE_GROUPS };
    op.addEnum(GROUP_POLICY, gEnum, 0);
    op.addBool(ENABLE_GLOBAL_SEQUENCING, true);
    return op;
  }
  
  public void mainrun()
  {
    createHierarchic();
    
    final Graph2D graph = getGraph2D();
    
    OptionHandler op = getOptionHandler();
    hierarchic.setRemoveFalseCrossings(op.getBool(REMOVE_FALSE_CROSSINGS));
    hierarchic.setMaximalDuration(op.getInt(MAXIMAL_DURATION)*1000);
    hierarchic.setMinimalNodeDistance(op.getInt(MINIMAL_NODE_DISTANCE));
    hierarchic.setMinimalEdgeDistance(op.getInt(MINIMAL_EDGE_DISTANCE));
    hierarchic.setMinimalFirstSegmentLength(op.getInt(MINIMAL_FIRST_SEGMENT_LENGTH));
    hierarchic.setMinimalLayerDistance(op.getInt(MINIMAL_LAYER_DISTANCE));
    
    final OrientationLayouter ol = (OrientationLayouter)hierarchic.getOrientationLayouter();
    if(op.get(ORIENTATION).equals(TOP_TO_BOTTOM))
      ol.setOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    else if(op.get(ORIENTATION).equals(LEFT_TO_RIGHT))
      ol.setOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    else if(op.get(ORIENTATION).equals(BOTTOM_TO_TOP))
      ol.setOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    else if(op.get(ORIENTATION).equals(RIGHT_TO_LEFT))
      ol.setOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    
    if (hierarchic instanceof HierarchicGroupLayouter){
      ((HierarchicGroupLayouter) hierarchic).setGlobalSequencingActive(op.getBool(GROUPING, ENABLE_GLOBAL_SEQUENCING));
    }
    
    String el = op.getString(EDGE_LABELING);
    if(!el.equals(NONE))
    {
      setupEdgeLabelModel(el, op.getString(EDGE_LABEL_MODEL));
      if(el.equals(GENERIC))
      {
        GreedyMISLabeling la = new GreedyMISLabeling();
        la.setPlaceNodeLabels(false);
        la.setPlaceEdgeLabels(true);
        la.setProfitModel(new LabelRanking());
        hierarchic.setLabelLayouter(la);
        hierarchic.setLabelLayouterEnabled(true);
      }
      else if(el.equals(HIERARCHIC))
      {
        CompositeLayoutStage ll = new CompositeLayoutStage();
        ll.appendStage(new LabelLayoutTranslator());
        ll.appendStage(new LabelLayoutDataRefinement());
        hierarchic.setLabelLayouter(ll);
        hierarchic.setLabelLayouterEnabled(true);
      }
    }
    else
    {
      hierarchic.setLabelLayouterEnabled(false);
    }
    
    
    String ls = op.getString(NODE_PLACEMENT);
    if(ls.equals(PENDULUM))
      hierarchic.setLayoutStyle(HierarchicLayouter.PENDULUM);
    else if(ls.equals(POLYLINE))
      hierarchic.setLayoutStyle(HierarchicLayouter.POLYLINE);
    else if(ls.equals(LINEAR_SEGMENTS))
      hierarchic.setLayoutStyle(HierarchicLayouter.LINEAR_SEGMENTS);
    else if(ls.equals(TREE))
      hierarchic.setLayoutStyle(HierarchicLayouter.TREE);
    else if(ls.equals(SIMPLEX))
      hierarchic.setLayoutStyle(HierarchicLayouter.SIMPLEX);
    else if(ls.equals(MEDIAN_SIMPLEX))
      hierarchic.setLayoutStyle(HierarchicLayouter.MEDIAN_SIMPLEX);
    String rs = op.getString(EDGE_ROUTING);
    if(rs.equals(POLYLINE))
      hierarchic.setRoutingStyle(HierarchicLayouter.ROUTE_POLYLINE);
    else if(rs.equals(ORTHOGONAL))
      hierarchic.setRoutingStyle(HierarchicLayouter.ROUTE_ORTHOGONAL);
    
       
    hierarchic.setSubgraphLayouterEnabled(op.getBool(ACT_ON_SELECTION_ONLY));
    
    String rp = op.getString(RANKING_POLICY);
    
    if(rp.equals(AS_IS))
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_FROM_SKETCH);
    else if(rp.equals(SIMPLEX))
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_OPTIMAL);
    else if(rp.equals(NO_RERANKING))
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_TOPMOST);
    else if (rp.equals(DOWNSHIFT_NODES))
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_DOWNSHIFT);
    else if (rp.equals(TIGHT_TREE))
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_TIGHT_TREE);
    else if(rp.equals(BFS))
    {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_BFS);
      getGraph2D().addDataProvider(BFSLayerer.CORE_NODES, Selections.createSelectionNodeMap(getGraph2D()));
    }
    
    String  wh = op.getString(WEIGHT_HEURISTIC);
    
    LayerSequencer layerSequencer = hierarchic.getLayerSequencer();
    if (layerSequencer instanceof ClassicLayerSequencer){
      ClassicLayerSequencer cls = (ClassicLayerSequencer)layerSequencer;
      if(wh.equals(MEDIAN))
        cls.setWeightHeuristic(ClassicLayerSequencer.MEDIAN_HEURISTIC);
      else
        cls.setWeightHeuristic(ClassicLayerSequencer.BARYCENTER_HEURISTIC);
      cls.setUseTransposition(op.getBool(USE_TRANSPOSITION));
      cls.setRandomizationRounds(op.getInt(NODE_ORDER, RANDOMIZATION_ROUNDS));
      hierarchic.setLayerSequencer(cls);
    }

    DataProvider dp = null;
    
    DataProvider oldSdp = graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
    DataProvider oldTdp = graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);

    if(op.getBool(BACKLOOP_ROUTING))
    {
      PortConstraint spc = null, tpc = null;
      switch(ol.getOrientation()) {
      case LayoutOrientation.TOP_TO_BOTTOM:
        spc = PortConstraint.create(PortConstraint.SOUTH);
        tpc = PortConstraint.create(PortConstraint.NORTH);
        break;
      case LayoutOrientation.LEFT_TO_RIGHT:
        spc = PortConstraint.create(PortConstraint.EAST);
        tpc = PortConstraint.create(PortConstraint.WEST);
        break;
      case LayoutOrientation.RIGHT_TO_LEFT:
        spc = PortConstraint.create(PortConstraint.WEST);
        tpc = PortConstraint.create(PortConstraint.EAST);
        break;
      case LayoutOrientation.BOTTOM_TO_TOP:
        spc = PortConstraint.create(PortConstraint.NORTH);
        tpc = PortConstraint.create(PortConstraint.SOUTH);
        break;
      }
      DataProvider sdp = new BackloopConstraintDP(spc, oldSdp);
      DataProvider tdp = new BackloopConstraintDP(tpc, oldTdp);
      
      if (oldSdp != null){
        graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      }
      if (oldTdp != null){
        graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      }
      
      graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY,sdp);
      graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY,tdp);
    }
    
    if(HierarchyManager.containsGroupNodes(graph))
    {
      LayoutStage preStage = null;
      if(op.get(GROUP_POLICY).equals(IGNORE_GROUPS)) {
        preStage = new GroupNodeHider();
        hierarchic.prependStage(preStage);
      } else {
        if(op.get(GROUP_POLICY).equals(FIX_GROUPS))
        {
           FixedGroupLayoutStage fixedGroupLayoutStage = new FixedGroupLayoutStage();
           if(op.get(EDGE_ROUTING).equals(ORTHOGONAL))
           {
             fixedGroupLayoutStage.setInterEdgeRoutingStyle(FixedGroupLayoutStage.ROUTING_STYLE_ORTHOGONAL);
           }
           preStage = fixedGroupLayoutStage;
           hierarchic.prependStage(preStage);
        }
      }
      
      try
      {
        launchLayouter(hierarchic); 
      }
      finally
      {
        if(preStage != null)
        {
          hierarchic.removeStage(preStage);
        }
      }      
    }
    else
    {
      launchLayouter(hierarchic);
    }
    
    if(op.getBool(BACKLOOP_ROUTING))
    {
      graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      if (oldSdp != null){
        graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, oldSdp);
      }
      if (oldTdp != null){
        graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, oldTdp);
      }
    }
    if (dp != null){
      graph.removeDataProvider(ClassicLayerSequencer.GROUP_KEY);
    }
    
    //cleanup BFSLayerer key if present
    graph.removeDataProvider(BFSLayerer.CORE_NODES);
  }
  
  static final class BackloopConstraintDP extends DataProviderAdapter
  {
    private PortConstraint pc;
    private DataProvider delegate;
    private static final PortConstraint anySide = PortConstraint.create(PortConstraint.ANY_SIDE);
    BackloopConstraintDP(PortConstraint pc, DataProvider delegate)
    {
      this.pc = pc;
      this.delegate = delegate;
    }
    
    public Object get(Object o)
    {
      if (delegate != null){
        Object delegateResult = delegate.get(o);
        if (delegateResult != null){
          return delegateResult;
        }
      } 
      Edge e = (Edge)o;
      if(e.isSelfLoop())
      {
        return anySide;
      } else {
        return pc;
      }
    }
  }
  
  void setupEdgeLabelModel(String edgeLabeling, String edgeLabelModel)
  {
    if(edgeLabeling.equals(NONE) || edgeLabelModel.equals(AS_IS))
    {
      return; //nothing to do
    }
    
    if(edgeLabelModel.equals(BEST))
    {
      if(edgeLabeling.equals(GENERIC))
        edgeLabelModel = SIDE_SLIDER;
      else if(edgeLabeling.equals(HIERARCHIC))
        edgeLabelModel = FREE;
    }
    
    byte model = EdgeLabel.SIDE_SLIDER;
    int preferredSide = LabelLayoutConstants.PLACE_RIGHT_OF_EDGE;
    if(edgeLabelModel.equals(CENTER_SLIDER))
    {
      model = EdgeLabel.CENTER_SLIDER;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    }
    else if(edgeLabelModel.equals(FREE))
    {
      model = EdgeLabel.FREE;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    }
    
    Graph2D graph = getGraph2D();
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for(int i = 0; i < er.labelCount(); i++)
      {
        EdgeLabel el = er.getLabel(i);
        el.setModel(model);
        int prefAlongEdge = el.getPreferredPlacement() & LabelLayoutConstants.PLACEMENT_ALONG_EDGE_MASK;
        el.setPreferredPlacement((byte)(preferredSide | prefAlongEdge));
      }
    }
  }
  
  
  public void dispose()
  {
    hierarchic = null;
  }
  
  private void createHierarchic()
  {
    if(hierarchic == null) 
    {
      hierarchic = new HierarchicGroupLayouter(); 
    }
  }
}

