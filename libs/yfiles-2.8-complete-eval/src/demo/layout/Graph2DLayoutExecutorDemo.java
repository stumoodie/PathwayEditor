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
import y.base.Node;
import y.layout.BufferedLayouter;
import y.layout.Layouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.random.RandomLayouter;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.JRootPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Random;

/**
 * Demonstrates how the {@link Graph2DLayoutExecutor} can be used to apply
 * layout algorithms to a {@link Graph2D}.
 */
public class Graph2DLayoutExecutorDemo extends DemoBase
{
  // the label that shows some status (if non-blocking)
  private JLabel statusLabel;
  // the progress bar that shows indeterminate progress (if non-blocking)
  private JProgressBar progressBar = new JProgressBar();

  // the types of execution
  private JComboBox layoutExecutionTypeBox;

  // the type of layouter
  private JComboBox layouterBox;

  public Graph2DLayoutExecutorDemo() {
    //build sample graph
    buildGraph( view.getGraph2D() );

    view.setViewPoint2D(-200.0, -200.0);
  }

  protected void configureDefaultRealizers() {
    // painting shadows is expensive and not well suited for graphs with many
    // nodes such as this demo's sample graph
    DemoDefaults.registerDefaultNodeConfiguration(false);
    DemoDefaults.configureDefaultRealizers(view);
  }

  /**
   * Overwritten to add the status label and the progress bar.
   */
  public void addContentTo(JRootPane rootPane) {
    this.statusLabel = new JLabel("Status");
    final Dimension minimumSize = this.statusLabel.getMinimumSize();
    this.statusLabel.setMinimumSize(new Dimension(Math.max(200, minimumSize.width), minimumSize.height));
    final JPanel panel = new JPanel();
    panel.add(this.statusLabel, BorderLayout.LINE_START);
    this.progressBar.setMaximum(100);
    this.progressBar.setMinimum(0);
    this.progressBar.setValue(0);
    panel.add(progressBar, BorderLayout.CENTER);
    getContentPane().add(panel, BorderLayout.SOUTH);
    super.addContentTo(rootPane);
  }

  /** Creates a relatively large random graph to give the layout algorithms something to chew. */
  void buildGraph(Graph2D graph) {
    graph.clear();
    Node[] nodes = new Node[400];
    for(int i = 0; i < nodes.length; i++)
    {
      nodes[i] = graph.createNode();
      graph.getRealizer(nodes[i]).setLabelText(String.valueOf(i));
    }
    Random random = new Random(0L);
    for ( int i = 0; i < nodes.length; i++ ) {

      int edgeCount;

      if (random.nextInt(10) == 0) {
        edgeCount = 4 + random.nextInt(5);
      } else {
        edgeCount = random.nextInt(3);
      }

      for ( int j = 0; j < edgeCount; j++ ) {
        graph.createEdge( nodes[ i ], nodes[ random.nextInt(nodes.length) ] );
      }
    }

    (new BufferedLayouter(new RandomLayouter())).doLayout(graph);
  }

  /**
   * Adds an extra layout action to the toolbar
   */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(new AbstractAction("Layout") {
      public void actionPerformed(ActionEvent e) {
        applyLayout();
      }
    });

    // chooser for the layouter
    layouterBox = new JComboBox(new Object[]{"Hierarchic", "Organic", "Orthogonal"});
    layouterBox.setMaximumSize(new Dimension(200, 100));
    layouterBox.setSelectedIndex(0);
    bar.add(layouterBox);

    // chooser for the execution type.
    layoutExecutionTypeBox = new JComboBox(
      new Object[]{"Animated", "AnimatedThreaded", "Buffered", "Threaded", "Unbuffered", "AnimatedInOwnThread"});
    layoutExecutionTypeBox.setMaximumSize(new Dimension(200, 100));
    layoutExecutionTypeBox.setSelectedIndex(1);
    bar.add(layoutExecutionTypeBox);

