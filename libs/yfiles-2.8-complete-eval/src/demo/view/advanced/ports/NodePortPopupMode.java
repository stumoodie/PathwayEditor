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
import y.base.NodeList;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.view.Graph2D;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.NodeScaledPortLocationModel;
import y.view.PopupMode;
import y.view.PortLabelModel;
import y.view.PortLocationModel;
import y.view.ShapeNodeRealizer;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/**
 * Provides controls to add and remove {@link y.view.NodePort}s.
 *
 */
final class NodePortPopupMode extends PopupMode {
  private YPoint point;


  /**
   * Provides a control to edit and remove {@link y.view.NodeLabel}s.
   * @param label the label to be edited or removed.
   * @return a popup menu that allows for editing and removing labels.
   */
  public JPopupMenu getNodeLabelPopup( final NodeLabel label ) {
    final JPopupMenu jpm = new JPopupMenu("NodeLabel");
    jpm.add(new AbstractAction("Edit Label") {
      public void actionPerformed( final ActionEvent e ) {
        editLabel(label);
      }
    });
    jpm.add(new AbstractAction("Delete Label") {
      public void actionPerformed( final ActionEvent e ) {
        final NodeRealizer nr = label.getRealizer();
        nr.removeLabel(label);
        getGraph2D().updateViews();
      }
    });
    return jpm;
  }

  /**
   * Provides a control to add {@link y.view.NodePort}s of various styles.
   * @param v the node to whose visual representation ports may be added.
   * @return a popup menu that allows for adding ports.
   */
  public JPopupMenu getNodePopup( final Node v ) {
    final JPopupMenu jpm = new JPopupMenu("Node");
    jpm.add(new AbstractAction("Add NodePort") {
      {
        setEnabled(false);
      }
      public void actionPerformed( final ActionEvent e ) {
      }
    });
    jpm.add(new AddPortAction(PortConfigurations.INSTANCE.portConfigRectangle, v));
    jpm.add(new AddPortAction(PortConfigurations.INSTANCE.portConfigDynamic, v));
    jpm.add(new AddPortAction(PortConfigurations.INSTANCE.portConfigEllipse, v));
    return jpm;
  }

  /**
   * Provides a control to add a label for the specified port, change the
   * location policy of the specified port, or to remove the specified port
   * from its associated node.
   * @param port the port to be changed or removed.
   * @return a popup menu that allows for adding labels to ports, changing
   * location policies of ports, and removing ports.
   */
  public JPopupMenu getNodePortPopup( final NodePort port ) {
    final JPopupMenu jpm = new JPopupMenu("NodePort");

    jpm.add(new AbstractAction("Add Label") {
      public void actionPerformed( final ActionEvent e ) {
        editLabel(addPortLabel(port.getRealizer(), port, "Port"));
      }
    });

    final JMenu policies = new JMenu("Change Location Policy");
    policies.add(new ChangeLocationPolicyAction(
            port, NodeScaledPortLocationModel.POLICY_DISCRETE));
    policies.add(new ChangeLocationPolicyAction(
            port, NodeScaledPortLocationModel.POLICY_BOUNDARY));
    policies.add(new ChangeLocationPolicyAction(
            port, NodeScaledPortLocationModel.POLICY_BOUNDARY_CENTER));
    policies.add(new ChangeLocationPolicyAction(
            port, NodeScaledPortLocationModel.POLICY_BOUNDARY_INSIDE));
    policies.add(new ChangeLocationPolicyAction(
            port, NodeScaledPortLocationModel.POLICY_FREE));
    jpm.add(policies);

    jpm.add(new AbstractAction("Remove Port") {
      public void actionPerformed( final ActionEvent e ) {
        final Graph2D graph = getGraph2D();
        graph.firePreEvent();
        try {
          removePortImpl(graph, port);
        } finally {
          graph.firePostEvent();
        }

        graph.updateViews();
      }

      private void removePortImpl( final Graph2D graph, final NodePort port ) {
        final NodeRealizer nr = port.getRealizer();
        final Node node = nr.getNode();
        graph.backupRealizers((new NodeList(node)).nodes());

        NodePort.remove(port);
      }
    });
    return jpm;
  }

