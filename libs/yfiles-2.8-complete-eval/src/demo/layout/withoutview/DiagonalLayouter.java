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
package demo.layout.withoutview;

import java.util.*;

import y.base.Node;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;

import y.layout.EdgeLayout;
import y.layout.LayoutGraph;
import y.layout.CanonicMultiStageLayouter;

import y.geom.YPoint;

/**
 * This class demonstrates how to write a 
 * custom layouter for the yFiles framework.
 * <br>
 * This class lays out a graph in the following style:
 * <br>
 * The nodes of each graph component will be placed on a 
 * diagonal line.
 * Edges will be routed with exactly one bend, so that no 
 * edges that share a common terminal node will cross.
 * <br>
 * See {@link demo.layout.module.DiagonalLayoutModule} for a module wrapper
 * and {@link demo.layout.module.LayoutModuleDemo} for the diagonal layouter in action. 
 */
public class DiagonalLayouter extends CanonicMultiStageLayouter
{
  double minimalNodeDistance = 40;
  
  /**
   * Creates a new instance of DiagonalLayouter
   */
  public DiagonalLayouter()
  {
    //do not use defualt behaviour. we handle parallel edge routing ourselves.  
    setParallelEdgeLayouterEnabled(false);
  }
  
  /**
   * Sets the minimal distance between nodes.
   */
  public void setMinimalNodeDistance(double d)
  {
    minimalNodeDistance = d;
  }
  
  /**
   * Returns the minimal distance between nodes.
   */
  public double getMinimalNodeDistance()
  {
    return minimalNodeDistance;
  }
  
  /**
   * Returns always <code>true</code>, because every graph can be
   * laid out.
   */
  public boolean canLayoutCore(LayoutGraph graph)
  {
    return true;
  }
  
  /**
   * Perform the layout.
   */
  protected void doLayoutCore(LayoutGraph graph)
  {

    //place the nodes on a diagonal line
    Node[] nodes = graph.getNodeArray();
    double offset = 0.0;
    for(int i = 0; i < nodes.length; i++)
    {
      Node v = nodes[i];
      graph.setLocation(v,offset,offset);
      offset += minimalNodeDistance + Math.max(graph.getWidth(v),graph.getHeight(v));
    }
    
    //comparator used to sort edges by the
    //index of their target node
    Comparator outComp = new Comparator() {
      public int compare(Object a, Object b) {
        Node va = ((Edge)a).target();
        Node vb = ((Edge)b).target();
        if(va != vb) 
          return va.index() - vb.index();
        else
          return ((Edge)a).index() - ((Edge)b).index();
      }
    };
    
    //comparator used to sort edges by the
    //index of their source node.
    Comparator inComp = new Comparator() {
      public int compare(Object a, Object b) {
        Node va = ((Edge)a).source();
        Node vb = ((Edge)b).source();
        if(va != vb) 
          return va.index() - vb.index();
        else
          return ((Edge)b).index() - ((Edge)a).index();
      }
    };
    
    //prepare edge layout. use exactly one bend per edge
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      EdgeLayout el = graph.getLayout(ec.edge());
      el.clearPoints();
      el.addPoint(0,0);
    }
    
    //route the edges
    for(int i = 0; i < nodes.length; i++)
    {
      Node v = nodes[i];

      
      EdgeList rightSide  = new EdgeList();
      EdgeList leftSide   = new EdgeList();
      
      //assign x coodinates to all outgoing edges of v
      v.sortOutEdges(outComp);
      for(EdgeCursor ec = v.outEdges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        Node w = e.target();
        
        if(w.index() < v.index())
          rightSide.addLast(e);
        else
          leftSide.addLast(e);
      }
      
      if(!rightSide.isEmpty())
      {
        double space  = graph.getWidth(v)/rightSide.size();
        double xcoord = graph.getX(v) + graph.getWidth(v) - space/2.0;
        for(EdgeCursor ec = rightSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, xcoord, p.getY());
          graph.setSourcePointAbs(e, new YPoint(xcoord, graph.getCenterY(v)));
          xcoord -= space;
        }
      }
      
      if(!leftSide.isEmpty())
      {
        double space  = graph.getWidth(v)/leftSide.size();
        double xcoord = graph.getX(v) + graph.getWidth(v) - space/2.0;
        for(EdgeCursor ec = leftSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, xcoord, p.getY());
          graph.setSourcePointAbs(e, new YPoint(xcoord,graph.getCenterY(v)));
          xcoord -= space;
        }
      }
      
      //assign y coodinates to all ingoing edges of v
      rightSide.clear();
      leftSide.clear();
      v.sortInEdges(inComp);
      for(EdgeCursor ec = v.inEdges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        Node w = e.source();
        
        if(w.index() < v.index())
          leftSide.addLast(e);
        else
          rightSide.addLast(e);
      }
      
      if(!rightSide.isEmpty())
      {
        double space  = graph.getHeight(v)/rightSide.size();
        double ycoord = graph.getY(v) + graph.getHeight(v) - space/2.0;
        for(EdgeCursor ec = rightSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, p.getX(), ycoord);
          graph.setTargetPointAbs(e, new YPoint(graph.getCenterX(v), ycoord));
          ycoord -= space;
        }
      }
      
      if(!leftSide.isEmpty())
      {
        double space  = graph.getHeight(v)/leftSide.size();
        double ycoord = graph.getY(v) + graph.getHeight(v) - space/2.0;
        for(EdgeCursor ec = leftSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, p.getX(), ycoord);
          graph.setTargetPointAbs(e, new YPoint(graph.getCenterX(v), ycoord));
          ycoord -= space;
        }
      }
    }
  }
}
