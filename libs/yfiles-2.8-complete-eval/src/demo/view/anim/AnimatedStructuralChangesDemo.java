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
import demo.view.DemoDefaults;

import y.anim.AnimationEvent;
import y.anim.AnimationFactory;
import y.anim.AnimationListener;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GraphMLIOHandler;
import y.layout.BufferedLayouter;
import y.layout.GraphLayout;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DViewRepaintManager;
import y.view.LayoutMorpher;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Demonstrates how to combine animation effects for structural graph changes
 * with animated graph layout changes.
 * The demonstrated effects will start automatically and loop until the user
 * ends the demo.
 *
 */
public class AnimatedStructuralChangesDemo extends DemoBase {
  /**
   * Preferred duration for all animation effects.
   */
  private static final int PREFERRED_DURATION = 500;

  /**
   * Maximum edge count when randomizing the graph structure.
   */
  private static final int MAX_EDGE_COUNT = 75;

  /**
   * Maximum node count when randomizing the graph structure.
   */
  private static final int MAX_NODE_COUNT = 50;


  private final Random random;
  private final ViewAnimationFactory factory;
  private final Graph2D graph;

  private boolean disposed;

  public AnimatedStructuralChangesDemo() {
    random = new Random(42);
    factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
    graph = view.getGraph2D();
    view.setPreferredSize(new Dimension(800, 600));
    view.addComponentListener(new ComponentAdapter() {
      public void componentResized(final ComponentEvent e) {
        if (e.getSource() == view) {
          view.removeComponentListener(this);

          // finally view has been assigned a valid size which allows
          // fitContent to work correctly
          view.fitContent();

          showInitialGraph();
        }
      }
    });

    configureRealizers();
    prepareInitialGraph();
  }

  private void configureRealizers() {
    // painting shadows is expensive and therefore not well suited for animations
    DemoDefaults.registerDefaultNodeConfiguration(false);
    DemoDefaults.configureDefaultRealizers(view);
  }

  /**
   * Overridden to disable user interaction.
   */
  protected EditMode createEditMode() {
    return null;
  }

  /**
   * Overridden to disable user interaction.
   */
  protected JMenuBar createMenuBar() {
    final JMenu file = new JMenu("File");
    file.add(new ExitAction());

    final JMenuBar jmb = new JMenuBar();
    jmb.add(file);
    return jmb;
  }

  /**
   * Overridden to disable user interaction.
   */
  protected JToolBar createToolBar() {
    return null;
  }

