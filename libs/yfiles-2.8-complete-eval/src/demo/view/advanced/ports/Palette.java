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

import y.base.Node;
import y.geom.YPoint;
import y.option.RealizerCellRenderer;
import y.view.DropSupport;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.NodeScaledPortLocationModel;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A palette that provides templates for nodes with node ports.
 *
 */
class Palette extends JPanel {
  /**
   * Initializes a new <code>Palette</code> instance for the specified graph
   * view.
   * @param view the {@link y.view.Graph2DView} holding the graph in which
   * nodes can be created using this palette's templates.
   */
  Palette( final Graph2DView view ) {
    super(new GridLayout(1, 1));

    // begin create templates
    final NodeRealizer nr = view.getGraph2D().getDefaultNodeRealizer();
    final NodeRealizer prototype = nr.createCopy();
    prototype.setSize(90, 60);
    prototype.setFillColor(Color.LIGHT_GRAY);

    final DefaultListModel model = new DefaultListModel();
    model.addElement(addDynamicPorts(prototype.createCopy()));
    model.addElement(addEllipsePorts(prototype.createCopy()));
    model.addElement(addRectanglePort(prototype.createCopy()));
    // end create templates

    // begin create control to choose between templates
    final Rectangle2D.Double r = new Rectangle2D.Double(0, 0, -1, -1);
    for (Enumeration en = model.elements(); en.hasMoreElements();) {
      ((NodeRealizer) en.nextElement()).calcUnionRect(r);
    }

    final JList jl = new JList(model);
    jl.setCellRenderer(new RealizerCellRenderer(
            (int) Math.ceil(r.getWidth()) + 10,
            (int) Math.ceil(r.getHeight()) + 10));
    jl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged( final ListSelectionEvent e ) {
        final Object value = jl.getSelectedValue();
        if (value instanceof NodeRealizer) {
          view.getGraph2D().setDefaultNodeRealizer((NodeRealizer) value);
        }
      }
    });
    jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jl.setSelectedIndex(0);

    add(new JScrollPane(jl));
    // end create control to choose between templates

    // begin setup drag and drop support
    //   nodes can be created by dragging this palette's templates to the
    //   associated graph view
    final DropSupport dropSupport = new DropSupport(view) {
      protected Node createNode(
              final Graph2DView view,
              final NodeRealizer nr,
              final double x,
              final double y
      ) {
        final Graph2D graph = view.getGraph2D();
        final Node node = super.createNode(view, nr, x, y);
        graph.getRealizer(node).setLabelText(Integer.toString(graph.nodeCount()));
        return node;
      }
    };
    dropSupport.setPreviewEnabled(true);

    final DragSource dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(jl, DnDConstants.ACTION_MOVE,
        new DragGestureListener() {
          public void dragGestureRecognized( final DragGestureEvent e ) {
            final Object value = jl.getSelectedValue();
            if (value instanceof NodeRealizer) {
              dropSupport.startDrag(dragSource, (NodeRealizer) value, e, DragSource.DefaultMoveDrop);
            }
          }
        });
    // end setup drag and drop support
  }

  /**
   * Adds dynamic, rectangular node ports to the specified realizer.
   * @param nr the {@link y.view.NodeRealizer} to which node ports are added.
   * @return the specified realizer instance.
   */
  private NodeRealizer addDynamicPorts( final NodeRealizer nr ) {
    return addPorts(
            nr,
            PortConfigurations.INSTANCE.portConfigDynamic,
            NodeScaledPortLocationModel.POLICY_BOUNDARY,
            new YPoint[]{
                    new YPoint(nr.getCenterX(), nr.getY()),
                    new YPoint(nr.getX(), nr.getCenterY()),
                    new YPoint(nr.getCenterX(), nr.getY() + nr.getHeight()),
                    new YPoint(nr.getX() + nr.getWidth(), nr.getCenterY()),
            }
    );
  }

  /**
   * Adds rectangular node ports to the specified realizer.
   * @param nr the {@link y.view.NodeRealizer} to which node ports are added.
   * @return the specified realizer instance.
   */
  private NodeRealizer addRectanglePort( final NodeRealizer nr ) {
    final double x = nr.getX();
    final double y = nr.getY();
    final double w = nr.getWidth();
    final double h = nr.getHeight();
    return addPorts(
            nr,
            PortConfigurations.INSTANCE.portConfigRectangle,
            NodeScaledPortLocationModel.POLICY_BOUNDARY,
            new YPoint[] {
                    new YPoint(x, y + h * 0.25),
                    new YPoint(x, y + h * 0.75),
                    new YPoint(x + w, y + h * 0.25),
                    new YPoint(x + w, y + h * 0.75),
            }
    );
  }

  /**
   * Adds elliptical node ports to the specified realizer.
   * @param nr the {@link y.view.NodeRealizer} to which node ports are added.
   * @return the specified realizer instance.
   */
  private NodeRealizer addEllipsePorts( final NodeRealizer nr ) {
    final double x = nr.getX();
    final double y = nr.getY();
    final double w = nr.getWidth();
    final double h = nr.getHeight();
    return addPorts(
            nr,
            PortConfigurations.INSTANCE.portConfigEllipse,
            NodeScaledPortLocationModel.POLICY_BOUNDARY_CENTER,
            new YPoint[]{
                    new YPoint(x, y + h * 0.25),
                    new YPoint(x, y + h * 0.75),
                    new YPoint(x + w * 0.25, y + h),
                    new YPoint(x + w * 0.5, y + h),
                    new YPoint(x + w * 0.75, y + h),
                    new YPoint(x + w, y + h * 0.75),
                    new YPoint(x + w, y + h * 0.25),
                    new YPoint(x + w * 0.75, y),
                    new YPoint(x + w * 0.5, y),
                    new YPoint(x + w * 0.25, y),
            }
    );
  }

  /**
   * Adds a node port to the specified realizer for each of the specified
   * port positions.
   * @param nr the {@link y.view.NodeRealizer} to which node ports are added.
   * @param configuration the name of the node port configuration for the
   * added node ports.
   * @param policy the location policy of the {@link y.view.PortLocationModel}
   * used for the added node ports.
   * @param positions the positions of the added node ports.
   * @return the specified realizer instance.
   */
  private NodeRealizer addPorts(
          final NodeRealizer nr,
          final String configuration,
          final byte policy,
          final YPoint[] positions
  ) {
    for (int i = 0; i < positions.length; ++i) {
      final NodePort port = new NodePort();
      port.setConfiguration(configuration);
      nr.addPort(port);
      final NodeScaledPortLocationModel model = new NodeScaledPortLocationModel();
      model.setPortLocationPolicy(policy);
      port.setModelParameter(model.createParameter(nr, positions[i]));
    }

    return nr;
  }
}
