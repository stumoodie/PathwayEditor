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

import y.base.Edge;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.PopupMode;
import y.view.Graph2D;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2DViewActions;
import y.view.AutoDragViewMode;
import y.view.MovePortMode;
import y.view.hierarchy.AutoBoundsFeature;
import y.base.Node;

import javax.swing.JPopupMenu;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;

/**
 * Component that visualizes Flowchart diagrams.
 */
public class FlowchartView extends Graph2DView {
  private FlowchartActions flowchartActions;

  /**
   * Creates a new Graph2DView containing an empty graph and register Flowchart specific view modes and actions
   * The constructor calls the following methods, in oder to register respective view modes and actions:
   * <ul>
   * <li> {@link #registerViewModes()}</li>
   * <li> {@link #registerViewActions()} </li>
   * <li> {@link #registerViewListeners()}</li>
   * </ul>
   */
  public FlowchartView() {
    super();
    //Some default behaviour
    this.setFitContentOnResize(true);

    //init
    registerViewModes();
    registerViewActions();
    registerViewListeners();
  }

  /**
   * Callback method, which registers Flowchart specific view actions.
   */
  protected void registerViewActions() {
    flowchartActions = new FlowchartActions(this);
    flowchartActions.install();
  }

  /**
   * Callback method, which registers Flowchart specific view modes and configures them.
   */
  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    // Route all edges orthogonally.
    editMode.setOrthogonalEdgeRouting(true);

    CreateEdgeMode createEdgeMode = (CreateEdgeMode) editMode.getCreateEdgeMode();
    createEdgeMode.setOrthogonalEdgeCreation(true);
    createEdgeMode.setIndicatingTargetNode(true);
    editMode.setSnappingEnabled(true);

    //add hierarchy actions to the views popup menu
    editMode.setPopupMode(new FlowchartPopupMode());
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    editMode.assignNodeLabel(false);

    ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
    editMode.showNodeTips(true);

    //allow moving view port with right drag gesture
    editMode.allowMovingWithPopup(true);
    addViewMode(editMode);

    //Auto drag mode
    addViewMode(new AutoDragViewMode());
  }

  /**
   * Callback method, which registers  specific listeners and configures them.
   */
  protected void registerViewListeners() {
    Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
    //zoom in/out at mouse pointer location
    wheelZoomListener.setCenterZooming(false);
    this.getCanvasComponent().addMouseWheelListener(wheelZoomListener);
  }


  //////////////////////////////////////////////////////////////////////////////
  // VIEW MODES ////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * provides the context sensitive popup menus
   */
  private class FlowchartPopupMode extends PopupMode {

    public JPopupMenu getNodePopup(Node v) {
      JPopupMenu pm = new JPopupMenu();
      if (v != null) {
        JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
        deleteItem.setText("Delete Node");
      }
      return pm;
    }

    public JPopupMenu getEdgePopup(Edge e) {
      JPopupMenu pm = new JPopupMenu();
      if (e != null) {
        JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
        deleteItem.setText("Delete Edge");
      }
      return pm;
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
      deleteItem.setText("Delete Selection");
      return pm;
    }
  }

  private static final class FlowchartActions extends Graph2DViewActions {
    private Graph2DView view;

    private FlowchartActions(Graph2DView view) {
      super(view);
      this.view = view;
    }

    /** Returns a new ActionMap that contains the actions provided by this class. */
    public ActionMap createActionMap() {
      final DeleteSelectionAction deleteAction = new DeleteSelectionAction(view) {

        protected void deleteNodeElements(Graph2D graph, Node node) {

          Node parent = graph.getHierarchyManager().getParentNode(node);
          if (parent != null) {
            AutoBoundsFeature abf = graph.getRealizer(parent).getAutoBoundsFeature();
            boolean oldEnabled = abf != null && abf.isAutoBoundsEnabled();
            if (oldEnabled) {
              abf.setAutoBoundsEnabled(false);
            }
            try {
              super.deleteNodeElements(graph, node);
            } finally {
              if (oldEnabled) {
                abf.setAutoBoundsEnabled(true);
              }
            }
          } else {
            super.deleteNodeElements(graph, node);
          }
        }

      };
      deleteAction.setDeletionMask(DeleteSelectionAction.ALL_TYPES_MASK);
      final ActionMap amap = super.createActionMap();
      amap.put(DELETE_SELECTION, deleteAction);
      return amap;
    }
  }
}