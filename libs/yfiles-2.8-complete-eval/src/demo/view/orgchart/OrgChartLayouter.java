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
package demo.view.orgchart;

import y.algo.Trees;
import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.base.YList;
import y.geom.LineSegment;
import y.geom.YLineSegmentCursor;
import y.geom.YPoint;
import y.geom.YPointPath;
import y.layout.AbstractLayoutStage;
import y.layout.EdgeLayout;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.tree.AssistantPlacer;
import y.layout.tree.DefaultNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.LeftRightPlacer;
import y.layout.tree.NodePlacer;
import y.util.Maps;

/**
 * A layout algorithm for tree-structured organization charts. It allows to specify different 
 * layout strategies for the child nodes of a node. Furthermore, it supports special placement for
 * nodes that are marked as assistants,
 */
public class OrgChartLayouter implements Layouter {

  /**
   * Child layout specifier. The children of a node shall be arranged left to right on the same layer.
   */
  public static final Object CHILD_LAYOUT_SAME_LAYER = "SAME_LAYER";
  
  /**
   * Child layout specifier. The children of a node shall be arranged below each other and placed left of a common bus.
   */
  public static final Object CHILD_LAYOUT_LEFT_BELOW = "LEFT_BELOW";

  /**
   * Child layout specifier. The children of a node shall be arranged below each other and placed right of a common bus.
   */
  public static final Object CHILD_LAYOUT_RIGHT_BELOW = "RIGHT_BELOW";
  
  /**
   * Child layout specifier. The children of a node shall be arranged on both sides of a common vertical bus. Children on both sides
   * are placed below each.    
   */
  public static final Object CHILD_LAYOUT_BELOW = "BELOW";
  
  /**
   * DataProvider key used to register a DataProvider with the input graph. For each node in the graph 
   * the registered DataProvider returns either of {@link #CHILD_LAYOUT_BELOW}, {@link #CHILD_LAYOUT_LEFT_BELOW}, 
   * {@link #CHILD_LAYOUT_RIGHT_BELOW}, or {@link #CHILD_LAYOUT_SAME_LAYER}.
   */
  public static final Object CHILD_LAYOUT_DPKEY = "OrgChartLayouter#CHILD_LAYOUT_DPKEY";
  
  /**
   * DataProvider key used to register a DataProvider with the input graph. For each node in the graph 
   * the registered DataProvider returns a boolean value that signifies whether or not the
   * node is to be considered an assistant to its parent node. Assistants are always placed along to the left or right of the
   * the vertical bus leaving the parent node. For non-assistant child nodes the child layout specified for the
   * parent node will be applied.
   */
  public static final Object ASSISTANT_DPKEY = "OrgChartLayouter#ASSISTANT_DPKEY";
  
  private boolean duplicateBendsOnSharedBus = false;


  /**
   * Sets whether or not to duplicate the control points of the returned edge paths 
   * that are placed on an path segment of another edge. For example, if an edge
   * has the control points, [a,b,c], and a and b are placed on a shared bus, then the
   * resulting edge path is [a,a,b,b,c]. Duplicating control points on a shared bus,
   * allows the edge rendering facility to treat such control points differently.
   * By default this feature is disabled.
   */
  public void setDuplicateBendsOnSharedBus(boolean duplicateBendsOnSharedBus) {
    this.duplicateBendsOnSharedBus = duplicateBendsOnSharedBus;
  }

  /**
   * Returns whether or not to duplicate the control points of the returned edge paths 
   * that are placed on an path segment of another edge. For example, if an edge 
   * has the control points, [a,b,c], and a and b are placed on a shared bus, then the
   * resulting edge path is [a,a,b,b,c]. Duplicating control points on a shared bus,
   * allows the edge rendering facility to treat such control points differently.
   * By default this feature is disabled.
   */
  public boolean isDuplicateBendsOnSharedBus() {
    return duplicateBendsOnSharedBus;
  }

  /**
   * Assigns coordinates to the elements of the input graph.
   */
  public void doLayout(LayoutGraph graph) {    
    GenericTreeLayouter gtl = new GenericTreeLayouter();
    gtl.setGroupingSupported(true);
    configureNodePlacers(graph);    
    gtl.doLayout(graph);
    if(isDuplicateBendsOnSharedBus()) {
      Layouter bendDuplicator = new BendDuplicatorStage(null);
      bendDuplicator.doLayout(graph);
    }
  }
  
