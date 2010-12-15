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


import demo.base.RandomGraphGenerator;
import y.algo.GraphChecker;
import y.algo.ShortestPaths;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.util.D;
import y.util.Timer;
import y.util.YRandom;


/**
 * This class compares the performance and results of some shortest path
 * algorithms available in yFiles.
 */
public class ShortestPathTest {
  static long seed = 0;

  private Timer timerA = new Timer( false );
  private Timer timerB = new Timer( false );
  private Timer timerC = new Timer( false );
  private Timer timerD = new Timer( false );

  /**
   * Program launcher. Accepts a random seed on the command line.
   */
  public static void main( String[] args ) {
    try {
      seed = Long.parseLong( args[0] );
    }
    catch ( Exception ex ) {
    }

    (new ShortestPathTest()).doIt();

  }

  private void doIt() {
    testSP( true );
    testAllPairs( true );
    testSingleSourceSingleSink( true );
    testSP( false );
    testAllPairs( false );
    testSingleSourceSingleSink( false );
  }


  /**
   * Tests single source single sink shortest path algorithms
   */
  private void testSingleSourceSingleSink( boolean directed ) {
    D.bug( ">>> testSingleSourceSingleSink(" + directed + ")" );

    timerA.reset();
    timerB.reset();

    YRandom random = new YRandom( seed );

    RandomGraphGenerator rg = new RandomGraphGenerator( seed );

    rg.allowCycles( true );
    rg.setNodeCount( random.nextInt( 200 ) );
    rg.setEdgeCount( random.nextInt( 1600 ) );

    Graph G = rg.generate();

    double[] cost = new double[G.E()];
    double[] distA = new double[G.N()];
    double[] distB = new double[G.N()];

    for ( EdgeCursor ec = G.edges(); ec.ok(); ec.next() ) {
      Edge e = ec.edge();
      int eid = e.index();
      cost[eid] = random.nextInt( 10000 );
    }

    for ( NodeCursor nc = G.nodes(); nc.ok(); nc.next() ) {
      Node v = nc.node();
      if ( v.index() % 60 == 59 ) D.bug( "." );
      else D.bu( "." );
      for ( NodeCursor ncc = G.nodes(); ncc.ok(); ncc.next() ) {
        Node w = ncc.node();
        timerA.start();
        ShortestPaths.dijkstra( G, v, directed, cost, distA );
        timerA.stop();
        timerB.start();
        double dist = ShortestPaths.singleSourceSingleSink( G, v, w, directed, cost, new Edge[G.N()] );
        timerB.stop();
        if ( distA[w.index()] != dist ) {
          D.bug( "\ndist mismatch: v = " + v + "  w = " + w );
          D.bug( "distA = " + distA[w.index()] + "  dist = " + dist );
        }
      }
    }

    D.bug( "\ndijkstra= " + timerA + "\nsource-target-dijkstra " + timerB );

    D.bug( "<<< testSingleSourceSingleSink(" + directed + ")\n\n" );
  }


  /**
   * Compares the built-in all pairs shortest path algorithm with
   * multiple calls to single source shortest path algorithms
   */
  private void testAllPairs( boolean directed ) {
    D.bug( ">>> testAllPairs(" + directed + ")" );

    timerA.reset();
    timerB.reset();

    YRandom random = new YRandom( seed );

    RandomGraphGenerator rg = new RandomGraphGenerator( seed );

    rg.allowCycles( true );
    rg.setNodeCount( random.nextInt( 1000 ) );
    rg.setEdgeCount( random.nextInt( 100000 ) );

    Graph G = rg.generate();

    double[] cost = new double[G.E()];
    double[][] distA = new double[G.N()][G.N()];
    double[] distB = new double[G.N()];


    for ( EdgeCursor ec = G.edges(); ec.ok(); ec.next() ) {
      Edge e = ec.edge();
      int eid = e.index();
      cost[eid] = random.nextInt( 100000 );
    }


    timerA.start();
    ShortestPaths.allPairs( G, directed, cost, distA );
    timerA.stop();


    for ( NodeCursor nc = G.nodes(); nc.ok(); nc.next() ) {
      Node v = nc.node();

      timerB.start();
      ShortestPaths.singleSource( G, v, directed, cost, distB );
      timerB.stop();
      if ( v.index() % 60 == 59 ) D.bug( "." );
      else D.bu( "." );

      for ( NodeCursor ncc = G.nodes(); ncc.ok(); ncc.next() ) {
        Node w = ncc.node();
        if ( distA[v.index()][w.index()] != distB[w.index()] ) {
          D.bug( "dist mismatch! v = " + v + "  w = " + w );
          D.bug( "distA = " + distA[v.index()][w.index()] + "   distB = " + distB[w.index()] );
          System.exit( 1 );
        }

      }
    }
    D.bug( "\nallPairs = " + timerA + "\nsingleSource " + timerB );

    D.bug( "<<< testAllPairs(" + directed + ")\n\n" );
  }

