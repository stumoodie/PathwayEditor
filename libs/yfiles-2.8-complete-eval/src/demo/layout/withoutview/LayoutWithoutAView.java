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

import javax.swing.JFrame;

import y.geom.YRectangle;
import y.geom.YPoint;
import y.geom.OrientedRectangle;
import y.layout.*;
import y.layout.DefaultLayoutGraph;
import y.layout.EdgeLabelModel;
import y.layout.LayoutGraph;
import y.layout.EdgeLabelLayout;
import y.layout.FreeEdgeLabelModel;
import y.layout.BufferedLayouter;
import y.layout.SliderEdgeLabelModel;
import y.layout.EdgeLabelLayoutImpl;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.labeling.GreedyMISLabeling;
import y.util.D;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;

import java.awt.EventQueue;

/**
 * This class shows how to use layout and labeling algorithms without using classes 
 * that are only present in the yFiles Viewer Distribution. Therefore this demo
 * only outputs the calculated coordinates of the graph layout to the console and 
 * displays it inside a simple preview panel. 
 * <br>
 * In this demo HierarchicLayouter is used to layout a small graph. 
 * <br>
 * First the edge labels of the graph will be laid out using a general labeling 
 * approach that effectively positions the labels after the node and edge positions
 * have already been fixed.
 * <br>
 * Second, a special edge labeling mechanism will be used that is currently only 
 * available in conjunction with HierarchicLayouter. While laying out the graph the 
 * edge labels will be considered as well. Therefore the node and edge positions 
 * can be chosen in such a way that, the labeling does not
 * introduce overlaps between labels and other entities in the graph.
 */
public class LayoutWithoutAView
{
  
  /**
   * Launcher
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        LayoutWithoutAView lwv = new LayoutWithoutAView();
        lwv.doit();
      }
    });
  }

  /**
   * Creates a small graph and applies an hierarchic layout to it.
   * Two different kinds of edge labeling mechanisms will be applied
   * to the graph.
   * <p>
   * The output of the calculated coordinates will be displayed in the
   * console.
   */ 
  public void doit()
  {
    DefaultLayoutGraph graph = new DefaultLayoutGraph();
    
    //construct graph. assign sizes to nodes
    Node v1 = graph.createNode();
    graph.setSize(v1,30,30);
    Node v2 = graph.createNode();
    graph.setSize(v2,30,30);
    Node v3 = graph.createNode();
    graph.setSize(v3,30,30);
    
    Edge e1 = graph.createEdge(v1,v2);
    Edge e2 = graph.createEdge(v2,v3);
    Edge e3 = graph.createEdge(v1,v3);
 
    //optionally setup some port constraints for HierarchicLayouter
    EdgeMap spc = graph.createEdgeMap();
    EdgeMap tpc = graph.createEdgeMap();
    //e1 shall leave and enter the node on the right side
    spc.set(e1, PortConstraint.create(PortConstraint.EAST));
    //additionally set a strong port constraint on the target side. 
    tpc.set(e1, PortConstraint.create(PortConstraint.EAST, true));
    //ports with strong port constraints will not be reset by the 
    //layouter.  So we specify the target port right now to connect 
    //to the upper-right corner of the node 
    graph.setTargetPointRel(e1, new YPoint(15, -15));
    
    //e2 shall leave and enter the node on the top side
    spc.set(e2, PortConstraint.create(PortConstraint.NORTH));
    tpc.set(e2, PortConstraint.create(PortConstraint.NORTH));
    //e3 uses no port constraints, i.e. layouter will choose best side
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, spc);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tpc);
    
    
    //setup two edge labels for edge e1. The size of the edge labels will be set to
    //80x20. Usually the size of the labels will be determined by
    //calculating the bounding box of a piece text that is displayed
    //with a specific font.
    EdgeLabelLayoutImpl ell1 = new EdgeLabelLayoutImpl();
    ell1.setBox(new YRectangle(0,0,80,20));
    //use a center slider edge model. the label will be placed on top of the
    //edge owning the label.
    ell1.setEdgeLabelModel(new SliderEdgeLabelModel(SliderEdgeLabelModel.CENTER_SLIDER));
    
    EdgeLabelLayoutImpl ell2 = new EdgeLabelLayoutImpl();
    ell2.setBox(new YRectangle(0,0,80,20));
    //use a side slider model. the label will be placed to one of the sides of the
    //edge owning the label.
    ell2.setEdgeLabelModel(new SliderEdgeLabelModel(SliderEdgeLabelModel.SIDE_SLIDER));
    
    EdgeLabelLayout[] ells = new EdgeLabelLayout[]{ell1, ell2};
    graph.setLabelLayout(e1, ells);
    
    IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setLabelLayouterEnabled(true);
    layouter.setLabelLayouter(new GreedyMISLabeling());
    
    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp1 = new LayoutPreviewPanel(new CopiedLayoutGraph(graph));
    lpp1.createFrame("Hierarchical with general edge labeling").setVisible(true);
    
    D.bug("\n\nGRAPH LAID OUT USING GENERAL EDGE LABELING");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));
    D.bug("e3 path = " + graph.getPath(e3));
    D.bug("ell1 upper left location = " + getEdgeLabelLocation(graph,e1,ell1).getBoundingBox().getLocation());
    D.bug("ell2 upper left location = " + getEdgeLabelLocation(graph,e1,ell2).getBoundingBox().getLocation());  
  
    EdgeLabelModel freeModel = new FreeEdgeLabelModel();
    ell1.setEdgeLabelModel(freeModel);
    ell1.setModelParameter(freeModel.getDefaultParameter());
    ell2.setEdgeLabelModel(freeModel);
    ell2.setModelParameter(freeModel.getDefaultParameter());

    layouter = new IncrementalHierarchicLayouter();
    layouter.setIntegratedEdgeLabelingEnabled(true);

    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp2 = new LayoutPreviewPanel(graph);
    lpp2.createFrame("Hierarchical with internal labeling").setVisible(true);

    D.bug("\n\nGRAPH LAID OUT USING HIERARCHIC LAYOUTER WITH INTERNAL EDGE LABELING");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));
    D.bug("e3 path = " + graph.getPath(e3));
    D.bug("ell1 upper left location = " + getEdgeLabelLocation(graph,e1,ell1).getBoundingBox().getLocation());
    D.bug("ell2 upper left location = " + getEdgeLabelLocation(graph,e1,ell2).getBoundingBox().getLocation());  
  }
  
  /**
   * Returns the calculated bounds of the edge label. 
   */ 
  OrientedRectangle getEdgeLabelLocation(LayoutGraph graph, Edge e, EdgeLabelLayout ell)
  {
    OrientedRectangle orientedRectangle = ell.getLabelModel().getLabelPlacement(
      ell.getBox(),
      graph.getEdgeLayout(e), 
      graph.getNodeLayout(e.source()),
      graph.getNodeLayout(e.target()),
      ell.getModelParameter());
    return orientedRectangle;
  }
}
