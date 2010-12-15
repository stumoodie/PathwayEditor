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
import y.anim.AnimationEvent;
import y.anim.AnimationFactory;
import y.anim.AnimationListener;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.option.GuiFactory;
import y.option.ResourceBundleGuiFactory;
import y.util.DefaultMutableValue2D;
import y.util.Value2D;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DViewRepaintManager;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.PopupMode;
import y.view.ViewAnimationFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.util.MissingResourceException;

/**
 * Shows various animation effects for graph elements and graph views:
 * <ul>
 *   <li>fade in and fade out for nodes and/or edges</li>
 *   <li>resizing of nodes</li>
 *   <li>edge traversals</li>
 *   <li>animated loading and clearing of graph structures</li>
 *   <li>animated zooming</li>
 *   <li>animated camera movement</li>
 * </ul>
 * Makes use of class {@link demo.view.anim.AnimationEffectsDemoBase}.
 */
public final class AnimationEffectsDemo extends AnimationEffectsDemoBase {
  private static final long LONG_DURATION = 2000;
  private static final long SHORT_DURATION = 500;
  private static final long DEFAULT_DURATION = 1000;
  private static final long PREFERRED_DURATION_CAMERA = 10000;
  private static final long PREFERRED_DURATION_GRAPH = 5000;
  private static final long PREFERRED_DURATION_TRAVERSAL = 2000;

  private final AnimationPlayer player;
  private final EndHandler endHandler;
  private final ViewAnimationFactory unmanagedFactory;

  /**
   * Creates a new AnimationEffectsDemo.
   */
  public AnimationEffectsDemo() {
    this(createGuiFactory(), false);
  }

  /**
   * Creates a new AnimationEffectsDemo.
   */
  private AnimationEffectsDemo(
          final GuiFactory i18n,
          final boolean wantsRadioButtons
  ) {
    super(i18n, wantsRadioButtons);
    this.endHandler = new EndHandler();
    this.player = new AnimationPlayer();
    this.player.setFps(240);
    this.player.addAnimationListener(endHandler);
    this.player.setBlocking(false);
    this.unmanagedFactory = new ViewAnimationFactory(view);

    configureDefaultRealizers();
  }

  protected void configureDefaultRealizers() {
    DemoDefaults.configureDefaultRealizers(view);
  }


  /**
   * Delegates appropriate to user-specified options.
   */
  void animate() {
    if (player.isPlaying()) {
      return;
    }

    switch (((Byte) oh.get("misc", "animation")).byteValue()) {
      case NO_ANIM:
        break;
      case ANIMATED_LOAD:
        animatedLoad();
        break;
      case ANIMATED_CLEAR:
        animatedClear();
        break;
      case TRAVERSE_EDGE:
        animateTraverseEdge();
        break;
      case ZOOM:
        animateZoom();
        break;
      case MOVE_CAMERA:
        animateMoveCamera();
        break;
      case MORPH:
        animateMorph();
        break;
      case RESIZE:
        animateResize();
        break;
      case BLINK:
        animateBlink();
        break;
    }
  }

  /**
   * Demonstrates animated loading of whole graph structures.
   */
  private void animatedLoad() {
    compoundAction = true;

    openGraph(i18n.getString(DEMO_NAME + ".RESOURCE.graph." +
        oh.get("misc", "animateLoad_graph")));

    final Graph2D graph = view.getGraph2D();
    
    if (graph.nodeCount() > 0) {
      final ViewAnimationFactory factory = createManagedAnimationFactory();
      play(factory.fadeIn(graph,
          (ViewAnimationFactory.NodeOrder) oh.get("misc", "animateLoad_nodeOrder"),
          oh.getBool("misc", "animateLoad_obeyEdgeDirection"),
          oh.getDouble("misc", "animateLoad_ratio"),
          PREFERRED_DURATION_GRAPH),
          factory.getRepaintManager());

    }

    compoundAction = false;
  }

