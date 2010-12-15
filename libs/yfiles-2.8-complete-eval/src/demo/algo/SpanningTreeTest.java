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

import y.algo.SpanningTrees;
import y.algo.GraphConnectivity;

import y.util.YRandom;
import y.util.DataProviders;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.Edge;
import y.base.DataProvider;
import y.base.EdgeList;
import y.base.EdgeCursor;

import demo.base.RandomGraphGenerator;

/**
 * This class compares the performance of different minimum spanning tree algorithms.
 */
public class SpanningTreeTest
{
  private static long seed = 0;

  /**
   * Program launcher. Accepts a random seed on the command line.
   */  
  public static void main(String[] args)
  {
    try 
    {
      seed = Long.parseLong(args[0]);
    }
    catch(Exception ex) {}
    
    (new SpanningTreeTest()).testMST();
  }
  

  
  private void testMST()
  {
    D.bug(">>> testMST");
    Timer timerA = new Timer(false);
    Timer timerB = new Timer(false);
    timerA.reset();
    timerB.reset();
    
    YRandom random = new YRandom(seed);
    
    RandomGraphGenerator rg = new RandomGraphGenerator(seed);
    rg.allowCycles(true);
    
    for(int size = 100; size <= 100000; size *= 10)
    {
      for(int trial = 0; trial < 100; trial++)
      {
        if(trial % 60 == 59) D.bug("."); else D.bu(".");
        
        rg.setNodeCount(random.nextInt(1000,2000));
        rg.setEdgeCount(random.nextInt(size/10,size));
        
        Graph G = rg.generate();
        int eCount = GraphConnectivity.makeConnected(G).size();
        
        double[] cost = new double[G.E()];
        
        for(EdgeCursor ec = G.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          int eid = e.index();
          cost[eid] = random.nextInt(100000);
        }
        
        DataProvider c = DataProviders.createEdgeDataProvider(cost);
        
        timerA.start();
        EdgeList resultA = SpanningTrees.kruskal(G,c);
        double costA = SpanningTrees.cost(resultA,c);
        timerA.stop();
        
        timerB.start();
        EdgeList resultB = SpanningTrees.prim(G,c);
        double costB = SpanningTrees.cost(resultB,c);
        timerB.stop();
        
        if(costA != costB)
        {
          D.bug("\ncost mismatch: trial = " + trial);
          D.bug("costA = " + costA + "   costBi = " + costB);
        }
      }
      D.bug("\nsize=" + size + "\nkruskal " + timerA + "\nprim    " + timerB);
      timerA.reset();
      timerB.reset();
    }
    
    D.bug("<<< testMST\n\n");
  }
}
