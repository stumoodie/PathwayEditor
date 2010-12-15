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
package demo.layout.router;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.algo.Trees;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.YList;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.layout.router.BusDescriptor;
import y.layout.router.BusRepresentations;
import y.layout.router.BusRouter;
import y.module.BusRouterModule;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.util.GraphHider;
import y.view.BridgeCalculator;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DUndoManager;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows the capabilities of the yFiles {@link BusRouter} and demonstrates specific <em>hub</em> nodes to ease the usage
 * in an interactive environment.
 * <p/>
 * Typically, in a bus, every member node is connected to every other member which results in a large number of edges in
 * the graph. To disburden users from entering all these edges manually, this application introduces a specific type of
 * nodes, so-called <em>hubs</em>, which act as interchange points of the bus. A bus consists of all its interconnected
 * hubs, and all edges and regular nodes connected to them. For convenience, all connectors and edges of the same bus
 * are drawn in a common color.
 * <p/>
 * Regular nodes, hubs and edges can be interactively added and deleted, and snap lines are provided to ease the
 * editing. {@link BusRouterModule} is used as a means to configure the router. If grid is enabled, the router
 * calculates grid routes and the view highlights the grid points.
 */
public class BusRouterDemo extends DemoBase {
  private static final int MODE_ALL = 0;
  private static final int MODE_SELECTED = 1;
  private static final int MODE_PARTIAL = 2;

  private BusDyer busDyer;
  private BusRouterDemoModule module;
  private BusRouterDemoTools demoTools;
  private JComponent glassPane;
  private Graph2DUndoManager undoManager;

  /**
   * Creates a new instance of this demo.
   */
  public BusRouterDemo() {
    this(null);
  }

  /**
   * Creates a new instance of this demo and adds a help pane for the specified file.
   */
  public BusRouterDemo(final String helpFilePath) {
    // add and prepare the tool pane
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, demoTools.createToolPane(), view);
    contentPane.add(mainSplitPane, BorderLayout.CENTER);
    demoTools.updateGrid();
    demoTools.updateOrthogonalMode();
    demoTools.updateSnapLines();

