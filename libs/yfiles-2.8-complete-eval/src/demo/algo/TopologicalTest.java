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

import y.algo.NodeOrders;

import y.util.YRandom;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.NodeCursor;
import y.base.Node;

import demo.base.RandomGraphGenerator;

/**
 * This class compares different methods that calculate a topological
 * node ordering on the nodes of an acyclic graph.
 **/
public class TopologicalTest
{
  private Timer timerA = new Timer(false);
  private Timer timerB = new Timer(false);
  private Timer timerC = new Timer(false);
  private Timer timerD = new Timer(false);

  public static void main(String[] args)
  {
    (new TopologicalTest()).testTOP();
  }
  

  
  private void testTOP()
  {
    timerA.reset();
    timerB.reset();
    timerC.reset();
    timerD.reset();
    
    timerD.start();
    for(int i = 0; i < 1000; i++)
      testTOP(i);
    timerD.stop();
    D.bug(
      "overall = "    + timerD + 
      "\ngenerate = " + timerC + 
      "\ntopological   = "      + timerA + 
      "\ndfs completion = "      + timerB
      );
  }
  
  private void testTOP(int loop)
  {
    D.bug("test TOP " + loop);
    
    YRandom random = new YRandom(loop);
    
    RandomGraphGenerator rg = new RandomGraphGenerator(loop);
    
    rg.allowCycles(loop % 2 == 0);
    rg.setNodeCount(100);
    rg.setEdgeCount(1000);
    
    
    timerC.start();
    Graph G = rg.generate();
    timerC.stop();
    
    
    int[] topOrderA = new int[G.N()];
    int[] topOrderB = new int[G.N()];
    
    timerA.start();
    boolean resultA = NodeOrders.topological(G,topOrderA);
    timerA.stop();

    if(resultA)
    {
      check("topological", G, topOrderA);
    }
    
    timerB.start();
    NodeOrders.dfsCompletion(G,topOrderB);
    timerB.stop();
    if(resultA)
    {
      check("dfs completion", G, reverse(topOrderB));
    }
  }
  
  
  private int[] reverse(int[] order)
  {
    int[] reverse = new int[order.length];
    for(int i = 0; i < order.length; i++)
    {
      reverse[i] = order.length-1-order[i];
    }
    return reverse;
  }
  
  private void check(String desc, Graph G, int[] topOrder)
  {
    boolean[] tag = new boolean[G.N()];
    for(NodeCursor nc = G.nodes(); nc.ok(); nc.next())
    {
      Node v = nc.node();
      int vid = v.index();
      int order = topOrder[vid];
      if(order < 0 || order >= G.N())
        error(desc + " : order number for " + v + " out of bounds: " + order);
      if(tag[order]) 
        error(desc + " : order number for " + v + " already assigned: " + order);
      for(NodeCursor ncc = v.successors(); ncc.ok(); ncc.next())
      {
        Node u = ncc.node();
        int uid = u.index();
        if(topOrder[uid] <= order)
          error(desc + " : nodes in wrong order!");
      }
      tag[order] = true;
    }
  }

  private void error(String msg)
  {
    D.bug(msg);
    System.exit(1);
  }
}
