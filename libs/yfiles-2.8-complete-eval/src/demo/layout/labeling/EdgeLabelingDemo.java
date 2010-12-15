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
package demo.layout.labeling;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.YCursor;
import y.base.YList;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.layout.BufferedLayouter;
import y.layout.EdgeLabelLayout;
import y.layout.EdgeLabelModel;
import y.layout.LabelCandidate;
import y.layout.LayoutGraph;
import y.layout.RotatedDiscreteEdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.option.CompoundEditor;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.ItemEditor;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DUndoManager;
import y.view.Graph2DViewActions;
import y.view.PopupMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * This demo shows how to configure edge labels and the corresponding edge label models as well as how to apply the
 * generic edge labeling algorithm.
 * <p/>
 * To add a new edge label right-click on an edge and choose item "Add Label". The properties of an existing edge label
 * (i.e., its label text and its preferred placement) can be changed by right-click on the label and choose item "Edit
 * Properties". Edge labels can be moved to another valid position according to the current label model by using drag
 * and drop.
 * <p/>
 * The demo allows to switch between two sample graphs (using the combo box in the toolbar), i.e., a graph drawn with an
 * orthogonal layout algorithm as well as a graph drawn with an organic layout algorithm. To manually start the generic
 * labeling algorithm click on the "Do Generic Labeling" button. Note: after changing one of the properties stated
 * below, the generic labeling algorithm is applied automatically.
 */
public class EdgeLabelingDemo extends DemoBase {
  //option handler texts
  private static final String PROPERTIES_GROUP = "Edge Label Properties";
  private static final String ROTATION_ANGLE_STRING = "Rotation Angle (Degrees)";
  private static final String EDGE_LABEL_MODEL_STRING = "Label Model";
  private static final String ALLOW_90_DEGREE_DEVIATION_STRING = "Allow 90 Degree Deviation";
  private static final String AUTO_FLIPPING_STRING = "Auto Flipping";
  private static final String AUTO_ROTATE_STRING = "Auto Rotation";
  private static final String EDGE_TO_LABEL_DISTANCE_STRING = "Edge To Label Distance";

  //edge label model constants
  private static final String MODEL_CENTERED = "Centered";
  private static final String MODEL_TWO_POS = "2 Pos";
  private static final String MODEL_SIX_POS = "6 Pos";
  private static final String MODEL_THREE_POS_CENTER = "3 Pos Center";
  private static final String MODEL_CENTER_SLIDER = "Center Slider";
  private static final String MODEL_SIDE_SLIDER = "Side Slider";
  private static final String[] EDGE_LABEL_MODELS = {
      MODEL_CENTERED, MODEL_TWO_POS, MODEL_SIX_POS, MODEL_THREE_POS_CENTER, MODEL_CENTER_SLIDER, MODEL_SIDE_SLIDER
  };

  private static final String CUSTOM_LABELING_CONFIG_NAME = "CUSTOM_LABELING_CONFIG";
  private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
  private static final int TOOLS_PANEL_WIDTH = 350;

  private DefaultLabelConfiguration customLabelConfig;
  private final OptionHandler optionHandler;

  public EdgeLabelingDemo() {
    this(null);
  }

  public EdgeLabelingDemo(final String helpFilePath) {
    //set view size and create content pane
    view.setPreferredSize(new Dimension(650, 400));
    view.setWorldRect(0, 0, 650, 400);
    view.setFitContentOnResize(true);

    // create the labeling option handler and the content pane
    optionHandler = createOptionHandler();
    contentPane.add(createToolsPanel(helpFilePath), BorderLayout.EAST);

    //load initial graph
    loadGraph("resource/orthogonal.graphml");
  }

  /**
   * Does the label placement using the generic labeling algorithm.
   */
  private void doLabelPlacement() {
    //create a profit model that assigns higher profit to the given angle
    final double rotationAngle = Math.toRadians(optionHandler.getDouble(ROTATION_ANGLE_STRING));
    final DemoProfitModel profitModel = new DemoProfitModel(rotationAngle, 1.0, 0.5);

    //configure and run the layouter
    final GreedyMISLabeling labelLayouter = new GreedyMISLabeling();
    labelLayouter.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
    labelLayouter.setPlaceEdgeLabels(true);
    labelLayouter.setPlaceNodeLabels(false);
    labelLayouter.setProfitModel(profitModel);
    labelLayouter.setCustomProfitModelRatio(0.1);

    new BufferedLayouter(labelLayouter).doLayout(view.getGraph2D());

    view.updateView();
  }