    return bar;
  }

  /**
   * Configures and invokes a layout algorithm
   */
  void applyLayout() {
    Layouter layouter = createLayouter();
    switch (layoutExecutionTypeBox.getSelectedIndex()) {
      case 0:
        applyLayoutAnimated(layouter);
        break;
      case 1:
        applyLayoutAnimatedThreaded(layouter);
        break;
      case 2:
        applyLayoutBuffered(layouter);
        break;
      case 3:
        applyLayoutThreaded(layouter);
        break;
      case 4:
        applyLayoutUnbuffered(layouter);
        break;
      case 5:
        applyLayoutAnimatedInOwnThread(layouter);
        break;
    }
  }

  /**
   * Creates and returns a Layouter instance according to the given layout options.
   */
  Layouter createLayouter() {
    switch (layouterBox.getSelectedIndex()) {
      default:
      case 0:
        return new IncrementalHierarchicLayouter();
      case 1:
        final SmartOrganicLayouter organicLayouter = new SmartOrganicLayouter();
        organicLayouter.setQualityTimeRatio(1.0);
        organicLayouter.setMaximumDuration(2L * 60L * 1000L);
        return organicLayouter;
      case 2:
        return new OrthogonalLayouter();
    }
  }

  /**
   * Applies the given layout algorithm to the graph
   * This is done in a separate Thread asynchronously.
   * Although the view and UI is responsive direct mouse and keyboard input is blocked.
   * The layout process can be canceled and even killed through a dialog that is spawned.
   */
  void applyLayoutAnimatedThreaded(final Layouter layouter) {
    this.progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED_THREADED);
    // set a slow animation, so that the animation can easily be canceled.
    layoutExecutor.getLayoutMorpher().setPreferredDuration(3000L);
    layoutExecutor.getLayoutMorpher().setEasedExecution(true);
    layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
    // lock the view so that the graph cannot be edited.
    layoutExecutor.setLockingView(true);

    final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), "");

    // the following method will return immediately and the layout and animation is performed in a new thread
    // asynchronously.
    final Graph2DLayoutExecutor.LayoutThreadHandle handle = layoutExecutor.doLayout(view, layouter, new Runnable() {
      public void run() {
        dialog.dispose();
        progressBar.setIndeterminate(false);
        statusLabel.setText("Layout Done");
      }
    }, new Graph2DLayoutExecutor.ExceptionListener() {
      public void exceptionHappened(Throwable t) {
        //dialog.dispose();
        t.printStackTrace(System.err);
        statusLabel.setText("Exception Happened.");
      }
    });

    // this is visible because the layout is not blocking (this) EDT
    this.statusLabel.setText("Layout is running");

    final Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    final JLabel label = new JLabel("Layout Running [" + layouter.getClass().getName() + "].");
    box.add(label);
    box.add(Box.createVerticalStrut(12));
    box.add(new JButton(new AbstractAction("Cancel") {
      private boolean canceled;
      public void actionPerformed(ActionEvent e) {
        // first, simply cancel the layout
        if (!canceled) {
          handle.cancel();
          statusLabel.setText("Cancelling");
          label.setText("Canceled Thread.[" + layouter.getClass().getName() + "].");
          ((JButton)e.getSource()).setText("Kill");
          canceled = true;
        } else {
          // if it's not dead, yet, one could possibly try to kill the thread. 
          // this is o.k. most of the time (no debugger, etc.), but should be used with care.
          handle.getThread().stop();
          setEnabled(false);
          statusLabel.setText("Killed");
        }
      }
    }));
    dialog.getContentPane().add(box);
    dialog.setLocationRelativeTo(view);
    dialog.pack();

    if (handle.isRunning()) {
      dialog.setVisible(true);
    }
  }

  /**
   * Applies the given layout algorithm to the graph
   * This is done synchronously blocking the calling Thread, thus leaving the view unresponsive during the layout.
   */
  void applyLayoutBuffered(final Layouter layouter){
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED);
    layoutExecutor.doLayout(view, layouter);
  }

  /**
   * Applies the given layout algorithm to the graph in an animated fashion.
   * This is done synchronously blocking the calling Thread, thus leaving the view unresponsive during the layout
   * and animation.
   */
  void applyLayoutAnimated(final Layouter layouter){
    // this won't be visible to the user because the EDT is blocked.
    statusLabel.setText("Starting Animated Blocking Layout");
    progressBar.setIndeterminate(true);
    try {
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED);
      layoutExecutor.doLayout(view, layouter);
    } finally {
      progressBar.setIndeterminate(false);
      statusLabel.setText("Animated Blocking Layout Done.");
    }
  }

  /**
   * Applies the given layout algorithm to the graph in an animated fashion using a blocking call
   * from a separate newly spawned thread.
   * This leaves the view responsive, but the view is still editable during the layout.
   */
  void applyLayoutAnimatedInOwnThread(final Layouter layouter){
    statusLabel.setText("Starting own layout thread.");
    progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED);
    new Thread(new Runnable() {
      public void run() {
        try {
          layoutExecutor.doLayout(view, layouter);
        } finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              statusLabel.setText("Layout Thread Finished.");
              progressBar.setIndeterminate(false);
            }
          });
        }
      }
    }).start();
  }

  /**
   * Runs the layout in a separate thread, leaving the view responsive
   * but the view is still editable during the layout.
   * @param layouter
   */
  void applyLayoutThreaded(final Layouter layouter){
    statusLabel.setText("Starting threaded layout");
    progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.THREADED);
    layoutExecutor.doLayout(view, layouter, new Runnable() {
      public void run() {
        statusLabel.setText("Layout Returned");
        progressBar.setIndeterminate(false);
      }
    }, null);
    statusLabel.setText("Return from doLayout()");
  }

  void applyLayoutUnbuffered(final Layouter layouter) {
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.UNBUFFERED);
    layoutExecutor.doLayout(view, layouter);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new Graph2DLayoutExecutorDemo()).start();
      }
    });
  }
}