  /**
   * Overwritten to support a specific popup menu for {@link y.view.NodePort}
   * instances.
   * @param hitInfo hit test information to the specified event position.
   * @param x absolute x-coordinate of the triggering event.
   * @param y absolute y-coordinate of the triggering event.
   * @param popupType requested popup type.
   * @return a popup menu for the requested type and position.
   */
  protected JPopupMenu getPopup(
          final HitInfo hitInfo,
          final double x,
          final double y,
          final int popupType
  ) {
    point = new YPoint(x ,y);
    return super.getPopup(hitInfo, x, y, popupType);
  }



  private NodeLabel addPortLabel(
          final NodeRealizer nr,
          final NodePort port,
          final String text
  ) {
    final NodeLabel nl = nr.createNodeLabel();
    nl.setText(text);
    nl.setLabelModel(new PortLabelModel(3));
    nl.setModelParameter(PortLabelModel.createParameter(port, PortLabelModel.NORTH));
    nr.addLabel(nl);
    return nl;
  }

  private void editLabel( final NodeLabel label ) {
    final YRectangle bnds = label.getBox();
    view.openLabelEditor(
            label,
            bnds.getX(),
            bnds.getY(),
            null,
            true
            );
  }


  /**
   * Action that changes the
   * {@link y.view.NodeScaledPortLocationModel#getPortLocationPolicy() port
   * location policy} for a {@link y.view.NodePort}.
   */
  private final class ChangeLocationPolicyAction extends AbstractAction {
    private final byte policy;
    private final NodePort port;

    /**
     * Initializes a new <code>ChangeLocationPolicyAction</code>.
     * @param port the {@link y.view.NodePort} whose policy has to be changed.
     * @param policy the policy specifier that should be used in the specified
     * port's location model.
     */
    ChangeLocationPolicyAction( final NodePort port, final byte policy ) {
      super(createActionName(policy));
      this.policy = policy;
      this.port = port;

      final PortLocationModel model = port.getModelParameter().getModel();
      if (model instanceof NodeScaledPortLocationModel &&
          ((NodeScaledPortLocationModel) model).getPortLocationPolicy() == policy) {
        setEnabled(false);
      }
    }

    public void actionPerformed( final ActionEvent e ) {
      final Graph2D graph = getGraph2D();
      graph.firePreEvent();
      try {
        changeLocationPolicyImpl(port, policy);
      } finally {
        graph.firePostEvent();
      }

      graph.updateViews();
    }

    /**
     * Changes the port location policy of the specified port's location model
     * to the specified policy.
     * @param port the {@link y.view.NodePort} whose policy has to be changed.
     * @param policy the policy specifier that should be used in the specified
     * port's location model.
     */
    private void changeLocationPolicyImpl(
            final NodePort port, final byte policy
    ) {
      final YPoint location = port.getLocation();
      final PortLocationModel model = port.getModelParameter().getModel();
      if (model instanceof NodeScaledPortLocationModel) {
        ((NodeScaledPortLocationModel) model).setPortLocationPolicy(policy);
        port.setModelParameter(model.createParameter(port.getRealizer(), location));
      } else {
        final NodeScaledPortLocationModel nsplm = new NodeScaledPortLocationModel();
        nsplm.setPortLocationPolicy(policy);
        port.setModelParameter(nsplm.createParameter(port.getRealizer(), location));
      }
    }
  }

  /**
   * Creates a suitable action name for an action that changes the port location
   * policy of a {@link y.view.NodePort}'s location model.
   * @param policy the policy specifier that should be used.
   * @return a suitable action name.
   */
  private static String createActionName( final byte policy ) {
    switch (policy) {
      case NodeScaledPortLocationModel.POLICY_DISCRETE:
        return "Discrete";
      case NodeScaledPortLocationModel.POLICY_BOUNDARY:
        return "Boundary";
      case NodeScaledPortLocationModel.POLICY_BOUNDARY_CENTER:
        return "Boundary or Center";
      case NodeScaledPortLocationModel.POLICY_BOUNDARY_INSIDE:
        return "Inside";
      case NodeScaledPortLocationModel.POLICY_FREE:
        return "Free";
      default:
        return "Unknown";
    }
  }

