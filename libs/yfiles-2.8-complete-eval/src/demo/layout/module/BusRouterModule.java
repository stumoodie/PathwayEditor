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
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.router.BusDescriptor;
import y.layout.router.BusRouter;
import y.option.ConstraintManager;
import y.option.DoubleOptionItem;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.Graph2D;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * Module for the {@link y.layout.router.BusRouter}.
 * <p>
 * There are more scopes in this module than in {@link BusRouter}. Each additional scope is mapped to an appropriate
 * combinations of scope and fixed edges in BusRouter.
 * </p>
 * <dl>
 * <dt>ALL</dt>
 * <dd>Maps to <code>BusRouter.SCOPE_ALL</code>. All edges are in scope, and all of them are movable.</dd>
 * <dt>SUBSET</dt>
 * <dd>Maps to <code>BusRouter.SCOPE_SUBSET</code>. The selected edges are in scope, and all of them are movable.</dd>
 * <dt>SUBSET_BUS</dt>
 * <dd>Each bus with at least one selected edge is in scope, and all of their edges are movable.</dd>
 * <dt>PARTIAL</dt>
 * <dd>Each bus with at least one selected node is in scope, and only the adjacent edges of the selected nodes are
 * movable.</dd>
 * </dl>
 *
 */
public class BusRouterModule extends LayoutModule {

  private static final String NAME = "BUS_ROUTER";
  private static final String GROUP_LAYOUT = "GROUP_LAYOUT";
  private static final String GROUP_SELECTION = "GROUP_SELECTION";
  private static final String GROUP_ROUTING = "GROUP_ROUTING";

  private static final String ALL = "ALL";
  private static final String BUSES = "BUSES";
  private static final String COLOR = "COLOR";
  private static final String CROSSING_COST = "CROSSING_COST";
  private static final String CROSSING_REROUTING = "CROSSING_REROUTING";
  private static final String GRID_ENABLED = "GRID_ENABLED";
  private static final String GRID_SPACING = "GRID_SPACING";
  private static final String MINIMUM_CONNECTIONS_COUNT = "MINIMUM_CONNECTIONS_COUNT";
  private static final String MINIMUM_BACKBONE_LENGTH = "MINIMUM_BACKBONE_LENGTH";
  private static final String MIN_DISTANCE_TO_EDGES = "MIN_DISTANCE_TO_EDGES";
  private static final String MIN_DISTANCE_TO_NODES = "MIN_DISTANCE_TO_NODES";
  private static final String PREFERRED_BACKBONE_COUNT = "PREFERRED_BACKBONE_COUNT";
  private static final String PARTIAL = "PARTIAL";
  private static final String SCOPE = "SCOPE";
  private static final String SINGLE = "SINGLE";
  private static final String SUBSET = "SUBSET";
  private static final String SUBSET_BUS = "SUBSET_BUS";

  private final BusRouter busRouter;

  /**
   * Specifies whether the options of group layout are used.
   */
  protected boolean optionsLayout;
  /**
   * Specifies whether the options for initial backbone selection are used.
   */
  protected boolean optionsSelection;
  /**
   * Specifies whether the options for routing and recombination are used.
   */
  protected boolean optionsRouting;

  /**
   * Creates a new instance of this module.
   */
  public BusRouterModule() {
    super(NAME, "yFiles Layout Team", "Routes edges in bus-style");
    busRouter = new BusRouter();
    optionsLayout = true;
    optionsSelection = true;
    optionsRouting = true;
  }

  /**
   * Creates an option handler for this module.
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler oh = new OptionHandler(NAME);
    addOptionItems(oh);
    return oh;
  }

  /**
   * Adds the option items used by this module to the given <code>OptionHandler</code>.
   * @param oh the <code>OptionHandler</code> to add the items to
   */
  protected void addOptionItems(OptionHandler oh) {
    ConstraintManager cm = new ConstraintManager(oh);

    BusRouter localRouter = new BusRouter();

    if (optionsLayout) {
      OptionItem item;
      oh.addEnum(SCOPE, new String[]{ALL, SUBSET, SUBSET_BUS, PARTIAL}, (int) localRouter.getScope());
      oh.addEnum(BUSES, new String[]{SINGLE, COLOR}, 0);
      oh.addBool(GRID_ENABLED, localRouter.isGridRoutingEnabled());
      item = oh.addInt(GRID_SPACING, localRouter.getGridSpacing());
      item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      item = oh.addInt(MIN_DISTANCE_TO_NODES, localRouter.getMinimumDistanceToNode());
      item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      item = oh.addInt(MIN_DISTANCE_TO_EDGES, localRouter.getMinimumDistanceToEdge());
      item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));

