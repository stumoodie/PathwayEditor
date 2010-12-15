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

import java.awt.Color;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.NodeCursor;
import y.base.Node;
import y.base.DataProvider;
import y.layout.LabelLayoutConstants;
import y.layout.LabelRanking;
import y.layout.OrientationLayouter;
import y.layout.LayoutGraph;
import y.layout.PortConstraintKeys;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.orthogonal.DirectedOrthogonalLayouter;
import y.option.ArrowCellRenderer;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.StrokeCellRenderer;
import y.util.DataProviderAdapter;
import y.util.pq.BHeapIntNodePQ;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Arrow;
import y.view.LineType;


/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.orthogonal.DirectedOrthogonalLayouter}.
 */
public class DirectedOrthogonalLayoutModule extends LayoutModule
{
  private static final String DIRECTED_ORTHOGONAL_LAYOUTER = "DIRECTED_ORTHOGONAL_LAYOUTER";

  private static final String LAYOUT = "LAYOUT";

  private static final String USE_EXISTING_DRAWING_AS_SKETCH = "USE_EXISTING_DRAWING_AS_SKETCH";
  private static final String GRID = "GRID";

  private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  private static final String BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";

  private static final String ORIENTATION = "ORIENTATION";

  private static final  String[] orientEnum = {
    TOP_TO_BOTTOM,
    LEFT_TO_RIGHT,
    BOTTOM_TO_TOP,
    RIGHT_TO_LEFT
  };

  private static final String AUTO_GROUP_DIRECTED_EDGES = "AUTO_GROUP_DIRECTED_EDGES";

  private static final String EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  private static final String EDGE_LABELING = "EDGE_LABELING";
  private static final String LABELING = "LABELING";
  private static final String GENERIC = "GENERIC";
  private static final String NONE = "NONE";
  private static final String INTEGRATED = "INTEGRATED";
  private static final String FREE = "FREE";
  private static final String SIDE_SLIDER = "SIDE_SLIDER";
  private static final String CENTER_SLIDER = "CENTER_SLIDER";
  private static final String AS_IS = "AS_IS";
  private static final String BEST = "BEST";
  private static final String CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";

  private static final String IDENTIFY_DIRECTED_EDGES = "IDENTIFY_DIRECTED_EDGES";
  private static final String USE_AS_CRITERIA = "USE_AS_CRITERIA";
  private static final String LINE_COLOR = "LINE_COLOR";
  private static final String TARGET_ARROW = "TARGET_ARROW";
  private static final String LINE_TYPE = "LINE_TYPE";

  private static final String[] edgeLabeling = {
    NONE,
    INTEGRATED,
    GENERIC
  };

  private static final String[] edgeLabelModel = {
    BEST,
    AS_IS,
    CENTER_SLIDER,
    SIDE_SLIDER,
    FREE,
  };

  public DirectedOrthogonalLayoutModule()
  {
    super (DIRECTED_ORTHOGONAL_LAYOUTER,"yFiles Layout Team",
           "Directed Orthogonal Layouter");
    setPortIntersectionCalculatorEnabled(true);
  }