  /**
   * Demonstrates animated removal of whole graph structures.
   */
  private void animatedClear() {
    compoundAction = true;

    final Graph2D graph = view.getGraph2D();
    if (graph.nodeCount() < 1) {
      openGraph(i18n.getString(DEMO_NAME + ".RESOURCE.graph.big"));
    }

    if (graph.nodeCount() > 0) {
      final ViewAnimationFactory factory = createManagedAnimationFactory();
      factory.setQuality(ViewAnimationFactory.HIGH_QUALITY);
      endHandler.setClear(true);
      play(factory.fadeOut(graph,
          (ViewAnimationFactory.NodeOrder) oh.get("misc", "animateClear_nodeOrder"),
          oh.getBool("misc", "animateClear_obeyEdgeDirection"),
          oh.getDouble("misc", "animateClear_ratio"),
          PREFERRED_DURATION_GRAPH),
          factory.getRepaintManager());
    }

    compoundAction = false;
  }

  /**
   * Demonstrates animated edge traversal.
   */
  private void animateTraverseEdge() {
    final Graph2D graph = view.getGraph2D();
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // use a concurrency object, since we want to traverse all selected edges
    // simultaneous
    final CompositeAnimationObject traversal = AnimationFactory.createConcurrency();

    EdgeCursor edgesToTraverse = graph.selectedEdges();
    if (!edgesToTraverse.ok()) {
      edgesToTraverse = graph.edges();
    }
    for (EdgeCursor ec = edgesToTraverse; ec.ok(); ec.next()) {
      // the edge to be traversed
      final EdgeRealizer er = graph.getRealizer(ec.edge());
      er.setVisible(true);

      final EdgeRealizer bak = er.createCopy();

      // set up the visual features of the "yet-to-be-traversed"
      // part of the edge
      final EdgeRealizer unvisited1 = er.createCopy();
      unvisited1.setLineColor((Color) oh.get("misc", "colorUnvisited"));
//      unvisited1.setSourceArrow(er.getSourceArrow());
//      unvisited1.setTargetArrow(er.getTargetArrow());

      final EdgeRealizer unvisited2 = er.createCopy();
      unvisited2.setLineColor((Color) oh.get("misc", "colorVisited"));
      unvisited2.setLineType(LineType.LINE_2);
      unvisited2.setSourceArrow(Arrow.NONE);
      unvisited2.setTargetArrow(Arrow.NONE);

      // set up the visual features of the "already-traversed"
      // part of the edge
      final EdgeRealizer visited1 = er.createCopy();
      visited1.setLineColor(unvisited2.getLineColor());
      visited1.setLineType(LineType.LINE_2);
      visited1.setSourceArrow(Arrow.NONE);
      visited1.setTargetArrow(Arrow.NONE);

      final EdgeRealizer visited2 = er.createCopy();
      visited2.setLineColor(unvisited1.getLineColor());
//      visited2.setSourceArrow(er.getTargetArrow());
//      visited2.setTargetArrow(er.getSourceArrow());

      // combine two traversals to run one after the other:
      // first traverse the edge from source to target,
      // then back from target to source; fix arrows in between
      final CompositeAnimationObject seq = AnimationFactory.createLazySequence();
      seq.addAnimation(
          factory.traverseEdge(er, visited1, unvisited1, true,
              ViewAnimationFactory.RESET_EFFECT,
              PREFERRED_DURATION_TRAVERSAL));

      // fix realizer state
      seq.addAnimation(new AnimationObject() {
        public void initAnimation() {
          er.setLineType(visited1.getLineType());
          er.setLineColor(visited1.getLineColor());
        }

        public void calcFrame(double time) {
          // do nothing
        }

        public void disposeAnimation() {
          // do nothing
        }

        public long preferredDuration() {
          return 0;
        }
      });
      seq.addAnimation(AnimationFactory.createPause(500));
      seq.addAnimation(
          factory.traverseEdge(er, visited2, unvisited2, false,
              ViewAnimationFactory.APPLY_EFFECT,
              PREFERRED_DURATION_TRAVERSAL));

      // fix realizer state
      seq.addAnimation(new AnimationObject() {
        public void initAnimation() {
          // do nothing
        }

        public void calcFrame(double time) {
          // do nothing
        }

        public void disposeAnimation() {
          er.setSelected(bak.isSelected());
          er.setLineColor(bak.getLineColor());
          er.setLineType(bak.getLineType());
          er.setSourceArrow(bak.getSourceArrow());
          er.setTargetArrow(bak.getTargetArrow());
        }

        public long preferredDuration() {
          return 0;
        }
      });
      traversal.addAnimation(seq);
    }

    if (!traversal.isEmpty()) {
      play(traversal, factory.getRepaintManager());
    } else {
      JOptionPane.showMessageDialog(null, i18n.getString(DEMO_NAME + ".message.selectEdge"));
    }
  }

