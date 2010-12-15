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
package demo.option;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Node;
import y.base.NodeCursor;
import y.option.OptionHandler;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Selections;
import y.view.ShapeNodeRealizer;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * <p>
 * Demonstrates how to create a node property editor for nodes.
 * This demo makes use of the "value-undefined" state of option items.
 * <p>
 * A node property editor can either be displayed for a single node
 * by double-clicking on the node or for multiple nodes by first
 * selecting the nodes and then clicking on the "Edit Node Properties" 
 * toolbar button.
 * <p>
 * The property editor will be initialized by the current settings
 * of the selected nodes. If the value of a specific property differs for two
 * selected nodes the editor will display the value as undefined. 
 * Upon closing the editor dialog, only well-defined values will be 
 * committed to the selected nodes.
 * </p>
 */
public class NodePropertyEditorDemo extends DemoBase
{
  NodePropertyEditorAction nodePropertyEditorAction;
  
  public NodePropertyEditorDemo() {
    
    //open property editor upon double-clicking on a node
    view.addViewMode(new ViewMode() {
      public void mouseClicked(MouseEvent ev) {
        if(ev.getClickCount() == 2) {
          Node v = getHitInfo(ev).getHitNode();
          if(v != null) {
            nodePropertyEditorAction.actionPerformed(null);
          }
        }
      }
    });
  }
  
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    //add node property action to toolbar
    bar.add(nodePropertyEditorAction = new NodePropertyEditorAction());
    return bar;
  }
  
  class NodePropertyEditorAction extends AbstractAction {

    NodePropertyHandler nodePropertyHandler;
    
    NodePropertyEditorAction() {
      super("Node Properties");
      putValue(Action.SMALL_ICON,
          new ImageIcon( DemoBase.class.getResource("resource/properties.png")));
      putValue(Action.SHORT_DESCRIPTION, "Edit Node Properties");
   
      Selections.SelectionStateObserver sso = new Selections.SelectionStateObserver() {
        protected void updateSelectionState(Graph2D graph) 
        {
          setEnabled(view.getGraph2D().selectedNodes().ok());
        } 
      };
      
      view.getGraph2D().addGraph2DSelectionListener(sso);
      view.getGraph2D().addGraphListener(sso);
  
      setEnabled(false);
    
      nodePropertyHandler = new NodePropertyHandler();
    }
    
    public void actionPerformed(ActionEvent ev) {
      Graph2D graph = view.getGraph2D();
      if(!Selections.isNodeSelectionEmpty(graph)) {
        nodePropertyHandler.updateValuesFromSelection(graph);
        if(nodePropertyHandler.showEditor(view.getFrame())) {
          nodePropertyHandler.commitNodeProperties(graph);
          graph.updateViews();
        }
      }
    }
  }
  
  public static class NodePropertyHandler extends OptionHandler 
  {
    static final String[] shapeTypes = { "Rectangle", "Rounded Rectangle", "Ellipse" };
    public NodePropertyHandler() {
      super("Node Properties");
      addString("Label", "").setValueUndefined(true);
      addEnum("Shape Type", shapeTypes, 0).setValueUndefined(true);
      addColor("Color", null, true).setValueUndefined(true);
      addDouble("Width", 0.0).setValueUndefined(true);
      addDouble("Height", 0.0).setValueUndefined(true);
    }
  
    /**
     * Retrieves the values from the set of selected nodes (actually node 
     * realizers) and stores them in the respective option items. 
     */
    public void updateValuesFromSelection(Graph2D graph)
    {
      NodeCursor nc = graph.selectedNodes();
      NodeRealizer nr = graph.getRealizer(nc.node());
      
      // Get the initial values from the first selected node. 
      String label = nr.getLabelText();
      boolean sameLabels = true;
      byte shapeType = 0;
      boolean onlyShapeNR = true;
      boolean sameShapeType = true;
      if (nr instanceof ShapeNodeRealizer)
        shapeType = ((ShapeNodeRealizer)nr).getShapeType();
      else
      {
        onlyShapeNR = false;
        sameShapeType = false;
      }
      Color color = nr.getFillColor();
      boolean sameColor = true;
      double width = nr.getWidth();
      boolean sameWidth = true;
      double height = nr.getHeight();
      boolean sameHeight = true;
      
      // Get all further values from the remaining set of selected node 
      // realizers. 
      if (nc.size() > 1)
      {
        for (nc.next(); nc.ok(); nc.next())
        {
          nr = graph.getRealizer(nc.node());
          
          if (sameLabels && !label.equals(nr.getLabelText()))
            sameLabels = false;
          if (sameShapeType && onlyShapeNR)
          {
            if (nr instanceof ShapeNodeRealizer)
            {
              if (shapeType != ((ShapeNodeRealizer)nr).getShapeType())
                sameShapeType = false;
            }
            else
            {
              onlyShapeNR = false;
              sameShapeType = false;
            }
          }
          if (sameColor && color != nr.getFillColor())
            sameColor = false;
          if (sameWidth && width != nr.getWidth())
            sameWidth = false;
          if (sameHeight && height != nr.getHeight())
            sameHeight = false;
          
          if (!(sameLabels | sameShapeType | sameColor | sameWidth | sameHeight))
            break;
        }
      }
      
      // If, for a single property, there are multiple values present in the set 
      // of selected node realizers, then the respective option item is set to 
      // indicate an "undefined value" state. 
      // Note that property "valueUndefined" for an option item is set *after* 
      // its value has actually been modified! 
      set("Label", label);
      getItem("Label").setValueUndefined(!sameLabels);
      
      set("Shape Type", shapeTypes[shapeType]);
      getItem("Shape Type").setValueUndefined(!sameShapeType);
      getItem("Shape Type").setEnabled(onlyShapeNR);

      set("Color", color);
      getItem("Color").setValueUndefined(!sameColor);
      
      set("Width", new Double(width));
      getItem("Width").setValueUndefined(!sameWidth);
      
      set("Height", new Double(height));
      getItem("Height").setValueUndefined(!sameHeight);
    }
   
    public void commitNodeProperties(Graph2D graph) 
    {
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
      {
        Node n = nc.node();
        NodeRealizer nr = graph.getRealizer(n);
        
        if (!getItem("Label").isValueUndefined())
          nr.setLabelText(getString("Label"));
        if (!getItem("Shape Type").isValueUndefined() && nr instanceof ShapeNodeRealizer)
          ((ShapeNodeRealizer)nr).setShapeType((byte)getEnum("Shape Type"));
        if (!getItem("Color").isValueUndefined())
          nr.setFillColor((Color)get("Color"));
        if (!getItem("Width").isValueUndefined())
          nr.setWidth(getDouble("Width"));
        if (!getItem("Height").isValueUndefined())
          nr.setHeight(getDouble("Height"));
      }
    }
  }
  
  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new NodePropertyEditorDemo()).start();
      }
    });
  }
}
