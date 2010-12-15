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

import y.base.Graph;
import y.base.Edge;
import y.base.Node;
import y.base.EdgeList;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.NodeMap;
import y.util.D;
import y.util.YRandom;

import demo.base.RandomGraphGenerator;

import y.algo.ShortestPaths;



/**
 * Demonstrates the usage of the ShortestPaths class that
 * provides easy to use algorithms for finding shortest paths 
 * within weighted graphs.
 *
 */
public class ShortestPathDemo
{
  /**
   * Main method:
   * <p>
   * Usage: java demo.algo.ShortestPathDemo &lt;nodeCount&gt; &lt;edgeCount&gt;
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
    
    // Create a random graph with the given edge and node count
    RandomGraphGenerator randomGraph = new RandomGraphGenerator(0L);
    randomGraph.setNodeCount(nodeCount);
    randomGraph.setEdgeCount(edgeCount);
    Graph graph = randomGraph.generate();
    
    // Create an edgemap and assign random double weights to 
    // the edges of the graph.
    EdgeMap weightMap = graph.createEdgeMap();
    YRandom random = new YRandom(0L);
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      Edge e = ec.edge();
      weightMap.setDouble(e,100.0*random.nextDouble());
    }
    
    
    
    // Calculate the shortest path from the first to the last node
    // within the graph
    if(!graph.isEmpty())
    {
      
      Node from = graph.firstNode();
      Node to   = graph.lastNode();
      EdgeList path;
      double sum = 0.0;
      
      // The undirected case first, i.e. edges of the graph and the
      // resulting shortest path are considered to be undirected
      
      path = ShortestPaths.singleSourceSingleSink(graph, from, to, false, weightMap);
      for(EdgeCursor ec = path.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        double weight = weightMap.getDouble( e );
        D.bug( e + " weight = " + weight );
        sum += weight;
      }
      if(sum == 0.0)
        D.bug("NO UNDIRECTED PATH");
      else
        D.bug("UNDIRECTED PATH LENGTH = " + sum);
      
      
      // Next the directed case, i.e. edges of the graph and the
      // resulting shortest path are considered to be directed.
      // Note that this shorteszt path can't be shorter then the one
      // for the undirected case
      
      path = ShortestPaths.singleSourceSingleSink(graph, from, to, true, weightMap );
      sum = 0.0;
      for(EdgeCursor ec = path.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        double weight = weightMap.getDouble( e );
        D.bug( e + " weight = " + weight );
        sum += weight;
      }
      if(sum == 0.0)
        D.bug("NO DIRECTED PATH");
      else
        D.bug("DIRECTED PATH LENGTH = " + sum);
      
      
      D.bug("\nAuxiliary distance test\n");
      
      NodeMap distanceMap = graph.createNodeMap();
      NodeMap predMap     = graph.createNodeMap();
      ShortestPaths.singleSource(graph, from, true, weightMap, distanceMap, predMap);
      if(distanceMap.getDouble(to) == Double.POSITIVE_INFINITY)
        D.bug("Distance from first to last node is infinite");
      else
        D.bug("Distance from first to last node is " + distanceMap.getDouble(to));
      
      // Dispose distanceMap since it is not needed anymore
      graph.disposeNodeMap(distanceMap);
      
    }
    
    // Dispose weightMap since it is not needed anymore
    graph.disposeEdgeMap( weightMap );
    
  }
  
  static void usage()
  {
    System.err.println("Usage: java demo.algo.ShortestPathDemo <nodeCount> <edgeCount>");
    System.err.println("Usage: Both <nodeCount> and <edgeCount> must be integral values.");
    System.exit(1);
  }
  
}
