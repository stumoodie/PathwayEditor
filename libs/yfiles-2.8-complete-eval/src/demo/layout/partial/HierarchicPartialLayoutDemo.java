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
package demo.layout.partial;

import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.partial.PartialLayouter;
import y.option.IntOptionItem;
import y.option.OptionHandler;

import java.awt.EventQueue;


/**
 * This demo shows how to apply the partial layouter to hierarchic layouts. The partial layouter changes the coordinates
 * for a given set of graph elements (called partial elements). The location or size of the remaining elements (called
 * fixed elements) is not allowed to be changed. The layout algorithm tries to place the partial elements such that the
 * resulting drawing (including the fixed elements) has a good quality with respect to common graph drawing aesthetics.
 * <p/>
 * Partial node elements can be assigned to so called subgraph components. During the layout process each subgraph
 * induced by the nodes of a component is first laid out using the specified subgraph layouter. Then, the different
 * components are placed one-by-one onto the drawing area such that the number of overlaps among graph elements is
 * small. The user can specify different objectives (placement strategies) for finding 'good' positions for subgraph
 * components.
 * <p/>
 * The demo allows to specify fixed and partial elements. Fixed elements are drawn grey and partial elements orange. To
 * change the fixed/partial state of elements, select the corresponding elements and click on the "Lock Selected
 * Elements" or "Unlock Selected Elements" button. The current state of selected elements can be toggled with a
 * mouse-double-click. To start the partial layouter click on the "Apply Partial Layout" button.
 */
public class HierarchicPartialLayoutDemo extends PartialLayoutBase {

  public HierarchicPartialLayoutDemo() {
    this(null);
  }

  public HierarchicPartialLayoutDemo(final String helpFilePath) {
    super(helpFilePath);
  }

  /**
   * Loads a graph, which contains fix nodes and nodes, which should be integrated into this graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/graphHierarchic.graphml");
  }

  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    layoutOptionHandler.addEnum("Subgraph Layout",
        new Object[]{"Incremental Hierarchic Layout", "Organic Layout", "Unchanged"}, 0);
    layoutOptionHandler.addEnum("Component Assignment",
        new Object[]{"Single Nodes", "Connected Graphs"}, 0);
    layoutOptionHandler.addEnum("Edge Routing Style",
        new Object[]{"Automatic", "Straight Line", "Orthogonal", "Organic"}, 0);
    layoutOptionHandler.addBool("Hierarchy Reorganization", false);
    layoutOptionHandler.addBool("Allow Mirroring", false);
    layoutOptionHandler.addInt("Minimum Node Distance", 5);
    layoutOptionHandler.getItem("Minimum Node Distance").setAttribute(
        IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(0));
    layoutOptionHandler.addEnum("Layout Orientation",
        new Object[]{"None", "Auto", "Top to Bottom", "Bottom to Top", "Left to Right", "Right to Left"}, 2);
    return layoutOptionHandler;
  }

  protected Layouter createConfiguredPartialLayouter() {
    final PartialLayouter partialLayouter = new PartialLayouter();

    if (optionHandler != null) {
      switch (optionHandler.getEnum("Subgraph Layout")) {
        default:
        case 0:
          final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
          ihl.setMinimumLayerDistance((double) optionHandler.getInt("Minimum Node Distance"));
          partialLayouter.setCoreLayouter(ihl);
          break;
        case 1:
          partialLayouter.setCoreLayouter(new SmartOrganicLayouter());
          break;
        case 2:
          // is null per default
      }
      switch (optionHandler.getEnum("Component Assignment")) {
        default:
        case 0:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE);
          break;
        case 1:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED);
          break;
      }
      switch (optionHandler.getEnum("Edge Routing Style")) {
        default:
        case 0:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_AUTOMATIC);
          break;
        case 1:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE);
          break;
        case 2:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL);
          break;
        case 3:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORGANIC);
          break;
      }
      partialLayouter.setOrientationOptimizationEnabled(optionHandler.getBool("Hierarchy Reorganization"));
      partialLayouter.setMirroringAllowed(optionHandler.getBool("Allow Mirroring"));
      partialLayouter.setMinimalNodeDistance(optionHandler.getInt("Minimum Node Distance"));
      switch (optionHandler.getEnum("Layout Orientation")) {
        default:
        case 0:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
          break;
        case 1:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_AUTO_DETECTION);
          break;
        case 2:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_TOP_TO_BOTTOM);
          break;
        case 3:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_BOTTOM_TO_TOP);
          break;
        case 4:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_LEFT_TO_RIGHT);
          break;
        case 5:
          partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_RIGHT_TO_LEFT);
          break;
      }
    }
    partialLayouter.setPositioningStrategy(PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER);
    partialLayouter.setConsiderNodeAlignment(true);
    return partialLayouter;
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new HierarchicPartialLayoutDemo("resource/hierarchiclayouthelp.html"))
            .start("Hierarchic Partial Layouter Demo");
      }
    });
  }

}