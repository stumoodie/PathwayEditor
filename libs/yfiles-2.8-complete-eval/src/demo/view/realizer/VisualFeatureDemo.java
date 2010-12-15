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
package demo.view.realizer;

import demo.view.DemoBase;
import y.base.Node;
import y.view.Arrow;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.Drawable;
import y.view.EdgeLabel;
import y.view.Graph2D;
import y.view.ImageNodeRealizer;
import y.view.InterfacePort;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.PolyLineEdgeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.YLabel;
import y.view.QuadCurveEdgeRealizer;
import y.view.Bend;
import y.view.GenericNodeRealizer;
import y.view.ShinyPlateNodePainter;
import y.view.BevelNodePainter;
import y.view.ArcEdgeRealizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.EventQueue;
import java.awt.geom.Ellipse2D;
import java.util.Set;
import java.util.Map;

/**
 * Demonstrates visual features and editor behaviour <ol>
 * <p/>
 * <li>EdgeLabels that display icons and text
 * <p/>
 * <li>Rotated Labels
 * <p/>
 * <li>Auto rotating EdgeLabels
 * <p/>
 * <li>Transparent colors
 * <p/>
 * <li>Gradients
 * <p/>
 * <li>Bridges for crossing PolyLine Edges
 * <p/>
 * <li>InterfacePorts that display icons. (A port defines the logical and visual endpoint of and edge path)
 * <p/>
 * <li>In edit mode you can reposition an edge label by pressing on it with the left mouse button and then by dragging
 * the label around. Possible label candidate boxes will appear along the edge. If you release the mouse button again,
 * the label will snap to the closest of the candidate boxes.
 * <p/>
 * <li>In edit mode you can interactively change the offsets of edge ports. Select the edge that should have different
 * ports. A little black dot will appear at the point where the port has it's logical location. You can drag the black
 * dot around. By doing so, port candidate boxes will appear around the connected node. If you release the mouse again
 * the port will snap to the closest available port candidate position.
 * <p/>
 * <li>In edit mode you can create an edge that has non-zero port offsets by starting edge creation with the shift key
 * pressed down. The point where you press will become the source port location of the edge. If you have the shift key
 * down when you finish edge creation (by releasing the mouse over a node) that the release point will become the offset
 * of the target port of the edge.
 * <p/>
 * </ol>
 */

public class VisualFeatureDemo extends DemoBase {
  private static final String SHINY_NODE_CONFIGURATION = "ShinyNodeConfig";
  private static final String BEVEL_NODE_CONFIGURATION = "BevelNodeConfig";

