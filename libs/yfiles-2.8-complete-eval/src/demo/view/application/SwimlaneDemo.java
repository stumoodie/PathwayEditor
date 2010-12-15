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

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Node;
import y.geom.YInsets;
import y.layout.LayoutOrientation;
import y.layout.NodeLabelModel;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.option.RealizerCellRenderer;
import y.view.CreateEdgeMode;
import y.view.Drawable;
import y.view.DropSupport;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DUndoManager;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.HitInfo;
import y.view.LineType;
import y.view.MultiplexingNodeEditor;
import y.view.NodeRealizer;
import y.view.ShapeNodePainter;
import y.view.Graph2DListener;
import y.view.Graph2DEvent;
import y.view.NodeLabel;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.ColumnDropTargetListener;
import y.view.tabular.RowDropTargetListener;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.ColumnNodeLabelModel;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.RowNodeLabelModel;
import y.view.tabular.TableLabelEditor;
import y.view.tabular.TableNodePainter;
import y.view.tabular.TableOrderEditor;
import y.view.tabular.TableSelectionEditor;
import y.view.tabular.TableSizeEditor;
import y.view.tabular.TableStyle;
import y.view.tabular.TableSupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

/**
 * <p>Demonstrates how to use and customize {@link y.view.tabular.TableGroupNodeRealizer} to work as a pool
 * having several swim lanes and milestones.</p>
 * <p>A list using {@link y.view.tabular.RowDropTargetListener} and
 * {@link y.view.tabular.ColumnDropTargetListener} is added to showcase how additional rows and
 * columns can be added via drag'n'drop.</p>
 * <p>Two different ways to customize the rendering of rows and columns are used:</p>
 * <ul>
 * <li>For columns, customized {@link y.view.tabular.TableStyle.SimpleStyle SimpleStyles} are registered as style properties
 * of the <code>TableGroupNodeRealizer</code> which are used by the default column sub painter.</li>
 * <li>For rows, a custom row sub painter is used that alternates the fill color of childless rows while rendering rows
 * with children in a third color.</li>
 * </ul>
 */
public class SwimlaneDemo extends DemoBase {
  static final String CONFIGURATION_GROUP_NODE = "CONFIGURATION_GROUP_NODE";
  static final String CONFIGURATION_TABLE_NODE = "CONFIGURATION_TABLE_NODE";

  static {
    initConfigurations();
  }


