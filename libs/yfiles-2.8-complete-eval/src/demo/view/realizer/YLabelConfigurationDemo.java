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
package demo.view.realizer;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.Node;
import y.geom.AffineLine;
import y.geom.YPoint;
import y.geom.YVector;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.YLabel;
import y.view.Arrow;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Point2D;
import java.util.Map;

/**
 * This class demonstrates the usages of {@link YLabel}'s configuration feature.
 *
 * @see YLabel#setConfiguration(String)
 */
public class YLabelConfigurationDemo extends DemoBase {
  /**
   * Launcher method. Execute this class to see sample instantiations of {@link YLabel}s using a custom
   * configuration in action.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new YLabelConfigurationDemo()).start();
      }
    });
  }

  /** Creates the YLabelConfigurationDemo demo. */
  public YLabelConfigurationDemo() {
    super();
    Graph2D graph2D;
    {
      // Get the factory to register custom styles/configurations.
      YLabel.Factory factory = NodeLabel.getFactory();

      // Retrieve a map that holds the default NodeLabel configuration.
      // The implementations contained therein can be replaced one by one in order
      // to create custom configurations...
      Map implementationsMap = factory.createDefaultConfigurationMap();

      // We will just customize the painting so register our custom painter
      implementationsMap.put(YLabel.Painter.class, new MyPainter());

      // Add the first configuration to the factory.
      factory.addConfiguration("Bubble", implementationsMap);

      // configure the default label to use our new configuration and give it a funky color and style
      graph2D = view.getGraph2D();
      NodeRealizer realizer = graph2D.getDefaultNodeRealizer();
      NodeLabel label = realizer.getLabel();
      label.setModel(NodeLabel.FREE);
      label.setOffset(50, 50);
      label.setConfiguration("Bubble");
      label.setLineColor(Color.DARK_GRAY);
      label.setBackgroundColor(new Color(202,227,255));
    }

    {
      // Make a similar configuration for edge labels.
      YLabel.Factory factory = EdgeLabel.getFactory();
      Map implementationsMap = factory.createDefaultConfigurationMap();
      implementationsMap.put(YLabel.Painter.class, new MyPainter());
      factory.addConfiguration("Bubble", implementationsMap);
      graph2D = view.getGraph2D();
      EdgeRealizer realizer = graph2D.getDefaultEdgeRealizer();
      EdgeLabel label = realizer.getLabel();
      label.setModel(EdgeLabel.SIX_POS);
      label.setDistance(30);
      label.setConfiguration("Bubble");
      label.setLineColor(Color.DARK_GRAY);
      label.setBackgroundColor(new Color(202,227,255));
    }

    // load a sample...
    loadGraph("resource/bubble.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
    view.getGraph2D().getDefaultEdgeRealizer().setTargetArrow(Arrow.NONE);
  }


  /**
   * A simple YLabel.Painter implementation that reuses most of the default painting behavior from
   * DefaultLabelConfiguration and just changes the way the background is painted.
   */
  static final class MyPainter extends DefaultLabelConfiguration {
    /** Overwrite the painting of the background only. */
    public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {

      // calculate the bubble
      Shape shape = new RoundRectangle2D.Double(x, y, width, height, Math.min(width / 3, 10), Math.min(height / 3, 10));

      double cx = x + width * 0.5d;
      double cy = y + height * 0.5d;

      if (label instanceof NodeLabel) {
        // calculate a wedge connecting the node and the rounded rectangle around the label text
        NodeRealizer labelRealizer = ((NodeLabel) label).getRealizer();
        Node node = ((NodeLabel) label).getNode();
        Graph2D graph2D = ((Graph2D) node.getGraph());
        NodeRealizer nodeRealizer = graph2D.getRealizer(node);

        double tx = graph2D.getCenterX(node);
        double ty = graph2D.getCenterY(node);

        // calculate an offset for the tip of the wedge
        if(!nodeRealizer.contains(cx, cy)) {
          double dirX = cx - labelRealizer.getCenterX();
          double dirY = cy - labelRealizer.getCenterY();
          Point2D result = new Point2D.Double();
          nodeRealizer.findIntersection(tx, ty, cx, cy, result);
          double l0 = Math.sqrt(dirX * dirX + dirY * dirY);
          if(l0 > 0) {
            double halfNodeWidth = nodeRealizer.getWidth() * 0.5 + 5;
            halfNodeWidth = (dirX > 0) ? halfNodeWidth : -1.0 * halfNodeWidth;
            tx = result.getX() + 5 * dirX / l0;
            ty = result.getY() + 5 * dirY / l0;
          }
        }

        // add the wedge to the bubble shape
        double dx = cx - tx;
        double dy = cy - ty;
        double l = Math.sqrt(dx * dx + dy * dy);
        if (l > 0) {
          double size = Math.min(width, height) * 0.25;
          GeneralPath p = new GeneralPath();
          p.moveTo((float) tx, (float) ty);
          p.lineTo((float) (cx + dy * size / l), (float) (cy - dx * size / l));
          p.lineTo((float) (cx - dy * size / l), (float) (cy + dx * size / l));
          p.closePath();
          Area area = new Area(shape);
          area.add(new Area(p));
          shape = area;
        }

      } else if (label instanceof EdgeLabel) {
        // calculate an anchor line connecting the edge and the rounded rectangle around the label text
        Edge edge = ((EdgeLabel) label).getEdge();
        Graph2D graph2D = ((Graph2D) edge.getGraph());
        EdgeRealizer edgeRealizer = graph2D.getRealizer(edge);
        GeneralPath path = edgeRealizer.getPath();
        double[] result = PointPathProjector.calculateClosestPathPoint(path, cx, cy);
        double dx = cx - result[0];
        double dy = cy - result[1];
        double l = Math.sqrt(dx * dx + dy * dy);

        // draw the anchor line with an offset to the edge
        if (l > 0) {
          double tx = result[0] + 5 * dx / l;
          double ty = result[1] + 5 * dy / l;
          Line2D line = new Line2D.Double(cx, cy, tx, ty);
          gfx.setColor(new Color(0, 0, 0, 64));
          gfx.draw(line);
        }
      }

      // paint the bubble using the colors of the label
      Color backgroundColor = label.getBackgroundColor();
      if (backgroundColor != null) {
        // shadow
        gfx.setColor(new Color(0, 0, 0, 64));
        gfx.translate(5, 5);
        gfx.fill(shape);
        gfx.translate(-5, -5);
        // and background
        gfx.setColor(backgroundColor);
        gfx.fill(shape);
      }

      // line
      Color lineColor = label.getLineColor();
      if (lineColor != null) {
        gfx.setColor(lineColor);
        gfx.draw(shape);
      }
    }

  }

  /** Helper class that provides diverse services related to working with points on a path. */
  static class PointPathProjector {
    private PointPathProjector() {
    }

    /**
     * Calculates the point on the path which is closest to the given point. Ties are broken arbitrarily.
     *
     * @param path where to look for the closest point
     * @param px   x coordinate of query point
     * @param py   y coordinate of query point
     * @return double[6] <ul> <li>x coordinate of the closest point</li> <li>y coordinate of the closest point</li>
     *         <li>distance of the closest point to given point</li> <li>index of the segment of the path including the
     *         closest point (as a double starting with 0.0, segments are computed with a path iterator with flatness
     *         1.0)</li> <li>ratio of closest point on the the including segment (between 0.0 and 1.0)</li> <li>ratio of
     *         closest point on the entire path (between 0.0 and 1.0)</li> </ul>
     */
    static double[] calculateClosestPathPoint(GeneralPath path, double px, double py) {
      double[] result = new double[6];
      YPoint point = new YPoint(px, py);
      double pathLength = 0;

      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      double[] curSeg = new double[4];
      double minDist;
      if (pi.ok()) {
        curSeg = pi.segment();
        minDist = YPoint.distance(px, py, curSeg[0], curSeg[1]);
        result[0] = curSeg[0];
        result[1] = curSeg[1];
        result[2] = minDist;
        result[3] = 0.0;
        result[4] = 0.0;
        result[5] = 0.0;
      } else {
        // no points in GeneralPath: should not happen in this context
        throw new IllegalStateException("path without any coordinates");
      }

      int segmentIndex = 0;
      double lastPathLength = 0.0;
      do {
        YPoint segmentStart = new YPoint(curSeg[0], curSeg[1]);
        YPoint segmentEnd = new YPoint(curSeg[2], curSeg[3]);
        YVector segmentDirection = new YVector(segmentEnd, segmentStart);
        double segmentLength = segmentDirection.length();
        pathLength += segmentLength;
        segmentDirection.norm();

        AffineLine currentSegment = new AffineLine(segmentStart, segmentDirection);
        AffineLine throughPoint = new AffineLine(point, YVector.orthoNormal(segmentDirection));
        YPoint crossing = AffineLine.getCrossing(currentSegment, throughPoint);
        YVector crossingVector = new YVector(crossing, segmentStart);

        YVector segmentVector = new YVector(segmentEnd, segmentStart);
        double indexEnd = YVector.scalarProduct(segmentVector, segmentDirection);
        double indexCrossing = YVector.scalarProduct(crossingVector, segmentDirection);

        double dist;
        double segmentRatio;
        YPoint nearestOnSegment;
        if (indexCrossing <= 0.0) {
          dist = YPoint.distance(point, segmentStart);
          nearestOnSegment = segmentStart;
          segmentRatio = 0.0;
        } else if (indexCrossing >= indexEnd) {
          dist = YPoint.distance(point, segmentEnd);
          nearestOnSegment = segmentEnd;
          segmentRatio = 1.0;
        } else {
          dist = YPoint.distance(point, crossing);
          nearestOnSegment = crossing;
          segmentRatio = indexCrossing / indexEnd;
        }

        if (dist < minDist) {
          minDist = dist;
          result[0] = nearestOnSegment.getX();
          result[1] = nearestOnSegment.getY();
          result[2] = minDist;
          result[3] = segmentIndex;
          result[4] = segmentRatio;
          result[5] = segmentLength * segmentRatio + lastPathLength;
        }

        segmentIndex++;
        lastPathLength = pathLength;
        pi.next();
      } while (pi.ok());

      if (pathLength > 0) {
        result[5] = result[5] / pathLength;
      } else {
        result[5] = 0.0;
      }
      return result;
    }

    /** Helper class used by PointPathProjector. */
    static class CustomPathIterator {
      private double[] cachedSegment;
      private boolean moreToGet;
      private PathIterator pathIterator;

      public CustomPathIterator(GeneralPath path, double flatness) {
        // copy the path, thus the original may safely change during iteration
        pathIterator = (new GeneralPath(path)).getPathIterator(null, flatness);
        cachedSegment = new double[4];
        getFirstSegment();
      }

      public boolean ok() {
        return moreToGet;
      }

      public final double[] segment() {
        if (moreToGet) {
          return cachedSegment;
        } else {
          return null;
        }
      }

      public void next() {
        if (!pathIterator.isDone()) {
          float[] curSeg = new float[2];
          cachedSegment[0] = cachedSegment[2];
          cachedSegment[1] = cachedSegment[3];
          pathIterator.currentSegment(curSeg);
          cachedSegment[2] = curSeg[0];
          cachedSegment[3] = curSeg[1];
          pathIterator.next();
        } else {
          moreToGet = false;
        }
      }

      private void getFirstSegment() {
        float[] curSeg = new float[2];
        if (!pathIterator.isDone()) {
          pathIterator.currentSegment(curSeg);
          cachedSegment[0] = curSeg[0];
          cachedSegment[1] = curSeg[1];
          pathIterator.next();
          moreToGet = true;
        } else {
          moreToGet = false;
        }
        if (!pathIterator.isDone()) {
          pathIterator.currentSegment(curSeg);
          cachedSegment[2] = curSeg[0];
          cachedSegment[3] = curSeg[1];
          pathIterator.next();
          moreToGet = true;
        } else {
          moreToGet = false;
        }
      }
    }
  }
}
