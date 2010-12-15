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

import y.base.Edge;
import y.base.Node;
import y.base.NodeMap;
import y.base.ListCell;
import y.layout.BufferedLayouter;
import y.layout.CopiedLayoutGraph;
import y.layout.DefaultLayoutGraph;
import y.layout.LayoutGraph;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.PartitionGrid;
import y.layout.hierarchic.incremental.ColumnDescriptor;
import y.util.D;
import y.util.DataProviders;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.geom.Line2D;

/**
 * This demo shows how to use the swim lane feature of IncrementalHierarchicLayouter
 * without using classes that are only present in the yFiles Viewer Distribution. 
 * In this demo, nodes will be assigned to certain regions of the diagram,
 * the so-called swim lanes. The diagram will be arranged using hierarchical layout
 * style, while nodes remain within the bounds of their lanes.
 * <br>
 * This demo displays the calculated coordinates in a simple graph viewer.
 * Additionally it outputs the calculated coordinates of the graph layout to
 * the console.
 */
public class SwimlaneLayoutWithoutAView
{
  
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        SwimlaneLayoutWithoutAView lwv = new SwimlaneLayoutWithoutAView();
        lwv.doit();
      }
    });
  }

  /**
   * Creates a small graph and applies a swim lane layout to it.
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
    Node v4 = graph.createNode();
    graph.setSize(v4,30,30);

    // create some edges...
    Edge e1 = graph.createEdge(v1,v2);
    Edge e2 = graph.createEdge(v1,v3);
    Edge e3 = graph.createEdge(v2,v4);
    
    // create a grid that models two swim lanes,
    // i.e. a grid with two columns and one row
    final PartitionGrid grid = new PartitionGrid(1, 2);

    // create a map to store the node to lane mapping
    NodeMap cellMap = graph.createNodeMap();

    // assign nodes to lanes
    // lanes correspond to cells, as the grid has only one row
    cellMap.set(v1, grid.createCellId(0, 0));
    cellMap.set(v2, grid.createCellId(0, 1));
    cellMap.set(v3, grid.createCellId(0, 1));
    cellMap.set(v4, grid.createCellId(0, 0));
 
    // register the information
    graph.addDataProvider(PartitionGrid.PARTITION_CELL_DPKEY, cellMap);
    graph.addDataProvider(PartitionGrid.PARTITION_GRID_DPKEY, DataProviders.createConstantDataProvider(grid));
    
    // create the layout algorithm
    IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    
    // start the layout
    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp1 = new LayoutPreviewPanel(new CopiedLayoutGraph(graph)) {
      Line2D.Double line = new Line2D.Double();
      protected void paint( final Graphics2D g, final LayoutGraph graph ) {
        final Color oldColor = g.getColor();
        g.setColor(Color.white);

        final Rectangle bbx = graph.getBoundingBox();
        line.y1 = Math.floor(bbx.getY()) - 10;
        line.y2 = Math.ceil(bbx.getMaxY()) + 10;
        for (ListCell cell = grid.getColumns().firstCell(); cell != null; cell = cell.succ()) {
          final ColumnDescriptor lane = (ColumnDescriptor) cell.getInfo();
          line.x1 = lane.getComputedPosition();
          line.x2 = line.x1;
          g.draw(line);
        }
        {
          final ColumnDescriptor lane = grid.getColumn(grid.getColumns().size() - 1);
          line.x1 += lane.getComputedWidth();
          line.x2 = line.x1;
          g.draw(line);
        }

        g.setColor(oldColor);
      }
    };
    lpp1.createFrame("Swimlanes").setVisible(true);
    
    D.bug("\n\nGRAPH LAID OUT HIERARCHICALLY IN SWIMLANES");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("v4 center position = " + graph.getCenter(v4));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));
    D.bug("e3 path = " + graph.getPath(e3));
    D.bug("Column 0 index = " + grid.getColumn(0).getIndex());
    D.bug("Column 0 position = " + grid.getColumn(0).getComputedPosition());
    D.bug("Column 0 width = " + grid.getColumn(0).getComputedWidth());
    D.bug("Column 1 index = " + grid.getColumn(1).getIndex());
    D.bug("Column 1 position = " + grid.getColumn(1).getComputedPosition());
    D.bug("Column 1 width = " + grid.getColumn(1).getComputedWidth());
  }
}