  private YInsets rowInsets;
  private YInsets columnInsets;
  private Graph2DUndoManager undoManager;
  private MinimumSizeManager minimumSizeManager;


  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SwimlaneDemo("resource/swimlanehelp.html")).start();
      }
    });
  }

  public SwimlaneDemo() {
    this(null);
  }

  public SwimlaneDemo( final String helpFilePath ) {
    addHelpPane(helpFilePath);
  }

  private NodeRealizer createConfiguredNormalNodeRealizer() {
    final NodeRealizer normalNode = view.getGraph2D().getDefaultNodeRealizer().createCopy();
    normalNode.setSize(80, 50);
    return normalNode;
  }

  private NodeRealizer createConfiguredGroupNodeRealizer() {
    final GenericGroupNodeRealizer ggnr = new GenericGroupNodeRealizer();
    ggnr.setConfiguration(CONFIGURATION_GROUP_NODE);
    ggnr.setFillColor(null);
    ggnr.setLineType(LineType.DASHED_DOTTED_1);
    ggnr.removeLabel(ggnr.getLabel());
    ggnr.setGroupClosed(false);
    ggnr.setSize(80, 50);
    return ggnr;
  }

  private NodeRealizer createConfiguredTableNodeRealizer() {
    final TableGroupNodeRealizer tgnr = new TableGroupNodeRealizer();
    tgnr.setConfiguration(CONFIGURATION_TABLE_NODE);

    // background color used for the TableGroupNodeRealizer and therefore per default for the table.
    tgnr.setFillColor(new Color(236, 245, 255));

    // use custom styles for selected and unselected columns
    final Color columnFillColor = new Color(113, 146, 178);
    tgnr.setStyleProperty(
            TableNodePainter.COLUMN_STYLE_ID,
            new TableStyle.SimpleStyle(
                    tgnr.getLineType(),
                    tgnr.getLineColor(),
                    columnFillColor,
                    null,
                    null,
                    columnFillColor
            )
    );

    final LineType lt = tgnr.getLineType();
    final Color columnSelectedFillColor = new Color(55, 93, 129);
    tgnr.setStyleProperty(
            TableNodePainter.COLUMN_SELECTION_STYLE_ID,
            new TableStyle.SimpleStyle(
                    LineType.getLineType((int) Math.ceil(lt.getLineWidth()) + 2, lt.getLineStyle()),
                    tgnr.getLineColor(),
                    columnSelectedFillColor,
                    null,
                    null,
                    columnSelectedFillColor
            )
    );

    // Defaults for columns and rows should be set before those of the table.
    // This way the defaults are also applied to the first row and column which
    // are automatically added to the table on it's first access.
    tgnr.setDefaultColumnWidth(600);
    tgnr.setDefaultMinimumColumnWidth(200);
    tgnr.setDefaultColumnInsets(columnInsets);
    tgnr.setDefaultRowHeight(150);
    tgnr.setDefaultMinimumRowHeight(50);
    tgnr.setDefaultRowInsets(rowInsets);
    tgnr.setAutoResize(true);

    final TableGroupNodeRealizer.Table table = tgnr.getTable();
    table.setInsets(new YInsets(30, 0, 0, 0));

    tgnr.setSize(250, 200);
    return tgnr;
  }

  /**
   * Adds configurations for nodes with a bevel node style and those using a
   * {@link y.view.tabular.TableGroupNodeRealizer} to the factory.
   */
  private static void initConfigurations() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    final Map groupMap = createGroupNodeConfiguration();
    factory.addConfiguration(CONFIGURATION_GROUP_NODE, groupMap);

    final Map tableMap = createTableNodeConfiguration();
    factory.addConfiguration(CONFIGURATION_TABLE_NODE, tableMap);
  }

  protected void initialize() {
    // a hierarchy manager has to be used for table group nodes to work.
    new HierarchyManager(view.getGraph2D());

    minimumSizeManager = new MinimumSizeManager(view.getGraph2D());

    rowInsets = new YInsets(0, 30, 0, 0);
    columnInsets = new YInsets(30, 5, 0, 5);
    final DropSupport dropSupport = createDropSupport(view);

    contentPane.add(createDragNDropList(dropSupport), BorderLayout.WEST);

    loadGraph( "resource/SwimlaneDemo.graphml" );

    undoManager = new Graph2DUndoManager(view.getGraph2D());
    undoManager.setViewContainer(view);

    view.setPreferredSize(new Dimension(950, 550));
    view.fitContent();
    view.updateView();
  }

  protected void loadGraph( final URL resource ) {
    // disable the size manager because loading a graph results in lots of
    // label text property changes
    minimumSizeManager.setEnabled(false);
    try {
      super.loadGraph(resource);
    } finally {
      minimumSizeManager.setEnabled(true);
    }
  }

  protected void registerViewActions() {
    // register keyboard actions
    Graph2DViewActions actions = new Graph2DViewActions(view);
    ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      InputMap imap = actions.createDefaultInputMap(amap);
      amap.remove(Graph2DViewActions.DELETE_SELECTION);
      if (isDeletionEnabled()) {
        amap.put(Graph2DViewActions.DELETE_SELECTION, createDeleteSelectionActionImpl());
      }
      view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
    }
  }

  private static DropSupport createDropSupport(Graph2DView view) {
    // a customized DropSupport is used which only created new nodes if they are dropped onto a group node
    DropSupport dropSupport = new DropSupport(view) {

      protected boolean dropNodeRealizer(Graph2DView view, NodeRealizer r, double worldCoordX, double worldCoordY) {
        final HierarchyManager hm = HierarchyManager.getInstance(view.getGraph2D());
        final HitInfo hitInfo = new HitInfo(view, worldCoordX, worldCoordY, true, HitInfo.NODE);
        if (hm != null &&
            hitInfo.hasHitNodes()) {
          final Node node = (Node) hitInfo.hitNodes().current();
          if (hm.isGroupNode(node) &&
              ! (r instanceof TableGroupNodeRealizer)) {
            // there is a group node at the drop location which will become the parent of the new node
            return super.dropNodeRealizer(view, r, worldCoordX, worldCoordY);
          }
        } else if (r instanceof TableGroupNodeRealizer) {
          return super.dropNodeRealizer(view, r, worldCoordX, worldCoordY);
        }
        return false;
      }
    };
    dropSupport.setSnappingEnabled(true);
    dropSupport.getSnapContext().setNodeToNodeDistance(30);
    dropSupport.getSnapContext().setNodeToEdgeDistance(20);
    dropSupport.getSnapContext().setUsingSegmentSnapLines(true);
    dropSupport.setPreviewEnabled(true);
    return dropSupport;
  }

  private JList createDragNDropList(final DropSupport support) {
    final Object[] listContent = new Object[] {
            createConfiguredTableNodeRealizer(),
            DropItemListCellRenderer.DROP_TYPE_ROW,
            DropItemListCellRenderer.DROP_TYPE_COLUMN,
            createConfiguredNormalNodeRealizer(),
            createConfiguredGroupNodeRealizer()
    };

    final Color lightBlueFillColor = new Color(126, 179, 240, 128);
    final Color unselectedBorderColor = new Color(58, 82, 109);
    final Stroke borderStroke = LineType.LINE_1;

    // configure how the icons for rows and column drag'n'dropable shall look like
    final DropDrawable rowIcon = new DropDrawable(unselectedBorderColor, lightBlueFillColor, borderStroke);
    rowIcon.insets = new YInsets(0, 15, 0, 0);
    rowIcon.setBounds(0, 0, 80, 50);

    final DropDrawable columnIcon = new DropDrawable(unselectedBorderColor, lightBlueFillColor, borderStroke);
    columnIcon.insets = new YInsets(15, 0, 0, 0);
    columnIcon.setBounds(0, 0, 80, 50);

    final DropItemListCellRenderer cellRenderer =
            new DropItemListCellRenderer(columnIcon, rowIcon);

    // configure the list itself
    final JList dropItemList = new JList(listContent);
    dropItemList.setCellRenderer(cellRenderer);
    dropItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    dropItemList.setSelectedIndex(0);
    dropItemList.setFixedCellHeight(100);
    dropItemList.setFixedCellWidth(100);
    dropItemList.setBorder(LineBorder.createBlackLineBorder());

    final DragSource dragSource = new DragSource();

    // configure the drop target listener used for rows and columns
    final RowDropTargetListener rowListener =
            new RowDropTargetListener(view) {
              DropDrawable drawable = new DropDrawable(unselectedBorderColor, lightBlueFillColor, borderStroke);

              protected Drawable createDrawable(Rectangle2D bounds, YInsets insets) {
                drawable.insets = insets;
                drawable.setBounds(bounds);
                return drawable;
              }
            };
    rowListener.setDefaultHeight(50);
    rowListener.setDefaultMinimumHeight(30);
    rowListener.setDrawableWidth(200);
    rowListener.setDefaultInsets(rowInsets);
    rowListener.setMaxLevel(2);


    final ColumnDropTargetListener columnListener =
            new ColumnDropTargetListener(view) {
              DropDrawable drawable = new DropDrawable(unselectedBorderColor, lightBlueFillColor, borderStroke);

              protected Drawable createDrawable(Rectangle2D bounds, YInsets insets) {
                drawable.insets = insets;
                drawable.setBounds(bounds);
                return drawable;
              }
            };
    columnListener.setDefaultWidth(100);
    columnListener.setDefaultMinimumWidth(200);
    columnListener.setDrawableHeight(180);
    columnListener.setDefaultInsets(columnInsets);
    columnListener.setMaxLevel(2);


    // use the drop support class to initialize the drag and drop operation.
    dragSource.createDefaultDragGestureRecognizer(dropItemList, DnDConstants.ACTION_MOVE,
        new DragGestureListener() {
          public void dragGestureRecognized(DragGestureEvent event) {
            final Object value = dropItemList.getSelectedValue();
            if (value.equals(DropItemListCellRenderer.DROP_TYPE_ROW)) {
              support.startDrag(dragSource,
                      rowListener,
                      event,
                      DragSource.DefaultMoveDrop);
            } else if (value.equals(DropItemListCellRenderer.DROP_TYPE_COLUMN)) {
              support.startDrag(dragSource,
                      columnListener,
                      event,
                      DragSource.DefaultMoveDrop);
            } else if (value instanceof NodeRealizer) {
              NodeRealizer nr = (NodeRealizer) value;
              support.startDrag(dragSource, nr, event, DragSource.DefaultMoveDrop);
            }
          }
        });
    return dropItemList;
  }


  /**
   * Creates a new edit mode that is configured to support user interaction with the {@link y.view.tabular.TableGroupNodeRealizer}.
   *
   * @return An edit mode for the user interaction in this demo.
   */
  protected EditMode createEditMode() {
    final EditMode editMode = super.createEditMode();

    // the property setNodeSearchingEnabled has to be set to 'true' to allow custom MouseInputEditorProviders to be used
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);

    // nodes may only be created via drag'n'drop
    editMode.allowNodeCreation(false);

    // activate snap lines
    editMode.setSnappingEnabled(true);

    // ensure orthogonal edges during interactive edits
    editMode.setOrthogonalEdgeRouting(true);

    // activate snapping and ensure orthogonal edges during edge creation
    final CreateEdgeMode cem = new CreateEdgeMode();
    cem.setFuzzyTargetPortDetermination(true);
    cem.setSnapToOrthogonalSegmentsDistance(5);
    cem.setUsingNodeCenterSnapping(true);
    cem.setSnappingOrthogonalSegments(true);
    cem.setIndicatingTargetNode(true);
    cem.setRemovingInnerBends(true);
    cem.setOrthogonalEdgeCreation(true);
    editMode.setCreateEdgeMode(cem);

    return editMode;
  }

  /**
   * Overwritten to add undo/redo and  a layout action to the demo's
   * tool bar.
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    final Action undoAction = undoManager.getUndoAction();
    final URL undoIconUrl = DemoBase.class.getResource("resource/undo.png");
    if (undoIconUrl != null) {
      undoAction.putValue(Action.SMALL_ICON, new ImageIcon(undoIconUrl));
      undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo");
    }
    toolBar.add(DemoDefaults.createActionControl(undoAction, false));
    final Action redoAction = undoManager.getRedoAction();
    final URL redoIconUrl = DemoBase.class.getResource("resource/redo.png");
    if (redoIconUrl != null) {
      redoAction.putValue(Action.SMALL_ICON, new ImageIcon(redoIconUrl));
      redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo");
    }
    toolBar.add(DemoDefaults.createActionControl(redoAction, false));

    toolBar.addSeparator();

    final AbstractAction layoutAction = new AbstractAction("Layout") {
      public void actionPerformed( ActionEvent e ) {
        layout(view.getGraph2D());
        view.updateView();
      }
    };
    final URL resource = DemoBase.class.getResource("resource/layout.png");
    if (resource != null) {
      layoutAction.putValue(Action.SMALL_ICON, new ImageIcon(resource));
      layoutAction.putValue(Action.SHORT_DESCRIPTION, "Layout");
    }

    toolBar.add(DemoDefaults.createActionControl(layoutAction, true));

    return toolBar;
  }

  /**
   * Overwritten to create an action that loads/opens a graph and clears
   * the undo queue right afterwards.
   * @return an action that loads/opens a graph and clears
   * the undo queue right afterwards.
   */
  protected Action createLoadAction() {
    final Action action = super.createLoadAction();
    return new AbstractAction((String) action.getValue(Action.NAME)) {
      public void actionPerformed( final ActionEvent e ) {
        action.actionPerformed(e);
        undoManager.resetQueue();
      }
    };
  }

  /**
   * Runs an incremental hierarchic layout that respects the assignments of nodes to swimlanes and milestones.
   */
  private void layout( final Graph2D graph ) {
    graph.firePreEvent();
    try {
      // undoability
      graph.backupRealizers();

      final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
      ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
      ihl.setOrthogonallyRouted(true);
      ihl.setRecursiveGroupLayeringEnabled(false);
      ((SimplexNodePlacer) ihl.getNodePlacer()).setBaryCenterModeEnabled(true);

      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED);
      layoutExecutor.setConfiguringTableNodeRealizers(true);
      layoutExecutor.getTableLayoutConfigurator().setCompactionEnabled(false);
      layoutExecutor.getTableLayoutConfigurator().setHorizontalLayoutConfiguration(true);
      layoutExecutor.doLayout(graph, ihl);
      view.fitContent();
      view.updateView();
    } finally {
      graph.firePostEvent();
    }
  }

  protected Action createDeleteSelectionAction() {
    final Action action = createDeleteSelectionActionImpl();
    final URL deleteIconUrl = DemoBase.class.getResource("resource/delete.png");
    if (deleteIconUrl != null) {
      action.putValue(Action.SMALL_ICON, new ImageIcon(deleteIconUrl));
    }
    action.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    return action;
  }

  private Action createDeleteSelectionActionImpl() {
    final Graph2DViewActions.DeleteSelectionAction action =
            new Graph2DViewActions.DeleteSelectionAction(view);
    action.setDeletionMask(Graph2DViewActions.DeleteSelectionAction.ALL_TYPES_MASK);
    action.setKeepingTableNodesOnTableContentDeletion(true);
    action.setKeepingParentGroupNodeSizes(true);
    return action;
  }

  private static Map createGroupNodeConfiguration() {
    final Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();
    final ShapeNodePainter painter = new ShapeNodePainter(ShapeNodePainter.ROUND_RECT);
    map.put(GenericNodeRealizer.ContainsTest.class, painter);
    map.put(GenericNodeRealizer.Painter.class, painter);
    map.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, null);
    return map;
  }

  private static Map createTableNodeConfiguration() {
    final Map map = TableGroupNodeRealizer.createDefaultConfigurationMap();

    // configure the painter used for the swim lanes
    final AlternatingPainter rowPainter = new AlternatingPainter();
    final TableNodePainter tableNodePainter = TableNodePainter.newDefaultInstance();
    tableNodePainter.setSubPainter(TableNodePainter.PAINTER_ROW_BACKGROUND, rowPainter);
    map.put(GenericNodeRealizer.Painter.class, tableNodePainter);

    // configure MouseInputEditor for the TableGroupNodeRealizer
    final MultiplexingNodeEditor editor = new MultiplexingNodeEditor();
    final TableLabelEditor editLabelEditor = new TableLabelEditor();
    editor.addNodeEditor(editLabelEditor);
    final TableSelectionEditor tableSelectionEditor = new TableSelectionEditor();
    tableSelectionEditor.setSelectionPolicy(TableSelectionEditor.RELATE_TO_NODE_SELECTION);
    editor.addNodeEditor(tableSelectionEditor);
    final TableSizeEditor resizeEditor = new TableSizeEditor();
    editor.addNodeEditor(resizeEditor);
    final TableOrderEditor tableOrderEditor = new TableOrderEditor();
    tableOrderEditor.setMaxColumnLevel(2);
    tableOrderEditor.setMaxRowLevel(2);
    editor.addNodeEditor(tableOrderEditor);
    map.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, editor);

    return map;
  }

  /**
   * Ensures that the minimum width of columns and the minimum height of rows
   * is never smaller than the width or height of their associated labels.
   * <p>
   * The implementation relies on the fact that there is at most one label
   * associated to any column or row.
   * </p>
   */
  private static final class MinimumSizeManager implements Graph2DListener {
    private boolean enabled;

    MinimumSizeManager( final Graph2D graph ) {
      graph.addGraph2DListener(this);
      enabled = true;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled( final boolean enabled ) {
      this.enabled = enabled;
    }

    public void onGraph2DEvent( final Graph2DEvent e ) {
      if (isEnabled()) {
        if ("text".equals(e.getPropertyName())) {
          final Object subject = e.getSubject();
          if (subject instanceof NodeLabel) {
            final NodeLabel label = (NodeLabel) subject;
            final NodeLabelModel model = label.getLabelModel();
            if (model instanceof ColumnNodeLabelModel) {
              handleColumnLabelEvent(label);
            } else if (model instanceof RowNodeLabelModel) {
              handleRowLabelEvent(label);
            }
          }
        }
      }
    }

    private void handleRowLabelEvent( final NodeLabel label ) {
      final Row row = RowNodeLabelModel.getRow(label);
      if (row != null) {
        final double h = label.getHeight() + 8;
        if (h > row.getHeight()) {
          (new TableSupport()).setHeight(row, h, false);
        }
        row.setMinimumHeight(Math.max(
                h, ((TableGroupNodeRealizer) label.getRealizer())
                        .getDefaultMinimumRowHeight()));
      }
    }

    private void handleColumnLabelEvent( final NodeLabel label ) {
      final Column column = ColumnNodeLabelModel.getColumn(label);
      if (column != null) {
        final double w = label.getWidth() + 8;
        if (w > column.getWidth()) {
          (new TableSupport()).setWidth(column, w, false);
        }
        column.setMinimumWidth(Math.max(
                w, ((TableGroupNodeRealizer) label.getRealizer())
                        .getDefaultMinimumColumnWidth()));
      }
    }
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////   Class AlternatingPainter    //////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * A background {@link y.view.GenericNodeRealizer.Painter Painter} for rows in
 * a table that paints all inner or parent rows (i.e. rows for which
 * <code>getRows()</code> returns a non-empty list) in a given color and
 * alternates between two different colors for leaf rows (i.e. rows for which
 * <code>getRows()</code> returns an empty list).
 */
final class AlternatingPainter extends ShapeNodePainter {
  private static final Color PARENT_ROW_COLOR = new Color(113, 146, 178);
  private static final Color EVEN_ROW_COLOR = new Color(196, 215, 237);
  private static final Color ODD_ROW_COLOR = new Color(171, 200, 226);
  private static final Color SELECTED_ROW_COLOR = new Color(55, 93, 129);

  AlternatingPainter() {
    super(ShapeNodePainter.RECT);
  }

  protected Paint getFillPaint( final NodeRealizer context, final boolean selected ) {
    return getFillColor(context, selected);
  }

  /**
   * Determines the fill color for the row corresponding to the specified
   * realizer.
   * @param context a dummy realizer representing a row in a table.
   * @param selected ignored.
   * @return the fill color for the row corresponding to the specified
   * realizer.
   */
  protected Color getFillColor( final NodeRealizer context, final boolean selected ) {
    final Row row = TableNodePainter.getRow(context);
    if (row.isSelected()) {
      return SELECTED_ROW_COLOR;
    }

    if (row.getRows().isEmpty()) {
      if (indexOf(row, TableNodePainter.getTable(context).getRows(), new int[]{-1}) % 2 == 0) {
        return EVEN_ROW_COLOR;
      } else {
        return ODD_ROW_COLOR;
      }
    } else {
      return PARENT_ROW_COLOR;
    }
  }

  /**
   * Calls the various utility method and callbacks in this class.
   */
  public void paint(NodeRealizer context, Graphics2D graphics) {
    if (!context.isVisible()){
      return;
    }
    backupGraphics(graphics);
    try {
      paintNode(context, graphics, false);
    } finally {
      restoreGraphics(graphics);
    }
  }

  /**
   * Determines the leaf index of the specified row.
   * @param row    the <code>Row</code> to search for.
   * @param i      used to track the number of previously visited leaf rows.
   *  (<code>int[]</code> is used as poor man's mutable <code>Integer</code>.)
   * @return the leaf index of the specified row.
   */
  private int indexOf( final Row row, final Collection rows, final int[] i ) {
    for (Iterator it = rows.iterator(); it.hasNext();) {
      final Row r = (Row) it.next();
      final List children = r.getRows();
      if (children.isEmpty()) {
        ++i[0];
        if (r.equals(row)) {
          return i[0];
        }
      } else {
        final int idx = indexOf(row, children, i);
        if (idx > -1) {
          return idx;
        }
      }
    }

    return -1;
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////   Class DropDrawable    ////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This class is used to render a representative of a row or a column either as {@link y.view.Drawable} during drag'n'drop
 * gestures or as {@link javax.swing.Icon} in the drag'n'drop list.
 */
class DropDrawable implements Drawable, Icon {
  Rectangle bounds;
  YInsets insets;

  Color borderColor;
  Color fillColor;
  Stroke stroke;

  /**
   * Creates a new instance using the specified colors and stroke.
   *
   * @param borderColor The color used for the stripe border.
   * @param fillColor The fill color of the stripe.
   * @param stroke The stroke used for the border.
   */
  DropDrawable(Color borderColor, Color fillColor, Stroke stroke) {
    this.borderColor = borderColor;
    this.fillColor = fillColor;
    this.stroke = stroke;
  }

  /**
   * Called from classes using the {@link y.view.Drawable} interface.
   * It delegates to {@link #paintIcon(java.awt.Component, java.awt.Graphics, int, int)}.
   *
   * @param g The graphics object to render on.
   */
  public void paint(Graphics2D g) {
    g.setStroke(stroke);
    paintIcon(null, g, bounds.x, bounds.y);
  }

  /**
   * Called from classes using the {@link javax.swing.Icon} interface and from {@link #paint(java.awt.Graphics2D)}.
   *
   * @param c The component the icon shall be rendered in.
   * @param g The graphics object to render on.
   * @param x The horizontal coordinate of the icon.
   * @param y The vertical coordinate of the icon.
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    // update the bounds if necessary
    if (bounds == null ||
        bounds.getX() != x ||
        bounds.getY() != y) {
      int newWidth = (bounds == null) ? 0 : bounds.width;
      int newHeight = (bounds == null) ? 0 : bounds.height;
      bounds = new Rectangle(x, y, newWidth, newHeight);
    }

    // if the stripe shall be painted as an icon, it shall be horizontally centered in it's containing component.
    int cWidth = 0;
    if (c != null) {
      cWidth = c.getWidth() - 2;
    }
    int offX = (cWidth > getIconWidth()) ? (cWidth - getIconWidth())/2 : 0;

    g.setColor(fillColor);
    g.fillRect(bounds.x + offX, bounds.y, bounds.width, bounds.height);

    g.setColor(borderColor);
    g.drawRect(bounds.x + offX, bounds.y, bounds.width, bounds.height);

    if (insets != null &&
        (insets.top + insets.bottom < bounds.height &&
         insets.left + insets.right < bounds.width)) {
      g.setColor(fillColor);
      g.fillRect((int) (bounds.x + offX + insets.left),
              (int) (bounds.y + insets.top),
              (int) (bounds.width - insets.left - insets.right),
              (int) (bounds.height - insets.top - insets.bottom));
      g.setColor(borderColor);
      g.drawRect((int) (bounds.x + offX + insets.left),
              (int) (bounds.y + insets.top),
              (int) (bounds.width - insets.left - insets.right),
              (int) (bounds.height - insets.top - insets.bottom));
    }
  }

  public Rectangle getBounds() {
    return bounds;
  }

  /**
   * Sets the specified <code>bounds</code>.
   * @param bounds The new bounds of the drawable.
   */
  public void setBounds(Rectangle2D bounds) {
    this.bounds = new Rectangle((int) bounds.getX(), (int) bounds.getY(),
                        (int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight()));
  }

  /**
   * Sets the bounds to the specified values.
   * @param x The horizontal coordinate.
   * @param y The vertical coordinate.
   * @param width The width of the drawable.
   * @param height The height of the drawable.
   */
  public void setBounds(int x, int y, int width, int height) {
    this.bounds = new Rectangle(x, y, width, height);
  }

  public int getIconWidth() {
    return bounds != null ? bounds.width : 0;
  }

  public int getIconHeight() {
    return bounds != null ? bounds.height : 0;
  }
}

/**
 * Cell renderer for the drop item list that is used as DnD source to create
 * new nodes, columns, and rows.
 */
class DropItemListCellRenderer implements ListCellRenderer {
  /**
   * Value type constant representing a {@link y.view.tabular.TableGroupNodeRealizer.Row}.
   */
  static final Object DROP_TYPE_ROW = "DROP_TYPE_ROW";
  /**
   * Value type constant representing a {@link y.view.tabular.TableGroupNodeRealizer.Column}.
   */
  static final Object DROP_TYPE_COLUMN = "DROP_TYPE_COLUMN";


  private static final Dimension PREFERRED_SIZE = new Dimension(100, 100);

  private final DefaultListCellRenderer dlcr;
  private final RealizerCellRenderer realizerRenderer;

  private final Icon rowIcon;
  private final Icon columnIcon;

  /**
   * Creates a new <code>DropItemListCellRenderer</code>.
   *
   * @param columnIcon   the icon to display {@link #DROP_TYPE_COLUMN} values.
   * @param rowIcon      the icon to display {@link #DROP_TYPE_COLUMN} values.
   */
  DropItemListCellRenderer(
          final Icon columnIcon,
          final Icon rowIcon
  ) {
    this.columnIcon = columnIcon;
    this.rowIcon = rowIcon;

    realizerRenderer = new RealizerCellRenderer(
            PREFERRED_SIZE.width, PREFERRED_SIZE.height);
    dlcr = new DefaultListCellRenderer();
  }

  public Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus
  ) {
    if (value instanceof NodeRealizer) {
      final Component c = realizerRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (c instanceof JComponent) {
        if (value instanceof GenericNodeRealizer) {
          final String configuration = ((GenericNodeRealizer) value).getConfiguration();
          if (SwimlaneDemo.CONFIGURATION_GROUP_NODE.equals(configuration)) {
            ((JComponent) c).setToolTipText("Create new group node");
          }
          if (SwimlaneDemo.CONFIGURATION_TABLE_NODE.equals(configuration)) {
            ((JComponent) c).setToolTipText("Create new table node");
          }
          if (DemoDefaults.NODE_CONFIGURATION.equals(configuration)) {
            ((JComponent) c).setToolTipText("Create new child node");
          }
        } else {
          ((JComponent) c).setToolTipText("Create new node");
        }
      }
      return c;
    } else {
      dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      dlcr.setText("");
      dlcr.setPreferredSize(PREFERRED_SIZE);
      if (DROP_TYPE_COLUMN.equals(value)) {
        dlcr.setIcon(columnIcon);
        dlcr.setToolTipText("Create new column");
      } else if (DROP_TYPE_ROW.equals(value)) {
        dlcr.setIcon(rowIcon);
        dlcr.setToolTipText("Create new row");
      } else {
        dlcr.setIcon(null);
        dlcr.setToolTipText(null);
      }
      return dlcr;
    }
  }
}
