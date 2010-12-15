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

import demo.view.hierarchy.GroupingDemo;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.net.URL;
import java.io.IOException;

import y.layout.Layouter;
import y.layout.partial.PartialLayouter;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.NodeRealizer;
import y.view.Graph2DViewActions;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.EdgeRealizer;
import y.view.ProxyShapeNodeRealizer;
import y.view.Selections;
import y.view.ViewMode;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.HierarchyManager;
import y.base.Node;
import y.base.NodeCursor;
import y.base.EdgeCursor;
import y.base.Edge;
import y.base.DataProvider;
import y.option.TableEditorFactory;
import y.option.OptionHandler;
import y.option.Editor;

import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;


/**
 * Base class for {@link y.layout.partial.PartialLayouter} demos.
 */
public abstract class PartialLayoutBase extends GroupingDemo {
  //Define colors for fixed/partial nodes/edges.
  protected static final Color COLOR_FIXED_NODE = Color.GRAY;
  protected static final Color COLOR_PARTIAL_NODE = new Color(255, 153, 0);
  protected static final Color COLOR_FIXED_EDGE = Color.BLACK;
  protected static final Color COLOR_PARTIAL_EDGE = COLOR_PARTIAL_NODE;
  protected final OptionHandler optionHandler;
  /**
   * Initializes a new <code>PartialLayoutBase</code> instance.
   * @param helpFilePath the path to the help document to be displayed or
   * <code>null</code> if no help document should be displayed.
   */
  protected PartialLayoutBase( final String helpFilePath ) {
    super();

    JComponent helpPane = null;
    if (helpFilePath != null) {
      final URL url = getClass().getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        helpPane = createHelpPane(url);
      }
    }

