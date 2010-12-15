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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import y.base.Edge;
import y.base.Node;
import y.base.NodeList;
import y.io.GIFIOHandler;
import y.io.ImageMapOutputHandler;
import y.io.LinkInfo;
import y.io.LinkMap;
import y.view.Graph2D;
import y.view.Graph2DView;

/**
 * This class shows how to generate an image and a hyperlinked 
 * HTML image map of a graph.
 */
public class ImageMapDemo
{
  
  public ImageMapDemo(String imageFileName, String htmlFileName)
  {
    Graph2D tree = new Graph2D();
    
    LinkMap linkMap = new LinkMap();
    
    buildTreeFromData(tree, linkMap);
    
    //layout as a tree. Other tree layout algorithms can be found
    //in package y.layout.tree.
    y.layout.tree.TreeLayouter tLayouter = new y.layout.tree.TreeLayouter(); //
    tLayouter.doLayout(tree); //
    
    //output to GIF. Other output handlers can be found in package y.io. 
    GIFIOHandler gifIO = new GIFIOHandler();
    Graph2DView view = gifIO.createDefaultGraph2DView(tree);
    tree.setCurrentView(view);
    
    //use ImageMapOutputHandler to generate an html image map that matches
    //the generated image.
    ImageMapOutputHandler htmlIO = new ImageMapOutputHandler();
    linkMap.setMapName("image");
    htmlIO.setReferences(linkMap);
    
    try
    {
      File file = new File(imageFileName);
      System.out.println("Writing GIF to " + file.getCanonicalPath());
      gifIO.write(tree, imageFileName);
    
      file = new File(htmlFileName);
      System.out.println("Writing HTML to " + file.getCanonicalPath());
      
      PrintWriter htmlOut = new PrintWriter(new FileWriter(htmlFileName));
      String htmlMap = htmlIO.createHTMLString(tree);
      
      //create valid html page that can be displayed in a browser.
      htmlOut.println(
          "<html>\n<head></head>\n<body>" + 
          htmlMap + "\n" +
          "<img src=" + imageFileName + " usemap=\"#image\" border=\"0\">\n" + 
          "</body></html>"); 
      htmlOut.close();
    }
    catch(IOException ioex)
    {
      ioex.printStackTrace();
    }
  }
  
  /**
   * Build a tree structure and provide link hyperlink information
   * for some nodes.
   */
  void buildTreeFromData(Graph2D graph, LinkMap linkMap)
  {
    NodeList queue = new NodeList();
    queue.add(graph.createNode(0,0, 100, 30, "Root"));
    for(int i = 0; i < 10; i++)
    {
      Node root = queue.popNode();
      LinkInfo link = new LinkInfo();
      link.setAttribute(LinkInfo.HTML_REFERENCE, "http://www.yworks.com");
      link.setAttribute(LinkInfo.HTML_ALT, "Visit yWorks");
      link.setAttribute(LinkInfo.HTML_TITLE, "Visit yWorks");
      linkMap.put(root, link);
      Node c1 = graph.createNode(0,0, 80, 30, "c1_" + graph.N());
      Edge e1 = graph.createEdge(root, c1);
      Node c2 = graph.createNode(0,0, 60, 30, "c2_" + graph.N());
      Edge e2 = graph.createEdge(root, c2);
      
      linkMap.put(e1, link);
      linkMap.put(e2, link);

      queue.add(c2);
      queue.add(c1);
    }
  }
  
  public static void main(String[] args)
  {
    ImageMapDemo demo = new ImageMapDemo("ImageMapDemo.gif","ImageMapDemo.html");
  }
 
}

    

      
