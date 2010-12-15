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
package demo.layout.hierarchic;

import y.base.DataProvider;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.LayoutOrientation;
import y.layout.NodeLayout;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SwimLaneDescriptor;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drawable implementation that displays swim lanes
 */
final class SwimlaneDrawable implements Drawable {
  private static final int X = 0;
  private static final int Y = 1;


  private final Line2D.Double line;
  private final Stroke stroke;

  private List lanes;
  private Rectangle bounds;
  private double spacing;
  private Color[] colors;
  private byte orientation;
  private boolean lastUpdateWasVertical;

  private Graph2D graph;
  private Graph2DView view;
  private boolean accessGraph;

  SwimlaneDrawable( final Graph2D graph ) {
    this(graph, null, true);
  }

  SwimlaneDrawable( final Graph2DView view ) {
    this(null, view, false);
  }

  SwimlaneDrawable(
          final Graph2D graph, final DataProvider swimLaneDescriptors
  ) {
    this(graph, null, true);
  }

  private SwimlaneDrawable(
          final Graph2D graph, final Graph2DView view, final boolean accessGraph
  ) {
    this.graph = graph;
    this.view = view;
    this.accessGraph = accessGraph;

    this.line = new Line2D.Double();
    this.stroke = new BasicStroke(1.25f);
    this.spacing = 20.0d;
    this.bounds = new Rectangle(20,20,200,200);
    this.lanes = new ArrayList(20);
    this.colors = new Color[]{new Color(150, 150, 255), new Color(0 , 0, 150)};
    this.orientation = LayoutOrientation.TOP_TO_BOTTOM;
    this.lastUpdateWasVertical = true;
  }

  public void setEvenLaneColor( final Color color ) {
    colors[0] = color;
  }

  public void setOddLaneColor( final Color color ) {
    colors[1] = color;
  }

  public byte getOrientation() {
    return orientation;
  }

  public void setOrientation( final byte orientation ) {
    this.orientation = orientation;
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public void updateLanes() {
    lanes.clear();

    final Graph2D g = getGraph();

    if (g.N() < 1) {
      return;
    }

    final DataProvider slds = getSwimLaneDescriptors(g);
    if (slds == null) {
      return;
    }

    double minY;
    double maxY;
    double minX;
    double maxX;
    if (LayoutOrientation.TOP_TO_BOTTOM == orientation ||
        LayoutOrientation.BOTTOM_TO_TOP == orientation) {
      minY = Double.MAX_VALUE;
      maxY = -Double.MAX_VALUE;
      for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()){
        final Node node = nc.node();
        final NodeLayout nl = g.getNodeLayout(node);
        minY = Math.min(minY, nl.getY());
        maxY = Math.max(maxY, nl.getY() + nl.getHeight());

        final SwimLaneDescriptor sld = (SwimLaneDescriptor) slds.get(node);
        if (sld == null) {
          continue;
        }
        while (lanes.size() - 1 < sld.getComputedLaneIndex()){
          lanes.add(new double[][]{{0, 0, 0, 0}, {0, 0}});
        }
        double[][] laneData = (double[][]) lanes.get(sld.getComputedLaneIndex());
        laneData[X][0] = sld.getComputedLanePosition();
        laneData[X][1] = laneData[X][0] + sld.getLeftLaneInset();
        laneData[X][3] = laneData[X][0] + sld.getComputedLaneWidth();
        laneData[X][2] = laneData[X][3] - sld.getRightLaneInset();
      }

      minX = Double.MAX_VALUE;
      maxX = -Double.MAX_VALUE;
      for (int i = 0; i < lanes.size(); i++){
        double[][] laneData = (double[][]) lanes.get(i);
        laneData[Y][0] = minY - spacing;
        laneData[Y][1] = maxY + spacing;
        minX = Math.min(laneData[X][0], minX);
        maxX = Math.max(laneData[X][3], maxX);
      }

      lastUpdateWasVertical = true;
    } else {
      minX = Double.MAX_VALUE;
      maxX = -Double.MAX_VALUE;
      for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()){
        final Node node = nc.node();
        final NodeLayout nl = g.getNodeLayout(node);
        minX = Math.min(minX, nl.getX());
        maxX = Math.max(maxX, nl.getX() + nl.getWidth());

        final SwimLaneDescriptor sld = (SwimLaneDescriptor) slds.get(node);
        if (sld == null) {
          continue;
        }
        while (lanes.size() - 1 < sld.getComputedLaneIndex()){
          lanes.add(new double[][]{{0, 0, 0, 0}, {0, 0}});
        }
        double[][] laneData = (double[][]) lanes.get(sld.getComputedLaneIndex());
        final double sign = LayoutOrientation.LEFT_TO_RIGHT == orientation ? -1 : 1;
        laneData[X][0] = sign * sld.getComputedLanePosition();
        laneData[X][1] = laneData[X][0] + sign * sld.getLeftLaneInset();
        laneData[X][3] = laneData[X][0] + sign * sld.getComputedLaneWidth();
        laneData[X][2] = laneData[X][3] - sign * sld.getRightLaneInset();
      }

      minY = Double.MAX_VALUE;
      maxY = -Double.MAX_VALUE;
      for (int i = 0; i < lanes.size(); i++){
        double[][] laneData = (double[][]) lanes.get(i);
        laneData[Y][0] = minX - spacing;
        laneData[Y][1] = maxX + spacing;
        minY = Math.min(laneData[X][0], minY);
        maxY = Math.max(laneData[X][3], maxY);
      }

      lastUpdateWasVertical = false;
    }

    bounds.setFrame(minX, minY, maxX - minX, maxY - minY);
    g.updateViews();
  }

  public void paint( final Graphics2D g ) {
    if (lanes.isEmpty()) {
      return;
    }

    final Color oldColor = g.getColor();
    final Stroke oldStroke = g.getStroke();
    g.setStroke(stroke);
    if (lastUpdateWasVertical) {
      for (int i = 0; i < lanes.size(); i++) {
        double[][] lane = (double[][]) lanes.get(i);

        line.y1 = lane[Y][0];
        line.y2 = lane[Y][1];

        g.setColor(colors[i % colors.length]);
        line.x1 = line.x2 = lane[X][1];
        g.draw(line);
        line.x1 = line.x2 = lane[X][2];
        g.draw(line);
      }
    } else {
      for (int i = 0; i < lanes.size(); i++) {
        double[][] lane = (double[][]) lanes.get(i);

        line.x1 = lane[Y][0];
        line.x2 = lane[Y][1];

        g.setColor(colors[i % colors.length]);
        line.y1 = line.y2 = lane[X][1];
        g.draw(line);
        line.y1 = line.y2 = lane[X][2];
        g.draw(line);
      }
    }
    g.setStroke(oldStroke);
    g.setColor(oldColor);
  }

  private DataProvider getSwimLaneDescriptors( final Graph2D graph ) {
    return graph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);
  }

  private Graph2D getGraph() {
    if (accessGraph) {
      return graph;
    } else {
      return view.getGraph2D();
    }
  }
}
