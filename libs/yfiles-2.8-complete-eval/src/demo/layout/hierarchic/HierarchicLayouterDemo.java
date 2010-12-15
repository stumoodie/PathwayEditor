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
package demo.layout.hierarchic;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.base.Graph;
import y.base.DataProvider;
import y.layout.LabelLayoutConstants;
import y.layout.PortConstraint;
import y.layout.PortConstraintConfigurator;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.ClassicLayerSequencer;
import y.module.HierarchicLayoutModule;
import y.view.Arrow;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.PortAssignmentMoveSelectionMode;
import y.io.GraphMLIOHandler;
import y.io.graphml.output.OutputHandlerProvider;
import y.io.graphml.output.QueryOutputHandlersEvent;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.graph2d.PortConstraintOutputHandler;
import y.io.graphml.graph2d.PortConstraintInputHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.QueryInputHandlersEvent;
import y.io.graphml.input.GraphMLParseException;
import y.util.Maps;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.WeakHashMap;

import org.w3c.dom.Element;

/**
 * This Demo shows how HierarchicLayouter can handle port constraints,
 * how it can take edge labels into consideration when laying out
 * a graph and how to specify groups of nodes, which will be placed next to
 * each other on each layer.
 * <br>
 * <br>
 * <b>Usage:</b>
 * <br>
 * After starting the demo create a graph manually in the graph editor
 * pane. Now click on the "Layout" Button in the toolbar to 
 * start the hierarchic layouter. 
 * <br>
 * Additional port constraints can be specified for HierarchicLayouter.
 * A port constraint expresses on what side of a node the source and/or 
 * target point of an edge should connect. To add a port constraint to an edge 
 * first select the edge by clicking on it. Then choose the desired source and target 
 * port constraint for the selected edge with the radio button panel on the left of
 * the graph pane. Selecting "South" from the "Source Port" choice for example 
 * means the edge selected edge should connect to the bottom side of the source node.
 * Alternatively once can simply move or create the first or last bend of an edge.
 * A visual clue will appear that determines the PortConstraint.
 * <br>
 * A port constraint that is marked as "strong", means that the associated 
 * port coordinate given to the layouter will not be modified by the layouter.  
 * <br>
 * After the port constraints have been set up it is time to press the 
 * "layout" button again. Now the resulting layout is a hierarchic layout
 * that obeys the additional port constraints.
 * <br>
 * To activate the edge labeling feature check the box named "Label Edges" just below
 * the port constraint selector pane.
 * If this feature is turned on then on there will be edge labels visible that
 * display the type of port constraint activated for the source and target port
 * of each edge. Now by pressing the "Layout" button again the resulting layout 
 * will consider these edge labels as well. One can see that none of the labels 
 * overlap and that the size of the layout has increased.
 * <br>
 * The toolbar button "Option..." allows to specify diverse layout parameters
 * for the layouter. Not all of these options are important to this demo.
 * Noteworthy options for this demo are "Edge Routing: Orthogonal" and in tab 
 * "Node Rank" the ranking policy "From Sketch".
 * <br>
 * "Edge Routing: Orthogonal" has the effect of routing all edges orthogonally, 
 * i.e. by using only horizontal and vertical line segments.
 * <br>
 * "Ranking Policy: From Sketch" has the effect that the layer partitions will be
 * established by the given drawing heuristically (looking at the y-coordinates 
 * of the nodes). By this option it is possible to put nodes in the same layer that
 * are connected by an edge.
 * <br>
 * <br>
 * Node Groups:
 * By clicking on one of the colored buttons in the left bar, the currently selected
 * nodes will be assigned to the corresponding group. This grouping will be
 * indicated by the node color. When being laid out, nodes on the same layer
 * having the same group (color) will be placed next to each other as a compound
 * group.
 * <br>
 * <br>
 * API usage:
 * <br>
 * Each port constraint is expressed
 * by an object of type PortConstraint. The side of the port can be specified by
 * one of the side specifiers NORTH, SOUTH, EAST, WEST of class PortConstraint.
 * PortConstraint data gets passed to HierarchicLayouter by binding named DataProviders
 * to the input graph. Use PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY to register
 * a data provider that returns a PortConstraint object for each source port of an edge
 * and use PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY to register
 * a data provider that returns a PortConstraint object for each target port of an edge.
 * <br>
 * The internal hierarchic labeling gets activated by associating an array of 
 * type LabelLayoutData to each edge of the graph. This is done by registering
 * a DataProvider with the key LabelLayoutData.EDGE_LABEL_LAYOUT_KEY to the
 * input graph. Alternatively one can use the layout stage
 * y.layout.LabelLayoutTranslator as the LabelLayouter of HierarchicLayouter.
 * In the latter case the initial label information will be automatically
 * read from the EdgeLabelLayout information of the input graph 
 * ( getEdgeLabelLayout(Edge e) ). It will also be written back to the 
 * EdgeLabelLayout of the graph.  
 * <br>
 * Groupings of nodes are expressed through the means of integer values.
 * Each node *can* be assigned an integer value, which determines the group
 * to which it belongs. The layouter will query the ClassicalLayerSequencer.GROUP_KEY
 * NodeDataProvider from the graph and will layout the nodes 
 */

