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
package demo.algo;

import y.algo.Dfs;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

import demo.base.RandomGraphGenerator;

/**
 * This class demonstrates how to sort the node set of an acyclic graph
 * topologically. 
 * A topological node order <CODE>S</CODE> of an 
 * acyclic graph <CODE>G</CODE>
 * has the property that for each node <CODE>v</CODE> of <CODE>G</CODE> 
 * all of its successors have a higher rank than <CODE>v</CODE> in
 * <CODE>S</CODE>.
 * <br>
 * The main purpose of this demo is to show how the generic Depth First Search
 * class ({@link y.algo.Dfs}) can be utilized to implement more sophisticated 
 * graph algorithms.
 *
 */

public class TopologicalSortDemo
{
  /**
   * Main method:
   * <p>
   * Usage: java demo.algo.TopologicalSortDemo &lt;nodeCount&gt; &lt;edgeCount&gt;
   * </p><p>
   * the first argument gives the desired node count of the graph 
   * and the second argument gives the desired edge count of the 
   * graph.
   * </p>
   */
  public static void main(String[] args)
  {
    int nodeCount = 30;
    int edgeCount = 60;
    
    if(args.length == 2) {
      try {
        nodeCount = Integer.parseInt(args[0]);
        edgeCount = Integer.parseInt(args[1]);
      } catch(NumberFormatException ex) {
        usage();
      }
    }
    
    // Create a random acyclic graph with the given edge and node count
    RandomGraphGenerator randomGraph = new RandomGraphGenerator(0L);
    randomGraph.setNodeCount(nodeCount);
    randomGraph.setEdgeCount(edgeCount);
    randomGraph.allowCycles( false ); //create a DAG
    Graph graph = randomGraph.generate();
    
    final NodeList tsOrder = new NodeList();
    
    if(!graph.isEmpty())
    {
      // find start node with indegree 0
      Node startNode = graph.firstNode();
      for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
      {
        if(nc.node().inDegree() == 0)
        {
          startNode = nc.node();
          break;
        }
      }
      
      // specialize DFS algorithm to collect topological information
      Dfs dfs = new Dfs() {
        protected void postVisit(Node v, int dfsNum, int compNum)
          {
            tsOrder.addFirst(v);
          }
      };
      
      // put dfs in directed mode
      dfs.setDirectedMode(true);
      // start specialized dfs
      dfs.start(graph, startNode);
      
    }
    
    System.out.println("Topological Order:");
    int index = 0;
    for(NodeCursor nc = tsOrder.nodes(); nc.ok(); nc.next(), index++)
    {
      System.out.println("" + index + ". " + nc.node());
    }
    
  }

  static void usage()
  {
    System.err.println("Usage: java demo.algo.TopologicalSortDemo <nodeCount> <edgeCount>");
    System.err.println("Usage: Both <nodeCount> and <edgeCount> must be integral values.");
    System.exit(1);
  }
}