  public OptionHandler createOptionHandler()
  {
    OptionHandler op = new OptionHandler(getModuleName());
    op.useSection(LAYOUT);
    op.addInt(GRID,25)
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    op.addEnum(ORIENTATION, orientEnum, 0);

    op.addBool(USE_EXISTING_DRAWING_AS_SKETCH, false);

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, IDENTIFY_DIRECTED_EDGES);
    OptionItem oi = op.addEnum(USE_AS_CRITERIA, new String[]{LINE_COLOR, TARGET_ARROW, LINE_TYPE}, 0);
    og.addItem(oi);
    oi = op.addColor(LINE_COLOR, Color.red, true);
    og.addItem(oi);
    EnumOptionItem eoi;
    eoi = new EnumOptionItem(TARGET_ARROW,
                             Arrow.availableArrows().toArray(),
                             Arrow.STANDARD);
    eoi.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER,
                     new ArrowCellRenderer());
    eoi.setUsingIntegers(true);
    op.addItem(eoi);
    og.addItem(eoi);
    eoi = new EnumOptionItem(LINE_TYPE,
                             LineType.availableLineTypes().toArray(),
                             LineType.LINE_2);
    eoi.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER,
                     new StrokeCellRenderer());
    eoi.setUsingIntegers(true);
    op.addItem(eoi);
    og.addItem(eoi);

    ConstraintManager cm = new ConstraintManager(op);

    cm.setEnabledOnValueEquals(USE_AS_CRITERIA, LINE_COLOR, LINE_COLOR);
    cm.setEnabledOnValueEquals(USE_AS_CRITERIA, TARGET_ARROW, TARGET_ARROW);
    cm.setEnabledOnValueEquals(USE_AS_CRITERIA, LINE_TYPE, LINE_TYPE);

    op.addBool(AUTO_GROUP_DIRECTED_EDGES, true);

    cm.setEnabledOnValueEquals(USE_EXISTING_DRAWING_AS_SKETCH, Boolean.FALSE,
                               AUTO_GROUP_DIRECTED_EDGES);

    op.useSection(LABELING);
    og = new OptionGroup();
    cm.setEnabledOnValueEquals(op.addEnum(EDGE_LABELING, edgeLabeling, 0), NONE,
                               og.addItem(op.addEnum(EDGE_LABEL_MODEL, edgeLabelModel, 0)),
                               true);
    og.addItem(op.addBool(CONSIDER_NODE_LABELS, false));


    return op;
  }

  public void mainrun()
  {
    final OptionHandler op = getOptionHandler();

    final Graph2D graph = getGraph2D();

    DataProvider upwardDP = null;
    if(graph.getDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY) == null) {
      //determine upward edges if not already marked.
      upwardDP = new DataProviderAdapter() {
        public boolean getBool(Object o) {
          EdgeRealizer er = graph.getRealizer((Edge)o);
          if(op.get(USE_AS_CRITERIA).equals(LINE_COLOR)) {
            Color c1 = (Color)op.get(LINE_COLOR);
            Color c2 = er.getLineColor();
            return c1 != null && c1.equals(c2);
          }
          else if(op.get(USE_AS_CRITERIA).equals(TARGET_ARROW)) {
            Arrow a1 = (Arrow)op.get(TARGET_ARROW);
            Arrow a2 = er.getTargetArrow();
            return a1 != null && a1.equals(a2);
          }
          else if (op.get(USE_AS_CRITERIA).equals(LINE_TYPE)) {
            LineType l1 = (LineType) op.get(LINE_TYPE);
            LineType l2 = er.getLineType();
            return l1 != null && l1.equals(l2);
          }
          return false;
        }
      };
      graph.addDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY, upwardDP);
    }

    DataProvider sgDPOrig = null, tgDPOrig = null;
    EdgeMap sgMap = null, tgMap = null;
    if(op.getBool(AUTO_GROUP_DIRECTED_EDGES)) {
      sgDPOrig = graph.getDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
      tgDPOrig = graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
      sgMap = graph.createEdgeMap();
      tgMap = graph.createEdgeMap();
      graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sgMap);
      graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tgMap);
      autoGroupEdges(graph, sgMap, tgMap, upwardDP);
    }

    DirectedOrthogonalLayouter orthogonal = new DirectedOrthogonalLayouter();

    orthogonal.setGrid(op.getInt(GRID));
    orthogonal.setUseSketchDrawing(op.getBool(USE_EXISTING_DRAWING_AS_SKETCH));

    final OrientationLayouter ol = (OrientationLayouter)orthogonal.getOrientationLayouter();
    if(op.get(ORIENTATION).equals(TOP_TO_BOTTOM))
      ol.setOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    else if(op.get(ORIENTATION).equals(LEFT_TO_RIGHT))
      ol.setOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    else if(op.get(ORIENTATION).equals(BOTTOM_TO_TOP))
      ol.setOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    else if(op.get(ORIENTATION).equals(RIGHT_TO_LEFT))
      ol.setOrientation(OrientationLayouter.RIGHT_TO_LEFT);

    ////////////////////////////////////////////////////////////////////////////
    // Labels
    ////////////////////////////////////////////////////////////////////////////

    if(op.getBool(CONSIDER_NODE_LABELS)) {
      orthogonal.setConsiderNodeLabelsEnabled(true);
    }

    String el = op.getString(EDGE_LABELING);
    orthogonal.setIntegratedEdgeLabelingEnabled(el.equals(INTEGRATED));
    orthogonal.setConsiderNodeLabelsEnabled(op.getBool(CONSIDER_NODE_LABELS));
     if (!el.equals(NONE)) {
      setupEdgeLabelModel(el, op.getString(EDGE_LABEL_MODEL));
    } else if (!op.getBool(CONSIDER_NODE_LABELS)) {
      orthogonal.setLabelLayouterEnabled(false);
    }
    
    try {
      launchLayouter(orthogonal, true);
      if (el.equals(GENERIC)) {
        GreedyMISLabeling la = new GreedyMISLabeling();
        la.setPlaceNodeLabels(false);
        la.setPlaceEdgeLabels(true);
        la.setProfitModel(new LabelRanking());        
        la.doLayout(graph);
      }
    }
    finally {
      if(op.getBool(AUTO_GROUP_DIRECTED_EDGES))
      {
        graph.removeDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
        graph.removeDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
        if(sgDPOrig != null)
          graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sgDPOrig);
        if(tgDPOrig != null)
          graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tgDPOrig);
        if (sgMap != null)
          graph.disposeEdgeMap(sgMap);
        if (tgMap != null)
          graph.disposeEdgeMap(tgMap);
      }
      if(upwardDP != null) {
        graph.removeDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY);
      }
    }
  }

  void setupEdgeLabelModel(String edgeLabeling, String edgeLabelModel) {
    if(edgeLabeling.equals(NONE) || edgeLabelModel.equals(AS_IS)) {
      return; //nothing to do
    }

    byte model = EdgeLabel.SIDE_SLIDER;
    if (edgeLabelModel.equals(CENTER_SLIDER)) {
      model = EdgeLabel.CENTER_SLIDER;
    } else if (edgeLabelModel.equals(FREE) || edgeLabelModel.equals(BEST)) {
      model = EdgeLabel.FREE;
    }

    Graph2D graph = getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        EdgeLabel el = er.getLabel(i);
        el.setModel(model);
//        el.setRotationAngle(0);
        int prefAlongEdge = el.getPreferredPlacement() & LabelLayoutConstants.PLACEMENT_ALONG_EDGE_MASK;
        int prefOnSide = el.getPreferredPlacement() & LabelLayoutConstants.PLACEMENT_ON_SIDE_OF_EDGE_MASK;
        if (model == EdgeLabel.CENTER_SLIDER && prefOnSide != LabelLayoutConstants.PLACE_ON_EDGE) {
          el.setPreferredPlacement((byte) (LabelLayoutConstants.PLACE_ON_EDGE | prefAlongEdge));
        } else if(model == EdgeLabel.SIDE_SLIDER && prefOnSide == LabelLayoutConstants.PLACE_ON_EDGE) {
          el.setPreferredPlacement((byte) (LabelLayoutConstants.PLACE_RIGHT_OF_EDGE | prefAlongEdge));
        }
      }
    }
  }

  /**
   * Automatically groups edges either on their source or target side, but never on
   * both sides at the same time.
   * @param graph input graph
   * @param sgMap source group id map
   * @param tgMap target group id map
   */
  void autoGroupEdges(LayoutGraph graph, EdgeMap sgMap, EdgeMap tgMap, DataProvider positiveDP) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      sgMap.set(ec.edge(), null);
      tgMap.set(ec.edge(), null);
    }

    BHeapIntNodePQ sourceGroupPQ = new BHeapIntNodePQ(graph);
    BHeapIntNodePQ targetGroupPQ = new BHeapIntNodePQ(graph);
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      int outDegree = 0;
      for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
        if (positiveDP.getBool(ec.edge()) && !ec.edge().isSelfLoop())
          outDegree++;
      }
      sourceGroupPQ.add(n, -outDegree);
      int inDegree = 0;
      for (EdgeCursor ec = n.inEdges(); ec.ok(); ec.next()) {
        if (positiveDP.getBool(ec.edge()) && !ec.edge().isSelfLoop())
          inDegree++;
      }
      targetGroupPQ.add(n, -inDegree);
    }

    while (!sourceGroupPQ.isEmpty() && !targetGroupPQ.isEmpty()) {
      int bestIn = 0, bestOut = 0;
      if (!sourceGroupPQ.isEmpty()) {
        bestOut = -sourceGroupPQ.getMinPriority();
      }
      if (!targetGroupPQ.isEmpty()) {
        bestIn = -targetGroupPQ.getMinPriority();
      }
      if (bestIn > bestOut) {
        Node n = targetGroupPQ.removeMin();
        for (EdgeCursor ec = n.inEdges(); ec.ok(); ec.next()) {
          Edge e = ec.edge();
          if (sgMap.get(e) == null && positiveDP.getBool(e) && !e.isSelfLoop()) {
            tgMap.set(e, n);
            sourceGroupPQ.changePriority(e.source(), sourceGroupPQ.getPriority(e.source()) + 1);
          }
        }
      } else {
        Node n = sourceGroupPQ.removeMin();
        for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
          Edge e = ec.edge();
          if (tgMap.get(e) == null && positiveDP.getBool(e) && !e.isSelfLoop()) {
            sgMap.set(e, n);
            targetGroupPQ.increasePriority(e.target(), targetGroupPQ.getPriority(e.target()) + 1);
          }
        }
      }
    }
  }
}
