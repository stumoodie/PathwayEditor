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

import y.base.Graph;
import y.base.Node;
import y.base.Edge;
import y.base.NodeCursor;
import y.base.EdgeCursor;

import y.base.NodeMap;
import y.base.EdgeMap;


/**
 * Demonstrates how to use the directed graph data type Graph.
 * <p>
 * <b>usage:</b> java demo.base.GraphDemo
 * </p>
 */
public class GraphDemo 
{
  public GraphDemo()
  {
    //instantiates an empty graph
    Graph graph = new Graph();
    
    //create a temporary node array for fast lookup
    Node[] tmpNodes = new Node[5];
    
    //create some nodes in the graph and store references in the array
    for(int i = 0; i < 5; i++)
    {
      tmpNodes[i] = graph.createNode();
    }
    
    //create some edges in the graph
    for(int i = 0; i < 5; i++)
    {
      for(int j = i+1; j < 5; j++)
      {
        //create an edge from node at index i to node at index j
        graph.createEdge(tmpNodes[i],tmpNodes[j]);
      }
    }
    
    
    //output the nodes of the graph 
    System.out.println("The nodes of the graph");
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      Node node = nc.node();
      System.out.println(node);
      System.out.println("in edges #" + node.inDegree());
      for(EdgeCursor ec = node.inEdges(); ec.ok(); ec.next())
      {
        System.out.println(ec.edge());
      }
      System.out.println("out edges #" + node.outDegree());
      for(EdgeCursor ec = node.outEdges(); ec.ok(); ec.next())
      {
        System.out.println(ec.edge());
      }
    }
    
      
    //output the edges of the graph 
    System.out.println("\nThe edges of the graph");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(ec.edge());
    }

    //reverse edges that have consecutive neighbors in graph
    //reversing means switching source and target node
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      if(Math.abs(ec.edge().source().index() - ec.edge().target().index()) == 1) 
        graph.reverseEdge(ec.edge());
    }
    
    System.out.println("\nthe edges of the graph after some edge reversal");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(ec.edge());
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Node- and EdgeMap handling   ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    //create a nodemap for the graph
    NodeMap nodeMap = graph.createNodeMap();
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      //get node at current cursor position
      Node node = nc.node();
      //associate descriptive String to the node via a nodemap 
      nodeMap.set(node,"this is node " + node.index());
    }
    
    //create an edgemap for the graph
    EdgeMap edgeMap = graph.createEdgeMap();
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      //get edge at current cursor position
      Edge edge = ec.edge();
      //associate descriptive String to the edge via an edgemap
      edgeMap.set(edge,"this is edge [" + 
                  nodeMap.get(edge.source()) + "," + 
                  nodeMap.get(edge.target()) + "]");
    }
    
    //output the nodemap values of the nodes
    System.out.println("\nThe node map values of the graph");
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      System.out.println(nodeMap.get(nc.node()));
    }
    
    //output the edges of the graph 
    System.out.println("\nThe edge map values of the graph");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(edgeMap.get(ec.edge()));
    }
    
    //cleanup unneeded node and edge maps again (free resources)
    graph.disposeNodeMap(nodeMap);
    graph.disposeEdgeMap(edgeMap);

    ///////////////////////////////////////////////////////////////////////////
    // removing elements from the graph  //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      //remove node that has a edge degree > 2
      if(nc.node().degree() > 2)
      {
        //removed the node and all of its adjacent edges from the graph
        graph.removeNode(nc.node());
      }
    }
    System.out.println("\ngraph after some node removal");
    System.out.println(graph);
    
    
    
  }
  
  public static void main(String[] args)
  {
    new GraphDemo();
  }
}
