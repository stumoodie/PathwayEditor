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
import y.base.Node;
import y.base.Edge;
import y.view.EditMode;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *  Demonstrates how to display context sensitive popup menus
 *  in the view.
 * 
 *  This demo does also show how to write an action that opens
 *  an inlined text editor in the view to modify the label of a node.  
 *
 *  To activate the popup menus right click either on a node or
 *  the view background. 
 */
public class PopupModeDemo extends DemoBase
{

  protected EditMode createEditMode() {
    EditMode editMode =  super.createEditMode();
    //add a popup child mode to editMode (one that listens to the right mouse click
    //and pops up context sensitive menus)
    editMode.setPopupMode( new DemoPopupMode() );
    loadGraph("resource/popup.graphml");
    return editMode;
  }
  
  class DemoPopupMode extends PopupMode
  {
    /** Popup menu for a hit edge */
    public JPopupMenu getEdgePopup(Edge e)
    {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new ShowEdgeInfo(e));
      return pm;
    }

    /** Popup menu for a hit node */
    public JPopupMenu getNodePopup(Node v)
    {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new ShowNodeInfo(v));
      pm.add(new EditLabel(v));
      return pm;
    }

    /** Popup menu for a paper (plain background) hit */
    public JPopupMenu getPaperPopup(double x, double y)
    {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new Zoom(0.8));
      pm.add(new Zoom(1.2));
      pm.add(new FitContent(view));
      return pm;
    }

    /** Popup menu for a paper hit if things are selected */
    public JPopupMenu getSelectionPopup(double x, double y)
    {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new DeleteSelection(view));
      return pm;
    }
  }

  /**
   * Action that displays an information dialog for an edge.
   */
  class ShowEdgeInfo extends AbstractAction
  {
    Edge e;

    ShowEdgeInfo(Edge e )
    {
      super("Edge Info");
      this.e = e;
    }

    public void actionPerformed(ActionEvent e)
    {
      JOptionPane.showMessageDialog(
              view,
              "Edge from \"" + this.e.source() +
              "\" to \"" + this.e.target() + "\".");
    }
  }

  /**
   * Action that displays an information dialog for a node.
   */
  class ShowNodeInfo extends AbstractAction
  {
    Node v;

    ShowNodeInfo(Node v)
    {
      super("Node Info");
      this.v = v;
    }

    public void actionPerformed(ActionEvent e)
    {
      JOptionPane.showMessageDialog(view,
                                    "Label text of node is " +
                                    view.getGraph2D().getLabelText(v) +
                                    "\n\n(Guess you knew that already :-)");
    }
  }

  /**
   * Action that opens a text editor for the label of a node 
   * <p>
   * The inlined label editor allows to enter multiple lines of
   * label text for a node. The "Enter" or "Return" key starts
   * a new line of text. To terminate the label editor click
   * the mouse somewhere outside of the label editor box.
   */
  class EditLabel extends AbstractAction
  {
    Node v;

    EditLabel(Node v)
    {
      super("Edit Label");
      this.v = v;
    }

    public void actionPerformed(ActionEvent e)
    {

      final NodeRealizer r = view.getGraph2D().getRealizer(v);
      final YLabel label = r.getLabel();


      // optional property change listener, that gets invoked
      // after the label editor has changed the value of the 
      // label text. what this listener does is to adapt the size
      // of the node to the new label text
      PropertyChangeListener pcl = new PropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent pce)
            {
              r.setSize(label.getWidth()+10,label.getHeight()+10);
            }
        };

      view.openLabelEditor(label,
                           label.getBox().getX(),
                           label.getBox().getY(),
                           pcl,    //optional propertyChangeListener
                           true    //optional single line mode activated
                           );
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new PopupModeDemo()).start("Popup Mode Demo");
      }
    });
  }
}


      