  /**
   * Configures the layout algorithm using the information provided by the
   * DataProviders registered with the keys {@link #ASSISTANT_DPKEY} and {@link #CHILD_LAYOUT_DPKEY}.   
   */
  protected void configureNodePlacers(LayoutGraph graph) {    
    DataProvider childLayoutDP = graph.getDataProvider(CHILD_LAYOUT_DPKEY);
    NodeMap nodePlacerMap = Maps.createHashedNodeMap();     
    if(childLayoutDP != null) {
      graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
      for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        nodePlacerMap.set(n, createNodePlacer(childLayoutDP.get(n)));        
      }
      graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
    }

    DataProvider assistDP = graph.getDataProvider(ASSISTANT_DPKEY);
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();        
      if(assistDP != null && assistDP.getBool(n)) {
        if(n.inDegree() > 0 && n.firstInEdge().source().outDegree() > 1) {
          AssistantPlacer placer = new AssistantPlacer();           
          NodePlacer parentPlacer = (NodePlacer) nodePlacerMap.get(n.firstInEdge().source());
          placer.setChildNodePlacer(parentPlacer);
          nodePlacerMap.set(n.firstInEdge().source(), placer);
        }
      }
    }
    graph.addDataProvider(AssistantPlacer.ASSISTANT_DPKEY, assistDP);     
  }
  
  /**
   * Creates a NodePlacer for the given child layout specifier.
   */
  protected NodePlacer createNodePlacer(Object childLayout) {    
    if(childLayout == CHILD_LAYOUT_LEFT_BELOW) {      
      DefaultNodePlacer placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, DefaultNodePlacer.ALIGNMENT_CENTER, DefaultNodePlacer.ROUTING_FORK, 20.0d, 80.d);
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_VERTICAL_TO_LEFT);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_LEADING_ON_BUS);
      placer.setRoutingStyle(DefaultNodePlacer.ROUTING_FORK_AT_ROOT);
      return placer;
    }
    else if(childLayout == CHILD_LAYOUT_RIGHT_BELOW) {
      DefaultNodePlacer placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, DefaultNodePlacer.ALIGNMENT_CENTER, DefaultNodePlacer.ROUTING_FORK, 20.0d, 80.d);
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_VERTICAL_TO_RIGHT);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_LEADING_ON_BUS);
      placer.setRoutingStyle(DefaultNodePlacer.ROUTING_FORK_AT_ROOT);
      return placer;
    }
    else if(childLayout == CHILD_LAYOUT_BELOW) {
      LeftRightPlacer placer = new LeftRightPlacer();
      placer.setPlaceLastOnBottom(false);
      return placer;
    }
    else { //default
      DefaultNodePlacer placer = new DefaultNodePlacer();
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_MEDIAN);
      return placer;
    }        
  }
    
  /**
   * The input graph needs to be a tree or a collection of trees.
   */
  public boolean canLayout(LayoutGraph graph) {    
    return Trees.isForest(graph); //simplified
  }
  
  /**
   * LayoutStage that duplicates bends that share a common bus.
   */
  static class BendDuplicatorStage extends AbstractLayoutStage {

    public BendDuplicatorStage() {
    }

    public BendDuplicatorStage(Layouter coreLayouter) {
      super(coreLayouter);
    }
    
    public boolean canLayout(LayoutGraph graph) {
      return true;
    }

    public void doLayout(LayoutGraph graph) {
      doLayoutCore(graph);
      
      for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        for(EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
          boolean lastSegmentOverlap = false;      
          EdgeLayout er = graph.getEdgeLayout(ec.edge());
          
          if(er.pointCount() > 0) {
            //last bend point
            YPoint bendPoint = er.getPoint(er.pointCount()-1);
            
            loop:for(EdgeCursor ecc = n.outEdges(); ecc.ok(); ecc.next()) {
              
              if(ecc.edge() != ec.edge()) {
                YPointPath path = graph.getPath(ecc.edge());
                for(YLineSegmentCursor lc = path.lineSegments(); lc.ok(); lc.next()) {
                  LineSegment seg = lc.lineSegment();
                  if(seg.contains(bendPoint)) {
                    lastSegmentOverlap = true;
                    break loop;
                  }
                }
              }
            }      
          }
          
          YList points = graph.getPointList(ec.edge());
          for(ListCell c = points.firstCell(); c != null; c = c.succ()) {
            YPoint p = (YPoint) c.getInfo();
            if(c.succ() == null && !lastSegmentOverlap) {
              break;
            }
            points.insertBefore(new YPoint(p.x,p.y), c);
          }
          graph.setPoints(ec.edge(), points);
        }
      }    
    }
  }
}
