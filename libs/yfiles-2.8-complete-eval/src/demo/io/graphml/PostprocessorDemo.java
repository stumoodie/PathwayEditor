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
import y.io.GraphMLIOHandler;
import y.io.graphml.graph2d.PostprocessorOutputHandler;
import y.module.YModule;
import y.util.D;
import y.util.DataProviderAdapter;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This demo centers around postprocessing actions that can be specified
 * within a GraphML file. These actions allow to process
 * the parsed graph structure before it gets returned by the GraphML parser.
 * <p>
 * A GraphML processor can be any instance of the yFiles module class YModule.
 * The configuration of a processor is done by changing the values
 * managed by the associated OptionHandler instance. This demo allows to configure
 * a processor interactively. Furthermore, it can be used to display the GraphML
 * representation of a processor module configuration.
 * When saving a file the XML representation of the current processor will be added
 * to the output file as well. When loading this file again,
 * the postprocessor will perform its action.
 * </p>
 */
public class PostprocessorDemo extends GraphMLDemo {

  private YModule processorModule;

  protected void loadInitialGraph() {
    //register a DataProvider that returns the selected
    //processor module. This dataprovider is used by
    //PostprocessorOutputHandler to lookup the postprocessors
    //it should serialize.
    view.getGraph2D().addDataProvider(PostprocessorOutputHandler.PROCESSORS_DPKEY,
        new DataProviderAdapter() {
          public Object get(Object graph) {
            return processorModule;
          }
        });

    loadGraph("resources/postprocessors/ant-build.graphml");
  }

  protected JToolBar createToolBar() {
    JToolBar jtb = super.createToolBar();
    jtb.addSeparator();

    //a combo box that contains the class names of available
    //postprocessors.
    final JComboBox combo = new JComboBox(new String[]{
        "y.module.IncrementalHierarchicLayoutModule",
        "y.module.SmartOrganicLayoutModule",
        "demo.io.graphml.NodeSizeAdapter"
    }
    );
    combo.setMaximumSize(new Dimension(300, 100));
    combo.setEditable(true);
    jtb.add(combo);
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        String className = combo.getSelectedItem().toString();
        try {
          processorModule = (YModule) Class.forName(className).newInstance();
        } catch (Exception ex) {
          D.showError("Can't create instance of class " + className);
        }
      }
    });
    combo.setSelectedIndex(0);

    jtb.add(new ConfigureProcessorAction());
    jtb.add(new ApplyProcessorAction());

    return jtb;
  }

  protected String[] getSampleFiles() {
    return new String[]{
        "resources/postprocessors/ant-build.graphml",
        "resources/postprocessors/food-owl.graphml",
    };
  }

  /**
   * Creates a GraphMLIOHandler that has additional output support for
   * GraphML postprocessors.
   *
   * Note that input support for PostProcessors is registered by default and
   * need not be added manually.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioh = super.createGraphMLIOHandler();
    ioh.getGraphMLHandler().addOutputHandlerProvider(new PostprocessorOutputHandler());
    return ioh;
  }

  /**
   * Actions that allows to configure the selected postprocessor interactively.
   */
  class ConfigureProcessorAction extends AbstractAction {
    ConfigureProcessorAction() {
      super("Configure...");
    }

    public void actionPerformed(ActionEvent ev) {
      if (processorModule != null) {
        if (processorModule.getOptionHandler().showEditor()) {
          processorModule.getOptionHandler().commitValues();
        }
      }
    }
  }

  /**
   * Actions that applies the selected processor on the displayed graph.
   */
  class ApplyProcessorAction extends AbstractAction {
    ApplyProcessorAction() {
      super("Apply");
    }

    public void actionPerformed(ActionEvent ev) {
      if (processorModule != null) {
        processorModule.start(view.getGraph2D());
        view.updateView();
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new PostprocessorDemo()).start();
      }
    });
  }
}
