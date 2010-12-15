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

import java.awt.*;
import javax.swing.*;

import y.view.Graph2DView;
import y.view.EditMode;

/**
 * The yFiles view says "Hello World."
 * <br>
 * Demonstrates basic usage of {@link y.view.Graph2DView}, the yFiles graph
 * viewer component, and shows how to provide editing support through
 * {@link y.view.EditMode}.
 */
public class SimpleDemo extends JPanel 
{
  Graph2DView view;
  
  public SimpleDemo()
  {
    setLayout(new BorderLayout());  
    view = new Graph2DView();
    EditMode mode = new EditMode();
    view.addViewMode(mode);
    add(view);
  }

  public void start()
  {
    JFrame frame = new JFrame(getClass().getName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo( final JRootPane rootPane )
  {
    rootPane.setContentPane(this);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        (new SimpleDemo()).start();
      }
    });
  }
}


      
