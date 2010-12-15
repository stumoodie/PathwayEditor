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
package demo.layout.organic;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.algo.Bfs;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.CopiedLayoutGraph;
import y.layout.LayoutTool;
import y.layout.organic.InteractiveOrganicLayouter;
import y.util.DefaultMutableValue2D;
import y.util.GraphHider;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;

/**
 * This demo shows how to interactively navigate through a large
 * graph by showing only the neighbourhood of a focused node (proximity browsing).
 * To focus another node the user can simply click it.
 * By selecting a new focus node the visible part of the graph will be automatically adjusted.
 * <br>
 * In this demo the layout of the displayed subgraph is
 * controlled by {@link y.layout.organic.InteractiveOrganicLayouter}. This layout variant
 * allows to automatically layout the graph and manually change its node positions
 * at the same time.
 * <br>
 * The {@link demo.layout.organic.AnimatedNavigationDemo} extends the functionality of this demo
 * and adds animation support and more.
 */
public class NavigationDemo extends DemoBase {
  protected static final long PREFERRED_DURATION = 1000;

  /**
   * The layouter runs in its own thread.
   */
  protected InteractiveOrganicLayouter layouter;
  /**
   * The actual focused node
   */
  protected Node centerNode;

  protected GraphHider graphHider;

  /**
   * The animationPlayer is used for camera movements and to update the positions.
   */
  protected AnimationPlayer animationPlayer;
  protected ViewAnimationFactory factory;
  private Thread layoutThread;

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new NavigationDemo()).start("Navigation Demo");
      }
    });
  }

  public NavigationDemo() {
    
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        moveFirstNodeToCenter();        
      }
    });
    
  }

  protected void initialize() {
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setDrawEdgesFirst(true);
    view.setPaintDetailThreshold(0.0);
    Graph2DViewRepaintManager repaintManager = new Graph2DViewRepaintManager(view);
    factory = new ViewAnimationFactory(repaintManager);
    factory.setQuality(ViewAnimationFactory.HIGH_PERFORMANCE);
    animationPlayer = factory.createConfiguredPlayer();
    animationPlayer.setFps(25);

    graphHider = new GraphHider(view.getGraph2D());
    graphHider.setFireGraphEventsEnabled(true);

    loadInitialGraph();

    initLayouter();
    initUpdater(repaintManager);
  }

  public void dispose() {
    if (animationPlayer != null) {
      animationPlayer.stop();
    }
    if (layouter != null) {
      layouter.stop();
    }
    if (layoutThread != null) {
      layoutThread.interrupt();
    }
  }

  protected EditMode createEditMode() {
    EditMode editMode = new EditMode() {
      protected void nodeClicked(Node v) {
        moveToCenter(v, true);
      }
    };
    editMode.allowBendCreation(false);
    editMode.allowEdgeCreation(false);
    editMode.allowMoveLabels(false);
    editMode.allowMovePorts(false);
    editMode.allowNodeCreation(false);
    editMode.allowNodeEditing(false);
    editMode.allowResizeNodes(false);
    editMode.setMoveSelectionMode(new InteractiveMoveSelectionMode(layouter));
    return editMode;
  }

  protected void moveFirstNodeToCenter() {
    if (view.getGraph2D().nodeCount() > 0) {
      moveToCenter(view.getGraph2D().firstNode(), false);
    }
  }

  /**
   * Creates and starts an animation object that updates the positions of the nodes.
   * {@link #updatePositions()}
   * @param repaintManager
   */
  protected void initUpdater(final Graph2DViewRepaintManager repaintManager) {
    // use a tweaked AnimationObject as javax.swing.Timer replacement
    final AnimationObject updater = new AnimationObject() {
      public long preferredDuration() {
        return Long.MAX_VALUE;
      }

      public void calcFrame(double time) {
        if (updatePositions()) {
          repaintManager.invalidate();
        }
      }

      public void initAnimation() {
      }

      public void disposeAnimation() {
      }
    };
    animationPlayer.animate(updater);
  }

  /**
   * Loads the initial graph that is used in this demo.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/peopleNav.graphml");
    LayoutTool.resetPaths(view.getGraph2D());
    view.setZoom(0.80);
    view.updateView();
  }

  /**
   * Initializes the {@link y.layout.organic.InteractiveOrganicLayouter} and starts a thread for the
   * layouter.
   */
  protected void initLayouter() {
    layouter = new InteractiveOrganicLayouter();

    //After two seconds the layouter will stop.
    layouter.setMaxTime(2000);

    // propagate changes
    //Use an instance of CopiedLayoutGraph to avoid race conditions with the layout thread
    layoutThread = layouter.startLayout(new CopiedLayoutGraph(view.getGraph2D()));
    layoutThread.setPriority(Thread.MIN_PRIORITY);
  }

  /**
   * This method is called by the pseudo AnimationObject created in {@link #initUpdater(y.view.Graph2DViewRepaintManager)}.
   * It copies the information from the internal data structure of the layouter to the realizers of the nodes.<br>
   *
   * For "smooth movement" only a part of the delta between the position the layouter has calculated and the actual
   * displayed position, is moved.
   *
   * @return whether the max movement is bigger than 0.
   */
  protected boolean updatePositions() {
    if (layouter == null || !layouter.isRunning()) {
      return false;
    }
    double maxMovement = layouter.commitPositionsSmoothly(50, 0.15);
    return maxMovement > 0;
  }

  /**
   * This method is called whenever a user clicks at a node.
   * The new node is "centered" and the corresponding sector of the graph is displayed.
   *
   * @param newCenterNode
   */
  protected void moveToCenter(final Node newCenterNode, boolean animated) {
    //The structure updater allows synchronized write access on the graph structure that is layoutet.
    //So it is possible to add/remove nodes and edges or change values (e.g. the position) of the existing nodes.
    //The changes are scheduled and commited later.
    if (centerNode != null) {
      //Make the old centered node movable
      layouter.setInertia(centerNode, 0);
    }
    centerNode = newCenterNode;

    //the new centered node is "pinned" It will no longer be moved by the layouter
    layouter.setInertia(newCenterNode, 1);

    NodeList hiddenNodes = new NodeList(graphHider.hiddenNodes());

    graphHider.unhideAll();

    NodeList toHide = new NodeList(view.getGraph2D().nodes());
    NodeMap nodeMap = view.getGraph2D().createNodeMap();
    NodeList[] layers = Bfs.getLayers(view.getGraph2D(), new NodeList(centerNode), false, nodeMap, 3);
    view.getGraph2D().disposeNodeMap(nodeMap);
    for (int i = 0; i < layers.length; i++) {
      NodeList layer = layers[i];
      toHide.removeAll(layer);
    }

    graphHider.hide(toHide);

    // use "smart" initial placement for new elements
    double centerX = view.getGraph2D().getCenterX(newCenterNode);
    double centerY = view.getGraph2D().getCenterY(newCenterNode);
    for (NodeCursor nc = hiddenNodes.nodes(); nc.ok(); nc.next()) {
      if (view.getGraph2D().contains(nc.node())) {
        view.getGraph2D().setCenter(nc.node(), centerX, centerY);
        layouter.setCenter(nc.node(), centerX, centerY);
      }
    }

    layouter.wakeUp();

    //The camera movement.
    double x;
    double y;
    YPoint point = layouter.getCenter(newCenterNode);
    if (point != null) {
      x = point.getX();
      y = point.getY();
    } else {
      NodeRealizer realizer = view.getGraph2D().getRealizer(newCenterNode);
      x = realizer.getX();
      y = realizer.getY();
    }

    if (animated) {
      //An AnimationObject controlling the movement of the camera is created
      AnimationObject animationObject = factory.moveCamera(DefaultMutableValue2D.create(x, y), PREFERRED_DURATION);
      AnimationObject easedAnimation = AnimationFactory.createEasedAnimation(animationObject, 0.15, 0.25);
      animationPlayer.animate(easedAnimation);
    } else {
      view.setCenter(x, y);
    }

    //Now synchronize the structure updates with the copied graph that is layouted.
    layouter.syncStructure();
    layouter.wakeUp();
  }

  /**
   * Tells the layouter to update the position of the given node
   */
  protected void setPosition(Node node, double x, double y) {
    if (layouter == null || !layouter.isRunning()) {
      return;
    }
    layouter.setCenter(node, x, y);
  }


  protected Action createLoadAction() {
    return new DemoBase.LoadAction() {
      public void actionPerformed(ActionEvent e) {
        layouter.stop();
        centerNode = null;
        super.actionPerformed(e);
        graphHider = new GraphHider(view.getGraph2D());
        LayoutTool.resetPaths(view.getGraph2D());

        initLayouter();

        moveFirstNodeToCenter();
      }
    };
  }

  protected boolean isDeletionEnabled() {
    return false;
  }
}