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
package demo.view.applet;

import javax.swing.JApplet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class represents a simple graph editor applet. The applet can be used inside a web browser with
 * a Java 2 plugin (version >= 1.4) installed.
 * <p>
 * This applet supports the applet parameter "graphsource" which allows to specify the graph that is initially
 * displayed by the applet. The graph needs to be in GraphML format. URLs are specified relative to
 * the document base of the applet.
 * </p>
 * <p>
 * To compile and deploy the applet it is best to use the Ant build script "build.xml" located in this directory.
 * It compiles the application classes, jars them as "application.jar" in this directory and also copies "y.jar" into this directory.
 * Once these Jar files are in place, the applet can be launched by opening the included HTML page
 * "applet.html" with your browser.
 * </p>
 * <p>
 * This applet demo has been successfully tested with IE7, IE6, Firefox 3.5, Chrome 3 and Safari 4.
 * </p>
 */
public class AppletDemo extends JApplet {
  
  DemoEditor demoEditor;

  /**
   * Mandatory default constructor for an applet.
   */
  public AppletDemo() {
  }

  /**
   * Applet initialization. Create the application GUI.
   */
  public void init() {
    super.init();
    demoEditor = new DemoEditor();
    getRootPane().setContentPane(demoEditor);
    getRootPane().setJMenuBar(demoEditor.createMenuBar());
  }

  /**
   * Start the applet. Try to load the graph given by applet parameter "graphsource".
   */
  public void start() {
    super.start();

    String graphSource = getParameter("graphsource");

    if (graphSource != null) {
      try {
        URL graphURL = new URL(getDocumentBase(), graphSource);
        try {
          URLConnection urlConnection = graphURL.openConnection();
          urlConnection.connect();
        } catch (IOException ioex) {
          //try classpath if resource node located at document base
          graphURL = getClass().getResource(graphSource);
        }
        if (graphURL != null) {
          demoEditor.loadAndDisplayGraph(graphURL);
        }
      } catch (MalformedURLException muex) {
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Returns applet parameter information.
   */
  public String[][] getParameterInfo() {
    return new String[][]{
        // Parameter Name     Kind of Value   Description
        {"graphsource", "URL", "an URL pointing to a diagram in GraphML format"}
    };
  }
}
