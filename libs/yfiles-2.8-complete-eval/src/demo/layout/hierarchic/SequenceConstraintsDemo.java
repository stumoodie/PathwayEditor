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
import y.base.Node;
import y.base.NodeCursor;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SequenceConstraintFactory;
import y.view.Graph2D;
import y.view.Graph2DView;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Demonstrates how to apply sequence constraints when calculating hierarchical
 * layouts. For hierarchical layouts, a sequence is the in-layer order of nodes,
 * e.g. with layout direction from top to bottom, a sequence is the left to
 * right order of nodes.
 *
 */
public class SequenceConstraintsDemo extends DemoBase {
  /**
   * Label text constant that marks a node to be the first one in its layer.
   */
  private static final String FIRST = "FIRST";

  /**
   * Label text constant that marks a node to be the last one in its layer.
   */
  private static final String LAST = "LAST";

  private final Random rndm;

  public SequenceConstraintsDemo() {
    rndm = new Random(0);
    loadGraph("resource/SequenceConstraintsDemo.graphml");    
    view.fitContent();
    view.updateView();
  }

  /**
   * Calculates a hierarchical layout for the specified graph.
   * The layout algorithm will sequence nodes according to the
   * lexicographical order of their labels. To enforce this in-layer order,
   * sequence constraints are used.
   *
   * @param view   the <code>Graph2DView</code> to use
   */
  private void doLayout(final Graph2DView view) {
    final Graph2D graph = view.getGraph2D();

    // classify nodes as "unlabeled", "labeled", FIRST, and LAST nodes
    // for labeled nodes we will later assign constraints, that force
    // the layout algorithm to order these nodes according to the
    // lexicographical order of their labels
    // unlabeled nodes may be sequenced as the layout algorithm deems
    // appropriate
    final List labeled = new ArrayList(graph.nodeCount());
    final List firsts = new ArrayList(graph.nodeCount());
    final List lasts = new ArrayList(graph.nodeCount());
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final String s = graph.getRealizer(nc.node()).getLabelText().trim();
      if (s.length() > 0) {
        if (FIRST.equalsIgnoreCase(s)) {
          firsts.add(nc.node());
        } else if (LAST.equalsIgnoreCase(s)) {
          lasts.add(nc.node());
        } else {
          labeled.add(nc.node());
        }
      }
    }

    // sort the labeled nodes to get an order that can be easily modeled
    // with consecutive "place after" constraints
    Collections.sort(labeled, new Comparator() {
      public int compare(final Object o1, final Object o2) {
        final String s1 = graph.getRealizer(((Node) o1)).getLabelText();
        final String s2 = graph.getRealizer(((Node) o2)).getLabelText();
        return s1.compareTo(s2);
      }
    });


    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setOrthogonallyRouted(true);
    
    // create a constraint factory for our graph
    final SequenceConstraintFactory scf = ihl.createSequenceConstraintFactory(graph);

    // create constraints for nodes with "normal" labels;
    // these nodes shall be sequenced according to the lexicographical order
    // of their labels
    for (int i = 1, n = labeled.size(); i < n; ++i) {
      scf.addPlaceNodeAfterConstraint(
          labeled.get(i - 1), labeled.get(i));
    }

    // create constraints for all nodes with FIRST labels;
    // these nodes shall always be placed at the start of their layers
    for (int i = 0, n = firsts.size(); i < n; ++i) {
      scf.addPlaceNodeAtHeadConstraint(firsts.get(i));
    }

    // create constraints for all nodes with LAST labels;
    // these nodes shall always be placed at the end of their layers
    for (int i = 0, n = lasts.size(); i < n; ++i) {
      scf.addPlaceNodeAtTailConstraint(lasts.get(i));
    }

    view.applyLayoutAnimated(ihl);

    // dispose the constraint factory to clear all previously specified
    // constraints and prevent memory leaks
    scf.dispose();
  }

  /**
   * Generates random node labels for the specified graph. Roughly 1/8
   * of the nodes will be marked either as a <code>FIRST</code> or as a
   * <code>LAST</code> node.
   */
  private void generateRandomLabels(final Graph2D graph) {
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final String label;
      if (rndm.nextDouble() < 0.125) {
        label = rndm.nextDouble() < 0.5 ? FIRST : LAST;
      } else {
        final char[] chars = new char[rndm.nextInt(2)];
        for (int i = 0; i < chars.length; ++i) {
          chars[i] = (char) (rndm.nextInt(26) + (int) 'A');
        }
        label = new String(chars);
      }
      graph.getRealizer(nc.node()).setLabelText(label);
    }
  }

  /**
   * Loads an initial sample graph.
   */
  private void loadInitialGraph() {
   
  }

  protected JToolBar createToolBar() {
    final JToolBar jtb = super.createToolBar();
    jtb.addSeparator();
    jtb.add(new AbstractAction("Layout") {
      {
        putValue(Action.SHORT_DESCRIPTION,
            "<html><head></head><body>" +
                "Applies a hierarchical layout." +
                "<p>" +
                "Nodes will be sequenced according to the lexicographical" +
                " order of their labels." +
                "<br>" +
                "Nodes with empty labels will be sequenced as the layout" +
                " algorithm deems appropriate." +
                "<br>" +
                "Finally, nodes labeled <code>FIRST</code> or" +
                " <code>LAST</code> are sequenced at layer start or layer" +
                " end respectively." +
                "</p>" +
                "</body></html>");
      }

      public void actionPerformed(final ActionEvent e) {
        doLayout(view);
        view.fitContent();
        view.updateView();
      }
    });
    jtb.add(new AbstractAction("New Labels") {
      {
        putValue(Action.SHORT_DESCRIPTION,
            "<html><head></head><body>" +
                "Generates random node labels." +
                "<p>" +
                " Roughly 1/8 of the nodes will be marked either as a" +
                " <code>FIRST</code> or as a <code>LAST</code> node." +
                "</p>" +
                "</body></html>");
      }

      public void actionPerformed(final ActionEvent e) {
        generateRandomLabels(view.getGraph2D());
        view.fitContent();
        view.updateView();
      }
    });
    return jtb;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SequenceConstraintsDemo()).start();
      }
    });
  }
}
