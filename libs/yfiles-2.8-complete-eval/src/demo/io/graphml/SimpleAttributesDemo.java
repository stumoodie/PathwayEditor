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
package demo.io.graphml;


import demo.view.DemoDefaults;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.option.OptionHandler;
import y.view.EditMode;
import y.view.HitInfo;
import y.view.PopupMode;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

/**
 * This demo shows how to configure GraphMLIOHandler to be able to handle
 * extra node and edge and  of simple type.
 * Additional data for a node or an edge can be edited by right-clicking on the corresponding
 * element.
 * The element tool tip will show the currently set data values for each element.
 */
public class SimpleAttributesDemo extends GraphMLDemo {

  /** stores a boolean value for each node **/
  private NodeMap node2BoolMap;

  /** stores an int value for each edge **/
  private EdgeMap edge2IntMap;

  public SimpleAttributesDemo() {
    //define a view mode that displays the currently set data values
    ViewMode tooltipMode = new ViewMode() {
      public void mouseMoved(double x, double y) {
        HitInfo info = getHitInfo(x, y);
        if (info.getHitNode() != null) {
          view.setToolTipText("Node:BooleanValue=" + node2BoolMap.getBool(info.getHitNode()));
        } else if (info.getHitEdge() != null) {
          view.setToolTipText("Edge:IntValue=" + edge2IntMap.getInt(info.getHitEdge()));
        }
      }
    };
    //add the view mode to the view
    view.addViewMode(tooltipMode);
  }

  protected void loadInitialGraph() {
    loadGraph("resources/custom/simple-attributes.graphml");
  }

  /**
   * Configures GraphMLIOHandler to read and write additional node, edge and graph data
   * of a simple type.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
      //Create maps that store the attributes
    if (node2BoolMap == null) {
      node2BoolMap = view.getGraph2D().createNodeMap();
    }
    if (edge2IntMap == null) {
      edge2IntMap = view.getGraph2D().createEdgeMap();
    }

    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();

    //  <key id="d1" for="node" attr.name="BooleanValue" attr.type="boolean"/>
    ioHandler.getGraphMLHandler().addInputDataAcceptor("BooleanValue", node2BoolMap, KeyScope.NODE, KeyType.BOOLEAN);
    ioHandler.getGraphMLHandler().addOutputDataProvider("BooleanValue", node2BoolMap, KeyScope.NODE, KeyType.BOOLEAN);

    //  <key id="d3" for="edge" attr.name="IntValue" attr.type="int"/>
    ioHandler.getGraphMLHandler().addInputDataAcceptor("IntValue", edge2IntMap, KeyScope.EDGE, KeyType.INT);
    ioHandler.getGraphMLHandler().addOutputDataProvider("IntValue", edge2IntMap, KeyScope.EDGE, KeyType.INT);
    return ioHandler;
  }


  protected String[] getSampleFiles() {
    return null;
  }

  /**
   * Create an edit mode that displays a context-sensitive popup-menu when right-clicking
   * on an the canvas.
   */
  protected EditMode createEditMode() {
    EditMode mode = super.createEditMode();

    mode.setPopupMode(new PopupMode() {
      public JPopupMenu getNodePopup(Node v) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Node Attribute...", v, node2BoolMap, KeyType.BOOLEAN));
        return pm;
      }

      public JPopupMenu getEdgePopup(Edge e) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Edge Attribute...", e, edge2IntMap, KeyType.INT));
        return pm;
      }
    });

    return mode;
  }


  /**
   * Editor action for the additional node, edge and graph attributes.
   */
  class EditAttributeAction extends AbstractAction {
    private Object object;
    private DataMap dataMap;
    private KeyType dataType;

    private OptionHandler op;

    EditAttributeAction(String name, Object object, DataMap dataMap, KeyType dataType) {
      super(name);
      this.object = object;
      this.dataMap = dataMap;
      this.dataType = dataType;
      op = new OptionHandler(name);
      if (dataType == KeyType.BOOLEAN) {
        op.addBool("Boolean Value", dataMap.getBool(object));
      } else if (dataType == KeyType.INT) {
        op.addInt("Integer Value", dataMap.getInt(object));
      }
    }

    public void actionPerformed(ActionEvent actionEvent) {
      if (op.showEditor()) {
        if (dataType == KeyType.BOOLEAN) {
          dataMap.setBool(object, op.getBool("Boolean Value"));
        } else if (dataType == KeyType.INT) {
          dataMap.setInt(object, op.getInt("Integer Value"));          
        }        
        graphMLPane.updateGraphMLText(view.getGraph2D());
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SimpleAttributesDemo()).start();
      }
    });
  }
}
