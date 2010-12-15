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
import y.option.OptionHandler;
import y.view.Graph2DPrinter;
import y.view.PrintPreviewPanel;
import y.view.GenericNodeRealizer;
import y.view.BevelNodePainter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Font;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Map;

/**
 * Demo that centers around the printing facilities of yFiles.
 * This class shows how to use the yFiles print preview and how to 
 * add a title and footer to the printed page or poster.   
 */
public class PrintPreviewDemo extends DemoBase {

  public PrintPreviewDemo() {
    loadGraph("resource/PrintPreviewDemo.graphml");
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    //register bevel node configuration that is used in initial graph
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map map = factory.createDefaultConfigurationMap();
    GenericNodeRealizer.Painter painter = new BevelNodePainter();
    map.put(GenericNodeRealizer.Painter.class, painter);
    map.put(GenericNodeRealizer.ContainsTest.class, painter);
    factory.addConfiguration("BevelNodeConfig", map);
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.add(new PrintPreviewAction());
    return bar;
  }

  /**
   * Action that brings up a customized print preview panel.
   */
  class PrintPreviewAction extends AbstractAction {
    Graph2DPrintPreviewPanel ppp;

    PrintPreviewAction() {
      super("Print Preview");

      PrinterJob printJob = PrinterJob.getPrinterJob();
      ppp = new Graph2DPrintPreviewPanel(
          printJob,
          new Graph2DPrinter(view),
          printJob.defaultPage());
    }

    public void actionPerformed(ActionEvent e) {
      final JDialog dialog = new JDialog((JFrame) view.getTopLevelAncestor(), contentPane.getName(), true);
      dialog.setContentPane(ppp);
      dialog.setResizable(true);
      dialog.pack();
      dialog.setVisible(true);
    }

  }

  /**
   * Extended print preview panel that incorporates the standard printing options
   * provided by class {@link y.view.Graph2DView}.
   */
  public class Graph2DPrintPreviewPanel extends PrintPreviewPanel {
    OptionHandler printOptions;
    Graph2DPrinter gp;


    public Graph2DPrintPreviewPanel(
        PrinterJob printJob,
        final Graph2DPrinter gp,
        PageFormat pf) {
      super(printJob,
          gp,
          gp.getPosterColumns(),
          gp.getPosterColumns() * gp.getPosterRows(),
          pf);
      this.gp = gp;

      //setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.useSection("General");

      printOptions.addInt("Poster Rows", gp.getPosterRows());
      printOptions.addInt("Poster Columns", gp.getPosterColumns());
      printOptions.addBool("Add Poster Coords", gp.getPrintPosterCoords());
      final String[] area = {"View", "Graph"};
      if (gp.getClipType() == Graph2DPrinter.CLIP_GRAPH) {
        printOptions.addEnum("Clip Area", area, 1);
      } else {
        printOptions.addEnum("Clip Area", area, 0);
      }

      Graph2DPrinter.DefaultTitleDrawable td = new Graph2DPrinter.DefaultTitleDrawable();
      printOptions.useSection("Title");
      printOptions.addString("Text", td.getText());
      printOptions.addColor("Titlebar Color", td.getTitleBarColor(), true);
      printOptions.addColor("Text Color", td.getTextColor(), true);
      printOptions.addInt("Font Size", contentPane.getFont().getSize());

      Graph2DPrinter.DefaultFooterDrawable fd = new Graph2DPrinter.DefaultFooterDrawable();
      printOptions.useSection("Footer");
      printOptions.addString("Text", fd.getText());
      printOptions.addColor("Footer Color", fd.getFooterColor(), true);
      printOptions.addColor("Text Color", fd.getTextColor(), true);
      printOptions.addInt("Font Size", contentPane.getFont().getSize());

      //show custom print dialog and adopt values
      Action optionAction = new AbstractAction("Options...") {
        public void actionPerformed(ActionEvent ev) {
          if (!printOptions.showEditor()) {
            return;
          }
          gp.setPosterRows(printOptions.getInt("Poster Rows"));
          gp.setPosterColumns(printOptions.getInt("Poster Columns"));
          gp.setPrintPosterCoords(
              printOptions.getBool("Add Poster Coords"));
          if ("Graph".equals(printOptions.get("Clip Area"))) {
            gp.setClipType(Graph2DPrinter.CLIP_GRAPH);
          } else {
            gp.setClipType(Graph2DPrinter.CLIP_VIEW);
          }

          Graph2DPrinter.DefaultTitleDrawable title =
              new Graph2DPrinter.DefaultTitleDrawable();
          title.setText(printOptions.getString("Title", "Text"));
          title.setTitleBarColor((Color) printOptions.get("Title", "Titlebar Color"));
          title.setTextColor((Color) printOptions.get("Title", "Text Color"));
          title.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Title", "Font Size")));
          gp.setTitleDrawable(title);

          Graph2DPrinter.DefaultFooterDrawable footer =
              new Graph2DPrinter.DefaultFooterDrawable();
          footer.setText(printOptions.getString("Footer", "Text"));
          footer.setFooterColor((Color) printOptions.get("Footer", "Footer Color"));
          footer.setTextColor((Color) printOptions.get("Footer", "Text Color"));
          footer.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Footer", "Font Size")));
          gp.setFooterDrawable(footer);

          setPages(0,
              gp.getPosterColumns(),
              gp.getPosterColumns() * gp.getPosterRows());

          zoomToFit();
        }
      };
      addControlComponent(new JButton(optionAction));

    }

  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new PrintPreviewDemo()).start();
      }
    });
  }

}

    

      
