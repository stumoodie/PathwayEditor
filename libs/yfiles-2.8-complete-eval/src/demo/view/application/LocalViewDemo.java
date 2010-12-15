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
package demo.view.application;

import demo.view.DemoBase;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.YCursor;
import y.geom.YPoint;
import y.layout.Layouter;
import y.layout.LayoutOrientation;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.util.GraphCopier;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.LocalViewCreator.*;
import y.view.ViewMode;
import y.view.LocalViewCreator;
import y.view.ModelViewManager;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.Action;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Demonstrates local views, a feature that uses a given (model) graph to
 * create a (hopefully smaller) graph which emphasizes a certain aspect of
 * the original graph. There are several predefined policies to create local
 * views:
 * <ul>
 * <li>Neighborhood: For a given set of nodes, display all nodes that are
 * reachable by a edge path up to a certain length.</li>
 * <li>Common Parent Group: For a given set of nodes, display all nodes that
 * share the same parent group node as one of the given nodes.</li>
 * <li>AncestorGroups: For given set of nodes, display all ancestor group nodes.
 * </li>
 * <li>Folder Contents: For a given set of folder nodes, display the folders'
 * inner graphs.</li>
 * <li>Source and Target: For a given set of edges, display the source and
 * target nodes.</li>
 * <li>Edge Group: For a given set of edges, display all edges that share
 * source and/or target with one of the original edges.</li>
 * </ul>
 *
 */
public class LocalViewDemo extends DemoBase {
  /**
   * The delay in milliseconds before a local view update
   * is performed after a trigger event.
   */
  private static final int TIMER_DELAY = 100;

  /**
   * Trigger type constant that represents the selection trigger.
   * @see demo.view.application.LocalViewDemo.SelectionTrigger
   */
  private static final byte SELECTION_TRIGGER = 1;
  /**
   * Trigger type constant that represents the hover trigger.
   * @see demo.view.application.LocalViewDemo.HoverTrigger
   */
  private static final byte HOVER_TRIGGER = 2;


  private final Graph2DView localView;

  // node related local view creators
  private final Neighborhood neighborhood;
  private final CommonParentGroup commonParentGroup;
  private final AncestorGroups ancestorGroups;
  private final FolderContents folderContents;

  // edge related local view creators
  private final SourceAndTarget sourceAndTarget;
  private final EdgeGroup edgeGroup;

  // custom local view creator
  private final SelectedSubgraph selectedSubgraph;

  // the local view creator responsible for the current local view
  private LocalViewCreator currentLocalViewCreator;
  // the selected local view creator for node related local view
  private AbstractLocalViewCreator nodeLocalViewCreator;
  // the selected local view creator for edge related local view
  private AbstractLocalViewCreator edgeLocalViewCreator;


  // reusable instance of selection trigger, i.e. a Graph2DSelectionListener
  // that triggers local view updates on selection events
  private final SelectionTrigger selectionTrigger;
  // reusable instance of hover trigger, i.e. a ViewMode
  // that triggers local view updates when the mouse hovers over graph elements
  private final HoverTrigger hoverTrigger;

  // used to keep track of the current trigger for local view updates
  private byte triggerType;


  public LocalViewDemo() {
    this(null);
  }

