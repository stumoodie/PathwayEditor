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
package demo.layout.tree;

import demo.view.DemoDefaults;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.tree.AbstractRotatableNodePlacer;
import y.layout.tree.AbstractRotatableNodePlacer.Matrix;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.BusPlacer;
import y.layout.tree.DoubleLinePlacer;
import y.layout.tree.LeftRightPlacer;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.LineType;
import y.view.PolyLineEdgeRealizer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This demo presents GenericTreeLayouter in conjunction with {@link NodePlacer}s that support
 * subtree rotation. The NodePlacers, rotations and root alignments for the selected nodes may
 * be changed using the panel on the left side.
 **/
public class RotatableNodePlacersDemo extends AbstractTreeDemo {
  private JComboBox nodePlacerCombo;
  private JComboBox rootAlignmentCombo;
  private JButton rotLeftButton;
  private JButton rotRightButton;
  private JButton mirHorButton;
  private JButton mirVertButton;

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new RotatableNodePlacersDemo()).start();
      }
    });
  }

  public RotatableNodePlacersDemo() {
    Graph2D graph = view.getGraph2D();

    graph.addGraph2DSelectionListener(new Graph2DSelectionListener() {
      public void onGraph2DSelectionEvent(Graph2DSelectionEvent e) {
        if (view.getGraph2D().selectedNodes().ok()) {
          readComboValues();
        } else {
          setEnabled(false);
        }
      }
    });

    //Realizers
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);
    ((PolyLineEdgeRealizer) defaultER).setSmoothedBends(true);
    defaultER.setLineType(LineType.LINE_2);


    JPanel configPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(10, 10, 10, 10);
    configPanel.add(new JLabel("Settings for actual selection"), constraints);

    constraints.gridy = 1;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("NodePlacer:"), constraints);

    constraints.gridy = 2;
    constraints.insets = new Insets(0, 0, 0, 0);
    nodePlacerCombo = new JComboBox();
    nodePlacerCombo.addItem("SimpleNodePlacer");
    nodePlacerCombo.addItem("DoubleLinePlacer");
    nodePlacerCombo.addItem("BusPlacer");
    nodePlacerCombo.addItem("LeftRightPlacer");
    nodePlacerCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          changeNodePlacersForSelection();
        }
      }
    });
    configPanel.add(nodePlacerCombo, constraints);

    constraints.gridy = 3;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("Rotation:"), constraints);

    constraints.gridy = 4;
    constraints.insets = new Insets(0, 0, 0, 0);

    JPanel rotationPanel = new JPanel();
    configPanel.add(rotationPanel, constraints);
    rotationPanel.setLayout(new FlowLayout());
    rotLeftButton = new JButton("Left");
    rotLeftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.ROT90);
      }
    });
    rotationPanel.add(rotLeftButton);
    rotRightButton = new JButton("Right");
    rotRightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.ROT270);
      }
    });
    rotationPanel.add(rotRightButton);

    constraints.gridy = 6;
    rotationPanel = new JPanel();
    configPanel.add(rotationPanel, constraints);
    rotationPanel.setLayout(new FlowLayout());
    mirHorButton = new JButton("Mir Hor");
    mirHorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.MIR_HOR);
      }
    });
    rotationPanel.add(mirHorButton);
    mirVertButton = new JButton("Mir Vert");
    mirVertButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.MIR_VERT);
      }
    });
    rotationPanel.add(mirVertButton);


    constraints.gridy = 7;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("Root Alignment:"), constraints);

    constraints.gridy = 8;
    constraints.insets = new Insets(0, 0, 0, 0);
    rootAlignmentCombo = new JComboBox();

    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.CENTER);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.CENTER_OVER_CHILDREN);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.MEDIAN);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.LEADING);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.LEFT);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.RIGHT);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.TRAILING);
    rootAlignmentCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          changeNodePlacersForSelection();
        }
      }
    });
    rootAlignmentCombo.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                    boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        RootAlignment rootAlignment = (RootAlignment) value;
        if (rootAlignment == RootAlignment.CENTER) {
          label.setText("Center");
        }
        if (rootAlignment == RootAlignment.LEADING) {
          label.setText("Leading");
        }
        if (rootAlignment == RootAlignment.LEFT) {
          label.setText("Left");
        }
        if (rootAlignment == RootAlignment.RIGHT) {
          label.setText("Right");
        }
        if (rootAlignment == RootAlignment.TRAILING) {
          label.setText("Trailing");
        }
        if (rootAlignment == RootAlignment.MEDIAN) {
          label.setText("Median");
        }
        if (rootAlignment == RootAlignment.CENTER_OVER_CHILDREN) {
          label.setText("Center over children");
        }
        return label;
      }
    });
    configPanel.add(rootAlignmentCombo, constraints);

    JPanel left = new JPanel(new BorderLayout());
    left.add(configPanel, BorderLayout.NORTH);

    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, view);
    sp.setOneTouchExpandable(true);
    sp.setContinuousLayout(false);
    contentPane.add(sp, BorderLayout.CENTER);
    createSampleGraph(graph);

    setEnabled(false);

    createSampleGraph(view.getGraph2D());
    calcLayout();
  }

  private void setEnabled(boolean enabled) {
    rootAlignmentCombo.setEnabled(enabled);
    rotLeftButton.setEnabled(enabled);
    rotRightButton.setEnabled(enabled);
    mirHorButton.setEnabled(enabled);
    mirVertButton.setEnabled(enabled);
    nodePlacerCombo.setEnabled(enabled);
  }

  private void rotate(Matrix rotation) {
    RootAlignment rootAlignment = (RootAlignment) rootAlignmentCombo.getSelectedItem();

    String placerName = (String) nodePlacerCombo.getSelectedItem();

    for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      AbstractRotatableNodePlacer oldPlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      Matrix matrix = oldPlacer == null ? rotation.multiply(Matrix.DEFAULT) : rotation.multiply(
          oldPlacer.getModificationMatrix());

      AbstractRotatableNodePlacer placer = createPlacer(placerName, matrix, rootAlignment);
      nodePlacerMap.set(node, placer);
    }
    calcLayout();
  }

  private boolean blockLayout;

  private void readComboValues() {
    blockLayout = true;

    setEnabled(true);

    NodeCursor nodeCursor = view.getGraph2D().selectedNodes();
    if (!nodeCursor.ok()) {
      setEnabled(false);
    } else {
      Node node = nodeCursor.node();

      AbstractRotatableNodePlacer nodePlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      if (nodePlacer == null) {
        return;
      }

      nodePlacerCombo.setSelectedItem(nodePlacer.getClass());

      if (nodePlacer instanceof SimpleNodePlacer) {
        rootAlignmentCombo.setSelectedItem(((SimpleNodePlacer) nodePlacer).getRootAlignment());
      }
      if (nodePlacer instanceof DoubleLinePlacer) {
        rootAlignmentCombo.setSelectedItem(((DoubleLinePlacer) nodePlacer).getRootAlignment());
      }
    }

    blockLayout = false;
  }

  private void changeNodePlacersForSelection() {
    if (blockLayout) {
      return;
    }
    RootAlignment rootAlignment = (RootAlignment) rootAlignmentCombo.getSelectedItem();

    String placerName = (String) nodePlacerCombo.getSelectedItem();

    for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      AbstractRotatableNodePlacer oldPlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      Matrix matrix = oldPlacer != null ? oldPlacer.getModificationMatrix() : AbstractRotatableNodePlacer.Matrix.DEFAULT;

      AbstractRotatableNodePlacer placer = createPlacer(placerName, matrix, rootAlignment);
      nodePlacerMap.set(node, placer);
    }
    calcLayout();
  }

  private static AbstractRotatableNodePlacer createPlacer(String placerName, Matrix modificationMatrix,
                                                          RootAlignment rootAlignment) {
    AbstractRotatableNodePlacer placer = null;
    if ("SimpleNodePlacer".equals(placerName)) {
      placer = new SimpleNodePlacer(modificationMatrix);
      ((SimpleNodePlacer) placer).setRootAlignment(rootAlignment);
    } else if ("DoubleLinePlacer".equals(placerName)) {
      placer = new DoubleLinePlacer(modificationMatrix);
      ((DoubleLinePlacer) placer).setRootAlignment(rootAlignment);
    } else if ("BusPlacer".equals(placerName)) {
      placer = new BusPlacer(modificationMatrix);
    } else if ("LeftRightPlacer".equals(placerName)) {
      placer = new LeftRightPlacer(modificationMatrix);
    }
    return placer;
  }

  protected boolean isDeletionEnabled() {
    return false;
  }

  private void createSampleGraph(Graph2D graph) {
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer(root).setFillColor(layerColors[0]);
    nodePlacerMap.set(root, new SimpleNodePlacer());
    createChildren(graph, root, 3, 1, 2);
    calcLayout();
  }

  private void createChildren(Graph2D graph, Node root, int children, int layer, int layers) {
    for (int i = 0; i < children; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[layer % layerColors.length]);

      Matrix rotationMatrix;
      if (layer == 1) {
        rotationMatrix = Matrix.MIR_VERT_ROT90;
      } else {
        rotationMatrix = Matrix.DEFAULT;
      }
      if (layers > 0) {
        SimpleNodePlacer nodePlacer = new SimpleNodePlacer(rotationMatrix);
        nodePlacerMap.set(child, nodePlacer);
      }
      if (layers > 0) {
        createChildren(graph, child, children, layer + 1, layers - 1);
      }
    }
  }
}
