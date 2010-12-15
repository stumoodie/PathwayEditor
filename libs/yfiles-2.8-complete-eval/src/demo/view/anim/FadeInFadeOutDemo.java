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

import demo.view.DemoBase;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import javax.swing.Action;
import java.awt.EventQueue;

/**
 * Demonstrates how to visually fade-in newly created nodes and
 * fade-out deleted nodes. This nice animation effect is triggered by a
 * special <code>GraphListener</code> implementation.
 * Note that this demo makes use of the yFiles class <code>
 * Graph2DViewRepaintManager</code> to increase the speed
 * of the animation effect.
 */
public class FadeInFadeOutDemo extends DemoBase {
  private static final long PREFERRED_DURATION = 500;

  /**
   * Creates a new FadeInFadeOutDemo and initializes the AnimationPlayer for
   * the fade effects.
   */
  public FadeInFadeOutDemo() {
    view.getGraph2D().addGraphListener(new FadeHandler(view));
  }

  protected Action createLoadAction() {
    //Overridden method to disable the Load menu in the demo
    return null;
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo
    return null;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new FadeInFadeOutDemo()).start("Fade Demo");
      }
    });
  }

  /**
   * Triggers fading effects on node creation and node removal.
   */
  private static final class FadeHandler implements GraphListener {
    private AnimationPlayer player;
    private ViewAnimationFactory factory;

    public FadeHandler(Graph2DView view) {
      factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
      player = factory.createConfiguredPlayer();
    }

    public void onGraphEvent(final GraphEvent e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      switch (e.getType()) {
        case GraphEvent.NODE_CREATION: {
          final NodeRealizer nr = graph.getRealizer((Node) e.getData());
          nr.setVisible(false);
          player.animate(factory.fadeIn(nr, PREFERRED_DURATION));
          break;
        }
        case GraphEvent.PRE_NODE_REMOVAL: {
          final NodeRealizer nr = graph.getRealizer((Node) e.getData());

          // let's create a drawable, so the animation can run no matter
          // if the node is in the graph or not
          final Drawable dnr = ViewAnimationFactory.createDrawable(nr);
          nr.setVisible(false);

          final AnimationObject fadeOut = factory.fadeOut(dnr, PREFERRED_DURATION);

          // let's start the animation with some delay so edges "vanish"
          // before nodes
          player.animate(
              AnimationFactory.createSequence(
                  AnimationFactory.createPause(100), fadeOut));
          break;
        }
      }
    }
  }
}
