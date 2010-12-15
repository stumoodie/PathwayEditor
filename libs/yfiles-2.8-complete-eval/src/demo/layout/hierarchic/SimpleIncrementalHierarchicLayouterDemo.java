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
package demo.layout.hierarchic;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.DataMap;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.util.Maps;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

/**
 * This simple demo shows how to use the {@link IncrementalHierarchicLayouter}
 * to either calculate a new layout or calculate a new layout given the current
 * sketch or incrementally layout selected nodes to an already existing graph whose
 * layout is read from the current sketch.
 * <br>
 * <br>
 * Things to try:
 * <br>
 * Create a graph and use the <b>Layout</b> button to lay it out from scratch.
 * Modify the graph (move nodes and or bends), deselect all elements and
 * choose <b>Layout From Sketch</b> to recalculate the layout using the given sketch
 * Add some nodes and connect them to the graph, select the newly added nodes
 * and choose <b>Layout Incrementally</b> to incrementally "add" the selected
 * elements optimally into the existing graph.
 */
public class SimpleIncrementalHierarchicLayouterDemo extends DemoBase
{
  private DataMap hintMap;

  private IncrementalHierarchicLayouter hierarchicLayouter;
  private IncrementalHintsFactory hintsFactory;

  public SimpleIncrementalHierarchicLayouterDemo()
  {
    final Graph2D graph = view.getGraph2D();
    new HierarchyManager(graph);
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);

    // create a map to store the hints for the incremental layout mechanism
    hintMap = Maps.createHashedDataMap();
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, hintMap);

    // create the layouter
    hierarchicLayouter = new IncrementalHierarchicLayouter();

    // set some defaults
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumFirstSegmentLength(15);
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumLastSegmentLength(20);
    hierarchicLayouter.getEdgeLayoutDescriptor().setOrthogonallyRouted(false);
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumDistance(10.0d);

    hierarchicLayouter.getNodeLayoutDescriptor().setLayerAlignment(0.5d);
    hierarchicLayouter.setMinimumLayerDistance(30.0d);
    hierarchicLayouter.getNodeLayoutDescriptor().setNodeLabelMode(NodeLayoutDescriptor.NODE_LABEL_MODE_CONSIDER_FOR_DRAWING);

    hierarchicLayouter.setConsiderNodeLabelsEnabled(true);

    ((SimplexNodePlacer) hierarchicLayouter.getNodePlacer()).setBaryCenterModeEnabled(true);

    // get a reference to a hints factory
    hintsFactory = hierarchicLayouter.createIncrementalHintsFactory();

    // disable the component layouter (optional)
    hierarchicLayouter.setComponentLayouterEnabled(false);

    loadGraph("resource/simple.graphml");
  }

  class LayoutFromSketchAction extends AbstractAction
  {
    LayoutFromSketchAction()
    {
      super("Layout From Sketch");
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcIncrementalLayout(new NodeList().nodes());
    }
  }

  class LayoutIncrementallyAction extends AbstractAction
  {
    LayoutIncrementallyAction()
    {
      super("Layout Incrementally");
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcIncrementalLayout(view.getGraph2D().selectedNodes());
    }
  }

  class LayoutAction extends AbstractAction
  {
    LayoutAction()
    {
      super("Layout");
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcFreshLayout();
    }
  }

  protected JToolBar createToolBar()
  {
    JToolBar tb = super.createToolBar();
    tb.add(new LayoutAction());
    tb.add(new LayoutFromSketchAction());
    tb.add(new LayoutIncrementallyAction());
    return tb;
  }

  public void calcFreshLayout()
  {
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    calcLayout(hierarchicLayouter);
  }

  public void calcIncrementalLayout(NodeCursor incrementalNodes)
  {
    try
    {
      // mark nodes as "new"
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next())
      {
        hintMap.set(incrementalNodes.node(), hintsFactory.createLayerIncrementallyHint(incrementalNodes.node()));
      }
      // read the old nodes from the sketch
      hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
      // calculate the layout incrementally
      calcLayout(hierarchicLayouter);
    }
    finally
    {
      // reset the marks
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next())
      {
        hintMap.set(incrementalNodes.node(), null);
      }
    }
  }

  protected void calcLayout(Layouter layouter)
  {
    Graph2D graph = view.getGraph2D();
    if (!graph.isEmpty())
    {
      Cursor oldCursor = view.getCanvasComponent().getCursor();
      try
      {
        view.applyLayoutAnimated(layouter);
      }
      finally
      {
        view.getCanvasComponent().setCursor(oldCursor);
      }
    }
    view.fitContent();
    view.updateView();
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SimpleIncrementalHierarchicLayouterDemo()).start("Simple IncrementalHierarchicLayouter Demo");
      }
    });
  }
}