  /**
   * Demonstrates animated zooming in and out.
   */
  private void animateZoom() {
    final ViewAnimationFactory factory = createUnmanagedAnimationFactory();
    final double newZoom = oh.getDouble("misc", "zoom_factor");
    play(factory.zoom(newZoom, ViewAnimationFactory.APPLY_EFFECT, DEFAULT_DURATION), null);
  }

  /**
   * Demonstrates animated camera movements (by adjusting the view center).
   */
  private void animateMoveCamera() {
    final double x1 = view.toWorldCoordX(0);
    final double x2 = view.toWorldCoordX(view.getWidth());
    final double y1 = view.toWorldCoordY(0);
    final double y2 = view.toWorldCoordY(view.getHeight());

    // create a path to traverse with the camera
    final GeneralPath path = new GeneralPath();
    path.moveTo((float) ((x1 + x2) * 0.5), (float) ((y1 + y2) * 0.5));
    path.lineTo((float) (x1 + 0.15 * (x2 - x1)), (float) (y1 + 0.15 * (y2 - y1)));
    path.lineTo((float) (x2 - 0.15 * (x2 - x1)), (float) (y1 + 0.15 * (y2 - y1)));
    path.lineTo((float) ((x1 + x2) * 0.5), (float) ((y1 + y2) * 0.5));
    path.lineTo((float) (x2 - 0.15 * (x2 - x1)), (float) (y2 - 0.15 * (y2 - y1)));
    path.lineTo((float) (x1 + 0.15 * (x2 - x1)), (float) (y2 - 0.15 * (y2 - y1)));
    path.quadTo((float) ((x1 + x2) * 0.5), (float) ((y1 + y2) * 0.5),
        (float) (x1 + 0.15 * (x2 - x1)), (float) (y1 + 0.25 * (y2 - y1)));
    path.quadTo((float) ((x1 + x2) * 0.5), (float) (y2 - 0.25 * (y2 - y1)),
        (float) ((x1 + x2) * 0.5), (float) ((y1 + y2) * 0.5));

    final ViewAnimationFactory factory = createUnmanagedAnimationFactory();
    play(factory.moveCamera(path, PREFERRED_DURATION_CAMERA), null);
  }

  /**
   * Demonstrates animated changing of visual features of a NodeRealizer.
   */
  private void animateMorph() {
    final Graph2D graph = view.getGraph2D();
    {
      // use a concurrency object to morph all selected nodes simultaneously
      final CompositeAnimationObject morph = AnimationFactory.createConcurrency();
      final ViewAnimationFactory factory = createManagedAnimationFactory();

      NodeCursor nodesToMorph = graph.selectedNodes();
      if (!nodesToMorph.ok()) {
        nodesToMorph = graph.nodes();
      }
      for (NodeCursor nc = nodesToMorph; nc.ok(); nc.next()) {
        final NodeRealizer nr = graph.getRealizer(nc.node());

        // set up the new visiual features according to user-specified options
        final NodeRealizer morphTarget = nr.createCopy();
        morphTarget.moveBy(oh.getDouble("misc", "translateX"),
            oh.getDouble("misc", "translateY"));
        morphTarget.setSize(oh.getDouble("misc", "width"),
            oh.getDouble("misc", "height"));
        morphTarget.setLineColor((Color) oh.get("misc", "lineColor"));
        morphTarget.setFillColor((Color) oh.get("misc", "fillColor"));
        morphTarget.setFillColor2((Color) oh.get("misc", "fillColor2"));

        morph.addAnimation(
            factory.morph(
                nr, morphTarget, ViewAnimationFactory.APPLY_EFFECT,
                DEFAULT_DURATION));
      }
      if (!morph.isEmpty()) {
        play(morph, factory.getRepaintManager());
      } else {
        JOptionPane.showMessageDialog(null, i18n.getString(DEMO_NAME + ".message.selectNode"));
      }
    }
  }

