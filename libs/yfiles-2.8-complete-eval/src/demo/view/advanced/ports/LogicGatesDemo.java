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
package demo.view.advanced.ports;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import demo.view.application.DragAndDropDemo;
import y.base.Edge;
import y.base.Node;
import y.base.YList;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.layout.LayoutOrientation;                            //
import y.layout.hierarchic.IncrementalHierarchicLayouter;     //
import y.view.Arrow;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.LineType;
import y.view.MovePortMode;
import y.view.NodePort;
import y.view.NodeScaledPortLocationModel;
import y.view.NodeRealizer;
import y.view.HitInfo;
import y.view.Drawable;
import y.view.AbstractCustomNodePainter;
import y.view.YRenderingHints;
import y.view.Graph2DTraversal;
import y.view.Graph2DLayoutExecutor;                          //

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Demonstrates how to create an application that makes use of nodes that have a fixed set of ports.
 * The nodes represent logic gates. Edges can only start at the ports located at the right side of nodes.
 * These are the outputs of the represented gates. Edges can only end at ports located at the left side
 * of the nodes. These are the inputs of the represented gates. Valid ports will be highlighted when
 * hovering over a node or when creating an edge. The nodes are realized using GenericNodeRealizers and the
 * ports are realized using NodePorts.
 */
public class LogicGatesDemo extends DemoBase {
  private static final String AND_GATE_NODE_CONFIGURATION = "AndGateNodeConfiguration";
  private static final String NAND_GATE_NODE_CONFIGURATION = "NandGateNodeConfiguration";
  private static final String NOT_GATE_NODE_CONFIGURATION = "NotGateNodeConfiguration";
  private static final String PORT_CONFIG_INPUT = "INPUT_PORT";
  private static final String PORT_CONFIG_OUTPUT = "OUTPUT_PORT";
  private static final Color PORT_HIGHLIGHT_COLOR = Color.GREEN;
  private static final Color LINE_COLOR = Color.BLACK;
  private static final LineType LINE_TYPE = LineType.LINE_2;

  public LogicGatesDemo() {
    this(null);
  }

  public LogicGatesDemo( final String helpFilePath ) {
    //load the default graph of the demo
    loadGraph("resource/LogicGatesDemo.graphml");
    addHelpPane(helpFilePath);
  }