  public VisualFeatureDemo() {

    final Graph2D graph = view.getGraph2D();


    // show bridges
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(new BridgeCalculator());

    ShapeNodeRealizer defaultNodeRealizer = new ShapeNodeRealizer();
    // configure a drop shadow
    defaultNodeRealizer.setDropShadowColor(new Color(0, 0, 0, 64));
    defaultNodeRealizer.setDropShadowOffsetX((byte) 5);
    defaultNodeRealizer.setDropShadowOffsetY((byte) 5);
    defaultNodeRealizer.setSize(50, 50);
    //set to graph as default
    graph.setDefaultNodeRealizer(defaultNodeRealizer);


    
    //Node 1 to show the line type of the node
    ShapeNodeRealizer node1Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node1Realizer.setCenter(50, 50);
    node1Realizer.setLineColor(Color.BLUE);
    node1Realizer.setLineType(LineType.DASHED_1);
    node1Realizer.setFillColor(Color.ORANGE);
    node1Realizer.setShapeType(ShapeNodeRealizer.DIAMOND);
    final Node node1 = graph.createNode(node1Realizer);


    //Node 2
    ShapeNodeRealizer node2Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node2Realizer.setCenter(250, 50);
    node2Realizer.setLineColor(Color.GRAY);
    node2Realizer.setLineType(LineType.LINE_1);
    node2Realizer.setFillColor2(Color.CYAN);
    node2Realizer.setFillColor(Color.WHITE);
    node2Realizer.setShapeType(ShapeNodeRealizer.DIAMOND);
    final Node node2 = graph.createNode(node2Realizer);


    //Node 3 to show the line type of the node
    ShapeNodeRealizer node3Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node3Realizer.setCenter(400, 50);
    node3Realizer.setLineColor(Color.BLACK);
    node3Realizer.setLineType(LineType.LINE_1);
    node3Realizer.setFillColor(new Color(192, 192, 192, 255));
    node3Realizer.setFillColor2(null);
    node3Realizer.setShapeType(ShapeNodeRealizer.ROUND_RECT);
    final Node node3 = graph.createNode(node3Realizer);

    //Node 4
    ShapeNodeRealizer node4Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node4Realizer.setCenter(600, 50);
    node4Realizer.setLineColor(Color.GRAY);
    node4Realizer.setLineType(LineType.LINE_1);
    node4Realizer.setFillColor(new Color(255, 102, 0, 255));
    node4Realizer.setFillColor2(Color.ORANGE);
    node4Realizer.setShapeType(ShapeNodeRealizer.TRAPEZOID_2);
    final Node node4 = graph.createNode(node4Realizer);

    //Instance of edge realizer that will be default
    PolyLineEdgeRealizer defaultEdgeRealizer = new PolyLineEdgeRealizer();
    graph.setDefaultEdgeRealizer(defaultEdgeRealizer);

    //add an edge between 1 und 2
    PolyLineEdgeRealizer edge1_2Realizer = new PolyLineEdgeRealizer();
    edge1_2Realizer.setLineType(LineType.DASHED_1);
    edge1_2Realizer.setSourceArrow(Arrow.STANDARD);
    edge1_2Realizer.setTargetArrow(Arrow.WHITE_DIAMOND);
    graph.createEdge(node1, node2, edge1_2Realizer);

    //add an edge between 2 und 3
    PolyLineEdgeRealizer edge2_3Realizer = new PolyLineEdgeRealizer();
    edge2_3Realizer.setLineType(LineType.DASHED_DOTTED_2);
    edge2_3Realizer.setSourceArrow(Arrow.NONE);
    edge2_3Realizer.setTargetArrow(Arrow.NONE);
    graph.createEdge(node2, node3,edge2_3Realizer);

    //add an edge between 3 und 4
    PolyLineEdgeRealizer edge3_4Realizer = new PolyLineEdgeRealizer();
    edge3_4Realizer.setLineType(LineType.LINE_1);
    edge3_4Realizer.setSourceArrow(Arrow.STANDARD);
    edge3_4Realizer.setTargetArrow(Arrow.DIAMOND);
    Bend bend1 = edge3_4Realizer.createBend(470.0, 70.0, null, Graph2D.AFTER);
    Bend bend2 = edge3_4Realizer.createBend(530.0, 30.0, bend1, Graph2D.AFTER);
    graph.createEdge(node3, node4,edge3_4Realizer);

    //Node 5
    ShapeNodeRealizer node5Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node5Realizer.setCenter(50, 150);
    node5Realizer.setLineColor(Color.BLACK);
    node5Realizer.setLineType(LineType.LINE_1);
    node5Realizer.setFillColor(Color.ORANGE);
    node5Realizer.setFillColor2(null);
    node5Realizer.setShapeType(ShapeNodeRealizer.TRIANGLE);
    final Node node5 = graph.createNode(node5Realizer);

    //Node 6
    ShapeNodeRealizer node6Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node6Realizer.setCenter(250, 150);
    node6Realizer.setLineColor(Color.BLACK);
    node6Realizer.setLineType(LineType.LINE_1);
    node6Realizer.setFillColor(new Color(255, 204, 0, 255));
    node6Realizer.setShapeType(ShapeNodeRealizer.HEXAGON);
    final Node node6 = graph.createNode(node6Realizer);


    //Configure  new node realizers with specific painters
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map configurationMap = factory.createDefaultConfigurationMap();

    // ShinyPlateNodePainter has an option to draw a drop shadow that is more efficient
    // than wrapping it in a ShadowNodePainter.
    ShinyPlateNodePainter shinyPlateNodePainter = new ShinyPlateNodePainter();
    shinyPlateNodePainter.setRadius(10);
    shinyPlateNodePainter.setDrawShadow(true);
    configurationMap.put(GenericNodeRealizer.Painter.class, shinyPlateNodePainter);
    configurationMap.put(GenericNodeRealizer.ContainsTest.class, shinyPlateNodePainter);
    factory.addConfiguration(SHINY_NODE_CONFIGURATION, configurationMap);

    BevelNodePainter bevelNodePainter = new BevelNodePainter();
    bevelNodePainter.setDrawShadow(true);
    configurationMap.put(GenericNodeRealizer.Painter.class, bevelNodePainter);
    configurationMap.put(GenericNodeRealizer.ContainsTest.class, bevelNodePainter);
    factory.addConfiguration(BEVEL_NODE_CONFIGURATION, configurationMap);


    //Node 7
    GenericNodeRealizer gnr_shiny = new GenericNodeRealizer(SHINY_NODE_CONFIGURATION);
    gnr_shiny.setLineColor(new Color(255, 153, 0, 255));
    gnr_shiny.setFillColor(new Color(255, 153, 0, 255));
    gnr_shiny.setSize(50, 50);
    gnr_shiny.setCenter(400, 150);
    Node node7 = graph.createNode(gnr_shiny);

    //Node 8
    GenericNodeRealizer gnr_bevel = new GenericNodeRealizer(BEVEL_NODE_CONFIGURATION);
    gnr_bevel.setLineColor(new Color(255, 153, 0, 255));
    gnr_bevel.setFillColor(new Color(255, 153, 0, 255));
    gnr_bevel.setSize(50, 50);
    gnr_bevel.setCenter(600, 150);
    Node node8 = graph.createNode(gnr_bevel);


    //add an edge between 1 und 5
    PolyLineEdgeRealizer edge1_5Realizer = new PolyLineEdgeRealizer();
    edge1_5Realizer.setSourceArrow(Arrow.NONE);
    edge1_5Realizer.setTargetArrow(Arrow.SKEWED_DASH);
    graph.createEdge(node1, node5, edge1_5Realizer);

    //add an edge between 5 und 6
    PolyLineEdgeRealizer edge5_6Realizer = new PolyLineEdgeRealizer();
    edge5_6Realizer.setSourceArrow(Arrow.WHITE_DELTA);
    edge5_6Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node5, node6, edge5_6Realizer);

