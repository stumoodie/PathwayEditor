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
package demo.view.hierarchy;

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;
import y.view.*;
import y.base.*;

/**
 * A MouseListener that listens for double click events on a JTree.
 * The node item that was clicked will be focused in an 
 * associated Graph2DView.
 */ 
public class HierarchyJTreeDoubleClickListener extends MouseAdapter
{
  Graph2DView view;
  
  public HierarchyJTreeDoubleClickListener(Graph2DView view)
  {
    this.view = view;
  }
  
  public void mouseClicked(MouseEvent e)
  {
    JTree tree =(JTree)e.getSource();
    
    if(e.getClickCount() == 2)
    {
      //D.bug("right mouse pressed");
      
      int y = e.getY();
      int x = e.getX();
      TreePath path = tree.getPathForLocation(x,y);
      if(path != null)
      {
        Object last =  path.getLastPathComponent();
        Graph2D focusedGraph = null;
        Node v = null;
        
        if(last instanceof Node)
        {
          v = (Node)last;
          focusedGraph = (Graph2D)v.getGraph();
        }
        else if(last instanceof Graph2D) //root
        {
          focusedGraph = (Graph2D)last;
        }
        
        if(focusedGraph != null)
        {
          view.setGraph2D(focusedGraph);
          if(v != null)
          {
            view.setCenter(focusedGraph.getCenterX(v),focusedGraph.getCenterY(v));
          }
          view.updateView();
        }
      }
    }
  }
}