  /**
   * Action that adds a specifically configured {@link y.view.NodePort} instance
   * to the visual representation of a node.
   */
  private final class AddPortAction extends AbstractAction {
    private final Node node;
    private final String configuration;

    /**
     * Initializes an new <code>AddPortAction</code>.
     * @param configurationName the configuration of the {@link y.view.NodePort}
     * instance that will be created.
     * @param node the node to which a {@link y.view.NodePort} instance is
     * added.
     */
    AddPortAction( final String configurationName, final Node node ) {
      super(createActionName(configurationName));
      this.configuration = configurationName;
      this.node = node;
      putValue(SMALL_ICON, new PortIcon(configurationName));
    }

    public void actionPerformed( final ActionEvent e ) {
      final Graph2D graph = getGraph2D();
      graph.firePreEvent();
      try {
        addPortImpl(graph, node, configuration);
      } finally {
        graph.firePostEvent();
      }

      graph.updateViews();
    }

    /**
     * Adds a new {@link y.view.NodePort} instance with the specified
     * configuration to the visual representation of the specified node.
     * @param graph that graph of the node to which a port is added.
     * @param node the node to which a port is added.
     * @param portConfigId the name of the port configuration to use.
     */
    private void addPortImpl(
            final Graph2D graph,
            final Node node,
            final String portConfigId
    ) {
      graph.backupRealizers((new NodeList(node)).nodes());

      final NodeRealizer nr = graph.getRealizer(node);
      final YPoint location =
              point == null
              ? new YPoint(nr.getCenterX(), nr.getCenterY())
              : point;

      final NodeScaledPortLocationModel model = new NodeScaledPortLocationModel();
      model.setPortLocationPolicy(NodeScaledPortLocationModel.POLICY_BOUNDARY);
      final NodePort port = new NodePort();
      port.setModelParameter(model.createParameter(nr, location));
      port.setConfiguration(portConfigId);
      nr.addPort(port);

      addPortLabel(nr, port, "Port " + nr.portCount());
    }
  }

  /**
   * Creates a suitable action name for an action that creates
   * {@link y.view.NodePort} instances that use the specified configuration.
   * @param portConfigId the name of the configuration.
   * @return a suitable action name.
   */
  private static String createActionName( final String portConfigId ) {
    String s = portConfigId;
    if (s != null) {
      s = s.trim();
      if (s.length() > 0) {
        s = s.toUpperCase();
        if (s.startsWith("PORT_")) {
          s = s.substring(5);
        }
        if (s.length() > 1) {
          s = s.charAt(0) + s.substring(1).toLowerCase();
        }

        return s;
      }
    }
    return null;
  }

  /**
   * A simple icon that display a {@link y.view.NodePort}.
   */
  private static final class PortIcon implements Icon {
    private static final int MARGIN = 2;

    private final NodePort port;
    private final double x;
    private final double y;
    private final int w;
    private final int h;

    PortIcon( final String configuration ) {
      final NodeRealizer dummy = new ShapeNodeRealizer();
      dummy.setFrame(0, 0, 10, 10);
      port = new NodePort();
      dummy.addPort(port);
      port.setConfiguration(configuration);
      final YRectangle bnds = port.getBounds();
      w = (int) Math.ceil(bnds.getWidth()) + 2*MARGIN;
      h = (int) Math.ceil(bnds.getHeight())  + 2*MARGIN;
      x = bnds.getX() - MARGIN;
      y = bnds.getY() - MARGIN;
    }

    public int getIconHeight() {
      return h;
    }

    public int getIconWidth() {
      return w;
    }

    public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
      final Graphics2D gfx = (Graphics2D) g.create();
      gfx.translate(x - this.x, y - this.y);
      port.paint(gfx);
      gfx.dispose();
    }
  }
}