    //add an edge between 6 und 6 (itself)
    PolyLineEdgeRealizer edge6_6Realizer = new PolyLineEdgeRealizer();
    edge6_6Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node6, node6, edge6_6Realizer);

    //add an edge between 6 und 7
    PolyLineEdgeRealizer edge6_7Realizer = new PolyLineEdgeRealizer();
    edge6_7Realizer.setSourceArrow(Arrow.NONE);
    edge6_7Realizer.setTargetArrow(Arrow.NONE);
    graph.createEdge(node6, node7, edge6_7Realizer);

    //add an edge between 7 und 8
    PolyLineEdgeRealizer edge7_8Realizer = new PolyLineEdgeRealizer();
    edge7_8Realizer.setSourceArrow(Arrow.WHITE_DELTA);
    edge7_8Realizer.setTargetArrow(Arrow.WHITE_DIAMOND);
    EdgeLabel labelFor7_8_Edge = edge7_8Realizer.getLabel();
    labelFor7_8_Edge.setText("EDGE LABEL");
    graph.createEdge(node7, node8,edge7_8Realizer);

    //Node 9
    ShapeNodeRealizer node9Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node9Realizer.setCenter(50, 250);
    node9Realizer.setLineColor(Color.BLACK);
    node9Realizer.setLineType(LineType.LINE_1);
    node9Realizer.setFillColor(new Color(153, 204, 255, 255));
    node9Realizer.setFillColor2(null);
    node9Realizer.setShapeType(ShapeNodeRealizer.OCTAGON);
    final Node node9 = graph.createNode(node9Realizer);

    //Node 10 (image node realizer)
    // display an ImageNodeRealizer
    ImageNodeRealizer imageNodeRealizer = new ImageNodeRealizer();
    imageNodeRealizer.setImageURL(getClass().getResource("/demo/view/resource/yicon.png"));
    imageNodeRealizer.setAlphaImageUsed(true);
    imageNodeRealizer.setCenter(250, 250);
    imageNodeRealizer.setToImageSize();
    imageNodeRealizer.setSize(40,50);
    final Node node10 = graph.createNode(imageNodeRealizer);


    // Edge and arrows
    //setup source arrow drawable
    Drawable drawable = new Drawable() {
      public void paint(Graphics2D g) {
        Color color = g.getColor();
        g.setColor(Color.yellow);
        Ellipse2D.Double ellipse = new Ellipse2D.Double(-20, -10, 20, 20);
        g.fill(ellipse);
        g.setColor(Color.orange);
        g.draw(ellipse);
        g.setColor(Color.black);
        g.drawString("A", -13, 5);
        g.setColor(color);
      }

      public Rectangle getBounds() {
        return new Rectangle(-20, -20, 20, 20);
      }
    };

    PolyLineEdgeRealizer edge9_10Realizer = new PolyLineEdgeRealizer();
    edge9_10Realizer.setSourceArrow(Arrow.addCustomArrow("coolArrow", drawable, 20, 3));
    // choose smooth bends
    edge9_10Realizer.setSmoothedBends(true);

    // choose a thicker line
    edge9_10Realizer.setLineType(LineType.LINE_2);

    Icon icon;
    //setup edge label
    EdgeLabel labelForEdge9_10 = edge9_10Realizer.getLabel();
    labelForEdge9_10.setText("rotated edge label");
    labelForEdge9_10.setRotationAngle(15);
    icon = new ImageIcon(getClass().getResource("resource/about24.gif"));
    labelForEdge9_10.setIcon(icon);

    //setup visual source port
    icon = new ImageIcon(getClass().getResource("resource/info24.gif"));
    InterfacePort p = new InterfacePort();
    p.setIcon(icon);
    edge9_10Realizer.setSourcePort(p);

    //setup visual target port
    icon = new ImageIcon(getClass().getResource("resource/home16.gif"));
    p = new InterfacePort();
    p.setIcon(icon);
    edge9_10Realizer.setTargetPort(p);

    //add an edge between 9 und 10
    graph.createEdge(node9, node10, edge9_10Realizer);


    //Node 11
    GenericNodeRealizer gnr_shiny2 = new GenericNodeRealizer(SHINY_NODE_CONFIGURATION);
    gnr_shiny2.setLineColor(new Color(255, 153, 0, 255));
    gnr_shiny2.setFillColor(new Color(255, 153, 0, 255));
    gnr_shiny2.setSize(15, 50);
    gnr_shiny2.setCenter(400, 250);
    Node node11 = graph.createNode(gnr_shiny2);

    //Edge 10 11
    PolyLineEdgeRealizer edge10_11Realizer = new PolyLineEdgeRealizer();
    edge10_11Realizer.setTargetArrow(Arrow.CONVEX);
    graph.createEdge(node10, node11, edge10_11Realizer);

    //Node 12
    ShapeNodeRealizer node12Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node12Realizer.setCenter(600, 250);
    node12Realizer.setLineColor(Color.BLACK);
    node12Realizer.setLineType(LineType.LINE_1);
    node12Realizer.setFillColor(new Color(255, 102, 0, 255));
    node12Realizer.setFillColor2(new Color(255, 153, 0, 255));
    node12Realizer.setShapeType(ShapeNodeRealizer.PARALLELOGRAM);
    final Node node12 = graph.createNode(600, 250);

    //Quadratic curve edge for nodes 11 und 12
    QuadCurveEdgeRealizer quadCurveEdgeRealizer = new QuadCurveEdgeRealizer();
    quadCurveEdgeRealizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node11, node12, quadCurveEdgeRealizer);
    //add symterical bedns on the edge
    double bendStartX = quadCurveEdgeRealizer.getSourceIntersection().getX();
    double bendEndX = quadCurveEdgeRealizer.getTargetIntersection().getX();
    //calculate the y - middle between source and target to alternate the position of the bends
    double bendStartEndY = (quadCurveEdgeRealizer.getSourceIntersection().getY() + quadCurveEdgeRealizer.getTargetIntersection().getY())/2;
    Bend lastBend = null;
    int numberOfBends = 10;
    double deltaX = (bendEndX - bendStartX) / numberOfBends;
    double x = bendStartX;
    double y = 0;
    for (int i = 0; i < numberOfBends; i++) {
      x = x + deltaX;
      if (i % 2 == 0) {
        y = bendStartEndY - 25;
      } else {
        y = bendStartEndY + 25;
      }
      lastBend = quadCurveEdgeRealizer.createBend(x, y, lastBend, Graph2D.AFTER);
    }

    //Node 13
    // reconfigure the default NodeRealizer
    ShapeNodeRealizer node13Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node13Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node13Realizer.setCenter(50, 350);
    node13Realizer.setFillColor(Color.YELLOW);
    final NodeLabel nodeLabel = node13Realizer.createNodeLabel();
    nodeLabel.setText("<html><b><font color=\"red\">HTML</b><br/>labels!</html>");
    node13Realizer.addLabel(nodeLabel);
    nodeLabel.setModel(NodeLabel.SANDWICH);
    nodeLabel.setPosition(NodeLabel.S);
    Node node13 = graph.createNode(node13Realizer);

    //Node 14
    ShapeNodeRealizer node14Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node14Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node14Realizer.setSize(50, 50);
    node14Realizer.setCenter(250,350);
    node14Realizer.setFillColor(Color.red);
    node14Realizer.setFillColor2(Color.yellow);
    final NodeLabel node14Label = node14Realizer.getLabel();
    node14Label.setFontSize(8);
    node14Label.setText("Transparency! and automatically cropped text for custom label size!.");
    Set configurations = NodeLabel.getFactory().getAvailableConfigurations();
    // set a custom configuration for the label
    if (configurations.contains("CroppingLabel")) {
      node14Label.setConfiguration("CroppingLabel");
      node14Label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      node14Label.setContentSize(40, 40);
    }
    node14Label.setRotationAngle(45);
    node14Label.setBackgroundColor(new Color(255, 255, 255, 128));
    node14Label.setLineColor(Color.GRAY);
    Node node14 = graph.createNode(node14Realizer);

    // add an edge from node 13 to 14
    final ArcEdgeRealizer arcEdgeRealizer = new ArcEdgeRealizer();
    arcEdgeRealizer.setTargetArrow(Arrow.STANDARD);
    arcEdgeRealizer.setLineType(LineType.DOTTED_2);
    EdgeLabel edge13_14Label = arcEdgeRealizer.getLabel();
    edge13_14Label.setText("Arc edge");
    edge13_14Label.setModel(EdgeLabel.FREE);
    graph.createEdge(node14, node13, arcEdgeRealizer);
    edge13_14Label.setOffset(-100,0);
    


     //Node 15
    ShapeNodeRealizer node15Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node15Realizer.setShapeType(ShapeNodeRealizer.RECT_3D);
    node15Realizer.setSize(50, 50);
    node15Realizer.setCenter(400,350);
    node15Realizer.setFillColor(Color.red);
    node15Realizer.setFillColor2(Color.yellow);
    final NodeLabel node15Label = node15Realizer.getLabel();
    node15Label.setFontSize(8);
    node15Label.setText("Transparency! and automatically cropped text for custom label size!.");
    
    // set a custom configuration for the label
    if (configurations.contains("CroppingLabel")) {
      node15Label.setConfiguration("CroppingLabel");
      node15Label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      node15Label.setContentSize(50, 50);
    }
    node15Label.setRotationAngle(45);
    node15Label.setBackgroundColor(new Color(255, 255, 255, 128));
    node15Label.setLineColor(Color.GRAY);
    Node node15 = graph.createNode(node15Realizer);

    //Node 16
    // reconfigure the default NodeRealizer
    ShapeNodeRealizer node16Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node16Realizer.setCenter(600, 350);
    node16Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node16Realizer.setFillColor(Color.WHITE);
    node16Realizer.setLineColor(new Color(255,80,0,255));
    node16Realizer.setLineType(LineType.LINE_4);
    final NodeLabel node16Label = node16Realizer.getLabel();
    node16Label.setText("<html><b>1,2,3...</b></html>");
    Node node16 = graph.createNode(node16Realizer);

    //Edge 14 15
    PolyLineEdgeRealizer edge14_15Realizer = new PolyLineEdgeRealizer();
    edge14_15Realizer.setTargetArrow(Arrow.addCustomArrow("offsetArrow", Arrow.T_SHAPE, 20));
    graph.createEdge(node14, node15, edge14_15Realizer);

    //Edge 15 16
    PolyLineEdgeRealizer edge15_16Realizer = new PolyLineEdgeRealizer();
    edge15_16Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node15, node16, edge15_16Realizer);

    //Edge 4 8
    PolyLineEdgeRealizer edge4_8Realizer = new PolyLineEdgeRealizer();
    edge4_8Realizer.setLineType(LineType.DOTTED_2);
    EdgeLabel edge4_8Label = edge4_8Realizer.getLabel();
    edge4_8Label.setText("Dotted");
    graph.createEdge(node4, node8, edge4_8Realizer);

    // add an edge from node 16 to 12
    final ArcEdgeRealizer arcEdge16_12Realizer = new ArcEdgeRealizer();
    arcEdge16_12Realizer.setTargetArrow(Arrow.STANDARD);
    arcEdge16_12Realizer.setLineType(LineType.DOTTED_1);
    graph.createEdge(node16, node12, arcEdge16_12Realizer);




  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new VisualFeatureDemo()).start("Visual Feature Demo");
      }
    });
  }
}


      