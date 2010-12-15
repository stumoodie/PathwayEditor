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
import demo.view.application.DragAndDropDemo;
import y.base.Edge;
import y.layout.router.BusRouter;
import y.option.CompoundEditor;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.IntOptionItem;
import y.option.ItemEditor;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.RealizerCellRenderer;
import y.option.ResourceBundleGuiFactory;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.ShapeNodePainter;
import y.view.View2DConstants;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Governs the settings for the grid, the snap lines, the orthogonal mode and the automatic routing feature.
 */
class BusRouterDemoTools {
  static final String HUB_CONFIGURATION = "BusHub";
  static final String REGULAR_CONFIGURATION = "Regular";

  private static final String GROUP_GRID = "GROUP_GRID";
  private static final String GROUP_EDIT = "GROUP_EDIT";
  private static final String GROUP_ROUTER = "GROUP_ROUTER";

  private static final String AUTOMATIC = "AUTOMATIC";
  private static final String GRID_ENABLED = "GRID_ENABLED";
  private static final String GRID_SPACING = "GRID_SPACING";
  private static final String ORTHOGONAL = "ORTHOGONAL";
  private static final String SNAPPING = "SNAPPING";

  private final BusRouter busRouter;
  private DragAndDropDemo.DragAndDropSupport dndSupport;
  private OptionHandler optionHandler;
  private final DemoBase.SnappingConfiguration snappingConfiguration;
  private final Graph2DView view;

  EdgeRealizer[] edgeRealizerTemplates;
  NodeRealizer[] nodeRealizerTemplates;