  /**
   * Assigns the current settings of the option handler to all edge labels in the graph and the graph's default edge.
   */
  private void updateEdgeLabels() {
    final Graph2D graph = view.getGraph2D();

    // update auto flipping on all existing labels
    customLabelConfig.setAutoFlippingEnabled(optionHandler.getBool(AUTO_FLIPPING_STRING));

    // get a label model for the current option handler settings
    final CompositeEdgeLabelModel edgeLabelModel = getCurrentEdgeLabelModel();

    // update the label of the default edge 
    final EdgeLabel defaultLabel = graph.getDefaultEdgeRealizer().getLabel();
    defaultLabel.setLabelModel(edgeLabelModel);
    defaultLabel.setModelParameter(edgeLabelModel.getDefaultParameter());

    //... and set it to each edge
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeLabelLayout[] labelLayouts = graph.getEdgeLabelLayout(edge);
      for (int i = 0; i < labelLayouts.length; i++) {
        final EdgeLabel label = (EdgeLabel) labelLayouts[i];

        final Object parameter = label.getLabelModel().createModelParameter(label.getOrientedBox(),
            graph.getEdgeLayout(edge), graph.getNodeLayout(edge.source()), graph.getNodeLayout(edge.target()));

        label.setLabelModel(edgeLabelModel);
        label.setModelParameter(parameter != null ? parameter : edgeLabelModel.getDefaultParameter());
      }
    }
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    //customize label configuration
    final YLabel.Factory factory = EdgeLabel.getFactory();
    final Map defaultConfigImplementationsMap = factory.createDefaultConfigurationMap();
    customLabelConfig = new DefaultLabelConfiguration();
    customLabelConfig.setAutoFlippingEnabled(false);
    defaultConfigImplementationsMap.put(YLabel.Painter.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.Layout.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class, customLabelConfig);
    factory.addConfiguration(CUSTOM_LABELING_CONFIG_NAME, defaultConfigImplementationsMap);

