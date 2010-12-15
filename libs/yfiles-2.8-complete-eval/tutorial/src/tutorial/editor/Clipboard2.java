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
package tutorial.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.view.Arrow;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DClipboard;
import y.view.Graph2DCopyFactory;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.LineType;
import y.view.NodeRealizer;

public class Clipboard2 {
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;
  /** A customized subclass of the yFiles graph type. */
  CustomGraph2D graph;
  /** The yFiles view mode that handles editing. */
  EditMode editMode;
  /** The yFiles class that provides clipboard support. */
  Graph2DClipboard clipboard;

  public Clipboard2(Dimension size, String title) {
    view = createGraph2DView();
    graph = (CustomGraph2D)view.getGraph2D();
    frame = createApplicationFrame(size, title, view);
    configureDefaultRealizers(graph);
  }

  public Clipboard2() {
    this(new Dimension(600, 300), "");
    frame.setTitle(getClass().getName());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView(new CustomGraph2D());
    // Add a mouse wheel listener to zoom in and out of the view.
    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    // Add the central view mode for an editor type application.
    editMode = new EditMode() {
      protected String getNodeTip(Node node) {
        // Retrieve the node's additional data.
        return "<html><b>Data:</b><br>" + getCustomGraph().getCustomText(node) + "</html>";
      }
    };
    editMode.showNodeTips(true);
    view.addViewMode(editMode);
    // "Install" keyboard support.
    new Graph2DViewActions(view).install();
    return view;
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(size);
    // Add the given view to the panel.
    panel.add(view, BorderLayout.CENTER);
    // Add a toolbar with some actions to the panel, too.
    panel.add(createToolBar(), BorderLayout.NORTH);
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getRootPane().setContentPane(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  protected void configureDefaultRealizers(Graph2D graph) {
    // Add an arrowhead decoration to the target side of the edges.
    graph.getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);
    // Set the node size and some other graphical properties.
    NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
    defaultNodeRealizer.setSize(80, 30);
    defaultNodeRealizer.setFillColor(Color.ORANGE);
    defaultNodeRealizer.setLineType(LineType.DASHED_1);
  }

  /** Creates a toolbar for this demo. */
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.add(new FitContent(getView()));
    toolbar.add(new Zoom(getView(), 1.25));
    toolbar.add(new Zoom(getView(), 0.8));
    
    toolbar.addSeparator();
    // Adding clipboard actions (Cut, Copy, Paste) to the tool bar.
    toolbar.add(getClipboardSupport().getCutAction());
    toolbar.add(getClipboardSupport().getCopyAction());
    toolbar.add(getClipboardSupport().getPasteAction());
    
    toolbar.addSeparator();
    // Add text field.
    final JTextField textField = new JTextField(20);
    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        CustomGraph2D graph = getCustomGraph();
        NodeCursor nc = graph.selectedNodes();
        for (; nc.ok(); nc.next()) {
          // Set the entered text as the node's additional data.
          graph.setCustomText(nc.node(), textField.getText());
        }
        textField.selectAll();
        graph.updateViews();
      }
    });
    toolbar.add(textField);
    
    return toolbar;
  }

  public void show() {
    frame.setVisible(true);
  }

  public Graph2DView getView() {
    return view;
  }

  public CustomGraph2D getCustomGraph() {
    return graph;
  }

  /** Returns the clipboard support for this application. */
  public Graph2DClipboard getClipboardSupport() {
    if (clipboard == null) {
      clipboard = new Graph2DClipboard(view);
    }
    return clipboard;
  }

  /** Action that fits the content nicely inside the view. */
  protected static class FitContent extends AbstractAction {
    Graph2DView view;
    
    public FitContent(Graph2DView view) {
      super("Fit Content");
      this.view = view;
      this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }
    
    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }

  /** Action that applies a specified zoom level to the given view. */
  protected static class Zoom extends AbstractAction {
    Graph2DView view;
    double factor;

    public Zoom(Graph2DView view, double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      this.view = view;
      this.factor = factor;
      this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      // Adjusts the size of the view's world rectangle. The world rectangle 
      // defines the region of the canvas that is accessible by using the 
      // scrollbars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);
      view.updateView();
    }
  }

  /**
   * A customized Graph2D subclass that conveniently enables storing and retrieving 
   * custom data associated with the nodes in the graph.
   */
  public class CustomGraph2D extends Graph2D {
    NodeMap customTextMap;

    /**
     * Instantiates a new CustomGraph2D. 
     * The instance conveniently handles additional data for nodes.
     */
    public CustomGraph2D() {
      init();
    }

    /** Creates a new CustomGraph2DCopyFactory. */
    protected GraphCopier.CopyFactory createGraphCopyFactory() {
      return new CustomGraph2DCopyFactory();
    }

    /** Returns a new CustomGraph2D instance. */
    public Graph createGraph() {
      return new CustomGraph2D();
    }

    /** Initializes the data store for the additional data. */
    protected void init() {
      customTextMap = this.createNodeMap();
    }

    /** Sets the given text as the additional data for the given node. */
    public void setCustomText(Node node, String text) {
      customTextMap.set(node, text);
    }

    /** Gets the text (i.e., the additional data) stored for the given node. */
    public String getCustomText(Node node) {
      return (String)customTextMap.get(node);
    }
  }

  public class CustomGraph2DCopyFactory extends Graph2DCopyFactory {
    public Graph createGraph() {
      return new CustomGraph2D();
    }

    public Node copyNode(Graph targetGraph, Node originalNode) {
      Node newNode = super.copyNode(targetGraph, originalNode);
      if (originalNode.getGraph() instanceof CustomGraph2D && 
          targetGraph instanceof CustomGraph2D) {
        CustomGraph2D sourceGraph = (CustomGraph2D)originalNode.getGraph();
        CustomGraph2D g = (CustomGraph2D)targetGraph;
        g.setCustomText(newNode, sourceGraph.getCustomText(originalNode));
      }
      return newNode;
    }
  }

  public static void main(String[] args) {
    Clipboard2 sge = 
      new Clipboard2(new Dimension(600, 300), "Clipboard");
    sge.show();
  }
}
