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
package demo.layout.labeling;

import y.geom.OrientedRectangle;
import y.geom.YDimension;
import y.base.YList;
import y.base.ListCell;
import y.layout.EdgeLabelModel;
import y.layout.EdgeLayout;
import y.layout.NodeLayout;
import y.layout.EdgeLabelLayout;
import y.layout.EdgeLabelCandidate;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class CompositeEdgeLabelModel implements EdgeLabelModel {

  private List models = new ArrayList();

  public Object getDefaultParameter() {
    final EdgeLabelModel model = (EdgeLabelModel) models.get(0);
    return new ModelParameter(model, model.getDefaultParameter());
  }

  public void add(EdgeLabelModel model){
    this.models.add(model);
  }

  public OrientedRectangle getLabelPlacement(YDimension labelSize, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                             NodeLayout targetLayout, Object param) {
    ModelParameter p = (ModelParameter) param;
    return p.model.getLabelPlacement(labelSize, edgeLayout, sourceLayout, targetLayout, p.parameter);
  }

  public YList getLabelCandidates(EdgeLabelLayout labelLayout, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                  NodeLayout targetLayout) {

    final YList list = new YList();
    for (Iterator it = models.iterator(); it.hasNext();) {
      EdgeLabelModel model = (EdgeLabelModel) it.next();
      final YList labelCandidates = model.getLabelCandidates(labelLayout, edgeLayout, sourceLayout, targetLayout);
      for (ListCell listCell = labelCandidates.firstCell(); listCell != null; listCell = listCell.succ()) {
        EdgeLabelCandidate candidate = (EdgeLabelCandidate) listCell.getInfo();
        ModelParameter newParam = new ModelParameter(model, candidate.getParameter());
        list.add(new EdgeLabelCandidate(candidate.getBox(), newParam, labelLayout, candidate.isInternal()));
      }
    }
    return list;
  }

  public Object createModelParameter(OrientedRectangle labelBounds, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                     NodeLayout targetLayout) {
    final EdgeLabelModel model = (EdgeLabelModel) models.get(0);
    final Object param = model.createModelParameter(labelBounds, edgeLayout, sourceLayout, targetLayout);
    return new ModelParameter(model, param);
  }

  public final class ModelParameter {
    private final EdgeLabelModel model;
    private final Object parameter;

    public ModelParameter(final EdgeLabelModel model, final Object parameter) {
      this.model = model;
      this.parameter = parameter;
    }

    public EdgeLabelModel getModel() {
      return model;
    }

    public Object getParameter() {
      return parameter;
    }
  }
}