  // For running a layout the node ports have to be translated to strong port      //
  // constraints for the edges connecting to the node ports since node ports are a //
  // view-only feature. The node port configurator used by the layout executor     //
  // provides a setting for doing this translation.                                //
  protected JToolBar createToolBar() {                                             //
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter(); //
    ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);                     //
    ihl.setOrthogonallyRouted(true);                                               //
    final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();            //
    // Set up port constraints for the node ports before performing the layout.    //
    executor.getNodePortConfigurator().setAutomaticPortConstraintsEnabled(true);   //
    final JToolBar jtb = super.createToolBar();                                    //
    jtb.addSeparator();                                                            //
    jtb.add(new AbstractAction("Layout") {                                         //
      public void actionPerformed(ActionEvent e) {                                 //
        executor.doLayout(view, ihl);                                              //
      }                                                                            //
    });                                                                            //
    return jtb;                                                                    //
  }                                                                                //

  protected void initialize() {
    initializePortConfiguration();

    // Configuration for Logic Gate Symbols
    List gatesNRList = new ArrayList();
    GenericNodeRealizer.Factory gnrFactory = GenericNodeRealizer.getFactory();

    // AND Gate Configuration
    Map andSymbolConfMap = gnrFactory.createDefaultConfigurationMap();
    AndGateConfiguration andGateHandler = new AndGateConfiguration(true);
    andSymbolConfMap.put(GenericNodeRealizer.Painter.class, andGateHandler);
    andSymbolConfMap.put(GenericNodeRealizer.ContainsTest.class, andGateHandler);
    gnrFactory.addConfiguration(AND_GATE_NODE_CONFIGURATION, andSymbolConfMap);
    GenericNodeRealizer andRealizer = new GenericNodeRealizer(AND_GATE_NODE_CONFIGURATION);
    andRealizer.setSize(100, 50);
    andRealizer.setLineColor(Color.BLACK);
    andRealizer.setLineType(LINE_TYPE);
    andRealizer.setFillColor(Color.WHITE);
    andRealizer.setFillColor2(DemoDefaults.DEFAULT_CONTRAST_COLOR);
    addPort(andRealizer, new YPoint(-0.5, -0.25), PORT_CONFIG_INPUT);
    addPort(andRealizer, new YPoint(-0.5,  0.25), PORT_CONFIG_INPUT);
    addPort(andRealizer, new YPoint( 0.5,  0.0),  PORT_CONFIG_OUTPUT);
    gatesNRList.add(andRealizer);

    // NAND Gate Configuration
    Map nandSymbolConfMap = gnrFactory.createDefaultConfigurationMap();
    AndGateConfiguration nandGateHandler = new AndGateConfiguration(false);
    nandSymbolConfMap.put(GenericNodeRealizer.Painter.class, nandGateHandler);
    nandSymbolConfMap.put(GenericNodeRealizer.ContainsTest.class, nandGateHandler);
    gnrFactory.addConfiguration(NAND_GATE_NODE_CONFIGURATION, nandSymbolConfMap);
    GenericNodeRealizer nandRealizer = new GenericNodeRealizer(NAND_GATE_NODE_CONFIGURATION);
    nandRealizer.setSize(100, 50);
    nandRealizer.setLineColor(Color.BLACK);
    nandRealizer.setLineType(LINE_TYPE);
    nandRealizer.setFillColor(Color.WHITE);
    nandRealizer.setFillColor2(DemoDefaults.DEFAULT_CONTRAST_COLOR);
    addPort(nandRealizer, new YPoint(-0.5, -0.25), PORT_CONFIG_INPUT);
    addPort(nandRealizer, new YPoint(-0.5,  0.25), PORT_CONFIG_INPUT);
    addPort(nandRealizer, new YPoint( 0.5,  0.0),  PORT_CONFIG_OUTPUT);
    gatesNRList.add(nandRealizer);

    // NOT Gate Configuration
    Map notSymbolConfMap = gnrFactory.createDefaultConfigurationMap();
    NotGateConfiguration notGateHandler = new NotGateConfiguration();
    notSymbolConfMap.put(GenericNodeRealizer.Painter.class, notGateHandler);
    notSymbolConfMap.put(GenericNodeRealizer.ContainsTest.class, notGateHandler);
    gnrFactory.addConfiguration(NOT_GATE_NODE_CONFIGURATION, notSymbolConfMap);
    GenericNodeRealizer notRealizer = new GenericNodeRealizer(NOT_GATE_NODE_CONFIGURATION);
    notRealizer.getLabel().setInsets(new Insets(0, 0, 0, 15));
    notRealizer.setSize(100, 50);
    notRealizer.setLineColor(Color.BLACK);
    notRealizer.setLineType(LINE_TYPE);
    notRealizer.setFillColor(Color.WHITE);
    notRealizer.setFillColor2(DemoDefaults.DEFAULT_CONTRAST_COLOR);
    addPort(notRealizer, new YPoint(-0.5, 0.0), PORT_CONFIG_INPUT);
    addPort(notRealizer, new YPoint( 0.5, 0.0), PORT_CONFIG_OUTPUT);
    gatesNRList.add(notRealizer);

    final Graph2D graph = this.view.getGraph2D();

    final GenericNodeRealizer[] logicSymbols = new GenericNodeRealizer[gatesNRList.size()];
    gatesNRList.toArray(logicSymbols);

    // Set default edge realizer configuration.
    EdgeRealizer er = graph.getDefaultEdgeRealizer();
    er.setLineType(LINE_TYPE);
    er.setLineColor(LINE_COLOR);
    er.setArrow(Arrow.NONE);

    // Create drag and drop support.
    DragAndDropDemo.DragAndDropSupport dragAndDropSupport = new DragAndDropDemo.DragAndDropSupport(logicSymbols, view);
    dragAndDropSupport.configureSnapping(true, 30, 20, true);

    // The default NodeRealizer depends on list selection in the drag and drop list.
    final JList list = dragAndDropSupport.getList();
    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        graph.setDefaultNodeRealizer(logicSymbols[list.getSelectedIndex()]);
      }
    });

    // Select the first element in the list.
    list.setSelectedIndex(0);
    JScrollPane scrollPane = new JScrollPane(list);

    contentPane.add(scrollPane, BorderLayout.WEST);
  }

  private static void addPort(GenericNodeRealizer owner, YPoint relativeOffsets, String portConfiguration) {
    NodePort port = new NodePort();
    owner.addPort(port);
    port.setConfiguration(portConfiguration);
    port.setModelParameter(new NodeScaledPortLocationModel().createScaledParameter(relativeOffsets));
  }

  private void initializePortConfiguration() {
    final HashMap portImpls = new HashMap();

    // Since the bounds of a node port depend very much on its owning node realizer, the handler for
    // the realizer also implements the NodePort.BoundsProvider interface.
    portImpls.put(NodePort.BoundsProvider.class, new NodePort.BoundsProvider() {
      public YRectangle getBounds(NodePort port) {
        return getLogicGateHandler(port).getBounds(port);
      }

      private LogicGateConfiguration getLogicGateHandler(NodePort port) {
        String configuration = ((GenericNodeRealizer) port.getRealizer()).getConfiguration();
        GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
        return (LogicGateConfiguration) factory.getImplementation(configuration, GenericNodeRealizer.ContainsTest.class);
      }
    });
    // The node ports are drawn like edges.
    portImpls.put(NodePort.Painter.class, new NodePort.Painter() {
      public void paint(NodePort port, Graphics2D gfx) {
        YRectangle bounds = port.getBounds();
        // Use the same stroke and color as for edges.
        gfx.setStroke(LINE_TYPE);
        gfx.setColor(LINE_COLOR);
        gfx.draw(new Line2D.Double(bounds.getX(), bounds.getY(), bounds.getX() + bounds.getWidth(), bounds.getY()));
      }
    });
    NodePort.getFactory().addConfiguration(PORT_CONFIG_INPUT, portImpls);
    NodePort.getFactory().addConfiguration(PORT_CONFIG_OUTPUT, portImpls);
  }

  protected EditMode createEditMode() {
    EditMode editMode = new HighlightOutputPortsEditMode();
    editMode.setMoveNodePortMode(null);
    editMode.allowMovePorts(true);
    editMode.setOrthogonalEdgeRouting(true);
    editMode.assignNodeLabel(false);

    // Start new edges only at output ports and end them only at input ports.
    CreateEdgeMode createEdgeMode = new CreateEdgeMode() {
      protected boolean acceptSourceNodePort(Node node, NodePort port, double x, double y) {
        // Accept a port for starting an edge, if it is an output of its gate.
        return isOutput(port);
      }
      protected boolean acceptTargetNodePort(Node node, NodePort port, double x, double y) {
        // Accept a port for ending an edge, if it is an input of its gate with no adjacent edges so far
        return isInput(port);
      }
      protected void drawTargetPortIndicator(Graphics2D gfx, final NodePort port) {
        // Do not use the standard visual for drawing target (input) ports. Use the highlightPort
        // method instead which is also used by the HighlightOutputPortsEditMode and MovePortMode.
        if (isInput(port)) {
          highlightPort(gfx, port);
        }
      }
    };
    createEdgeMode.setIndicatingTargetNode(true);
    createEdgeMode.setOrthogonalEdgeCreation(true);
    editMode.setCreateEdgeMode(createEdgeMode);

    // Ensure that the aspect ratio of a gate is kept on resizing.
    HotSpotMode hotSpotMode = new HotSpotMode() {
      protected boolean isModifierPressed(MouseEvent me) {
        return true;
      }
    };
    editMode.setHotSpotMode(hotSpotMode);

    // Ensure that the restriction on edges is kept while reassigning their endpoints.
    final MovePortMode mpm = new MovePortMode() {
      protected YList getPortCandidates(Node v, Edge e, double gridSpacing) {
        YList result = new YList();
        NodeRealizer nr = getGraph2D().getRealizer(v);
        // Do we move the target port of the edge?
        boolean newTarget = port == port.getOwner().getTargetPort();
        for (int i = 0; i < nr.portCount(); i++) {
          final NodePort nodePort = nr.getPort(i);
          if ((newTarget && isInput(nodePort)) || (!newTarget && isOutput(nodePort))) {
            result.add(nodePort.getLocation());
          }
        }
        return result;
      }

      protected void drawPortCandidate(Graphics2D gfx, YPoint p, Node v, Edge e, boolean isSnapActive,
                                       boolean isSnapCandidate) {
        // Draw port candidates like the EditMode/CreateEdgeMode.
        if (isSnapCandidate) {
          HitInfo info = view.getHitInfoFactory().createHitInfo(p.x, p.y, Graph2DTraversal.NODE_PORTS, true);
          NodePort hitNodePort = info.getHitNodePort();
          if (hitNodePort != null && isInput(hitNodePort)) {
            highlightPort(gfx, hitNodePort);
          }
        }
      }
    };
    mpm.setChangeEdgeEnabled(true);
    mpm.setUsingRealizerPortCandidates(true);
    mpm.setUsingNodePortCandidates(true);
    mpm.setSegmentSnappingEnabled(true);
    editMode.setMovePortMode(mpm);

    return editMode;
  }

  private boolean isInput(NodePort port) {
    return port.getConfiguration().equals(PORT_CONFIG_INPUT);
  }

  private boolean isOutput(NodePort port) {
    return port.getConfiguration().equals(PORT_CONFIG_OUTPUT);
  }

  // This method is used to highlight candidate ports by the custom view modes.
  public static void highlightPort(Graphics2D gfx, NodePort port1) {
    final YRectangle bounds = port1.getBounds();
    Rectangle2D.Double box = new Rectangle2D.Double(
        bounds.getX(), bounds.getY() - 2, bounds.getWidth(), bounds.getHeight() + 4);
    Color oldColor = gfx.getColor();
    gfx.setColor(PORT_HIGHLIGHT_COLOR);
    gfx.fill(box);
    gfx.setColor(oldColor);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new LogicGatesDemo("resource/logicgateshelp.html")).start();
      }
    });
  }

  /** This EditMode highlights output ports when hovering over them. */
  private class HighlightOutputPortsEditMode extends EditMode {
    private Node previousNode;
    private ArrayList portHighlightDrawables;

    public HighlightOutputPortsEditMode() {
      super();
      portHighlightDrawables = new ArrayList();
    }

    public void mousePressed(MouseEvent e) {
      unhighlightPorts();
      super.mousePressed(e);
    }

    public void mouseMoved(double x, double y) {
      super.mouseMoved(x, y);
      final HitInfo hi = getHitInfo(x, y);
      if (hi.hasHitNodes()) {
        // The mouse is over a node.
        final Node node = hi.getHitNode();
        final Graph2D graph = view.getGraph2D();
        final NodeRealizer nr = graph.getRealizer(node);
        if ((node != previousNode) && !nr.isSelected()) {
          // Highlight output ports of the hit node.
          unhighlightPorts();
          for (int i = 0; i < nr.portCount(); i++) {
            final NodePort port = nr.getPort(i);
            if (isOutput(port)) {
              highlight(port);
            }
          }
          view.updateView();
          previousNode = node;
        }
      } else {
        previousNode = null;
        unhighlightPorts();
      }
    }

    private void highlight(NodePort port) {
      PortHighlightDrawable portHighlightDrawable = new PortHighlightDrawable(port);
      portHighlightDrawables.add(portHighlightDrawable);
      view.addDrawable(portHighlightDrawable);
    }

    private void unhighlightPorts() {
      if (!portHighlightDrawables.isEmpty()) {
        for (Iterator ports = portHighlightDrawables.iterator(); ports.hasNext();) {
          PortHighlightDrawable currPortHighlightDrawable = (PortHighlightDrawable) ports.next();
          view.removeDrawable(currPortHighlightDrawable);
        }
        portHighlightDrawables.clear();
        view.updateView();
      }
    }
  }

  /** This drawable is used by the HighlightOutputPortsEditMode. */
  private static final class PortHighlightDrawable implements Drawable {
    private NodePort port;

    public PortHighlightDrawable(NodePort port) {
      this.port = port;
    }

    public void paint(Graphics2D gfx) {
      highlightPort(gfx, port);
    }

    public Rectangle getBounds() {
      YRectangle rect = port.getBounds();
      return new Rectangle((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
    }
  }

  /**
   * This is an abstract base class for handling visual aspects of logic gates. It is responsible for
   * painting the gate symbol (it extends AbstractCustomNodePainter) and provides a contains test (by
   * implementing GenericNodeRealizer.ContainsTest). Moreover it also provides the bounds for the input
   * and output node ports.
   */
  private abstract static class LogicGateConfiguration
      extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {

    // This is the height of the bounds of a node port (see getBounds(NodePort)). If the bounds
    // have height zero, node ports cannot be found by a hit test.
    private static final double REALLY_SMALL_HEIGHT = 0.001;

    // The raw bounds are assumed to encompass the gate symbol and its input and output ports.
    private static final Rectangle2D.Double rawBounds = new Rectangle2D.Double(0, 0, 120, 60);

    // This is the gate symbol. It is initialized in subclasses.
    protected GeneralPath symbol;

    // This is true for an inverted gate, i.e. a gate which inverts its output like a NAND gate.
    private boolean inverted;

    protected LogicGateConfiguration(boolean inverted) {
      super();
      this.inverted = inverted;
    }

    public boolean isInverted() {
      return inverted;
    }

    protected GeneralPath getSymbol() {
      return symbol;
    }

    protected Rectangle2D.Double getRawBounds() {
      return rawBounds;
    }

    // AbstractCustomNodePainter implementation
    protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
      Shape shape = getTransformedSymbol(context);
      final boolean useSelectionStyle = selected(context, graphics);
      Color fillColor1 = getFillColor(context, useSelectionStyle);
      Color fillColor2 = getFillColor2(context, useSelectionStyle);
      if (fillColor2 != null) {
        double x = context.getX();
        double y = context.getY();
        double width = context.getWidth();
        double height = context.getHeight();
        GradientPaint gp = new GradientPaint((float) x, (float) y, fillColor1, (float) x + (float) width / 2,
            (float) y + (float) height / 2, fillColor2);
        graphics.setPaint(gp);
      } else {
        graphics.setColor(fillColor1);
      }
      graphics.fill(shape);
      graphics.setStroke(getLineStroke(context, useSelectionStyle));
      graphics.setColor(getLineColor(context, useSelectionStyle));
      graphics.draw(shape);
    }

    // GenericNodeRealizer.ContainsTest implementation
    // This method returns true if the given coordinate lies within the symbol or the ports.
    public boolean contains(NodeRealizer context, double x, double y) {
      if (!context.getBoundingBox().contains(x, y)) {
        // This is an optional optimization. If the coordinate is outside of the bounds
        // of the context we neither have to look at the symbol nor the ports.
        return false;
      }
      if (getTransformedSymbol(context).contains(x, y)) {
        return true;
      }
      // Consider node ports.
      for (int i = 0; i < context.portCount(); i++) {
        NodePort port = context.getPort(i);
        YRectangle bounds = port.getBounds();
        double tolerance = 4;
        if (bounds.x - tolerance <= x && x <= bounds.x + bounds.width + tolerance &&
            bounds.y - tolerance <= y && y <= bounds.y + bounds.height + tolerance) {
          return true;
        }
      }
      return false;
    }

    // The bounds for a port are a horizontal line starting at the port location at the left or right
    // border of the realizer and ending at the opposite border of the gate symbol in the middle of the
    // realizer.
    public YRectangle getBounds(NodePort port) {
      YPoint location = port.getLocation();
      if (port.getConfiguration().equals(PORT_CONFIG_INPUT)) {
        double width = getXScale(port.getRealizer()) * getLeftOffset();
        return new YRectangle(location.getX(), location.getY(), width, REALLY_SMALL_HEIGHT);
      } else {
        double width = getXScale(port.getRealizer()) * getRightOffset();
        return new YRectangle(location.getX() - width, location.getY(), width, REALLY_SMALL_HEIGHT);
      }
    }

    static boolean selected(final NodeRealizer context, final Graphics2D gfx) {
      return context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
    }

    // Returns a copy of the symbol which is translated and scaled according to the location and dimension
    // of the given node realizer.
    private Shape getTransformedSymbol(NodeRealizer context) {
      AffineTransform transform = new AffineTransform();
      Rectangle2D.Double bounds = getRawBounds();
      transform.translate(context.getX() - bounds.getX(), context.getY() - bounds.getY());
      transform.scale(getXScale(context), getYScale(context));
      return getSymbol().createTransformedShape(transform);
    }

    protected double getYScale(NodeRealizer context) {
      return context.getHeight() / getRawBounds().getHeight();
    }

    protected double getXScale(NodeRealizer context) {
      return context.getWidth() / getRawBounds().getWidth();
    }

    private double getLeftOffset() {
      return getSymbol().getBounds2D().getX() - getRawBounds().getX();
    }

    private double getRightOffset() {
      return getRawBounds().getMaxX() - getSymbol().getBounds2D().getMaxX();
    }
  }

  private static class AndGateConfiguration extends LogicGateConfiguration {
    public AndGateConfiguration(boolean inverted) {
      super(inverted);

      symbol = new GeneralPath();
      symbol.moveTo(70f, 0f);
      symbol.lineTo(20f, 0f);
      symbol.lineTo(20f, 60f);
      symbol.lineTo(70f, 60f);
      symbol.append(new Arc2D.Double(40, 0, 60, 60, 270, 180, Arc2D.OPEN), true);

      // Draw negation symbol for the NAND case.
      if (isInverted()) {
        Ellipse2D negationSymbol = new Ellipse2D.Double();
        negationSymbol.setFrame(100, 26, 8, 8);
        symbol.append(negationSymbol, false);
      }
    }
  }

  private static class NotGateConfiguration extends LogicGateConfiguration {
    public NotGateConfiguration() {
      super(true);
      
      symbol = new GeneralPath();
      symbol.moveTo(30f, 0f);
      symbol.lineTo(90f, 30f);
      symbol.lineTo(30f, 60f);
      symbol.closePath();

      Ellipse2D negationSymbol = new Ellipse2D.Double();
      negationSymbol.setFrame(90, 26, 8, 8);
      symbol.append(negationSymbol, false);
    }
  }
}