    final EdgeLabel label = view.getGraph2D().getDefaultEdgeRealizer().getLabel();
    label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
    label.setLineColor(LABEL_LINE_COLOR);
  }

  /**
   * Creates an option handler with settings for label model and label size.
   */
  private OptionHandler createOptionHandler() {
    final OptionHandler oh = new OptionHandler("Options");
    oh.addDouble(ROTATION_ANGLE_STRING, 0.0, 0.0, 360.0);
    oh.addBool(AUTO_FLIPPING_STRING, true);
    oh.addEnum(EDGE_LABEL_MODEL_STRING, EDGE_LABEL_MODELS, 4);
    oh.addBool(AUTO_ROTATE_STRING, true);
    oh.addBool(ALLOW_90_DEGREE_DEVIATION_STRING, true);
    oh.addDouble(EDGE_TO_LABEL_DISTANCE_STRING, 5.0, 1.0, 20.0);

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, PROPERTIES_GROUP);
    og.addItem(oh.getItem(ROTATION_ANGLE_STRING));
    og.addItem(oh.getItem(AUTO_FLIPPING_STRING));
    og.addItem(oh.getItem(EDGE_TO_LABEL_DISTANCE_STRING));
    og.addItem(oh.getItem(EDGE_LABEL_MODEL_STRING));
    og.addItem(oh.getItem(AUTO_ROTATE_STRING));
    og.addItem(oh.getItem(ALLOW_90_DEGREE_DEVIATION_STRING));

    ConstraintManager cm = new ConstraintManager(oh);
    //only enable item EDGE_TO_LABEL_DISTANCE_STRING for models that do not place labels on the edge segments
    final String[] nonCenteredModels = {MODEL_TWO_POS, MODEL_SIX_POS, MODEL_SIDE_SLIDER};
    cm.setEnabledOnCondition(cm.createConditionValueIs(EDGE_LABEL_MODEL_STRING, nonCenteredModels),
        oh.getItem(EDGE_TO_LABEL_DISTANCE_STRING));

    cm.setEnabledOnValueEquals(AUTO_ROTATE_STRING, Boolean.TRUE, ALLOW_90_DEGREE_DEVIATION_STRING);

    oh.addChildPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateEdgeLabels();
        doLabelPlacement();
      }
    });

    return oh;
  }

  /**
   * Returns a label model for the current option handler settings.
   */
  private CompositeEdgeLabelModel getCurrentEdgeLabelModel() {
    final byte modelId = getModel(optionHandler.getEnum(EDGE_LABEL_MODEL_STRING));
    final double angle = Math.toRadians(optionHandler.getDouble(ROTATION_ANGLE_STRING));

    final CompositeEdgeLabelModel compositeEdgeLabelModel = new CompositeEdgeLabelModel();
    compositeEdgeLabelModel.add(getEdgeLabelModel(modelId, optionHandler.getBool(AUTO_ROTATE_STRING),
        optionHandler.getDouble(EDGE_TO_LABEL_DISTANCE_STRING), angle));

    if (optionHandler.getBool(ALLOW_90_DEGREE_DEVIATION_STRING)) {
      //add model that creates label candidates for the alternative angle
      final double rotatedAngle = (angle + Math.PI * 0.5) % (2.0 * Math.PI);
      compositeEdgeLabelModel.add(getEdgeLabelModel(modelId, optionHandler.getBool(AUTO_ROTATE_STRING),
          optionHandler.getDouble(EDGE_TO_LABEL_DISTANCE_STRING), rotatedAngle));
    }

    return compositeEdgeLabelModel;
  }

  /**
   * Returns the model type for the specified index.
   */
  private static byte getModel(int index) {
    if (index < 0 || index >= EDGE_LABEL_MODELS.length) {
      return EdgeLabel.SIDE_SLIDER;
    }

    final String modelString = EDGE_LABEL_MODELS[index];
    if (MODEL_CENTERED.equals(modelString)) {
      return EdgeLabel.CENTERED;
    } else if (MODEL_TWO_POS.equals(modelString)) {
      return EdgeLabel.TWO_POS;
    } else if (MODEL_SIX_POS.equals(modelString)) {
      return EdgeLabel.SIX_POS;
    } else if (MODEL_THREE_POS_CENTER.equals(modelString)) {
      return EdgeLabel.THREE_CENTER;
    } else if (MODEL_CENTER_SLIDER.equals(modelString)) {
      return EdgeLabel.CENTER_SLIDER;
    } else {
      return EdgeLabel.SIDE_SLIDER;
    }
  }

  /**
   * Creates and configures an edge label model using the given parameter.
   */
  private static EdgeLabelModel getEdgeLabelModel(byte modelId, boolean autoRotationEnabled, double distance,
                                                  double angle) {
    if (modelId == EdgeLabel.CENTER_SLIDER || modelId == EdgeLabel.SIDE_SLIDER) {
      final byte mode = (modelId == EdgeLabel.CENTER_SLIDER) ?
          RotatedSliderEdgeLabelModel.CENTER_SLIDER :
          RotatedSliderEdgeLabelModel.SIDE_SLIDER;
      RotatedSliderEdgeLabelModel elm = new RotatedSliderEdgeLabelModel(mode);
      elm.setAutoRotationEnabled(autoRotationEnabled);
      if (distance < 1.0 && modelId == EdgeLabel.SIDE_SLIDER) {
        elm.setDistance(1.0); //setting distance to 0 would automatically switch to CENTER_SLIDER model
      } else {
        elm.setDistance(distance);
      }
      elm.setAngle(angle);
      elm.setDistanceRelativeToEdge(true);
      return elm;
    } else {
      final int mode;
      if (modelId == EdgeLabel.TWO_POS) {
        mode = RotatedDiscreteEdgeLabelModel.TWO_POS;
      } else if (modelId == EdgeLabel.CENTERED) {
        mode = RotatedDiscreteEdgeLabelModel.CENTERED;
      } else if (modelId == EdgeLabel.THREE_CENTER) {
        mode = RotatedDiscreteEdgeLabelModel.THREE_CENTER;
      } else {
        mode = RotatedDiscreteEdgeLabelModel.SIX_POS; //default value
      }
      RotatedDiscreteEdgeLabelModel elm = new RotatedDiscreteEdgeLabelModel(mode);
      elm.setAutoRotationEnabled(autoRotationEnabled);
      elm.setAngle(angle);
      elm.setDistance(distance);
      elm.setPositionRelativeToSegment(true);
      return elm;
    }
  }

  /**
   * Creates the tools panel containing the settings and the help panel.
   */
  private JPanel createToolsPanel(String helpFilePath) {
    JPanel toolsPanel = new JPanel(new BorderLayout());
    toolsPanel.add(createOptionHandlerComponent(optionHandler), BorderLayout.NORTH);

    if (helpFilePath != null) {
      final URL url = getClass().getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        JComponent helpPane = createHelpPane(url);
        if (helpPane != null) {
          helpPane.setMinimumSize(new Dimension(200, 200));
          helpPane.setPreferredSize(new Dimension(TOOLS_PANEL_WIDTH, 400));
          toolsPanel.add(helpPane, BorderLayout.CENTER);
        }
      }
    }

    return toolsPanel;
  }

  /**
   * Create a menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu menu = new JMenu("File");
    menu.add(new ExitAction());
    menuBar.add(menu);

    menu = new JMenu("Sample Graphs");
    menuBar.add(menu);

    menu.add(new AbstractAction("Orthogonal Graph") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/orthogonal.graphml");
      }
    });
    menu.add(new AbstractAction("Organic Graph") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/organic.graphml");
      }
    });

    return menuBar;
  }

  /**
   * Creates an EditMode and adds a popup mode that displays the demo context menu.
   */
  protected void registerViewModes() {
    EditMode mode = new EditMode();
    mode.setPopupMode(new DemoPopupMode());
    view.addViewMode(mode);
  }

  /**
   * Creates the default view actions but removes the mnemonic for label editing since it is complicated to update the
   * model if a new label is created by such an edit.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    view.getCanvasComponent().getActionMap().remove(Graph2DViewActions.EDIT_LABEL);
  }

  /**
   * Creates the default tool bar and adds additional buttons for undo, redo and label placement.
   */
  protected JToolBar createToolBar() {
    final JToolBar bar = super.createToolBar();
    bar.addSeparator();

    Graph2DUndoManager undoManager = new Graph2DUndoManager(view.getGraph2D());
    undoManager.setViewContainer(view);

    //add undo action to toolbar
    Action action = undoManager.getUndoAction();
    action.putValue(Action.SMALL_ICON,
        new ImageIcon(DemoBase.class.getResource("resource/undo.png")));
    action.putValue(Action.SHORT_DESCRIPTION, "Undo");
    bar.add(action);

    //add redo action to toolbar
    action = undoManager.getRedoAction();
    action.putValue(Action.SMALL_ICON,
        new ImageIcon(DemoBase.class.getResource("resource/redo.png")));
    action.putValue(Action.SHORT_DESCRIPTION, "Redo");
    bar.add(action);
    bar.addSeparator();

    //the layout button
    bar.add(new LayoutAction());

    return bar;
  }

  /**
   * Loads a graph and applies the label configuration to the existing labels.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    DemoDefaults.applyRealizerDefaults(view.getGraph2D());
    final Graph2D graph = view.getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final EdgeLabelLayout[] labelLayouts = graph.getEdgeLabelLayout(ec.edge());
      for (int i = 0; i < labelLayouts.length; i++) {
        final EdgeLabel label = (EdgeLabel) labelLayouts[i];
        label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
      }
    }

    updateEdgeLabels();
    doLabelPlacement();
  }

  class DemoPopupMode extends PopupMode {

    public JPopupMenu getEdgePopup(final Edge edge) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Add Label") {

        public void actionPerformed(ActionEvent e) {
          if (edge == null) {
            return;
          }

          final CompositeEdgeLabelModel edgeLabelModel = getCurrentEdgeLabelModel();
          final EdgeLabel label = new EdgeLabel("Label");
          label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
          label.setLabelModel(edgeLabelModel);
          label.setModelParameter(edgeLabelModel.getDefaultParameter());
          view.getGraph2D().getRealizer(edge).addLabel(label);

          final Object parameter = calculateBestParameter(edge, label);
          if (parameter != null) {
            label.setModelParameter(parameter);
          }

          view.getGraph2D().updateViews();
        }
      });
      return pm;
    }

    public JPopupMenu getEdgeLabelPopup(final EdgeLabel label) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Edit Label") {

        public void actionPerformed(ActionEvent e) {
          if (label != null) {
            EdgeLabelPropertyHandler ph = new EdgeLabelPropertyHandler(label, view);
            ph.showEditor(null, OptionHandler.OK_APPLY_CANCEL_BUTTONS);
          }
        }
      });
      return pm;
    }

    /**
     * Determines a model parameter that represents a label position that is not already occupied by another label and
     * lays near the current mouse position.
     */
    private Object calculateBestParameter(Edge e, EdgeLabelLayout eLayout) {
      if (lastPressEvent == null) {
        return null;
      }

      //identify occupied boxes
      YList occupiedRectList = new YList();
      LayoutGraph graph = view.getGraph2D();
      EdgeLabelLayout[] ell = graph.getEdgeLabelLayout(e);
      for (int i = 0; i < ell.length; i++) {
        if (ell[i] != eLayout) {
          occupiedRectList.add(ell[i].getOrientedBox().getBoundingBox());
        }
      }

      //find label candidates with non-occupied boxes and low distance to point "mousePressedEventLocation"
      final YPoint mousePressedEventLocation = new YPoint(view.toWorldCoordX(lastPressEvent.getX()),
          view.toWorldCoordY(lastPressEvent.getY()));
      double minDist = Double.MAX_VALUE;
      LabelCandidate chosen = null;
      YList candidates = eLayout.getLabelModel().getLabelCandidates(eLayout, graph.getEdgeLayout(e),
          graph.getNodeLayout(e.source()), graph.getNodeLayout(e.target()));
      for (YCursor cu = candidates.cursor(); cu.ok(); cu.next()) {
        LabelCandidate lc = (LabelCandidate) cu.current();

        boolean isOccupied = false;
        YRectangle bBox = lc.getBox().getBoundingBox();
        for (YCursor cur = occupiedRectList.cursor(); cur.ok() && !isOccupied; cur.next()) {
          isOccupied = YRectangle.intersects(bBox, (YRectangle) cur.current());
        }

        if (!isOccupied) {
          double dist = YPoint.distance(mousePressedEventLocation, lc.getBox().getCenter());
          if (dist < minDist) {
            minDist = dist;
            chosen = lc;
          }
        }
      }

      return chosen == null ? null : chosen.getModelParameter();
    }
  }

  /**
   * Performs the generic labeling.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Place Labels");
      putValue(Action.SHORT_DESCRIPTION, "Place labels");
//      putValue(Action.SMALL_ICON, new ImageIcon(DemoBase.class.getResource("resource/layout.png")));
    }

    public void actionPerformed(ActionEvent e) {
      doLabelPlacement();
    }
  }

  /**
   * Creates a component for the specified option handler using the default editor factory and sets all of its items to
   * auto adopt and auto commit.
   */
  private static JComponent createOptionHandlerComponent(OptionHandler oh) {
    final DefaultEditorFactory defaultEditorFactory = new DefaultEditorFactory();
    final Editor editor = defaultEditorFactory.createEditor(oh);

    //propagate auto adopt and auto commit to editor and its children
    final ArrayList stack = new ArrayList();
    stack.add(editor);
    while (!stack.isEmpty()) {
      Object editorObj = stack.remove(stack.size() - 1);
      if (editorObj instanceof ItemEditor) {
        ((ItemEditor) editorObj).setAutoAdopt(true);
        ((ItemEditor) editorObj).setAutoCommit(true);
      }
      if (editorObj instanceof CompoundEditor) {
        for (Iterator iter = ((CompoundEditor) editorObj).editors(); iter.hasNext();) {
          stack.add(iter.next());
        }
      }
    }

    //build and return component
    JComponent optionComponent = editor.getComponent();
    optionComponent.setMinimumSize(new Dimension(200, 50));
    return optionComponent;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new EdgeLabelingDemo("resource/edgelabelingdemohelp.html")).start("Edge Labeling Demo");
      }
    });
  }
}