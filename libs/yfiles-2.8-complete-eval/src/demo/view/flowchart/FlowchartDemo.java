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

import javax.swing.JMenuBar;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JMenu;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import demo.view.DemoBase;
import y.view.Graph2DClipboard;
import y.view.Graph2DView;
import y.view.Overview;
import y.view.Graph2DUndoManager;


/**
 * A viewer and editor for flowchart diagrams. It shows how to
 * <ul>
 * <li>integrate and configure an adjusted view, the {@link FlowchartView}</li>
 * <li>add a palette of flowchart symbols, the {@link FlowchartPalette}, to ease the creation of diagrams</li>
 * <li>add the specific properties panel</li>
 * <li>implement a {@link y.view.GenericNodeRealizer.Painter} tailored for the drawing of flowchart symbols</li>
 * </ul>
 */
public class FlowchartDemo extends DemoBase{

  private static final String[] EXAMPLES_FILE_NAMES = {
      "problemsolving.graphml",
      "studentRegistration.graphml"
  };

  FlowchartPalette palette;
  private Graph2DUndoManager undoManager;
  private Graph2DClipboard clipboard;

  /** Instantiates this demo. Builds the GUI. */
  public FlowchartDemo() {
    super();

    final JComponent workingWindows = createWorkBench();
    if (workingWindows!=null) {
      contentPane.add(workingWindows, BorderLayout.CENTER);
    }

    loadGraph("resource/graphs/" + EXAMPLES_FILE_NAMES[1]);
  }

  /**
   * Overwritten to register no view mode at all.
   * {@link demo.view.flowchart.FlowchartView}, the editor component that is
   * used to edit flowchart diagrams, registers all required
   * view modes upon its instantiation.
   * @see demo.view.flowchart.FlowchartView#registerViewModes()
   */
  protected void registerViewModes() {
  }

  /**
   * Creates a {@link FlowchartView}.
   * @return a <code>FlowchartView</code>
   */
  protected Graph2DView createGraphView() {
    Graph2DView view = new FlowchartView();
    view.setFitContentOnResize(true);
    return view;
  }

  /** Initializes the Flowchart palette and the undo manager. */
  protected void initialize() {
    palette = new FlowchartPalette(view);
    palette.setSnapMode(true);

    undoManager = new Graph2DUndoManager(view.getGraph2D());
    undoManager.setViewContainer(view);

    clipboard = new Graph2DClipboard(view);
    clipboard.setCopyFactory(view.getGraph2D().getGraphCopyFactory());
  }

  /**
   * Adds menu items for example graphs to the default menu bar.
   * @return the menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenu examplesMenu = new JMenu("Examples");
    for (int i = 0; i < EXAMPLES_FILE_NAMES.length; i++) {
      final String fileName = EXAMPLES_FILE_NAMES[i];
      examplesMenu.add(new JMenuItem(new AbstractAction(fileName) {
        public void actionPerformed(ActionEvent e) {
          loadGraph("resource/graphs/" + fileName);
        }
      }));
    }

    JMenuBar menuBar = super.createMenuBar();
    menuBar.add(examplesMenu);
    return menuBar;
  }

  /**
   * Adds undo/redo actions and cut/copy/paste actions to the default toolbar.
   * @return the toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createUndoAction());
    toolBar.add(createRedoAction());
    toolBar.addSeparator();
    toolBar.add(createCutAction());
    toolBar.add(createCopyAction());
    toolBar.add(createPasteAction());
    return toolBar;
  }

  /**
   * Callback for loading a graph. Overwritten to allow an empty URL and to reset the undo queue.
   */
  protected void loadGraph(URL resource) {
    view.getGraph2D().firePreEvent();
    view.getGraph2D().clear();
    super.loadGraph(resource);
    view.getGraph2D().firePostEvent();
    undoManager.resetQueue();
  }

  /**
   * Creates four panels which form the workbench of this demo: a Flowchart view, an overview, a palette and a property
   * window.
   * @return a JComponent containing the view, overview, palette and property panel.
   */
  private JComponent createWorkBench() {
    JPanel titledOverview = createTitledPanel(createOverview(), "Overview");
    JPanel titledPalette = createTitledPanel(this.palette, "Palette");
    JSplitPane overviewPaletteSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, titledOverview, titledPalette);
    //Split for left panel (overview+palette panel) and the view
    return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewPaletteSplit, view);
  }

  /**
   * Callback method that creates and configures the undo action.
   * @return the undo action.
   */
  protected Action createUndoAction() {
    Action undoAction = undoManager.getUndoAction();
    putIcon(undoAction, DemoBase.class.getResource("resource/undo.png"));
    undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo");

    return undoAction;
  }

  /**
   * Callback method that creates and configures the redo action.
   * @return the redo action.
   */
  protected Action createRedoAction() {
    Action redoAction = undoManager.getRedoAction();
    putIcon(redoAction, DemoBase.class.getResource("resource/redo.png"));
    redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo");
    return redoAction;
  }

  /**
   * Callback method that creates and configures the cut action.
   * @return the cut action.
   */
  protected Action createCutAction() {
    Action cutAction = clipboard.getCutAction();
    putIcon(cutAction, DemoBase.class.getResource("resource/cut.png"));
    cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut");

    view.getCanvasComponent().getActionMap().put("CUT", cutAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), "CUT");

    return cutAction;
  }

  /**
   * Callback method that creates and configures the copy action.
   * @return the copy action.
   */
  Action createCopyAction() {
    Action copyAction = clipboard.getCopyAction();
    putIcon(copyAction, DemoBase.class.getResource("resource/copy.png"));
    copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy");
    
    view.getCanvasComponent().getActionMap().put("COPY", copyAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "COPY");

    return copyAction;
  }

  /**
   * Callback method that creates and configures the paste action.
   * @return the paste action.
   */
  Action createPasteAction() {
    Action pasteAction = clipboard.getPasteAction();
    putIcon(pasteAction, DemoBase.class.getResource("resource/paste.png"));
    pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste");

    view.getCanvasComponent().getActionMap().put("PASTE", pasteAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), "PASTE");

    return pasteAction;
  }

  /**
   * Creates a pre-configured {@link y.view.Overview}.
   * @return the pre-configured overview.
   */
  private Overview createOverview() {
    final Overview overview = new Overview(view);
    //blurs the part of the graph which can currently not be seen
    overview.putClientProperty("Overview.PaintStyle", "Funky");
    //allows zooming from within the overview
    overview.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
    //provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation with arrow keys)
    overview.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
    //determines how to differ between the part of the graph that can currently be seen, and the rest
    overview.putClientProperty("Overview.Inverse", Boolean.TRUE);
//    overview.setMinimumSize(new Dimension(200, 100));

    overview.setPreferredSize(new Dimension((int) (0.2 * (double) contentPane.getWidth()), 200));
    return overview;
  }

  /** Creates a panel which contains the specified component and a title on top of it. */
  protected JPanel createTitledPanel(JComponent content, String title) {
    JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    label.setBackground(new Color(231, 219, 182));
    label.setOpaque(true);
    label.setForeground(Color.DARK_GRAY);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setFont(label.getFont().deriveFont(13.0f));

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(label, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private void putIcon(Action action, URL url) {
    if (url != null) {
      action.putValue(Action.SMALL_ICON, new ImageIcon(url));
    }
  }

  public static void main(String[] args) {

    EventQueue.invokeLater(new Runnable() {

      public void run() {
        initLnF();
        final FlowchartDemo demo = new FlowchartDemo();
        demo.start("Flowchart Editor");
      }
    }
    );

  }
}