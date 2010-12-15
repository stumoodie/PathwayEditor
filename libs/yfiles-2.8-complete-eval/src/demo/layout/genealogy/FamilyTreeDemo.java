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
package demo.layout.genealogy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import demo.layout.genealogy.iohandler.GedcomHandler;
import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.algo.Bfs;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyType;
import y.layout.genealogy.FamilyTreeLayouter;
import y.module.FamilyTreeLayoutModule;
import y.util.D;
import y.util.GraphHider;
import y.view.BendList;
import y.view.BevelNodePainter;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.GenericEdgePainter;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NavigationMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShinyPlateNodePainter;


/**
 * This Demo shows how to use the FamilyTreeLayouter.
 * <br>
 * <b>Usage:</b>
 * <br>
 * Load a Gedcom file with "Load..." from the "File" menu. The gedcom file is converted on the fly into GraphML
 * and loaded into the graph by the {@link demo.layout.genealogy.iohandler.GedcomHandler}. After loading,
 * the graph will be laid out by the {@link y.layout.genealogy.FamilyTreeLayouter}.
 * NOTE: you will find some sample files in your &lt;src&gt;/demo/view/layout/genealogy/samples folder
 * <br>
 * To re-layout the graph press the "layout" button. An options dialog will open where you can modify
 * some basic and advanced options. Clicking "OK" will induce a new layout with the new settings.
 * <br>
 * To load some sample graphs which are provided with this demo select one from the "Examples" ComboBox.
 * There are four different family trees with different complexity provided.
 * NOTE: for this feature, Gedcom files (ending: .ged) must be exported as resources.
 * <br/>
 * Clicking on a node will collapse the graph to two generations around the clicked node. The "Show all" button
 * will expand the graph again
 * <br/>
 * <br/>
 * API usage:
 * <br>
 * The FamilyTreeLayouter needs to distinguish between family nodes, i.e. nodes representing a FAM entry
 * in the Gedcom file, and nodes representing individuals (i.e. persons, INDI entries). To do so,
 * a data provider with the key {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE} has to be registered
 * to the graph. This data provider will return a String which denotes nodes representing individuals.
 * In this demo, this is achieved by comparing the node's background color with the color, family nodes are
 * painted with (Color.black).
 * <br>
 * For writing, the GedcomHandler needs to distinguish between family nodes and individuals as well
 * as between male and female individuals. To do so, a data provider with the key
 * {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE} has to be registered to the graph.
 * These data provider will return a String which can be used to distinguish between families, male and female
 * individual.
 * In this demo, this is achieved by comparing the node's background color with predefined values.
 * As this demo is a viewer without editing capabilities, the export function is not implemented in the GUI
 * (Although it is implemented in the code: see class ExportAction)
 * <br>
 * <br>
 * Note that the GraphML format offers the possibility to extract additional information from the original file.
 * In this demo, String attributes whether the node represents a family or a male or female individual are mapped
 * to the graph and can be used as data providers for the layouter and the GedcomHandler.
 * <br>
 * To use this feature, modify the code as indicated in the source code. The methods to modify are
 * {@link FamilyTreeDemo#loadGedcom} and {@link GedcomHandler#read}
 */

