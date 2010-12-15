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

import y.layout.ProfitModel;
import y.layout.LabelCandidate;
import y.layout.EdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.RotatedDiscreteEdgeLabelModel;
import demo.layout.labeling.CompositeEdgeLabelModel;

/**
 * A simple profit model used by the edge labeling demo.
 *
 */
public class DemoProfitModel implements ProfitModel {  
  private double angle;
  private double angleProfit;
  private double otherProfit;

  /**
   * For a given label candidate lc this profit model returns profit "angleProfit" if lc's angle == "angle"
   * and profit "otherProfit", otherwise.
   *
   * @param angle the value of the angle (in radians) that has profit "angleProfit"
   * @param angleProfit the profit used for angles with value "angle"
   * @param otherProfit the profit used for angles with values != "angle"
   */
  public DemoProfitModel(double angle, double angleProfit, double otherProfit) {
    this.angle = angle;
    this.angleProfit = angleProfit;
    this.otherProfit = otherProfit;
  }

  public double getProfit(LabelCandidate candidate) {
    Object param = candidate.getModelParameter();
    if (param instanceof CompositeEdgeLabelModel.ModelParameter) {
      double angle = determineAngle((CompositeEdgeLabelModel.ModelParameter) param);
      if (angle == this.angle) {
        return angleProfit;
      } 
    }
    return otherProfit;
  }

  //returns the angle encoded by the given model parameter
  private static double determineAngle(CompositeEdgeLabelModel.ModelParameter param) {
    EdgeLabelModel elm = param.getModel();
    double angle = 0;
    if(elm instanceof RotatedSliderEdgeLabelModel) {
      angle = ((RotatedSliderEdgeLabelModel) elm).getAngle();
    } else if(elm instanceof RotatedDiscreteEdgeLabelModel) {
      angle = ((RotatedDiscreteEdgeLabelModel) elm).getAngle();
    } else {
      throw new RuntimeException("Unknown EdgeLableModel!");
    }
    return angle;
  }
}