    addHelpPane(helpFilePath);
    loadGraph("resource/threeBuses.graphml");
  }

  /**
   * Does the bus routing. This requires three steps: first, replace the hubs of each effected bus by a complete
   * subgraph; secondly, do the layouting of the resulting graph; and finally, convert the routed complete subgraphs
   * back to hub representation.
   */
  private void doLayout(final int mode) {
    prepareForLayout();
    final Graph2D graph = view.getGraph2D();

    // first step: replace the hubs of each bus in scope by a corresponding complete subgraph
    final EdgeMap edgeDescriptors = graph.createEdgeMap();
    final EdgeMap edgeIdMap = graph.createEdgeMap();

    try {
      EdgeList scopeList = replaceHubs(edgeDescriptors, mode);

      // second step: do the layout and create required data providers
      switch (mode) {
        case MODE_SELECTED:
        case MODE_PARTIAL:
          module.getBusRouter().setScope(BusRouter.SCOPE_SUBSET);
          graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
            public boolean getBool(Object dataHolder) {
              return edgeDescriptors.get(dataHolder) != null;
            }
          });
          break;
        case MODE_ALL:
        default:
          module.getBusRouter().setScope(BusRouter.SCOPE_ALL);
          break;
      }
      graph.addDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY, edgeDescriptors);
      module.start(graph);
      graph.removeDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY);
      graph.removeDataProvider(BusRouter.EDGE_SUBSET_DPKEY);

      // final step: create the hubs for the new layout
      BusRepresentations.replaceSubgraphByHubs(graph, scopeList.edges(), edgeDescriptors, edgeIdMap);
      busDyer.colorize(edgeIdMap);
    } catch (RuntimeException e) {
      String message = "Warning: " + e.getMessage() + "\n" +
          "The automatic routing for moved or new elements failed since the remainder is not a bus.\n" +
          "The option 'Automatic Routing' was disabled. Please choose 'Route All' from the toolbar\n" +
          "to calculate a valid routing from scratch and then enable this option again.";
      JOptionPane.showMessageDialog(view, message, "Automatic Routing", JOptionPane.WARNING_MESSAGE);
      demoTools.setAutomaticEnabled(false);
    } finally {
      graph.disposeEdgeMap(edgeDescriptors);
      graph.disposeEdgeMap(edgeIdMap);

      restoreAfterLayout();
      graph.updateViews();
    }
  }

  /**
   * Prepares properties of the graph and the view for the layout execution.
   */
  private void prepareForLayout() {
    final Graph2D graph = view.getGraph2D();

    if (glassPane == null) {
      // creates a glass pane to lock the GUI during layout
      glassPane = new JPanel();
      final JRootPane rootPane = view.getRootPane();
      rootPane.setGlassPane(glassPane);
    }

    // disable bridges since they do not look good during the layout animation
    glassPane.setEnabled(true);
    setBridgeCalculatorEnabled(false);
    graph.firePreEvent();
    graph.backupRealizers();
    // set the appropriate realizers for the new hub nodes and orthogonal edges
    graph.setDefaultNodeRealizer(demoTools.nodeRealizerTemplates[1]);
    graph.setDefaultEdgeRealizer(demoTools.edgeRealizerTemplates[1]);
  }

  /**
   * Restores properties of the graph and the view after the layout execution.
   */
  private void restoreAfterLayout() {
    final Graph2D graph = view.getGraph2D();

    // restore the original realizers
    demoTools.updateDefaultRealizer();

    glassPane.setEnabled(false);
    setBridgeCalculatorEnabled(true);
    graph.firePostEvent();
  }

  /**
   * Converts the buses defined by the hub nodes into the respective complete subgraph in which there are only
   * connections between the regular bus nodes as required by the {@link y.layout.router.BusRouter}. Also, creates
   * appropriate {@link BusDescriptor}s. If the mode is set to MODE_SELECTED, only buses which contain at least one
   * selected edge or hub are converted. If the mode is set to MODE_SELECTED_PARTS, only buses which contain at least
   * one moveable edge are converted.
   * <p/>
   * This method sets the bus IDs of all edges to the bus color. This is not required for the conversion of the layout
   * but simplifies the restoration of the correct bus color after the layout execution.
   *
   * @param busDescriptors an edge map that will be filled with {@link y.layout.router.BusDescriptor}s
   * @param mode           the mode to use
   * @throws IllegalArgumentException if the fixed subgraph is not a connected orthogonal tree
   */
  private EdgeList replaceHubs(EdgeMap busDescriptors, final int mode) {
    final Graph2D graph = view.getGraph2D();

    // a map which marks fixed and movable edges
    EdgeMap movableMarker = graph.createEdgeMap();

    // first step: identify the buses which belong to the scope of the given mode. Therefore, we have to check for
    // selected edges and end-nodes.
    GraphHider hider = new GraphHider(graph);
    List selectedBuses = new YList();

    try {
      final EdgeList[] busEdgesArray = BusRepresentations.toEdgeLists(graph, new HubMarkerDataProvider());
      hider.hideAll();

      // iteratively unhide one bus and its connected nodes
      for (int i = 0; i < busEdgesArray.length; i++) {
        final EdgeList busEdges = busEdgesArray[i];
        final NodeList busNodes = new NodeList();
        for (EdgeCursor ec = busEdges.edges(); ec.ok(); ec.next()) {
          busNodes.add(ec.edge().source());
          busNodes.add(ec.edge().target());
        }
        hider.unhideNodes(busNodes, false);
        hider.unhideEdges(busEdges);

        if (mode == MODE_SELECTED) {
          for (EdgeCursor ec = busEdges.edges(); ec.ok(); ec.next()) {
            final Edge edge = ec.edge();
            if (graph.isSelected(edge) || graph.isSelected(edge.source()) || graph.isSelected(edge.target())) {
              selectedBuses.add(busEdges);
              break;
            }
          }
        } else if (mode == MODE_PARTIAL) {
          if (markMoveableEdges(movableMarker)) {
            selectedBuses.add(busEdges);
          }
        } else {
          selectedBuses.add(busEdges);
        }

        hider.hideAll();
      }
    } finally {
      hider.unhideAll();
    }

    // store the bus color to set it to as ID of the new edges
    Map idToColor = new HashMap();
    for (Iterator listIter = selectedBuses.iterator(); listIter.hasNext();) {
      final EdgeList edgeList = (EdgeList) listIter.next();
      idToColor.put(new Integer(idToColor.size()), graph.getRealizer(edgeList.firstEdge()).getLineColor());
    }

    // second step, A: remove singleton hubs since they are not handled by BusRepresentations.replaceHubsBySubgraph(..)
    NodeList singletonHubs = new NodeList();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (isHub(node) && node.degree() == 0) {
        singletonHubs.add(node);
      }
    }
    for (NodeCursor nc = singletonHubs.nodes(); nc.ok(); nc.next()) {
      graph.removeNode(nc.node());
    }

    // second step, B: do the conversion of the edge structure
    final EdgeList[] selectedBusesArray = (EdgeList[]) selectedBuses.toArray(new EdgeList[selectedBuses.size()]);
    final EdgeList subGraphEdges;
    if (mode == MODE_PARTIAL) {
      subGraphEdges = BusRepresentations.replaceHubsBySubgraph(graph, selectedBusesArray, new HubMarkerDataProvider(),
          DataProviders.createNegatedDataProvider(movableMarker), busDescriptors);
    } else {
      subGraphEdges = BusRepresentations.replaceHubsBySubgraph(graph, selectedBusesArray, new HubMarkerDataProvider(),
          DataProviders.createConstantDataProvider(Boolean.FALSE), busDescriptors);
    }

    // third step: set the original bus color as ID of the new edges. This makes it easy to restore the color after the
    // layout and the creation of the new hubs.
    for (EdgeCursor ec = subGraphEdges.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final BusDescriptor descriptor = (BusDescriptor) busDescriptors.get(edge);
      final Color color = (Color) idToColor.get(descriptor.getID());
      descriptor.setID(color);
      graph.getRealizer(edge).setLineColor(color);
    }

    return subGraphEdges;
  }

  /**
   * Returns whether there is at least one moveable edge in the bus and marks all moveable edges in the provided edge
   * map. This is required only for MODE_SELECTED_PARTS.
   *
   * @param moveableMarker an edge map in which the movable edges are marked
   * @return <code>true</code> if the bus contains a movable edge
   * @throws IllegalArgumentException if a bus contains movable edges and its fixed subgraph is not a connected
   *                                  orthogonal tree
   */
  private boolean markMoveableEdges(EdgeMap moveableMarker) {
    final Graph2D graph = view.getGraph2D();
    boolean containsMoveable = false;

    // Iteratively, do a search starting from each selected regular node along paths of degree-2 hubs and hide the
    // discovered edges. These edges are moveable.
    GraphHider hider = new GraphHider(graph);
    for (EdgeCursor ec = new EdgeList(graph.edges()).edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!graph.contains(edge) || (isHub(edge.source()) && isHub(edge.target()))
          || !(graph.isSelected(edge) || (graph.isSelected(edge.source()) && !isHub(edge.source()))
          || (graph.isSelected(edge.target()) && !isHub(edge.target())))) {
        // start a search at a regular node either if it is selected by itself or if its edge is selected
        continue;
      }

      containsMoveable = true;
      moveableMarker.setBool(edge, true);
      Node node = isHub(edge.source()) ? edge.source() : edge.target();
      hider.hide(edge);

      while (node != null && node.degree() == 1) {
        final Edge chainEdge = node.inDegree() > 0 ? node.firstInEdge() : node.firstOutEdge();
        final Node opposite = chainEdge.opposite(node);

        if (isHub(opposite)) {
          moveableMarker.setBool(chainEdge, true);
          hider.hide(chainEdge);
          node = opposite;
        } else {
          node = null;
        }
      }
    }

    if (!containsMoveable) {
      hider.unhideAll();
      return false;
    }

    // Everything that is not hidden is fixed and should be a tree and orthogonal.
    // Find (multi-)edges to regular nodes and hide them for the tree check.
    EdgeList nodeLinks = new EdgeList();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!isHub(edge.source()) || !isHub(edge.target())) {
        nodeLinks.add(edge);
      }
    }
    hider.hide(nodeLinks);
    if (!Trees.isForest(graph)) {
      hider.unhideAll();
      throw new IllegalArgumentException("Fixed subgraph is not a connected tree.");
    }
    hider.unhideEdges(nodeLinks);

    if (!isOrthogonal(graph)) {
      hider.unhideAll();
      throw new IllegalArgumentException("Fixed subgraph is not orthogonal.");
    }

    hider.unhideAll();
    return true;
  }

  /**
   * Returns whether the specified node is a hub. A node is a hub if its realizer is the one of hubs.
   */
  private boolean isHub(final Node node) {
    final NodeRealizer realizer = view.getGraph2D().getRealizer(node);
    return realizer instanceof GenericNodeRealizer
        && BusRouterDemoTools.HUB_CONFIGURATION.equals(((GenericNodeRealizer) realizer).getConfiguration());
  }

  /**
   * Initialize this demo.
   */
  protected void initialize() {
    super.initialize();

    module = new BusRouterDemoModule();
    module.getLayoutExecutor().setLockingView(true);
    // the backup of the realizers is done before the placement of the hubs in method {@link # prepareForLayout}
    module.getLayoutExecutor().setBackupRealizersEnabled(false);

    demoTools = new BusRouterDemoTools(view, module.getBusRouter());

    undoManager = new Graph2DUndoManager(view.getGraph2D());
    undoManager.setViewContainer(view);

    // create support for colored buses
    busDyer = new BusDyer(view.getGraph2D(), new HubMarkerDataProvider());
    view.getGraph2D().addGraphListener(busDyer);

    setBridgeCalculatorEnabled(true);
  }

  /**
   * Creates the default toolbar and adds undo/redo and the routing actions.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.setFloatable(false);

    toolBar.addSeparator();

    //add undo action to toolbar
    Action undoAction = undoManager.getUndoAction();
    undoAction.putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/undo.png")));
    undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo");
    toolBar.add(undoAction);

    //add redo action to toolbar
    Action redoAction = undoManager.getRedoAction();
    redoAction.putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/redo.png")));
    redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo");
    toolBar.add(redoAction);

    toolBar.addSeparator();

    // add bus router to toolbar
    Action routeAllAction = new AbstractAction("Route All") {
      public void actionPerformed(ActionEvent e) {
        doLayout(MODE_ALL);
      }
    };
    routeAllAction.putValue(Action.SHORT_DESCRIPTION, "Route all buses");
    routeAllAction.putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/layout.png")));
    toolBar.add(DemoDefaults.createActionControl(routeAllAction, true));

    // add bus router to toolbar
    Action routeSelectedAction = new AbstractAction("Route Selected") {
      public void actionPerformed(ActionEvent e) {
        doLayout(MODE_SELECTED);
      }
    };
    routeSelectedAction.putValue(Action.SHORT_DESCRIPTION, "Route selected buses");
    routeSelectedAction.putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/layout.png")));
    toolBar.add(DemoDefaults.createActionControl(routeSelectedAction, true));

    // add settings for bus router to toolbar
    Action propertiesAction = new AbstractAction("Properties...") {
      public void actionPerformed(ActionEvent e) {
        final OptionHandler oh = module.getOptionHandler();
        oh.showEditor(view.getFrame());
      }
    };
    propertiesAction.putValue(Action.SHORT_DESCRIPTION, "Configure the bus router");
    propertiesAction.putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/properties.png")));
    toolBar.add(DemoDefaults.createActionControl(propertiesAction, true));

    return toolBar;
  }

  /**
   * Creates the default menu bar and adds an additional menu of examples graphs.
   */
  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = super.createMenuBar();
    JMenu menu = new JMenu("Sample Graphs");
    menuBar.add(menu);

    menu.add(new EmptyGraphAction("Empty Graph"));

    menu.add(new AbstractAction("One Bus") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/oneBus.graphml");
      }
    });
    menu.add(new AbstractAction("Three Buses") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/threeBuses.graphml");
      }
    });

    return menuBar;
  }

  /**
   * Creates modified edit modes for this demo. After each node movement, the connections of the affected nodes to their
   * buses are rerouted while the other parts remain fixed. If the source of an edge creation is a hub, the edge is
   * colored in the bus' color. If a new edge is a singleton bus, that is it connects two regular nodes, it is instantly
   * routed.
   */
  protected EditMode createEditMode() {
    EditMode editMode = super.createEditMode();
    editMode.setMoveSelectionMode(new MoveSelectionMode(view) {
      protected void selectionMovedAction(double dx, double dy, double x, double y) {
        super.selectionMovedAction(dx, dy, x, y);
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            if (demoTools.isAutomaticRoutingEnabled()) {
              doLayout(MODE_PARTIAL);
            }
          }
        });
      }
    });

    editMode.setCreateEdgeMode(new CreateEdgeMode() {
      private Color color;

      protected boolean acceptSourceNode(Node source, double x, double y) {
        color = isHub(source) ? view.getGraph2D().getRealizer(source).getFillColor() : Color.BLACK;
        return super.acceptSourceNode(source, x, y);
      }

      protected void edgeCreated(final Edge edge) {
        super.edgeCreated(edge);
        if (demoTools.isAutomaticRoutingEnabled()
            && !isHub(edge.source()) && !isHub(edge.target())) {
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              getGraph2D().unselectAll();
              getGraph2D().setSelected(edge, true);
              doLayout(MODE_SELECTED);
            }
          });
        }
      }

      protected EdgeRealizer getDummyEdgeRealizer() {
        final EdgeRealizer realizer = super.getDummyEdgeRealizer();
        if (color != null) {
          realizer.setLineColor(color);
        }
        return realizer;
      }
    });

    // copied from DemoBase
    ((CreateEdgeMode) editMode.getCreateEdgeMode()).setIndicatingTargetNode(true);
    return editMode;
  }

  /**
   * Specifies whether bridges are shown and configures a {@link BridgeCalculator} accordingly.
   */
  private void setBridgeCalculatorEnabled(boolean enable) {
    if (enable) {
      // create the BridgeCalculator
      BridgeCalculator bridgeCalculator = new BridgeCalculator();
      ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);
      bridgeCalculator.setCrossingMode(BridgeCalculator.CROSSING_MODE_ORDER_INDUCED);
      bridgeCalculator.setCrossingStyle(BridgeCalculator.CROSSING_STYLE_ARC);
      bridgeCalculator.setOrientationStyle(BridgeCalculator.ORIENTATION_STYLE_UP);
    } else {
      ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(null);
    }
  }

  /**
   * Call-back for loading a graph. Overwritten to reset the undo queue.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);
    undoManager.resetQueue();
  }

  /**
   * A data provider which specifies for each node whether it is a hub or not.
   */
  private class HubMarkerDataProvider extends DataProviderAdapter {

    public boolean getBool(Object dataHolder) {
      return dataHolder instanceof Node && isHub((Node) dataHolder);
    }
  }

  /**
   * Clears the graph and its undo queue, and sets the view to the initial zoom factor.
   */
  private class EmptyGraphAction extends AbstractAction {

    private EmptyGraphAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().clear();
      view.getGraph2D().setURL(null);
      view.fitContent();
      view.updateView();
      undoManager.resetQueue();
    }
  }

  /**
   * Checks whether all edge paths of a graph are orthogonal.
   *
   * @param graph the graph
   * @return <code>true</code> if all paths are orthogonal.
   */
  static boolean isOrthogonal(final LayoutGraph graph) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final YPoint[] path = graph.getPath(ec.edge()).toArray();
      for (int i = 1; i < path.length; i++) {
        final YPoint p1 = path[i-1];
        final YPoint p2 = path[i];
        if (Math.abs(p1.x - p2.x) > 1.0e-5 && Math.abs(p1.y - p2.y) > 1.0e-5) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Runs this demo.
   *
   * @param args unused
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(new Locale("en")); // suppress German text in router option handler 
        initLnF();
        (new BusRouterDemo("resource/busrouterhelp.html")).start();
      }
    });
  }
}
