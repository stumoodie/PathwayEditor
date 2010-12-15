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
package demo.layout.organic;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.layout.organic.OrganicLayouter;
import y.module.GRIPModule;
import y.module.OrganicEdgeRouterModule;
import y.module.OrganicLayoutModule;
import y.module.SmartOrganicLayoutModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;

/**
 * Demonstrates different organic layout algorithms and 
 * how to specify individual preferred edge lengths 
 * for OrganicLayouter.  
 * <br>
 * In this demo the edge lengths can be specified by right clicking 
 * on an edge or applying the current node distances using the button from the 
 * toolbar.
 * <br>
 * Choose the item "Edit Preferred Edge Length" from the context menu to open up 
 * a label editor that allows for entering a value for the edge length in pixels.
 * Note that the entered value must be numeric. Otherwise 
 * a default length will be chosen.
 */
public class OrganicLayouterDemo extends DemoBase {
  EdgeMap preferredEdgeLengthMap;
  YModule module;

  public OrganicLayouterDemo() {
    preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
    view.getGraph2D().addDataProvider(OrganicLayouter.PREFERRED_EDGE_LENGTH_DATA, preferredEdgeLengthMap);
    module = new SmartOrganicLayoutModule();
    loadGraph("resource/organic.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode(editMode);

    editMode.setPopupMode(new PopupMode() {
      public JPopupMenu getEdgePopup(Edge e) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditLabel(e));
        return pm;
      }
    });
  }

  /**
   * Returns ViewActionDemo toolbar plus actions to trigger some layout algorithms 
   */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();

    bar.addSeparator();
    bar.add(new LayoutAction());
    bar.add(new OptionAction());
    bar.add(new AssignLengthsAction("Assign Lengths"));
    return bar;
  }

  protected JMenuBar createMenuBar() {
    JMenuBar mb = super.createMenuBar();
    JMenu layoutMenu = new JMenu("Style");
    ButtonGroup bg = new ButtonGroup();
    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new OrganicLayoutModule();
      }
    };
    JRadioButtonMenuItem item = new JRadioButtonMenuItem("Classic");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new SmartOrganicLayoutModule();
      }
    };
    item = new JRadioButtonMenuItem("Smart");
    item.addActionListener(listener);
    item.setSelected(true);
    bg.add(item);
    layoutMenu.add(item);

    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new GRIPModule();
      }
    };
    item = new JRadioButtonMenuItem("GRIP");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new OrganicEdgeRouterModule();
      }
    };
    item = new JRadioButtonMenuItem("EdgeRouting");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    mb.add(layoutMenu);
    return mb;
  }

  /**
   *  Displays the layout options for organic layouter
   */
  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Options...");
    }

    public void actionPerformed(ActionEvent e) {
      //display the option handler
      OptionHandler op = module.getOptionHandler();
      if (op != null) {
        op.showEditor();
      }
    }
  }

  /**
   *  Launches the OrganicLayouter.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout");
    }

    public void actionPerformed(ActionEvent e) {
      //update preferredEdgeLengthData before launching the module
      Graph2D graph = view.getGraph2D();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();
        String eLabel = graph.getLabelText(edge);
        preferredEdgeLengthMap.set(edge, null);
        try {
          preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
        }
        catch (Exception ex) {
        }
      }

      //start the module
      module.start(view.getGraph2D());
    }
  }

  /**
   * Action that opens a text editor for the label of an edge
   * <p>
   * The inlined label editor allows to enter a single line of
   * label text for an edge. To terminate the label editor 
   * press "Enter".
   */
  class EditLabel extends AbstractAction {
    Edge e;

    EditLabel(Edge e) {
      super("Edit Preferred Edge Length");
      this.e = e;
    }

    public void actionPerformed(ActionEvent ev) {

      final EdgeRealizer r = view.getGraph2D().getRealizer(e);
      final YLabel label = r.getLabel();

      view.openLabelEditor(label,
          label.getBox().getX(),
          label.getBox().getY(),
          null, true);
    }
  }

  class AssignLengthsAction extends AbstractAction {
    public AssignLengthsAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        NodeRealizer snr = g.getRealizer(ec.edge().source());
        NodeRealizer tnr = g.getRealizer(ec.edge().target());
        double deltaX = snr.getCenterX() - tnr.getCenterX();
        double deltaY = snr.getCenterY() - tnr.getCenterY();
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        EdgeRealizer er = g.getRealizer(ec.edge());
        er.getLabel().setText(Integer.toString((int) dist));
      }
      g.updateViews();
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new OrganicLayouterDemo()).start("Organic Layouter Demo");
      }
    });
  }
}


      
