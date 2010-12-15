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
package demo.view.realizer;

import demo.view.DemoBase;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.geom.YInsets;
import y.layout.LayoutOrientation;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.LayerConstraintFactory;
import y.view.AbstractCustomNodePainter;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.LineType;
import y.view.MultiplexingNodeEditor;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodePainter;
import y.view.YRenderingHints;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.Table;
import y.view.tabular.TableNodePainter;
import y.view.tabular.TableSelectionEditor;
import y.view.tabular.TableStyle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * Demonstrates different visual styles for table groups and their content.
 *
 */
public class TableStyleDemo extends DemoBase {
  private static final String CONFIGURATION_POOL_GRADIENT = "POOL_GRADIENT";
  private static final String CONFIGURATION_POOL_ALTERNATING = "POOL_ALTERNATING";
  private static final String CONFIGURATION_POOL_GENERIC = "POOL_GENERIC";
  private static final String CONFIGURATION_POOL_BPMN_STYLE = "POOL_BPMN_STYLE";
  private static final String CONFIGURATION_GRADIENT_RECT = "GRADIENT_RECT";
  private static final String CONFIGURATION_GRADIENT_ROUNDRECT = "GRADIENT_ROUNDRECT";
  private static final String CONFIGURATION_GRADIENT_DIAMOND = "GRADIENT_DIAMOND";
  private static final String CONFIGURATION_GRADIENT_ELLIPSE = "GRADIENT_ELLIPSE";
  private static final String CONFIGURATION_SIMPLE_ROUNDRECT = "SIMPLE_ROUNDRECT";
  private static final String CONFIGURATION_SIMPLE_DIAMOND = "SIMPLE_DIAMOND";
  private static final String CONFIGURATION_SIMPLE_ELLIPSE = "SIMPLE_ELLIPSE";

  private static final Color MAGENTA = new Color(253, 0, 127);
  private static final Color ORANGE = new Color(249, 134, 5);
  private static final Color DARK_GRAY = new Color(132, 131, 129);
  private static final Color GREEN = new Color(156, 210, 60);
  private static final Color PASTEL_YELLOW = new Color(254, 254, 212);
  private static final Color PASTEL_GREEN = new Color(212, 254, 228);
  private static final Color PASTEL_BLUE = new Color(212, 228, 254);
  private static final Color LIGHT_BLUE = new Color(161, 188, 255);
  private static final Color DARK_GREEN = new Color(98, 167, 22);
  private static final Color BLOOD_RED = new Color(153, 0, 0);

  static {
    initConfigurations();
  }


  public TableStyleDemo() {
    initGraph(view.getGraph2D());
    createBpmnStyleSample(view.getGraph2D());
    view.fitContent();
  }

  /**
   * Creates an almost view-only edit mode. Almost view-only because
   * nodes may be selected and table nodes, columns, and rows may be resized.
   */
  protected EditMode createEditMode() {
    final EditMode editMode = new EditMode();
    editMode.allowBendCreation(false);
    editMode.allowEdgeCreation(false);
    editMode.allowLabelSelection(false);
    editMode.allowMoveLabels(false);
    editMode.allowMovePorts(false);
    editMode.allowMoveSelection(false);
    editMode.allowMoving(false);
    editMode.allowMovingWithPopup(false);
    editMode.allowNodeCreation(false);
    editMode.allowNodeEditing(false);
    editMode.allowResizeNodes(false);

    // activate node specific user interaction
    // e.g. TableGroupNodeRealizer usually is configured to recognize mouse
    // gestures for selecting and resizing tables, columns, and rows as well
    // as reordering columns and rows
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    return editMode;
  }

  /**
   * Creates a {@link y.view.hierarchy.HierarchyManager} for the specified
   * graph.
   * @param graph   the graph to configure.
   */
  private void initGraph( final Graph2D graph ) {
    new HierarchyManager(graph);
  }

