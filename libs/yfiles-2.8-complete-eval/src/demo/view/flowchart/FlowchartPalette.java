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
package demo.view.flowchart;

import y.geom.YPoint;
import y.option.RealizerCellRenderer;
import y.view.DropSupport;
import y.view.EdgeRealizer;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import y.view.Graph2D;
import y.view.PolyLineEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Arrow;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import demo.view.flowchart.painters.FlowchartAnnotationPainter;
import demo.view.flowchart.painters.FlowchartRealizerFactory;
import demo.view.flowchart.painters.FlowchartRealizerConstants;

/**
 * This is a component, which represents a palette of flowchart nodes and edges and allows to drag them into a Graph2DView.
 */
public class FlowchartPalette extends JComponent {
  private DragAndDropSupport dropSupport;
  boolean snapMode;

  /**
   * Creates a new FlowchartPalette, containing pre-configured list of node and edge realizers.
   */
  public FlowchartPalette(final Graph2DView view) {
    final BorderLayout borderLayout = new BorderLayout();
    borderLayout.setVgap(10);
    this.setLayout(borderLayout);
    this.add(createDefaultPalette(view),BorderLayout.CENTER);
    initializeDefaultRealizers(view);
  }

  /**
   * Returns whether or not snapping is enabled.
   * @return true if snap mode enabled.
   * @see #setSnapMode(boolean)
   */
  public boolean isSnapMode() {
    return snapMode;
  }

  /**
   * Activates/deactivates snapping between graph elements, while dragging of a Flowchart-Node into the view.
   * @param snapMode Whether to enable snapping.
   */
  public void setSnapMode(boolean snapMode) {
    this.snapMode = snapMode;
    dropSupport.configureSnapping(snapMode, 30, 15, true);
  }

  /**
   * Initializes default realizers
   * @param view The respective Graph2DView.
   */
  protected void initializeDefaultRealizers(Graph2DView view) {
    Graph2D graph = view.getGraph2D();
    final EdgeRealizer der = graph.getDefaultEdgeRealizer();
    if (der instanceof PolyLineEdgeRealizer){
      ((PolyLineEdgeRealizer)der).setSmoothedBends(true);
      der.setTargetArrow(Arrow.STANDARD);
    }
  }

