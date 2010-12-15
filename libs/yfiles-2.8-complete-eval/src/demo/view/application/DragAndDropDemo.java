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

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.Graph2DView;
import y.view.Drawable;
import y.view.DropSupport;
import y.view.PolyLineEdgeRealizer;
import y.view.QuadCurveEdgeRealizer;
import y.view.EdgeRealizer;
import y.view.SplineEdgeRealizer;
import y.view.BezierEdgeRealizer;
import y.view.Arrow;
import y.option.RealizerCellRenderer;
import y.base.Node;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.AbstractAction;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import demo.view.DemoBase;

/**
 * Demo that shows how to display and drag different {@link NodeRealizer} and {@link EdgeRealizer}
 * instances from a list and how to drop them onto a {@link Graph2DView} using a {@link Drawable}
 * that indicates the drop operation. Moreover, using snap lines for node drag and drop is demonstrated.
 * This demo makes use of the {@link java.awt.dnd.DnDConstants java.awt.dnd} package.
 */
public class DragAndDropDemo extends DemoBase {
  private final DragAndDropSupport dndSupport;

  /** Creates a new instance of DragAndDropDemo */
  public DragAndDropDemo() {
    // create the customized DnD support instance
    dndSupport = new DragAndDropSupport(createRealizers(), view);

    // get the List UI
    final JList realizerList = dndSupport.getList();

    //add the realizer list to the panel
    contentPane.add(new JScrollPane(realizerList), BorderLayout.WEST);
  }

  /** Creates a toolbar for this demo. */
  protected JToolBar createToolBar() {
    final JToolBar bar = super.createToolBar();
    bar.add(new JToggleButton(new AbstractAction("Snapping") {
      public void actionPerformed(ActionEvent e) {
        dndSupport.configureSnapping(((JToggleButton) e.getSource()).isSelected(), 30.0, 15.0, true);
      }
    }));
    return bar;
  }

