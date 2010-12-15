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

import y.base.Node;
import y.layout.CanonicMultiStageLayouter;
import y.layout.LayoutOrientation;
import y.layout.OrientationLayouter;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.tree.ARTreeLayouter;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.HVTreeLayouter;
import y.layout.tree.TreeLayouter;
import y.layout.tree.TreeReductionStage;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Graph2DView;

import java.awt.Dimension;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.tree.TreeLayouter}, {@link
 * y.layout.tree.BalloonLayouter}, {@link y.layout.tree.ARTreeLayouter} and {@link y.layout.tree.HVTreeLayouter}.
 *
 */
public class TreeLayoutModule extends LayoutModule {

  private static final String LAYOUT_STYLE = "LAYOUT_STYLE";
  private static final String PREFERRED_CHILD_WEDGE = "PREFERRED_CHILD_WEDGE";
  private static final String DIRECTED_ROOT = "DIRECTED_ROOT";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String ALLOW_OVERLAPS = "ALLOW_OVERLAPS";
  private static final String GENERAL = "GENERAL";

  private static final String ALLOW_NON_TREE_EDGES = "ALLOW_NON_TREES";
  private static final String ROUTING_STYLE_FOR_NON_TREE_EDGES = "ROUTING_STYLE_FOR_NON_TREE_EDGES";
  private static final String ROUTE_ORGANIC = "ROUTE_ORGANIC";
  private static final String ROUTE_ORTHOGONAL = "ROUTE_ORTHOGONAL";
  private static final String ROUTE_STRAIGHTLINE = "ROUTE_STRAIGHTLINE";

  private static final String COMPACTNESS_FACTOR = "COMPACTNESS_FACTOR";
  private static final String MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  private static final String ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  private static final String BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  private static final String BALLOON = "BALLOON";
  private static final String MINIMAL_LAYER_DISTANCE = "MINIMAL_LAYER_DISTANCE";
  private static final String ORIENTATION = "ORIENTATION";
  private static final String PREFERRED_ROOT_WEDGE = "PREFERRED_ROOT_WEDGE";
  private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  private static final String HV = "HV";
  private static final String VERTICAL_SPACE = "VERTICAL_SPACE";
  private static final String AR = "AR";
  private static final String HORIZONTAL_SPACE = "HORIZONTAL_SPACE";
  private static final String TREE = "TREE";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  private static final String MINIMAL_EDGE_LENGTH = "MINIMAL_EDGE_LENGTH";
  private static final String ROOT_NODE_POLICY = "ROOT_NODE_POLICY";
  private static final String CENTER_ROOT = "CENTER_ROOT";
  private static final String WEIGHTED_CENTER_ROOT = "WEIGHTED_CENTER_ROOT";

  private static final String BEND_DISTANCE = "BEND_DISTANCE";
  private static final String ASPECT_RATIO = "ASPECT_RATIO";
  private static final String USE_VIEW_ASPECT_RATIO = "USE_VIEW_ASPECT_RATIO";

  private static final String DIRECTED = "DIRECTED";
  private static final String ORTHOGONAL_EDGE_ROUTING = "ORTHOGONAL_EDGE_ROUTING";

  private static final String CHILD_PLACEMENT_POLICY = "CHILD_PLACEMENT_POLICY";
  private static final String SIBLINGS_ON_SAME_LAYER = "SIBLINGS_ON_SAME_LAYER";
  private static final String ALL_LEAVES_ON_SAME_LAYER = "ALL_LEAVES_ON_SAME_LAYER";
  private static final String LEAVES_STACKED = "LEAVES_STACKED";
  private static final String LEAVES_STACKED_LEFT = "LEAVES_STACKED_LEFT";
  private static final String LEAVES_STACKED_RIGHT = "LEAVES_STACKED_RIGHT";
  private static final String LEAVES_STACKED_LEFT_AND_RIGHT = "LEAVES_STACKED_LEFT_AND_RIGHT";
  private static final String[] enumLeafLayoutPolicy = {
          SIBLINGS_ON_SAME_LAYER,
          ALL_LEAVES_ON_SAME_LAYER,
          LEAVES_STACKED,
          LEAVES_STACKED_LEFT,
          LEAVES_STACKED_RIGHT,
          LEAVES_STACKED_LEFT_AND_RIGHT,
  };

  private static final String ENFORCE_GLOBAL_LAYERING = "ENFORCE_GLOBAL_LAYERING";

  private static final String INTEGRATED_EDGE_LABELING = "INTEGRATED_EDGE_LABELING";
  private static final String INTEGRATED_NODE_LABELING = "INTEGRATED_NODE_LABELING";

  private static final String VERTICAL_ALIGNMENT = "VERTICAL_ALIGNMENT";
  private static final String BUS_ALIGNMENT = "BUS_ALIGNMENT";

