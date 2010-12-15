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
package demo.view;

import y.base.Node;
import y.view.Arrow;
import y.view.EditMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.ImageNodeRealizer;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

/**
 * <p>
 *  Demonstrates simple usage of {@link Graph2DView}, {@link Graph2D}
 *  and {@link EditMode}.
 * </p>
 * <p>
 *  This demo creates an initial graph by adding nodes and edges
 *  to the {@link Graph2D} displayed by the main {@link Graph2DView}
 *  view using API calls. It further shows how some graphical node
 *  and edge properties can be set (see {@link #buildGraph()}).
 * </p>
 * <p>
 *  Additionally it is shown how the appearance of the default nodes
 *  and edges can be set (see {@link #configureDefaultRealizers()}).
 *  This applies to new nodes and edges added to the initial graph.
 *  Editing the initial graph is possible due to the {@link EditMode}
 *  added to the view.
 * </p>
 */
public class BuildGraphDemo extends JPanel 
{
  Graph2DView view;
  
  public BuildGraphDemo()
  {
    setLayout(new BorderLayout());  
    view = new Graph2DView();
    EditMode mode = new EditMode();
    view.addViewMode(mode);
    add(view);

    configureDefaultRealizers();
    buildGraph();
  }
  
  protected void configureDefaultRealizers()
  {
    Graph2D graph = view.getGraph2D();

    //change the looks of the default edge 
    EdgeRealizer er = graph.getDefaultEdgeRealizer();
    //a standard (target) arrow
    er.setArrow(Arrow.STANDARD); 

    //change the looks (and type) of the default node
    ShapeNodeRealizer snr = new ShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT);
    snr.setSize(80, 30);
    snr.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);

    //use it as default node realizer
    graph.setDefaultNodeRealizer(snr);
  }

  void buildGraph()
  {
    Graph2D graph = view.getGraph2D();
    
    //register an image with ImageNodeRealizer.
    //must be a path name relative to your java CLASSPATH.
    ImageNodeRealizer inr = new ImageNodeRealizer();
    //set the image
    inr.setImageURL(getClass().getResource("resource/yicon.png"));
    //set node size equals to half of original image size
    inr.setToImageSize();
    inr.setSize(inr.getWidth()/2, inr.getHeight()/2);
    inr.setLocation(60, 200);
    //set a label text
    inr.setLabelText("yFiles");

    //set the label model to be 8-pos (eight available positions around node)
    inr.getLabel().setModel(NodeLabel.EIGHT_POS);

    //set the label position (S == South of Node)
    inr.getLabel().setPosition(NodeLabel.S);

    //create a node that displays the image
    Node v = graph.createNode(inr);



    //create some edges and new nodes
    for (int i = 0; i < 5; i++) {
      Node w = graph.createNode();

      //customize position and label of the created node
      NodeRealizer nr = graph.getRealizer(w);
      nr.setLocation(300, 100 + i*50);
      nr.setLabelText("Node " + (i+1));

      graph.createEdge(v, w);

      //decorations for the created edge
      EdgeRealizer er = graph.getRealizer(graph.lastEdge());
      if (i % 2 == 0) {
        //set diamond source arrow
        er.setSourceArrow(Arrow.WHITE_DIAMOND);
      } else {
        //a label for the edge
        er.getLabel().setText("Edge " + (i + 1));
      }
    }
  }

  public void start()
  {
    JFrame frame = new JFrame(getClass().getName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo( final JRootPane rootPane )
  {
    rootPane.setContentPane(this);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        (new BuildGraphDemo()).start();
      }
    });
  }
}
