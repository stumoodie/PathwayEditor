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

package demo.base;

import y.base.EdgeMap;
import y.base.Graph;
import y.base.Edge;
import y.base.NodeMap;
import y.base.Node;
import y.util.YRandom;
import y.base.EdgeCursor;
import y.base.NodeCursor;
import y.util.D;

/**
 * This class represents an extended Graph object whose nodes
 * and edges carry additional data. The yFiles way of adding additional 
 * features to nodes and edges is not to extend the node and edge
 * objects themselves but to extend the graph that contains the
 * nodes and edges. The graph stores the extra data in internal Node-
 * and/or EdgeMaps. Access to the additional node and edge data is provided
 * by setter and getter methods of the extended graph.
 * <br>
 * There is a main method in this class that serves as a test driver for 
 * the implementation.
 */
public class ExtendedGraph extends Graph
{
  /**
   * internal NodeMap that stores additional node data
   */
  private NodeMap extraNodeData;
  
  /**
   * internal EdgeMap that stores additional edge data
   */
  private EdgeMap extraEdgeData;
 
  /////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTION /////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new instance of ExtendedGraph */
  public ExtendedGraph()
  {
    extraNodeData = createNodeMap();
    extraEdgeData = createEdgeMap();
  }
  
  /** Creates a graph of type ExtendedGraph */
  public Graph createGraph()
  {
    return new ExtendedGraph();
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // SETTER AND GETTER ////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  
  /**
   * Returns the edge weight associated with the given edge.
   * By default 0 will be returned.
   */
  public double getEdgeWeight(Edge e)
  {
    return extraEdgeData.getDouble(e);
  }
  
  /**
   * Sets the edge weight associated with the given edge.
   */
  public void setEdgeWeight(Edge e, double weight)
  { 
    extraEdgeData.setDouble(e, weight);
  }
  
  /**
   * Returns the node info associated with the given node.
   *
   * By default 0 will be returned.
   */
  public NodeInfo getNodeInfo(Node v)
  {
    NodeInfo info = (NodeInfo)extraNodeData.get(v);
    
//lazy default initialisation could be performed here 
//    if(info == null)
//    {
//      info = new NodeInfo();
//      setNodeInfo(v, info);
//    }

    return info;

  }
  
  public void setNodeInfo(Node v, NodeInfo info)
  {
    extraNodeData.set(v, info);
  }

  public static class NodeInfo
  {
    public String name;
    public String type;
    public int    version;
  
    public NodeInfo()
    {
      this("unknown","unknown",0);
    }
    
    public NodeInfo(String name, String type, int version)
    {
      this.name = name;
      this.type = type;
      this.version = version;
    }
    
    public String toString()
    {
      return "Name=" + name + "  Type=" + type + "  Version=" + version;
    }
  }
  
  
  public static void main(String[] args)
  {
     ExtendedGraph graph = new ExtendedGraph();
     RandomGraphGenerator rg = new RandomGraphGenerator(0);
     rg.setNodeCount(10);
     rg.setEdgeCount(10);
     rg.generate(graph);
     YRandom random = new YRandom(0);
     for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
     {
       graph.setEdgeWeight(ec.edge(), random.nextDouble(0.0,10.0));
     }
     for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
     {
       Node v = nc.node();
       NodeInfo info = new NodeInfo("Node #" + v.index(),"Extra Node",1);
       graph.setNodeInfo(v, info);
     }
     
     for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
     {
       D.bug(graph.getNodeInfo(nc.node()));
     }
     for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
     {
       D.bug("Edge " + ec.edge().index() + " weight= " + graph.getEdgeWeight(ec.edge()));
     }
     
  }
}
