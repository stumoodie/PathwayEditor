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
import y.algo.Cycles;
import y.algo.GraphChecker;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.util.D;
import y.util.Maps;
import y.util.Timer;

/**
 * Tests consistency and performance of two different cycle detection mechanisms
 * in yFiles.
 */
public class CyclesTest {

  private Timer t1 = new Timer( false );
  private Timer t2 = new Timer( false );

  private int akku1 = 0;
  private int akku2 = 0;


  public static void main( String[] args ) {
    CyclesTest cyclesTest = new CyclesTest();
    cyclesTest.doIt();
  }

  private void doIt() {
    for ( int i = 0; i < 1000; i++ ) {
      D.bug( "test " + i );
      test( i );
    }

    D.bug( "overall reversed edges (default method) " + akku1 + "    time: " + t1 );
    D.bug( "overall reversed edges (dfs     method) " + akku2 + "    time: " + t2 );
  }

  private void test( int seed ) {
    RandomGraphGenerator rg = new RandomGraphGenerator( seed );
    rg.setNodeCount( 100 );
    rg.setEdgeCount( 300 );
    rg.allowCycles( true );

    Graph graph1 = rg.generate();

    EdgeMap cycleEdge = Maps.createIndexEdgeMap( new boolean[graph1.E()] );

    //find a set of edges whose reversal make the given graph
    //acyclic.  reverse whose edges
    t1.start();
    Cycles.findCycleEdges( graph1, cycleEdge );
    int count1 = 0;
    for ( EdgeCursor ec = graph1.edges(); ec.ok(); ec.next() ) {
      if ( cycleEdge.getBool( ec.edge() ) ) {
        graph1.reverseEdge( ec.edge() );
        count1++;
      }
    }
    t1.stop();

    //check acyclicity of graph
    if ( GraphChecker.isCyclic( graph1 ) ) {
      D.bug( "graph1 still contains cycles!!!" );
      EdgeList cycle = Cycles.findCycle( graph1, true );
      error( "cycle = " + cycle );
    }


    rg.setSeed( seed );
    Graph graph2 = rg.generate();

    //use alternative DFS based method to detect
    //with a set of cyclicity edges. 
    t2.start();
    Cycles.findCycleEdgesDFS( graph2, cycleEdge );
    int count2 = 0;
    for ( EdgeCursor ec = graph2.edges(); ec.ok(); ec.next() ) {
      if ( cycleEdge.getBool( ec.edge() ) ) {
        graph2.reverseEdge( ec.edge() );
        count2++;
      }
    }
    t2.stop();

    if ( GraphChecker.isCyclic( graph2 ) ) {
      D.bug( "graph2 still contains cycles!!!" );
      EdgeList cycle = Cycles.findCycle( graph2, true );
      error( "cycle = " + cycle );
    }

    akku1 += count1;
    akku2 += count2;
  }

  private void error( String msg ) {
    D.bug( msg );
    System.exit( 666 );
  }
}