      cm.setEnabledOnValueEquals(GRID_ENABLED, Boolean.TRUE, GRID_SPACING);

      OptionGroup og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_LAYOUT);
      og.addItem(oh.getItem(SCOPE));
      og.addItem(oh.getItem(BUSES));
      og.addItem(oh.getItem(GRID_ENABLED));
      og.addItem(oh.getItem(GRID_SPACING));
      og.addItem(oh.getItem(MIN_DISTANCE_TO_NODES));
      og.addItem(oh.getItem(MIN_DISTANCE_TO_EDGES));
    }

    if (optionsSelection) {
      OptionItem item;
      item = oh.addInt(PREFERRED_BACKBONE_COUNT, localRouter.getPreferredBackboneSegmentCount());
      item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      item = oh.addDouble(MINIMUM_BACKBONE_LENGTH, localRouter.getMinimumBackboneSegmentLength());
      item.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(1.0));

      OptionGroup og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_SELECTION);
      og.addItem(oh.getItem(PREFERRED_BACKBONE_COUNT));
      og.addItem(oh.getItem(MINIMUM_BACKBONE_LENGTH));
    }

    if (optionsRouting) {
      OptionItem item;
      item = oh.addDouble(CROSSING_COST, localRouter.getCrossingCost());
      item.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
      oh.addBool(CROSSING_REROUTING, localRouter.isReroutingEnabled());
      item.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
      item = oh.addInt(MINIMUM_CONNECTIONS_COUNT, localRouter.getMinimumBusConnectionsCount());
      item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));

      OptionGroup og = new OptionGroup();
      og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_ROUTING);
      og.addItem(oh.getItem(CROSSING_COST));
      og.addItem(oh.getItem(CROSSING_REROUTING));
      og.addItem(oh.getItem(MINIMUM_CONNECTIONS_COUNT));
    }

    initOptionHandler(oh, busRouter);
  }

  /**
   * Sets the option items of the given option handler to the settings of the given <code>BusRouter</code>.
   * @param oh        the option handler
   * @param busRouter the bus router
   */
  protected void initOptionHandler(OptionHandler oh, BusRouter busRouter) {
    if (optionsLayout) {
      oh.set(SCOPE, toScopeName(busRouter.getScope()));
      oh.set(GRID_ENABLED, Boolean.valueOf(busRouter.isGridRoutingEnabled()));
      oh.set(GRID_SPACING, new Integer(busRouter.getGridSpacing()));
      oh.set(MIN_DISTANCE_TO_NODES, new Integer(busRouter.getMinimumDistanceToNode()));
      oh.set(MIN_DISTANCE_TO_EDGES, new Integer(busRouter.getMinimumDistanceToEdge()));
    }

    if (optionsSelection) {
      oh.set(CROSSING_COST, new Double(busRouter.getCrossingCost()));
      oh.set(CROSSING_REROUTING, Boolean.valueOf(busRouter.isReroutingEnabled()));
    }

    if (optionsRouting) {
      oh.set(PREFERRED_BACKBONE_COUNT, new Integer(busRouter.getPreferredBackboneSegmentCount()));
      oh.set(MINIMUM_CONNECTIONS_COUNT, new Integer(busRouter.getMinimumBusConnectionsCount()));
      oh.set(MINIMUM_BACKBONE_LENGTH, new Double(busRouter.getMinimumBackboneSegmentLength()));
    }
  }

  /**
   * Configures an instance of {@link y.layout.router.BusRouter}. The values provided by this module's option handler
   * are being used for this purpose.
   * @param busRouter the BusRouter to be configured.
   */
  public void configure(BusRouter busRouter) {
    final OptionHandler oh = getOptionHandler();

    if (optionsLayout) {
      busRouter.setScope(toBusRouterScope(oh.get(SCOPE)));
      busRouter.setGridRoutingEnabled(oh.getBool(GRID_ENABLED));
      busRouter.setGridSpacing(oh.getInt(GRID_SPACING));
      busRouter.setMinimumDistanceToNode(oh.getInt(MIN_DISTANCE_TO_NODES));
      busRouter.setMinimumDistanceToEdge(oh.getInt(MIN_DISTANCE_TO_EDGES));
    }

    if (optionsSelection) {
      busRouter.setCrossingCost(oh.getDouble(CROSSING_COST));
      busRouter.setReroutingEnabled(oh.getBool(CROSSING_REROUTING));
    }

    if (optionsRouting) {
      busRouter.setPreferredBackboneSegmentCount(oh.getInt(PREFERRED_BACKBONE_COUNT));
      busRouter.setMinimumBusConnectionsCount(oh.getInt(MINIMUM_CONNECTIONS_COUNT));
      busRouter.setMinimumBackboneSegmentLength(oh.getDouble(MINIMUM_BACKBONE_LENGTH));
    }
  }

  /**
   * Returns the bus router used by this module.
   * @return the bus router used by this modul
   */
  protected BusRouter getBusRouter() {
    return busRouter;
  }

  /**
   * Launches the layouter of this module.
   * @noinspection ConstantConditions,PointlessBooleanExpression
   */
  protected void mainrun() {
    launchLayouter(busRouter);
  }

  /**
   * Prepares this module for {@link #mainrun()}.
   */
  protected void init() {
    super.init();
    configure(busRouter);

    if (!optionsLayout) {
      return;
    }

    final Graph2D graph = getGraph2D();
    final OptionHandler oh = getOptionHandler();

    // Explicit boolean variables for BUSES and SCOPE since these are often queried
    final boolean busByColor = COLOR.equals(oh.get(BUSES));
    final boolean scopePartial = PARTIAL.equals(oh.get(SCOPE));

    /*
     * The following creates bus descriptors according to the set options for SCOPE and BUSES and with respect to the
     * current selection.
     */
    final EdgeMap descriptorMap = Maps.createHashedEdgeMap();
    graph.addDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY, descriptorMap);
    // Create the bus descriptors. For scopes SUBSET_BUS and PARTIAL, this is done for all edges since the bus IDs
    // are required to determine which edges belong to the final scope.
    for (EdgeCursor ec = SUBSET.equals(oh.get(SCOPE)) ? graph.selectedEdges() : graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      // Except for scope PARTIAL, all edges in scope are movable
      final boolean fixed = scopePartial && !graph.isSelected(edge.source()) && !graph.isSelected(edge.target());
      final Color id = busByColor ? graph.getRealizer(edge).getLineColor() : Color.BLACK;
      descriptorMap.set(edge, new BusDescriptor(id, fixed));
    }

    /*
     * Create the appropriate selected edges data provider for the current scope.
     */
    final DataProvider descriptorDP = graph.getDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY);
    if (SUBSET.equals(oh.get(SCOPE))) {
      // The selected edges are in scope, and all of them are movable
      graph.addDataProvider(busRouter.getSelectedEdgesDpKey(), new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return dataHolder instanceof Edge && graph.isSelected((Edge) dataHolder);
        }
      });
    } else if (SUBSET_BUS.equals(oh.get(SCOPE))) {
      // Each bus with at least one selected edge is in scope, and all of their edges are movable.
      final Set selectedIDs = new HashSet();
      for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
        selectedIDs.add(((BusDescriptor) descriptorDP.get(ec.edge())).getID());
      }
      graph.addDataProvider(busRouter.getSelectedEdgesDpKey(), new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedIDs.contains(((BusDescriptor) descriptorDP.get(dataHolder)).getID());
        }
      });
    } else if (PARTIAL.equals(oh.get(SCOPE))) {
      // Each bus with at least one selected node is in scope, and the adjacent edges of the selected
      // nodes are movable.
      final Set selectedIDs = new HashSet();
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
          selectedIDs.add(((BusDescriptor) descriptorDP.get(ec.edge())).getID());
        }
      }
      graph.addDataProvider(busRouter.getSelectedEdgesDpKey(), new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedIDs.contains(((BusDescriptor) descriptorDP.get(dataHolder)).getID());
        }
      });
    }

  }

  /**
   * Disposes this module after {@link #mainrun()}.
   */
  protected void dispose() {
    if (optionsLayout) {
      // remove the data providers set by this module
      getGraph2D().removeDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY);

      if (!ALL.equals(getOptionHandler().get(SCOPE))) {
        getGraph2D().removeDataProvider(busRouter.getSelectedEdgesDpKey());
      }
    }

    super.dispose();
  }

  private static byte toBusRouterScope(Object scopeName) {
    if (ALL.equals(scopeName)) {
      return BusRouter.SCOPE_ALL;
    } else if (SUBSET.equals(scopeName) || SUBSET_BUS.equals(scopeName) || PARTIAL.equals(scopeName)) {
      return BusRouter.SCOPE_SUBSET;
    } else {
      return BusRouter.SCOPE_ALL;
    }
  }

  private static String toScopeName(byte busRouterScope) {
    if (BusRouter.SCOPE_ALL == busRouterScope) {
      return ALL;
    } else if (BusRouter.SCOPE_SUBSET == busRouterScope) {
      return SUBSET;
    } else {
      throw new IllegalArgumentException("Unknown scope: " + busRouterScope);
    }
  }
}
