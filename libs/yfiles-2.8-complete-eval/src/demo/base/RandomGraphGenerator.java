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

import y.util.YRandom;
import y.base.Graph;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

/**
 * A class that creates random graphs. The size of the graph and other options
 * may be specified. These options influence the properties of the created
 * graph.
 *
 */
public class RandomGraphGenerator 
{
  private int nodeCount;
  private int edgeCount;
  private boolean allowCycles;
  private boolean allowSelfLoops;
  private boolean allowMultipleEdges;
  
  private YRandom random;
  
  /** Constructs a new random graph generator */
  public RandomGraphGenerator()
  {
    this(System.currentTimeMillis());
  }
  
  /** 
   * Constructs a new random graph generator that uses
   * the given random seed to initialize.
   */
  public RandomGraphGenerator(long seed)
  {
    random = new YRandom(seed);
    nodeCount = 30;
    edgeCount = 40;
    allowSelfLoops = false;
    allowCycles    = true;
    allowMultipleEdges = false;
  }
  
  /**
   * Sets the random seed for this generator.
   */
  public void setSeed(long seed)
  {
    random.setSeed(seed);
  }
  
  /**
   * Sets the node count of the graph to be generated.
   * The default value is 30.
   */
  public void setNodeCount(int nodeCount)
  {
    this.nodeCount = nodeCount;
  }
  
  /**
   * Sets the edge count of the graph to be generated.
   * The default value is 40. If the edge count is 
   * higher than it is theoretically possible by the 
   * generator options set, then the highest possible
   * edge count is applied instead.
   */
  public void setEdgeCount(int edgeCount)
  {
    this.edgeCount = edgeCount;
  }
  
  /**
   * Returns the edge count of the graph to be generated.
   */
  public int getEdgeCount()
  {
    return edgeCount;
  }
  
  /**
   * Returns the node count of the graph to be generated.
   */
  public int getNodeCount()
  {
    return nodeCount;
  }
  
  /**
   * Whether or not to allow the generation of cyclic graphs, i.e. 
   * graphs that contain directed cyclic paths. If allowed 
   * it still could happen by chance that the generated
   * graph is acyclic. By default allowed.
   */
  public void allowCycles(boolean allow)
  {
    this.allowCycles = allow;
  }
  
  /**
   * Returns whether or not to allow the generation of cyclic graphs.
   */
  public boolean allowCycles()
  {
    return allowCycles;
  }
  
  /**
   * Whether or not to allow the generation of selfloops, i.e.
   * edges with same source and target nodes.
   * If allowed it still could happen by chance that
   * the generated graph contains no selfloops.
   * By default disallowed.
   */
  public void allowSelfLoops(boolean allow)
  {
    this.allowSelfLoops = allow;
  }
  
  /**
   * Returns whether or not to allow the generation of selfloops.
   */  
  public boolean allowSelfLoops()
  {
    return allowSelfLoops;
  }
  
  /**
   * Whether or not to allow the generation of graphs that contain multiple
   * edges, i.e. graphs that has more than one edge that connect the same pair
   * of nodes. If allowed it still could happen by chance that
   * the generated graph does not contain multiple edges.
   * By default disallowed.
   */
  public void allowMultipleEdges(boolean allow)
  {
    this.allowMultipleEdges = allow;
  }
  
  /** 
   * Returns whether or not to allow the generation of graphs that contain
   * multiple edges.
   */
  public boolean allowMultipleEdges()
  {
    return allowMultipleEdges;
  }
  
  /** 
   * Returns a newly created random graph that obeys the specified settings.
   */
  public Graph generate()
  {
    Graph graph = new Graph();
    generate(graph);
    return graph;
  }
  
  
  /**
   * Clears the given graph and generates new nodes and edges for it,
   * so that the specified settings are obeyed.
   */
  public void generate(Graph graph)
  {
    if(allowMultipleEdges)
    {
      generateMultipleGraph(graph);
    }
    else if(nodeCount > 1 && edgeCount > 10 && Math.log(nodeCount)*nodeCount < edgeCount)
    { 
      generateDenseGraph(graph);
    }
    else
    {
      generateSparseGraph(graph);
    }
  }
  
  /**
   * Random graph generator in case multiple edges are allowed.
   */
  private void generateMultipleGraph(Graph G)
  {
    
    int n = nodeCount;
    int m = edgeCount;

    int[] deg = new int[n];
    Node[] V = new Node[n];
    for(int i = 0; i < n; i++) V[i] = G.createNode();
    
    for(int i = 0; i < m; i++) deg[random.nextInt(n)]++;
    for(int i = 0; i < n; i++)
    {
      Node v = V[i];
      int d = deg[i];
      while( d > 0 )
      {
        int j = random.nextInt(n);
        if( j == i && (!allowCycles || !allowSelfLoops)) continue;
        G.createEdge(v,V[j]);
        d--;
      }
    }
    
    if(!allowCycles)
    {
      for(EdgeCursor ec = G.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        if(e.source().index() > e.target().index())
          G.reverseEdge(e);
      }
    }
  }
  
  
  /**
   * Random graph generator for dense graphs.
   */
  private void generateDenseGraph(Graph g)
  {
    g.clear();
    Node[] nodes = new Node[nodeCount];
    
    for(int i = 0; i < nodeCount; i++)
      nodes[i] = g.createNode();
    
    random.permutate(nodes);
        
    int m = Math.min(getMaxEdges(),edgeCount);
    int n = nodeCount;
    
    int adder = (allowSelfLoops && allowCycles) ? 0 : 1;
    
    boolean[] edgeWanted = random.getBoolArray(getMaxEdges(),m);
    for(int i = 0, k = 0; i < n; i++)
      for(int j = i + adder ; j < n; j++, k++)
      {
        if(edgeWanted[k])
        {
          if(allowCycles && random.nextFloat() > 0.5f)
            g.createEdge(nodes[j],nodes[i]);
          else
            g.createEdge(nodes[i],nodes[j]);
        }
      }
  }
  
 
  /**
   * Random graph generator for sparse graphs.
   */
  private void generateSparseGraph(Graph G)
  {
    G.clear();
    
    int n = nodeCount;
    
    int m = Math.min(getMaxEdges(),edgeCount);
    
    Node[] V = new Node[n];
    
    for(int i = 0; i < n; i++) 
    {
      V[i] = G.createNode();
    }
    random.permutate(V);

    int count = m;
    while (count > 0)
    {
      int vi = random.nextInt(n);
      Node v = V[vi];
      Node w = V[random.nextInt(n)];
      if ( v.getEdge(w) != null || (v == w && (!allowSelfLoops || !allowCycles))) {
        continue;
      }
      G.createEdge(v,w);
      count--;
    }
    
    if(!allowCycles)
    {
      for(EdgeCursor ec = G.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        if(e.source().index() > e.target().index())
          G.reverseEdge(e);
      }
    }
  }

  /**
   * Helper method that returns the maximum number of edges
   * of a graph that still obeys the set structural constraints.
   */
  private int getMaxEdges()
  {
    if(allowMultipleEdges) return Integer.MAX_VALUE;
    int maxEdges = nodeCount*(nodeCount-1)/2;
    if(allowCycles && allowSelfLoops) maxEdges += nodeCount;
    return maxEdges;
  }
  
  /**
   * Startup routine. Creates a random graph and outputs
   * it to the console.
   */
  public static void main(String[] args)
  {
    RandomGraphGenerator rgg = new RandomGraphGenerator();
    rgg.setNodeCount(10);
    rgg.setEdgeCount(20);

    Graph randomGraph = rgg.generate();
    System.out.println(randomGraph);
  }
}