  /**
   * Creates a new instance.
   */
  BusRouterDemoTools(Graph2DView view, BusRouter busRouter) {
    this.view = view;
    this.busRouter = busRouter;
    this.edgeRealizerTemplates = createEdgeRealizerTemplates();
    this.nodeRealizerTemplates = createNodeRealizerTemplates();

    this.snappingConfiguration = DemoBase.createDefaultSnappingConfiguration();
    this.snappingConfiguration.setGridType(View2DConstants.GRID_POINTS);
    createOptionHandler();

    // create the data provider needed for the Orthogonal Mode
    view.getGraph2D().addDataProvider(EditMode.ORTHOGONAL_ROUTING_DPKEY, new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        final EdgeRealizer realizer = BusRouterDemoTools.this.view.getGraph2D().getRealizer((Edge) dataHolder);
        return dataHolder instanceof Edge && LineType.LINE_2.equals(realizer.getLineType());
      }
    });
  }

  /**
   * Creates the tool panel which contains the DragAndDrop list and the settings panel.
   */
  JPanel createToolPane() {
    final int itemWidth = (int) (nodeRealizerTemplates[0].getWidth() + 25.0);
    final int itemHeight = (int) (nodeRealizerTemplates[0].getHeight() + 40.0);
    dndSupport = new DragAndDropDemo.DragAndDropSupport(nodeRealizerTemplates, view, itemWidth, itemHeight) {
      protected RealizerCellRenderer createCellRenderer(int itemWidth, int itemHeight) {
        return new LabelledRealizerCellRenderer(itemWidth, itemHeight);
      }
    };

    JScrollPane listScrollPane = new JScrollPane(dndSupport.getList());
    listScrollPane.setPreferredSize(new Dimension(itemWidth, (int) (2.2 * (double) itemHeight)));
    listScrollPane.setMinimumSize(new Dimension((int) (1.2 * (double) itemWidth), (int) (1.2 * (double) itemHeight)));

    JPanel toolPane = new JPanel(new BorderLayout());
    toolPane.add(listScrollPane, BorderLayout.NORTH);
    toolPane.add(new JScrollPane(createOptionComponent()), BorderLayout.CENTER);

    return toolPane;
  }

  /**
   * Creates an option handler containing items for grid, orthogonal mode, snap lines and automatic routing.
   */
  private void createOptionHandler() {
    optionHandler = new OptionHandler("BUS_ROUTER_DEMO_SETTINGS");
    OptionItem item;
    OptionGroup og;

    // items of group ROUTER
    optionHandler.addBool(AUTOMATIC, true);

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_ROUTER);
    og.addItem(optionHandler.getItem(AUTOMATIC));

    // items of group GRID
    item = optionHandler.addBool(GRID_ENABLED, false);
    item.addPropertyChangeListener("value", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateGrid();
      }
    });
    item = optionHandler.addInt(GRID_SPACING, 20);
    item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    item.addPropertyChangeListener("value", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateGrid();
      }
    });

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_GRID);
    og.addItem(optionHandler.getItem(GRID_ENABLED));
    og.addItem(optionHandler.getItem(GRID_SPACING));

    ConstraintManager cm = new ConstraintManager(optionHandler);
    cm.setEnabledOnValueEquals(GRID_ENABLED, Boolean.TRUE, GRID_SPACING);

    // items of group EDIT
    item = optionHandler.addBool(ORTHOGONAL, false);
    item.addPropertyChangeListener("value", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateOrthogonalMode();
      }
    });

    item = optionHandler.addBool(SNAPPING, true);
    item.addPropertyChangeListener("value", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateSnapLines();
      }
    });

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_EDIT);
    og.addItem(optionHandler.getItem(ORTHOGONAL));
    og.addItem(optionHandler.getItem(SNAPPING));
  }

  /**
   * Creates a control for the settings to be used in the tool pane.
   * @return a control for the settings.
   */
  JComponent createOptionComponent() {
    DefaultEditorFactory editorFactory = new DefaultEditorFactory();
    ResourceBundleGuiFactory gf = null;
    try {
      gf = new ResourceBundleGuiFactory();
      gf.addBundle(BusRouterDemo.class.getName());
      editorFactory.setGuiFactory(gf);
    }
    catch (final MissingResourceException mre) {
      //noinspection UseOfSystemOutOrSystemErr
      System.err.println("Could not find resources! " + mre);
    }

    Editor editor = editorFactory.createEditor(optionHandler);

    // set the editor to auto adopt and auto commit, so no OK button is needed
    final ArrayList stack = new ArrayList();
    stack.add(editor);
    while (!stack.isEmpty()) {
      final Object last = stack.remove(stack.size() - 1);
      if (last instanceof CompoundEditor) {
        for (Iterator it = ((CompoundEditor) last).editors(); it.hasNext();) {
          stack.add(it.next());
        }
      }
      if (last instanceof ItemEditor) {
        ((ItemEditor) last).setAutoCommit(true);
        ((ItemEditor) last).setAutoAdopt(true);
      }
    }

    return editor.getComponent();
  }

  /**
   * Returns whether the automatic routing for new or changed parts of the graph is enabled.
   * @return <code>true</code> is automatic routing is enabled
   */
  boolean isAutomaticRoutingEnabled() {
    return optionHandler.getBool(AUTOMATIC);
  }

  /**
   * Sets whether the automatic routing for new or changed parts of the graph is enabled.
   * @param enabled the state to set
   */
  void setAutomaticEnabled(boolean enabled) {
    optionHandler.set(AUTOMATIC, Boolean.valueOf(enabled));
  }

  /**
   * Sets the default realizers of this view's graph according to the selection in die DragAndDrop list and the current
   * setting for orthogonal mode.
   */
  void updateDefaultRealizer() {
    view.getGraph2D().setDefaultNodeRealizer(nodeRealizerTemplates[dndSupport.getList().getSelectedIndex()]);
    view.getGraph2D().setDefaultEdgeRealizer(
        optionHandler.getBool(ORTHOGONAL) ? edgeRealizerTemplates[1] : edgeRealizerTemplates[0]);
  }

  /**
   * Updates the grid settings of this view to the values specified by the option handler.
   */
  void updateGrid() {
    busRouter.setGridRoutingEnabled(optionHandler.getBool(GRID_ENABLED));
    busRouter.setGridSpacing(optionHandler.getInt(GRID_SPACING));
    snappingConfiguration.setGridSnappingEnabled(optionHandler.getBool(GRID_ENABLED));
    snappingConfiguration.setGridDistance((double) optionHandler.getInt(GRID_SPACING));
    configureSnapping();
  }

  /**
   * Updates the orthogonal mode of this view to the values specified by the option handler.
   */
  void updateOrthogonalMode() {
    final CreateEdgeMode createEdgeMode = (CreateEdgeMode) ((EditMode) view.getViewModes().next()).getCreateEdgeMode();
    final boolean orthogonalMode = optionHandler.getBool(ORTHOGONAL);
    createEdgeMode.setOrthogonalEdgeCreation(orthogonalMode);
    view.getGraph2D().setDefaultEdgeRealizer(orthogonalMode ? edgeRealizerTemplates[1] : edgeRealizerTemplates[0]);
  }

  /**
   * Updates the settings for the snap lines to the values specified by the option handler.
   */
  void updateSnapLines() {
    snappingConfiguration.setSnappingEnabled(optionHandler.getBool(SNAPPING));
    configureSnapping();
  }

  /**
   * Configures the snap line support of this view, its edit mode, and the DragAndDrop support.
   */
  private void configureSnapping() {
    snappingConfiguration.configureView(view);
    snappingConfiguration.configureEditMode((EditMode) view.getViewModes().next());
    dndSupport.configureSnapping(snappingConfiguration, true);
  }

  /**
   * Creates two edge realizers, one for orthogonal edges and one for all other edges.
   * @return an array containing the two edge realizers
   */
  private static EdgeRealizer[] createEdgeRealizerTemplates() {
    final PolyLineEdgeRealizer er = new PolyLineEdgeRealizer();
    er.setTargetArrow(Arrow.NONE);
    EdgeRealizer[] realizers = new EdgeRealizer[2];

    realizers[0] = er.createCopy();
    realizers[0].setLineType(LineType.LINE_1);

    realizers[1] = er.createCopy();
    realizers[1].setLineType(LineType.LINE_2);

    return realizers;
  }

  /**
   * Creates the two node realizers used for the template pane, the realizer for regular nodes and the realizer for hub
   * nodes.
   * @return an array containing the two node realizers
   */
  private static NodeRealizer[] createNodeRealizerTemplates() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map map = GenericNodeRealizer.getFactory().createDefaultConfigurationMap();
    NodeRealizer[] realizers = new NodeRealizer[2];

    {
      map.put(GenericNodeRealizer.Painter.class,
          factory.getImplementation(DemoDefaults.NODE_CONFIGURATION, GenericNodeRealizer.Painter.class));
      factory.addConfiguration(REGULAR_CONFIGURATION, map);

      NodeRealizer nr = new GenericNodeRealizer(REGULAR_CONFIGURATION);
      nr.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
      nr.setLineColor(DemoDefaults.DEFAULT_NODE_LINE_COLOR);
      nr.setSize(80.0, 40.0);
      nr.removeLabel(nr.getLabel(0));
      realizers[0] = nr;
    }
    {
      map.put(GenericNodeRealizer.Painter.class, new ShapeNodePainter(ShapeNodePainter.RECT));
      factory.addConfiguration(HUB_CONFIGURATION, map);

      NodeRealizer nr = new GenericNodeRealizer(HUB_CONFIGURATION);
      nr.setFillColor(Color.BLACK);
      nr.setLineColor(null);
      nr.setSize(10.0, 10.0);
      nr.removeLabel(nr.getLabel(0));
      realizers[1] = nr;
    }
    return realizers;
  }

  /**
   * A <code>RealizerCellRenderer</code> which adds descriptive labels to the node icons.
   */
  static class LabelledRealizerCellRenderer extends RealizerCellRenderer {
    LabelledRealizerCellRenderer(
        int itemWidth, int itemHeight) {
      super(itemWidth, itemHeight);
    }

    /**
     * Creates an icon that displays the specified realizer and adds descriptive labels to regular and hub nodes.
     * @param realizer   the node visualization to display.
     * @param iconWidth  the desired width of the created icon.
     * @param iconHeight the desired height of the created icon.
     * @return
     */
    protected Icon createNodeRealizerIcon(NodeRealizer realizer, int iconWidth, int iconHeight) {
      NodeRealizer labelledRealizer = realizer.createCopy();
      labelledRealizer.setLabelText(isHubRealizer(labelledRealizer) ? "Hub" : "Regular Node");
      labelledRealizer.getLabel().setModel(NodeLabel.EIGHT_POS);
      labelledRealizer.getLabel().setPosition(NodeLabel.S);
      return super.createNodeRealizerIcon(labelledRealizer, iconWidth, iconHeight);
    }

    protected String createNodeToolTipText(NodeRealizer realizer) {
      return isHubRealizer(realizer) ? "Hub" : "Regular Node";
    }

    private boolean isHubRealizer(NodeRealizer realizer) {
      return realizer instanceof GenericNodeRealizer &&
          BusRouterDemoTools.HUB_CONFIGURATION.equals(((GenericNodeRealizer) realizer).getConfiguration());
    }
  }
}