public class FamilyTreeDemo extends DemoBase {
  private static final int PREFERRED_FONT_SIZE = 18;

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        new FamilyTreeDemo().start();
      }
    });
  }
  
  /**
   * Add a NavigationMode
   */
  protected void registerViewModes() {
    view.addViewMode(new NavigationMode() {
      public void mouseClicked(double x, double y) {
        super.mouseClicked(x, y);
        if(getHitInfo(x,y).getHitNode() != null) {
          moveToCenter(getLastHitInfo().getHitNode());
        }
      }
    });
  }
  
  private GraphHider graphHider;


  /**
   * Centers the graph on the given node and hides all nodes which are more than 2 generations away.
   * @param newCenter The node to be the new center of the graph
   */
  private void moveToCenter(final Node newCenter) {

    graphHider.unhideAll();     // undo the previous selection

    // the list for all nodes that are to hide: initially filled with all nodes in the graph
    NodeList toHide = new NodeList(view.getGraph2D().nodes());
    NodeMap nodeMap = view.getGraph2D().createNodeMap();
    // search the graph for the newCenter's adjacent 5 generations
    // NOTE: families also count as one generation, so 5 "graph" generations correspond to 2 "genealogic" generations
    // These will be returned arranged in layers in the Array of nodeLists
    NodeList[] layers = Bfs.getLayers(view.getGraph2D(), new NodeList(newCenter), false, nodeMap, 5);
    view.getGraph2D().disposeNodeMap(nodeMap);
    // remove these nodes from the toHide list (which initially contains all nodes of the graph)
    for (int i = 0; i < layers.length; i++) {
      NodeList layer = layers[i];
      toHide.removeAll(layer);
    }

    // hide all nodes in the toHide list
    graphHider.hide(toHide);

    // run a layout for the remaining nodes
    getLayoutModule().start(view.getGraph2D());
  }

  /**
   * Action to run a layout.
   */
  protected class ShowAllAction extends AbstractAction {


    public ShowAllAction() {
      super("Show all");
    }

    /**
     * Invoked when an action occurs. Displays an options dialog and runs a layout with the selected parameters
     * if the user clicks ok. Uses a module for this task.
     */
    public void actionPerformed(ActionEvent e) {
      if (graphHider != null) {
        graphHider.unhideAll();
      }
      getLayoutModule().mainrun();
      view.fitContent();
    }

/* Use this if you want to configure the layouter programmatically */
//    public void actionPerformed(ActionEvent e) {
//      FamilyTreeLayouter ftl = new FamilyTreeLayouter();
//       try {
//         ftl.doLayout( view.getGraph2D() );
//       } catch (Exception e1) {
//           D.show( e1 );
//       }
//    }


  }


  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar jToolBar = super.createToolBar();
    jToolBar.addSeparator();
    jToolBar.add(new LayoutAction());
    //jToolBar.add(new ExportAction());
    JComboBox jcb = createExampleComboBox();
    if (jcb != null) {
      jToolBar.add(jcb);
    }
    jToolBar.add(new ShowAllAction());
    return jToolBar;
  }

  /**
   * Creates a ComboBox to select the provided samples.
   * @return The sample ComboBox or null, if no samples were found or provided.
   */
  private JComboBox createExampleComboBox() {
    String fqResourceName = FamilyTreeDemo.class.getPackage().getName().replace('.', '/') + "/samples/KENNEDY.GED";


    URL resource = getClass().getResource("samples/KENNEDY.GED");
    if (resource == null) {
      D.showError("Cannot load example files: missing resource " + fqResourceName + "\n" +
          "Please ensure that your IDE recognizes \"*.ged\" files as resource files. \n" +
          "Meanwhile you can load the sample files via the \"File/Load\" menu from the source folder of your distribution.");
      return null;
    }
    String name = resource.getFile();
    final String dirName = name.substring(0, name.lastIndexOf('/'));

    final String[] dir = new File(dirName).list(new FilenameFilter() {
      public boolean accept(File d, String s) {
        return s.toLowerCase().endsWith(".ged");
      }
    });
    if (dir == null) {
      D.showError("Cannot load example files: " + dirName + " not found");
      return null;
    }

    final JComboBox jcb = new JComboBox(dir);
    jcb.setMaximumSize(new Dimension(200, 100));
    jcb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String fileName = (String) jcb.getSelectedItem();
        loadGedcom(dirName + System.getProperty("file.separator") + fileName);
      }
    });

    if (jcb.getItemCount() > 0) {
      jcb.setSelectedIndex(0);
    }
    
    return jcb;
  }

  /**
   * Loads a gedcom file with the provided filename into the editor.
   */
  private void loadGedcom(String name) {

    Cursor oldCursor = view.getViewCursor();
    
    final Graph2D graph = view.getGraph2D();
    if (graphHider != null) {
      graphHider.unhideAll();
    }
    graph.clear();
    GedcomHandler gh = new GedcomHandler();
    GraphMLIOHandler delegate = new GraphMLIOHandler();
    NodeMap nodeTypeMap = graph.createNodeMap();
    graph.addDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE, nodeTypeMap);
    delegate.getGraphMLHandler().addInputDataAcceptor("GedcomType", nodeTypeMap, KeyType.STRING);

    gh.setReaderDelegate(delegate);
    try {
      gh.read(graph, name);
    } catch (IOException e1) {
      D.show(e1);
    }

    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      final NodeRealizer realizer = graph.getRealizer(node);
      if (realizer.labelCount() > 0) {
        final NodeLabel label = realizer.getLabel();
        if (label.getFontSize() < PREFERRED_FONT_SIZE) {
          label.setFontSize(PREFERRED_FONT_SIZE);

          if (label.getWidth() + 2*label.getDistance() > realizer.getWidth()) {
            realizer.setWidth(label.getWidth() + 2*label.getDistance() + 8);
          }
        }
      }
    }

    try {
      getLayoutModule().start(view.getGraph2D());
    } catch (Exception e1) {
      D.show(e1);
    }
  }


  /**
   * Overrides the default method which creates the loadGedcom entry in the file menu to import a gedcom file
   * rather than to loadGedcom a graph.
   * @return A new instance of ImportAction
   */
  protected Action createLoadAction() {
    return new ImportAction();
  }


  /**
   * Action that loads a Gedcom file.
   */
  protected class ImportAction extends AbstractAction {
    JFileChooser chooser;

    public ImportAction() {
      super("Load...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".ged");
          }

          public String getDescription() {
            return "Gedcom files";
          }
        });
      }

      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        loadGedcom(chooser.getSelectedFile().toString());
      }
    }


  }

  private FamilyTreeLayoutModule getLayoutModule() {
    if (ftlm == null) {
      ftlm = new FamilyTreeLayoutModule();       
      ftlm.getOptionHandler().set("MALE_COLOR", DemoDefaults.DEFAULT_CONTRAST_COLOR);
      ftlm.getOptionHandler().set("FEMALE_COLOR", DemoDefaults.DEFAULT_NODE_COLOR);      
    }
    return ftlm;
  }

  private FamilyTreeLayoutModule ftlm;


  /**
   * Action to run a layout.
   */
  protected class LayoutAction extends AbstractAction {


    public LayoutAction() {
      super("Layout");
    }

    /**
     * Invoked when an action occurs. Displays an options dialog and runs a layout with the selected parameters
     * if the user clicks ok. Uses a module for this task.
     */
    public void actionPerformed(ActionEvent e) {
      if (getLayoutModule().getOptionHandler().showEditor()) {        
        getLayoutModule().start(view.getGraph2D());        
      }
    }

/* Use this if you want to configure the layouter programmatically */
//    public void actionPerformed(ActionEvent e) {
//      FamilyTreeLayouter ftl = new FamilyTreeLayouter();
//       try {
//         ftl.doLayout( view.getGraph2D() );
//       } catch (Exception e1) {
//           D.show( e1 );
//       }
//    }


  }

  /**
   * Action to export the graph into a gedcom file
   * NOTE: This action is not added to the toolbar
   */
  protected class ExportAction extends AbstractAction {

    private JFileChooser chooser;

    public ExportAction() {
      super("Export");
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {

      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory()
                || f.getName().toLowerCase().endsWith(".ged");
          }

          public String getDescription() {
            return "Gedcom files";
          }
        });
      }
      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        if (!name.toLowerCase().endsWith(".ged")) {
          name = name + ".ged";
        }

        GedcomHandler gh = new GedcomHandler();
        final Graph2D graph = view.getGraph2D();

        // Write the graph using the GedcomHandler
        try {
          gh.write(graph, name);
        } catch (IOException e1) {
          D.show(e1);
        }
      }
    }

  }

  //////////////////////////////////////// Optical improvements :-) /////////////////////////////////////////////////

  /**
   * Initialize the node and edge style
   */
  protected void initialize() {

    // Use a BevelNodePainter for the Individuals
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map implementationsMap = factory.createDefaultConfigurationMap();
    ShinyPlateNodePainter spnp = new ShinyPlateNodePainter();
    spnp.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, spnp);
    factory.addConfiguration("Individual", implementationsMap);

    // Use a BevelNodePainter with rounded corners for the families
    implementationsMap = factory.createDefaultConfigurationMap();
    BevelNodePainter painter = new BevelNodePainter();
    painter.setRadius(10);
    painter.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
    factory.addConfiguration("Family", implementationsMap);

    // Use a custom edge realizer
    GenericEdgeRealizer.Factory edgeFactory = GenericEdgeRealizer.getFactory();
    implementationsMap = edgeFactory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new CustomEdgePainter());
    edgeFactory.addConfiguration("Edge", implementationsMap);
    GenericEdgeRealizer ger = new GenericEdgeRealizer();
    ger.setConfiguration("Edge");
    ger.setLineColor(new Color(0x808080));
    ger.setLineType(LineType.LINE_2);
    view.getGraph2D().setDefaultEdgeRealizer(ger);

    // Crossing/Bridges: Vertical edges over horiziontal edges display gaps in horizontal edges
    BridgeCalculator bc = new BridgeCalculator();
    bc.setCrossingStyle(BridgeCalculator.CROSSING_STYLE_GAP);
    bc.setCrossingMode(BridgeCalculator.CROSSING_MODE_ORDER_INDUCED);
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bc);

    graphHider = new GraphHider(view.getGraph2D());

    
  }


  /**
   * A custom EdgePainter implementation that draws the edge path 3D-ish and adds
   * a drop shadow also. (see demo.view.realizer.GenericEdgePainterDemo)
   */
  static final class CustomEdgePainter extends GenericEdgePainter {

    protected GeneralPath adjustPath(EdgeRealizer context, BendList bends, GeneralPath path,
                                     BridgeCalculator bridgeCalculator,
                                     boolean selected) {
      if (bridgeCalculator != null) {
        GeneralPath p = new GeneralPath();
        try {
          PathIterator pathIterator = bridgeCalculator.insertBridges(path.getPathIterator(null, 1.0d));
          p.append(pathIterator, true);
          return super.adjustPath(context, bends, p, bridgeCalculator, selected);
        } finally {
        }
      } else {
        return super.adjustPath(context, bends, path, bridgeCalculator, selected);
      }
    }


    protected void paintPath(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      Stroke s = gfx.getStroke();
      Color oldColor = gfx.getColor();
      if (s instanceof BasicStroke) {
        Color c;
        if (selected) {
          initializeSelectionLine(context, gfx, selected);
          c = gfx.getColor();
        } else {
          initializeLine(context, gfx, selected);
          c = gfx.getColor();
          gfx.setColor(new Color(128, 128, 128, 40));
          gfx.translate(4, 4);
          gfx.draw(path);
          gfx.translate(-4, -4);
        }
        Color newC = selected ? Color.RED : c;
        gfx.setColor(new Color(128 + newC.getRed() / 2, 128 + newC.getGreen() / 2, 128 + newC.getBlue() / 2));
        gfx.translate(-1, -1);
        gfx.draw(path);
        gfx.setColor(new Color(newC.getRed() / 2, newC.getGreen() / 2, newC.getBlue() / 2));
        gfx.translate(2, 2);
        gfx.draw(path);
        gfx.translate(-1, -1);
        gfx.setColor(c);
        gfx.draw(path);
        gfx.setColor(oldColor);
      } else {
        gfx.draw(path);
      }
    }
  }
}
