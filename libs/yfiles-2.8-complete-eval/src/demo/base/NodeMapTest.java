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

import java.util.HashMap;
import java.util.Map;
import y.util.Maps;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.NodeMap;
import y.base.NodeCursor;
import y.base.Node;


/**
 * This class compares the performance of different 
 * mechanisms to bind extra data to the nodes of a graph.
 * In scenarios where the indices of the nodes do not change
 * while the extra node data is needed, it is best to use array based
 * mechanisms that use the index of a node to access the data.
 * <br>
 * In scenarios where the indices of the nodes will change
 * while the extra node data is needed, it is necessary to
 * use {@link y.base.NodeMap} implementations that do not depend on the indices
 * of the nodes (see {@link Node#index()}) or {@link java.util.Map}
 * implementations like {@link java.util.HashMap} provided by the java
 * collections framework.
 */
public class NodeMapTest
{
  
  static Timer t1 = new Timer(false);
  static Timer t2 = new Timer(false);
  static Timer t3 = new Timer(false);
  static Timer t4 = new Timer(false);
  static Timer t5 = new Timer(false);
  
  public static void main(String[] args)
  {
    test1();
  }
  
  static void test1()
  {
    Graph graph = new Graph();
    for(int i = 0; i < 20000; i++) graph.createNode();
    
    for(int loop = 0; loop < 10; loop++)
    {
      D.bu(".");
      
      t1.start();
      NodeMap map = graph.createNodeMap();
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v,i);
          i = map.getInt(v);
        }
      }
      graph.disposeNodeMap(map);
      t1.stop();
      
      
      t2.start();
      map = Maps.createIndexNodeMap(new int[graph.N()]);
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v,i);
          map.getInt(v);
        }
      }
      t2.stop();
      
      
      t3.start();
      map = Maps.createHashedNodeMap(); 
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v, i);
          i = map.getInt(v);
        }
      }
      t3.stop();
      
      t4.start();
      int[] array = new int[graph.N()];
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          int vid = nc.node().index();
          array[vid] = i;
          i = array[vid];
        }
      }
      t4.stop();

    
      t5.start();
      Map jmap = new HashMap(2*graph.N()+1); //use hash map with good initial size
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          jmap.put(v, new Integer(i));
          i = ((Integer)jmap.get(v)).intValue();
        }
      }
      t5.stop();
      
    }
    
    D.bug("");
    D.bug("TIME:  standard NodeMap: " + t1);
    D.bug("TIME:  index    NodeMap: " + t2);
    D.bug("TIME:  hashed   NodeMap: " + t3);
    D.bug("TIME:  plain array     : " + t4);
    D.bug("TIME:  HashMap         : " + t5);
  }
}

    
    
    
