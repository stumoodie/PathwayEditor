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
package demo.io.graphml;


import demo.view.DemoDefaults;
import y.io.XmlXslIOHandler;
import y.util.D;
import y.view.Graph2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.stream.StreamSource;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 This demo shows how XML files can imported as
 GraphML by the means of an XSLT stylesheet.
 Sample stylesheets for the following XML data are provided:
 <ul>
 <li><a href="resources/xsl/ant2graphml.xsl">Ant build scripts</a></li>
 <li><a href="resources/xsl/owl2graphml.xsl">OWL web ontology data</a></li>
 <li><a href="resources/xsl/xmltree2graphml.xsl">the XML tree structure</a></li>
 </ul>
 */
public class XmlXslDemo extends GraphMLDemo {
  private String[][] sampleFiles;

  public XmlXslDemo() {
    graphMLPane.setEditable(false);
    if (sampleFiles != null) {
      loadXml(getClass().getResource(sampleFiles[0][0]), getClass().getResource(sampleFiles[0][1]));
    }
  }

  protected JComboBox createSampleGraphComboBox() {
    sampleFiles = new String[][]{
        {"resources/xml/ant-build.xml",
            "resources/xsl/ant2graphml.xsl"},
        {"resources/xml/food.owl",
            "resources/xsl/owl2graphml.xsl"},
        {"resources/xml/food.owl",
            "resources/xsl/xmltree2graphml.xsl"},
    };

    final JComboBox box = new JComboBox();
    for (int i = 0; i < sampleFiles.length; ++i) {
      String xml = sampleFiles[i][0];
      String xsl = sampleFiles[i][1];
      box.addItem(xml.substring(xml.lastIndexOf('/') + 1) + " + " + xsl.substring(xsl.lastIndexOf('/') + 1));
    }
    box.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = box.getSelectedIndex();
        final URL xml = getClass().getResource(sampleFiles[index][0]);
        final URL xsl = getClass().getResource(sampleFiles[index][1]);
        loadXml(xml, xsl);
      }
    });
    box.setMaximumSize(new Dimension(200, 100));
    return box;
  }


  protected void loadInitialGraph() {
    //No initial GraphML graph...
  }

  public void loadXml(URL xmlResource, URL xslResource) {
    Graph2D graph = view.getGraph2D();
    try {
      XmlXslIOHandler ioh = new XmlXslIOHandler(createGraphMLIOHandler());
      ioh.setXslSource(new StreamSource(xslResource.openStream()));
      ioh.read(graph, xmlResource);
      view.fitContent();
      view.updateView();
    }
    catch (IOException ioe) {
      D.show(ioe);
    }
    finally {
      graphMLPane.updateGraphMLText(graph);
    }
  }

  protected Action createLoadAction() {
    return new AbstractAction("Load...") {

      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        URL xslResource = null;
        URL xmlResource = null;
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("XML input");

        if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
          try {
            xmlResource = chooser.getSelectedFile().toURI().toURL();
          } catch (MalformedURLException urlex) {
            urlex.printStackTrace();
          }
        }
        if (xmlResource != null) {
          chooser.setAcceptAllFileFilterUsed(false);
          chooser.setDialogTitle("XSL stylesheet");
          chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
              return f.isDirectory() || f.getName().endsWith(".xsl");
            }

            public String getDescription() {
              return "XML stylesheets (.xsl)";
            }
          });

          if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
            try {
              xslResource = chooser.getSelectedFile().toURI().toURL();
              if (xslResource != null) {
                loadXml(xmlResource, xslResource);
              }
            } catch (MalformedURLException urlex) {
              urlex.printStackTrace();
            }

          }
        }
      }

    };
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new XmlXslDemo()).start();
      }
    });
  }
}
