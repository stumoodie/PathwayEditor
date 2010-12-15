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
package demo.view;

import y.io.GraphMLIOHandler;
import y.option.OptionHandler;
import y.util.D;
import y.view.EditMode;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Demonstrates basic usage of the Graph2DView.
 * <p>
 * Demonstrates how some actions can be performed on the view.
 * The actions are:
 * </p>
 * <ul>
 *   <li>Remove selected parts of the view content</li>
 *   <li>Zoom out of the view</li>
 *   <li>Zoom in on the view</li>
 *   <li>Reset the zoom in on the view</li>
 *   <li>Fit view content to the size of the the view</li>
 *   <li>Print a graph</li>
 *   <li>Load a graph in GraphML format</li>
 *   <li>Save a graph in GraphML format</li>
 * </ul>
 * <p>
 * Additionally, this demo shows how to set up the default edit mode
 * to display tool tips over nodes.
 * </p>
 */
public class ViewActionDemo extends JPanel {

  /**
   * The view component of this demo.
   */
  protected Graph2DView view;
  /**
   * The view mode to be used with the view.
   */
  protected EditMode editMode;


  public ViewActionDemo() {
    setLayout(new BorderLayout());

    view = new Graph2DView();
    view.setAntialiasedPainting(true);
    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());

    editMode = createEditMode();
    if (editMode != null) {
      view.addViewMode(editMode);
    }