  private static final String BALLOON_FROM_SKETCH = "FROM_SKETCH";

  private static final String[] enumRoute = {ROUTE_ORGANIC, ROUTE_ORTHOGONAL, ROUTE_STRAIGHTLINE};

  private static final String[] enumStyle = {DIRECTED, BALLOON, HV, AR};
  private static final String[] enumOrient = {TOP_TO_BOTTOM, LEFT_TO_RIGHT,
      BOTTOM_TO_TOP, RIGHT_TO_LEFT};
  private static final String[] enumRoot = {DIRECTED_ROOT, CENTER_ROOT, WEIGHTED_CENTER_ROOT};

  private static final String PORT_STYLE = "PORT_STYLE";
  private static final String NODE_CENTER_PORTS = "NODE_CENTER";
  private static final String BORDER_CENTER_PORTS = "BORDER_CENTER";
  private static final String BORDER_DISTRIBUTED_PORTS = "BORDER_DISTRIBUTED";

  private static final String[] enumPortStyle = {
      NODE_CENTER_PORTS,
      BORDER_CENTER_PORTS,
      BORDER_DISTRIBUTED_PORTS
  };


  public TreeLayoutModule() {
    super(TREE, "yFiles Layout Team", "A layouter for tree structures");
    setPortIntersectionCalculatorEnabled(true);
  }