public class HierarchicLayouterDemo extends DemoBase {
  private static final String EDGE_LABELING = "EDGE_LABELING";
  private static final String HIERARCHIC = "HIERARCHIC";
  private static final String NONE = "NONE";

  private PortSpec sourceSpec, targetSpec;
  private EdgeMap sourceGroupIdMap, targetGroupIdMap;
  private JCheckBox labelBox;
  private EdgeMap sourcePortMap;
  private EdgeMap targetPortMap;

  private HierarchicLayoutModule layoutModule;

  private NodeMap groupMap;
  private PortAssignmentMoveSelectionMode paMode;

  public HierarchicLayouterDemo() {
    final Graph2D graph = view.getGraph2D();
    groupMap = graph.createNodeMap();
    graph.addDataProvider(ClassicLayerSequencer.GROUP_KEY, groupMap);
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);

    sourcePortMap = graph.createEdgeMap();
    targetPortMap = graph.createEdgeMap();
    sourceGroupIdMap = graph.createEdgeMap();
    targetGroupIdMap = graph.createEdgeMap();
    graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sourceGroupIdMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, targetGroupIdMap);
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);

    paMode.setSpc(sourcePortMap);
    paMode.setTpc(targetPortMap);


    JPanel left = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 0;

    left.add(new JButton(new AbstractAction("Parse Ports") {
      public void actionPerformed(ActionEvent ae) {
        new PortConstraintConfigurator()
            .createPortConstraintsFromSketch(graph, sourcePortMap, targetPortMap);
      }
    }), gbc);

    gbc.gridx = 1;

    labelBox = new JCheckBox("Label Edges");
    labelBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        setLabelEdges(labelBox.isSelected());
      }
    });

    left.add(labelBox);

    sourceSpec = new PortSpec("Source Port", sourcePortMap, sourceGroupIdMap);
    targetSpec = new PortSpec("Target Port", targetPortMap, targetGroupIdMap);
    gbc.gridx = 0;
    gbc.gridy = 1;
    left.add(sourceSpec, gbc);
    gbc.gridx = 1;
    left.add(targetSpec, gbc);
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;

    JPanel groupSpec;
    groupSpec = new JPanel(new GridBagLayout());
    groupSpec.setBorder(BorderFactory.createTitledBorder("Node Groups"));

    // build the grouping mechanism
    Color[] groupColors = new Color[]{null, Color.blue, Color.yellow, Color.red};

    for (int i = 0; i < groupColors.length; i++) {
      JButton groupButton = new GroupButton(groupColors[i], i);
      groupSpec.add(groupButton, gbc);
    }
    gbc.weightx = gbc.weighty = 1;
    groupSpec.add(new JPanel(), gbc);
    gbc.weightx = gbc.weighty = 0;
    gbc.gridwidth = 2;
    left.add(groupSpec, gbc);
    gbc.gridwidth = 1;

    gbc.weighty = 1.0d;
    left.add(new JPanel(), gbc);

    contentPane.add(new JScrollPane(left), BorderLayout.WEST);

    graph.addGraph2DSelectionListener(new Graph2DSelectionListener() {
      public void onGraph2DSelectionEvent(Graph2DSelectionEvent ev) {
        if (ev.getSubject() instanceof Edge) {
          Edge e = (Edge) ev.getSubject();
          if (ev.getGraph2D().isSelected(e)) {
            PortConstraint pc = getSPC(e);
            sourceSpec.setSide(pc.getSide());
            sourceSpec.setStrong(pc.isStrong());
            sourceSpec.setGroupId(sourceGroupIdMap.get(e));
            pc = getTPC(e);
            targetSpec.setSide(pc.getSide());
            targetSpec.setStrong(pc.isStrong());
            targetSpec.setGroupId(targetGroupIdMap.get(e));
          }
        }
      }
    });

    layoutModule = new HierarchicLayoutModule();
    loadGraph("resource/portConstraints.graphml");
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.add(new LayoutAction());
    bar.add(new OptionAction());
    return bar;
  }

  /**
   * this method assigns the group id and the corresponding color hint
   * to the currently selected nodes
   */
  protected void assignGroup(Color color, int index) {
    Graph2D graph = view.getGraph2D();
    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      if (color == null) {
        color = graph.getDefaultNodeRealizer().getFillColor();
        // unset the actual group index
        groupMap.set(n, null);
      } else {
        // set the actual group index
        groupMap.setInt(n, index);
      }
      // set the color hint
      graph.getRealizer(n).setFillColor(color);

    }
    graph.updateViews();
  }


  // helper class
  class GroupButton extends JButton implements ActionListener {
    Color color;
    int index;

    GroupButton(Color color, int index) {
      super("");
      setText(index > 0 ? "Group " + index : "No Group");
      this.color = color;
      this.index = index;
      setBackground(color);
      this.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
      HierarchicLayouterDemo.this.assignGroup(color, index);
    }
  }

  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout");
    }

    public void actionPerformed(ActionEvent ev) {

      Graph2D graph = view.getGraph2D();
      //for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
      //{
      //  Edge e = ec.edge();
      //  EdgeRealizer er = graph.getRealizer(e);
      //if(er.labelCount() >= 2)
      //{
      //  er.getLabel(0).setPreferredPlacement((byte)(EdgeLabel.PLACE_AT_SOURCE | EdgeLabel.PLACE_RIGHT_OF_EDGE));
      //  er.getLabel(1).setPreferredPlacement((byte)(EdgeLabel.PLACE_AT_TARGET | EdgeLabel.PLACE_RIGHT_OF_EDGE));
      //}
      //}
      layoutModule.start(graph);
    }
  }

  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Option...");
    }

    public void actionPerformed(ActionEvent ev) {
      layoutModule.getOptionHandler().showEditor();
    }
  }


  PortConstraint getSPC(Edge e) {
    PortConstraint pc = (e != null) ? (PortConstraint) sourcePortMap.get(e) : null;

    if (pc == null) {
      pc = PortConstraint.create(PortConstraint.ANY_SIDE);
    }

    return pc;
  }

  PortConstraint getTPC(Edge e) {
    PortConstraint pc = (e != null) ? (PortConstraint) targetPortMap.get(e) : null;

    if (pc == null) {
      pc = PortConstraint.create(PortConstraint.ANY_SIDE);
    }

    return pc;
  }

  void setPC(EdgeMap portMap, EdgeMap groupMap, byte side, boolean strong, Object groupId) {
    Graph2D graph = view.getGraph2D();
    PortConstraint pc = PortConstraint.create(side, strong);
    String preText = pc.toString();
    if (groupId != null) {
      preText = preText + '[' + groupId + ']';
    }
    for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      portMap.set(e, pc);
      getSPC(e);
      getTPC(e);
      groupMap.set(e, groupId);
      if (graph.getRealizer(e).labelCount() >= 2) {
        String text = portMap == sourcePortMap ? "source " + preText : "target" + preText;
        graph.getRealizer(e).getLabel(portMap == sourcePortMap ? 0 : 1).setText(text);
      }
    }
    graph.updateViews();
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new HierarchicLayouterDemo()).start("Hierarchic Layouter Demo");
      }
    });
  }


  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioh = super.createGraphMLIOHandler();
    ioh.getGraphMLHandler().addOutputHandlerProvider(new OutputHandlerProvider() {
      public void onQueryOutputHandler(QueryOutputHandlersEvent event) throws GraphMLWriteException {
        boolean isRegistered = false;
        Graph g = event.getContext().getGraph();
        Object[] keys = g.getDataProviderKeys();
        for (int i = 0; i < keys.length; i++) {
          Object key = keys[i];
          if(PortConstraintKeys.SOURCE_GROUPID_KEY.equals(key)  ||
              PortConstraintKeys.TARGET_GROUPID_KEY.equals(key) ||
              PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY.equals(key) ||
              PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY.equals(key)) {
            isRegistered = true;
            break;
          }
        }
        if (isRegistered) {
          event.addOutputHandler(new PortConstraintOutputHandler(), KeyScope.EDGE);
        }
      }
    });

    ioh.getGraphMLHandler().addInputHandlerProvider(new InputHandlerProvider() {
      public void onQueryInputHandler(QueryInputHandlersEvent event) throws GraphMLParseException {
        Element keyDefinition = event.getKeyDefinition();
        PortConstraintInputHandler handler = new PortConstraintInputHandler();

        if (handler.acceptKey(keyDefinition)) {
          initDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, event.getContext().getGraph());
          initDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, event.getContext().getGraph());
          initDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, event.getContext().getGraph());
          initDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, event.getContext().getGraph());
          event.addInputHandler(handler);
        }
      }

      private void initDataProvider(Object key, Graph graph) {
        DataProvider dp = graph.getDataProvider(key);
        if (dp == null || !(dp instanceof EdgeMap)) {
          dp = Maps.createEdgeMap(new WeakHashMap());
          graph.addDataProvider(key, dp);
        }
      }
    });
    return ioh;
  }

  class PortSpec extends JPanel {
    JRadioButton anySideB, northB, southB, eastB, westB;
    JCheckBox cb;
    JComboBox box;
    EdgeMap portMap;
    EdgeMap groupMap;

    Object[] items = new Object[]{"no Group", new Integer(1), new Integer(2), new Integer(3), new Integer(4),
        new Integer(5), new Integer(6), new Integer(7)};

    PortSpec(String title, EdgeMap portMap, EdgeMap groupMap) {
      super(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      this.setBorder(BorderFactory.createTitledBorder(title));
      this.portMap = portMap;
      this.groupMap = groupMap;
      final ButtonGroup bg = new ButtonGroup();


      anySideB = new JRadioButton("ANY_SIDE");
      northB = new JRadioButton("NORTH");
      southB = new JRadioButton("SOUTH");
      eastB = new JRadioButton("EAST");
      westB = new JRadioButton("WEST");
      anySideB.setSelected(true);

      cb = new JCheckBox("Strong");


      box = new JComboBox(items);

      ActionListener rl = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          ButtonModel bm = bg.getSelection();
          byte side = PortConstraint.ANY_SIDE;
          if (bm == southB.getModel()) {
            side = PortConstraint.SOUTH;
          } else if (bm == northB.getModel()) {
            side = PortConstraint.NORTH;
          } else if (bm == eastB.getModel()) {
            side = PortConstraint.EAST;
          } else if (bm == westB.getModel()) {
            side = PortConstraint.WEST;
          }
          boolean strong = cb.isSelected();
          Object groupId = box.getSelectedItem();
          if (groupId instanceof String) {
            groupId = null;
          }
          setPC(PortSpec.this.portMap, PortSpec.this.groupMap, side, strong, groupId);
        }
      };

      addButton(anySideB, bg, rl, gbc);
      addButton(northB, bg, rl, gbc);
      addButton(southB, bg, rl, gbc);
      addButton(eastB, bg, rl, gbc);
      addButton(westB, bg, rl, gbc);
      this.add(cb, gbc);
      this.add(box, gbc);
      box.addActionListener(rl);
      cb.addActionListener(rl);
    }

    void addButton(JRadioButton b, ButtonGroup bg, ActionListener rl, GridBagConstraints gbc) {
      bg.add(b);
      this.add(b, gbc);
      b.addActionListener(rl);
    }

    void setSide(byte side) {
      switch (side) {
        case PortConstraint.ANY_SIDE:
          anySideB.setSelected(true);
          break;
        case PortConstraint.NORTH:
          northB.setSelected(true);
          break;
        case PortConstraint.SOUTH:
          southB.setSelected(true);
          break;
        case PortConstraint.WEST:
          westB.setSelected(true);
          break;
        case PortConstraint.EAST:
          eastB.setSelected(true);
          break;
      }
    }

    void setStrong(boolean strong) {
      cb.setSelected(strong);
    }

    void setGroupId(Object id) {
      if (id == null) {
        box.setSelectedIndex(0);
      } else {
        box.setSelectedItem(id);
      }
    }
  }

  void setLabelEdges(boolean labelEdges) {
    Graph2D graph = view.getGraph2D();

    if (labelEdges) {
      addLabels(graph.getDefaultEdgeRealizer());
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        addLabels(graph.getRealizer(ec.edge()));
      }
      layoutModule.getOptionHandler().set(EDGE_LABELING, HIERARCHIC);
    } else {
      removeLabels(graph.getDefaultEdgeRealizer());
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        removeLabels(graph.getRealizer(ec.edge()));
      }
      layoutModule.getOptionHandler().set(EDGE_LABELING, NONE);
    }

    view.updateView();

  }


  void addLabels(EdgeRealizer er) {
    removeLabels(er);

    EdgeLabel el = new EdgeLabel(getSPC(er.getEdge()).toString());
    el.setModel(EdgeLabel.CENTER_SLIDER);
    el.setPosition(EdgeLabel.SCENTR);
    el.setPreferredPlacement(LabelLayoutConstants.PLACE_AT_SOURCE);
    er.addLabel(el);


    el = new EdgeLabel(getTPC(er.getEdge()).toString());
    el.setModel(EdgeLabel.CENTER_SLIDER);
    el.setPosition(EdgeLabel.TCENTR);
    el.setPreferredPlacement(LabelLayoutConstants.PLACE_AT_TARGET);
    er.addLabel(el);

    el = new EdgeLabel("Center");
    el.setModel(EdgeLabel.CENTER_SLIDER);
    el.setPosition(EdgeLabel.CENTER);
    el.setPreferredPlacement(LabelLayoutConstants.PLACE_AT_CENTER);
    er.addLabel(el);
  }

  void removeLabels(EdgeRealizer er) {
    while (er.labelCount() > 0) {
      er.removeLabel(er.getLabel(er.labelCount() - 1));
    }
  }

  protected void registerViewModes() {
    EditMode mode = new EditMode();
    mode.setMoveSelectionMode(paMode = new PortAssignmentMoveSelectionMode(null, null));
    view.addViewMode(mode);
  }
}
