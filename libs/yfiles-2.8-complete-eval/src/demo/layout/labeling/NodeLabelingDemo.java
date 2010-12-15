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
import y.base.Node;
import y.base.NodeCursor;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.NodeLabelLayout;
import y.layout.BufferedLayouter;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.ItemEditor;
import y.option.CompoundEditor;
import y.view.DefaultBackgroundRenderer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;
import y.view.DefaultLabelConfiguration;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.geom.LineSegment;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.geom.Line2D;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This demo shows how to configure node labels and the corresponding label models as well as how to apply the
 * generic node label placement algorithm.
 *
 * A new city (node) can be added by left-clicking on the corresponding map location. To edit a node label right-click
 * on the label or the corresponding node and choose item "Edit Label". Node labels can be moved to another valid
 * position according to the current label model by using drag and drop.
 *
 * To manually start the generic labeling algorithm click on the "Do Generic Labeling" button. Note: after changing one
 * of the properties stated below, the generic labeling algorithm is applied automatically.
 *
 */
public class NodeLabelingDemo extends DemoBase {
  private static final String NODE_LABEL_MODEL_STRING = "Label Model";
  private static final String LABEL_SIZE_STRING = "Label Size";
  private static final String PROPERTIES_GROUP = "Node Label Properties";

  //node label model constants
  private static final String MODEL_CORNERS = "Corners";  
  private static final String MODEL_SANDWICH = "Sandwich";
  private static final String MODEL_SIDE = "Side";
  private static final String MODEL_FREE = "Free";
  private static final String MODEL_EIGHT_POS = "8 Pos";
  private static final String[] NODE_LABEL_MODELS = {
      MODEL_CORNERS, MODEL_SANDWICH, MODEL_SIDE, MODEL_FREE, MODEL_EIGHT_POS
  };

  private static final int TOOLS_PANEL_WIDTH = 350;

  private final OptionHandler optionHandler;

  public NodeLabelingDemo() {
    this(null);
  }

  public NodeLabelingDemo(final String helpFilePath) {
    // render a map of the USA in the background
    DefaultBackgroundRenderer renderer = new DefaultBackgroundRenderer(view);
    URL bgImage = getClass().getResource("resource/usamap.gif");
    renderer.setImageResource(bgImage);
    renderer.setMode(DefaultBackgroundRenderer.DYNAMIC);
    renderer.setColor(Color.white);
    view.setBackgroundRenderer(renderer);
    view.setPreferredSize(new Dimension(650, 400));
    view.setWorldRect(0, 0, 650, 400);

    // create the labeling option handler and the content pane
    optionHandler = createOptionHandler();
    contentPane.add(createToolsPanel(helpFilePath), BorderLayout.EAST);

    loadGraph(getClass().getResource("resource/uscities.graphml"));

    // do initial label placement
    doLabelPlacement();
  }

  /**
   * Does the label placement using the generic labeling algorithm. Before this, the model and size of the labels is
   * set according to the option handlers settings.
   */
  private void doLabelPlacement() {
    // update node label model as well as node label size
    final Graph2D graph = view.getGraph2D();
    final byte modelId = getModel(optionHandler.getEnum(NODE_LABEL_MODEL_STRING));
    final int labelSize = optionHandler.getInt(LABEL_SIZE_STRING);
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      NodeLabelLayout[] nll = graph.getNodeLabelLayout(nc.node());
      for (int i = 0; i < nll.length; i++) {
        ((NodeLabel) nll[i]).setModel(modelId);
        ((NodeLabel) nll[i]).setFontSize(labelSize);
      }
    }

    // update the default node realizer
    final NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
    defaultNodeRealizer.getLabel().setFontSize(labelSize);
    defaultNodeRealizer.getLabel().setModel(modelId);

    // configure and run the layouter
    final GreedyMISLabeling labelLayouter = new GreedyMISLabeling();
    labelLayouter.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
    labelLayouter.setPlaceEdgeLabels(false);
    labelLayouter.setPlaceNodeLabels(true);
    labelLayouter.setApplyPostprocessing(true);