  /**
   * calls testSP with different random seeds.
   */
  private void testSP( boolean directed ) {
    D.bug( ">>> testSP(" + directed + ")" );

    timerA.reset();
    timerB.reset();
    timerC.reset();
    timerD.reset();

    timerD.start();
    for ( int i = 0; i < 100; i++ ) {
      if ( i % 60 == 59 ) D.bug( "." );
      else D.bu( "." );

      testSP( i, directed );
    }
    D.bug( "" );
    timerD.stop();
    D.bug( "overall = " + timerD + "\ndijkstra = " + timerA + "\nbellmanFord = " + timerB + "\nacyclic = " + timerC );

    D.bug( "<<< testSP(" + directed + ")\n\n" );

  }

  /**
   * Compares dijkstra with bellman-ford shortest path algorithms and with
   * special routine for acyclic graphs.
   */
  private void testSP( int loop, boolean directed ) {
    YRandom random = new YRandom( seed + loop );

    RandomGraphGenerator rg = new RandomGraphGenerator( seed + loop );

    rg.allowCycles( false );
    rg.setNodeCount( random.nextInt( 100 ) );
    rg.setEdgeCount( random.nextInt( 5555 ) );

    Graph G = rg.generate();

    //D.bug("\nn="  + G.N() + " m=" + G.E());

    double[] cost = new double[G.edgeCount()];
    double[] distA = new double[G.nodeCount()];
    double[] distB = new double[G.nodeCount()];
    double[] distC = new double[G.nodeCount()];

    for ( EdgeCursor ec = G.edges(); ec.ok(); ec.next() ) {
      Edge e = ec.edge();
      int eid = e.index();
      cost[eid] = random.nextInt( 100000 );
    }

    for ( NodeCursor nc = G.nodes(); nc.ok(); nc.next() ) {
      Node s = nc.node();
      int sid = s.index();
      timerA.start();
      ShortestPaths.dijkstra( G, s, directed, cost, distA );
      timerA.stop();

      timerB.start();
      boolean resultB = ShortestPaths.bellmanFord( G, s, directed, cost, distB );
      timerB.stop();


      boolean resultC = GraphChecker.isAcyclic( G ) && directed;
      timerC.start();
      if ( resultC ) ShortestPaths.acyclic( G, s, cost, distC );
      timerC.stop();
      if ( resultB ) {
        for ( NodeCursor ncc = G.nodes(); ncc.ok(); ncc.next() ) {
          Node w = ncc.node();
          int wid = w.index();

          if ( distA[wid] != distB[wid] ) {
            D.bug( "dist mismatch" );
            D.bug( "" + w + " dijkstra: " + distA[wid] + " bellmanford: " + distB[wid] );
            System.exit( 1 );
          }
        }
      }

      if ( resultC ) {
        for ( NodeCursor ncc = G.nodes(); ncc.ok(); ncc.next() ) {
          Node w = ncc.node();
          int wid = w.index();

          if ( distA[wid] != distC[wid] ) {
            D.bug( "dist mismatch" );
            D.bug( "" + w + " dijkstra: " + distA[wid] + " acyclic: " + distC[wid] );
            System.exit( 1 );
          }
        }
      }

    }
  }

}