  /**
   * Creates a sample graph depicting a table node that uses
   * {@link demo.view.realizer.TableStyleDemo.GradientRowPainter} as a custom
   * row painter.
   * @param graph   the graph to configure.
   */
  private void createGradientSample( final Graph2D graph ) {
    graph.clear();


    // create the realizer for the table node
    final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
    tgnr.setConfiguration(CONFIGURATION_POOL_GRADIENT);
    tgnr.setLocation(0, 0);
    tgnr.setAutoResize(true);

    tgnr.setFillColor(Color.LIGHT_GRAY);
    // this color is used by GradientRowPainter together with a row specific
    // color to create a gradient fill for each row
    tgnr.setFillColor2(Color.WHITE);

    tgnr.setDefaultColumnInsets(new YInsets(0, 5, 0, 5));
    tgnr.setDefaultColumnWidth(400);
    tgnr.setDefaultRowHeight(100);
    tgnr.setDefaultRowInsets(new YInsets(5, 20, 5, 0));


    final Table dt = tgnr.getTable();

    // create one labeled row for each of the four colors
    final Color[] colors = {GREEN, DARK_GRAY, ORANGE, MAGENTA};
    final double rowSizeAdjustment = 10;
    final HashMap row2color = new HashMap();
    for (int i = 0; i < colors.length; ++i) {
      final Row row;
      if (i == 0) {
        row = dt.getRow(0);
      } else {
        row = dt.addRow();
      }
      row2color.put(row, colors[i]);

      final NodeLabel label = tgnr.createNodeLabel();
      label.setText("Lane " + (i + 1));
      final double minHeight = label.getWidth() + rowSizeAdjustment;
      row.setMinimumHeight(minHeight);

      // associate the label to the row
      // the ratio value of 0 means the label will be left-aligned (regarding
      // the row) and rotated 90 degress counter clockwise
      tgnr.configureRowLabel(label, row, true, 0);

      // row labels are normal node labels and have to be explicitly added
      // to the realizer as usual
      tgnr.addLabel(label);
    }

    // sets the style property that is used by GradientRowPainter to determine
    // the color that defines the gradient fill for each row
    tgnr.setStyleProperty(
            GradientRowPainter.STYLE_ROW_COLOR_MAP,
            new GradientRowPainter.RowColorMap() {
      public Color getColor( final Row row ) {
        return (Color) row2color.get(row);
      }
    });

    // ensure that the table has the correct (sufficiently large) size
    tgnr.updateTableBounds();


    final HierarchyManager hm = HierarchyManager.getInstance(graph);

    // create a group node ..
    final Node pool = hm.createGroupNode(graph);
    // .. and assign it the previously created table realizer
    graph.setRealizer(pool, tgnr);




    // prototype realizer for rectangular child nodes
    final GenericNodeRealizer rectangle = new GenericNodeRealizer();
    rectangle.setConfiguration(CONFIGURATION_GRADIENT_RECT);
    rectangle.setSize(80, 60);
    rectangle.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    rectangle.setFillColor(Color.WHITE);

    // prototype realizer for rectangular child nodes with rounded corners
    final GenericNodeRealizer roundRect = new GenericNodeRealizer();
    roundRect.setConfiguration(CONFIGURATION_GRADIENT_ROUNDRECT);
    roundRect.setSize(80, 60);
    roundRect.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    roundRect.setFillColor(Color.WHITE);
    roundRect.setFillColor2(PASTEL_BLUE);

    // prototype realizer for circular child nodes
    final GenericNodeRealizer ellipse = new GenericNodeRealizer();
    ellipse.setConfiguration(CONFIGURATION_GRADIENT_ELLIPSE);
    ellipse.setSize(60, 60);
    ellipse.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    ellipse.setFillColor(Color.WHITE);
    ellipse.setFillColor2(PASTEL_YELLOW);

    // prototype realizer for diamond-shaped child nodes
    final GenericNodeRealizer diamond = new GenericNodeRealizer();
    diamond.setConfiguration(CONFIGURATION_GRADIENT_DIAMOND);
    diamond.setSize(60, 60);
    diamond.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    diamond.setFillColor(Color.WHITE);
    diamond.setFillColor2(PASTEL_GREEN);


    // create some nodes with different realizers
    final Node[] nodes = new Node[22];
    for (int i = 0; i < nodes.length; ++i) {
      if (i < 5 ) {
        if (i % 2 == 0) {
          nodes[i] = graph.createNode(ellipse.createCopy());
        } else {
          nodes[i] = graph.createNode(rectangle.createCopy());
        }
      } else if (i == 6 || i == 11 || i == 12) {
        nodes[i] = graph.createNode(diamond.createCopy());
      } else if (i == 5 || i == 19 || (i > 8 && i < 18)) {
        nodes[i] = graph.createNode(roundRect.createCopy());
      } else {
        nodes[i] = graph.createNode(rectangle.createCopy());
      }
    }


    // label the recently created nodes and assign them to the previously
    // created group node
    for (int i = 0; i < nodes.length; ++i) {
      graph.getRealizer(nodes[i]).setLabelText(Integer.toString(i + 1));

      // important: assign the node to the table group
      hm.setParentNode(nodes[i], pool);

      // assign the child nodes to different rows
      if (i < 5) {
        dt.moveToRow(nodes[i], dt.getRow(0));
      } else if (i < 9) {
        dt.moveToRow(nodes[i], dt.getRow(1));
      } else if (i < 18) {
        dt.moveToRow(nodes[i], dt.getRow(2));
      } else {
        dt.moveToRow(nodes[i], dt.getRow(3));
      }
    }


    // create some edges
    final Edge[] edges = {
      graph.createEdge(nodes[0], nodes[5]),
      graph.createEdge(nodes[2], nodes[14]),
      graph.createEdge(nodes[4], nodes[17]),

      graph.createEdge(nodes[5], nodes[6]),
      graph.createEdge(nodes[6], nodes[9]),
      graph.createEdge(nodes[6], nodes[10]),
      graph.createEdge(nodes[6], nodes[11]),

      graph.createEdge(nodes[9], nodes[12]),
      graph.createEdge(nodes[10], nodes[12]),
      graph.createEdge(nodes[11], nodes[12]),
      graph.createEdge(nodes[12], nodes[19]),
      graph.createEdge(nodes[13], nodes[19]),

      graph.createEdge(nodes[19], nodes[20]),
      graph.createEdge(nodes[19], nodes[21]),
    };
//    for (int i = 0; i < edges.length; ++i) {
//      graph.getRealizer(edges[i]).setLabelText(Integer.toString(i));
//    }


    // setup port constraints for diamond-shaped child nodes

    // create the corresponding port constraints ...
    final EdgeMap srcPc = graph.createEdgeMap();
    srcPc.set(edges[4], PortConstraint.create(PortConstraint.WEST));
    srcPc.set(edges[5], PortConstraint.create(PortConstraint.EAST));
    final EdgeMap tgtPc = graph.createEdgeMap();
    tgtPc.set(edges[7], PortConstraint.create(PortConstraint.WEST));
    tgtPc.set(edges[8], PortConstraint.create(PortConstraint.EAST));

    // ... and register the port constraints
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, srcPc);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tgtPc);


    try {
      layout(graph, true);
    } finally {

      graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      graph.disposeEdgeMap(tgtPc);
      graph.disposeEdgeMap(srcPc);
    }
  }

  /**
   * Creates a sample graph depicting a table node that uses alternating
   * colors to paint its columns.
   * @param graph   the graph to configure.
   */
  private void createAlternatingSample( final Graph2D graph ) {
    graph.clear();


    // create the realizer for the table node
    final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
    tgnr.setConfiguration(CONFIGURATION_POOL_ALTERNATING);
    tgnr.setLocation(0, 0);
    tgnr.setAutoResize(true);

    tgnr.setDefaultColumnInsets(new YInsets(20, 5, 0, 5));
    tgnr.setDefaultColumnWidth(100);
    tgnr.setDefaultRowHeight(100);
    tgnr.setDefaultRowInsets(new YInsets(30, 0, 10, 0));


    final Table dt = tgnr.getTable();
    dt.setInsets(new YInsets(30, 5, 5, 5));

    // create couple of columns in the table model
    final List cols = dt.getColumns();
    dt.addColumn();
    dt.addColumn();
    dt.addColumn();
    final Column[] columns = new Column[cols.size()];
    cols.toArray(columns);

    final Row row0 = dt.getRow(0);


    final NodeLabel label = tgnr.getLabel();
    label.setText("Pool");
    label.setPosition(NodeLabel.TOP);

    // configure the columns:
    //  - assign each column a label
    //  - set a suitable minimum size (width) to each column
    final double columnSizeAdjustment = 10;
    int i = 1;
    for (Iterator it = cols.iterator(); it.hasNext(); ++i) {
      final Column column = (Column) it.next();
      final NodeLabel columnLabel = tgnr.createNodeLabel();
      columnLabel.setText("Lane " + i);
      final double minWidth = columnLabel.getWidth() + columnSizeAdjustment;
      column.setMinimumWidth(minWidth);

      // associate the label to the column
      tgnr.configureColumnLabel(columnLabel, column, true, 0);

      // column labels are normal node labels and have to be explicitly added
      // to the realizer as usual
      tgnr.addLabel(columnLabel);
    }

    // ensure that the table has the correct (sufficiently large) size
    tgnr.updateTableBounds();


    final HierarchyManager hm = HierarchyManager.getInstance(graph);


    // create a group node ..
    final Node pool = hm.createGroupNode(graph);
    // .. and assign it the previously created table realizer
    graph.setRealizer(pool, tgnr);


    // prototype realizer for child nodes
    final GenericNodeRealizer prototype = new GenericNodeRealizer();
    prototype.setConfiguration(CONFIGURATION_GRADIENT_RECT);
    prototype.setSize(90, 60);
    prototype.setCenter(
            columns[0].calculateBounds().getCenterX(),
            row0.calculateBounds().getCenterY());
    prototype.setFillColor(Color.WHITE);
    prototype.setFillColor2(PASTEL_BLUE);

    // create a couple of child nodes with different shapes
    final String[] configurations = {
            CONFIGURATION_GRADIENT_RECT,
            CONFIGURATION_GRADIENT_DIAMOND,
            CONFIGURATION_GRADIENT_DIAMOND,
            CONFIGURATION_GRADIENT_DIAMOND,
            CONFIGURATION_GRADIENT_RECT,
            CONFIGURATION_GRADIENT_RECT,
            CONFIGURATION_GRADIENT_RECT,
            CONFIGURATION_GRADIENT_RECT,
    };
    final Node[] nodes = new Node[configurations.length];
    for (int j = 0; j < nodes.length; ++j) {
      final GenericNodeRealizer nr = new GenericNodeRealizer(prototype);
      nr.setConfiguration(configurations[j]);
      nr.setLabelText(Integer.toString(j + 1));
      nodes[j] = graph.createNode(nr);

      // important: assign the node to the table group
      hm.setParentNode(nodes[j], pool);

      // move the new node into (the first and only row of the) the table
      dt.moveToRow(nodes[j], row0);
    }

    // distribute the child nodes over the table
    dt.moveToColumn(nodes[0], columns[3]);
    dt.moveToColumn(nodes[1], columns[2]);
    dt.moveToColumn(nodes[2], columns[1]);
    dt.moveToColumn(nodes[4], columns[2]);
    dt.moveToColumn(nodes[7], columns[3]);


    // couple of edges
    final Edge[] edges = {
      graph.createEdge(nodes[0], nodes[1]),
      graph.createEdge(nodes[1], nodes[0]),
      graph.createEdge(nodes[1], nodes[2]),
      graph.createEdge(nodes[2], nodes[3]),
      graph.createEdge(nodes[2], nodes[4]),
      graph.createEdge(nodes[3], nodes[5]),
      graph.createEdge(nodes[3], nodes[6]),
      graph.createEdge(nodes[4], nodes[6]),
      graph.createEdge(nodes[4], nodes[7]),
      graph.createEdge(nodes[6], nodes[7]),
    };
//    for (int j = 0; j < edges.length; ++j) {
//      graph.getRealizer(edges[j]).setLabelText(Integer.toString(j));
//    }


    // setup port constraints for diamond-shaped child nodes

    // create the corresponding port constraints ...
    final EdgeMap srcPc = graph.createEdgeMap();
    srcPc.set(edges[1], PortConstraint.create(PortConstraint.EAST));
    srcPc.set(edges[3], PortConstraint.create(PortConstraint.WEST));
    srcPc.set(edges[4], PortConstraint.create(PortConstraint.EAST));
    srcPc.set(edges[5], PortConstraint.create(PortConstraint.WEST));
    srcPc.set(edges[6], PortConstraint.create(PortConstraint.EAST));
    final EdgeMap tgtPc = graph.createEdgeMap();
    tgtPc.set(edges[0], PortConstraint.create(PortConstraint.NORTH));

    // ... and register the port constraints
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, srcPc);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tgtPc);


    try {
      layout(graph, true);
    } finally {

      graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      graph.disposeEdgeMap(tgtPc);
      graph.disposeEdgeMap(srcPc);
    }
  }

  /**
   * Creates a sample graph depicting a table node that uses the default
   * painter configuration with custom row styles.
   * @param graph   the graph to configure.
   */
  private void createGenericSample( final Graph2D graph ) {
    graph.clear();


    final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
    tgnr.setConfiguration(CONFIGURATION_POOL_GENERIC);
    tgnr.setLocation(0, 0);
    tgnr.setAutoResize(true);

    // file color 2 is used by RowStyle as the row fill color
    tgnr.setFillColor2(LIGHT_BLUE);
    // register RowStyle as the style to be used when painting rows
    tgnr.setStyleProperty(TableNodePainter.ROW_STYLE_ID, new RowStyle(false));
    tgnr.setStyleProperty(TableNodePainter.ROW_SELECTION_STYLE_ID, new RowStyle(true));

    tgnr.setDefaultColumnInsets(new YInsets(0, 5, 0, 10));
    tgnr.setDefaultColumnWidth(100);
    tgnr.setDefaultRowInsets(new YInsets(5, 25, 5, 0));
    tgnr.setDefaultRowHeight(80);

    final Table table = tgnr.getTable();
    table.setInsets(new YInsets(10, 25, 10, 10));

    final Column column0 = table.getColumn(0);
    final Row row0 = table.getRow(0);
    final Row row1 = table.addRow();
    table.addRow();

    final NodeLabel label = tgnr.getLabel();
    label.setText("Pool");
    label.setPosition(NodeLabel.LEFT);
    label.setRotationAngle(270);

    // configure the rows:
    //  - assign each row a label
    //  - set a suitable minimum size (height) to each row
    final double rowSizeAdjustment = 10;
    int i = 1;
    for (Iterator it = table.getRows().iterator(); it.hasNext(); ++i) {
      final Row row = (Row) it.next();
      final NodeLabel rowLabel = tgnr.createNodeLabel();
      rowLabel.setText("Lane " + i);
      final double minHeight = rowLabel.getWidth() + rowSizeAdjustment;
      row.setMinimumHeight(minHeight);

      // associate the label to the row
      // the ratio value of 0 means the label will be left-aligned (regarding
      // the row) and rotated 90 degress counter clockwise
      tgnr.configureRowLabel(rowLabel, row, true, 0);

      // row labels are normal node labels and have to be explicitly added
      // to the realizer as usual
      tgnr.addLabel(rowLabel);
    }

    // ensure that the table has the correct (sufficiently large) size
    tgnr.updateTableBounds();


    final HierarchyManager hm = HierarchyManager.getInstance(graph);


    // create a group node ..
    final Node pool = hm.createGroupNode(graph);
    // .. and assign it the previously created table realizer
    graph.setRealizer(pool, tgnr);


    // prototype realizer for child nodes
    final GenericNodeRealizer prototype = new GenericNodeRealizer();
    prototype.setConfiguration(CONFIGURATION_GRADIENT_RECT);
    prototype.setSize(90, 60);
    prototype.setCenter(
            column0.calculateBounds().getCenterX(),
            row0.calculateBounds().getCenterY());
    prototype.setFillColor(Color.WHITE);
    prototype.setFillColor2(PASTEL_BLUE);

    // create a couple of child nodes
    final Node[] nodes = new Node[6];
    for (int j = 0; j < nodes.length; ++j) {
      final NodeRealizer nr = prototype.createCopy();
      nr.setLabelText(Integer.toString(j + 1));
      nodes[j] = graph.createNode(nr);

      // important: assign the node to the table group
      hm.setParentNode(nodes[j], pool);

      // move the row into the table's second row
      table.moveToRow(nodes[j], row1);
    }
    // move the first and last child to different rows
    table.moveToRow(nodes[0], row0);
    table.moveToRow(nodes[5], table.getRow(table.rowCount() - 1));


    // change the shape of one of the child nodes
    ((GenericNodeRealizer) graph.getRealizer(nodes[2]))
            .setConfiguration(CONFIGURATION_GRADIENT_DIAMOND);

    // create a couple of edges
    final Edge[] edges = {
      graph.createEdge(nodes[0], nodes[1]),
      graph.createEdge(nodes[1], nodes[2]),
      graph.createEdge(nodes[2], nodes[3]),
      graph.createEdge(nodes[2], nodes[4]),
      graph.createEdge(nodes[4], nodes[5]),
    };
//    for (int j = 0; j < edges.length; ++j) {
//      graph.getRealizer(edges[j]).setLabelText(Integer.toString(j));
//    }


    // setup port constraints for diamond-shaped child nodes

    // create the corresponding port constraints ...
    final EdgeMap srcPc = graph.createEdgeMap();
    srcPc.set(edges[2], PortConstraint.create(PortConstraint.NORTH));
    srcPc.set(edges[3], PortConstraint.create(PortConstraint.EAST));

    // ... and register the port constraints
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, srcPc);

    try {
      layout(graph, false);
    } finally {
      graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      graph.disposeEdgeMap(srcPc);
    }
  }

  /**
   * Creates a sample graph depicting a BPMN style diagram.
   * @param graph   the graph to configure.
   */
  private void createBpmnStyleSample( final Graph2D graph ) {
    graph.clear();


    final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
    tgnr.setConfiguration(CONFIGURATION_POOL_BPMN_STYLE);
    tgnr.setAutoResize(true);
    tgnr.setDefaultColumnInsets(new YInsets(0, 5, 0, 5));
    tgnr.setDefaultRowInsets(new YInsets(0, 25, 0, 0));

    // set up the table
    final Table table = tgnr.getTable();
    table.setInsets(new YInsets(20, 25, 5, 10));
    table.addColumn();
    table.addColumn();
    table.addRow();
    table.addRow();

    final NodeLabel label = tgnr.getLabel();
    label.setText("Pool");
    label.setPosition(NodeLabel.LEFT);
    label.setRotationAngle(270);

    final YInsets insets = table.getInsets();
    final double columnSizeAdjustment = 10 + 2 * insets.right;
    final double rowSizeAdjustment = 10;

    // configure the rows:
    //  - assign each row a label
    //  - set a suitable minimum size (height) to each row
    int r = 1;
    for (Iterator it = table.getRows().iterator(); it.hasNext(); ++r) {
      final Row row = (Row) it.next();
      final NodeLabel rowLabel = tgnr.createNodeLabel();
      rowLabel.setText("Lane " + r);
      final double minHeight = rowLabel.getWidth() + rowSizeAdjustment;
      row.setHeight(minHeight);
      row.setMinimumHeight(minHeight);

      // associate the label to the row
      // the ratio value of 0 means the label will be left-aligned (regarding
      // the row) and rotated 90 degress counter clockwise
      tgnr.configureRowLabel(rowLabel, row, true, 0);

      // row labels are normal node labels and have to be explicitly added
      // to the realizer as usual
      tgnr.addLabel(rowLabel);
    }

    // configure the columns:
    //  - assign each row a label
    //  - set a suitable minimum size (width) to each column
    int c = 1;
    for (Iterator it = table.getColumns().iterator(); it.hasNext(); ++c) {
      final Column column = (Column) it.next();
      final NodeLabel columnLabel = tgnr.createNodeLabel();
      columnLabel.setText("Milestone " + c);
      final double minWidth = columnLabel.getWidth() + columnSizeAdjustment;
      column.setWidth(minWidth);
      column.setMinimumWidth(minWidth);

      // associate the label to the column
      tgnr.configureColumnLabel(columnLabel, column, false, 0);

      // column labels are normal node labels and have to be explicitly added
      // to the realizer as usual
      tgnr.addLabel(columnLabel);
    }

    // ensure that the table has the correct (sufficiently large) size
    tgnr.updateTableBounds();


    final HierarchyManager hm = HierarchyManager.getInstance(graph);


    // create a group node ..
    final Node pool = hm.createGroupNode(graph);
    // .. and assign it the previously created table realizer
    graph.setRealizer(pool, tgnr);


    // prototype realizer for rectangular child nodes
    final GenericNodeRealizer roundRect = new GenericNodeRealizer();
    roundRect.setConfiguration(CONFIGURATION_SIMPLE_ROUNDRECT);
    roundRect.setSize(80, 60);
    roundRect.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    roundRect.setFillColor(Color.WHITE);
    roundRect.setLineColor(new Color(3, 104, 154));

    // prototype realizer for circular child nodes
    final GenericNodeRealizer ellipse = new GenericNodeRealizer();
    ellipse.setConfiguration(CONFIGURATION_SIMPLE_ELLIPSE);
    ellipse.setSize(30, 30);
    ellipse.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    ellipse.setFillColor(Color.WHITE);
    ellipse.setLineColor(new Color(198, 194, 139));

    // prototype realizer for diamond-shaped child nodes
    final GenericNodeRealizer diamond = new GenericNodeRealizer();
    diamond.setConfiguration(CONFIGURATION_SIMPLE_DIAMOND);
    diamond.setSize(30, 30);
    diamond.setCenter(tgnr.getCenterX(), tgnr.getCenterY());
    diamond.setFillColor(Color.WHITE);
    diamond.setLineColor(new Color(166, 166, 29));


    // create several child nodes using the previously created prototype
    // realizers and distribute the nodes over the table
    final Node[] nodes = new Node[15];
    for (int i = 0; i < nodes.length; ++i) {
      if (i < 5) {
        nodes[i] = graph.createNode(ellipse.createCopy());
      } else if (i < 11) {
        nodes[i] = graph.createNode(roundRect.createCopy());
      } else {
        nodes[i] = graph.createNode(diamond.createCopy());
      }

      // important: assign the node to the table group
      hm.setParentNode(nodes[i], pool);

      // "assign" the node to a column
      if (i == 4 || i == 9 || i == 10 || i == 14) {
        table.moveToColumn(nodes[i], table.getColumn(2));
      } else if ((0 < i && i < 4) || i == 7 || i == 8 || i == 13) {
        table.moveToColumn(nodes[i], table.getColumn(1));
      } else {
        table.moveToColumn(nodes[i], table.getColumn(0));
      }

      // "assign" the node to a row
      if (i == 10) {
        table.moveToRow(nodes[i], table.getRow(2));
      } else if (i == 4 || i == 8 || i == 14) {
        table.moveToRow(nodes[i], table.getRow(1));
      } else {
        table.moveToRow(nodes[i], table.getRow(0));
      }
    }
//    for (int i = 0; i < nodes.length; ++i) {
//      graph.getRealizer(nodes[i]).setLabelText(Integer.toString(i));
//    }


    // create some edges
    final Edge[] edges = {
      graph.createEdge(nodes[0], nodes[5]),
      graph.createEdge(nodes[2], nodes[7]),
      graph.createEdge(nodes[3], nodes[8]),
      graph.createEdge(nodes[5], nodes[6]),
      graph.createEdge(nodes[6], nodes[11]),
      graph.createEdge(nodes[7], nodes[13]),
      graph.createEdge(nodes[8], nodes[14]),
      graph.createEdge(nodes[9], nodes[4]),
      graph.createEdge(nodes[10], nodes[4]),
      graph.createEdge(nodes[11], nodes[1]),
      graph.createEdge(nodes[11], nodes[12]),
      graph.createEdge(nodes[12], nodes[2]),
      graph.createEdge(nodes[12], nodes[3]),
      graph.createEdge(nodes[13], nodes[1]),
      graph.createEdge(nodes[13], nodes[12]),
      graph.createEdge(nodes[14], nodes[9]),
      graph.createEdge(nodes[14], nodes[10]),
    };
//    for (int i = 0; i < edges.length; ++i) {
//      graph.getRealizer(edges[i]).setLabelText(Integer.toString(i));
//    }

    for (int i = 0; i < nodes.length; ++i) {
      final Node node = nodes[i];
      if (node.inDegree() == 0) {
        graph.getRealizer(node).setLineColor(DARK_GREEN);
      } else if (node.outDegree() == 0) {
        graph.getRealizer(node).setLineColor(BLOOD_RED);
      }
    }


    // setup port constraints to get BPMN-like edge routing for diamond-shaped
    // and circular child nodes

    // create the corresponding port constraints ...
    final EdgeMap srcPc = graph.createEdgeMap();
    srcPc.set(edges[9], PortConstraint.create(PortConstraint.NORTH));
    srcPc.set(edges[10], PortConstraint.create(PortConstraint.SOUTH));
    srcPc.set(edges[12], PortConstraint.create(PortConstraint.SOUTH));
    srcPc.set(edges[13], PortConstraint.create(PortConstraint.NORTH));
    srcPc.set(edges[14], PortConstraint.create(PortConstraint.WEST));
    srcPc.set(edges[15], PortConstraint.create(PortConstraint.NORTH));
    srcPc.set(edges[16], PortConstraint.create(PortConstraint.SOUTH));
    final EdgeMap tgtPc = graph.createEdgeMap();
    tgtPc.set(edges[4], PortConstraint.create(PortConstraint.WEST));
    tgtPc.set(edges[5], PortConstraint.create(PortConstraint.EAST));
    tgtPc.set(edges[7], PortConstraint.create(PortConstraint.NORTH));
    tgtPc.set(edges[8], PortConstraint.create(PortConstraint.SOUTH));
    tgtPc.set(edges[13], PortConstraint.create(PortConstraint.SOUTH));
    tgtPc.set(edges[14], PortConstraint.create(PortConstraint.NORTH));

    // ... and register the port constraints
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, srcPc);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tgtPc);


    // add a few layering constraint for a more BPMN-like result
    final LayerConstraintFactory lcf = new IncrementalHierarchicLayouter()
            .createLayerConstraintFactory(graph);
    lcf.addPlaceNodeInSameLayerConstraint(nodes[7], nodes[1]);
    lcf.addPlaceNodeInSameLayerConstraint(nodes[7], nodes[13]);

    try {
      layout(graph, false);
    } finally {

      lcf.dispose();

      graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      graph.disposeEdgeMap(tgtPc);
      graph.disposeEdgeMap(srcPc);
    }
  }

  /**
   * Performs a layout calculation for the specified graph using
   * {@link y.layout.hierarchic.IncrementalHierarchicLayouter}.
   * @param graph   the graph to be laid out.
   * @param vertical   if <code>true</code> a top-to-bottom layout is calculated
   * and if <code>false</code> a left-to-right layout is calculated.
   */
  private void layout( final Graph2D graph, final boolean vertical ) {
    // setup a suitable layout algorithm for a graph with table nodes
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setOrthogonallyRouted(true);
    ihl.setLayoutOrientation(
            vertical
            ? LayoutOrientation.TOP_TO_BOTTOM
            : LayoutOrientation.LEFT_TO_RIGHT);

    final Graph2DLayoutExecutor layoutExecutor =
            new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED);
    layoutExecutor.setConfiguringTableNodeRealizers(true);
    layoutExecutor.doLayout(graph, ihl);
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    
    bar.addSeparator();
    final JComboBox cb = 
        new JComboBox(new Object[] {"Gradient Rows", 
                                    "Alternating Columns", 
                                    "Generic", 
                                    "BPMN Style"});
    cb.setMaximumSize(new Dimension(200, 100));
    cb.setSelectedItem("BPMN Style");
    cb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String sample = cb.getSelectedItem().toString();
        if ("Gradient Rows".equals(sample)) {
          createGradientSample(view.getGraph2D());
        }
        else if ("Alternating Columns".equals(sample)) {
          createAlternatingSample(view.getGraph2D());
        }
        else if ("Generic".equals(sample)) {
          createGenericSample(view.getGraph2D());
        }
        else if ("BPMN Style".equals(sample)) {
          createBpmnStyleSample(view.getGraph2D());
        }
        view.fitContent();
        view.updateView();
      }
    });
    bar.add(cb);

    return bar;
  }

  /**
   * Overwritten.
   * @return the application menu bar.
   */
  protected JMenuBar createMenuBar() {
    final JMenu file = new JMenu("File");
    file.add(new PrintAction());
    file.addSeparator();
    file.add(new ExitAction());

    final JMenuBar jmb = new JMenuBar();
    jmb.add(file);
    return jmb;
  }

  /**
   * Overwritten to prevent deletion of graph elements.
   * @return <code>false</code>.
   */
  protected boolean isDeletionEnabled() {
    return false;
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new TableStyleDemo()).start();
      }
    });
  }

  /**
   * Creates and registers lots of configurations for the various (generic)
   * node realizers used throughout the demo.
   */
  private static void initConfigurations() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();


    // configurations used for table nodes
    {
      // configuration for the table node in the BPMN style sample
      final Map bpmn = TableGroupNodeRealizer.createDefaultConfigurationMap();
      configureSelectionMode(bpmn);
      configureHotSpots(bpmn);
      bpmn.put(GenericNodeRealizer.Painter.class, TableNodePainter.newBpmnInstance());


      // configuration for the table node in the generic sample
      final Map generic = TableGroupNodeRealizer.createDefaultConfigurationMap();
      configureSelectionMode(generic);
      configureHotSpots(generic);


      // configuration for the table node in the alternating columns sample
      final Map alternating = TableGroupNodeRealizer.createDefaultConfigurationMap();
      configureSelectionMode(alternating);
      configureHotSpots(alternating);
      alternating.put(GenericNodeRealizer.Painter.class,
                    TableNodePainter.newAlternatingColumnsInstance());


      // configure the table painter for the gradient rows sample
      final TableNodePainter painter = TableNodePainter.newDefaultInstance();
      // disable column background rendering
      painter.setSubPainter(TableNodePainter.PAINTER_COLUMN_BACKGROUND, null);
      // register a custom row subordinate painter
      painter.setSubPainter(TableNodePainter.PAINTER_ROW_BACKGROUND, new GradientRowPainter());

      // configuration for the table node in the gradient rows sample
      final Map gradient = TableGroupNodeRealizer.createDefaultConfigurationMap();
      configureSelectionMode(gradient);
      configureHotSpots(gradient);
      gradient.put(GenericNodeRealizer.Painter.class, painter);


      // register the configurations
      factory.addConfiguration(
              CONFIGURATION_POOL_GRADIENT, gradient);
      factory.addConfiguration(
              CONFIGURATION_POOL_ALTERNATING, alternating);
      factory.addConfiguration(
              CONFIGURATION_POOL_GENERIC, generic);
      factory.addConfiguration(
              CONFIGURATION_POOL_BPMN_STYLE, bpmn);
    }


    // configurations used for child nodes in the BPMN style sample
    {
      final ShapeNodePainter roundRect =
              new ShapeNodePainter(ShapeNodePainter.ROUND_RECT);
      final Map simpleRoundRect = factory.createDefaultConfigurationMap();
      simpleRoundRect.put(GenericNodeRealizer.Painter.class, roundRect);
      simpleRoundRect.put(GenericNodeRealizer.ContainsTest.class, roundRect);

      final ShapeNodePainter diamond =
              new ShapeNodePainter(ShapeNodePainter.DIAMOND);
      final Map simpleDiamond = factory.createDefaultConfigurationMap();
      simpleDiamond.put(GenericNodeRealizer.Painter.class, diamond);
      simpleDiamond.put(GenericNodeRealizer.ContainsTest.class, diamond);

      final ShapeNodePainter ellipse =
              new ShapeNodePainter(ShapeNodePainter.ELLIPSE);
      final Map simpleEllipse = factory.createDefaultConfigurationMap();
      simpleEllipse.put(GenericNodeRealizer.Painter.class, ellipse);
      simpleEllipse.put(GenericNodeRealizer.ContainsTest.class, ellipse);

      factory.addConfiguration(CONFIGURATION_SIMPLE_ROUNDRECT, simpleRoundRect);
      factory.addConfiguration(CONFIGURATION_SIMPLE_DIAMOND, simpleDiamond);
      factory.addConfiguration(CONFIGURATION_SIMPLE_ELLIPSE, simpleEllipse);
    }


    // configurations used for all other child nodes
    {
      final Map gradientRect = factory.createDefaultConfigurationMap();
      configureSelectionMode(gradientRect);
      gradientRect.put(
            GenericNodeRealizer.Painter.class,
            new SimpleGradientNodePainter(ShapeNodePainter.RECT));

      final Map gradientRoundRect = factory.createDefaultConfigurationMap();
      final SimpleGradientNodePainter roundRect =
              new SimpleGradientNodePainter(ShapeNodePainter.ROUND_RECT);
      gradientRoundRect.put(GenericNodeRealizer.Painter.class, roundRect);
      gradientRoundRect.put(GenericNodeRealizer.ContainsTest.class, roundRect);

      final Map gradientDiamond = factory.createDefaultConfigurationMap();
      final SimpleGradientNodePainter diamond =
              new SimpleGradientNodePainter(ShapeNodePainter.DIAMOND);
      gradientDiamond.put(GenericNodeRealizer.Painter.class, diamond);
      gradientDiamond.put(GenericNodeRealizer.ContainsTest.class, diamond);

      final Map gradientEllipse = factory.createDefaultConfigurationMap();
      final SimpleGradientNodePainter ellipse =
              new SimpleGradientNodePainter(ShapeNodePainter.ELLIPSE);
      gradientEllipse.put(GenericNodeRealizer.Painter.class, ellipse);
      gradientEllipse.put(GenericNodeRealizer.ContainsTest.class, ellipse);

      factory.addConfiguration(CONFIGURATION_GRADIENT_RECT, gradientRect);
      factory.addConfiguration(CONFIGURATION_GRADIENT_ROUNDRECT, gradientRoundRect);
      factory.addConfiguration(CONFIGURATION_GRADIENT_DIAMOND, gradientDiamond);
      factory.addConfiguration(CONFIGURATION_GRADIENT_ELLIPSE, gradientEllipse);
    }
  }

  /**
   * Configures the specified configuration map for default hot spot painting
   * and hit testing.
   * @param map   a configuration map.
   */
  private static void configureHotSpots( final Map map ) {
    // setting a null HotSpotPainter actually configures GenericNodeRealizer
    // to use the default hot spot painting
    map.put(GenericNodeRealizer.HotSpotPainter.class, null);
    // setting a null HotSpotHitTest actually configures GenericNodeRealizer
    // to use the default hot spot hit testing
    map.put(GenericNodeRealizer.HotSpotHitTest.class, null);
  }

  /**
   * Configures the <code>TableSelectionMode</code> in the specified
   * configuration map to couple column/row selection state and realizer
   * selection state.
   * @param map   a configuration map.
   */
  private static void configureSelectionMode( final Map map ) {
    final Object miep =
            map.get(GenericNodeRealizer.GenericMouseInputEditorProvider.class);
    if (miep instanceof MultiplexingNodeEditor) {
      final MultiplexingNodeEditor editor = (MultiplexingNodeEditor) miep;
      for (Iterator it = editor.getNodeEditors().iterator(); it.hasNext();) {
        final Object mode = it.next();
        if (mode instanceof TableSelectionEditor) {
          ((TableSelectionEditor) mode).setSelectionPolicy(
                  TableSelectionEditor.RELATE_TO_NODE_SELECTION);
        }
      }
    }
  }


  /**
   * {@link y.view.ShapeNodePainter} painter that uses a vertical gradient paint
   * (defined by the context realizer's fill color and fill color 2) to fill
   * node shapes and adds a very simple drop shadow.
   */
  private static final class SimpleGradientNodePainter extends ShapeNodePainter {
    SimpleGradientNodePainter( final byte type ) {
      super(type);
    }

    protected void paintFilledShape(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape
    ) {
      if (!context.isTransparent()) {
        final boolean useSelectionStyle = useSelectionStyle(context, graphics);
        final Paint paint = getFillPaint(context, useSelectionStyle);
        if (paint != null) {
          final AffineTransform oldTransform = graphics.getTransform();
          graphics.translate(3, 3);
          graphics.setColor(Color.GRAY);
          graphics.fill(shape);
          graphics.setTransform(oldTransform);

          graphics.setPaint(paint);
          graphics.fill(shape);
        }
      }
    }

    protected Paint getFillPaint( final NodeRealizer context, final boolean selected ) {
      Color fill1 = getFillColor(context, selected);
      if (fill1 != null) {
        Color fill2 = getFillColor2(context, selected);
        if (fill2 != null) {
          final float x = (float) context.getX();
          final double y = context.getY();
          return new GradientPaint(
                  x, (float) y, fill1,
                  x, (float) (y + context.getHeight()), fill2, true);
        } else {
          return fill1;
        }
      } else {
        return null;
      }
    }

    private static boolean useSelectionStyle(
            final NodeRealizer context,
            final Graphics2D graphics
    ) {
      return context.isSelected() &&
             YRenderingHints.isSelectionPaintingEnabled(graphics);
    }
  }

  /**
   * {@link y.view.GenericNodeRealizer.Painter} meant to be used as a row
   * background painter for {@link y.view.tabular.TableNodePainter}.
   * The row background is filled using gradient paints defined by custom
   * style properties.
   */
  private static final class GradientRowPainter extends AbstractCustomNodePainter {
    /**
     * Style property ID used to retrieve style properties of type
     * {@link demo.view.realizer.TableStyleDemo.GradientRowPainter.RowColorMap}
     * that are used to create appropriate gradient paints.
     */
    static final String STYLE_ROW_COLOR_MAP = "ROW_COLOR_MAP";


    final Rectangle2D.Double shape;

    GradientRowPainter() {
      shape = new Rectangle2D.Double();
    }

    /**
     * Overwritten to prevent hot spot and label painting.
     * @param dummy   the dummy realizer representing the bounds of the row
     * that is to be painted.
     * @param graphics   the graphics context for painting.
     */
    public void paint( final NodeRealizer dummy, final Graphics2D graphics ) {
      if (!dummy.isVisible()) {
        return;
      }
      backupGraphics(graphics);
      try {
        paintNode(dummy, graphics, false);
      } finally {
        restoreGraphics(graphics);
      }
    }

    /**
     * Paints the row represented by the specified realizer.
     * @param dummy   the dummy realizer representing the bounds of the row
     * that is to be painted.
     * @param graphics   the graphics context for painting.
     * @param sloppy   ignored.
     */
    protected void paintNode(
            final NodeRealizer dummy,
            final Graphics2D graphics,
            final boolean sloppy
    ) {
      if (!dummy.isTransparent()) {
        final Paint paint = getFillPaint(dummy, dummy.isSelected());
        if (paint != null) {
          shape.setFrame(dummy.getX(), dummy.getY(), dummy.getWidth(), dummy.getHeight());
          graphics.setPaint(paint);
          graphics.fill(shape);
        }

        final YInsets insets = getRow((dummy)).getInsets();
        if (insets != null && insets.left > 0) {
          final Color color = getFillColor(dummy, false);
          if (color != null) {
            shape.setFrame(dummy.getX(), dummy.getY(), insets.left, dummy.getHeight());
            graphics.setColor(color);
            graphics.fill(shape);
          }
        }
      }
    }

    /**
     * Determines the fill paint for the row represented by the specified
     * realizer depending on the registered {@link #STYLE_ROW_COLOR_MAP} style
     * property.
     * @param dummy   the dummy realizer representing the bounds of the row
     * that is to be painted.
     * @param selected whether the node is currently selected
     * @return the background fill paint for the row represented by the
     * specified realizer.
     */
    protected Paint getFillPaint( final NodeRealizer dummy, final boolean selected ) {
      final GenericNodeRealizer gnr = (GenericNodeRealizer) dummy;
      final RowColorMap rcm = (RowColorMap) gnr.getStyleProperty(STYLE_ROW_COLOR_MAP);
      if (rcm != null) {
        final Row row = getRow(gnr);
        Color color = rcm.getColor(row);
        if (color == null) {
          color = new Color(0, 0, 0, 0);
        }

        final double x = dummy.getX();
        final float y = (float) dummy.getY();
        return new GradientPaint(
                (float) x, y, color,
                (float) (x + dummy.getWidth()),  y, getFillColor2(dummy, false));
      } else {
        return getFillColor(dummy, selected);
      }
    }

    /**
     * Returns the row represented by the specified realizer.
     * @param dummy   a {@link y.view.GenericNodeRealizer} representing
     * a row in a {@link y.view.tabular.TableGroupNodeRealizer}.
     * @return the row represented by the specified realizer.
     */
    private static Row getRow( final NodeRealizer dummy ) {
      return TableNodePainter.getRow(dummy);
    }


    private interface RowColorMap {
      public Color getColor( Row row );
    }
  }


  /**
   * <code>TableStyle</code> intended for rows that uses a realizer's fill color
   * 2 as fill color.
   */
  private static final class RowStyle implements TableStyle {
    private final boolean selected;

    RowStyle( final boolean selected ) {
      this.selected = selected;
    }

    public Stroke getBorderLineType( final NodeRealizer context ) {
      return null;
    }

    public Color getBorderLineColor( final NodeRealizer context ) {
      return null;
    }

    public Color getBorderFillColor( final NodeRealizer context ) {
      return null;
    }

    public Stroke getLineType( final NodeRealizer context ) {
      if (selected) {
        final LineType lt = context.getLineType();
        return LineType.getLineType(
                (int)Math.ceil(lt.getLineWidth()) + 2,
                lt.getLineStyle());
      } else {
        return context.getLineType();
      }
    }

    public Color getLineColor( final NodeRealizer context ) {
      return context.getLineColor();
    }

    public Color getFillColor( final NodeRealizer context ) {
      return context.getFillColor2();
    }
  }
}