  /**
   * Creates a collection of realizer
   * instance. The realizer instances have different shapes
   * and colors.
   */
  protected Collection createRealizers()
  {
    List result = new ArrayList();

    Map shapeTypeToStringMap = ShapeNodeRealizer.shapeTypeToStringMap();
    float hueIncrease = 1.0f / (float) shapeTypeToStringMap.size();
    float hue = 0.0f;
    for (Iterator iter = shapeTypeToStringMap.keySet().iterator(); iter.hasNext(); hue += hueIncrease) {
      Byte shapeType = (Byte) iter.next();
      ShapeNodeRealizer r = new ShapeNodeRealizer(shapeType.byteValue());
      r.setWidth(100.0);
      r.setLabelText((String) shapeTypeToStringMap.get(shapeType));
      r.setFillColor(new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f)));
      result.add(r);
    }

    final PolyLineEdgeRealizer smoothedPolyLine = new PolyLineEdgeRealizer();
    smoothedPolyLine.setSmoothedBends(true);

    List edgeRealizers = new ArrayList();
    edgeRealizers.add(new PolyLineEdgeRealizer());
    edgeRealizers.add(smoothedPolyLine);
    edgeRealizers.add(new QuadCurveEdgeRealizer());
    edgeRealizers.add(new BezierEdgeRealizer());
    edgeRealizers.add(new SplineEdgeRealizer());

    // Set the target arrow for the edge realizers.
    for (Iterator iterator = edgeRealizers.iterator(); iterator.hasNext();) {
      EdgeRealizer edgeRealizer = (EdgeRealizer) iterator.next();
      edgeRealizer.setTargetArrow(Arrow.STANDARD);
    }

    result.addAll(edgeRealizers);

    return result;
  }

  /**
   * Support class that be used to create a JList that contains NodeRealizers that can be dragged
   * and dropped onto the given Graph2DView object.
   */
  public static class DragAndDropSupport {
    protected JList realizerList;
    protected DropSupport dropSupport;


    public DragAndDropSupport(Collection realizerList, final Graph2DView view) {
      this(realizerList.toArray(), view);
    }

    public DragAndDropSupport(Object[] realizers, final Graph2DView view) {
      this(realizers, view, 120, 45);
    }

    public DragAndDropSupport(Object[] realizers, final Graph2DView view, int itemWidth, int itemHeight) {
      initializeDropSupport(view);
      initializeRealizerList(realizers, view, itemWidth, itemHeight);
      initializeDragSource();
    }

    /**
     * Creates the drop support class that can be used for dropping realizers onto the Graph2DView.
     */
    protected void initializeDropSupport(final Graph2DView view) {
      dropSupport = new DropSupport(view) {
        protected Node createNode(Graph2DView view, NodeRealizer r, double worldCoordX, double worldCoordY) {
          final Node node = super.createNode(view, r, worldCoordX, worldCoordY);
          nodeCreated(node, worldCoordX, worldCoordY);
          return node;
        }
      };

      dropSupport.setPreviewEnabled(true);
    }

    /**
     * Creates a nice GUI for displaying NodeRealizers.
     */
    protected void initializeRealizerList(Object[] realizers, final Graph2DView view, int itemWidth, int itemHeight) {
      realizerList = new JList(realizers);
      realizerList.setCellRenderer(createCellRenderer(itemWidth, itemHeight));

      // set the currently selected NodeRealizer as default nodeRealizer
      realizerList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (realizerList.getSelectedValue() instanceof NodeRealizer) {
            nodeRealizerSelected(view, (NodeRealizer) realizerList.getSelectedValue());
          } else if (realizerList.getSelectedValue() instanceof EdgeRealizer) {
            edgeRealizerSelected(view, (EdgeRealizer) realizerList.getSelectedValue());
          }
        }
      });

      realizerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      realizerList.setSelectedIndex(0);
    }

    /**
     * Defines the realizer list to be the drag source use the string-valued name of the realizer as transferable.
     */
    protected void initializeDragSource() {
      final DragSource dragSource = new DragSource();
      dragSource.createDefaultDragGestureRecognizer(realizerList, DnDConstants.ACTION_MOVE,
          new DragGestureListener() {
            public void dragGestureRecognized(DragGestureEvent event) {
              final Object value = realizerList.getSelectedValue();
              if (value instanceof NodeRealizer) {
                NodeRealizer nr = (NodeRealizer) value;
                // use the drop support class to initialize the drag and drop operation.
                dropSupport.startDrag(dragSource, nr, event, DragSource.DefaultMoveDrop);
              } else if (value instanceof EdgeRealizer) {
                EdgeRealizer nr = (EdgeRealizer) value;
                // use the drop support class to initialize the drag and drop operation.
                dropSupport.startDrag(dragSource, nr, event, DragSource.DefaultMoveDrop);
              }
            }
          });
    }

    /**
     * Configures the {@link DropSupport}of this class according to the specified snapping configuration.
     */
    public void configureSnapping(final SnappingConfiguration config, final boolean previewEnabled) {
      dropSupport.setSnappingEnabled(config.isSnappingEnabled() || config.isGridSnappingEnabled());
      config.configureSnapContext(dropSupport.getSnapContext());
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Configures the {@link DropSupport}of this class according to the specified parameters.
     */
    public void configureSnapping(final boolean snapping, final double nodeToNodeDistance,
                                  final double nodeToEdgeDistance, final boolean previewEnabled) {
      dropSupport.setSnappingEnabled(snapping);
      dropSupport.getSnapContext().setNodeToNodeDistance(nodeToNodeDistance);
      dropSupport.getSnapContext().setNodeToEdgeDistance(nodeToEdgeDistance);
      dropSupport.getSnapContext().setUsingSegmentSnapLines(snapping);
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Creates the realizer cell renderer used by this class.
     */
    protected RealizerCellRenderer createCellRenderer(int itemWidth, int itemHeight) {
      return new RealizerCellRenderer(itemWidth, itemHeight);
    }

    protected void nodeCreated(Node node, double worldCoordX, double worldCoordY) {
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList.
     * This method sets the given NodeRealizer as the view's graph default node realizer.
     */
    protected void nodeRealizerSelected(Graph2DView view, NodeRealizer realizer) {
      view.getGraph2D().setDefaultNodeRealizer(realizer);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList.
     * This method sets the given EdgeRealizer as the view's graph default node realizer.
     */
    protected void edgeRealizerSelected(Graph2DView view, EdgeRealizer realizer) {
      view.getGraph2D().setDefaultEdgeRealizer(realizer);
    }

    /**
     * Return the JList that has been configured by this support class.
     */
    public JList getList() {
      return realizerList;
    }
  }

  /**
   * Instantiates and starts this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new DragAndDropDemo()).start("Drag and Drop Demo");
      }
    });
  }
}