  public LocalViewDemo( final String helpFilePath ) {
    final Graph2D graph = view.getGraph2D();
    (new HierarchyManager(graph)).addHierarchyListener(
            new GroupNodeRealizer.StateChangeListener());


    // instantiate several local view creators that share the current view's
    // graph as model as well as the graph used as local view
    // the factory of the current view's graph is used to actually populate
    // the local view with elements
    final GraphCopier.CopyFactory factory = graph.getGraphCopyFactory();
    final Graph2D localViewGraph = (Graph2D) factory.createGraph();

    localView = new Graph2DView(localViewGraph);
    localView.setPreferredSize(new Dimension(320, 240));

    final Layouter layouter = createLayouter();
    neighborhood = new Neighborhood(graph, factory, localViewGraph);
    neighborhood.setLayouter(layouter);
    commonParentGroup = new CommonParentGroup(graph, factory, localViewGraph);
    ancestorGroups = new AncestorGroups(graph, factory, localViewGraph);
    folderContents = new FolderContents(graph, factory, localViewGraph);

    sourceAndTarget = new SourceAndTarget(graph, factory, localViewGraph);
    sourceAndTarget.setLayouter(layouter);
    edgeGroup = new EdgeGroup(graph, factory, localViewGraph);
    edgeGroup.setLayouter(layouter);

    selectedSubgraph = new SelectedSubgraph(graph, factory, localViewGraph);


    setEdgeLocalViewCreator(sourceAndTarget);
    setNodeLocalViewCreator(neighborhood);


    // load a sample graph
    loadGraph("resource/LocalViewDemo.graphml");


    // add a ViewMode to the localView that upon double-clicking on items
    // will select and focus the corresponding item in the original view
    localView.addViewMode(new ViewMode() {
      public void mouseClicked(double x, double y) {
        // double click?
        if (lastClickEvent.getClickCount() == 2) {
          final HitInfo hitInfo = getHitInfo(x, y);
          final Graph2DView view = LocalViewDemo.this.view;
          final Graph2D viewGraph = view.getGraph2D();
          // did we click on a node?
          if (hitInfo.getHitNode() != null) {
            // find the original node in the "model"
            final LocalViewCreator lvc = getCurrentLocalViewCreator();
            final Node modelNode = lvc.getModelNode(hitInfo.getHitNode());
            if (modelNode != null) {
              // we found one, so select and focus it
              viewGraph.unselectAll();
              final NodeRealizer nr = viewGraph.getRealizer(modelNode);
              Point2D.Double center = new Point2D.Double(nr.getCenterX(), nr.getCenterY());
              double minZoom = Math.min(view.getWidth() / (nr.getWidth() + 40),
                  view.getHeight() / (nr.getHeight() + 40));
              view.focusView(Math.min(minZoom, view.getZoom()), center, true);
              viewGraph.setSelected(modelNode, true);
              viewGraph.updateViews();
            }
          } else if (hitInfo.getHitEdge() != null) { // we clicked on an edge
            // find the original one
            final LocalViewCreator lvc = getCurrentLocalViewCreator();
            final Edge modelEdge = lvc.getModelEdge(hitInfo.getHitEdge());
            if (modelEdge != null) {
              // found one - so select and focus source and target nodes
              final NodeRealizer snr = viewGraph.getRealizer(modelEdge.source());
              final NodeRealizer tnr = viewGraph.getRealizer(modelEdge.target());
              Point2D.Double sCenter = new Point2D.Double(snr.getCenterX(), snr.getCenterY());
              Point2D.Double tCenter = new Point2D.Double(tnr.getCenterX(), tnr.getCenterY());
              double minZoom =
                  Math.min(
                      view.getWidth()/(snr.getWidth() + tnr.getWidth() + Math.abs(sCenter.x - tCenter.x)),
                      view.getHeight() / (snr.getHeight() + tnr.getHeight() + Math.abs(sCenter.y - tCenter.y)));
              view.focusView(Math.min(view.getZoom(), minZoom), new Point2D.Double((sCenter.x + tCenter.x) * 0.5d, (sCenter.y + tCenter.y) * 0.5d), true);
              viewGraph.unselectAll();
              viewGraph.setSelected(modelEdge, true);
              viewGraph.updateViews();
            }
          }
        }
      }
    });

    localView.setFitContentOnResize(true);
    
    selectionTrigger = new SelectionTrigger();
    hoverTrigger = new HoverTrigger();

    final JSplitPane localViewAndSettings = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, localView, createSettingsComponent());
    localViewAndSettings.setResizeWeight(1);
    final JSplitPane center = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, localViewAndSettings, view);
    center.setResizeWeight(0);
    
    contentPane.add(center, BorderLayout.CENTER);
    addHelpPane(helpFilePath);

    // set the initial trigger for local view updates
    setTrigger(SELECTION_TRIGGER);


    // set an initial selection
    if (!graph.isEmpty()) {
      graph.unselectAll();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        if (nc.node().inDegree() > 0 && nc.node().outDegree() > 0) {
          graph.setSelected(nc.node(), true);
          break;
        }
      }
      if (graph.isSelectionEmpty()) {
        graph.setSelected(graph.firstNode(), true);
      }
      graph.updateViews();
    }
  }

  /**
   * Overwritten to disable node and edge creation.
   */
  protected EditMode createEditMode() {
     EditMode editMode = super.createEditMode();
     editMode.allowEdgeCreation(false);
     editMode.allowNodeCreation(false);
     return editMode;
  }

  /**
   * Overwritten to disable saving graphs.
   */
  protected Action createSaveAction() {
    return null;
  }

  /**
   * Overwritten to clear local view upon loading.
   */
  protected void loadGraph( final URL resource ) {
    if (resource != null && localView != null) {
      localView.getGraph2D().clear();
    }

    super.loadGraph(resource);

    if (resource != null && localView != null) {
      localView.updateView();
    }
  }

  /**
   * Sets the trigger for local view updates represented by the specified type
   * constant.
   * @param triggerType either {@link #SELECTION_TRIGGER} representing
   * {@link #selectionTrigger} or {@link #HOVER_TRIGGER} representing
   * {@link #hoverTrigger}.
   * @see demo.view.application.LocalViewDemo.HoverTrigger
   * @see demo.view.application.LocalViewDemo.SelectionTrigger
   */
  private void setTrigger( final byte triggerType ) {
    // remove the old trigger
    switch (this.triggerType) {
      case SELECTION_TRIGGER:
        view.getGraph2D().removeGraph2DSelectionListener(selectionTrigger);
        break;
      case HOVER_TRIGGER:
        view.removeViewMode(hoverTrigger);
        break;
    }

    this.triggerType = triggerType;

    // add the new trigger
    switch (this.triggerType) {
      case SELECTION_TRIGGER:
        view.getGraph2D().addGraph2DSelectionListener(selectionTrigger);
        break;
      case HOVER_TRIGGER:
        view.addViewMode(hoverTrigger);
        break;
    }
  }


  /**
   * Specifies the local view creator for edge related local views.
   * @param elvc   the new edge related local view creator.
   */
  private void setEdgeLocalViewCreator( final AbstractLocalViewCreator elvc ) {
    final boolean update = currentLocalViewCreator == edgeLocalViewCreator;

    edgeLocalViewCreator = elvc;

    // update the demo's local view if the last view creator was edge related
    updateEdgeViewImpl(update);
  }

  /**
   * Refreshes the demo's local view if the current local view creator is
   * node related.
   */
  private void updateEdgeView() {
    updateEdgeViewImpl(currentLocalViewCreator == edgeLocalViewCreator);
  }

  private void updateEdgeViewImpl( final boolean update ) {
    if (update) {
      if (SELECTION_TRIGGER == triggerType) {
        createEdgeView(view.getGraph2D().selectedEdges());
      } else {
        createEdgeView(new EdgeList().edges());
      }
    }
  }

  /**
   * Specifies the local view creator for node related local views.
   * @param nlvc   the new node related local view creator.
   */
  private void setNodeLocalViewCreator( final AbstractLocalViewCreator nlvc ) {
    final boolean update = currentLocalViewCreator == nodeLocalViewCreator;

    nodeLocalViewCreator = nlvc;

    // update the demo's local view if the last view creator was node related
    updateNodeViewImpl(update);
  }

  /**
   * Refreshes the demo's local view if the current local view creator is
   * edge related.
   */
  private void updateNodeView() {
    updateNodeViewImpl(currentLocalViewCreator == nodeLocalViewCreator);
  }

  private void updateNodeViewImpl( final boolean update ) {
    if (update) {
      if (SELECTION_TRIGGER == triggerType) {
        createNodeView(view.getGraph2D().selectedNodes());
      } else {
        createNodeView((new NodeList().nodes()));
      }
    }
  }

  /**
   * Updates the demo's local view using the specified edges and the currently
   * selected edge related local view creator.
   * @param edges   a cursor over a collection of edges.
   */
  private void createEdgeView( final YCursor edges ) {
    // mark the selected local view creator for edges as the currently active
    // local view creator
    currentLocalViewCreator = edgeLocalViewCreator;

    // replaced the local view creator's focus elements with the passed in
    // edges
    edgeLocalViewCreator.clearFocusEdges();
    for (; edges.ok(); edges.next()) {
      edgeLocalViewCreator.addFocusEdge((Edge) edges.current());
    }

    // update the demo's local view
    // the create call will ...
    //    ... clear the creator's associated view graph
    //    ... creates new elements in the creator's associated view graph
    //    ... lays out the creator's associated view graph
    //    ... and finally calls updateView for all Views associated to
    //        the creator's associated view graph (and fitContent for all of
    //        these that are of type Graph2DView)
    edgeLocalViewCreator.updateViewGraph();
  }

  /**
   * Updates the demo's local view using the specified nodes and the currently
   * selected node related local view creator.
   * @param nodes   a cursor over a collection of nodes.
   */
  private void createNodeView( final YCursor nodes ) {
    // mark the selected local view creator for nodes as the currently active
    // local view creator
    currentLocalViewCreator = nodeLocalViewCreator;

    // replaced the local view creator's focus elements with the passed in
    // nodes
    nodeLocalViewCreator.clearFocusNodes();
    for (; nodes.ok(); nodes.next()) {
      nodeLocalViewCreator.addFocusNode((Node) nodes.current());
    }

    // update the demo's local view
    // the create call will ...
    //    ... clear the creator's associated view graph
    //    ... creates new elements in the creator's associated view graph
    //    ... lays out the creator's associated view graph
    //    ... and finally calls updateView for all Views associated to
    //        the creator's associated view graph (and fitContent for all of
    //        these that are of type Graph2DView)
    nodeLocalViewCreator.updateViewGraph();
  }

  /**
   * Returns the local view creator responsible for the current local view.
   * @return the local view creator responsible for the current local view.
   */
  private LocalViewCreator getCurrentLocalViewCreator() {
    return currentLocalViewCreator;
  }

  /**
   * Creates user interface controls for the various settings of the available
   * local view creators.
   * @return user interface controls for the various settings of the available
   * local view creators.
   */
  private JComponent createSettingsComponent() {
    final JPanel settingsPane = new JPanel(new CardLayout());

    final Box nodeStrategyButtons = Box.createVerticalBox();
    nodeStrategyButtons.setBorder(BorderFactory.createTitledBorder("Node Strategies"));

    final ButtonGroup nodeStrategiesGroup = new ButtonGroup();

    // controls for selecting the node related view creators
    addStrategy(settingsPane, nodeStrategyButtons, nodeStrategiesGroup, "Neighborhood", createNeighborhoodSettings(), neighborhood, true);
    addStrategy(settingsPane, nodeStrategyButtons, nodeStrategiesGroup, "Common Parent Group", createCommonParentGroupSettings(), commonParentGroup, true);
    addStrategy(settingsPane, nodeStrategyButtons, nodeStrategiesGroup, "Ancestor Groups", createAncestorGroupsSettings(), ancestorGroups, true);
    addStrategy(settingsPane, nodeStrategyButtons, nodeStrategiesGroup, "Folder Contents", createFolderContentsSettings(), folderContents, true);
    addStrategy(settingsPane, nodeStrategyButtons, nodeStrategiesGroup, "Selected Subgraph", null, selectedSubgraph, true);


    final ButtonGroup edgeStrategiesGroup = new ButtonGroup();
    final Box edgeStrategyButtons = Box.createVerticalBox();
    edgeStrategyButtons.setBorder(BorderFactory.createTitledBorder("Edge Strategies"));

    // controls for selecting the edge related view creators
    addStrategy(settingsPane, edgeStrategyButtons, edgeStrategiesGroup, "Source and Target", null, sourceAndTarget, false);
    addStrategy(settingsPane, edgeStrategyButtons, edgeStrategiesGroup, "Edge Group", createEdgeGroupSettings(), edgeGroup, false);


    Box container = Box.createVerticalBox();
    container.add(createTriggerPane());

    // put all the controls together and ensure nice resizing behavior
    final GridBagConstraints gbc = new GridBagConstraints();
    final JPanel strategies = new JPanel(new GridBagLayout());
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.weightx = 1;
    gbc.weighty = 0;
    strategies.add(nodeStrategyButtons, gbc);
    strategies.add(edgeStrategyButtons, gbc);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    strategies.add(Box.createGlue(), gbc);

    Box strategy = Box.createHorizontalBox();
    strategy.add(strategies);
    strategy.add(settingsPane);

    container.add(strategy);
    return container;
  }

  /**
   * Creates user controls for the {@link y.view.LocalViewCreator.EdgeGroup}
   * local view creator.
   * @return user controls for the {@link y.view.LocalViewCreator.EdgeGroup}
   * local view creator.
   */
  private JComponent createEdgeGroupSettings() {
    final JCheckBox src = new JCheckBox("Source");
    src.setSelected(true);
    final JCheckBox tgt = new JCheckBox("Target");
    tgt.setSelected(true);
    final AbstractAction groupTypes = new AbstractAction() {
      public void actionPerformed( final ActionEvent e ) {
        src.setEnabled(tgt.isSelected());
        tgt.setEnabled(src.isSelected());
        byte types = 0;
        if (src.isSelected()) {
          types |= EdgeGroup.GROUP_BY_SOURCE;
        }
        if (tgt.isSelected()) {
          types |= EdgeGroup.GROUP_BY_TARGET;
        }
        edgeGroup.setGroupByPolicy(types);
        updateEdgeView();
      }
    };
    src.addActionListener(groupTypes);
    tgt.addActionListener(groupTypes);

    final Box settings = Box.createVerticalBox();
    settings.add(new JLabel("Group by"));
    settings.add(src);
    settings.add(tgt);
    settings.add(Box.createGlue());
    return settings;
  }

  /**
   * Creates user controls for the {@link y.view.LocalViewCreator.FolderContents}
   * local view creator.
   * @return user controls for the {@link y.view.LocalViewCreator.FolderContents}
   * local view creator.
   */
  private JComponent createFolderContentsSettings() {
    final ButtonGroup includeFoldersGroup = new ButtonGroup();
    final JRadioButton always = new JRadioButton("Always Include Folders");
    always.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        folderContents.setFolderPolicy(FolderContents.FOLDER_POLICY_ALWAYS);
        updateNodeView();
      }
    });
    includeFoldersGroup.add(always);
    final JRadioButton asNeeeded = new JRadioButton("Include Folders As Needed");
    asNeeeded.setSelected(true);
    asNeeeded.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        folderContents.setFolderPolicy(FolderContents.FOLDER_POLICY_AS_NEEDED);
        updateNodeView();
      }
    });
    includeFoldersGroup.add(asNeeeded);
    final JRadioButton never = new JRadioButton("Never Include Folders");
    never.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        folderContents.setFolderPolicy(FolderContents.FOLDER_POLICY_NEVER);
        updateNodeView();
      }
    });
    includeFoldersGroup.add(never);

    final Box settings = Box.createVerticalBox();
    settings.add(always);
    settings.add(asNeeeded);
    settings.add(never);
    settings.add(Box.createGlue());
    return settings;
  }

  /**
   * Creates user controls for the {@link y.view.LocalViewCreator.AncestorGroups}
   * local view creator.
   * @return user controls for the {@link y.view.LocalViewCreator.AncestorGroups}
   * local view creator.
   */
  private JComponent createAncestorGroupsSettings() {
    final JCheckBox includeFocusNodes = new JCheckBox("Include Focus Nodes");
    includeFocusNodes.setSelected(ancestorGroups.isIncludeFocusNodes());
    includeFocusNodes.addActionListener(new AbstractAction() {
      public void actionPerformed( final ActionEvent e ) {
        ancestorGroups.setIncludeFocusNodes(includeFocusNodes.isSelected());
        updateNodeView();
      }
    });

    final Box settings = Box.createVerticalBox();
    settings.add(includeFocusNodes);
    settings.add(Box.createGlue());
    return settings;
  }

  /**
   * Creates user controls for the {@link y.view.LocalViewCreator.CommonParentGroup}
   * local view creator.
   * @return user controls for the {@link y.view.LocalViewCreator.CommonParentGroup}
   * local view creator.
   */
  private JComponent createCommonParentGroupSettings() {
    final JCheckBox includeDescendants = new JCheckBox("Include Descendants");
    includeDescendants.setSelected(commonParentGroup.isIncludeDescendants());
    includeDescendants.addActionListener(new AbstractAction() {
      public void actionPerformed( final ActionEvent e ) {
        commonParentGroup.setIncludeDescendants(includeDescendants.isSelected());
        updateNodeView();
      }
    });

    final Box settings = Box.createVerticalBox();
    settings.add(includeDescendants);
    settings.add(Box.createGlue());
    return settings;
  }

  /**
   * Creates user controls for the {@link y.view.LocalViewCreator.Neighborhood}
   * local view creator.
   * @return user controls for the {@link y.view.LocalViewCreator.Neighborhood}
   * local view creator.
   */
  private JComponent createNeighborhoodSettings() {
    final ButtonGroup nodesGroup = new ButtonGroup();
    final JRadioButton preds = new JRadioButton("Predecessors");
    preds.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setNeighborhoodType(Neighborhood.NEIGHBORHOOD_TYPE_PREDECESSORS);
        updateNodeView();
      }
    });
    nodesGroup.add(preds);
    final JRadioButton succs = new JRadioButton("Successors");
    succs.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setNeighborhoodType(Neighborhood.NEIGHBORHOOD_TYPE_SUCCESSORS);
        updateNodeView();
      }
    });
    nodesGroup.add(succs);
    final JRadioButton predsSuccs = new JRadioButton("Predecessors and Successors");
    predsSuccs.setSelected(true);
    predsSuccs.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setNeighborhoodType(Neighborhood.NEIGHBORHOOD_TYPE_PREDECESSORS_AND_SUCCESSORS);
        updateNodeView();
      }
    });
    nodesGroup.add(predsSuccs);
    final JRadioButton neighbors = new JRadioButton("All Neighbors");
    neighbors.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setNeighborhoodType(Neighborhood.NEIGHBORHOOD_TYPE_NEIGHBORS);
        updateNodeView();
      }
    });
    nodesGroup.add(neighbors);

    final JSpinner neighborhoodMGD = new JSpinner();
    neighborhoodMGD.getModel().setValue(new Integer(neighborhood.getMaximumGraphDistance()));
    ((SpinnerNumberModel) neighborhoodMGD.getModel()).setMinimum(new Integer(0));
    neighborhoodMGD.addChangeListener(new ChangeListener() {
      public void stateChanged( final ChangeEvent e ) {
        final Object value = neighborhoodMGD.getModel().getValue();
        if (value instanceof Integer) {
          final int mgd = ((Integer) value).intValue();
          if (mgd > -1) {
            neighborhood.setMaximumGraphDistance(mgd);
            updateNodeView();
          }
        }
      }
    });

    final ButtonGroup edgesGroup = new ButtonGroup();
    final JRadioButton none = new JRadioButton("None");
    none.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setEdgePolicy(Neighborhood.EDGE_POLICY_NONE);
        updateNodeView();
      }
    });
    edgesGroup.add(none);
    final JRadioButton subgraph = new JRadioButton("Subgraph Edges");
    subgraph.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setEdgePolicy(Neighborhood.EDGE_POLICY_INDUCED_SUBGRAPH);
        updateNodeView();
      }
    });
    edgesGroup.add(subgraph);
    final JRadioButton shortestPaths = new JRadioButton("Shortest Paths");
    shortestPaths.setSelected(true);
    shortestPaths.addActionListener(new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        neighborhood.setEdgePolicy(Neighborhood.EDGE_POLICY_SHORTEST_PATHS);
        updateNodeView();
      }
    });
    edgesGroup.add(shortestPaths);


    final Box nodeTypes = Box.createVerticalBox();
    nodeTypes.add(preds);
    nodeTypes.add(succs);
    nodeTypes.add(predsSuccs);
    nodeTypes.add(neighbors);

    final GridBagConstraints gbc = new GridBagConstraints();
    final JPanel includedNodes = new JPanel(new GridBagLayout());
    includedNodes.setBorder(BorderFactory.createTitledBorder("Included Nodes"));
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.gridheight = 1;
    gbc.weightx = 1;
    gbc.weighty = 0;
    includedNodes.add(nodeTypes, gbc);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(5, 0, 5, 0);
    gbc.weightx = 0;
    includedNodes.add(new JLabel("Maximum Graph Distance"), gbc);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    gbc.insets = new Insets(5, 5, 5, 0);
    gbc.weightx = 1;
    includedNodes.add(neighborhoodMGD, gbc);

    final Box includedEdges = Box.createVerticalBox();
    includedEdges.setBorder(BorderFactory.createTitledBorder("Included Edges"));
    includedEdges.add(none);
    includedEdges.add(subgraph);
    includedEdges.add(shortestPaths);

    final JPanel controls = new JPanel(new GridBagLayout());
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 1;
    gbc.weighty = 0;
    controls.add(includedNodes, gbc);
    gbc.gridy = 1;
    controls.add(includedEdges, gbc);

    final JPanel settings = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    settings.add(controls);
    return settings;
  }

  /**
   * Creates user controls for selecting the update trigger of the demo's local
   * view.
   * @return user controls for selecting the update trigger of the demo's local
   * view.
   */
  private JComponent createTriggerPane() {
    final ButtonGroup updateGroup = new ButtonGroup();
    final JRadioButton updateHover = new JRadioButton(new AbstractAction("Mouse Hover") {
      public void actionPerformed( final ActionEvent e ) {
        setTrigger(HOVER_TRIGGER);
      }
    });
    updateGroup.add(updateHover);
    final JRadioButton updateSelection = new JRadioButton(new AbstractAction("Selection Change") {
      public void actionPerformed( final ActionEvent e ) {
        setTrigger(SELECTION_TRIGGER);
      }
    });
    updateSelection.setSelected(true);
    updateGroup.add(updateSelection);

    final Box updatePane = Box.createHorizontalBox();
    updatePane.add(Box.createHorizontalStrut(5));
    updatePane.add(new JLabel("Update Trigger: "));
    updatePane.add(updateHover);
    updatePane.add(updateSelection);
    updatePane.add(Box.createGlue());
    return updatePane;
  }

  /**
   * Adds the user controls for the specified local view creator.
   * @param title   the display name of the specified local view creator.
   * @param settings   the user controls for the specified local view creator's
   * specific settings.
   * @param lvc   the local view creator for which user controls are added.
   * @param nodeBased   <code>true</code> if the specified local view creator
   * is node related, <code>false</code> otherwise.
   */
  private void addStrategy(
          final JPanel settingsPane,
          final Box strategyButtons,
          final ButtonGroup strategiesGroup,
          final String title,
          JComponent settings,
          final AbstractLocalViewCreator lvc,
          final boolean nodeBased
  ) {
    if (settings == null) {
      settings = new JPanel();
      settings.add(new JLabel("No Settings"));
    }
    final JRadioButton strategyButton = new JRadioButton(new AbstractAction(title) {
      public void actionPerformed(ActionEvent e) {
        ((CardLayout) settingsPane.getLayout()).show(settingsPane, title);
        if (nodeBased) {
          setNodeLocalViewCreator(lvc);
        } else {
          setEdgeLocalViewCreator(lvc);
        }
      }
    });

    final CompoundBorder border = BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(title + " Settings"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    settings.setBorder(border);
    settingsPane.add(settings, title);

    strategiesGroup.add(strategyButton);
    if (strategiesGroup.getButtonCount() == 1) {
      strategiesGroup.setSelected(strategyButton.getModel(), true);
    }
    strategyButtons.add(strategyButton);
  }

  /**
   * Creates a layouter that can be used to lay out the contents of a local view
   * after an local view update.
   * @return a layouter that can be used to lay out the contents of a local view
   * after an local view update.
   */
  private Layouter createLayouter() {
    // the sample graph should be an UML inheritance diagram which means
    // a hierachical layout style is well suited to lay out all or parts
    // of such diagrams
    final IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);

    // specify the routing style for edges and minimum edge lengths
    // to produce "nice" local views
    final EdgeLayoutDescriptor eld = layouter.getEdgeLayoutDescriptor();
    eld.setOrthogonallyRouted(true);
    eld.setMinimumFirstSegmentLength(25);
    eld.setMinimumLength(35);
    return layouter;
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new LocalViewDemo("resource/localviewhelp.html")).start();
      }
    });
  }

  /**
   * A <code>Graph2DSelectionListener</code> that triggers an update of the
   * demo's local view upon selection changes.
   */
  private final class SelectionTrigger implements Graph2DSelectionListener {
    private final Timer timer;
    private Graph2DSelectionEvent lastEvent;

    SelectionTrigger() {
      timer = new Timer(TIMER_DELAY, new ActionListener() {
        public void actionPerformed( final ActionEvent e ) {
          if (lastEvent != null) {
            handleEvent(lastEvent);
          }
        }

        /**
         * Triggers the actual update for the demo's local view.
         */
        private void handleEvent( final Graph2DSelectionEvent e ) {
          if (e.isNodeSelection()) {
            createNodeView(e.getGraph2D().selectedNodes());
          } else if (e.isEdgeSelection()) {
            createEdgeView(e.getGraph2D().selectedEdges());
          }
        }
      });
      timer.setRepeats(false);
    }

    public void onGraph2DSelectionEvent( final Graph2DSelectionEvent e ) {
      if (e.isNodeSelection() || e.isEdgeSelection()) {
        lastEvent = e;
        timer.restart();
      }
    }
  }

  /**
   * A <code>ViewMode</code> that triggers an update of the demo's local view
   * whenever the mouse hovers over graph elements for a certain amount of time.
   */
  private final class HoverTrigger extends ViewMode {
    private final Timer timer;
    private YPoint lastPosition;

    private HoverTrigger() {
      timer = new Timer(TIMER_DELAY, new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          if (lastPosition != null) {
            handleHit(getHitInfo(lastPosition.x, lastPosition.y));
          }
        }

        /**
         * Triggers the actual update for the demo's local view.
         */
        private void handleHit( final HitInfo hitInfo ) {
          if (hitInfo.hasHitNodes()) {
            createNodeView(hitInfo.hitNodes());
          } else if (hitInfo.hasHitEdges()) {
            createEdgeView(hitInfo.hitEdges());
          }
        }
      });
      timer.setRepeats(false);
    }

    public void mouseMoved(double x, double y) {
      super.mouseMoved(x, y);
      this.lastPosition = new YPoint(x, y);
      timer.restart();
    }
  }

  /**
   * Custom local view creator that displays the subgraph that is induced by
   * the currently selected nodes of the creator's focus nodes set.
   * This class serves as a sample implementation of a simple strategy
   * for creating local views.
   */
  private static final class SelectedSubgraph extends AbstractLocalViewCreator {
    private final Graph2D model;
    private final GraphCopier.CopyFactory factory;
    private final Graph2D view;

    /**
     * Creates a new <code>SelectedSubgraph</code> instance
     * @param model   the creator's model graph.
     * @param factory   the <code>CopyFactory</code> used to create graph
     * elements in the creator's view graph. (The specified factory has to be
     * able to create copies of graph elements from the specified model graph in
     * the specified view graph.)
     * @param view   the creator's view graph. This graph is updated/modified
     * whenever the creator's <code>create</code> method is called.
     * @see #updateViewGraph()
     */
    SelectedSubgraph(
            final Graph2D model,
            final GraphCopier.CopyFactory factory,
            final Graph2D view
    ) {
      super(AbstractLocalViewCreator.ELEMENT_TYPE_NODE);
      this.model = model;
      this.factory = factory;
      this.view = view;

      // ModelViewManager is a convenient way to get model-to-view mappings
      // for free when creating copies/views of graphs
      final ModelViewManager mvm = ModelViewManager.getInstance(model);
      if (!mvm.isViewGraph(view)) {
        mvm.addViewGraph(view, null, false, false);
        mvm.setCopyFactory(view, factory);
      }
    }

    /**
     * Returns the creator's model graph.
     * @return the creator's model graph.
     */
    public Graph2D getModel() {
      return model;
    }

    /**
     * Returns the creator's view graph.
     * @return the creator's view graph.
     */
    public Graph2D getViewGraph() {
      return view;
    }

    /**
     * Returns a node in the creator's model graph that corresponds to the
     * specified node in the creator's view graph.
     * @param view   a node in the creator's view graph.
     * @return a node in the creator's model graph that corresponds to the
     * specified node in the creator's view graph.
     */
    public Node getModelNode( final Node view ) {
      return getManager().getModelNode(view);
    }

    /**
     * Returns a node in the creator's view graph that corresponds to the
     * specified node in the creator's model graph or <code>null</code> if
     * there is no corresponding node.
     * @param model   a node in the creator's model graph.
     * @return a node in the creator's view graph that corresponds to the
     * specified node in the creator's model graph or <code>null</code> if
     * there is no corresponding node.
     */
    public Node getViewNode( final Node model ) {
      return getManager().getViewNode(model, getViewGraph());
    }

    /**
     * Returns a edge in the creator's model graph that corresponds to the
     * specified edge in the creator's view graph.
     * @param view   a edge in the creator's view graph.
     * @return a edge in the creator's model graph that corresponds to the
     * specified edge in the creator's view graph.
     */
    public Edge getModelEdge( final Edge view ) {
      return getManager().getModelEdge(view);
    }

    /**
     * Returns a edge in the creator's view graph that corresponds to the
     * specified edge in the creator's model graph or <code>null</code> if
     * there is no corresponding edge.
     * @param model   a edge in the creator's model graph.
     * @return a edge in the creator's view graph that corresponds to the
     * specified edge in the creator's model graph or <code>null</code> if
     * there is no corresponding edge.
     */
    public Edge getViewEdge( final Edge model ) {
      return getManager().getViewEdge(model, getViewGraph());
    }

    /**
     * Returns <code>null</code> to indicate that the creator's view graph
     * should not be laid out on updates.
     * @return <code>null</code> to indicate that the creator's view graph
     * should not be laid out on updates.
     */
    protected Layouter createDefaultLayouter() {
      return null;
    }

    /**
     * Updates the creator's view graph.
     */
    protected void buildViewGraph() {
      final Graph2D model = getModel();
      if (!model.isSelectionEmpty()) {
        final HashSet nodes = new HashSet();
        final HashSet edges = new HashSet();

        // take only the currently selected nodes into account
        for (Iterator it = focusNodes(); it.hasNext();) {
          final Node node = (Node) it.next();
          if (model.isSelected(node)) {
            nodes.add(node);
          }
        }

        // collect the edges that make up the induced subgraph
        for (EdgeCursor ec = model.edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (nodes.contains(edge.source()) && nodes.contains(edge.target())) {
            edges.add(edge);
          }
        }

        // now create corresponding graph elements in the creator's view graph
        final Graph2D view = getViewGraph();
        final ModelViewManager mvm = getManager();

        // in theory, each local view creator could use a different factory
        // but share the same view graph. ModelViewManager, however, can only
        // store one factory per view graph. therefore the "correct" factory
        // has to be temporarily set for the creator's view graph.
        final GraphCopier.CopyFactory oldFactory = mvm.getCopyFactory(view);
        if (oldFactory != factory) {
          mvm.setCopyFactory(view, factory);
        }
        try {
          mvm.synchronizeModelToViewGraph(
                  (new NodeList(nodes.iterator())).nodes(),
                  (new EdgeList(edges.iterator())).edges(),
                  view);
        } finally {
          // reset the original factory stored for the view graph
          if (oldFactory != factory) {
            mvm.setCopyFactory(view, oldFactory);
          }
        }
      }
    }

    private ModelViewManager getManager() {
      return ModelViewManager.getInstance(getModel());
    }
  }
}