    Graph2DViewActions actions = new Graph2DViewActions(view);
    InputMap imap = actions.createDefaultInputMap();
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);

    add(view, BorderLayout.CENTER);
    add(createToolBar(), BorderLayout.NORTH);
  }

  protected EditMode createEditMode() {
    final EditMode editMode = new EditMode();
    editMode.showNodeTips(true);
    return editMode;
  }

  /**
   * Creates a toolbar for this demo.
   * @return the application toolbar.
   */
  protected JToolBar createToolBar() {
    JToolBar bar = new JToolBar();
    bar.add(new DeleteSelection());
    bar.add(new Zoom(1.2));
    bar.add(new Zoom(0.8));
    bar.add(new ResetZoom());
    bar.add(new FitContent());

    return bar;
  }

  /**
   * Create a menu bar for this demo.
   * @return the application menu bar.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar bar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menu.add(createLoadAction());
    menu.add(createSaveAction());
    menu.addSeparator();
    menu.add(new PrintAction());
    menu.addSeparator();
    menu.add(new ExitAction());
    bar.add(menu);
    return bar;
  }

  protected Action createLoadAction() {
    return new LoadAction();
  }

  protected Action createSaveAction() {
    return new SaveAction();
  }

  /**
   * Creates an application frame for this demo and displays it.
   * The name of this class will be the title of the displayed frame.
   */
  public void start() {
    start(getClass().getName());
  }

  /**
   * Creates an application frame for this demo and displays it.
   * @param title the title of the display frame.
   */
  public void start(final String title) {
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo(final JRootPane rootPane) {
    rootPane.setJMenuBar(createMenuBar());
    rootPane.setContentPane(this);
  }

  /**
   * Initializes to a "nice" look and feel.
   */
  public static void initLnF() {
    try {
      if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName())
          && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith(
          "Windows") && "6.1".equals(System.getProperty("os.version")))) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Launches this demo.
   * @param args ignored.
   */
  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new ViewActionDemo()).start();
      }
    });
  }

  protected GraphMLIOHandler createGraphMLIOHandler() {
    return new GraphMLIOHandler();
  }


  /**
   * Action that prints the contents of the view
   */
  protected class PrintAction extends AbstractAction {
    PageFormat pageFormat;
    OptionHandler printOptions;

    public PrintAction() {
      super("Print");
      putValue(Action.SHORT_DESCRIPTION, "Print");

      //setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.addInt("Poster Rows", 1);
      printOptions.addInt("Poster Columns", 1);
      printOptions.addBool("Add Poster Coords", false);
      final String[] area = {"View", "Graph"};
      printOptions.addEnum("Clip Area", area, 1);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DPrinter gprinter = new Graph2DPrinter(view);

      //show custom print dialog and adopt values
      if (!printOptions.showEditor()) {
        return;
      }
      gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
      gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
      gprinter.setPrintPosterCoords(
          printOptions.getBool("Add Poster Coords"));
      if ("Graph".equals(printOptions.get("Clip Area"))) {
        gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
      } else {
        gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
      }

      //show default print dialogs
      PrinterJob printJob = PrinterJob.getPrinterJob();
      if (pageFormat == null) {
        pageFormat = printJob.defaultPage();
      }
      PageFormat pf = printJob.pageDialog(pageFormat);
      if (pf == pageFormat) {
        return;
      } else {
        pageFormat = pf;
      }

      //setup printjob.
      //Graph2DPrinter is of type Printable
      printJob.setPrintable(gprinter, pageFormat);

      if (printJob.printDialog()) {
        try {
          printJob.print();
        } catch (PrinterException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * Action that terminates the application
   */
  protected static class ExitAction extends AbstractAction {
    ExitAction() {
      super("Exit");
      putValue(Action.SHORT_DESCRIPTION, "Exit");
    }

    public void actionPerformed(ActionEvent e) {

      System.exit(0);
    }
  }

  JFileChooser createGraphMLFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".graphml");
      }

      public String getDescription() {
        return "GraphML Format (.graphml)";
      }
    });

    return chooser;
  }

  /**
   * Action that saves the current graph to a file in GraphML format.
   */
  protected class SaveAction extends AbstractAction {
    JFileChooser chooser;

    public SaveAction() {
      super("Save...");
      putValue(Action.SHORT_DESCRIPTION, "Save...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = createGraphMLFileChooser();
      }
      if (chooser.showSaveDialog(ViewActionDemo.this) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        GraphMLIOHandler ioh = new GraphMLIOHandler();
        try {
          ioh.write(view.getGraph2D(), name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
  }

  /**
   * Action that loads the current graph from a file in GraphML format.
   */
  protected class LoadAction extends AbstractAction {
    JFileChooser chooser;

    public LoadAction() {
      super("Load...");
      putValue(Action.SHORT_DESCRIPTION, "Load...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = createGraphMLFileChooser();
      }
      if (chooser.showOpenDialog(ViewActionDemo.this) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        GraphMLIOHandler ioh = createGraphMLIOHandler();
        try {
          view.getGraph2D().clear();
          ioh.read(view.getGraph2D(), name);
        } catch (IOException ioe) {
          D.show(ioe);
        }

        //force redisplay of view contents
        view.fitContent();
        view.getGraph2D().updateViews();
      }
    }
  }

  /**
   * Action that deletes the selected parts of the graph.
   */
  protected class DeleteSelection extends AbstractAction {
    public DeleteSelection() {
      super("Delete Selection");
      putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
      URL imageURL = getClass().getResource("resource/delete.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().removeSelection();
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Action that applies a specified zoom level to the view.
   */
  protected class Zoom extends AbstractAction {
    double factor;

    public Zoom(double factor) {
      final String name = "Zoom " + (factor > 1.0 ? "In" : "Out");
      putValue(Action.NAME, name);
      putValue(Action.SHORT_DESCRIPTION, name);
      URL imageURL;
      if (factor > 1.0d) {
        imageURL = getClass().getResource("resource/zoomIn.png");
      } else {
        imageURL = getClass().getResource("resource/zoomOut.png");
      }
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.factor = factor;
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      //optional code that adjusts the size of the
      //view's world rectangle. The world rectangle
      //defines the region of the canvas that is
      //accessible by using the scrollbars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that resets the view's zoom level to <code>1.0</code>.
   */
  protected class ResetZoom extends AbstractAction {
    public ResetZoom() {
      super("Reset Zoom");
      this.putValue(Action.SHORT_DESCRIPTION, "Reset Zoom");
      final URL imageURL = getClass().getResource("resource/zoomOriginal.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
    }

    public void actionPerformed( final ActionEvent e ) {
      view.setZoom(1);
      // optional code that adjusts the size of the
      // view's world rectangle. The world rectangle
      // defines the region of the canvas that is
      // accessible by using the scroll bars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that fits the content nicely inside the view.
   */
  protected class FitContent extends AbstractAction {
    public FitContent() {
      super("Fit Content");
      putValue(Action.SHORT_DESCRIPTION, "Fit Content");
      final URL imageURL = getClass().getResource("resource/zoomFit.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
    }

    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }
}
