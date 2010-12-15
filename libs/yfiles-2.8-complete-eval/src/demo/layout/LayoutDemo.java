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
package demo.layout;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Edge;
import y.base.Node;
import y.layout.CanonicMultiStageLayouter;
import y.layout.LabelLayoutConstants;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.OrientationLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.labeling.AbstractLabelingAlgorithm;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.option.OptionHandler;
import y.util.D;
import y.view.EdgeLabel;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;
import javax.swing.JRootPane;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Demonstrates how layout and labeling algorithms can be applied to a
 * graph being displayed within a viewer component.
 * <br>
 * The view actions provided with ViewActionDemo are accessible as well.
 * <br>
 * The layout options can be either given as command line arguments or
 * within a settings dialog that will automatically pop up if no
 * command line arguments are provided.
 * <br>
 * Command line usage:
 * <pre>
 * java demo.view.layout.LayoutDemo {hierarchic,orthogonal,organic,circular} [anim] [label]
 * </pre>
 * <br>
 * The first command line argument is the name of the layout algorithm
 * to be applied. 
 * If the optional argument "label" is given, a generic labeling algorithm will 
 * be used to place all edge labels.
 * If the optional argument "anim" is given, then the
 * layout will be applied to the graph in an animated fashion.
 */
public class LayoutDemo extends DemoBase {
  OptionHandler layoutOptions;

  public LayoutDemo() {
    this(new String[]{"hierarchic", "anim", "label"});
  }

  public LayoutDemo(String[] args) {
    initLayoutOptions(args);

    //build sample graph
    buildGraph(view.getGraph2D());
  }

  /**
   * Initializes layout options from command line arguments.
   * If no command line arguments are given, a settings dialog will
   * automatically be displayed.
   */
  void initLayoutOptions(String[] args) {
    //create layout options
    boolean anim = true;
    boolean label = true;
    String layout = "hierarchic";
    if (args.length > 0) {
      layout = args[0];
      List list = Arrays.asList(args);
      anim = list.contains("anim");
      label = list.contains("label");
    }

    layoutOptions = new OptionHandler("Settings");
    final String[] algoEnum = {"hierarchic", "orthogonal", "organic", "circular"};
    layoutOptions.addEnum("Layout Style", algoEnum, layout, null);
    layoutOptions.addBool("Activate Generic Labeling", label);
    layoutOptions.addBool("Activate Layout Morphing", anim);

    if (args.length == 0) {
      layoutOptions.showEditor();
    }
  }


  /** Creates a small random graph with labelled edges */
  void buildGraph(Graph2D graph) {
    graph.clear();
    Node[] nodes = new Node[10];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = graph.createNode();
      graph.getRealizer(nodes[i]).setLabelText(String.valueOf(i));
    }

    Random random = new Random(0);
    for (int i = 0; i < nodes.length; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        if (random.nextDouble() > 0.75) {
          Edge edge = graph.createEdge(nodes[i], nodes[j]);
          EdgeLabel edgeLabel = new EdgeLabel(i + " -> " + j);
          // For generic edge labeling, edge label model "free" yields the best 
          // results.
          edgeLabel.setModel(EdgeLabel.FREE);
          edgeLabel.setPreferredPlacement((byte)(LabelLayoutConstants.PLACE_RIGHT_OF_EDGE | 
                                                 LabelLayoutConstants.PLACE_AT_CENTER));
          graph.getRealizer(edge).addLabel(edgeLabel);
        }
      }
    }
  }

  /**
   * Adds an extra layout action to the toolbar
   */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(new LayoutAction());
    return bar;
  }

  /**
   * Layout action that configures and launches a layout algorithm.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Auto-Layout Graph");
    }

    public void actionPerformed(ActionEvent e) {
      if (layoutOptions.showEditor()) {
        applyLayout();
      }
    }
  }

  /**
   * Configures and invokes a layout algorithm
   */
  void applyLayout() {
    Layouter layouter = createLayouter(layoutOptions);
    applyLayout(layouter, layoutOptions.getBool("Activate Layout Morphing"));
  }

  /**
   * Creates and returns a Layouter instance according to the given layout options.
   */
  Layouter createLayouter(OptionHandler layoutOptions) {

    String layout = layoutOptions.getString("Layout Style");
    boolean label = layoutOptions.getBool("Activate Generic Labeling");

    CanonicMultiStageLayouter layouter = null;

    if ("circular".equals(layout)) {
      CircularLayouter cl = new CircularLayouter();
      cl.getSingleCycleLayouter().setMinimalNodeDistance(100);
      layouter = cl;
    } else if ("hierarchic".equals(layout)) {
      IncrementalHierarchicLayouter hl = new IncrementalHierarchicLayouter();
      //set some options
      hl.getNodeLayoutDescriptor().setMinimumLayerHeight(60);
      hl.getNodeLayoutDescriptor().setMinimumDistance(20);

      //use left-to-right layout orientation
      OrientationLayouter ol = new OrientationLayouter();
      ol.setOrientation(LayoutOrientation.LEFT_TO_RIGHT);
      hl.setOrientationLayouter(ol);

      layouter = hl;
    } else if ("organic".equals(layout)) {
      SmartOrganicLayouter ol = new SmartOrganicLayouter();
      //set some options
      ol.setPreferredEdgeLength(80);
      ol.setQualityTimeRatio(1.0);
      ol.setNodeOverlapsAllowed(false);
      layouter = ol;
    } else if ("orthogonal".equals(layout)) {
      //set some options
      layouter = new OrthogonalLayouter();
    }

    if (layouter == null) {
      usage();
    }

    if (label) {
      // Automatic placement of edge labels using the generic labeling feature.
      AbstractLabelingAlgorithm la = 
          (AbstractLabelingAlgorithm)layouter.getLabelLayouter();
      // Node labels already have a good position.
      la.setPlaceNodeLabels(false);
      layouter.setLabelLayouterEnabled(true);
    }

    return layouter;
  }

  /**
   * Applies the given layout algorithm to the graph
   * residing in the view. depending on the parameter
   * the layout will be applied in an animated fashion
   * to the graph or not.
   */
  void applyLayout(Layouter layouter, boolean animated) {
    if (animated) {
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
      layoutExecutor.getLayoutMorpher().setPreferredDuration(800);
      layoutExecutor.getLayoutMorpher().setEasedExecution(true);
      layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
      layoutExecutor.doLayout(view, layouter);
    } else {
      view.applyLayout(layouter);

      //adjusts the zoom and origin of the view to make the
      //whole graph visible
      view.fitContent();

      //an ALTERNATIVE to fitContent is updateWorldRect
      //adjusts view scrollbars, so that the whole graph is visible
      //does not change zoom on the view

      //view.updateWorldRect();

      view.updateView();
    }
  }


  void usage() {
    D.bug("USAGE: java demo.view.layout.LayoutDemoTmp " +
        "{organic,circular,random,hierarchic,orthogonal} [label] [anim]");
    System.exit(0);
  }

  public void addContentTo(final JRootPane rootPane) {
    super.addContentTo(rootPane);
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        applyLayout();
      }
    });
  }

  public static void main( final String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new LayoutDemo(args)).start();
      }
    });
  }
}
