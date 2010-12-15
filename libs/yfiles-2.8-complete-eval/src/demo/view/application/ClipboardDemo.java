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
import y.view.Graph2DClipboard;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * This class demonstrates how to use the yFiles clipboard
 * functionality to cut, copy and paste parts of a graph.
 */
public class ClipboardDemo extends DemoBase
{
  Action cutAction;
  Action copyAction;
  Action pasteAction;

  public ClipboardDemo()
  {
    view.getCanvasComponent().getActionMap().put("CUT", cutAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_X,  KeyEvent.CTRL_MASK),"CUT");

    view.getCanvasComponent().getActionMap().put("COPY", copyAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), "COPY");

    view.getCanvasComponent().getActionMap().put("PASTE", pasteAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK), "PASTE");

    loadGraph(getClass().getResource("resource/ClipboardDemo.graphml"));
  }

  protected void registerViewActions() {
    super.registerViewActions();
    //create new clipboard.
    Graph2DClipboard clipboard = new Graph2DClipboard(view);

    //get Cut action from clipboard
    cutAction = clipboard.getCutAction();
    final URL cutResource = DemoBase.class.getResource("resource/cut.png");
    if (cutResource != null) {
      cutAction.putValue(Action.SMALL_ICON, new ImageIcon(cutResource));
    }
    cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut");

    //get Copy action from clipboard
    copyAction = clipboard.getCopyAction();
    final URL copyResource = DemoBase.class.getResource("resource/copy.png");
    if (copyResource != null) {
      copyAction.putValue(Action.SMALL_ICON, new ImageIcon(copyResource));
    }
    copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy");

    //get Paste action from clipboard
    pasteAction = clipboard.getPasteAction();
    final URL pasteResource = DemoBase.class.getResource("resource/paste.png");
    if (pasteResource != null) {
      pasteAction.putValue(Action.SMALL_ICON, new ImageIcon(pasteResource));
    }
    pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste");
  }

  protected JToolBar createToolBar() {
    JToolBar jtb = super.createToolBar();
    jtb.addSeparator();
    jtb.add(cutAction);
    jtb.add(copyAction);
    jtb.add(pasteAction);
    return jtb;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new ClipboardDemo()).start();
      }
    });
  }
}
