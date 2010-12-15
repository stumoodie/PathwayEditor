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
package demo.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import y.base.Edge;
import y.base.Node;
import y.base.NodeList;
import y.io.JPGIOHandler;
import y.io.TiledImageOutputHandler;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.hierarchy.HierarchyManager;

/**
 * This class shows how to export a diagram to multiple image tiles. 
 * Also, this demo shows how to add a title to the exported diagram.
 * Executing the demo will generate multiple JPG images that make up a 
 * diagram. Additionally, a HTML file will be produced that displays the
 * generated image tiles properly arranged in a table. 
 * Adding a title to the diagram is implemented by enlarging the image
 * size and adding a title Drawable to the view.   
 */
public class TiledImageDemo
{
  
  public TiledImageDemo(String imageFileBase)
  {
    Graph2D diagram = new Graph2D();
    
    generateDiagram(diagram);
    
    
    //output to JPG. Other output handlers can be found in package y.io. 
    JPGIOHandler jpgIO = new JPGIOHandler();
    Graph2DView view = jpgIO.createDefaultGraph2DView(diagram);
    
    //add title to image
    Point2D vp = view.getViewPoint2D();
    view.setSize(view.getWidth(), (int)(view.getHeight()+50));
    view.setViewPoint2D(vp.getX(), vp.getY()-50/view.getZoom());
    Rectangle rect = view.getVisibleRect();
    TitleDrawable td = new TitleDrawable(imageFileBase);
    td.setFrame(rect.x,rect.y,rect.width,50/view.getZoom());  
    view.addDrawable(td);
  
    diagram.setCurrentView(view);
    
    TiledImageOutputHandler tiledIO = new TiledImageOutputHandler(jpgIO);
    tiledIO.setMaximumTileSize(500,500);
    tiledIO.setHTMLTableGenerationActive(true);
    
    try
    {
      File file = new File(imageFileBase + ".html");
      System.out.println("Writing HTML table for tiled images: " + file.getCanonicalPath());
      tiledIO.write(diagram, imageFileBase + ".jpg");
    }
    catch(IOException ioex)
    {
      ioex.printStackTrace();
    }
    
  }
  
  /**
   * Drawable implementation that displays a title for a diagram.
   */ 
   static class TitleDrawable extends Rectangle implements Drawable {
    String title;
    TitleDrawable(String title) {
      this.title = title;
    }
    public void paint(Graphics2D g)
    {
      g.setColor(Color.lightGray);
      g.fillRect(x,y,width,height);
      g.setColor(Color.black);
      Font f = new Font("Dialog", Font.PLAIN, (int)(0.8*height));
      TextLayout tl = new TextLayout(title,f, g.getFontRenderContext());
      Rectangle2D rect = tl.getBounds();
      tl.draw(g, (float)(x+(width-rect.getWidth())/2.0), (float)(y-rect.getY()+(height-rect.getHeight())/2.0));
    }
  };

  /**
   * Build a tree structure and provide link hyperlink information
   * for some nodes.
   */
  void generateDiagram(Graph2D graph)
  {
    HierarchyManager hm = new HierarchyManager(graph);

    NodeList queue = new NodeList();
    queue.add(graph.createNode(0,0, 100, 30, "Root"));
    for(int i = 0; i < 100; i++)
    {
      Node root = queue.popNode();
      Node c1 = graph.createNode(0,0, 80, 30, "c1_" + graph.N());
      Edge e1 = graph.createEdge(root, c1);
      Node c2 = graph.createNode(0,0, 60, 30, "c2_" + graph.N());
      Edge e2 = graph.createEdge(root, c2);
      queue.add(c2);
      queue.add(c1);
    }

    //layout as a tree. 
    y.layout.tree.TreeLayouter tLayouter = new y.layout.tree.TreeLayouter(); //
    tLayouter.doLayout(graph); //
    
  }
  
  public static void main(String[] args)
  {
    TiledImageDemo demo = new TiledImageDemo("TiledImageDemo");
  }
 
}

    

      
