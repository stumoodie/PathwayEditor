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
package demo.view.viewmode;

import demo.view.DemoBase;
import y.base.Edge;
import y.base.Node;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.HitInfo;

import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.EventQueue;

/**
 * Demonstrates how to customize CreateEdgeMode in order to control
 * the creation of edges and to provide feedback whether
   creating an edge to the node the mouse is hovering over is possible.
 * <br>
 * This demo only allows the creation of edges that start with nodes
 * that have an evenly numbered label and 
 * end with nodes that have unevenly numbered labels.
 */
public class CreateEdgeModeDemo extends DemoBase {
  // whether or not to display a message box when edge creation 
  // is not allowed.
  boolean showMessage = true;

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode( editMode );
    //set a custom CreateEdgeMode for the edge mode
    editMode.setCreateEdgeMode( new DemoCreateEdgeMode() );
    loadGraph("resource/popup.graphml");
  }

  protected Action createLoadAction() {
    //Overridden method to disable the Load menu in the demo
    return null;
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo
    return null;
  }

  class DemoCreateEdgeMode extends CreateEdgeMode {

    public void edgeMoved( double x, double y ) {
      super.edgeMoved( x, y );
      updateDummy( x, y );
    }

    public void edgeCreated( Edge e ) {
      getGraph2D().getRealizer( e ).setLineColor( getGraph2D().getDefaultEdgeRealizer().getLineColor() );
    }


    private void updateDummy( double x, double y ) {
      Node hitNode = (new HitInfo(view, x, y, true, HitInfo.NODE)).getHitNode();
      if ( hitNode != null ) {
        if ( acceptTargetNode( hitNode, x, y ) ) {
          getDummyEdgeRealizer().setLineColor( Color.green );
        } else {
          getDummyEdgeRealizer().setLineColor( Color.red );
        }
      } else {
        getDummyEdgeRealizer().setLineColor( getGraph2D().getDefaultEdgeRealizer().getLineColor() );
      }
    }

    protected boolean acceptSourceNode( Node source, double x, double y ) {
      try {
        return Integer.parseInt( getGraph2D().getLabelText( source ) ) % 2 == 0;
      } catch ( Exception ex ) {
        return true;
      }
    }

    protected void sourceNodeDeclined( Node source, double x, double y ) {
      if ( showMessage ) {
        cancelEdgeCreation();
        JOptionPane.showMessageDialog( this.view,
                                       "Only start nodes with even numbers allowed!",
                                       "Forbidden!",
                                       JOptionPane.ERROR_MESSAGE );
      }
    }

    protected boolean acceptTargetNode( Node target, double x, double y ) {
      try {
        return Integer.parseInt( getGraph2D().getLabelText( target ) ) % 2 != 0;
      } catch ( Exception ex ) {
        return true;
      }
    }

    protected void targetNodeDeclined( Node target, double x, double y ) {
      if ( showMessage ) {
        cancelEdgeCreation();
        JOptionPane.showMessageDialog( this.view,
                                       "Only end nodes with uneven numbers allowed!",
                                       "Forbidden!",
                                       JOptionPane.ERROR_MESSAGE );

      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new CreateEdgeModeDemo()).start("Create Edge Mode Demo");
      }
    });
  }
}


      
