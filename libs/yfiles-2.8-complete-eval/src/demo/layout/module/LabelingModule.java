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

import y.layout.LabelRanking;
import y.layout.EdgeLabelModel;
import y.layout.RotatedDiscreteEdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.labeling.SALabeling;
import y.option.MappedListCellRenderer;
import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.util.DataProviderAdapter;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.YLabel;
import y.view.Graph2DLayoutExecutor;
import java.util.Map;

/**
 * This module represents an interactive configurator and launcher for the
 * yFiles labeling algorithms.
 *
 */
public class LabelingModule extends YModule {

  private static final String ALLOW_NODE_OVERLAPS = "ALLOW_NODE_OVERLAPS";
  private static final String INPUT = "INPUT";
  private static final String CONSIDER_INVISIBLE_LABELS = "CONSIDER_INVISIBLE_LABELS";
  private static final String ALLOW_EDGE_OVERLAPS = "ALLOW_EDGE_OVERLAPS";
  private static final String DIVERSE_LABELING = "DIVERSE_LABELING";
  private static final String QUALITY = "QUALITY";
  private static final String USE_OPTIMIZATION = "USE_OPTIMIZATION";
  private static final String USE_POSTPROCESSING = "USE_POSTPROCESSING";
  private static final String CONSIDER_SELECTED_FEATURES_ONLY = "CONSIDER_SELECTED_FEATURES_ONLY";
  private static final String SCOPE = "SCOPE";
  private static final String PLACE_EDGE_LABELS = "PLACE_EDGE_LABELS";
  private static final String MODEL = "MODEL";
  private static final String EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  private static final String AUTO_ROTATE = "AUTO_ROTATE";
  private static final String UNKNOWN_MODEL_VALUE = "UNKNOWN_MODEL_VALUE";
  private static final String PLACE_NODE_LABELS = "PLACE_NODE_LABELS";
  private static final String OPTIMIZATION_BALANCED = "OPTIMIZATION_BALANCED";
  private static final String OPTIMIZATION_NODE_OVERLAP = "OPTIMIZATION_NODE_OVERLAP";
  private static final String OPTIMIZATION_LABEL_OVERLAP = "OPTIMIZATION_LABEL_OVERLAP";
  private static final String OPTIMIZATION_EDGE_OVERLAP = "OPTIMIZATION_EDGE_OVERLAP";
  private static final String OPTIMIZATION_NONE = "OPTIMIZATION_NONE";
  private static final String OPTIMIZATION_STRATEGY = "OPTIMIZATION_STRATEGY";

  // the following are not keys for i18n
  private static final String AS_IS = "As Is";
  private static final String BEST = "Best";

  private static final String[] optimizationStrategy = {
      OPTIMIZATION_BALANCED, OPTIMIZATION_NONE, OPTIMIZATION_EDGE_OVERLAP, OPTIMIZATION_LABEL_OVERLAP,
      OPTIMIZATION_NODE_OVERLAP
  };

  public LabelingModule() {
    super(DIVERSE_LABELING, "yFiles Layout Team", "Places Labels");
  }

