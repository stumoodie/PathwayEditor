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
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.partial.PartialLayouter;
import y.option.OptionHandler;
import y.view.Graph2DLayoutExecutor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

/**
 * This demo shows how to apply the partial layouter to orthogonal layouts. The partial layouter changes the coordinates
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
public class OrthogonalPartialLayoutDemo extends PartialLayoutBase {

  public OrthogonalPartialLayoutDemo() {
    this(null);
  }

  public OrthogonalPartialLayoutDemo(final String helpFilePath) {
    super(helpFilePath);
  }

  /**
   * Loads a graph, which contains fix nodes and nodes, which should be integrated into this graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/orthogonal.graphml");
  }

  /**
   * Adds an action for orthogonal layout to the default toolbar.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();

    final OrthogonalLayoutAction orthogonalLayoutAction = new OrthogonalLayoutAction("Orthogonal Layout");
    orthogonalLayoutAction.putValue(Action.SHORT_DESCRIPTION, "Orthogonal Layout");
    toolBar.add(orthogonalLayoutAction);

    return toolBar;
  }

  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    layoutOptionHandler.addEnum("Subgraph Layout",
        new Object[]{"Orthogonal Layout", "Unchanged"}, 0);
    layoutOptionHandler.addInt("Grid Size", 10, 1, 50);
    layoutOptionHandler.addEnum("Component Assignment",
        new Object[]{"Single Nodes", "Connected Graphs", "Same Component"}, 0);
    layoutOptionHandler.addBool("Use Snapping", true);
    layoutOptionHandler.addBool("Use Sketch", false);
    layoutOptionHandler.addBool("Resize Fixed Groups", true);

    return layoutOptionHandler;
  }

  protected Layouter createConfiguredPartialLayouter() {
    final PartialLayouter partialLayouter = new PartialLayouter();

    if (optionHandler != null) {
      switch (optionHandler.getEnum("Subgraph Layout")) {
        default:
        case 0:
          partialLayouter.setCoreLayouter(getOrthogonalLayouter());
          break;
        case 1:
          // is null per default
      }
      partialLayouter.setPositioningStrategy(
          optionHandler.getBool(
              "Use Sketch") ? PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH : PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER);

      switch (optionHandler.getEnum("Component Assignment")) {
        default:
        case 0:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE);
          break;
        case 1:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED);
          break;
        case 2:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CUSTOMIZED);
          break;
      }
      partialLayouter.setConsiderNodeAlignment(optionHandler.getBool("Use Snapping"));
      partialLayouter.setFixedGroupResizingEnabled(optionHandler.getBool("Resize Fixed Groups"));
      partialLayouter.setMinimalNodeDistance(optionHandler.getInt("Grid Size"));
    }
    partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL);
    partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
    return partialLayouter;
  }

  /**
   * This method configures and returns the OrthogonalGroupLayouter
   * @return an instance of OrthogonalGroupLayouter
   */
  private OrthogonalGroupLayouter getOrthogonalLayouter() {
    OrthogonalGroupLayouter layouter = new OrthogonalGroupLayouter();
    if (optionHandler != null) {
      layouter.setGrid(optionHandler.getInt("Grid Size"));
    }
    return layouter;
  }

  /**
   * Launches the OrthogonalLayouter.
   */
  class OrthogonalLayoutAction extends AbstractAction {
    OrthogonalLayoutAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
      executor.getLayoutMorpher().setEasedExecution(true);
      executor.getLayoutMorpher().setSmoothViewTransform(true);
      executor.doLayout(view, getOrthogonalLayouter());
      view.updateView();
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new OrthogonalPartialLayoutDemo("resource/orthogonallayouthelp.html"))
            .start("Orthogonal Partial Layouter Demo");
      }
    });
  }
}