  /**
   * Creates a default flowchart realizers palette
   * @param view The respective Graph2DView, that is the target of the drag&drop action from the realizer palette.
   * @return
   */
  private JComponent createDefaultPalette(final Graph2DView view) {

    final ArrayList realizers = new ArrayList();
    addDefaultTemplates(realizers);

    //add the realizer list to the panel
    //create the drag and drop list filled with the available realizer configurations
    dropSupport = new DragAndDropSupport(realizers, view);
    final JList realizerList = dropSupport.getList();
    realizerList.setCellRenderer(new RealizerCellRenderer(60, 45) {
      protected Icon createEdgeRealizerIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        if (realizer.labelCount() > 0) {
          final String text = realizer.getLabelText();
          if ("No".equalsIgnoreCase(text) || "Yes".equalsIgnoreCase(text)) {
            return new EdgeRealizerIcon(realizer, iconWidth, iconHeight) {
              protected YPoint calculateSourceBend(EdgeRealizer realizer, int iconWidth, int iconHeight) {
                return new YPoint(0.5 * iconWidth, iconHeight - realizer.getLabel().getHeight() - 2);
              }
            };
          }
        }
        return super.createEdgeRealizerIcon(realizer, iconWidth, iconHeight);
      }
    });
    realizerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    realizerList.setVisibleRowCount(-1);
    JScrollPane palette = new JScrollPane(realizerList);
    palette.setPreferredSize(new Dimension(220, 300));
    return palette;
  }

  /**
   * Adds default flowchart templates to the palette list.
   *
   * @param realizers The list of all template realizers
   */
  protected void addDefaultTemplates(final List realizers) {
    realizers.add(FlowchartRealizerFactory.createDirectData());
    realizers.add(FlowchartRealizerFactory.createDataBase());
    realizers.add(FlowchartRealizerFactory.createProcess());
    realizers.add(FlowchartRealizerFactory.createDecision());
    realizers.add(FlowchartRealizerFactory.createDocument());
    realizers.add(FlowchartRealizerFactory.createStart1());
    realizers.add(FlowchartRealizerFactory.createStart2());
    realizers.add(FlowchartRealizerFactory.createPredefinedProcess());
    realizers.add(FlowchartRealizerFactory.createStoredData());
    realizers.add(FlowchartRealizerFactory.createInternalStorage());
    realizers.add(FlowchartRealizerFactory.createSequentialData());
    realizers.add(FlowchartRealizerFactory.createManualInput());
    realizers.add(FlowchartRealizerFactory.createCard());
    realizers.add(FlowchartRealizerFactory.createPaperTape());
    realizers.add(FlowchartRealizerFactory.createCloud());
    realizers.add(FlowchartRealizerFactory.createDelay());
    realizers.add(FlowchartRealizerFactory.createDisplay());
    realizers.add(FlowchartRealizerFactory.createManualOperation());
    realizers.add(FlowchartRealizerFactory.createPreparation());
    realizers.add(FlowchartRealizerFactory.createLoopLimit());
    realizers.add(FlowchartRealizerFactory.createTerminator());
    realizers.add(FlowchartRealizerFactory.createOnPageReference());
    realizers.add(FlowchartRealizerFactory.createOffPageReference());
    realizers.add(FlowchartRealizerFactory.createAnnotation(FlowchartAnnotationPainter.PROPERTY_ORIENTATION_VALUE_AUTO));
    realizers.add(FlowchartRealizerFactory.createDefaultConnection());
    realizers.add(FlowchartRealizerFactory.createNoConnection());
    realizers.add(FlowchartRealizerFactory.createYesConnection());

    //Configure shadow:
    for (int i=0; i < realizers.size(); i++){
      final Object realizer = realizers.get(i);
      if (realizer instanceof GenericNodeRealizer){
        ((GenericNodeRealizer) realizer).setStyleProperty(FlowchartRealizerConstants.PROPERTY_SHADOW, Boolean.TRUE);
      }
    }
  }


  private static final class DragAndDropSupport {
    private final JList realizerList;
    private DropSupport dropSupport;

    public DragAndDropSupport(Collection realizers, final Graph2DView view) {
      // create the drop support class that can be used for dropping realizers
      // onto the Graph2DView
      dropSupport = new DropSupport(view);

      dropSupport.setPreviewEnabled(true);

      // create a nice GUI for displaying NodeRealizers
      DefaultListModel model = new DefaultListModel();
      for (Iterator it = realizers.iterator(); it.hasNext();) {
        model.addElement(it.next());
      }
      realizerList = new JList(model);
      realizerList.setCellRenderer(new RealizerCellRenderer(120, 45));

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

      // define the realizer list to be the drag source
      // use the string-valued name of the realizer as transferable
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

    public void configureSnapping(final boolean snapping, final int nodeToNodeDistance, final int nodeToEdgeDistance,
                                  final boolean previewEnabled) {
      configureDropSupport(dropSupport, snapping, previewEnabled, nodeToNodeDistance, nodeToEdgeDistance);
    }

    protected static void configureDropSupport(final DropSupport dropSupport, final boolean snapping,
                                               final boolean previewEnabled, final double nodeToNodeDistance,
                                               final double nodeToEdgeDistance) {
      dropSupport.setSnappingEnabled(snapping);
      dropSupport.getSnapContext().setNodeToNodeDistance(nodeToNodeDistance);
      dropSupport.getSnapContext().setNodeToEdgeDistance(nodeToEdgeDistance);
      dropSupport.getSnapContext().setUsingSegmentSnapLines(snapping);
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList. This method sets the given
     * NodeRealizer as the view's graph default node realizer.
     */
    protected void nodeRealizerSelected(Graph2DView view, NodeRealizer realizer) {
      view.getGraph2D().setDefaultNodeRealizer(realizer);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList. This method sets the given
     * EdgeRealizer as the view's graph default node realizer.
     */
    protected void edgeRealizerSelected(Graph2DView view, EdgeRealizer realizer) {
      view.getGraph2D().setDefaultEdgeRealizer(realizer);
    }

    /** Return the JList that has been configured by this support class. */
    public JList getList() {
      return realizerList;
    }
  }
}