  private void prepareInitialGraph() {
    // try to load an initial graph
    final URL resource = getClass().getResource("resource/hierarchic.graphml");
    
    if (resource != null) {
      final GraphMLIOHandler ioh = new GraphMLIOHandler();
      try {
        ioh.read(graph, resource);
      } catch (IOException ioe) {
        System.err.println(ioe.getMessage());
        graph.clear();
      }
    } else {
      System.err.println("Could not load \"resource/hierarchic.graphml\".");
      graph.clear();
    }

    DemoDefaults.applyRealizerDefaults(graph);
    
    if (graph.nodeCount() > 0) {
      graph.setDefaultNodeRealizer(graph.getRealizer(graph.firstNode()).createCopy());
    }
    // by default newly created nodes are invisible
    // animation effects will make new nodes visible later
    graph.getDefaultNodeRealizer().setVisible(false);

    if (graph.edgeCount() > 0) {
      graph.setDefaultEdgeRealizer(graph.getRealizer(graph.firstEdge()).createCopy());
    }
    // by default newly created edges are invisible
    // animation effects will make new edges visible later
    graph.getDefaultEdgeRealizer().setVisible(false);

    // set all graph elements to invisible initially
    // the first create animation will make these elements visible later on
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      graph.getRealizer(nc.node()).setVisible(false);
    }

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      graph.getRealizer(ec.edge()).setVisible(false);
    }
  }

  private void showInitialGraph() {
    final ArrayList newNodes = new ArrayList(graph.nodeCount());
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      newNodes.add(nc.node());
    }
    final ArrayList newEdges = new ArrayList(graph.edgeCount());
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      newEdges.add(ec.edge());
    }

    final AnimationPlayer player = new AnimationPlayer(false);

    // register the ViewAnimationFactory's repaint manager as animation
    // listener to prevent repaints for the complete Graph2DView and
    // thereby improving animation performance if possible
    player.addAnimationListener(factory.getRepaintManager());

    player.addAnimationListener(new Command() {
      void execute() {
        // start the main execution loop
        AnimatedStructuralChangesDemo.this.execute();
      }
    });

    // start the animation and idle for some time at the end
    player.animate(AnimationFactory.createSequence(
        createCreateAnimation(newNodes, newEdges),
        AnimationFactory.createPause(PREFERRED_DURATION)));
  }

  public void dispose() {
    disposed = true;
  }

  /**
   * Randomizes the graph structure, calculates a new graph layout, and finally
   * animates the structural and layout changes.
   */
  private void execute() {
    if (disposed) {
      return;
    }

    // determine nodes and edges that should be deleted
    final HashSet nodesToBeDeleted = new HashSet();
    final HashSet edgesToBeDeleted = new HashSet();
    markNodesForDeletion(nodesToBeDeleted, edgesToBeDeleted);
    markEdgesForDeletion(edgesToBeDeleted);

    // temporarily remove the elements that will be deleted later on
    // these elements are removed for two reasons:
    // 1. to prevent new edges being created for nodes that are marked for
    //    deletion
    // 2. to prevent these elements from being considered when calculating
    //    a new graph layout
    for (Iterator it = edgesToBeDeleted.iterator(); it.hasNext();) {
      graph.hide((Edge) it.next());
    }
    for (Iterator it = nodesToBeDeleted.iterator(); it.hasNext();) {
      graph.hide((Node) it.next());
    }

    // create some new nodes and edges
    final HashSet newNodes = new HashSet();
    createNodes(newNodes);
    final HashSet newEdges = new HashSet();
    createEdges(newNodes, newEdges);

    // calculate a new graph layout for the new graph structure
    // i.e. all elements marked for deletion have been removed at this point
    // and all new elements have been created already (so new elements will
    // appear at the correct location later)
    final GraphLayout gl = calcLayout(newNodes, newEdges);

    // now reinsert the elements marked for deletion, so the animation effects
    // will work properly
    // the actual deletion will be done by the animation effect, see also
    // the documentation for ViewAnimationFactory's APPLY_EFFECT
    for (Iterator it = nodesToBeDeleted.iterator(); it.hasNext();) {
      graph.unhide((Node) it.next());
    }
    for (Iterator it = edgesToBeDeleted.iterator(); it.hasNext();) {
      graph.unhide((Edge) it.next());
    }

    // create a shared, non-blocking AnimationPlayer
    // non-blocking, so a user can still interact with the SWING GUI
    // (even if it is only to quit the demo)
    final AnimationPlayer player = new AnimationPlayer(false);

    // now chain several animation effects
    // this is done because animations such as LayoutMorpher and
    // ViewAnimation.extract/ViewAnimation.retract are rather expensive to
    // create (which could severly hamper the animation frame rate) and
    // more important these animations use the state of their targets
    // at *instantiation* time

    // triggers re-execution of this method at the end of the final animation
    final Command loop = new Command() {
      void execute() {
        // cleanup
        player.removeAnimationListener(this);
        player.removeAnimationListener(factory.getRepaintManager());

        // loop
        AnimatedStructuralChangesDemo.this.execute();
      }
    };

    // triggers creating new nodes by fade in and new edges by extract
    final Command animateCreate = new Command() {
      void execute() {
        // cleanup
        player.removeAnimationListener(this);
        player.removeAnimationListener(view);

        // register looping for execution
        player.addAnimationListener(loop);

        // register the ViewAnimationFactory's repaint manager as animation
        // listener to prevent repaints for the complete Graph2DView and
        // thereby improving animation performance if possible
        player.addAnimationListener(factory.getRepaintManager());

        // start the animation and idle for some time at the end
        player.animate(AnimationFactory.createSequence(
            createCreateAnimation(newNodes, newEdges),
            AnimationFactory.createPause(PREFERRED_DURATION)));
      }
    };

    // triggers applying the new graph layout in an animated fashion
    final Command animateMorphing = new Command() {
      void execute() {
        // cleanup
        player.removeAnimationListener(this);
        player.removeAnimationListener(factory.getRepaintManager());

        // register the next animation effect for execution
        player.addAnimationListener(animateCreate);

        // register the complete Graph2DView as animation listener because
        // LayoutMorpher does not support repaint managers
        player.addAnimationListener(view);

        // start the animation
        player.animate(createMorphingAnimation(gl));
      }
    };

    // triggers deleting marked elements
    final Command animateDelete = new Command() {
      void execute() {
        // register the next animation effect for execution
        player.addAnimationListener(animateMorphing);

        // register the ViewAnimationFactory's repaint manager as animation
        // listener to prevent repaints for the complete Graph2DView and
        // thereby improving animation performance if possible
        player.addAnimationListener(factory.getRepaintManager());

        // start the animation
        player.animate(createDeleteAnimation(nodesToBeDeleted, edgesToBeDeleted));
      }
    };

    animateDelete.execute();
  }

  /*
  * #####################################################################
  * methods for randomized structural changes
  * #####################################################################
  */

  /**
   * Randomly determine edges to be deleted from the graph.
   * @param edgesToBeDeleted   will store the edges to be deleted.
   */
  private void markEdgesForDeletion(
      final Set edgesToBeDeleted
  ) {
    for (EdgeCursor ec = graph.edges();
         ec.ok() && graph.edgeCount() - edgesToBeDeleted.size() > 4;
         ec.next()) {
      if (!edgesToBeDeleted.contains(ec.edge()) && random.nextDouble() < 0.05) {
        edgesToBeDeleted.add(ec.edge());
      }
    }
  }

  /**
   * Randomly determines nodes to be deleted from the graph.
   * @param nodesToBeDeleted   will store the nodes to be deleted.
   * @param edgesToBeDeleted   will store all edges incident to nodes to be
   * deleted. (When removing nodes from a graph, incident edges are
   * automatically removed, too. However, by collecting these edges, they can
   * be deleted in an automated fashion.)
   */
  private void markNodesForDeletion(
      final Set nodesToBeDeleted,
      final Set edgesToBeDeleted
  ) {
    for (NodeCursor nc = graph.nodes();
         nc.ok() &&
             graph.nodeCount() - nodesToBeDeleted.size() > 4 &&
             graph.edgeCount() - edgesToBeDeleted.size() > 4;
         nc.next()) {
      if (random.nextDouble() < 0.05) {
        nodesToBeDeleted.add(nc.node());
        for (EdgeCursor ec = nc.node().edges(); ec.ok(); ec.next()) {
          edgesToBeDeleted.add(ec.edge());
        }
      }
    }
  }

  /**
   * Creates a random number of new nodes.
   * @param newNodes   will store the newly created nodes.
   */
  private void createNodes(
      final Set newNodes
  ) {
    if (graph.nodeCount() < MAX_NODE_COUNT + 1) {
      for (int i = 0, n = random.nextInt(MAX_NODE_COUNT + 1 - graph.nodeCount()); i < n; ++i) {
        final Node node = graph.createNode();
        newNodes.add(node);
      }
    }
  }

  /**
   * Creates a random number of new edges between randomly chosen new nodes.
   * New edges are created preferably between an old node and a new node.
   * Nodes are considered to be <em>new</em>, iff <code>newNodes.contains</code>
   * returns <code>true</code> and to be <em>old</em> otherwise.
   * <p>
   * Note, the implementation of this method relies on the fact that it is
   * called right after {@link #createNodes(java.util.Set)}.
   * @param newNodes   nodes marked as new.
   * @param newEdges   will store the newly created edges.
   */
  private void createEdges(
      final HashSet newNodes,
      final HashSet newEdges
  ) {
    if (graph.edgeCount() < MAX_EDGE_COUNT + 1) {
      final Node[] nodes = graph.getNodeArray();
      final int newCount = newNodes.size();
      final int oldCount = nodes.length - newCount;

      if (newCount > 1 && oldCount > 1) {
        // sort old nodes from upper left to lower right
        // this will result in new edges between old nodes being in hierarchic
        // flow direction
        Arrays.sort(nodes, 0, oldCount, new Comparator() {
          public int compare(final Object n1, final Object n2) {
            final double dy = graph.getCenterY((Node) n1) - graph.getCenterY((Node) n2);
            if (dy < 0) {
              return -1;
            } else if (dy > 0) {
              return 1;
            } else {
              final double dx = graph.getCenterX((Node) n1) - graph.getCenterX((Node) n2);
              if (dx < 0) {
                return -1;
              } else if (dx > 0) {
                return 1;
              } else {
                return 0;
              }
            }
          }
        });

        for (int i = 0, n = random.nextInt(MAX_EDGE_COUNT + 1 - graph.edgeCount()); i < n; ++i) {
          final double d = random.nextDouble();
          final Edge edge;
          if (d < 0.1) {
            // create an edge between two old nodes
            final int n1 = random.nextInt(oldCount);
            final int n2 = n1 + random.nextInt(oldCount - n1);
            edge = n1 != n2 ? graph.createEdge(nodes[n1], nodes[n2]) : null;
          } else if (d < 0.5) {
            // create an edge between an old and a new node
            edge = graph.createEdge(nodes[random.nextInt(oldCount)], nodes[oldCount + random.nextInt(newCount)]);
          } else if (d < 0.9) {
            // create an edge between a new and an old node
            edge = graph.createEdge(nodes[oldCount + random.nextInt(newCount)], nodes[random.nextInt(oldCount)]);
          } else {
            // create an edge between two new nodes
            final int n1 = oldCount + random.nextInt(newCount);
            final int n2 = oldCount + random.nextInt(newCount);
            edge = n1 != n2 ? graph.createEdge(nodes[n1], nodes[n2]) : null;
          }
          if (edge != null) {
            newEdges.add(edge);
          }
        }
      } else if (oldCount > 1) {
        // create edges between old nodes only (there are no new nodes)
        for (int i = 0, n = random.nextInt(MAX_EDGE_COUNT + 1 - graph.edgeCount()); i < n; ++i) {
          final int n1 = random.nextInt(oldCount);
          final int n2 = n1 + random.nextInt(oldCount - n1);
          if (n1 != n2) {
            newEdges.add(graph.createEdge(nodes[n1], nodes[n2]));
          }
        }
      } else if (newCount > 1) {
        // create edges between new nodes only (there are no old nodes)
        for (int i = 0, n = random.nextInt(MAX_EDGE_COUNT + 1 - graph.edgeCount()); i < n; ++i) {
          final int n1 = random.nextInt(newCount);
          final int n2 = random.nextInt(newCount);
          if (n1 != n2) {
            newEdges.add(graph.createEdge(nodes[n1], nodes[n2]));
          }
        }
      }
    }
  }

  /*
  * #####################################################################
  * factory methods for animations
  * #####################################################################
  */

  /**
   * Creates an animation for fading in new nodes and extracting new edges.
   * As a side effect, this animation will result in the new nodes and new
   * edges being visible.
   * @param newNodes   the nodes that should be faded in.
   * @param newEdges   the edges that should be extracted.
   * @return an animation for fading in new nodes and extracting new edges.
   */
  private AnimationObject createCreateAnimation(
      final Collection newNodes,
      final Collection newEdges
  ) {
    // create fade in animations for the new nodes and set them up to
    // play simultaneously
    final CompositeAnimationObject addNodes = AnimationFactory.createConcurrency();
    for (Iterator it = newNodes.iterator(); it.hasNext();) {
      final NodeRealizer nr = graph.getRealizer((Node) it.next());
      addNodes.addAnimation(factory.fadeIn(nr, PREFERRED_DURATION * 2));
    }

    // create extract animations for the new edges and set them up to
    // play simultaneously
    final CompositeAnimationObject addEdges = AnimationFactory.createConcurrency();
    for (Iterator it = newEdges.iterator(); it.hasNext();) {
      final EdgeRealizer er = graph.getRealizer((Edge) it.next());
      addEdges.addAnimation(factory.extract(er, PREFERRED_DURATION));
    }

    // create an animation that will first fade in nodes and then extract edges
    //
    // note that initAnimation for *both* addNodes and addEdges will happen
    // before both addNodes and addEdges are played and disposeAnimation
    // for *both* addNodes and addEdges will happen after addNodes and
    // addEdges are played
    // see also the API documentation for createSequence
    return AnimationFactory.createSequence(addNodes, addEdges);
  }

  /**
   * Creates an animation that applies the specified graph layout to the
   * graph structure.
   * <p>
   * Note that the graph may not be structurally altered in between creating
   * and disposing (at the end of playing) of it.
   * </p>
   * @param gl   the new graph layout to be applied in an animated fashion.
   * @return an animation that applies the specified graph layout to the
   * graph structure.
   */
  private AnimationObject createMorphingAnimation(
      final GraphLayout gl
  ) {
    final LayoutMorpher morphing = new LayoutMorpher(view, gl) {
      public void disposeAnimation() {
        super.disposeAnimation();
        view.fitContent();
      }
    };
    morphing.setPreferredDuration(PREFERRED_DURATION);
    morphing.setSmoothViewTransform(true);
    return AnimationFactory.createEasedAnimation(morphing);
  }

  /**
   * Creates an animation for retracting edges and fading out nodes.
   * As a side effect, this animation will result in said edges and nodes being
   * removed from the graph.
   * @param nodesToBeDeleted   the nodes to fade out
   * @param edgesToBeDeleted   the edges to retract
   * @return an animation for retracting edges and fading out nodes.
   */
  private AnimationObject createDeleteAnimation(
      final Set nodesToBeDeleted,
      final Set edgesToBeDeleted
  ) {
    // create retract animations for the edges and set them up to play
    // simultaneously
    // note, the specified APPLY_EFFECT will result in the edges being actually
    // removed at the end of the animation
    final CompositeAnimationObject deleteEdges = AnimationFactory.createConcurrency();
    for (Iterator it = edgesToBeDeleted.iterator(); it.hasNext();) {
      final EdgeRealizer er = graph.getRealizer((Edge) it.next());
      deleteEdges.addAnimation(factory.retract(
          er, ViewAnimationFactory.APPLY_EFFECT, PREFERRED_DURATION));
    }

    // create fade out animations for the nodes and set them up to play
    // simultaneously
    // note, the specified APPLY_EFFECT will result in the nodes being actually
    // removed at the end of the animation
    final CompositeAnimationObject deleteNodes = AnimationFactory.createConcurrency();
    for (Iterator it = nodesToBeDeleted.iterator(); it.hasNext();) {
      final NodeRealizer nr = graph.getRealizer((Node) it.next());
      deleteNodes.addAnimation(factory.fadeOut(
          nr, ViewAnimationFactory.APPLY_EFFECT, PREFERRED_DURATION));
    }

    // create an animation that will first retract edges and then fade out nodes
    //
    // note that initAnimation for *both* deleteEdges and deleteNodes will
    // happen before both deleteEdges and deleteNodes are played and
    // disposeAnimation for *both* deleteEdges and deleteNodes will happen
    // after deleteEdges and deleteNodes are played
    // see also the API documentation for createSequence
    return AnimationFactory.createSequence(deleteEdges, deleteNodes);
  }


  /**
   * Calculates a new hierarchic layout.
   * @param newNodes   nodes to be incrementally inserted into the existing
   * layout.
   * @param newEdges   edges to be incrementally inserted into the existing
   * layout.
   * @return a new hierarchic layout.
   */
  private GraphLayout calcLayout(
      final Set newNodes,
      final Set newEdges
  ) {
    final DataMap hints = Maps.createDataMap(new WeakHashMap());
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    ihl.setOrthogonallyRouted(true);
    final IncrementalHintsFactory hf = ihl.createIncrementalHintsFactory();
    for (Iterator it = newNodes.iterator(); it.hasNext();) {
      final Object node = it.next();
      hints.set(node, hf.createLayerIncrementallyHint(node));
    }
    for (Iterator it = newEdges.iterator(); it.hasNext();) {
      final Object edge = it.next();
      hints.set(edge, hf.createSequenceIncrementallyHint(edge));
      if (((Edge) edge).source().degree() == 1) {
        final Node node = ((Edge) edge).source();
        hints.set(node, hf.createLayerIncrementallyHint(node));
      }
      if (((Edge) edge).target().degree() == 1) {
        final Node node = ((Edge) edge).target();
        hints.set(node, hf.createLayerIncrementallyHint(node));
      }
    }
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, hints);
    try {
      return (new BufferedLayouter(ihl)).calcLayout(graph);
    } finally {
      graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
    }
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new AnimatedStructuralChangesDemo()).start();
      }
    });
  }


  private abstract static class Command implements AnimationListener {
    public void animationPerformed(final AnimationEvent e) {
      if (e.getHint() == AnimationEvent.END) {
        execute();
      }
    }

    abstract void execute();
  }
}