    //Init GUI components:
    final JPanel propertiesPanel = new JPanel(new BorderLayout());
    optionHandler = createOptionHandler();
    propertiesPanel.add(createOptionTable(optionHandler), BorderLayout.NORTH);
    if (helpPane != null) {
      helpPane.setPreferredSize(new Dimension(400, 400));
      propertiesPanel.add(helpPane);
    }

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view, propertiesPanel);
    splitPane.setResizeWeight(0.95);
    splitPane.setContinuousLayout(false);
    contentPane.add(splitPane, BorderLayout.CENTER);
  }


  /**
   * Adds the following actions to the default tool bar:
   * <ul>
   * <li>LayoutAction - layouts the graph</li>
   * <li>FixColorSelectionAction - determines fix nodes</li>
   * <li>PartialColorSelectionAction - determines partial nodes</li>
   * </ul>
   */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();

    bar.addSeparator();
    bar.add(new ColorSelectionAction("Lock Selection", true, view));
    bar.add(new ColorSelectionAction("Unlock Selection", false, view));

    bar.addSeparator();
    final AbstractAction reloadAction = new AbstractAction("Reload") {
      public void actionPerformed(ActionEvent e) {
        loadInitialGraph();
      }
    };
    reloadAction.putValue(Action.SHORT_DESCRIPTION, "Reload the Initial Graph");
    bar.add(reloadAction);

    bar.addSeparator();
    final LayoutAction layoutAction = new LayoutAction("Layout");
    layoutAction.putValue(Action.SHORT_DESCRIPTION, "Partial Layout");
    bar.add(layoutAction);

    return bar;
  }

  /**
   * Adds a view mode which allows to toggle the fixed/partial state of nodes
   * or edges (on mouse double click).
   */
  protected void registerViewModes() {
    super.registerViewModes();

    view.addViewMode(new ToggleColorMode());
  }
  
  protected void configureDefaultGroupNodeRealizers() {
    //Create additional configuration for default group node realizers
    DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory) getHierarchyManager().getGraphFactory();

    Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    factory.addConfiguration(CONFIGURATION_GROUP, map);
    Object abf = factory.getImplementation(CONFIGURATION_GROUP,
        GenericGroupNodeRealizer.GenericAutoBoundsFeature.class);
    if (abf instanceof DefaultGenericAutoBoundsFeature) {
      ((DefaultGenericAutoBoundsFeature) abf).setConsiderNodeLabelSize(true);
    }

    GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();

    //Register first, since this will also configure the node label
    gnr.setConfiguration(CONFIGURATION_GROUP);

    //Nicer colors
    gnr.setFillColor(new Color(202, 236, 255, 132));
    gnr.setLineColor(new Color(102, 102, 153, 255));
    gnr.setLineType(LineType.DOTTED_1);
    NodeLabel label = gnr.getLabel();
    label.setBackgroundColor(COLOR_PARTIAL_NODE);
    label.setTextColor(Color.BLACK);
    label.setFontSize(15);

    hgf.setProxyNodeRealizerEnabled(true);

    hgf.setDefaultGroupNodeRealizer(gnr.createCopy());
    hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
  }


  /**
   * Sets the background color of the specified realizer's default label.
   * If the specified realizer is a proxy realizer, the default labels of
   * the proxy's possible delegates are changed recursively.
   * @param nr the realizer whose default label is changed.
   * @param color the new background color for the default label.
   */
  private static void setLabelBackgroundColor( final NodeRealizer nr, final Color color ) {
    if (nr instanceof ProxyShapeNodeRealizer) {
      final ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
      for (int i = 0, n = pnr.realizerCount(); i < n; ++i) {
        setLabelBackgroundColor(pnr.getRealizer(i), color);
      }
    } else {
      nr.getLabel().setBackgroundColor(color);
    }
  }

  /** Creates a table editor component for the specified option handler. */
  private JComponent createOptionTable(OptionHandler oh) {
    oh.setAttribute(TableEditorFactory.ATTRIBUTE_INFO_POSITION, TableEditorFactory.InfoPosition.NONE);

    TableEditorFactory tef = new TableEditorFactory();
    Editor editor = tef.createEditor(oh);
  
    JComponent optionComponent = editor.getComponent();
    optionComponent.setPreferredSize(new Dimension(400, 200));
    optionComponent.setMaximumSize(new Dimension(400, 200));
    return optionComponent;
  }

  /**
   * Creates an option handler to manage layout properties.
   * @return an option handler for the layout.
   */
  protected abstract OptionHandler createOptionHandler();

  /**
   * Callback method to partially layout a graph.
   * This method is called from
   * {@link demo.layout.partial.PartialLayoutBase.LayoutAction} and
   * {@link demo.layout.partial.PartialLayoutBase.DuplicateSubgraphAction}.
   *
   */
  protected void layoutSubgraph() {
    Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setKeepZoomFactor(true);
    layoutExecutor.doLayout(view, createConfiguredPartialLayouter());
  }

    /**
   * Creates a configured partial Layouter instance
   * @return an partial layouter instance.
   */
  protected abstract Layouter createConfiguredPartialLayouter();

  /**
   * Registers key bindings for both predefined actions and custom actions.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put("DUPLICATE_ACTION", new DuplicateSubgraphAction("Duplicate and Layout Subgraph"));

    InputMap inputMap = view.getCanvasComponent().getInputMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), "DUPLICATE_ACTION");

  }

  /**
   * Triggers a layout calculation for the subgraph that is induced by
   * elements marked as partial.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      //Select subgraph by color:
      PartialElementsMarkers ps = new PartialElementsMarkers();
      boolean selectionExists = ps.markByColor(view.getGraph2D());
      if (selectionExists) {
        layoutSubgraph();
      }
      ps.resetMarkers(view.getGraph2D());
    }
  }

  /**
   * Action that duplicates the subgraph that is induced by the currently
   * selected nodes or edges.
   */
  class DuplicateSubgraphAction extends AbstractAction {
    DuplicateSubgraphAction(String name) {
      super(name);
    }

    /**
     * Duplicates the subgraph that is induced by the currently selected nodes or
     * edges.
     */
    public void actionPerformed(ActionEvent e) {
      //Duplicate selection
      final Graph2DViewActions.DuplicateAction duplicateAction = new Graph2DViewActions.DuplicateAction(view);
      duplicateAction.duplicate(view);

      //Layout the duplicated nodes:
      PartialElementsMarkers ps = new PartialElementsMarkers();
      final boolean selectionExists = ps.markBySelection(view.getGraph2D());
      if (selectionExists) {
        layoutSubgraph();
      }
      ps.resetMarkers(view.getGraph2D());
    }
  }

  /**
   * Provides methods to mark nodes and/or edges as <em>partial</em>
   * (with regards to {@link PartialLayouter#PARTIAL_NODES_DPKEY} and
   * {@link PartialLayouter#PARTIAL_EDGES_DPKEY} according
   * to either their color or their selection state (with regards to
   * {@link y.view.Graph2D#isSelected(y.base.Edge)} and
   * {@link y.view.Graph2D#isSelected(y.base.Node)}).
   */
  static class PartialElementsMarkers {
    DataProvider odpSelectedNodes;
    DataProvider odpSelectedEdges;

    /**
     * Adds data providers to the given graph for keys
     * {@link PartialLayouter#PARTIAL_NODES_DPKEY} and {@link PartialLayouter#PARTIAL_EDGES_DPKEY}
     * that reflect the selection state (with regards to
     * {@link y.view.Graph2D#isSelected(y.base.Edge)} and
     * {@link y.view.Graph2D#isSelected(y.base.Node)}) of nodes and edges.
     * @param graph the graph for which selection markers are created.
     * @return <code>true</code> if the specified graph has selected nodes
     * and/or selected edges and <code>false</code> otherwise.
     */
    boolean markBySelection(final Graph2D graph) {
      //store the old data provider
      odpSelectedNodes = graph.getDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
      odpSelectedEdges = graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);

      //register dp
      graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, Selections.createSelectionNodeMap(graph));
      graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, Selections.createSelectionEdgeMap(graph));

      return (graph.selectedNodes().ok() || graph.selectedEdges().ok());
    }

    /**
     * Adds data providers to the given graph for keys
     * {@link PartialLayouter#PARTIAL_NODES_DPKEY} and {@link PartialLayouter#PARTIAL_EDGES_DPKEY}
     * that report an edge as <em>partial</em> if its state color equals
     * the specified partial edge color and a node if its state color equals
     * the specified partial node color.
     * @param graph the graph for which selection markers are created.
     * @return <code>true</code> if the specified graph has nodes and/or edges
     * with the appropriate state colors and <code>false</code> otherwise.
     */
    boolean markByColor(final Graph2D graph) {
      //store the old data provider
      odpSelectedNodes = graph.getDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
      odpSelectedEdges = graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
        
      //Determine partial nodes/edges by color:
      DataProviderAdapter isPartialNode = new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          Node n = (Node) dataHolder;
          if (graph.getHierarchyManager().isGroupNode(n) ||
              graph.getHierarchyManager().isFolderNode(n)) {
            return COLOR_PARTIAL_NODE.equals(graph.getRealizer(n).getLabel().getBackgroundColor());
          } else {
            return COLOR_PARTIAL_NODE.equals(graph.getRealizer(n).getFillColor());
          }
        }
      };

      DataProviderAdapter isPartialEdge = new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return COLOR_PARTIAL_EDGE.equals(graph.getRealizer((Edge) dataHolder).getLineColor());
        }
      };

      graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, isPartialNode);
      graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, isPartialEdge);

      for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        if (isPartialNode.getBool(node)){
          return true;
        }
      }
      for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
        Edge edge = edgeCursor.edge();
        if (isPartialEdge.getBool(edge)){
          return true;
        }
      }

      return false;
    }
    
    void resetMarkers(Graph2D graph) {
      //reset data provider
      graph.removeDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
      if (odpSelectedNodes != null) {
        //set the old data provider
        graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, odpSelectedNodes);
      }

      graph.removeDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
      if (odpSelectedEdges != null) {
        //set the old data provider
        graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, odpSelectedEdges);
      }
    }
  }

  /**
   * {@link y.view.ViewMode} that toggles that fixed/partial state of a node
   * or an edge by coloring the element in the appropriate state color.
   * @see demo.layout.partial.PartialLayoutBase#COLOR_FIXED_EDGE
   * @see demo.layout.partial.PartialLayoutBase#COLOR_FIXED_NODE
   * @see demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_EDGE
   * @see demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_NODE
   */
  static class ToggleColorMode extends ViewMode {
    /**
     * Toggles the fixed/partial state color of double-clicked nodes or edges.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseClicked( final double x, final double y ) {
      if (lastClickEvent.getButton() == MouseEvent.BUTTON1 &&
          lastClickEvent.getClickCount() == 2) {
        mouseClickedImpl(x, y);
      }
    }

    private void mouseClickedImpl( final double x, final double y ) {
      final HitInfo info = view.getHitInfoFactory().createHitInfo(
              x, y, Graph2DTraversal.NODES | Graph2DTraversal.EDGES, true);
      if (info.hasHitNodes()) {
        toggleColor(info.getHitNode());
      } else if (info.hasHitEdges()) {
        toggleColor(info.getHitEdge());
      }
    }

    /**
     * Toggles the fixed/partial state color of the specified edge.
     * @param edge the edge whose state color is changed.
     */
    private void toggleColor( final Edge edge ) {
      final EdgeRealizer er = view.getGraph2D().getRealizer(edge);
      if (er.getLineColor() == COLOR_PARTIAL_EDGE) {
        er.setLineColor(COLOR_FIXED_EDGE);
      } else {
        er.setLineColor(COLOR_PARTIAL_EDGE);
      }
      view.getGraph2D().updateViews();
    }

    /**
     * Toggles the fixed/partial state color of the specified node.
     * @param node the node whose state color is changed.
     */
    private void toggleColor( final Node node ) {
      final Graph2D g = view.getGraph2D();
      final NodeRealizer nr = g.getRealizer(node);
      final HierarchyManager hierarchyManager = g.getHierarchyManager();
      if (hierarchyManager.isGroupNode(node) ||
          hierarchyManager.isFolderNode(node)) {            
        if (nr.getLabel().getBackgroundColor() == COLOR_PARTIAL_NODE) {
          setLabelBackgroundColor(nr, COLOR_FIXED_NODE);
        } else {
          setLabelBackgroundColor(nr, COLOR_PARTIAL_NODE);
        }
      } else {
        if (nr.getFillColor() == COLOR_PARTIAL_NODE) {
          nr.setFillColor(COLOR_FIXED_NODE);
        } else {
          nr.setFillColor(COLOR_PARTIAL_NODE);
        }
      }
      g.updateViews();
    }
  }

  /**
   * Marks selected nodes and/or edges as either fixed or partial.
   * Marking is done by coloring said elements using one of the appropriate
   * state colors {@link demo.layout.partial.PartialLayoutBase#COLOR_FIXED_EDGE},
   * {@link demo.layout.partial.PartialLayoutBase#COLOR_FIXED_NODE},
   * {@link demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_EDGE}, and
   * {@link demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_NODE}
   */
  static class ColorSelectionAction extends AbstractAction {
    private final boolean fixed;
    private final Graph2DView view;

    /**
     * Initializes a new <code>ColorSelectionAction</code> instance.
     * @param name the display name for a control triggering the action.
     * @param fixed if <code>true</code> edges are colored using
     * {@link demo.layout.partial.PartialLayoutBase#COLOR_FIXED_EDGE} and
     * nodes are colored using
     * {@link demo.layout.partial.PartialLayoutBase#COLOR_FIXED_NODE}; if
     * <code>false</code> edges are colored using
     * {@link demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_EDGE} and
     * nodes are colored using
     * {@link demo.layout.partial.PartialLayoutBase#COLOR_PARTIAL_NODE}.
     * @param view the view displaying the graph whose selected elements are
     * to be colored.
     */
    ColorSelectionAction(
            final String name,
            final boolean fixed,
            final Graph2DView view
    ) {
      super(name);
      this.fixed = fixed;
      this.view = view;
    }

    public void actionPerformed( final ActionEvent e ) {
      final Graph2D g = view.getGraph2D();

      final Color nodeColor = fixed ? COLOR_FIXED_NODE : COLOR_PARTIAL_NODE;
      for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        if (g.isSelected(n)) {
          if (g.getHierarchyManager().isGroupNode(n) ||
              g.getHierarchyManager().isFolderNode(n)) {
            setLabelBackgroundColor(g.getRealizer(n), nodeColor);
          } else {
            g.getRealizer(n).setFillColor(nodeColor);
          }
        }
      }

      final Color edgeColor = fixed ? COLOR_FIXED_EDGE : COLOR_PARTIAL_EDGE;
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (g.isSelected(edge)) {
          g.getRealizer(edge).setLineColor(edgeColor);
        }
      }

      g.updateViews();
    }
  }
}