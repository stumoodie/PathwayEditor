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
package demo.view.anim;

import demo.view.DemoDefaults;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.util.DefaultMutableValue2D;
import y.util.Value2D;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Demonstrates usage and effects of ease in and/or ease out for animation
 * effects.
 */
public class EaseInEaseOutDemo {
  private static final int PREFERRED_DURATION = 2000;

  private final Graph2DView view;
  private Value2D[][] positions;
  private Timer timer;

  AnimationPlayer player;
  ViewAnimationFactory factory;

  /**
   * Creates a new EaseInEaseOutDemo and initializes a Timer that triggers the
   * animation effects.
   */
  public EaseInEaseOutDemo() {
    this.view = new Graph2DView();
    DemoDefaults.configureDefaultRealizers(view);
    init();    
  }
 
  /**
   * Initializes the start and end points for the animated movement effects.
   * Creates nodes to demonstrate animated movement.
   */
  private void init() {
    positions = new Value2D[4][2];
    for (int i = 0, n = positions.length; i < n; ++i) {
      positions[i][0] = DefaultMutableValue2D.create(70, 110 + i * 60);
      positions[i][1] = DefaultMutableValue2D.create(410, 110 + i * 60);
    }

    final String[] labels = {
        "Normal", "Ease In", "Ease In, Ease Out", "Ease Out"
    };

    final Graph2D graph = view.getGraph2D();

    for (int i = 0, n = positions.length; i < n; ++i) {
      final Value2D pos = positions[i][0];
      final NodeRealizer nr = graph.getRealizer(
          graph.createNode(pos.getX(), pos.getY()));
      nr.setSize(120, 30);
      nr.setLabelText(labels[i]);
    }


    timer = new Timer(PREFERRED_DURATION + 500,
        new ActionListener() {
          private boolean invert;

          public void actionPerformed(final ActionEvent e) {
            play(invert);
            invert = !invert;
          }
        });
    timer.setInitialDelay(1000);
    timer.start();
  }

  /**
   * Plays the movement animation for the nodes in the graph.
   * Four different kinds of movement animations are created:
   * <ul>
   * <li> normal (i.e. no ease effect) </li>
   * <li> ease in </li>
   * <li> ease in and ease out </li>
   * <li> ease out </li>
   * </ul>
   *
   * @param invert   if <code>true</code> the nodes move from right to left;
   *                 otherwise the nodes move from left to right.
   */
  private void play(final boolean invert) {
    final Graph2D graph = view.getGraph2D();

    if (factory == null) {
      factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
    }

    // we want to play all four animations at the same time
    final CompositeAnimationObject moves = AnimationFactory.createConcurrency();

    for (int i = 0, n = positions.length; i < n; ++i) {
      final Value2D dest = positions[i][invert ? 0 : 1];
      final NodeRealizer nr = graph.getRealizer(graph.getNodeArray()[i]);

      // create a movement effect from the realizer's current position to
      // the specified destination
      AnimationObject move =
          factory.move(nr, dest, ViewAnimationFactory.APPLY_EFFECT,
              PREFERRED_DURATION);

      switch (i) {
        case 1:
          // create an ease in effect
          move = AnimationFactory.createEasedAnimation(move, 1, 1);
          break;
        case 2:
          // create an ease in and ease out effect
          move = AnimationFactory.createEasedAnimation(move);
          break;
        case 3:
          // create an ease out effect
          move = AnimationFactory.createEasedAnimation(move, 0, 0);
          break;
      }

      // register the individual animations for concurrent processing
      moves.addAnimation(move);
    }

    if (player == null) {
      player = factory.createConfiguredPlayer();
    }
    // play the animations
    player.animate(moves);
  }


  /**
   * Creates an application frame for this demo
   * and displays it. The given string is the title of
   * the displayed frame.
   */
  private void start(final String title) {
    final JFrame frame = new JFrame(title);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo(final JRootPane rootPane) {
    final JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(view, BorderLayout.CENTER);

    rootPane.setContentPane(contentPane);
  }

  public void dispose() {
    if (timer != null) {
      if (timer.isRunning()) {
        timer.stop();
      }
      timer = null;
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        (new EaseInEaseOutDemo()).start("Ease Demo");
      }
    });
  }
}