    new BufferedLayouter(labelLayouter).doLayout(view.getGraph2D());

    view.updateView();
  }

  /**
   * Creates an option handler with settings for label model and label size.
   */
  private OptionHandler createOptionHandler() {
    final OptionHandler oh = new OptionHandler("Options");
    oh.addEnum(NODE_LABEL_MODEL_STRING, NODE_LABEL_MODELS, 2);
    oh.addInt(LABEL_SIZE_STRING, 12, 10, 25);

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, PROPERTIES_GROUP);
    og.addItem(oh.getItem(NODE_LABEL_MODEL_STRING));
    og.addItem(oh.getItem(LABEL_SIZE_STRING));

    oh.addChildPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        //apply generic labeling after each change
        doLabelPlacement();
      }
    });

    return oh;
  }

  /**
   * Returns the model type for the specified index.
   */
  private static byte getModel(int index) {
    if (index < 0 || index >= NODE_LABEL_MODELS.length) {
      return NodeLabel.SANDWICH;
    }

    final String modelString = NODE_LABEL_MODELS[index];
    if (MODEL_CORNERS.equals(modelString)) {
      return NodeLabel.CORNERS;
    } else if (MODEL_EIGHT_POS.equals(modelString)) {
      return NodeLabel.EIGHT_POS;
    } else if (MODEL_FREE.equals(modelString)) {
      return NodeLabel.FREE;
    } else if (MODEL_SIDE.equals(modelString)) {
      return NodeLabel.SIDES;
    } else {
      return NodeLabel.SANDWICH;
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

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    //customize label configuration
    final YLabel.Factory factory = NodeLabel.getFactory();
    final Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(YLabel.Painter.class, new MyPainter());
    factory.addConfiguration("Customized", implementationsMap);

    //set customized configuration as default
    NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
    nodeRealizer.setSize(10.0, 10.0);
    nodeRealizer.getLabel().setText("City");
    nodeRealizer.getLabel().setConfiguration("Customized");
  }

  protected EditMode createEditMode() {
    //configure edit mode
    final EditMode mode = super.createEditMode();
    mode.allowEdgeCreation(false);
    mode.allowMoveSelection(false);
    mode.setSnappingEnabled(false);
    mode.allowResizeNodes(false);
    mode.setPopupMode(new DemoPopupMode());
    return mode;
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(new LayoutAction());
    return bar;
  }

  /**
   * Loads a graph and applies the label configuration to the existing labels.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    final Graph2D graph2D = view.getGraph2D();
    DemoDefaults.applyRealizerDefaults(graph2D);
    for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
      final NodeLabelLayout[] nll = graph2D.getNodeLabelLayout(nc.node());
      for (int i = 0; i < nll.length; i++) {
        ((NodeLabel) nll[i]).setConfiguration("Customized");
      }
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
   * Customized popup mode.
   */
  class DemoPopupMode extends PopupMode {
    /**
     * Popup menu for a hit node
     */
    public JPopupMenu getNodePopup(Node v) {
      JPopupMenu pm = new JPopupMenu();
      NodeRealizer r = this.view.getGraph2D().getRealizer(v);
      YLabel label = r.getLabel();
      pm.add(new EditLabel(label));
      return pm;
    }

    /**
     * Popup menu for a hit node label
     */
    public JPopupMenu getNodeLabelPopup(NodeLabel label) {
      JPopupMenu pm = new JPopupMenu();      
      pm.add(new EditLabel(label));
      return pm;
    }
  }

  /**
   * Opens a text editor for the specified label.
   */
  class EditLabel extends AbstractAction {
    YLabel label;

    EditLabel(YLabel l) {
      super("Edit Label");
      label = l;
    }

    public void actionPerformed(ActionEvent e) {
      view.openLabelEditor(label, label.getTextLocation().getX(), label.getTextLocation().getY());     
    }
  }

  /**
   * A simple YLabel.Painter implementation that reuses most of the default painting behavior from
   * DefaultLabelConfiguration and additionally draws a line between the node and its label.
   */
  static final class MyPainter extends DefaultLabelConfiguration {
    /** Overwrite the painting of the background only. */
    public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
      super.paintBox(label, gfx, x, y, width, height);
      if (label instanceof NodeLabel) {
        //determine the line connecting the node center with the center of the corresponding node label
        final Node node = ((NodeLabel) label).getNode();
        final Graph2D graph2D = ((Graph2D) node.getGraph());
        final LineSegment connectingLine = new LineSegment(new YPoint(x + width * 0.5d, y + height * 0.5d),
            graph2D.getCenter(node));

        //determine start/end point of the line (project the connecting line onto the label/node box)
        final YRectangle labelBox = new YRectangle(x, y, width, height);
        YPoint startPoint = calcBorderIntersectionPoints(labelBox, connectingLine);
        final YRectangle nodeBox = graph2D.getRectangle(node);
        YPoint endPoint = calcBorderIntersectionPoints(nodeBox, connectingLine);

        //draw the line
        if (startPoint != null && endPoint != null) {
          Line2D line = new Line2D.Double(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
          gfx.setColor(new Color(0, 0, 0, 150));
          gfx.draw(line);
        }
      }
    }
  }

  /**
   * Creates a component for the specified option handler using the default editor factory and sets all of its items
   * to auto adopt and auto commit.
   */
  private static JComponent createOptionHandlerComponent(OptionHandler oh) {
    final DefaultEditorFactory defaultEditorFactory = new DefaultEditorFactory();
    final Editor editor = defaultEditorFactory.createEditor(oh);

    //propagate auto adopt and auto commit to editor and its children
    final ArrayList stack = new ArrayList();
    stack.add(editor);
    while(!stack.isEmpty()) {
      Object editorObj = stack.remove(stack.size() - 1);
      if(editorObj instanceof ItemEditor) {
        ((ItemEditor) editorObj).setAutoAdopt(true);
        ((ItemEditor) editorObj).setAutoCommit(true);
      }
      if(editorObj instanceof CompoundEditor) {
        for (Iterator iter = ((CompoundEditor) editorObj).editors(); iter.hasNext(); ) {
          stack.add(iter.next());
        }
      }
    }

    //build and return component
    JComponent optionComponent = editor.getComponent();
    optionComponent.setMinimumSize(new Dimension(200, 50));
    return optionComponent;
  }

  /**
   * Calculates the intersection point between the given line segment l and the given rectangle r.
   * We assume that at least one endpoint lies inside r -> at most one intersection point.
   */
  private static YPoint calcBorderIntersectionPoints(YRectangle r, LineSegment l) {
    if(!r.contains(l.getFirstEndPoint()) && !r.contains(l.getSecondEndPoint())) {
      throw new RuntimeException("Input no valid!");
    }    

    //check if l intersects a side of r
    final YPoint[] rCorners = new YPoint[4]; 
    rCorners[0] = r.getLocation();
    rCorners[1] = new YPoint(rCorners[0].x, rCorners[0].y + r.getHeight());
    rCorners[2] = new YPoint(rCorners[1].x + r.getWidth(), rCorners[1].y);
    rCorners[3] = new YPoint(rCorners[2].x, rCorners[0].y);
    for(int i = 0; i < rCorners.length; i++) {
      final LineSegment rSide = new LineSegment(rCorners[i], rCorners[(i + 1) % 4]);
      YPoint intersectionPoint = LineSegment.getIntersection(rSide, l);
      if(intersectionPoint != null) {
        return intersectionPoint; //found the intersection
      }
    }
    
    //check special case were l intersects a corner of the rectangle
    for(int i = 0; i < rCorners.length; i++) {
      if(l.intersects(rCorners[i])) {
        return rCorners[i];
      }
    }

    return null; //no intersection
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new NodeLabelingDemo("resource/nodelabelingdemohelp.html")).start("Labeling Demo");
      }
    });
  }
}


      
