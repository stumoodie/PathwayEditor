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
package demo.view.viewmode;


import demo.view.DemoBase;
import y.base.Edge;
import y.base.Node;
import y.base.YCursor;
import y.base.YList;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Port;

import java.awt.EventQueue;

/**
 * Demonstrates how CreateEdgeMode can be customized in order to
 * control automatic assignments of ports for edges.
 * Edges are created in such a way, that the source port is always on
 * the top side of the source node and the target port is always on the bottom
 * side of the target node.
 *
 * Usage: Create some nodes and edges. Select an edge to check its source
 * and target ports.
 */
public class PortCreateEdgeModeDemo extends DemoBase
{

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode( editMode );
    //set a custom CreateEdgeMode for the edge mode
    editMode.setCreateEdgeMode( new PortCreateEdgeMode() );
  }

  public static class PortCreateEdgeMode extends CreateEdgeMode
  {
    private Edge edge; // need this for the hook

    /**
     * If a node was hit at the given coordinates, that node
     * will be used as target node for the newly created edge.
     *
     */
    public void mouseReleasedLeft(double x, double y)
    {
      // simulate a pressed shift...
      // this will trigger CreateEdgeMode, to preassign offset
      // to source and target ports
      super.mouseShiftReleasedLeft(x, y);

      if (edge != null){ // the edge has just been created
        Graph2D graph = (Graph2D) edge.getGraph();
        EdgeRealizer er = graph.getRealizer(edge);

        // get a list of port candidates
        YList ports = getPorts(edge.source(), edge);
        Port p = er.getSourcePort();
        // snap to one of them
        snap(er, true, p.getOffsetX(), p.getOffsetY(), ports);

        // get a list of port candidates
        ports = getPorts(edge.target(), edge);
        p = er.getTargetPort();
        // snap to one of them
        snap(er, false, p.getOffsetX(), p.getOffsetY(), ports);

        // do some clean up
        edge = null;
        graph.updateViews();
      }

    }

    /**
     * Initiates the creation of an edge.
     * 
     */
    public void mousePressedLeft(double x, double y)
    {
      // simulate a pressed shift...
      // this will trigger CreateEdgeMode, to preassign offset
      // to source and target ports
      super.mouseShiftPressedLeft(x, y);
    }


    public void edgeCreated(Edge e){
      //remember the edge...
      this.edge = e;
    }

    /**
     * This method finds a list of Port objects for a specific edge/node pair
     *
     * @param onNode  the node
     * @param forEdge the edge
     * @return a list of Port objects
     */
    public YList getPorts(Node onNode, Edge forEdge) {
      YList list = new YList();
      Graph2D graph = (Graph2D) onNode.getGraph();
      NodeRealizer nr = graph.getRealizer(onNode);
      EdgeRealizer er = graph.getRealizer(forEdge);

      if (onNode == forEdge.source()) {
        // source ports are centered on top of the node
        list.add(new Port(0, -nr.getHeight() / 2));
      } else {
        // target ports are centered at the bottom of the node
        list.add(new Port(0, nr.getHeight() / 2));
      }
      return list;
    }

    /**
     * This method calculates a metric for ports and points
     *
     * @param x    the initial x offset
     * @param y    the initial y offset
     * @param port the port
     * @return the distance between the point (x,y) and the port
     */
    public static double getDistance(double x, double y, Port port) {
      return Math.sqrt((x - port.getOffsetX()) * (x - port.getOffsetX())
          + (y - port.getOffsetY()) * (y - port.getOffsetY()));
    }

    /**
     * This method chooses from a list of given ports for an edge
     * a suitable port, given an initial placement.
     *
     * @param edge   the affected edge
     * @param source whether we look at the source node
     * @param x      the initial x offset
     * @param y      the initial y offset
     * @param ports  a list of Port objects (candidates)
     */
    public void snap(EdgeRealizer edge, boolean source, double x, double y, YList ports) {
      if (ports == null || ports.size() < 1) {
        return; // do nothing
      }

      // find the closest port with regards to the getDistance function
      Port closest = (Port) ports.first();
      double dist = getDistance(x, y, closest);

      for (YCursor cursor = ports.cursor(); cursor.ok(); cursor.next()) {
        Port p = (Port) cursor.current();
        double d2 = getDistance(x, y, p);
        if (d2 < dist) {
          dist = d2;
          closest = p;
        }
      }

      // assign the port
      if (source) {
        edge.setSourcePort(closest);
      } else {
        edge.setTargetPort(closest);
      }
    }

  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new PortCreateEdgeModeDemo()).start("Port Demo");
      }
    });
  }
}


      