  /** Creates an option handler for this layouter */
  protected OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());
    op.useSection(SCOPE);
    op.addBool(PLACE_NODE_LABELS, true);
    op.addBool(PLACE_EDGE_LABELS, true);
    op.addBool(CONSIDER_SELECTED_FEATURES_ONLY, false);
    op.addBool(CONSIDER_INVISIBLE_LABELS, false);
    op.useSection(QUALITY);
    op.addBool(USE_OPTIMIZATION, false);
    op.addEnum(OPTIMIZATION_STRATEGY, optimizationStrategy, 0);
    op.addBool(ALLOW_NODE_OVERLAPS, false);
    op.addBool(ALLOW_EDGE_OVERLAPS, true);
    op.addBool(USE_POSTPROCESSING, false);

    op.useSection(MODEL);
    Map map = EdgeLabel.modelToStringMap();
    Object asIs = AS_IS;
    map.put(asIs, asIs);
    Object best = BEST;
    map.put(best, best);    
    op.addEnum(EDGE_LABEL_MODEL, map.keySet().toArray(), best, new MappedListCellRenderer(map));
    op.addBool(AUTO_ROTATE, false);

    // enable the auto rotate item for applicably label models only
    final ConstraintManager cm = new ConstraintManager(op);
    final Object[] nonAutoRotatableModels = {AS_IS, BEST, new Byte(EdgeLabel.FREE)};
    final ConstraintManager.Condition condition = cm.createConditionValueIs(EDGE_LABEL_MODEL, nonAutoRotatableModels);    
    cm.setEnabledOnCondition(condition.inverse(), op.getItem(AUTO_ROTATE));

    return op;
  }

  protected void init() {
    final OptionHandler op = getOptionHandler();
    DataProvider labelSet = new LabelSetDP(
        getGraph2D(),
        op.getBool(CONSIDER_SELECTED_FEATURES_ONLY),
        op.getBool(PLACE_NODE_LABELS),
        op.getBool(PLACE_EDGE_LABELS),
        op.getBool(CONSIDER_INVISIBLE_LABELS));
    getGraph2D().addDataProvider(INPUT, labelSet);

    setupEdgeLabelModels(op.get(EDGE_LABEL_MODEL), labelSet, op.getBool(AUTO_ROTATE));
  }

  protected void mainrun() {
    final OptionHandler op = getOptionHandler();
    final MISLabelingAlgorithm al = op.getBool(USE_OPTIMIZATION) ?
        (MISLabelingAlgorithm) new SALabeling() :
        new GreedyMISLabeling();

    al.setOptimizationStrategy((byte) op.getEnum(OPTIMIZATION_STRATEGY));
    if (al.getOptimizationStrategy() == MISLabelingAlgorithm.OPTIMIZATION_NONE) {
      al.setProfitModel(new LabelRanking());
    }
    al.setRemoveNodeOverlaps(!op.getBool(ALLOW_NODE_OVERLAPS));
    al.setRemoveEdgeOverlaps(!op.getBool(ALLOW_EDGE_OVERLAPS));
    al.setApplyPostprocessing(op.getBool(USE_POSTPROCESSING));

    al.setSelection(INPUT);

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.UNBUFFERED);
    final Graph2DView view = getGraph2DView();
    if (view == null) {
      layoutExecutor.doLayout(getGraph2D(), al);
    } else {
      layoutExecutor.doLayout(view, al);
    }

    getGraph2D().removeDataProvider(INPUT);
    getGraph2D().updateViews();
  }

  static EdgeLabelModel getEdgeLabelModel(byte modelValue) {
    final EdgeLabelModel labelModel;
    if(EdgeLabel.CENTERED == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.CENTERED);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if(EdgeLabel.TWO_POS == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.TWO_POS);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if(EdgeLabel.SIX_POS == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.SIX_POS);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if(EdgeLabel.THREE_CENTER == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.THREE_CENTER);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if(EdgeLabel.CENTER_SLIDER == modelValue) {
      labelModel = new RotatedSliderEdgeLabelModel(RotatedSliderEdgeLabelModel.CENTER_SLIDER);
      ((RotatedSliderEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if(EdgeLabel.SIDE_SLIDER == modelValue) {
      labelModel = new RotatedSliderEdgeLabelModel(RotatedSliderEdgeLabelModel.SIDE_SLIDER);
      ((RotatedSliderEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else {
      labelModel = null;
    }
    return labelModel;
  }

  /**
   * Selects the labels we want to set.
   */
  static class LabelSetDP extends DataProviderAdapter
  {
    private final boolean considerOnlySelected;
    private final Graph2D graph;
    private final boolean nodes;
    private final boolean edges;
    private final boolean invisible;

    LabelSetDP(Graph2D g,boolean sel,boolean n,boolean e,boolean uv)
    {
      considerOnlySelected = sel;
      graph = g;
      nodes = n;
      edges = e;
      invisible = uv;
    }

    public boolean getBool(Object o)
    {
      YLabel ylabel = (YLabel) o;
      if (!ylabel.isVisible() && !invisible) {
        return false;
      }
      if (o instanceof NodeLabel) {
        NodeLabel l = (NodeLabel) o;
        if (l.getModel() == NodeLabel.INTERNAL) return false;
      }
      if (considerOnlySelected)
      {
        if ((o instanceof NodeLabel) && nodes)
        {
          NodeLabel l = (NodeLabel) o;
          if (graph.isSelected(l.getNode())) {
            return true;
          } else {
            return false;
          }
        }
        if ((o instanceof EdgeLabel) && edges) {
          EdgeLabel l = (EdgeLabel) o;
          if (graph.isSelected(l.getEdge())) {
            return true;
          } else {
            return false;
          }
        }
        return false;
      } else {
        if ((o instanceof NodeLabel) && nodes) return true;
        if ((o instanceof EdgeLabel) && edges) return true;
        return false;
      }
    }
  }

  void setupEdgeLabelModels(Object modelValue, DataProvider labelFilter, boolean autoRotate) {
    if (AS_IS.equals(modelValue)) {
      return;
    }

    final byte model;
    EdgeLabelModel labelModel = null;
    if (BEST.equals(modelValue)) {
      model = EdgeLabel.FREE;
    } else if (modelValue instanceof Byte) {
      model = ((Byte) modelValue).byteValue();
      if (autoRotate) {
        labelModel = getEdgeLabelModel(model);
      }
    } else {
      throw new IllegalArgumentException(UNKNOWN_MODEL_VALUE + modelValue);
    }

    final Graph2D graph = getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        EdgeLabel label = er.getLabel(i);
        if (labelFilter.getBool(label)) {
          if (labelModel != null) {
            label.setLabelModel(labelModel);
          } else {
            label.setModel(model);
          }
          label.setModelParameter(label.getLabelModel().getDefaultParameter());
        }
      }
    }
  }
}