  /**
   * Demonstrates animated resizing of nodes.
   */
  private void animateResize() {
    final Graph2D graph = view.getGraph2D();
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // use a concurrency object to resize all selected nodes simultaneously
    final CompositeAnimationObject resize = AnimationFactory.createConcurrency();

    NodeCursor nodesToResize = graph.selectedNodes();
    if (!nodesToResize.ok()) {
      nodesToResize = graph.nodes();
    }
    for (NodeCursor nc = nodesToResize; nc.ok(); nc.next()) {
      // set up the new size according to user-specified option
      final Value2D size =
          DefaultMutableValue2D.create(oh.getDouble("misc", "resize_width"),
              oh.getDouble("misc", "resize_height"));

      resize.addAnimation(
          factory.resize(
              graph.getRealizer(nc.node()), size,
              ViewAnimationFactory.APPLY_EFFECT, SHORT_DURATION));
    }
    if (!resize.isEmpty()) {
      play(resize, factory.getRepaintManager());
    } else {
      JOptionPane.showMessageDialog(null, i18n.getString(DEMO_NAME + ".message.selectNode"));
    }
  }

  /**
   * Demonstrates blinking of nodes.
   */
  private void animateBlink() {
    final Graph2D graph = view.getGraph2D();
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // use a concurrency object to make all selected nodes blink simultaneously
    final CompositeAnimationObject blink = AnimationFactory.createConcurrency();

    NodeCursor nodesToBlink = graph.selectedNodes();
    if (!nodesToBlink.ok()) {
      nodesToBlink = graph.nodes();
    }
    for (NodeCursor nc = nodesToBlink; nc.ok(); nc.next()) {
      // set-up blink count according to user-specified option
      final int repetitions = oh.getInt("misc", "repetitions");
      if (repetitions > 1) {
        blink.addAnimation(
            AnimationFactory.createRepetition(
                factory.blink(graph.getRealizer(nc.node()),
                    SHORT_DURATION),
                repetitions, false));
      } else {
        blink.addAnimation(
            factory.blink(graph.getRealizer(nc.node()), SHORT_DURATION));
      }
    }

    if (!blink.isEmpty()) {
      play(blink, factory.getRepaintManager());
    } else {
      JOptionPane.showMessageDialog(null, i18n.getString(DEMO_NAME + ".message.selectNode"));
    }
  }