  /** module support */
  public OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());

    op.useSection(GENERAL);
    op.addEnum(LAYOUT_STYLE, enumStyle, 0);

    op.addBool(ALLOW_NON_TREE_EDGES, false);
    op.addEnum(ROUTING_STYLE_FOR_NON_TREE_EDGES, enumRoute, 0);
    ConstraintManager cm = new ConstraintManager(op);
    cm.setEnabledOnValueEquals(ALLOW_NON_TREE_EDGES, Boolean.TRUE, ROUTING_STYLE_FOR_NON_TREE_EDGES);

    op.addBool(ACT_ON_SELECTION_ONLY, false);

    op.useSection(DIRECTED);
    TreeLayouter treeLayouter = new TreeLayouter();
    op.addInt(MINIMAL_NODE_DISTANCE,
        (int) treeLayouter.getMinimalNodeDistance(), 1, 100);
    op.addInt(MINIMAL_LAYER_DISTANCE,
        (int) treeLayouter.getMinimalLayerDistance(), 10, 300);
    op.addEnum(ORIENTATION, enumOrient, 0);
    op.addEnum(PORT_STYLE, enumPortStyle, 0);

    op.addBool(INTEGRATED_NODE_LABELING, false);
    op.addBool(INTEGRATED_EDGE_LABELING, false);

    OptionItem edgeRoutingOption = op.addBool(ORTHOGONAL_EDGE_ROUTING, false);
    OptionItem busAlignmentOption = op.addDouble(BUS_ALIGNMENT, 0.5, 0, 1);
    OptionItem optionItem = op.addDouble(VERTICAL_ALIGNMENT, 0.5, 0, 1);

    op.addEnum(CHILD_PLACEMENT_POLICY, enumLeafLayoutPolicy, 0);
    op.addBool(ENFORCE_GLOBAL_LAYERING, false);

    busAlignmentOption.setAttribute(DefaultEditorFactory.ATTRIBUTE_MIN_VALUE_LABEL_TEXT, "TOP");
    busAlignmentOption.setAttribute(DefaultEditorFactory.ATTRIBUTE_MAX_VALUE_LABEL_TEXT, "BOTTOM");
    new ConstraintManager(op).setEnabledOnValueEquals(edgeRoutingOption, Boolean.TRUE, busAlignmentOption);

    optionItem.setAttribute(DefaultEditorFactory.ATTRIBUTE_MIN_VALUE_LABEL_TEXT, "TOP");
    optionItem.setAttribute(DefaultEditorFactory.ATTRIBUTE_MAX_VALUE_LABEL_TEXT, "BOTTOM");

    op.useSection(BALLOON);
    BalloonLayouter balloonLayouter = new BalloonLayouter();
    op.addEnum(ROOT_NODE_POLICY, enumRoot, 0);
    op.addInt(PREFERRED_CHILD_WEDGE, balloonLayouter.getPreferredChildWedge(), 1, 359);
    op.addInt(PREFERRED_ROOT_WEDGE, balloonLayouter.getPreferredRootWedge(), 1, 360);
    op.addInt(MINIMAL_EDGE_LENGTH, balloonLayouter.getMinimalEdgeLength(), 10, 400);
    op.addDouble(COMPACTNESS_FACTOR, balloonLayouter.getCompactnessFactor(), 0.1, 0.9);
    op.addBool(ALLOW_OVERLAPS, balloonLayouter.getAllowOverlaps());
    op.addBool(BALLOON_FROM_SKETCH, balloonLayouter.isFromSketchModeEnabled());

    op.useSection(HV);
    HVTreeLayouter hv = new HVTreeLayouter();
    op.addInt(HORIZONTAL_SPACE, (int) hv.getHorizontalSpace());
    op.addInt(VERTICAL_SPACE, (int) hv.getVerticalSpace());

    op.useSection(AR);
    ARTreeLayouter ar = new ARTreeLayouter();
    op.addInt(HORIZONTAL_SPACE, (int) ar.getHorizontalSpace());
    op.addInt(VERTICAL_SPACE, (int) ar.getVerticalSpace());
    op.addInt(BEND_DISTANCE, (int) ar.getBendDistance());
    op.addBool(USE_VIEW_ASPECT_RATIO, true);
    op.addDouble(ASPECT_RATIO, ar.getAspectRatio());
    cm.setEnabledOnValueEquals(USE_VIEW_ASPECT_RATIO, Boolean.FALSE, ASPECT_RATIO);

    return op;
  }




  public void mainrun() {
    CanonicMultiStageLayouter layouter = null;
    Graph2D graph = getGraph2D();

    OptionHandler op = getOptionHandler();
    String style = op.getString(LAYOUT_STYLE);

    if (style.equals(DIRECTED)) {
      TreeLayouter tree = new TreeLayouter();

      tree.setMinimalNodeDistance(op.getInt(DIRECTED, MINIMAL_NODE_DISTANCE));
      tree.setMinimalLayerDistance(op.getInt(DIRECTED, MINIMAL_LAYER_DISTANCE));

      OrientationLayouter ol = (OrientationLayouter) tree.getOrientationLayouter();
      if (op.getString(ORIENTATION).equals(TOP_TO_BOTTOM)) {
        ol.setOrientation(LayoutOrientation.TOP_TO_BOTTOM);
      } else if (op.getString(ORIENTATION).equals(BOTTOM_TO_TOP)) {
        ol.setOrientation(LayoutOrientation.BOTTOM_TO_TOP);
      } else if (op.getString(ORIENTATION).equals(RIGHT_TO_LEFT)) {
        ol.setOrientation(LayoutOrientation.RIGHT_TO_LEFT);
      } else {
        ol.setOrientation(LayoutOrientation.LEFT_TO_RIGHT);
      }

      if (op.getBool(ORTHOGONAL_EDGE_ROUTING)) {
        tree.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
      } else {
        tree.setLayoutStyle(TreeLayouter.PLAIN_STYLE);
      }

      final String leafLayotPolicyStr = op.getString(CHILD_PLACEMENT_POLICY);
      if (SIBLINGS_ON_SAME_LAYER.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_SIBLINGS_ON_SAME_LAYER);
      } else if (LEAVES_STACKED_LEFT.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED_LEFT);
      } else if (LEAVES_STACKED_RIGHT.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED_RIGHT);
      } else if (LEAVES_STACKED_LEFT_AND_RIGHT.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED_RIGHT);
      } else if (LEAVES_STACKED.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED);
      } else if (ALL_LEAVES_ON_SAME_LAYER.equals(leafLayotPolicyStr)) {
        tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_ALL_LEAVES_ON_SAME_LAYER);
      }

      if (op.getBool(ENFORCE_GLOBAL_LAYERING)) {
        tree.setEnforceGlobalLayering(true);
      } else {
        tree.setEnforceGlobalLayering(false);
      }

      if (op.getString(PORT_STYLE).equals(NODE_CENTER_PORTS)) {
        tree.setPortStyle(TreeLayouter.NODE_CENTER_PORTS);
      } else if (op.getString(PORT_STYLE).equals(BORDER_CENTER_PORTS)) {
        tree.setPortStyle(TreeLayouter.BORDER_CENTER_PORTS);
      } else if (op.getString(PORT_STYLE).equals(BORDER_DISTRIBUTED_PORTS)) {
        tree.setPortStyle(TreeLayouter.BORDER_DISTRIBUTED_PORTS);
      }

      tree.setIntegratedNodeLabelingEnabled(op.getBool(INTEGRATED_NODE_LABELING));
      tree.setIntegratedEdgeLabelingEnabled(op.getBool(INTEGRATED_EDGE_LABELING));

      tree.setVerticalAlignment(op.getDouble(VERTICAL_ALIGNMENT));
      tree.setBusAlignment(op.getDouble(BUS_ALIGNMENT));

      layouter = tree;
    } else if (style.equals(BALLOON)) {
      BalloonLayouter balloon = new BalloonLayouter();

      if (op.get(ROOT_NODE_POLICY).equals(enumRoot[0])) {
        balloon.setRootNodePolicy(BalloonLayouter.DIRECTED_ROOT);
      } else if (op.get(ROOT_NODE_POLICY).equals(enumRoot[1])) {
        balloon.setRootNodePolicy(BalloonLayouter.CENTER_ROOT);
      } else {
        balloon.setRootNodePolicy(BalloonLayouter.WEIGHTED_CENTER_ROOT);
      }

      balloon.setPreferredChildWedge(op.getInt(PREFERRED_CHILD_WEDGE));
      balloon.setPreferredRootWedge(op.getInt(PREFERRED_ROOT_WEDGE));
      balloon.setMinimalEdgeLength(op.getInt(BALLOON, MINIMAL_EDGE_LENGTH));
      balloon.setCompactnessFactor(op.getDouble(COMPACTNESS_FACTOR));
      balloon.setAllowOverlaps(op.getBool(ALLOW_OVERLAPS));
      balloon.setFromSketchModeEnabled(op.getBool(BALLOON_FROM_SKETCH));
      layouter = balloon;
    } else if (style.equals(HV)) {
      HVTreeLayouter hv = new HVTreeLayouter();
      DataProviderAdapter dp = new DataProviderAdapter() {
        public Object get(Object node) {
          if (getGraph2D().isSelected((Node) node)) {
            return HVTreeLayouter.VERTICAL_SUBTREE;
          } else {
            return HVTreeLayouter.HORIZONTAL_SUBTREE;
          }
        }
      };

      graph.addDataProvider(HVTreeLayouter.SUBTREE_ORIENTATION, dp);

      hv.setHorizontalSpace(op.getInt(HV, HORIZONTAL_SPACE));
      hv.setVerticalSpace(op.getInt(HV, VERTICAL_SPACE));

      layouter = hv;
    } else if (style.equals(AR)) {
      ARTreeLayouter ar = new ARTreeLayouter();

      DataProviderAdapter dp = new DataProviderAdapter() {
        public Object get(Object node) {
          if (getGraph2D().isSelected((Node) node)) {
            return ARTreeLayouter.ROUTING_HORIZONTAL;
          } else {
            return ARTreeLayouter.ROUTING_VERTICAL;
          }
        }
      };

      if (op.getBool(USE_VIEW_ASPECT_RATIO)) {
        Graph2DView view = getGraph2DView();
        if (view != null) {
          Dimension dim = view.getSize();
          ar.setAspectRatio(dim.getWidth() / (double) dim.getHeight());
        } else {
          ar.setAspectRatio(1);
        }
      } else {
        ar.setAspectRatio(op.getDouble(ASPECT_RATIO));
      }
      ar.setHorizontalSpace(op.getInt(AR, HORIZONTAL_SPACE));
      ar.setVerticalSpace(op.getInt(AR, VERTICAL_SPACE));
      ar.setBendDistance(op.getInt(AR, BEND_DISTANCE));

      graph.addDataProvider(ARTreeLayouter.ROUTING_POLICY, dp);
      layouter = ar;
    }

    layouter.setSubgraphLayouterEnabled(op.getBool(ACT_ON_SELECTION_ONLY));

    //configure tree reduction state and non-tree edge routing
    TreeReductionStage trs = null;
    if (op.getBool(ALLOW_NON_TREE_EDGES)) {
      trs = new TreeReductionStage();
      layouter.appendStage(trs);
      if (ROUTE_ORGANIC.equals(op.get(ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
        OrganicEdgeRouter organic = new OrganicEdgeRouter();
        trs.setNonTreeEdgeRouter(organic);
        trs.setNonTreeEdgeSelectionKey(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
      }
      if (ROUTE_ORTHOGONAL.equals(op.get(ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
        OrthogonalEdgeRouter orthogonal = new OrthogonalEdgeRouter();
        orthogonal.setCrossingCost(1.0);
        orthogonal.setReroutingEnabled(true);
        orthogonal.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);

        trs.setNonTreeEdgeSelectionKey(orthogonal.getSelectedEdgesDpKey());
        trs.setNonTreeEdgeRouter(orthogonal);
      }
      if (ROUTE_STRAIGHTLINE.equals(op.get(ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
        trs.setNonTreeEdgeRouter(trs.createStraightlineRouter());
      }
    }


    try {
      launchLayouter(layouter);
    } finally {
      // make sure the DataProviders will always be unregistered
      graph.removeDataProvider(ARTreeLayouter.ROUTING_POLICY);
      graph.removeDataProvider(HVTreeLayouter.SUBTREE_ORIENTATION);
      if (trs != null) {
        layouter.removeStage(trs);
      }
    }
  }
}