  /**
   * Demonstrates animated creation of nodes.
   * @param nr   the <code>NodeRealizer</code> representing the node
   */
  private void animateCreate(final NodeRealizer nr) {
    nr.setVisible(false);

    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // select animation according to user-specified option
    switch (((Byte) oh.get("elements", "createNode")).byteValue()) {
      case NO_ANIM:
        nr.setVisible(true);
        break;
      case BLUR_IN:
        play(factory.blurIn(nr, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case FADE_IN:
        play(factory.fadeIn(nr, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case IMPLODE:
        play(factory.implode(nr, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case WHIRL_IN:
        play(factory.whirlIn(nr, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
    }
  }

  /**
   * Demonstrates animated creation of edges.
   * @param er   the <code>EdgeRealizer</code> representing the edge
   */
  private void animateCreate(final EdgeRealizer er) {
    er.setVisible(false);

    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // select animation according to user-specified option
    switch (((Byte) oh.get("elements", "createEdge")).byteValue()) {
      case NO_ANIM:
        er.setVisible(true);
        break;
      case BLUR_IN:
        play(factory.blurIn(er, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case FADE_IN:
        play(factory.fadeIn(er, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case IMPLODE:
        play(factory.implode(er, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case EXTRACT:
        play(factory.extract(er, DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
    }
  }

  /**
   * Demonstrates animated deletion of nodes.
   * @param nr   the <code>NodeRealizer</code> representing the node
   */
  private void animateDelete(final NodeRealizer nr) {
    final Graph2D graph = view.getGraph2D();
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    final CompositeAnimationObject deletes =
        AnimationFactory.createLazySequence();
    final CompositeAnimationObject deleteEdges =
        AnimationFactory.createConcurrency();

    // first delete all edges of the specified node in an animated fashion
    for (EdgeCursor ec = nr.getNode().edges(); ec.ok(); ec.next()) {
      final EdgeRealizer er = graph.getRealizer(ec.edge());

      // select animation according to user-specified option
      switch (((Byte) oh.get("elements", "deleteEdge")).byteValue()) {
        case NO_ANIM:
          deleteEdges.addAnimation(new RemoveEdge(er, factory.getRepaintManager()));
          break;
        case BLUR_OUT:
          deleteEdges.addAnimation(
              factory.blurOut(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case FADE_OUT:
          deleteEdges.addAnimation(
              factory.fadeOut(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case EXPLODE:
          deleteEdges.addAnimation(
              factory.explode(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case RETRACT:
          deleteEdges.addAnimation(
              factory.retract(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
      }
    }
    if (deleteEdges.preferredDuration() > 0) {
      deletes.addAnimation(deleteEdges);
    }

    // select animation according to user-specified option
    switch (((Byte) oh.get("elements", "deleteNode")).byteValue()) {
      case NO_ANIM:
        deletes.addAnimation(new RemoveNode(nr, factory.getRepaintManager()));
        break;
      case BLUR_OUT:
        deletes.addAnimation(
            factory.blurOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                DEFAULT_DURATION));
        break;
      case FADE_OUT:
        deletes.addAnimation(
            factory.fadeOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                DEFAULT_DURATION));
        break;
      case EXPLODE:
        deletes.addAnimation(
            factory.explode(nr, ViewAnimationFactory.APPLY_EFFECT,
                DEFAULT_DURATION));
        break;
      case WHIRL_OUT:
        deletes.addAnimation(
            factory.whirlOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                DEFAULT_DURATION));
        break;
    }
    play(deletes, factory.getRepaintManager());
  }

  /**
   * Demonstrates animated deletion of edges.
   * @param er   the <code>EdgeRealizer</code> representing the edge
   */
  private void animateDelete(final EdgeRealizer er) {
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    // select animation according to user-specified option
    switch (((Byte) oh.get("elements", "deleteEdge")).byteValue()) {
      case NO_ANIM:
        play(new RemoveEdge(er, factory.getRepaintManager()),
            factory.getRepaintManager());
        break;
      case BLUR_OUT:
        play(factory.blurOut(er, ViewAnimationFactory.APPLY_EFFECT,
            DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case FADE_OUT:
        play(factory.fadeOut(er, ViewAnimationFactory.APPLY_EFFECT,
            SHORT_DURATION),
            factory.getRepaintManager());
        break;
      case EXPLODE:
        play(factory.explode(er, ViewAnimationFactory.APPLY_EFFECT,
            DEFAULT_DURATION),
            factory.getRepaintManager());
        break;
      case RETRACT:
        play(factory.retract(er, ViewAnimationFactory.APPLY_EFFECT,
            SHORT_DURATION),
            factory.getRepaintManager());
        break;
    }
  }

  /**
   * Demonstrates animated deletion of all selected graph elements.
   */
  private void animateDeleteSelection() {
    final ViewAnimationFactory factory = createManagedAnimationFactory();

    final Graph2D graph = view.getGraph2D();

    final byte animatedEdgeDelete =
        ((Byte) oh.get("elements", "deleteEdge")).byteValue();

    final byte animatedNodeDelete =
        ((Byte) oh.get("elements", "deleteNode")).byteValue();

    // use a concurrency object to make all selected edges disappear
    // simultaneously
    final CompositeAnimationObject deleteEdges =
        AnimationFactory.createConcurrency();

    final EdgeMap markAsScheduled = graph.createEdgeMap();

    // first schedule all selected edges for removal
    for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
      final EdgeRealizer er = graph.getRealizer(ec.edge());

      // select animation according to user-specified option
      switch (animatedEdgeDelete) {
        case NO_ANIM:
          deleteEdges.addAnimation(new RemoveEdge(er, factory.getRepaintManager()));
          break;
        case BLUR_OUT:
          deleteEdges.addAnimation(
              factory.blurOut(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case FADE_OUT:
          deleteEdges.addAnimation(
              factory.fadeOut(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case EXPLODE:
          deleteEdges.addAnimation(
              factory.explode(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
        case RETRACT:
          deleteEdges.addAnimation(
              factory.retract(er, ViewAnimationFactory.APPLY_EFFECT,
                  SHORT_DURATION));
          break;
      }
      markAsScheduled.setBool(ec.edge(), true);
    }

    // then schedule all edges to or from selected nodes for removal
    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      for (EdgeCursor ec = nc.node().edges(); ec.ok(); ec.next()) {
        if (!markAsScheduled.getBool(ec.edge())) {
          final EdgeRealizer er = graph.getRealizer(ec.edge());

          // select animation according to user-specified option
          switch (animatedEdgeDelete) {
            case NO_ANIM:
              deleteEdges.addAnimation(new RemoveEdge(er, factory.getRepaintManager()));
              break;
            case BLUR_OUT:
              deleteEdges.addAnimation(
                  factory.blurOut(er, ViewAnimationFactory.APPLY_EFFECT,
                      SHORT_DURATION));
              break;
            case FADE_OUT:
              deleteEdges.addAnimation(
                  factory.fadeOut(er, ViewAnimationFactory.APPLY_EFFECT,
                      SHORT_DURATION));
              break;
            case EXPLODE:
              deleteEdges.addAnimation(
                  factory.explode(er, ViewAnimationFactory.APPLY_EFFECT,
                      SHORT_DURATION));
              break;
            case RETRACT:
              deleteEdges.addAnimation(
                  factory.retract(er, ViewAnimationFactory.APPLY_EFFECT,
                      SHORT_DURATION));
              break;
          }
          markAsScheduled.setBool(ec.edge(), true);
        }
      }
    }

    graph.disposeEdgeMap(markAsScheduled);

    // use a concurrency object to make all selected edges disappear
    // simultaneously
    final CompositeAnimationObject deleteNodes =
        AnimationFactory.createConcurrency();

    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      final NodeRealizer nr = graph.getRealizer(nc.node());

      // select animation according to user-specified option
      switch (animatedNodeDelete) {
        case NO_ANIM:
          deleteNodes.addAnimation(new RemoveNode(nr, factory.getRepaintManager()));
          break;
        case BLUR_OUT:
          deleteNodes.addAnimation(
              factory.blurOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                  DEFAULT_DURATION));
          break;
        case FADE_OUT:
          deleteNodes.addAnimation(
              factory.fadeOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                  DEFAULT_DURATION));
          break;
        case EXPLODE:
          deleteNodes.addAnimation(
              factory.explode(nr, ViewAnimationFactory.APPLY_EFFECT,
                  LONG_DURATION));
          break;
        case WHIRL_OUT:
          deleteNodes.addAnimation(
              factory.whirlOut(nr, ViewAnimationFactory.APPLY_EFFECT,
                  DEFAULT_DURATION));
          break;
      }
    }

    // play animations

    // use a sequence object to make all selected edges disappear
    // before the selected nodes disappear
    final CompositeAnimationObject deletes = AnimationFactory.createLazySequence();

    if (!deleteEdges.isEmpty()) {
      deletes.addAnimation(deleteEdges);
    }
    if (!deleteNodes.isEmpty()) {
      deletes.addAnimation(deleteNodes);
    }
    if (!deletes.isEmpty()) {
      play(deletes, factory.getRepaintManager());
    }
  }

  /**
   * Plays the specified animation an registers the specified repaint manager
   * as an <code>AnimationListener</code>.
   */
  private void play(final AnimationObject ao, final Graph2DViewRepaintManager arm) {
    player.addAnimationListener(new AutoRemoveListener(arm != null
        ? (AnimationListener) arm
        : view));
    player.setSpeed(oh.getDouble("global", "speed"));
    player.animate(ao);
  }


  /**
   * Factory method to create an <code>ViewAnimationFactory</code>.
   */
  private ViewAnimationFactory createUnmanagedAnimationFactory() {
    return unmanagedFactory;
  }

  /**
   * Factory method to create an <code>ViewAnimationFactory</code> that uses
   * a <code>Graph2DViewRepaintManager</code>.
   */
  private ViewAnimationFactory createManagedAnimationFactory() {
    final ViewAnimationFactory factory =
        new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
    factory.setQuality(ViewAnimationFactory.HIGH_QUALITY);
    return factory;
  }

  /**
   * Factory method to create an <code>Action</code> that triggers animated
   * edge creation between selected nodes.
   */
  private Action createCreateEdgeAction() {
    final Action create = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        final Graph2D graph = view.getGraph2D();

        Node lastNode = null;
        for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
          if (lastNode != null) {
            final EdgeRealizer er = new PolyLineEdgeRealizer();
            er.setVisible(false);
            graph.createEdge(lastNode, nc.node(), er);
            animateCreate(er);
          }
          lastNode = nc.node();
        }

        if (lastNode == null) {
          JOptionPane.showMessageDialog(null, i18n.getString(DEMO_NAME + ".message.selectNodes"));
        }
      }
    };
    localizeAction(create, DEMO_NAME + ".action.CreateEdge");

    return create;
  }

  /**
   * Factory method to create an <code>Action</code> that triggers animated
   * deletion of selected graph elements.
   */
  private Action createDeleteSelectionAction() {
    final Action delete = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        animateDeleteSelection();
      }
    };
    localizeAction(delete, DEMO_NAME + ".action.DeleteSelection");

    return delete;
  }

  /**
   * Factory method to create an <code>Action</code> that triggers animated
   * deletion of selected nodes.
   */
  private Action createDeleteNodeAction() {
    final Action delete = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        final Graph2D graph = view.getGraph2D();
        final NodeCursor nc = graph.selectedNodes();
        if (nc.ok()) {
          animateDelete(graph.getRealizer(nc.node()));
//          view.updateView();
        }
      }
    };
    localizeAction(delete, DEMO_NAME + ".action.DeleteNode");

    return delete;
  }

  /**
   * Factory method to create an <code>Action</code> that triggers animated
   * deletion of selected edges.
   */
  private Action createDeleteEdgeAction() {
    final Action delete = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        final Graph2D graph = view.getGraph2D();
        final EdgeCursor ec = graph.selectedEdges();
        if (ec.ok()) {
          animateDelete(graph.getRealizer(ec.edge()));
        }
      }
    };
    localizeAction(delete, DEMO_NAME + ".action.DeleteEdge");

    return delete;
  }


  /**
   * Registers a <code>GraphListener</code> on the demo's
   * <code>Graph2DView</code> that triggers animated node creation.
   */
  private void registerGraphListener() {
    final Graph2D graph = view.getGraph2D();
    graph.addGraphListener(new GraphListener() {
      public void onGraphEvent(final GraphEvent e) {
        if (GraphEvent.NODE_CREATION == e.getType() && !compoundAction) {
          animateCreate(graph.getRealizer((Node) e.getData()));
        }
      }
    });
  }

  /**
   * Registers an <code>EditMode</code> that provides context popup menus
   * to trigger animated node and edge deletion and edge creation.
   */
  private void registerEditMode() {
    EditMode editMode = new EditMode();
    editMode.allowMovingWithPopup(true);
    
    PopupMode popupMode = new PopupMode() {
      private final Action createEdge = createCreateEdgeAction();
      private final Action deleteNode = createDeleteNodeAction();
      private final Action deleteEdge = createDeleteEdgeAction();

      public JPopupMenu getSelectionPopup(double x, double y) {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(createEdge);
        return menu;
      }

      public JPopupMenu getNodePopup(final Node v) {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(deleteNode);
        return menu;
      }

      public JPopupMenu getEdgePopup(final Edge e) {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(deleteEdge);
        return menu;
      }
    };
    editMode.setPopupMode(popupMode);
    view.addViewMode(editMode);
  }

  /**
   * Binds key events to actions.
   */
  private void registerActions() {
    final ActionMap amap = new ActionMap();
    amap.put("DELETE_SELECTION", createDeleteSelectionAction());
    final InputMap imap = new InputMap();
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_SELECTION");
    view.getCanvasComponent().setActionMap(amap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
  }


  public void addContentTo(final JRootPane rootPane) {
    openGraph(i18n.getString(DEMO_NAME + ".RESOURCE.graph.small"));
    registerGraphListener();
    registerEditMode();
    registerActions();
    rootPane.setContentPane(createContentPane());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        final GuiFactory gf = createGuiFactory();
        final AnimationEffectsDemo demo = new AnimationEffectsDemo(gf, true);
        final JFrame frame = new JFrame(gf.getString(DEMO_NAME + ".title"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        demo.addContentTo(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      }
    });
  }

  private static GuiFactory createGuiFactory() {
    try {
      final ResourceBundleGuiFactory i18n = new ResourceBundleGuiFactory();
      i18n.addBundle(AnimationEffectsDemo.class.getName());
      return i18n;
    }
    catch (final MissingResourceException mre) {
      System.err.println("Could not find resources! " + mre);
      return new GuiFactory() {
        public JButton createButton(final String action) {
          return new JButton(action);
        }

        public String getString(final String key) {
          return key;
        }

        public Action createHelpAction(final String helpKey) {
          return null;
        }
      };
    }
  }

  /**
   * <code>AnimationListener</code> implementation that automatically
   * de-registers itself on <code>AnimationEvent.END</code>.
   */
  private final class AutoRemoveListener implements AnimationListener {
    private final AnimationListener delegate;

    AutoRemoveListener(final AnimationListener delegate) {
      this.delegate = delegate;
    }

    public void animationPerformed(final AnimationEvent e) {
      delegate.animationPerformed(e);
      if (AnimationEvent.END == e.getHint()) {
        player.removeAnimationListener(this);
      }
    }
  }

  /**
   * <code>AnimationListener</code> implementation that triggers
   * a view update on <code>AnimationEvent.END</code>.
   */
  private final class EndHandler implements AnimationListener {
    private boolean clear;

    EndHandler() {
      this.clear = false;
    }

    void setClear(final boolean clear) {
      this.clear = clear;
    }

    public void animationPerformed(final AnimationEvent e) {
      if (AnimationEvent.END == e.getHint()) {
        if (clear) {
          view.getGraph2D().clear();
          view.fitContent();
          clear = false;
        }
        view.updateView();
      }
    }
  }

  /**
   * <em>Animation</em> that removes a node without visual feed back.
   * This <code>AnimationObject</code> is used to simplify the
   * <em>delete selection</em> action when using animated edge deletion
   * but no animated node deletion.
   */
  private static final class RemoveNode implements AnimationObject {
    private NodeRealizer realizer;
    private Graph2DViewRepaintManager manager;

    public RemoveNode(final NodeRealizer realizer,
                      final Graph2DViewRepaintManager manager) {
      this.realizer = realizer;
      this.manager = manager;
    }

    public void initAnimation() {
      if (manager != null) {
        manager.add(realizer);
      }
    }

    public void calcFrame(final double time) {
    }

    public void disposeAnimation() {
      final Node node = realizer.getNode();
      node.getGraph().removeNode(node);

      if (manager != null) {
        manager.remove(realizer);
        manager = null;
      }

      realizer = null;
    }

    public long preferredDuration() {
      return 10;
    }
  }

  /**
   * <em>Animation</em> that removes an edge without visual feed back.
   * This <code>AnimationObject</code> is used to simplify the
   * <em>delete selection</em> action when using animated node deletion
   * but no animated edge deletion.
   */
  private static final class RemoveEdge implements AnimationObject {
    private EdgeRealizer realizer;
    private Graph2DViewRepaintManager manager;

    public RemoveEdge(final EdgeRealizer realizer,
                      final Graph2DViewRepaintManager manager) {
      this.realizer = realizer;
      this.manager = manager;
    }

    public void initAnimation() {
      if (manager != null) {
        manager.add(realizer);
      }
    }

    public void calcFrame(final double time) {
    }

    public void disposeAnimation() {
      final Edge edge = realizer.getEdge();
      edge.getGraph().removeEdge(edge);

      if (manager != null) {
        manager.remove(realizer);
        manager = null;
      }

      realizer = null;
    }

    public long preferredDuration() {
      return 10;
    }
  }
}
